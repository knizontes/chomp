package main_pack.ring;

import java.net.InetAddress;

import main_pack.data.avl.AVLTree;



/**
 * 
 * Finger Table Class
 * 
 * Utility class per il lookup delle risorse sull'anello realizzato mediante uso di un AVL tree confinato all'interno di un
 * keyspace.
 *
 */
public class FingerTable {
	
	private AVLTree avl;
	private int keyspace;
	private PeerNode busiest;
	
	public FingerTable(){
		avl=new AVLTree();
	}
	
	public void setKeyspace(int keyspace) {
		this.keyspace = keyspace;
	}

	public int addPeer(int node_id, String node_ip){
		PeerNode pn = new PeerNode(node_id,node_ip);
		if (busiest==null)
			busiest = pn;
		return avl.add_node(pn);
	}
	
	public int addPeer(int node_id, String node_ip, int area_id){
		PeerNode pn = new PeerNode(node_id,node_ip, area_id);
		if (busiest==null)
			busiest = pn;
		return avl.add_node(pn);
	}
	
	public int addPeer(PeerNode peerNode){
		if (busiest==null)
			busiest = peerNode;
		return avl.add_node(peerNode);
	}
	
	public int removePeer(PeerNode peerNode){
		int retval = avl.remove_node(peerNode);
		if (peerNode==busiest)
			updateBusiest();
		return retval;
	}
	
	private void updateBusiest(){
		PeerNode pn;
		busiest=null;
		for (int i=1; i<avl.size();++i){
			if ((pn=(PeerNode)avl.getNode(i))==null)
				continue;
			if ((busiest==null)||(pn.getCharge_k()>busiest.getCharge_k()))
				busiest=pn;
		}
	}
	
	public void setCharge(int node_id, int new_charge_k){
		PeerNode pn =(PeerNode) avl.search(node_id);
		if ((new_charge_k>busiest.getCharge_k())&& (pn!=busiest))
			busiest = pn;
		pn.setCharge_k(new_charge_k);
		
	}
	
	public PeerNode getPeerNode(int peerId){
		return ((PeerNode)avl.search(peerId));
	}
	
	public int getMinPeerId(){
		return avl.getMinNode().getNode_id();
	}
	
	
	public PeerNode getPredecessor(int nd_id){
		PeerNode retval= (PeerNode)avl.keyStrictPredecessor(nd_id);
		/*ring closure*/
		if (retval==null)
			retval=(PeerNode)avl.getMaxNodePredecessor();
		return retval;
	}
	
	public PeerNode keySuccessor(int key){
		return ((PeerNode)avl.keySuccessor(key));
	}
	
	public void PrintTree(){
		avl.printTree();
	}
	
	public int getKeyspace(){
		return keyspace;
	}
	
	public int removePeer(int peer_id){
		
		PeerNode peer = (PeerNode)avl.search(peer_id);
		PeerNode first_node = keySuccessor(0);
		PeerNode last_node = (PeerNode)avl.search(keyspace);
		if (peer==null)
			return 0;
		
		int retval;
		if ((peer.getNode_id()==first_node.getNode_id())||(peer.getNode_id()==keyspace)){

			retval=avl.remove_node(first_node);
			first_node = keySuccessor(0);
			last_node.setIp(first_node.getIp());
			
			return retval;
		}

		return avl.remove_node(peer);
	}

	public PeerNode[] getPeerNodes() {
		PeerNode[] peerNodes = new PeerNode[avl.size_effective()];
		PeerNode nd;
		int cur=0;
		for (int i=0; i<avl.size(); ++i){
			if ((nd=(PeerNode)avl.getNode(i))!=null){
				peerNodes[cur]=nd;
				++cur;
			}
		}
		return peerNodes;
	}
	
	public int getPeersNum(){
		return avl.size();
	}
	
	public PeerNode getNodeFromPosition(int index){
		return (PeerNode) avl.getNode(index);
	}
	
	public int getSuccessor(int node_id){
		return avl.keySuccessor(node_id).getNode_id();
	}
	
	public PeerNode getBusiest(){
		return busiest;
	}

	
	
	
	/**
	 * Read all the fingrtable records and return the id of the one which match the parameter with its ip field,
	 * return -1 otherwise.
	 * @param ip - the ip to be matched
	 * @return the id of the peer whose ip matches with the function parameter, -1 otherwise
	 */
	public int getIdFromIp(String ip) {
		PeerNode pn;
		for (int i=0; i< avl.size(); ++i){
			if ((pn = (PeerNode)avl.getNode(i))==null)
				continue;
			if (pn.getIp().ipToString().equals(ip))
				return pn.getNode_id();
		}
		return -1;
		
	}

	public void setBusiest(int node_id) {
		if (busiest.getNode_id()==node_id)
			return;
		PeerNode newBusiest= (PeerNode)avl.search(node_id);
		if (newBusiest!=null)
			busiest=newBusiest;
	}
	
	
}
