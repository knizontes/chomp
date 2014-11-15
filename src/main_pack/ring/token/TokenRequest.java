package main_pack.ring.token;

import java.io.Serializable;
import java.net.InetAddress;

import main_pack.data.messaging.Request;

/**
 * 
 * Tipo di richiesta utente che deve essere eseguita dal nodo
 *
 */
public class TokenRequest implements Serializable{

	private Request req;
	private int redFromPeersNum=0;
	private int requestOwnerId;
	
	public TokenRequest (Request req, int requestOwnerId){
		this.req=req;
		this.requestOwnerId=requestOwnerId;
	}
	
	public void requestRed(){
		++redFromPeersNum;
	}

	public int getRequestType(){
		return req.getReqType();
	}
	
	public int getRedFromPeersNum(){
		return redFromPeersNum;
	}
	
	public Request getRequest(){
		return req;
	}
	
	public int getRequestOwnerId(){
		return requestOwnerId;
	}
	
}
