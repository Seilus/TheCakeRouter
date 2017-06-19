package odbieranie;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) throws IOException {

		int usedPort=9015;
		final DatagramSocket fileSocket = new DatagramSocket(usedPort); 
		ExecutorService runnableExec = Executors.newFixedThreadPool(3);

		while(true){
			//Runnable reporting = new Runnable() {
			//	@Override
				//public void run() {
					
						DatagramPacket initialData;
						initialData = new DatagramPacket(new byte[4], 4);
						InetAddress senderIP=null;
						System.out.println("koko");
						try {
							
							fileSocket.receive(initialData);
							senderIP = InetAddress.getByAddress(initialData.getData());
							System.out.println("pierwszy try");
							System.out.println(senderIP.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						try {
							fileSocket.receive(initialData);
							System.out.println("got array size");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int fileSize=0;
						System.out.println(initialData.getData());
						System.out.println(initialData.getLength());
						ByteBuffer buffer=ByteBuffer.wrap(initialData.getData(), 0, 4);
						//for (int i=0; i<4; i++) {
						//  fileSize = ( fileSize << 8 ) - Byte.MIN_VALUE + (int) initialData.getData()[i]; 
						//  System.out.println(initialData.getData()[i]);
						//}
						fileSize=buffer.getInt();
						System.out.println(fileSize);
					    try {
							DatagramPacket file=new DatagramPacket(new byte[fileSize], fileSize);
							System.out.println("zapisywanie");
							fileSocket.receive(file);
							System.out.println("Provide the path for the file:");
							Scanner keyScan=new Scanner(System.in);
							String filePath=keyScan.nextLine();
							keyScan.close();
							FileOutputStream fileStream = new FileOutputStream(filePath);
							fileStream.write(file.getData());
							fileStream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						DatagramPacket response= new DatagramPacket(new byte[1], 1);
						byte resp=(byte)9;
						byte[] respon=new byte[1];
						respon[0]=resp;
						response.setData(respon);
						try {
							response.setAddress(senderIP);
							response.setPort(usedPort);
							fileSocket.send(response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

				
				//}
		//	};
			//runnableExec.submit(reporting);
		}
		
	}	
}
