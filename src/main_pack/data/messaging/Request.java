package main_pack.data.messaging;

import java.io.Serializable;
import java.net.InetAddress;

import main_pack.data.IndexTableName;
import main_pack.data.Traveler;
import main_pack.utils.Timestamp;

/**
 * Class Request
 * 
 * Messaggio di richiesta dallo switch (peer che gestisce la richiesta proveniente dal client) al peer competente per la richiesta in esame 
 * Il formato del messaggio di richiesta si compone di:identificativo univoco numerico per il tracciamento della richiesta,
 * identificativo del tipo di operazione richiesta (POST, GET, REMOVE, REMOVE_ALL), IP del Client mittente che ha sottomesso la richiesta,
 * porta d'ascolto per la risposta sul Client, oggetto generico della richiesta (cast a Traveler),array di Index Tablle, hash, flag per 
 * livello di verbosita' delle stampe di controllo.
 * 
 */
public class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static final String REQUESTTAG="[REQUEST]";

	private int idReq;
	private InetAddress clientIP;
	private int port;
	private Object obj;
	private IndexTableName[] itn_array;
	private int reqType;
	private Boolean verbose;
	private int requestOwnerId=-1;
	private int hash;
	
	public static final int POST = 0;
	public static final int GET = 1;
	public static final int PUT = 2;
	public static final int REMOVE = 3;
	public static final int REMOVE_ALL = 4;
	
	
	
	//Versione con IndexTableName[] 
	public Request(int idReq, int reqType, InetAddress clientIP, int port, Object obj, IndexTableName[] itn_array,int hash, Boolean verbose)
	{
		this.idReq = idReq;
		this.reqType = reqType;
		this.clientIP = clientIP;
		this.port = port;
		this.obj = obj;
		this.itn_array = itn_array;
		this.verbose=verbose;
		this.hash=hash;
		
	}
	
	public Request(int idReq, int reqType, InetAddress clientIP, int port, Object obj,int hash)
	{
		this.idReq = idReq;
		this.reqType = reqType;
		this.clientIP = clientIP;
		this.port = port;
		this.obj = obj;
		this.hash=hash;
		
	}

	
	
	public int getRequestOwnerId() {
		return requestOwnerId;
	}

	public void setRequestOwnerId(int requestOwnerId) {
		this.requestOwnerId = requestOwnerId;
	}

	public int getIdReq() {
		return idReq;
	}

	public int getReqType() {
		return reqType;
	}

	public InetAddress getClientIP() {
		return clientIP;
	}
	
	public int getPort()
	{
		return port;
	}

	public Object getObj() {
		return obj;
	}
	
	public IndexTableName[] getAllItn()
	{
		return itn_array;
	}

	private String tag(){
		return (Timestamp.now()+"-"+REQUESTTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	public int getHash(){
		return hash;
	}
	
	public void print()
	{
		if (obj==null)
			println("DEBUG - obj null");
		
		StringBuilder itn_arrayToString=new StringBuilder("");
		for (int i=0; i<itn_arrayToString.length();++i)
			itn_arrayToString.append("\n"+itn_array[i].toString());
		println("[Request #"+idReq+"]:from "+((Traveler)obj).signature()+" \nitn_array:\n"+itn_arrayToString.toString());
	}
	
	public String signature()
	{
		return "[Request #"+idReq+"]:from "+((Traveler)obj).signature();
	}
	
}
