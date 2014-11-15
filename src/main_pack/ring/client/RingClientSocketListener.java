package main_pack.ring.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Callable;


import main_pack.ring.common.RingCommon;
import main_pack.ring.common.RingPacket;
import main_pack.utils.Timestamp;

/**
 * 
 * RingClientSocketListener Class
 * 
 * Thread d'ascolto per i messaggi in ricezione lato Client.
 *
 */
public class RingClientSocketListener  implements Callable<RingPacket>{

//	private byte[] data;
	
	private static final String RINGCLIENTLISTENERTAG="[RING CLIENT LISTENER]";
	
	private DatagramPacket receivedPacket;
	private RingPacket pp;
	private DatagramSocket clientSocket;
	private int listen_port;
	private Boolean verbose;
	
	public RingClientSocketListener( DatagramPacket receivedPacket, RingPacket pp, DatagramSocket clientSocket, Boolean verbose){
//		this.data=data;
		this.receivedPacket=receivedPacket;
		this.pp=pp;
//		this.listen_port=listen_port;
		this.clientSocket = clientSocket;
		this.verbose=verbose;
	}
	
	public RingPacket call() throws Exception {
//		PingCommon.clearBuffer(data);
		/*wait until datagram packet arrival*/
		println("waiting for ping reply...");
		try{
			clientSocket.receive(receivedPacket);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		/*resetting the socket - erase packet from the queue if any*/
//		resetSocket();
		println("got a ping reply from host:"+receivedPacket.getAddress().getHostAddress());
		pp = RingCommon.getPingPacket(receivedPacket);
//		System.out.println(tag()+"***");
		return pp;
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+RINGCLIENTLISTENERTAG);
	}

	private void resetSocket(){
		clientSocket.close();
		try {
			clientSocket = new DatagramSocket(listen_port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resetPingSocketListener( DatagramPacket receivedPacket, RingPacket pp){
//		this.data=data;
		this.receivedPacket=receivedPacket;
		this.pp=pp;
//		this.clientSocket=clientSocket;
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
//	private void clearBuffer(byte [] buffer){
//		for(int i=0; i<buffer.length;++i)
//			buffer[i]=0;
//	}

}
