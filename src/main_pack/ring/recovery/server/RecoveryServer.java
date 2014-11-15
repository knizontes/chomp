package main_pack.ring.recovery.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;


import main_pack.chomp_engine.ChompEngine;
import main_pack.data.messaging.ListIndexValue;
//import trasferimentoTCP.ServerGetList;
import main_pack.utils.Timestamp;

/**
 * 
 * Riceve le copie delle risorse primarie di un'altro nodo, le aggiunge negli opportuni recovery level dopo aver 
 * richiesto gli eventuali dati struttrati traveler sconosciuti
 *
 */
public class RecoveryServer implements Runnable{
	

	public static final int RECOVERY_SERVER_PORT = 3336; 
	private final static String RECOVERY_SERVER_TAG="[RECOVERY SERVER]";
	
	private ServerSocket ss;
//	private Socket clientSocket;

	
	private Boolean verbose;
	private ChompEngine chomp_eng;
	
	//private ListIndexValue lista;


	public RecoveryServer(Boolean verbose, ChompEngine chomp_eng)
	{
		this.verbose=verbose;
		this.chomp_eng= chomp_eng;
		try 
		{
			ss = new ServerSocket(RECOVERY_SERVER_PORT);
//			System.out.println("[SERVER_GET_LIST] - Server creato su porta "+RECOVERY_SERVER_PORT);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}


//	private ListIndexValue getList() 
//	{
////        for(;;)
////        {	
//		    ListIndexValue l = null;
//			System.out.println("[Server] - In ascolto sulla porta " + ss);
//	
//			try 
//			{
//				clientSocket = ss.accept();
//				InputStream is = clientSocket.getInputStream();
//				ObjectInputStream ois = new ObjectInputStream(is);
//	
//				l = (ListIndexValue) ois.readObject();
//	
//				if(l != null)
//					System.out.println("ricevuto : " + l.toString()); 
//				else
//					System.out.println("NON ricevuto : " + l.toString());
//	
//				ois.close();
//				is.close();
//				clientSocket.close();
//			}
//			catch (IOException e) 
//			{
//				// TODO Auto-generated catch block
//				System.out.println("ioexception");
//				e.printStackTrace();
//			} 
//			catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				System.out.println("cnfexception");
//				e.printStackTrace();
//			}
//
////			 System.out.println("LIV : "+l.toString());
////			 for( int i=0; i<l.size(); i++)
////				{
////					System.out.println(l.getListElement(i).getNameIndexTable());
////					for(int k =0; k<l.getListElement(i).getCFS().size(); k++)
////						System.out.println(l.getListElement(i).getCFS().get(k));
////				}
////        }	
//		return l;
//	}

	@Override
	public void run() {
		println("recovery server started");
		for(;;){
			try {
				Socket clientSocket;
				clientSocket = ss.accept();
				RecoveryServerThread serverThread = new RecoveryServerThread(clientSocket,chomp_eng);
				serverThread.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	private String tag(){
		return (Timestamp.now()+"-"+RECOVERY_SERVER_TAG);
	}
	
	
      

	


	

}
