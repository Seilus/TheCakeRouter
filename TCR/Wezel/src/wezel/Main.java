package wezel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main{

	public static void main(String[] args) throws IOException {
		int reportPort=9915;
		int sendPort=9014;
		System.out.println("Please provide the IP address that you wish to use during the connection:");
		Scanner keyScan=new Scanner(System.in);
		String ownIP=keyScan.nextLine();
		keyScan.close();

		final DatagramSocket socket = new DatagramSocket(reportPort); 
		final DatagramSocket fileSocket = new DatagramSocket(sendPort); 
        ExecutorService runnableExec = Executors.newFixedThreadPool(3);
        LinkedList<InetAddress> droga=new LinkedList<InetAddress>();
		while(true){
			//raportuje się jako węzeł do użycia w przesyłaniu
			Runnable reporting = new Runnable() {
				@Override
				public void run() {
					try {
						DatagramPacket senderIPData = new DatagramPacket(new byte[4], 4);
						socket.receive(senderIPData);
						InetAddress senderIP=InetAddress.getByAddress(senderIPData.getData());
						DatagramPacket response= new DatagramPacket(InetAddress.getByName(ownIP).getAddress(), 4);
						response.setAddress(senderIP);
						response.setPort(reportPort);
						socket.send(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			};
			runnableExec.submit(reporting);
			//przesyła dalej plik, listę węzłów
			Runnable sending=new Runnable(){
				@Override
				public void run() {
					try {
						DatagramPacket noOfNodes=new DatagramPacket(new byte[1], 1);
						fileSocket.receive(noOfNodes);
						int number=(int) noOfNodes.getData()[0];
						DatagramPacket nodeIP=new DatagramPacket(new byte[4], 4);
						fileSocket.receive(nodeIP);
						InetAddress returnAddress=nodeIP.getAddress();
						fileSocket.receive(nodeIP);
						InetAddress nextAddress=nodeIP.getAddress();
						
						droga.add(InetAddress.getByName(ownIP));
						for (int i=0; i<(number-2); i++){
							fileSocket.receive(nodeIP);
							droga.add(nodeIP.getAddress());
						}
						DatagramPacket sizeOfFile=new DatagramPacket(new byte[4], 4);
						fileSocket.receive(sizeOfFile);
						int fileSize = 0;
					    for (int i=0; i<4; i++) {
					      fileSize = ( fileSize << 8 ) - Byte.MIN_VALUE + (int) sizeOfFile.getData()[i];
					    }
					    DatagramPacket sentFile=new DatagramPacket(new byte[fileSize], fileSize);
					    fileSocket.receive(sentFile);
					    byte nodes=(byte)droga.size();
					    byte[] nodesNo=new byte[1];
					    nodesNo[1]=nodes;
					    noOfNodes.setData(nodesNo);
					    noOfNodes.setAddress(nextAddress);
					    noOfNodes.setPort(sendPort);
					    fileSocket.send(noOfNodes);
					    for(int i=0; i<droga.size(); i++){
							DatagramPacket nodeSend=new DatagramPacket(droga.get(i).getAddress(), droga.get(i).getAddress().length);
							nodeSend.setAddress(nextAddress);
							nodeSend.setPort(sendPort);
							fileSocket.send(nodeSend);
					    }
					    sizeOfFile.setAddress(nextAddress);
					    sizeOfFile.setPort(sendPort);
					    fileSocket.send(sizeOfFile);
					    sentFile.setAddress(nextAddress);
					    sentFile.setPort(sendPort);
					    fileSocket.send(sentFile);
					    
					    DatagramPacket returnInfo=new DatagramPacket(new byte[1],1);
					    fileSocket.receive(returnInfo);
					    returnInfo.setAddress(returnAddress);
					    returnInfo.setPort(sendPort);
					    fileSocket.send(returnInfo);
					    }
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			};
			runnableExec.submit(sending);
		}
	}

}