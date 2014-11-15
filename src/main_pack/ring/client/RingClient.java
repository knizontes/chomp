package main_pack.ring.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;




import main_pack.chomp.ChompServer;
import main_pack.ring.Chompeer;
import main_pack.ring.PeerNode;
import main_pack.ring.RingServer;
import main_pack.ring.common.RingCommon;
import main_pack.ring.common.RingPacket;
import main_pack.ring.token.Token;
import main_pack.utils.Timestamp;

/**
 * 
 * RingClient Class
 * 
 * Client Thread per la gestione del comportamento Client del generico peer. 
 * Implementa le funzioni per l'invio di Ring Message, la routine di ping, il piggybacking del token e l'affidabilita' della
 * comunicazione su UDP
 *
 */
public class RingClient extends RingCommon implements Runnable{
	
	public final static int LISTEN_PORT=3332;
	private final static String RINGCLIENTTAG="[RING CLIENT]";
	public final static int RETRY=5;//5; 
	public final static int PING_INTERLEAVING_TIME_MS=5000;
	public final static int BOOT_PHASE_INTERLEAVING_TIME_MS=15000;
	public final static int BOOT = 0;
	public final static int JOIN = 1;
//	public final static int TOKEN_PING_WAIT=5;
	
	private Chompeer chompeer;
	private DatagramSocket clientListenSocket;
	private DatagramSocket clientSendSocket;
	private Lock clientSendSocketLock;
	
	private Lock pingLock = new ReentrantLock();
	private Condition pingActivate = pingLock.newCondition();
	private Boolean pingOn = true;
	
	private Token token;
	private long pingCounter=0;
//	private int tokenPingWaitCounter=0;
	

	private int successor;
	
	private long pingnumDebug=0;
	private Boolean verbose = false;
	
	
	public RingClient(Chompeer chompeer, Boolean verbose){
		this.chompeer=chompeer;
		this.verbose = verbose;
		clientSendSocketLock = new ReentrantLock();
		try {
			clientListenSocket = new DatagramSocket(LISTEN_PORT);
			clientSendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void resetSuccessor(){
		successor = chompeer.getSuccessor();
	}
	
	
	public void lockSendSocket(){
		clientSendSocketLock.lock();
	}

	public void unlockSendSocket(){
		clientSendSocketLock.unlock();
	}
	
	

	/**
	 * The ring booting procedure send a join signal to all the known chompeer
	 */
	public void ringBoot(){
		println("ring boot procedure started...");
		/*get the list of known peer from the configuration file*/
		PeerNode[] peerNodes = chompeer.getPeerNodes();
		byte[] data = new byte[1024];
		byte[] toSendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		DatagramPacket toSendPacket = new DatagramPacket(data,data.length);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		RingPacket pp;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		DatagramPacket joinPacket;
		InetAddress dstAddress;
		ExecutorService executor;
		Future<RingPacket> future;
		InetAddress dstIp;
		
		
		try {
		
			/*the executor allows to wait for a reply until timeout*/
			executor = Executors.newSingleThreadExecutor();
			
			/*sending the signal to all known chompeers*/
			for (int i=0; i<(peerNodes.length-1);++i){
				/*get the ip of the next node to contact*/
				dstIp = peerNodes[i].getIp().ipToInetAddress();
				
				/*creating  and filling join packet*/
				pp = new RingPacket(chompeer.getChompeer_id(),peerNodes[i].getNode_id(),RingPacket.JOIN,-1);
				pp.setIp(dstIp);
			

				dstAddress = peerNodes[i].getIp().ipToInetAddress();
				
				/*creating the payload byte array with the join packet*/
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(pp);
				data = baos.toByteArray();
				toSendData = RingCommon.dataFormatting(data);
				
				/*create the datagram with the prepared payload*/
				joinPacket = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
				
				/*preparing the receive datagram*/
				RingCommon.clearBuffer(receiveData);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				/*initialize the future with the ping packet*/
				future = executor.submit(new RingClientSocketListener( receivePacket, pp, clientListenSocket, verbose));
				println("sending join to host:"+dstAddress.getHostAddress()+"...");
				
				RingPacket debugP;
				
				/*try to send the join packet RETRY times*/
				int attempts;
				for (attempts=0; attempts<RETRY; ++attempts){
					/*wait 5 seconds for the join reply*/
					try {
						/*sending the join packet to node*/
						
						lockSendSocket();
						clientSendSocket.send(joinPacket);
						unlockSendSocket();
						println("sent join to host:"+dstAddress.getHostAddress()+"...");


						debugP=future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					} catch (ExecutionException e) {
						e.printStackTrace();
						break;
					} catch (TimeoutException e) {
						/*the dst node has not replied until 5 seconds*/
						System.out.println("[RING CLIENT DEBUG]host "+dstAddress.getHostAddress()+" seems down during boot ring routine...");
						println("host "+dstAddress.getHostAddress()+" seems down during boot ring routine...");
						continue;
					} 
					println("got join reply...");
					break;
				}		
				
				/*if RETRY attempts have been done then remove the peer form the list of known peers*/
				if (attempts>=RETRY){
					System.out.println("[RING CLIENT DEBUG]removing peer:"+peerNodes[i].getNode_id());
					chompeer.removePeer(peerNodes[i].getNode_id());
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		println(" boot procedure ended...\n\n");
	}
	
	
	/**
	 * 
	 * Procedura di assegnazione nuovo id al nodo che esegue la join
	 * 
	 */
	public int getNewId(int fromNodeId){
		RingPacket requireIdPacket;
		DatagramPacket receivePacket;
		DatagramPacket getIDDatagram;
		InetAddress dstAddress;
		ExecutorService executor;
		Future<RingPacket> future;
		
		byte[] data = new byte[1024];
		byte[] toSendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		
		Boolean replied;
		int attempts;
		
		try {
			/*the executor allows to wait for a reply until timeout*/
			executor = Executors.newSingleThreadExecutor();
			
			/*creating  and filling require id packet*/
			requireIdPacket = new RingPacket(-1,fromNodeId,RingPacket.GET_NEW_ID,-1);
			requireIdPacket.setIp(chompeer.getPeerNode(fromNodeId).getIp().ipToInetAddress());
			dstAddress = chompeer.getPeerNode(fromNodeId).getIp().ipToInetAddress();
			
			/*creating the payload byte array with the join packet*/
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(requireIdPacket);
			data = baos.toByteArray();
			toSendData = RingCommon.dataFormatting(data);
			

			/*create the datagram with the prepared payload*/
			getIDDatagram = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
			
			/*preparing the receive datagram*/
			RingCommon.clearBuffer(receiveData);
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			/*initialize the future with the ping packet*/
			future = executor.submit(new RingClientSocketListener( receivePacket, requireIdPacket, clientListenSocket,verbose));
			println("sending GET_NEW_ID message to host:"+dstAddress.getHostAddress()+"...");
			
			
			for (attempts=0; attempts<RETRY; ++attempts){
				/*wait 5 seconds for the join reply*/
				try {
					
//					/*initialize the future with the ping packet*/
//					future = executor.submit(new PingClientSocketListener( receivePacket, requireIdPacket, clientListenSocket));
					
					/*sending the join packet to node*/
					lockSendSocket();
					clientSendSocket.send(getIDDatagram);
					unlockSendSocket();
					println("sent GET_NEW_ID message to host:"+dstAddress.getHostAddress()+"...");
					System.out.println("[RING CLIENT DEB]sent GET_NEW_ID message to host:"+dstAddress.getHostAddress()+"...");


					future.get(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				} catch (ExecutionException e) {
					e.printStackTrace();
					break;
				} catch (TimeoutException e) {
					/*the dst node has not replied until 5 seconds*/
					println("host "+dstAddress.getHostAddress()+" seems down...");
					continue;
				} 
				try {
					requireIdPacket= RingCommon.getPingPacket(receivePacket);
					System.out.println("[RING CLIENT]old chompeer id:"+chompeer.getChompeer_id());
					chompeer.setChompeer_id(requireIdPacket.getDst());
					System.out.println("[RING CLIENT]new chompeer id:"+chompeer.getChompeer_id());
					chompeer.addPeerNode(chompeer.getChompeer_id(), chompeer.getChompeer_ip().toString().substring(1));
					chompeer.printFingerTable();
					println("got GET_NEW_ID reply, my id is "+chompeer.getChompeer_id()+"...");
					return requireIdPacket.getDst();
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				replied=true;
				break;
			}
			
		
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	/**
	 * Whoami routine for ip recognition
	 */
	private void sendWhoAmI(){
		Random randomGenerator = new Random(System.nanoTime());
		int rnd = randomGenerator.nextInt(chompeer.getPeersNum());
		PeerNode pn ;
		Boolean replied=false;
		
		
		println("===|ring whoami procedure started...|===");
		
		/*get the list of known peer from the configuration file*/
		
		byte[] data = new byte[1024];
		byte[] toSendData = new byte[1024];
		byte[] receiveData = new byte[1024];
//		DatagramPacket toSendPacket = new DatagramPacket(data,data.length);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		RingPacket pp;
		RingPacket requireIdPacket;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		DatagramPacket whoamiDatagram;
		DatagramPacket getIDDatagram;
		InetAddress dstAddress;
		ExecutorService executor;
		Future<RingPacket> future;
		InetAddress dstIp;
//		int newId;
		
		
		try {
		
			/*the executor allows to wait for a reply until timeout*/
			executor = Executors.newSingleThreadExecutor();
			
			/*sending the signal to all known chompeers*/
			for (int i=0; (!replied)&&(i<(chompeer.getPeersNum()-1));++i){
				pn = chompeer.getPeerNodeFromPosition((rnd+i)%(chompeer.getPeersNum()-1));
				
				/*get the ip of the next node to contact*/
				dstIp = pn.getIp().ipToInetAddress();
				
				
				/*creating  and filling join packet*/
				pp = new RingPacket(-1,pn.getNode_id(),RingPacket.WHO_AM_I,-1);
				pp.setIp(dstIp);
				dstAddress = pn.getIp().ipToInetAddress();
//				println("contacting node:"+pn.getIp().ipToString());
				
				
				/*creating the payload byte array with the join packet*/
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(pp);
				data = baos.toByteArray();
				toSendData = RingCommon.dataFormatting(data);
				
				/*create the datagram with the prepared payload*/
				whoamiDatagram = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
				
				/*preparing the receive datagram*/
				RingCommon.clearBuffer(receiveData);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				/*initialize the future with the ping packet*/
				future = executor.submit(new RingClientSocketListener( receivePacket, pp, clientListenSocket,verbose));
				println("sending whoami to host:"+dstAddress.getHostAddress()+"...");
				
				RingPacket debugP;
				
				/*try to send the whoami packet RETRY times*/
				int attempts;
				for (attempts=0; attempts<RETRY; ++attempts){
					/*wait 5 seconds for the join reply*/
					try {
						/*sending the join packet to node*/
						
						lockSendSocket();
						clientSendSocket.send(whoamiDatagram);
						unlockSendSocket();
						println("sent whoami to host:"+dstAddress.getHostAddress()+"...");


						debugP=future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					} catch (ExecutionException e) {
						e.printStackTrace();
						break;
					} catch (TimeoutException e) {
						/*the dst node has not replied until 5 seconds*/
						println("host "+dstAddress.getHostAddress()+" seems down...");
						continue;
					} 
					try {
						pp= RingCommon.getPingPacket(receivePacket);
						chompeer.setChompeer_ip(pp.getIp());
						println("got whoami reply, my address is "+chompeer.getChompeer_ip().toString().substring(1)+"...");
						/*if the couple <node id, node ip> was in the configuration file, the node already knows its id from the table*/
						int id;
						if ((id=chompeer.getIdFromIp(chompeer.getChompeer_ip().toString().substring(1)))>=0){
							chompeer.setChompeer_id(id);
							println("found my ip in the finger table, my id is "+chompeer.getChompeer_id()+"...");
							return;
						} 
						else
							println("my ip is not in the fingertable, contacting the busiest node to get an id");
							
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
//					replied=true;
					break;
				}		
								
				/*creating  and filling require id packet*/
				requireIdPacket = new RingPacket(pp.getDst(),-1,RingPacket.GET_NEW_ID,-1);
				requireIdPacket.setIp(dstIp);
				dstAddress = pp.getBusiestIp();
				
				/*creating the payload byte array with the join packet*/
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(requireIdPacket);
				data = baos.toByteArray();
				toSendData = RingCommon.dataFormatting(data);
				
				/*create the datagram with the prepared payload*/
				getIDDatagram = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
				
				/*preparing the receive datagram*/
				RingCommon.clearBuffer(receiveData);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				/*initialize the future with the ping packet*/
				future = executor.submit(new RingClientSocketListener( receivePacket, requireIdPacket, clientListenSocket,verbose));
				println("sending GET_NEW_ID message to host:"+dstAddress.getHostAddress()+"...");
				/*try to send the join packet RETRY times*/
				
				for (attempts=0; attempts<RETRY; ++attempts){
					/*wait 5 seconds for the join reply*/
					try {
						
//						/*initialize the future with the ping packet*/
//						future = executor.submit(new PingClientSocketListener( receivePacket, requireIdPacket, clientListenSocket));
						
						/*sending the join packet to node*/
						lockSendSocket();
						clientSendSocket.send(getIDDatagram);
						unlockSendSocket();
						println("sent GET_NEW_ID message to host:"+dstAddress.getHostAddress()+"...");


						debugP=future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					} catch (ExecutionException e) {
						e.printStackTrace();
						break;
					} catch (TimeoutException e) {
						/*the dst node has not replied until 5 seconds*/
						println("host "+dstAddress.getHostAddress()+" seems down...");
						continue;
					} 
					try {
						requireIdPacket= RingCommon.getPingPacket(receivePacket);
						chompeer.setChompeer_id(requireIdPacket.getDst());
						chompeer.addPeerNode(chompeer.getChompeer_id(), chompeer.getChompeer_ip().toString().substring(1));
						println("got GET_NEW_ID reply, my id is "+chompeer.getChompeer_id()+"...");
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					replied=true;
					break;
				}
				
			}
			
			
			
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		println(" whoami procedure ended...\n\n");
	}
		
		
	
	
	public void run() {
		/*the client wait 15 sec until begin its routine - this let the servers start*/
		try {
			Thread.sleep(ChompServer.BOOT_MILLISECONDS);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		println("ring client thread started...");
		
		/*if the node do not know its ip in the overlay send a join to a random host.
		 * When the routine ends the peer knows both its ip and id in the overlay.
		 */
		if (chompeer.getChompeer_ip()==null){
			sendWhoAmI();
			/*setup node and ring before the node join*/
			chompeer.sendNewNodeJoinSignals();
		}
		
		try {
			Thread.sleep(BOOT_PHASE_INTERLEAVING_TIME_MS);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		/*starting the booting procedure - joining the overlay*/
		ringBoot();
		
		/*the client wait 15 sec after the boot (join) routine*/
		try {
			Thread.sleep(BOOT_PHASE_INTERLEAVING_TIME_MS);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	
		System.out.println("\n");
		println("ping client routine started...");
	
		/*initializing the ping packet*/
		byte[] data;
		byte [] toSendData;
		byte [] receiveData=new byte [1024];
		RingPacket pp;
		int attempts=0;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		DatagramPacket sendPacket;
		DatagramPacket receivePacket;
		InetAddress dstAddress;
		ExecutorService executor;
		Future<RingPacket> future;
		RingClientSocketListener clientSocketListener=new RingClientSocketListener(null,null, clientListenSocket,verbose);
		InetAddress dstIp;
		
		try {

			
			
			while (true){
				/*wait for PING_INTERLEAVING_TIME_MS to send the next ping*/
				try {
					Thread.sleep(PING_INTERLEAVING_TIME_MS);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				while (!pingOn){
					pingLock.lock();
					try {
						pingActivate.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					pingLock.unlock();
				}
				
				/*get the ip of the next node to contact*/
				dstIp = chompeer.getChompeer_ip();
				
				/*filling the ping packet payload*/
				pp = new RingPacket(chompeer.getChompeer_id(), chompeer.getSuccessor(), RingPacket.SEND, pingCounter);
				pp.setIp(dstIp);
				Boolean hasToken=false;
				if (token!=null){
					hasToken=true;
//					System.out.println("[DEBUG RING CLIENT]sending ping with token:");
//					token.printRecords();
					pp.setToken(token);
				}
				/*creating the ping datagram payload*/
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(pp);
				
				data = baos.toByteArray();
				toSendData = RingCommon.dataFormatting(data);
				
				/*initializing the ping packet*/
				dstAddress=chompeer.getSuccessorAddress();
				sendPacket = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
				
				/*preparing the ping response packet*/
				RingCommon.clearBuffer(receiveData);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				
				/*initializing the future*/
				executor = Executors.newSingleThreadExecutor();
				RingPacket receivedPing=null;
				clientSocketListener.resetPingSocketListener(receivePacket, receivedPing);
				future = executor.submit(clientSocketListener);
				RingPacket pingReply;
				
				/*wait for the reply*/
				for (attempts=0; attempts<RETRY; ++attempts){
					
					
					try {
						/*sending the ping*/
//						println("sending ping to host "+dstAddress.getHostAddress()+" with size "+data.length+"~"+toSendData.length+"...");
						lockSendSocket();
//						++pingnumDebug;
						
						clientSendSocket.send(sendPacket);
						unlockSendSocket();
						
						if (hasToken)
							println("retry:"+attempts+",sent ping ("+pingCounter+") with token to host "+sendPacket.getAddress().toString().substring(1)+":"+sendPacket.getPort());
						else
							println("retry:"+attempts+",sent ping ("+pingCounter+") to host "+sendPacket.getAddress().toString().substring(1)+":"+sendPacket.getPort());
						
						if ((pingCounter<0)||(pingCounter>=(Long.MAX_VALUE-1)))
							pingCounter=0;
						/*wait until 5 seconds the ping reply*/
						pingReply=future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						/*got the reply*/
						break;
					} catch (ExecutionException e) {
						e.printStackTrace();
						break;
					} catch (TimeoutException e) {
						println("the host "+dstAddress.getHostAddress()+" seems down...");
						continue;
					}
					break;
				}
				/*if 4 attempts happened the node is assumed to be down */
				if (attempts>=RETRY)
					peerLost(chompeer.getSuccessor());
				resetToken();
				++pingCounter;

				
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
		
	}
	
	
	
	
	
	/**
	 * 
	 * The peerLost routine remove all information about a node from the whole network
	 * 
	 */
	public void peerLost(int peer_id) throws IOException{
		println("removing peer with id:"+peer_id);
		if (peer_id==821){
			System.out.println();
			System.out.println("[RING CLIENT DEB]peerlost removing node 821!!!");
			System.out.println();
		}
		
		if (peer_id==chompeer.getKeySpace())
			peer_id=chompeer.getFirstChompeerId();
		chompeer.removePeer(peer_id);
		PeerNode[] peerNodes = chompeer.getPeerNodes();
		DatagramPacket sendPacket;
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		byte [] data, toSendData;
		RingPacket pp = new RingPacket(chompeer.getChompeer_id(), 0, RingPacket.REMOVE, -1);
		pp.setTo_remove(peer_id);
		InetAddress dstAddress;
		
		for (int i=0; i<peerNodes.length;++i){
			int id=peerNodes[i].getNode_id();
			if (id==chompeer.getChompeer_id())
				continue;
			pp.setDst(id);
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(pp);
			data = baos.toByteArray();
			dstAddress=peerNodes[i].getIp().ipToInetAddress();
			toSendData = RingCommon.dataFormatting(data);
			sendPacket = new DatagramPacket(toSendData, toSendData.length, dstAddress, RingServer.LISTEN_PORT);
			lockSendSocket();
			clientSendSocket.send(sendPacket);
			unlockSendSocket();
		}
	}
	
	public void setToken(Token t){
		
		token=t;
	}
		
	private void resetToken(){
		token=null;
	}
	
	
	public void pingOff(){
		pingOn=false;
	}
	
	public void pingOn(){
		pingOn=true;
		chompeer.resetSuccessor();
		pingLock.lock();
		pingActivate.signal();
		pingLock.unlock();
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+RINGCLIENTTAG);
	}
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	
	
}
