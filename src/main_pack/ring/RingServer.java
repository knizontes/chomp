package main_pack.ring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.sound.midi.Receiver;



import main_pack.ring.client.RingClient;
import main_pack.ring.common.RingCommon;
import main_pack.ring.common.RingPacket;
import main_pack.ring.token.TokenManager;
import main_pack.utils.Timestamp;

/**
 * 
 * RingServer Class
 * 
 * Server Thread che costituisce l'elemento Server del generico peer necessario per la ricezione dei segnali
 * sull'anello e responsabile delle operazioni di:
 * <p> JOIN: join di un nuovo nodo nell'anello;
 * <p> REMOVE: rimozione di un nodo dall'anello;
 * <p> WHO_AM_I: comunicazione dell'ip ad un peer che ne faccia richiesta;
 * <p> GET_NEW_ID: ribilanciamento del carico mediante assegnazione di un nuovo id per per ripartizione del carico del peer sovraccaricato
 *
 */
public class RingServer implements Runnable {

	public final static int LISTEN_PORT=3333;
	private final static String PINGSERVERTAG="[RING SERVER]";
	
	private Chompeer chompeer;
	private Boolean verbose = false;
	private TokenManager tokenMan;
	
	private Boolean debugBoolean=true;
	
	public RingServer (Chompeer chompeer,TokenManager tokenMan, Boolean verbose){
		this.verbose = verbose;
		this.chompeer=chompeer;
		this.tokenMan=tokenMan;
	}
	
	public TokenManager getTokenManager(){
		return tokenMan;
	}
	
	public void run(){
		println("ping server thread started...");
		
		/*Payload of the UDP datagram*/
		byte[] receiveData = new byte [4096];
		DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
		RingPacket pp;
		DatagramSocket serverSocket;
		
		/*The joined flag is set if the chompeer already knows his id in the overlay and his ip-address */
		Boolean joined=false;
		try {
			serverSocket = new DatagramSocket(LISTEN_PORT);
		
			while (true){
		    	/*wait for receive a datagram packet*/
				serverSocket.receive(receivePacket);
				
				pp = RingCommon.getPingPacket(receivePacket);
				if (chompeer.getChompeer_ip()==null){
					chompeer.setChompeer_ip(pp.getIp());
					println("my ip is:"+chompeer.getChompeer_ip().toString().substring(1));
				}
				if ((chompeer.getChompeer_id()<0)&&(pp.getDst()!=-1)){
					chompeer.setChompeer_id(pp.getDst());
					println("my id is:"+chompeer.getChompeer_id());
				}
				if (pp.getFlag()==RingPacket.WHO_AM_I){
					println("got a WHO_AM_I message from host "+receivePacket.getSocketAddress().toString().substring(1)+"...");
					whoamiReply(receivePacket);
				}
				else if (pp.getFlag()==RingPacket.GET_NEW_ID){
					
					
					println("got a GET_NEW_ID message from host "+receivePacket.getSocketAddress().toString().substring(1)+"...");
					getNewNodeIdReply(receivePacket);					
				}
				else if (pp.getFlag()==RingPacket.JOIN){
					println("got a JOIN message from host "+receivePacket.getSocketAddress().toString().substring(1)+"...");
					if (joinReply(receivePacket,pp,joined)<0)
						println("No match between finger table ip for node and sending node one");
					joined=true;
				}else if (pp.getFlag()==RingPacket.REMOVE){
					println("got a REMOVE message from host "+receivePacket.getSocketAddress().toString().substring(1)+"...");	
					remove(pp);
				}
				else{
					println("got the ("+pp.getPingNum()+") ping from host "+receivePacket.getSocketAddress().toString().substring(1)+"...");
					pingRespond(pp,receivePacket);
				}
		    }
		} catch (IOException e) {
			println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			println("bbbbbbbbbbbbbbbbbbbbbbbbbbb");
			e.printStackTrace();
		} catch (Exception e) {
			println("ccccccccccccccccccccccccccc");
			e.printStackTrace();
		}
	}
	
	private void remove(RingPacket pp) {
		
		chompeer.removePeer(pp.getTo_remove());
		
	}


	private void whoamiReply(DatagramPacket receivedPacket) throws IOException{
		InetAddress dstIp = receivedPacket.getAddress();
		RingPacket pingResponsePacket; 
		byte [] data;
		byte [] toSendData;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		DatagramSocket responseSocket = new DatagramSocket();
		DatagramPacket responsePacket;
		int busiest_id = chompeer.getBusiestNodeId();
		pingResponsePacket = new RingPacket(-1, busiest_id, RingPacket.WHO_AM_I, -1);
		pingResponsePacket.setIp(dstIp);
		pingResponsePacket.setBusiestIp(chompeer.getBusiestNodeIp());
		
		oos.writeObject(pingResponsePacket);
		data = baos.toByteArray();
		toSendData = RingCommon.dataFormatting(data);
		responsePacket = new DatagramPacket(toSendData, toSendData.length, dstIp, RingClient.LISTEN_PORT);
		
		responseSocket.send(responsePacket);
		responseSocket.close();
		println("join replied to host:"+dstIp.getHostAddress()+"...");
	}
	
	/**
	 * 
	 * @param receivedPacket
	 * @param pp
	 * @param joined
	 * @return joinRespond returns 1 if the node which sent the packet is already in the fingertable, 2 if the node is not in the ft 
	 * @throws IOException
	 */
	
	private int joinReply(DatagramPacket receivedPacket,RingPacket pp,Boolean joined) throws IOException {
		println("join procedure started...");
		InetAddress ip = pp.getIp();
		int id = pp.getDst();
		int src_id = pp.getSrc();
		PeerNode pn = chompeer.getPeerNode(id);
		PeerNode srcNd = chompeer.getPeerNode(src_id);
		InetAddress dstIp = receivedPacket.getAddress();
		RingPacket pingResponsePacket; 
		byte [] data;
		byte [] toSendData;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		DatagramSocket responseSocket = new DatagramSocket();
		DatagramPacket responsePacket;

//		System.out.println("joining message...");
		
		/*if the source node is a new node it must be added to the finger table*/
		if (srcNd==null){
			println("the join message comes from a new node...");
			chompeer.addPeerNode(src_id, receivedPacket.getAddress().toString().substring(1));
			srcNd=chompeer.getPeerNode(id);
			chompeer.sendTables();
		}

		
		/*if the <ip,id> couple declared in the packet matches with the finger-table node fields reply*/
		if (pn.getIp().ipToInetAddress().equals(ip)){
			/*If the chompeer do not know yet its ip and id in the overlay use the field of the join to set up*/
			if (!joined){
				chompeer.setChompeer_id(id);
			}
			pingResponsePacket = new RingPacket(id, -1, RingPacket.JOIN, -1);
			
			oos.writeObject(pingResponsePacket);
			data = baos.toByteArray();
			toSendData = RingCommon.dataFormatting(data);
			responsePacket = new DatagramPacket(toSendData, toSendData.length, dstIp, RingClient.LISTEN_PORT);
			
			responseSocket.send(responsePacket);
			responseSocket.close();
			println("join replied to host:"+dstIp.getHostAddress()+"...");

			return 1;
		}
		return -1;
		
	}


	private void getNewNodeIdReply (DatagramPacket receivedPacket) throws IOException{

		byte flag;
		RingPacket responseData;
		byte [] data;
		byte [] toSendData;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		DatagramPacket responsePacket;
		InetAddress dstAddress;
		int new_id= nextIdAlgo();
		System.out.println();
		System.out.println("[RING SERVER DEB]the new id i'm assignig is:"+new_id);
		System.out.println();
		

		DatagramSocket responseSocket = new DatagramSocket();
		dstAddress = receivedPacket.getAddress();
		
		/*preparing payload*/
		responseData=new RingPacket(-1, new_id, RingPacket.NOP, -1);
		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		oos.writeObject(responseData);
		data = baos.toByteArray();
		toSendData = RingCommon.dataFormatting(data);
		responsePacket = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingClient.LISTEN_PORT);
		try {
			responseSocket.send(responsePacket);
		}catch (Exception e) {
			e.printStackTrace();
		}
		println("GET NEW ID reply sent to "+responsePacket.getAddress().toString().substring(1)+":"+responsePacket.getPort()+" length:"+responsePacket.getData().length);
	
	}
	

	private void pingRespond(RingPacket pp,DatagramPacket receivedPacket) throws IOException{
		byte flag;
		RingPacket responseData;
		byte [] data;
		byte [] toSendData;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		DatagramPacket responsePacket;
		InetAddress dstAddress;
		PeerNode srcPeer;
		DatagramSocket responseSocket = new DatagramSocket();
		
		srcPeer=chompeer.getPeerNode(pp.getSrc());
		if (srcPeer!=null){
			println("src peer "+srcPeer.getIp().ipToString()+" not null...");
			flag=RingPacket.NOP;
		}
		else{
			println(pp.getSrc()+" src peer null");
			flag=RingPacket.REBOOT;
		}
		dstAddress = pp.getIp();

		/*preparing payload*/
		responseData=new RingPacket(pp.getDst(), pp.getSrc(), flag, -1);
		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		oos.writeObject(responseData);
		data = baos.toByteArray();
		toSendData = RingCommon.dataFormatting(data);

		responsePacket = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingClient.LISTEN_PORT);
		try {
			responseSocket.send(responsePacket);
		}catch (Exception e) {
			println("***************************");
			e.printStackTrace();
		}
		if (pp.tokenPiggybacked()){
			println("handling the token");
			tokenMan.tokenRoutine(pp.getToken());
			println("token handled");
		}
		println("ping ("+pp.getPingNum()+") reply sent to "+responsePacket.getAddress().toString().substring(1)+
				":"+responsePacket.getPort()+" length:"+responsePacket.getData().length);
		if (debugBoolean&&(pp.getPingNum()>7)){
			debugBoolean=false;
			tokenMan.generateToken();
		}
	}
	
	
	/*the algorithm of choice of a new id near the busiest one*/
	private int nextIdAlgo(){
		int retval = chompeer.getHashValueAverage();
		System.out.println("[RING SERVER DEB] hash average is:"+retval);
		if (retval<0){
			retval= chompeer.getPredecessorId();
			if ((chompeer.getChompeer_id()-retval)<0)
				return (chompeer.getKeySpace()+retval)/2;
			return (retval+chompeer.getChompeer_id())/2;
		}
			
		return retval;
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	private void print(String s){
		if (verbose)
			System.out.print(s);
	}
	
	
	
	private String tag(){
		return (Timestamp.now()+"-"+PINGSERVERTAG);
	}

	
}
















