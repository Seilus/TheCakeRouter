package odbieranie;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) throws IOException {

		int usedPort=9014;
		final DatagramSocket fileSocket = new DatagramSocket(usedPort); 
		ExecutorService runnableExec = Executors.newFixedThreadPool(3);

		while(true){
			Runnable reporting = new Runnable() {
				@Override
				public void run() {
					try {
						DatagramPacket initialData = new DatagramPacket(new byte[4], 4);
						fileSocket.receive(initialData);
						InetAddress senderIP=InetAddress.getByAddress(initialData.getData());
						fileSocket.receive(initialData);
						int fileSize = 0;
					    for (int i=0; i<4; i++) {
					      fileSize = ( fileSize << 8 ) - Byte.MIN_VALUE + (int) initialData.getData()[i];
					    }
					    DatagramPacket file=new DatagramPacket(new byte[fileSize], fileSize);
					    fileSocket.receive(file);
						System.out.println("Provide the path for the file:");
						Scanner keyScan=new Scanner(System.in);
						String filePath=keyScan.nextLine();
						keyScan.close();
						FileOutputStream fileStream = new FileOutputStream(filePath);
					    fileStream.write(file.getData());
					    fileStream.close();
						DatagramPacket response= new DatagramPacket(new byte[1], 1);
						byte resp=(byte)9;
						byte[] respon=new byte[1];
						respon[0]=resp;
						response.setData(respon);
						response.setAddress(senderIP);
						response.setPort(usedPort);
						fileSocket.send(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				}
			};
			runnableExec.submit(reporting);
		}
		
	}	
}