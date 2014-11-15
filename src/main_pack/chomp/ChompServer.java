package main_pack.chomp;

/**
 * Chomp Main Class!!! 
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;




import main_pack.chomp_engine.ChompEngine;
import main_pack.data.IndexTableName;
import main_pack.data.Traveler;
import main_pack.data.messaging.IndexValue;
import main_pack.metrics.MetricsManager;
import main_pack.request_server.RequestServer;
import main_pack.ring.Chompeer;
import main_pack.ring.recovery.RecoveryEngine;
import main_pack.ring.recovery.server.RecoveryServer;
import main_pack.switchRMI.ServerServiceRMI;
import main_pack.utils.Timestamp;

/**
 * MAIN CLASS: classe principale. Dichiara ed istanzia tutti i thread componenti lo stack protocollare necessario al funzionamento del sistema.
 *
 */
public class ChompServer extends UnicastRemoteObject	{

	private static final String CHOMPSERVERTAG="[CHOMP SERVER]";
	public static final int BOOT_MILLISECONDS=20000; //20 sec
	private Chompeer chompeer;
	private ChompEngine chomp_eng;
	private RecoveryServer rec_server;
	private RecoveryEngine rec_eng;
	private RequestServer req_server;
	private MetricsManager metricsMan;
	private Thread metricsThread;
	private ServerServiceRMI ss_rmi;
	private short verbosityFlag;
	Boolean verbose;
	
	/**
	 * Flag per attivare verbosita' a livello di Chomp Server
	 */
	public static final short chompServerVerbose= 1;
	/**
	 * Flag per attivare verbosita' a livello di Chompeer
	 */
	public static final short chompeerVerbose= 2;
	/**
	 * Flag per attivare verbosita' a livello di Chomp Engine
	 */
	public static final short chompEngineVerbose= 4;
	/**
	 * Flag per attivare verbosita' a livello di Recovery Engine
	 */
	public static final short recoveryEngineVerbose= 8;
	/**
	 * Flag per attivare verbosita' a livello di Load Balancer
	 */
	public static final short loadBalancerVerbose= 16;
	/**
	 * Flag per attivare verbosita' a livello di Request Server
	 */
	public static final short requestServerVerbose=32;
	/**
	 * Flag per attivare verbosita' a livello di Switch RMI
	 */
	public static final short switchRmiVerbose=64;
	/**
	 * Flag per attivare verbosita' a livello di Metrics Manager
	 */
	public static final short metricsManagerVerbose=128;
	
	/**
	 * Costruttore per l'oggetto ChompServer
	 * @param verbosityFlag
	 * @throws RemoteException
	 */
	
	public ChompServer (short verbosityFlag) throws RemoteException{
		super();
		this.verbosityFlag=verbosityFlag;
		
		verbose = (verbosityFlag&chompServerVerbose)!=0;
		println("verbosityflag:"+verbosityFlag);
		if((verbosityFlag&chompServerVerbose)!=0)
			println("chomp server verbose ");
		if((verbosityFlag&chompeerVerbose)!=0)
			println("chompeer verbose ");
		if((verbosityFlag&chompEngineVerbose)!=0)
			println("chomp engine verbose ");
		if((verbosityFlag&recoveryEngineVerbose)!=0)
			println("recovery engine verbose ");
		if((verbosityFlag&loadBalancerVerbose)!=0)
			println("load balancer verbose ");
		if((verbosityFlag&requestServerVerbose)!=0)
			println("request server verbose ");
		if((verbosityFlag&switchRmiVerbose)!=0)
			println("switch rmi verbose ");
		if(((verbosityFlag&metricsManagerVerbose))!=0)
			println("metrics manager verbose ");
		chompeer = new Chompeer(this,(chompeerVerbose&verbosityFlag)!=0); 
	}
	
	/**
	 * Metodo di inizializzazione di tutti i thread coinvolti nello stack protocollare del generico peer 
	 */
	public void chompEngineRun(){
		chomp_eng = new ChompEngine(chompeer, ((verbosityFlag&chompEngineVerbose)!=0));
		req_server = new RequestServer(chomp_eng,((byte)(requestServerVerbose&verbosityFlag))!=0);
		metricsMan= new MetricsManager(chomp_eng.getIndexTableManager(), ((verbosityFlag&metricsManagerVerbose)!=0));
		metricsThread= new Thread(metricsMan,"metrics manager");
		chompeer.tokenManagerSetup(req_server, metricsMan);
		rec_server= new RecoveryServer(((verbosityFlag&recoveryEngineVerbose)!=0), chomp_eng);
		rec_eng= new RecoveryEngine(chomp_eng, chompeer,((verbosityFlag&recoveryEngineVerbose)!=0));
		chomp_eng.setRecoveryEngine(rec_eng);
	}
	
	/**
	 * Metodo di avviamento dei componenti necessari al funzionamento del peer nell'anello
	 */
	public void chompeerRun(){
		chompeer.run();
	}
	
	/**
	 * Metodo di avviamento del componente RMI per la gestione delle richieste provenienti da un generico Client
	 */
	public void switchRmiRun(){
		try {
//			println("Avvio switch...");
			ss_rmi = new ServerServiceRMI(chompeer, ((byte)(verbosityFlag&switchRmiVerbose)!=0));
			metricsMan.setSwitch(ss_rmi);
			println("starting switch...");
			Naming.rebind(ss_rmi.name_service, ss_rmi);
			println("switch started!");
			metricsThread.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Metodo di avviamento del thread di gestione delle richieste provenienti al peer
	 */
	public void requestServerRun(){
		req_server.start();
	}
	
	/**
	 * Metodo di avviamento del thread server relativo al meccanismo di recovery sul peer
	 */
	public void recoveryServerRun(){
		Thread recoveryServerTh = new Thread (rec_server,"recovery server");
		recoveryServerTh.start();
		
	}
	
	/**
	 * Metodo di avviamento del thread di gestione del recovery sul peer
	 */
	public void recoveryEngineRun() {
		
		Thread recoveryEngTh = new Thread (rec_eng,"recovery engine");
		recoveryEngTh.start();
	}
	
	public int post(Traveler t, IndexTableName it_name, int key) throws Exception{
		int retval = chomp_eng.addIndexTableValue(t, it_name, key);
		chomp_eng.getHT().printTravelers();
		return retval; 	
	}
	
	public int getHashValueAverage(){
		return chomp_eng.getHashValueAverage();
	}
	
	public int getHashValueForFirstNodeAverage(int chompeer_id, int keySpace) {
		return chomp_eng.getHashValueForFirstNodeAverage(chompeer_id,keySpace);
	}
	
	
	
	public IndexValue get(IndexTableName []it_name_array, String CF_filter){
		return chomp_eng.query(it_name_array, CF_filter);
	}
	
	public ChompEngine getChompEngine(){
		return chomp_eng;
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+CHOMPSERVERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	/**
	 * Metodo di fusione tra livelli di recovery dipendentemente dal livello specificato come parametro in ingresso (recoveryLevel)
	 */
	public void mergeLevel(byte recoveryLevel){
		chomp_eng.mergeLevel(recoveryLevel);
	}

	
	public static void main(String[] args) {
		System.out.println("****************************");
		System.out.println("*	Let's CHOMP!	   *");
		System.out.println("****************************\n\n");
		ChompServer chompServer;
		try {
//			chompServer = new ChompServer((short)(/*chompEngineVerbose|*/chompServerVerbose/*|
//					chompeerVerbose|requestServerVerbose*/|switchRmiVerbose/*|recoveryEngineVerbose*/));
			System.out.println("flags:"+Short.parseShort(args[0]));
			chompServer = new ChompServer(Short.parseShort(args[0]));
		
		char c = 'a';
//		ArrayList<IndexTableName> itn_list = new ArrayList<IndexTableName>();
//		IndexValue iv;
		
		chompServer.chompeerRun();
		chompServer.chompEngineRun();
		chompServer.recoveryServerRun();
		/*the client wait 20 sec until begin its routine - this let the servers start*/
		Thread.sleep(BOOT_MILLISECONDS);
		chompServer.recoveryEngineRun();
		chompServer.requestServerRun();
		//			System.out.println("[MAIN]Avviato req server...");
		//			chompServer.recoveryEngineRun();


		Thread.sleep(BOOT_MILLISECONDS);
		chompServer.switchRmiRun();
		//			chompServer.recoveryEngineRun();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		
	}

	
	
}
