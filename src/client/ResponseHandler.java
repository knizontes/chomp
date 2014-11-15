package client;

import java.awt.BufferCapabilities;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;


import main_pack.data.Traveler;
import main_pack.data.messaging.ListIndexValue;
import main_pack.data.messaging.Response;
import main_pack.utils.Timestamp;

/**
 * 
 * Thread incaricato della gestione delle risposte ricevute lato CLIENT (Listener) in seguito alle richieste sottomesse al sistema Chomp 
 *
 */
public class ResponseHandler implements Runnable{

	private static final String RESPONSEHANDLERTAG="[RESPONSE HANDLER]";

	private Socket s;
	private static int counter = 0;
	private int id;
	private int reqId;
	private ClientEngine ce;
	private Listener listener;
	private Boolean verbose;
	//	private DataInputStream dis = null; 

	public ResponseHandler(Socket socket, int reqId, ClientEngine ce, Listener listener, Boolean verbose) throws IOException
	{
		id = counter;
		counter++;
		this.listener=listener;
		this.verbose=verbose;
		this.reqId=reqId;
		this.ce=ce;
		this.s = socket;
		println("Created Response Handler #"+id);
		//dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
	}

	public void printTravelers(Traveler[] travelers) {
		// TODO Auto-generated method stub
		for(int i=0; i<travelers.length; i++)
			travelers[i].print();
	}
	
	public void printTravelers(ArrayList<Traveler> travelers)
	{
		for(Traveler t : travelers)
			t.print();
	}

	public Traveler getOneTraveler()
	{
		try{
			InputStream is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			Traveler travs = (Traveler) ois.readObject();

			if(travs != null)
				travs.print(); 
			else
				println("NON ricevuto : " + travs.toString());

			ois.close();
			is.close();
			this.s.close();

			return travs;
		} 
		catch(Exception e)
		{
			println("errore ricezione nella socket 1");
		}
		return null;
	}

	
	public Traveler[] getTravelers()
	{
		try{
			InputStream is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			Traveler[] travelers = (Traveler[] ) ois.readObject();

			if(travelers != null)
				printTravelers(travelers); 
			else
				println("NON ricevuto : " + travelers.toString());

			ois.close();
			is.close();
			this.s.close();

			return travelers;
		} 
		catch(Exception e)
		{
			println("errore ricezione nella socket 2");
		}
		return null;

	}
	
	/**
	 * Routine di ricezione dei viaggiatori (oggetti Travelers) tramite socket instaurata con il peer competente per le risorse in trasmissione
	 */
	public void receiveTravelers()
	{
		//System.out.println("[CLIENT] Ricezione lista di Viaggiatori...(tramite ArrayList<Traveler>...");//TEST
		println("receive travelers routine");
		try{
			InputStream is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			Response response = (Response)ois.readObject();
			response.print();
			if (response.getIdReq()!=ce.getReqId())
				return;

			ArrayList<Traveler> travelers = response.getTravelers();
			println("receive travelers routine 2");
			if(travelers != null)
				printTravelers(travelers); 
			else
				println("NON ricevuto : " + travelers.toString());
			ois.close();
			is.close();
			this.s.close();
			listener.addResponse(travelers);
			println("sending signal to Client Engine");
			ce.signalGetResponse();
//			ce.waitGetResponse.signal();
			println("sended signal to Client Engine");
//			return travelers;
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			println("errore ricezione nella socket");
		}
		

	}

	private String tag(){
		return (Timestamp.now()+"-"+RESPONSEHANDLERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	public void run() {
		
		println("started");
		receiveTravelers();
//		this.getOneTraveler();
        println("end of routine, thread stops"); 
	}

}
