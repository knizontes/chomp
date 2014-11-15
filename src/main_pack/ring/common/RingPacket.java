package main_pack.ring.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

import main_pack.ring.token.Token;

/**
 * 
 * Classe rappresentativa del generico pacchetto UDP scambiato come messaggio sull'anello.
 * Formato composto da:
 * <p> ip mittente da cui e' originato il datagramma;
 * <p> ip destinatario del datagramma;
 * <p> flag rappresentativo dell'operazione richiesta;
 *  
 *
 */
public class RingPacket implements Serializable{

	public static final byte SEND=0;
	public static final byte NOP=1;
	public static final byte REBOOT=2;
	public static final byte JOIN=3;
	public static final byte WHO_AM_I = 4;
//	public static final byte GET_NEW_NODE_ID = 5;
	public static final byte GET_NEW_ID = 6;
	public static final byte REMOVE=7;
	

	private int src;
	private int dst;
	
	private byte flag;
//	private String timestamp;
	private InetAddress ip;
	private InetAddress busiestIp;
	private int to_remove;
	private Token token;
	private long pingNum;
	
		
	public RingPacket(int src, int dst, byte flag, long pingNum){
		this.src=src;
		this.dst=dst;
		this.flag=flag;
//		this.timestamp=timestamp;
		to_remove=-1;
		this.pingNum=pingNum;
//		this.ip=ip;
	}
	
	public void setToken(Token t){
		token=t;
	}
	
	public Token getToken(){
		return token;
	}
	
	public Boolean tokenPiggybacked(){
		if (token==null)
			return false;
		return true;
	}
	
	public void setDst(int dst_id){
		dst=dst_id;
	}

	public int getTo_remove() {
		return to_remove;
	}

	public void setTo_remove(int to_remove) {
		this.to_remove = to_remove;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
	public long getPingNum(){
		return pingNum;
	}

	public InetAddress getBusiestIp() {
		return busiestIp;
	}

	public void setBusiestIp(InetAddress busiestIp) {
		this.busiestIp = busiestIp;
	}

	public int getSrc() {
		return src;
	}

	public int getDst() {
		return dst;
	}

	public byte getFlag() {
		return flag;
	}

	
	
}
