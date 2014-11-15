package main_pack.ring;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;




import main_pack.chomp.ChompServer;
import main_pack.data.IndexTableManager;
import main_pack.metrics.MetricsManager;
import main_pack.request_server.RequestServer;
import main_pack.ring.client.RingClient;
import main_pack.ring.initializer.ConfigReader;
import main_pack.ring.recovery.RecoveryEngine;
import main_pack.ring.token.TokenGenerator;
import main_pack.ring.token.TokenManager;
import main_pack.utils.Timestamp;

/**
 * 
 * Generico Peer dell'anello. Implementa le funzioni per la strutturazione del Chomp
 * Ring, gestisce la rilevazione di nodi che hanno subito crash attraverso
 * un sitema di ping, offre ai livelli superiori i metodi che implementano i
 * protocolli per lo spostamento del nodo e per il piggybacking del token sul
 * ping;
 *
 */
public class Chompeer {

	private final static String CHOMPEERTAG="[CHOMPEER]";

	
	private FingerTable ft;
	private ConfigReader configReader;
	private int chompeer_id;
	private InetAddress chompeer_ip;
	private TokenManager tokenMan;
	private Thread tokenManTh;
	private MetricsManager metricsMan;
	private TokenGenerator tokenGenerator;
	private RecoveryEngine rec_eng;

	private int successor;
	private RingServer ringServer;
	private RingClient ringClient;
	private Thread pingServerTh;
	private Thread pingClientTh;
	private Thread tokenGeneratorTh;
	private ChompServer chompServer;
	
	private Boolean verbose=false;
	
	
	
	public Chompeer (ChompServer chompServer, Boolean verbose){
		ft=new FingerTable();
		configReader= new ConfigReader(ft,"peer.config.xml");
		this.chompServer=chompServer;
		successor=-1;
		chompeer_id=-1;
		chompeer_ip=null;
		this.verbose=verbose;
	}
	
	public int getFirstChompeerId(){
		return ft.keySuccessor(0).getNode_id();
	}
	
	public int removePeer(int peer_id){
		PeerNode pred;
		if (peer_id==ft.getKeyspace())
			peer_id=ft.getMinPeerId();
		

		println("got a remove request for node "+peer_id);
		if ((pred= ft.getPredecessor(chompeer_id)).getNode_id()==peer_id){
			chompServer.mergeLevel(IndexTableManager.RECOVERY1);

		}
		if(ft.getPredecessor(pred.getNode_id()).getNode_id()==peer_id){
			chompServer.mergeLevel(IndexTableManager.RECOVERY2);
		}
		
		int retval = ft.removePeer(peer_id);
		
		
		println("Old successor:"+successor);
		System.out.println("[CHOMPEER]Old successor:"+successor);
		resetSuccessor();
		println("New successor:"+successor);
		System.out.println("[CHOMPEER]New successor:"+successor);
		closeRing();
		
		return retval;
	}
	
	public int getPredecessorId(){
		return ft.getPredecessor(chompeer_id).getNode_id();
	}
	
	private int closeRing(){
		PeerNode firstNode = ft.keySuccessor(0);
		return ft.addPeer(ft.getKeyspace(),firstNode.getIp().ipToString());
	}
	
	public void setChompeer_id(int chompeer_id) {
		PeerNode successorPeer;
		this.chompeer_id = chompeer_id;
		successorPeer=ft.keySuccessor(chompeer_id+1);
		if (successorPeer!=null)
			successor=successorPeer.getNode_id();
		else 
			successor=ft.keySuccessor(0).getNode_id();
	}

	public void _setChompeer_ip(InetAddress chompeer_ip) {
		this.chompeer_ip = chompeer_ip;
	}
	
	public TokenManager getTokenManager(){
		return tokenMan;
	}
	
	public void setRecoveryEngine(RecoveryEngine rec_eng){
		this.rec_eng=rec_eng;
	}
	
	public void sendNewNodeJoinSignals(){
		rec_eng.sendNewNodeJoinSignals();
	}
	
	/**
	 * Read all the fingrtable records and return the id of the one which match the parameter with its ip field,
	 * return -1 otherwise.
	 * @param ip - the ip to be matched
	 * @return the id of the peer whose ip matches with the function parameter, -1 otherwise
	 */
	
	public int getIdFromIp(String ip){
		return ft.getIdFromIp( ip);
	}
	
	public void resetMigratingState(){
		tokenMan.resetPresentMigratingState();
	}

	public void run(){
		println("started");
		configReader.init();

		tokenMan= new TokenManager(this, true);
		tokenGenerator= new TokenGenerator(tokenMan);
		ringServer= new RingServer(this,tokenMan,verbose);
		ringClient = new RingClient(this,verbose);
		tokenMan.setTokenGenerator(tokenGenerator);
		tokenMan.setRingClient(ringClient);
		tokenGeneratorTh= new Thread(tokenGenerator,"Token Generator");
		tokenManTh=new Thread(tokenMan,"token manager thread");
		tokenManTh.start();
		tokenGeneratorTh.start();
		pingServerTh = new Thread(ringServer,"RingServer");
		pingClientTh = new Thread(ringClient,"RingClient");
		pingServerTh.start();
		pingClientTh.start();
	}
	
	public void tokenManagerSetup(RequestServer reqServer, MetricsManager metricsMan){
		tokenMan.setup(reqServer, metricsMan);
		this.metricsMan=metricsMan;
	}
	
	public void join(){
		ringClient.ringBoot();
	}
	
	public void printFingerTable(){
		ft.PrintTree();
	}
	
	public Boolean nodeExist(int peer_id){
		return ((ft.getPeerNode(peer_id))!=null);
	}
	
	public PeerNode getPeerNode(int peer_id){
		return ft.getPeerNode(peer_id);
	}
	
	public PeerNode getPeerNodeFromPosition(int index){
		return ft.getNodeFromPosition(index);
	}
	
	public int addPeerNode(int node_id, String node_ip){
		int retval = ft.addPeer(node_id, node_ip);
		resetSuccessor();
		return retval;
	}
	
	
	public InetAddress getSuccessorAddress() throws UnknownHostException{
		return ft.getPeerNode(ft.getSuccessor(chompeer_id+1)).getIp().ipToInetAddress();
	}
	
	public InetAddress getSuccessor2Address() throws UnknownHostException{
		return getSuccessor2().getIp().ipToInetAddress();
	}
	
	private PeerNode getSuccessor2(){
		if ((successor+1)>=ft.getKeyspace())
			return ft.getPeerNode(ft.getSuccessor((ft.getSuccessor(0)+1)));
		return ft.getPeerNode(ft.getSuccessor(successor+1));
	}
	
	private PeerNode getSuccessor3(){
		if ((getSuccessor2().getNode_id()+1)>=ft.getKeyspace())
			return ft.getPeerNode(ft.getSuccessor((ft.getSuccessor(0)+1)));
		return ft.getPeerNode(ft.getSuccessor(getSuccessor2().getNode_id()+1));
	}
		
	

	public InetAddress getSuccessor3Address() throws UnknownHostException{
		return getSuccessor3().getIp().ipToInetAddress();
	}
	


	public InetAddress getAuthoritativePeer(int key) throws UnknownHostException{
		return ft.keySuccessor(key).getIp().ipToInetAddress();
	}
	
	public int getSuccessor() {
		return successor;
	}
	
	public void removeChompeerFromRing() throws IOException{
		println(" leaving id "+chompeer_id);
		ringClient.peerLost(chompeer_id);
		metricsMan.resetTables();
	}

	public void resetSuccessor() {
		println("Old successor:"+successor);
		successor = ft.getSuccessor(chompeer_id+1);
		ringClient.resetSuccessor();
		println("New successor:"+successor);
	}
	
	public int getBusiestNodeId(){
		return ft.getBusiest().getNode_id();
	}
	
	public void setBusiestNodeId(int node_id){
		ft.setBusiest(node_id);
	}
	
	public InetAddress getBusiestNodeIp() throws UnknownHostException{
		return ft.getBusiest().getIp().ipToInetAddress();
	}

	public int getChompeer_id() {
		return chompeer_id;
	}

	public InetAddress getChompeer_ip() {
		return chompeer_ip;
	}
	
	public void setChompeer_ip(InetAddress chompeer_ip) {
		this.chompeer_ip = chompeer_ip;
	}

	public int getHashValueAverage(){
		if (isFirstNode())
			return chompServer.getHashValueForFirstNodeAverage(chompeer_id,ft.getKeyspace());
		return chompServer.getHashValueAverage();
	}

	public PeerNode [] getPeerNodes(){
		return ft.getPeerNodes();
	}
	
	public int getPeersNum(){
		return ft.getPeersNum();
	}
	
	public int getKeySpace(){
		return ft.getKeyspace();
	}
	
	public Boolean isFirstNode(){
		return new Boolean(ft.getSuccessor(0)==chompeer_id);
	}
	
	public int getNewId(int busiestNodeId){
		return ringClient.getNewId(busiestNodeId);
	}
	
	public void pingOff(){
		ringClient.pingOff();
	}
	
	public void pingOn(){
		ringClient.pingOn();
	}
	
	public void sendTables(){
		rec_eng.sendTables();
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	private String tag(){
		return (Timestamp.now()+"-"+CHOMPEERTAG);
	}
	
}
