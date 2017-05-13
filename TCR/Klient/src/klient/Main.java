package klient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) throws IOException {
		int usedPort=914;
        DatagramSocket socket = new DatagramSocket(usedPort); 
        ExecutorService runnableExec = Executors.newFixedThreadPool(10);
        byte[] msg = InetAddress.getLocalHost().toString().getBytes("utf8"); 
        DatagramPacket ping=new DatagramPacket(msg, msg.length);
        InetAddress address=InetAddress.getLocalHost(); //To raczej nie ten
        byte[] add=address.getAddress(); 
        add[3]=(byte) 255; //Broadcast, czyli zmieniamy ostatni bajt na 255 ?
        InetAddress bCastAddress=InetAddress.getByAddress(add);
        
        System.out.println(bCastAddress.toString());
        ping.setAddress(bCastAddress);
        ping.setPort(usedPort);
        socket.send(ping);
        LinkedList<InetAddress> listaWezlow=new LinkedList<InetAddress>();
        
        
        
        //tutaj runnable który będzie przyjmował odpowiedzi i wczytywał je do LinkedLista?
        Runnable listening=new Runnable(){
        	@Override
        	public void run(){
        		try{
        			DatagramPacket wezel=new DatagramPacket(new byte[1024], 1024);
        			socket.receive(wezel);
        			listaWezlow.add(InetAddress.getByAddress(wezel.getData()));
        		}
        		catch (IOException e){
        			e.printStackTrace();
        		}

        	}
        };	
        runnableExec.submit(listening);
        String tAddress = null;// o to bedziemy pytac w programie
        InetAddress targetAddress=InetAddress.getByName(tAddress);
        
        
        socket.close();
    }
}
