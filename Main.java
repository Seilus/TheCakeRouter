package klient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) throws IOException {
		int usedPort=9915;
		System.out.println("Please provide the IP address that you wish to use during the connection:");
		Scanner keyScan=new Scanner(System.in);
		String ownIP=keyScan.nextLine();
		//IPscan.close();
		/*
		 * Ponieważ maszyny mogą mieć kilka adresów IP, lepiej zawsze pytać o to, jakiego trzeba użyć
		 */
        final DatagramSocket socket = new DatagramSocket(usedPort); 
        ExecutorService runnableExec = Executors.newFixedThreadPool(10);
        byte[] address = InetAddress.getByName(ownIP).getAddress(); 
        DatagramPacket ping=new DatagramPacket(address, address.length);
        byte[] bCastAdd=address;
        bCastAdd[3]=(byte) 255; //Broadcast, czyli zmieniamy ostatni bajt na 255 ?
        InetAddress bCastAddress=InetAddress.getByAddress(bCastAdd);
        //System.out.println(bCastAddress.toString());
        ping.setAddress(bCastAddress);
        ping.setPort(usedPort);
        socket.send(ping);
        LinkedList<InetAddress> listaWezlow=new LinkedList<InetAddress>();
        //tutaj runnable który będzie przyjmował odpowiedzi i wczytywał je do LinkedLista?
        
        int timer=0;	
		while (timer<100) {
			Runnable listening = new Runnable() {
				@Override
				public void run() {
					try {
						DatagramPacket wezel = new DatagramPacket(new byte[4], 4);
						socket.receive(wezel);
						listaWezlow.add(InetAddress.getByAddress(wezel.getData()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			};
			runnableExec.submit(listening);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timer++;
			
		}
		listaWezlow.removeFirst();
		System.out.println(listaWezlow.size());
		for(int i=0; i<listaWezlow.size(); i++){
			System.out.println(i+". "+listaWezlow.get(i));
		}
       
       // String tAddress = null;// o to bedziemy pytac w programie
        //InetAddress targetAddress=InetAddress.getByName(tAddress);
        int[] indeksy = new int[listaWezlow.size()];
        String input="string";
        Arrays.fill(indeksy, -1);
        
        System.out.println("Choose the indices of the nodes that you wish to use, in the order that you wish to use them");
        System.out.println("Upon choosing all desired nodes, type \"end\"");
        //InputStreamReader indexScan=new InputStreamReader(System.in);
        //BufferedReader indexRead=new BufferedReader(indexScan);
        //Scanner indexRead=new Scanner(System.in);
        int k=0;
        while(true){
        	
        	if (keyScan.hasNextLine()) {
				input = keyScan.nextLine();
				if(input.equals("end")){
					break;
				}
				int ind;
				try {
					ind = Integer.parseInt(input);
					if (ind+1 > listaWezlow.size() || ind < 0) {
						System.out.println("Invalid index detected. Input will be ignored");
					} else {
						indeksy[k] = ind;
						k++;
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println("You must provide integer indices");
				}
				
			}
        	if(k==listaWezlow.size()){
        		break;
        	}
        }
        //socket.close();
        LinkedList<InetAddress> droga=new LinkedList<InetAddress>();
        for(int i=0; i<indeksy.length; i++){
        	//System.out.println(indeksy[i]);
        	if(indeksy[i]>-1){
        		droga.add(listaWezlow.get(indeksy[i]));
        	}
        }
		for(int i=0; i<droga.size(); i++){
			System.out.println(i+". "+droga.get(i));
		}
		int sendPort=9014;
		DatagramSocket sendFileSocket=new DatagramSocket(sendPort);
		System.out.println("Please provide the desired IP address of the recipient:");
		String recIP=keyScan.nextLine();
		InetAddress recipientAddress=InetAddress.getByName(recIP);
		droga.add(recipientAddress);
		InetAddress firstNode=droga.removeFirst();
		for(int i=0; i<droga.size(); i++){
			DatagramPacket nodeSend=new DatagramPacket(droga.get(i).getAddress(), droga.get(i).getAddress().length);
			nodeSend.setAddress(firstNode);
			nodeSend.setPort(sendPort);
			sendFileSocket.send(nodeSend);
		}
		System.out.println("Provide the path of the file that is to be sent:");
		String path=keyScan.nextLine();
		//File package=new File()
    }
}
