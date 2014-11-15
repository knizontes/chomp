package client;

//import java.io.Console;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import main_pack.data.IndexTableName;
import main_pack.data.Place;
import main_pack.data.Traveler;
import main_pack.switchRMI.ServerInterface;
import main_pack.utils.Timestamp;
import main_pack.data.Route;

/**
Interfaccia utente per l'accesso alle operazioni: POST, GET, PUT, REMOVE e REMOVE_ALL.   
 */

public class ClientEngine extends UnicastRemoteObject
{
	
	private static final long serialVersionUID = 1L;
	private static final String CLIENTENGINETAG="[CLIENT ENGINE]";

	private Traveler traveler;// traveler da usare nelle transazioni

	private InetAddress ia;//inet address per trasmettere al peer
//	public final static int portClient = 20000;
	private Lock lock = new ReentrantLock();
	public Condition waitGetResponse = lock.newCondition();
	//attributi usati per i servizi RMI
	private final int REGISTRYPORT = 1099;
	String registryHost = "localhost";
	String serviceName = "ClientServerServiceRMI"; 
	String name_service;	
	ServerInterface si;
	private Boolean verbose;
	private int submittedGetNum=0;
	

	//--------------------------------------------

	//request identifier
	private int reqId=0;

	private Listener listener;
	private Thread listenerThread;

/**
 * Costruttore di default della classe, ha come parametri l'indirizzo ip dello switch rmi e modalita' verbose
 * @param host_rmi_server 
 * @param verbose
 * @throws RemoteException
 */

	public ClientEngine(String host_rmi_server, Boolean verbose) throws RemoteException
	{
		super();
		this.verbose=verbose;
		try {
			this.ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			System.out.println("errore in get local address");
			//e1.printStackTrace();
		}
		listener = new Listener(true,this);
		listenerThread = new Thread(listener,"listener thread");
		//vedere dove posizionare il RUN del thread
		listenerThread.start();


		this.registryHost = host_rmi_server;

		this.name_service = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
		//lookup per il servizio RMI
		try {
			this.si = _lookup();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

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

	public String getName_service() {
		return name_service;
	}

	public void setName_service(String name_service) {
		this.name_service = name_service;
	}

	public int getREGISTRYPORT() {
		return REGISTRYPORT;
	}




	// imposta il traveler da usare nelle funzioni
	public void setTraveler(Traveler t)
	{
		this.traveler = t;
	}

	private int getIdReq(){
		++reqId;
		return reqId-1;
	}
	
	public Traveler getCurrentTraveler()
	{
		return this.traveler;	
	}

	public void printCurrentTraveler()
	{
		System.out.println("Traveler correntemente in uso:");

		System.out.println(getCurrentTraveler().getNome()+" "
				+getCurrentTraveler().getCognome()+" "
				+getCurrentTraveler().getCF());
	}


	private IndexTableName createIndexTableName(String itn) throws NumberFormatException, Exception
	{
		return new IndexTableName(itn);
	}


	private void printTravelers(Traveler[] travelers) {
		// TODO Auto-generated method stub
		for(int i=0; i<travelers.length; i++)
			travelers[i].print();
	}

	public int getReqId(){
		return reqId;
	}

	/**
	 * usata per inserire un IndextableName
	 * @return ritorna l'indexTableName inserita
	 */
	private IndexTableName insertIndexTableName() {
		// TODO Auto-generated method stub
		//IndexTableName itn = null;
		Scanner s = new Scanner(System.in);
		//CONTROLLO INSERIMENTO VALORI CORRETTI
		String giorno;
		Integer c;

		do{
			System.out.println("inserire giorno (1-7) : ");
			giorno = s.nextLine();
			c = Integer.parseInt(giorno);
		}while(c<1 || c>7);

		System.out.println("inserire tratta: (int_partenza-int_dest)");
		String tratta = s.nextLine();

		Integer hr,min;
		String ora=null;
		do{
			System.out.println("inserire ora: (ora:min)");
			ora = s.nextLine();
			StringTokenizer st = new StringTokenizer(ora, ":");
			String h =st.nextToken();
			String m = st.nextToken();
			hr=Integer.parseInt(h);
			min=Integer.parseInt(m);

		}while((hr<0||hr>23) || (min<0||min>59));

		try {
			return new IndexTableName(giorno+"/"+tratta+"/"+ora);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


/**
 * Metodo che permette all'utente di inserire un array di IndexTableName
 * @return
 */
	private IndexTableName[] createIndexTableNameArray() {
		
        ArrayList<IndexTableName> alitn = new ArrayList<IndexTableName>();
        
		System.out.println("inserimento percorsi");

		Scanner percorsi = new Scanner(System.in);
		//int i=0;

		//inserire un nuovo percorso finquando si scrive 'yes'
		do{

			IndexTableName itn = this.insertIndexTableName();
			//array[i] = itn;
			//i++;
			alitn.add(itn);
			
			System.out.println("inserire altra tratta? (yes/no)");

		}while(percorsi.nextLine().equalsIgnoreCase("yes"));
        
		
		IndexTableName array[] = new IndexTableName[alitn.size()];

		for(int i=0;i<alitn.size();i++)
			array[i] = alitn.get(i);
		
		println("alitn size"+alitn.size()+", array length"+array.length);
		for (int i=0; i<alitn.size();++i)
			println("DEBUG - "+i+". alitn:"+alitn.get(i).toString()+ " array:"+array[i].toString());
		
		return array;
	}


	private void orderIndexTableName( IndexTableName[] itnarray)
	{
		for(int i=0; i<itnarray.length; i++)
		{
			Route r = itnarray[i].getRoute();
			if( r.getArrival().getPlace() < r.getStart().getPlace() )
			{
				Place min = r.getArrival();
				Place max = r.getStart();
				itnarray[i].getRoute().setStart(min);
				itnarray[i].getRoute().setArrival(max);
			}	
		}

	}


	private ServerInterface _lookup() throws MalformedURLException, RemoteException, NotBoundException
	{
		ServerInterface si = (ServerInterface) Naming.lookup(this.name_service);
		return si;
	}


	//	***********************************************
	//	METODI PER INTERFACCIA REST - USATA TRAMITE RMI
	//	***********************************************
	//invoca il post del server mettendo nel campo traveler il traveler in memoria
	//si indica al server di inserire il traveler
	

/**
 * POST: operazione d'inserimento di un viaggiatore nel sistema
 * @param itn
 * @return Stringa di conferma della presa in carico della richiesta da parte del sistema
 * @throws ServerNotActiveException
 */
	private String post(IndexTableName[] itn) throws ServerNotActiveException {
		try {
			System.out.println("posting "+ traveler.getCF()+" to index tables:");
			for (int i=0; i<itn.length;++i)
				System.out.println(i+"."+itn[i].toFormattedString());
			return this.si.post(traveler, itn);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "fail";
	}
	
	
	private String post(IndexTableName[] itn, Traveler t) throws ServerNotActiveException {
		try {
			System.out.println("posting "+ t.getCF()+" to index tables:");
			for (int i=0; i<itn.length;++i)
				System.out.println(i+"."+itn[i].toFormattedString());
			return this.si.post(t, itn);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "fail";
	}


/**
 * GET: operazione di ricerca dei viaggiatori che percorrano le tratte inserite in input 
 * @param itn_array
 * @return Stringa di conferma della presa in carico della richiesta da parte del sistema
 */

	//invoca il get del server 
	//il server ritorna al client l'insieme dei viaggiatori che corrispondono a 
	//quelli passati come parametro
	public String [] get(IndexTableName[] itn_array)
	{
		
		int response_num=0;
		
//		Boolean responded=false;
		ArrayList<ArrayList<IndexTableName>> routesItnArray= new ArrayList<ArrayList<IndexTableName>>();
		routesItnArray.add(new ArrayList<IndexTableName>());
		routesItnArray.get(0).add(itn_array[0]);
		Boolean added;
		for (int i=1; i<itn_array.length;++i){
			added=false;
			for (int j=0;j<routesItnArray.size();++j){
				if (itn_array[i].getRoute().toOrderedString().equals(routesItnArray.get(j).get(0).getRoute().toOrderedString())){
					routesItnArray.get(j).add(itn_array[i]);
					added=true;
					break;
				}
			}
			if (!added){
				routesItnArray.add(new ArrayList<IndexTableName>());
				routesItnArray.get(routesItnArray.size()-1).add(itn_array[i]);
			}
		}
		for (int i=0; i<routesItnArray.size();++i)
			for (int j=0; j<routesItnArray.get(i).size();++j)
				println(routesItnArray.get(i).get(j).toString());
		submittedGetNum=routesItnArray.size();
		reqId=0;
		Boolean timeout_expired=false;
		println ("routes size:"+routesItnArray.get(0).size());
		for (int i=0; i<routesItnArray.get(0).size();++i)
			println(((IndexTableName)routesItnArray.get(0).get(i)).toString());
		
		while(true){
			for (int i=0; i<submittedGetNum; ++i){
				try {
					println(""+i);
					Object[] objQuery = routesItnArray.get(i).toArray();
					IndexTableName[] tmp = new IndexTableName[objQuery.length];
					for (int j=0;j<objQuery.length;++j)
						tmp[j]=(IndexTableName)objQuery[j];
					println("get returns:"+si.get(traveler, tmp, reqId));
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
			while(response_num<routesItnArray.size()){
				try {
					lock.lock();
					println("waiting for "+routesItnArray.size() +" responses, received "+response_num);
					waitGetResponse.await(10, TimeUnit.SECONDS);
					++response_num;
					lock.unlock();
				} catch (InterruptedException e) {
					timeout_expired=true;
					break;
				}
			}
			if (!timeout_expired)
				break;
			
			++reqId;
		}
		
		return listener.queryResponse();
	}

	public int getSubmittedGetNum(){
		return submittedGetNum;
	}

	
	public void signalGetResponse(){
		println("DEBUG - signaling...");
		lock.lock();
		waitGetResponse.signal();
		lock.unlock();
		println("DEBUG - signaled");
	}

	// invoca il put del server che 
	//aggiorna informazioni sul traveler passato come parametro
	/**
	 * PUT: aggiorna le informazioni che riguardano il Traveler corrente
	 * @return
	 */
	public String put()
	{

		try {
			return this.si.put(traveler);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "fail";
	}

	// invoca il remove del server che
	// rimuove il traveler da l'indextable dell'index table name
	
	/**
	 * REMOVE: operazione di rimozione del viaggiatore dalle IndexTable specificate nel parametro d'ingresso
	 * @param itm
	 * @return Stringa di conferma della presa in carico della richiesta da parte del sistema
	 */
	public String remove(IndexTableName [] itm) 
	{
		int retval=0;
		try {
			return this.si.remove(this.traveler, itm);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "fail";
	}

	// invoca il remove del server
	// il server rimuove tutte le occorrenze di Traveler dai nodi del sistema
	
	/**
	 * REMOVE_ALL: operazione di rimozione di tutte le occorrenze del viaggiatore dal sistema
	 * @return Stringa di conferma della presa in carico della richiesta da parte del sistema
	 */
	public String removeAll()
	{
		try {
			return this.si.removeAll(this.traveler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return "fail";
	}

	
	

	// ****************
	// FINE METODI REST
	// ****************

	private String tag(){
		return (Timestamp.now()+"-"+CLIENTENGINETAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	public static IndexTableName[] fromArrayListToArray(ArrayList<IndexTableName> alitn)
	{
		IndexTableName [] itn = new IndexTableName[alitn.size()];
		
		for(int i=0; i<alitn.size(); i++)
		    itn[i] = alitn.get(i);
		
		return itn;
	}
	
	
	/**
	 * Operazione di aggiornamento delle informazioni sul viaggiatore
	 */
	private void changeInfo() {
		System.out.println("Le vecchie informazioni sono:\n\t"+getCurrentTraveler().getInfo()+
				"\n\nInserisci le nuove informazioni sul viaggiatore (max 160 caratteri):\n");
		Scanner s = new Scanner(System.in);
		String info;
		if ((info=s.nextLine()).length()<=160)
			getCurrentTraveler().setInfo(info);
		else
			getCurrentTraveler().setInfo(info.substring(0, 160));
		System.out.println("Le nuove informazioni sul viaggiatore sono:\n"+getCurrentTraveler().getInfo()+
				"\n # caratteri:"+getCurrentTraveler().getInfo().length());
		put();
		
	}
	
	
	/**
	 * Operazione d'aggiornamento della password d'accesso al sistema per il viaggiatore
	 */
	private void changePwd(){
		System.out.println("Inserisci la vecchia password:");
		String password;
		Hasher hasher = new Hasher();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			password=in.readLine();
			traveler.setPwdHash(hasher.stringHash(password));
			System.out.println("[DEBUG]hash size:"+traveler.getPwdHash().length());
			put();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	//********************* MAIN ******************************
	//*********************************************************


	public static void main(String argv[]) throws IOException
	{
		System.out.println("********* CLIENT CHOMP *********");
		//ogni volta che il client effettua una richiesa 
		//sicrea un thread che gestisce la risposta della rete

		if(argv.length != 6)
		{
			//<host di collegamento> non si deve passare come paramentro da riga di comando 
			//ma il client cerca l'indirizzo da una lista di indirizzi IP possibili 
			//	System.out.println("Usage: java ClientEngine <nome> <cognome> <cf> <itn:WeekDay(short)/Route(int-int)/Time(Short:Short)> <host di collegamento????>");
			System.out.println("Usage: java ClientEngine <nome> <cognome> <cf> <info> <pwdhash> <host di collegamento????>");
			System.exit(0);
		}


		String nome    = argv[0],
				cognome = argv[1],
				cf      = argv[2],
				info    = argv[3],
				pwdHash = argv[4],
				ipServer= argv[5];
				//	itn     = argv[3],
				

		ClientEngine ce = new ClientEngine(ipServer,true);

		//inizializzare current Traveler
		Traveler currentTraveler = new Traveler(cf, nome, cognome, info, pwdHash);
		ce.setTraveler(currentTraveler);

		IndexTableName [] itn= null;
		//		IndexTableName itn= null;
		//ripristinare
		//ce.runClient(ce.getCurrentTraveler(), itn);

		//ciclo infinito per effettuare richieste
		System.out.println("CLIENT AVVIATO");
		int richiesta = 0;
		while(true)
		{

			int max_option = 10;
			int chose;
			Scanner scanner = new Scanner(System.in);

			do{
				System.out.println("***************************");
				System.out.println("0 - TEST echo");
				System.out.println("1 - post");
				System.out.println("2 - get");
				System.out.println("3 - cambia informazioni");
				System.out.println("4 - cambia password");
				System.out.println("5 - remove");
				System.out.println("6 - remove all");
				System.out.println("7 - setTraveler");
				System.out.println("8 - TEST popola il sistema");
				System.out.println("9 - get Current Traveler");
				System.out.println("10 - EXIT");
				System.out.println("***************************");

				chose = scanner.nextInt(); 

			}
			while(chose > max_option);

			try{
				switch(chose) 
				{
				case 0 : {
					System.out.println("******* TEST echo *******");

					String ciao = ce.si.getEcho("ciao");
					System.out.println("messaggio ritornato: "+ciao);
					//ce.post(new IndexTableName(itn));
					break;
				}
				case 1 : {

					System.out.println("******* POST *******");

					//					itn = ce.insertIndexTableName();
					//					String ret = ce.post(itn);

					itn = ce.createIndexTableNameArray();
					String ret = ce.post(itn);

					ce.orderIndexTableName(itn);
					System.out.println(ret);
					break;
				}
				case 2 : {
					//				per ogni index table name 
					//				inserire : giorno, tratta, ora 
					//				questo per ogni tragitto da fare con la stessa persona
					//				ogni tratta si inserisce nel array
					System.out.println("******* GET *******");
					//IndexTableName itm = new IndexTableName(itn);
					itn = ce.createIndexTableNameArray();
					String [] responseTravelers=ce.get(itn);
					
					if(itn != null)
					{	
						System.out.println("Viaggiatori che effettuano la tratta :");

						for(int i=0; i<responseTravelers.length; i++)
							System.out.println((i+1)+". "+responseTravelers[i]); 
						System.out.println();
						System.out.println();
						System.out.println("******************************");
						Thread.sleep(1000);
						//ce.getCurrentTraveler().printTraveler();
					}
					else
						System.out.println("effettuare il POST specificando il percorso(IndexTableName)");
					break;
				}

				case 3 : {
					System.out.println("******* Informazioni *******");
					ce.changeInfo();
					break;
				}
				
				case 4 : {
					System.out.println("******* cambio password *******");
					ce.changePwd();
					break;
				}

				case 5 : {
					System.out.println("******* REMOVE *******");
					itn = ce.createIndexTableNameArray();
					ce.remove(itn);
					break;
				}

				case 6 : {
					System.out.println("******* REMOVEALL *******");
					ce.removeAll();
					break;
				} 

				case 7 : {
					System.out.println("******* NEW TRAVELER *******");
					//ce.new_traveler();
					break;
				}

				case 8 : {
					//
					//PRIMA DI USARE QUESTA OPZIONE ESEGUIRE IL MAIN DI XMLloader
					//
					
					System.out.println("******* TEST popola il sistema *******");
					
					String filename = "travelers.xml";
					oldXMLloader xl = new oldXMLloader(filename);
			        

					//lettura dei dati 
				    ArrayList<InfoTrav> it = xl.readTraveler();
					
				    for(int i=0; i<it.size(); i++)
						{
						   ce.setTraveler(it.get(i).getT());
						   
						   itn = ClientEngine.fromArrayListToArray(it.get(i).getAlitn());
						   String ret = ce.post(itn);
						   System.out.println(CLIENTENGINETAG+ret);
//						   ce.orderIndexTableName(itn);
//						   for(int j=0; j<itn.length; j++)
//						   {
//							   String ret = ce.post(itn[j]);
//							   System.out.println(CLIENTENGINETAG+ret);
//							   ce.orderIndexTableName(itn);
//						   }
						}
					break;
				}


				case 9 :{
					System.out.println(ce.getCurrentTraveler().privateSignature());
					break;
				}
				
				case 10 : {					
					System.out.println("bye");
					System.exit(0);
				}

				default : break;

				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}//fine while 
	}

	


}
