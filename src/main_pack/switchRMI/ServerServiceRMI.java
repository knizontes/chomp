package main_pack.switchRMI;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import client.ClientEngine;
import client.Listener;



import main_pack.data.IndexTableName;
import main_pack.data.Traveler;
import main_pack.data.messaging.Request;
import main_pack.request_server.RequestServer;
import main_pack.ring.Chompeer;
import main_pack.ring.token.TokenManager;
import main_pack.utils.Hasher;
import main_pack.utils.Timestamp;


/*
 *  CLASSE CHE DEVE ESSERE INSERITA NEL CHOMPEER 
 *  IN MODO CHE SI OCCUPI DELLE RICHIESTE RMI 
 *  CLIENT - SERVER       
 */

/**
 * 
 * ServerServiceRMI Class
 * 
 * Thread di gestione delle invocazioni dei metodi remoti (RMI) i quali redirigono il flusso della richiesta verso il 
 * peer competente per la risorsa cercata.
 *
 */
public class ServerServiceRMI extends UnicastRemoteObject implements ServerInterface, Runnable
{

	private static final String SWITCHRMITAG="[SWITCH RMI]";
	private final int REGISTRYPORT = 1099;
	private final static int RETRIES_NUM=5;
	private String registryHost ;
	private String serviceName = "ClientServerServiceRMI"; 
	public String name_service;
	private Chompeer chompeer;
	private TokenManager tokenMan;
	private Hasher hasher;
	private Boolean verbose;
	private short dnsAddressed=0;
	
	public ServerServiceRMI(Chompeer chompeer, Boolean verbose) throws RemoteException {
		super();
		this.chompeer=chompeer;
		this.tokenMan=chompeer.getTokenManager();
		this.verbose=verbose;
		registryHost= chompeer.getChompeer_ip().getHostAddress();
//		System.out.println("----------->"+ registryHost);
		hasher = new Hasher(chompeer.getKeySpace());
		println("initialized...");
        name_service = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;	
	}
	
	public String getEcho(String echo) throws RemoteException
	{
		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
		println("Messaggio ricevuto:"+echo);
		return echo;
	}

	
	public String getNameService(){
		return name_service;
	}
	
	//I METODI FANNO HASH SUL INDEXTABLENAME PER INOLTRARE L'OPERAZIONE SUL NODO COMPERTENTE
	
	
	/**
	 *POST: Inserisce info relative al viaggio che vuole compiere il relativo traveler
	 * @param myself è il traveler specificato dal client
	 * @param itn è l'array di percorsi da compiere
	 * @return stringa che conferma la presa in carico della richiesta  
	 */
	@Override
	public String post( Traveler myself, IndexTableName []itn)  {
			
		
		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
		//Deduzione tramite funzione hash del nodo competente per la richiesta (Hash su ?)
		
			//TODO
			//La funzione hash deve restituire l'ip e la porta del peer da contattare
		
		
 		//Instaurazione connessione con peer competente
		int key;
		int retries=0;
		Boolean retry=true;
		while (retry&&(retries<RETRIES_NUM)){
			try {
				System.out.println();
				println("post routine:");
					//Una POST per ogni IndexTableName dell'array itn
					for(int i=0;i < itn.length; i++)
					{
						println((i+1)+"new table value:"+itn[i].toString());
						key = hasher.hash(itn[i].getRoute().toOrderedString());
						InetAddress dst = chompeer.getAuthoritativePeer(key);
						Socket peer = new Socket(dst, RequestServer.PORT); //WARNING!Richiesta da inviare al RingServer?Porta 4343 di test!
						
						ObjectOutputStream oos = new ObjectOutputStream(peer.getOutputStream());
						
						//Creazione richiesta
						IndexTableName [] it = new IndexTableName[1];
						it[0]=itn[i];
						Request req = new Request(-1,Request.POST,InetAddress.getByName(RemoteServer.getClientHost()), -1, myself, it,key,verbose); //WARNING!Da correggere...SOLO PER TESTING!
						
				 		//Trasferimento richiesta
						oos.writeObject(req);
						
						oos.flush();
				 		//Chiusura connessione
						
						oos.close();
						peer.close();
						retry=false;
					}
				} 
			catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				println("the chompeer seems to be down, waiting 10 seconds until retryng to connect");
				++retries;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			} catch (ServerNotActiveException e) {
				e.printStackTrace();
			}
		}
		
 		
 		//Attesa esito operazione sul Client che l'ha richiesta
 		
 		//NOTA: La risposta arriverà al client direttamente dal peer competente tramite instaurazione di connessione TCP diretta
		
		//System.out.println("return?");//TEST
		
		return "[SERVER] post("+myself.toString()+","+itn.toString()+") presa in carico...";
	}

	
	//EFFETTUA HASH SUL INDEXTABLENAME DEL PRIMO ELEMENTO NELL'ARRAY
	//PER INOLTRARE L'OPERAZIONE SUL NODO COMPERTENTE, 
	//AL NODO COMPETENTE SI INVIA ANCHE L'ARRAY CON I RESTANTI INDEXTABLENAME
	//SULL'ELEMENTO IN TESTA SI RICALCOLA L'HASH E SI RIPETE IL PROCEDIMENTO
 	/**
 	 * GET: Permette lato client di sapere quali viaggiatori effettuano le tratte specificate nel paramentro itn 
 	 * @param myself è il traveler specificato dal client
	 * @param itn è l'array di percorsi per cui si effetua la richiesta 
	 * @param idReq identificativo richiesta, serve al client per porter ricostruire la risposta
	 * @return stringa che conferma la presa in carico della richiesta
 	 */
	@Override
	public String get(Traveler myself, IndexTableName[] itn, int idReq)
			throws UnknownHostException, IOException {
 		
 		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
 		//Deduzione tramite funzione hash del nodo competente per la richiesta (Hash su ?)
		
		//La funzione hash deve restituire l'ip e la porta del peer da contattare
	
		//Instaurazione connessione con peer competente
	
 		//1-1 Richiesta-Connessione
 	
 		int key;
		try {
//			println("DEBUG-stringa su cui fare hash: "+itn[0].getRoute().toOrderedString());
			key = hasher.hash(itn[0].getRoute().toOrderedString());
			InetAddress dst = chompeer.getAuthoritativePeer(key);
			Socket peer = new Socket(dst, RequestServer.PORT); //WARNING!Richiesta da inviare al RingServer?Porta 4343 di test!
			
			ObjectOutputStream oos = new ObjectOutputStream(peer.getOutputStream());
			
			//Creazione richieste
			println("DEBUG - itn size:"+itn.length);
			Request req = new Request( idReq,Request.GET, InetAddress.getByName(RemoteServer.getClientHost()), Listener.LISTEN_PORT, myself, itn,key, verbose); //WARNING!Da correggere...SOLO PER TESTING!
			
				//Trasferimento richiesta
			
			oos.writeObject(req);
			
			oos.flush();
			
				//Chiusura connessione
			
			oos.close();
			peer.close();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
 	
		//Attesa esito operazione sul Client che l'ha richiesta

 		
 		return "[SERVER] get("+myself.toString()+","+itn.toString()+") presa in carico...";
	}
 	//FA GIRARE IL TOKEN NELL'ANELLO CON LE MODIFICHE RELATIVE AL TRAVELER
	
	/**
 	 * PUT: Effettua la modifica sulle informazioni di utente 
 	 * @param myself è il traveler specificato dal client
	 * @return stringa che conferma la presa in carico della richiesta
 	 */
	@Override
	public String put(Traveler myself) throws IOException {
		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
		
		Request req = new Request( -1,Request.PUT, null, -1, myself, null,0, verbose); 
		tokenMan.addRequest(req);

		return "[SERVER] put("+myself.toString()+") presa in carico...";
	}


	/**
 	 * REMOVE: Rimuove l'utente dalle tratte specificate in itn 
 	 * @param myself è il traveler specificato dal client
 	 * @param itn array di tratte da rimuovere
	 * @return stringa che conferma la presa in carico della richiesta
 	 */
	@Override
	public String remove(Traveler myself, IndexTableName[] itn) throws RemoteException {
		
		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
		// TODO Auto-generated method stub
		//return 0;
		
		//Deduzione tramite funzione hash del nodo competente per la richiesta (Hash su ?)
 		//Rimozione occorrenze del traveler myself dall'anello facendo girare tale info sull'anello con 
		
		//Attesa esito operazione sul Client che l'ha richiesta
		
		
		int key;
		try {
//			println("DEBUG-stringa su cui fare hash: "+itn[0].getRoute().toOrderedString());
			key = hasher.hash(itn[0].getRoute().toOrderedString());
			InetAddress dst = chompeer.getAuthoritativePeer(key);
			Socket peer = new Socket(dst, RequestServer.PORT); //WARNING!Richiesta da inviare al RingServer?Porta 4343 di test!
			
			ObjectOutputStream oos = new ObjectOutputStream(peer.getOutputStream());
			
			//Creazione richieste
			println("DEBUG - itn size:"+itn.length);
			Request req = new Request( -1,Request.REMOVE, InetAddress.getByName(RemoteServer.getClientHost()), Listener.LISTEN_PORT, myself, itn,key, verbose); //WARNING!Da correggere...SOLO PER TESTING!
			
				//Trasferimento richiesta
			
			oos.writeObject(req);
			
			oos.flush();
			
				//Chiusura connessione
			
			oos.close();
			peer.close();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return "[SERVER] remove("+myself.toString()+","+itn.toString()+") presa in carico...";
	}


	/** Rimuove tutte le occorrenze del Traveler dai nodi del sistema
	 * @param myself il Traveler da rimuovere
	 * @return stinga che conferma l'operazione presa in carico
	 */
	@Override
	public String removeAll(Traveler myself) throws RemoteException {
		/*if a rmi function is invoked the dnsAddressed flag is setted. 
		 * This means a dns maps the chomp node so it works like a switch
		 */
		dnsAddressed=1;
		Request req = new Request( -1,Request.REMOVE_ALL, null, -1, myself, null,0, verbose); 
		tokenMan.addRequest(req);
		return "[SERVER] removeAll("+myself.toString()+") presa in carico...";
	}

	
	public int isDnsAddressed(){
		return (int)dnsAddressed;
	}

	public String getRegistryHost() {
		return registryHost;
	}

	public void setRegistryHost(String registryHost) {
		this.registryHost = registryHost;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getREGISTRYPORT() {
		return REGISTRYPORT;
	}	
	
	
	private String tag(){
		return (Timestamp.now()+"-"+SWITCHRMITAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
	
		
	}

//	public static void main(String[] args) throws NumberFormatException, Exception {
//		
//		ServerServiceRMI ssr = new ServerServiceRMI();
//		Naming.rebind(ssr.name_service, ssr);
//		System.out.println("[ServerServiceRMI] - Start");
//		
////		ssr.get(new Traveler(), new IndexTableName("Prova"));
////		ssr.get(new Traveler(), new IndexTableName("Maddai"));
////		
////		System.out.println(ssr.idReq);
//	}

}
