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
		int sendPort=9015;
		System.out.println("Please provide the IP address that you wish to use during the connection:");
		Scanner keyScan=new Scanner(System.in);
		String ownIP=keyScan.nextLine();
		keyScan.close();

		final DatagramSocket soc = new DatagramSocket(reportPort); 
		final DatagramSocket filSoc = new DatagramSocket(sendPort); 
		//tutaj na pewno nie ma być 1, bo ma obsługiwać kilka połączeń na raz, ale nawet z jednym nie udało mi się dobrze zrobić
        ExecutorService runnableExec = Executors.newFixedThreadPool(1);
        LinkedList<InetAddress> droga=new LinkedList<InetAddress>();
        //nie wiem już, dlaczego ograniczyłem tego while'a, powinno raczej być po prostu (true)
        int i=0;
		while(i<5){
			i++;
			
			//raportuje się jako węzeł do użycia w przesyłaniu
			Runnable reporting = new Runnable() {
				//to akurat działa, w sensie wysyłający dostaje do listy adres jak należy
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
				//tutaj zaczynają się już schody. Do pewnego momentu działa- wszystkie to wypisywanie na ekran tylko po to, żeby kontrolować, do którego momentu się wykonuje
				public void run() {
					try {
						//pierwsze co dostaje to liczba adresów IP w liście
						System.out.println("we're doing things!");
						DatagramPacket noOfNodes = new DatagramPacket(new byte[1], 1);
						filSoc.receive(noOfNodes);
						System.out.println("we're doing things2!");
						int number = (int) noOfNodes.getData()[0];
						System.out.println(number);
						System.out.println("we're doing things3!");
						DatagramPacket nodeIP = new DatagramPacket(new byte[4], 4);
						filSoc.receive(nodeIP);
						//pierwszy otrzymany adres powinien być zwrotny
						InetAddress returnAddress = nodeIP.getAddress();
						System.out.println("return add:" + returnAddress.toString());
						filSoc.receive(nodeIP);
						//drugi to adres kolejnego węzła
						System.out.println("we're doing things4!");
						InetAddress nextAddress = nodeIP.getAddress();
						
						System.out.println("next add:" + nextAddress.toString());
						//te dwa powyższe adresy dobrze wczytywał akurat
						
						//to nie wiem, jak działa, bo miałem za mało komputerów na testy
						droga.add(InetAddress.getByName(ownIP));
						for (int i = 0; i < (number - 2); i++) {
							filSoc.receive(nodeIP);
							droga.add(nodeIP.getAddress());
						}
						//otrzymuje rozmiar pliku (jako int) żeby wiedział, ile bajtów odebrać
						DatagramPacket sizeOfFile = new DatagramPacket(new byte[4], 4);
						filSoc.receive(sizeOfFile);
						int fileSize = 0;
						for (int i = 0; i < 4; i++) {
							fileSize = (fileSize << 8) - Byte.MIN_VALUE + (int) sizeOfFile.getData()[i];
						}
						//dostaje plik
						DatagramPacket sentFile = new DatagramPacket(new byte[fileSize], fileSize);
						filSoc.receive(sentFile);
						byte nodes = (byte) droga.size();
						byte[] nodesNo = new byte[1];
						nodesNo[1] = nodes;
						//wysyła dalej analogicznie jak dostawał- ilość węzłów, węzły, rozmiar pliku i plik
						//i teoretycznie odbiorca nawet coś czasami dostawał (nie zawsze) ale nie to, co powinien
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
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			runnableExec.submit(sending);
		}	
	}
}