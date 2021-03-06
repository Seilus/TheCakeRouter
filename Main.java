package wezel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main{

	public static void main(String[] args) throws IOException {
		int reportPort=9915;
		int sendPort=9015;
		System.out.println("Please provide the IP address that you wish to use during the connection:");
		Scanner keyScan=new Scanner(System.in);
		String ownIP=keyScan.nextLine();
		keyScan.close();

		final DatagramSocket soc = new DatagramSocket(reportPort); 
		final DatagramSocket filSoc = new DatagramSocket(sendPort); 
        ExecutorService runnableExec = Executors.newFixedThreadPool(1);
        LinkedList<InetAddress> droga=new LinkedList<InetAddress>();
        int i=0;
		while(i<5){
			i++;
			
			//raportuje się jako węzeł do użycia w przesyłaniu
			Runnable reporting = new Runnable() {
				@Override
				public void run() {
					try {
						DatagramPacket senderIPData = new DatagramPacket(new byte[4], 4);
						soc.receive(senderIPData);
						System.out.println(InetAddress.getByAddress(senderIPData.getData()));
						InetAddress senderIP=InetAddress.getByAddress(senderIPData.getData());
						DatagramPacket response= new DatagramPacket(InetAddress.getByName(ownIP).getAddress(), 4);
						response.setAddress(senderIP);
						response.setPort(reportPort);
						soc.send(response);
						System.out.println(ownIP.toString());
						//socket.close();
					} catch (IOException e) {
					e.printStackTrace();
					}
					
				}
			};
		
			runnableExec.submit(reporting);
		
		
				//przesyła dalej plik, listę węzłów
			Runnable sending = new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println("we're doing things!");
						DatagramPacket noOfNodes = new DatagramPacket(new byte[1], 1);
						filSoc.receive(noOfNodes);
						System.out.println("we're doing things2!");
						int number = (int) noOfNodes.getData()[0];
						System.out.println(number);
						System.out.println("we're doing things3!");
						DatagramPacket nodeIP = new DatagramPacket(new byte[4], 4);
						filSoc.receive(nodeIP);
						InetAddress returnAddress = nodeIP.getAddress();
						System.out.println("return add:" + returnAddress.toString());
						filSoc.receive(nodeIP);
						System.out.println("we're doing things4!");
						InetAddress nextAddress = nodeIP.getAddress();
						
						System.out.println("next add:" + nextAddress.toString());
						droga.add(InetAddress.getByName(ownIP));
						for (int i = 0; i < (number - 2); i++) {
							filSoc.receive(nodeIP);
							droga.add(nodeIP.getAddress());
						}
						DatagramPacket sizeOfFile = new DatagramPacket(new byte[4], 4);
						filSoc.receive(sizeOfFile);
						int fileSize = 0;
						for (int i = 0; i < 4; i++) {
							fileSize = (fileSize << 8) - Byte.MIN_VALUE + (int) sizeOfFile.getData()[i];
						}
						DatagramPacket sentFile = new DatagramPacket(new byte[fileSize], fileSize);
						filSoc.receive(sentFile);
						byte nodes = (byte) droga.size();
						byte[] nodesNo = new byte[1];
						nodesNo[1] = nodes;
						noOfNodes.setData(nodesNo);
						noOfNodes.setAddress(nextAddress);
						noOfNodes.setPort(sendPort);
						filSoc.send(noOfNodes);
						for (int i = 0; i < droga.size(); i++) {
							DatagramPacket nodeSend = new DatagramPacket(droga.get(i).getAddress(),
									droga.get(i).getAddress().length);
							nodeSend.setAddress(nextAddress);
							nodeSend.setPort(sendPort);
							filSoc.send(nodeSend);
						}
						sizeOfFile.setAddress(nextAddress);
						sizeOfFile.setPort(sendPort);
						filSoc.send(sizeOfFile);
						sentFile.setAddress(nextAddress);
						sentFile.setPort(sendPort);
						filSoc.send(sentFile);
						DatagramPacket returnInfo = new DatagramPacket(new byte[1], 1);
						filSoc.receive(returnInfo);
						returnInfo.setAddress(returnAddress);
						returnInfo.setPort(sendPort);
						filSoc.send(returnInfo);
						//    fileSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			runnableExec.submit(sending);
		}	
	}
}
