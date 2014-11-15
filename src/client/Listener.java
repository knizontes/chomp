package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;

import main_pack.data.Traveler;
import main_pack.utils.Timestamp;

/**
 * 
 * Thread d'ascolto (lato CLIENT) delle risposte provenienti dal sistema Chomp alle richieste sottomesse.
 * Ricompone le risposte a query complesse sulla base degli id contenuti nei messaggi di risposta provenienti eventualmente
 * da peer diversi (competenti per risorse diverse) dell'anello. 
 *
 */

//thread creato dal CLIENT che resta in ascolto della 
//risposta di un peer nell'anello
public class Listener implements Runnable {

	private static final String CLIENTLISTENERTAG="[CLIENT LISTENER]";

	//attributi per usare socket TCP per ottenere una risposta dall'anello
	//private Socket socket;
	public ServerSocket serverSocket;
	public final static int LISTEN_PORT = 3334;
	private Boolean verbose;
	private ClientEngine ce;
	private ArrayList<ArrayList<Traveler>> responses;
	//---------------------------------------------

	public Listener(Boolean verbose, ClientEngine ce)
	{
		this.verbose=verbose;
		this.ce=ce;
		try {
			serverSocket = new ServerSocket(LISTEN_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		responses = new ArrayList<ArrayList<Traveler>>();
	}

	
	public void addResponse(ArrayList<Traveler> response){
		responses.add(response);
	}

	private String tag(){
		return (Timestamp.now()+"-"+CLIENTLISTENERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	@Override
	public void run() {
		int counter=0;
		println("Avviato gestore richieste lato client...");
		while(true){
			try {

					Socket socket = serverSocket.accept();
					ResponseHandler rl = new ResponseHandler(socket,ce.getReqId(),ce,this,true);

					Thread rh = new Thread(rl,"response handler"+ce.getReqId());
					rh.start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


	public String[] queryResponse() {
		if (responses.size()<=0)
			return new String [0];
		ArrayList<Traveler> response = responses.get(0);
		ArrayList<Traveler> tmp;
		for (int i=1; i<responses.size();++i){
			if (response.size()<=0)
				break;
			tmp=new ArrayList<Traveler>();
			for (Traveler t:response){
				for (int j=0; j<responses.get(i).size();++j){
					if (t.getCF().equals(responses.get(i).get(j).getCF()))
						tmp.add(t);
				}
			}
			response=tmp;
			
		}
		String []retval = new String [response.size()];
		for (int i=0; i<response.size();++i)
			retval[i]=response.get(i).privateSignature();
		return retval;
	}

//	public static void main(String[] args) {
//
//		Listener l = new Listener(true);
//		l.run();
//
//	}

}
