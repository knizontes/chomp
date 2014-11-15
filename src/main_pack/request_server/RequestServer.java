package main_pack.request_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;

import main_pack.chomp.ChompServer;
import main_pack.chomp_engine.ChompEngine;
import main_pack.chomp_engine.SST.SSTable;
import main_pack.data.Traveler;
import main_pack.data.messaging.Request;
import main_pack.data.messaging.Response;
import main_pack.utils.Timestamp;

/**
 * RequestServer Class
 * 
 * Thread di gestione delle richieste provenienti dal peer operante come switch RMI il quale inoltra al Request Server
 * del peer competente per le risorse cercate le richieste ad esso indirizzate dal Client.
 * 
 * La risposta del Request Server e' incapsulata nel messaggio Response il quale viene indirizzato direttamente al Client
 * generatore della richiesta originaria secondo lo schema seguente:<p>
 *  
 * |RequestServer| --> Response (TCP message) --> Client
 * 
 */
public class RequestServer extends Thread {
	
	private static final String REQUESTSERVERTAG="[REQUEST SERVER]";
	private ServerSocket ss;
	private Socket s;
	private ChompEngine ce;
	private SSTable sst;
	public static final int  PORT = 3335;
	private int reqCounter = 0;
	private Boolean verbose;
	
	public RequestServer(ChompEngine ce, Boolean verbose)
	{
		this.verbose=verbose;
		try {
			ss = new ServerSocket(PORT);
			s = new Socket();
			this.ce=ce;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void run() {
		
		println("Avviato Request Server sulla porta "+PORT+"...");
	
		while(true)
		{
			try {
				
			s = ss.accept();//Accetta nuova connessione in ingresso
			 
			ObjectInputStream ois;//Dichiara lo stream di oggetti in input
			
			ois = new ObjectInputStream(s.getInputStream());//Inizializza lo stream in ingresso
			
			Request req = (Request)ois.readObject();//Dichiara ed inizializza,previa ricezione, la nuova richiesta
			
			++reqCounter;//Incremento il contatore delle richieste ricevute
			
			req.print();//Stampa la richiesta appena ricevuta
			
			s.close();//Chiude la socket creata specificamente per la nuova richiesta in ingresso
			
			switch (req.getReqType()) {
				case Request.GET:
					println("got a get request");
					get(req);
					break;
					
				case Request.POST:

					println("got a post request");
					post(req);					
					break;
				
				case Request.PUT:
					println("got a put request");
					break;
					
				case Request.REMOVE:
					println("got a remove request");
					remove(req);
					break;
					
				case Request.REMOVE_ALL:
					println("got a remove all request");
					break;
	
				default:
					break;
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void post(Request req){
		try {
			ce.addIndexTableValue((Traveler)req.getObj(), req.getAllItn()[0], req.getHash());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void get(Request req){
		try {
//			println("got a get request");
			//Dichiara ed inizializza il viaggiatore esecutore della richiesta
			Traveler t = (Traveler)req.getObj();
			//Stampa il viaggiatore esecutore della richiesta
			t.print();
			Socket toClient;
			//Istanzia la socket per la risposta al client esecutore della richiesta
			toClient = new Socket(req.getClientIP(), req.getPort());
			//Dichiara ed inizializza lo stream di oggetti da trasmettere al client
			ObjectOutputStream oos = new ObjectOutputStream(toClient.getOutputStream());
			ArrayList<Traveler> travelers =ce.finalQuery(req.getAllItn(), t.getCF());
			//Dichiara ed inizializza la risposta da inviare al client esecutore della richiesta
			Response response = new Response(req.getIdReq(), travelers);
			//Invia la risposta al client
			oos.writeObject(response);
			//Flush della scrittura
			oos.flush();
			//Chiude lo stream in output
			oos.close();
			//Chiude la socket di comunicazione con il client esecutore della richiesta
			toClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void remove(Request req){
		println("got a remove request for "+((Traveler)req.getObj()).getNome()+" "+((Traveler)req.getObj()).getCognome());
		ce.removeTravelerFromIndexTable(((Traveler)req.getObj()).getCF(), req.getAllItn());

	}
	
	public void removeAll(Request req){
		println("got a remove all request for "+((Traveler)req.getObj()).getNome()+" "+((Traveler)req.getObj()).getCognome());
		ce.removeTravelerFromAllTables(((Traveler)req.getObj()).getCF());
	}
	
	public void put(Request req){
		println("got a put request for "+((Traveler)req.getObj()).getNome()+" "+((Traveler)req.getObj()).getCognome());
		ce.updateTraveler((Traveler)req.getObj());
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+REQUESTSERVERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}

	
}
