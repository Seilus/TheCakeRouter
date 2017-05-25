package wezel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		while(true){
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
			
			Runnable sending=new Runnable(){
				@Override
				public void run() {
					try {
						DatagramPacket noOfNodes=new DatagramPacket(new byte[1], 1);
						fileSocket.receive(noOfNodes);
						int number=(int) noOfNodes.getData()[0];
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
