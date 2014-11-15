package main_pack.ring.token;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 
 * Oggetto che contiene una coppia (Chompeer id, load factor) descrittiva del carico utente (load factor) sopportato dal peer (Chompeer id)
 *
 */
public class TokenRecord implements Serializable{
	private InetAddress chompeer_ip;
	private int chompeer_id;
	private long loadFactor;
	
	public TokenRecord(InetAddress chompeer_ip, int chompeer_id, long loadFactor){
		this.chompeer_ip=chompeer_ip;
		this.chompeer_id=chompeer_id;
		this.loadFactor = loadFactor;
	}

	public InetAddress getChompeer_ip() {
		return chompeer_ip;
	}

	public int getChompeer_id() {
		return chompeer_id;
	}
	
	public long getLoadFactor(){
		return loadFactor;
	}
	
	public void setLoadFactor(long loadFactor){
		this.loadFactor=loadFactor;
	}
	
	
	
}
