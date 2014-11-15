package main_pack.switchRMI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


import main_pack.data.Traveler;
import main_pack.data.messaging.Request;

public class Test {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocket ss = new ServerSocket(4343);
		
		System.out.println("[TEST] Avviato server su porta 4343...");
		
		while(true)
		{
			Socket s = ss.accept();//In attesa di connessione...
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			
			Request req = (Request)ois.readObject();//Request trasmessa
			
			req.print();//Stampo la Request appena arrivata
			
			s.close();//Chiudo la socket una volta ricevuto il messaggio Request
			
			switch (req.getReqType()) {
			case Request.GET:
				
				Traveler t = (Traveler)req.getObj();
				
				t.print();
				
				Socket spara = new Socket(req.getClientIP(), req.getPort());
				
				ObjectOutputStream oos = new ObjectOutputStream(spara.getOutputStream());
				
				//Traveler tnew = new Traveler("caijoijasd", "Daniele", "Morittu");
				
				Traveler[] t_array = new Traveler[9];
				
				t_array[0] = new Traveler("1", "Primo", "Traveler");
				t_array[1] = new Traveler("2", "Secondo", "Viaggiatore");
				t_array[2] = new Traveler("3", "Terzo", "Viaggiatore");
				t_array[3] = new Traveler("4", "Secondo", "Viaggiatore");
				t_array[4] = new Traveler("5", "Secondo", "Viaggiatore");
				t_array[5] = new Traveler("6", "Secondo", "Viaggiatore");
				t_array[6] = new Traveler("7", "Secondo", "Viaggiatore");
				t_array[7] = new Traveler("8", "Secondo", "Viaggiatore");
				t_array[8] = new Traveler("9", "Secondo", "Viaggiatore");
				
				
				oos.writeObject(t_array);
				
				oos.flush();
				
				oos.close();
				
				spara.close();
				
				
				break;
				
			case Request.POST:
				
				
				break;
			
			case Request.PUT:
				
				break;
				
			case Request.REMOVE:
				
				break;
				
			case Request.REMOVE_ALL:
				
				break;

			default:
				break;
			}
			
		}
	}
}
