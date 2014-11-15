package main_pack.data.messaging;

import java.io.Serializable;
import java.util.ArrayList;

import main_pack.data.Traveler;

/**
 * 
 * Class Response
 * 
 * Messaggio di risposta dal peer competente per la risorsa richiesta verso il Client che ha originato la richiesta.
 * Il formato del messaggio si compone di: identificativo univoco numerico della risposta con riferimento all'id del 
 * messaggio di richiesta ricevuto, ArrayList di Traveler che soddisfino la query di ricerca sottomessa lato Client.
 *
 */
public class Response implements Serializable{
	
	private int idReq;
	private ArrayList<Traveler> travelers;
	
	public Response(int idReq, ArrayList<Traveler> travelers)
	{
		this.idReq = idReq;
		this.travelers = travelers;
	}

	public ArrayList<Traveler> getTravelers() {
		return travelers;
	}

	public void addTravelers(Traveler t) {
		travelers.add(t);
	}

	public int getIdReq() {
		return idReq;
	}
	
	public int getNumTravelers()
	{
		return travelers.size();
	}
	
	public void print()
	{
		System.out.println("["+idReq+"]:"+travelers.size());
	}
	

}
