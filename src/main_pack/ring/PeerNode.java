package main_pack.ring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import main_pack.data.avl.AVLNode;
import main_pack.data.avl.AVLTree;
import main_pack.ring.data.Ip;

/**
 * 
 * Nodo caratteristico della Finger Table il quale incapsula le informazioni seguenti:
 * <p> ip del peer;
 * <p> id del nodo;
 * <p> stato di carico del nodo (charge_k).
 *
 */
public class PeerNode extends AVLNode{
	
	private Ip ip;
	private int area_id;
	private int charge_k=0;
	
	public PeerNode(int node_id, String ip){
		super(node_id);
		this.ip=new Ip(ip);	
	}
	
	public PeerNode(int node_id, String ip, int area_id){
		super(node_id);
		this.ip=new Ip(ip);
		this.area_id=area_id;
	}
	
	public void setNode(AVLNode new_node){
		ip=((PeerNode)new_node).getIp();
		super.setNode_id(new_node.getNode_id());
	}	

	public int getCharge_k() {
		return charge_k;
	}

	public void setCharge_k(int charge_k) {
		this.charge_k = charge_k;
	}

	public int getArea_id() {
		return area_id;
	}

	public void setArea_id(int area_id) {
		this.area_id = area_id;
	}

	public Ip getIp() {
		return ip;
	}

	
	public void setIp(Ip ip) {
		this.ip = ip;
	}

	public static void main(String [] args){
		String s,input;
		input=new String("/home/knizontes/avl_input");
		int lastIp=0;
		AVLTree avl= new AVLTree();
		ArrayList<PeerNode> nodes = new ArrayList<PeerNode>();
		s= new String("-1");
		try {
			BufferedReader in = new BufferedReader( new FileReader(input));
			while (true){
				if (( s=in.readLine())== null)
					break;
				nodes.add(new PeerNode(Integer.parseInt(s), "192.168.1."+(255-((lastIp*3)%255))));
				System.out.println("Adding "+s+" peer...");
				avl.add_node(nodes.get(lastIp));
				++lastIp;
			}
		}catch(Exception e){}

		avl.printTree();
		int key=25;
		AVLNode successor=avl.keySuccessor(key);
		if (successor==null)
			System.out.println("The successor of key "+key+" doesn't t exist...");
		else
			System.out.println("The successor node of key "+key+" is:"+successor.getNode_id());
		System.out.println("removing node:"+nodes.get(0).getNode_id()+"...");
		avl.remove_node(nodes.get(0));
		avl.printTree();
		return;
	}
	
}
