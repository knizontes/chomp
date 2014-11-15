package main_pack.data.avl.string_key;

import java.io.Serializable;

/**
 * 
 * Abstract class necessaria alla specializzazione dei nodi: HeaderTableNode, IndexTable, IndexTableNode
 *
 */

public abstract class AVLStringKeyNode implements Serializable{

	/*
	 * The identifier of the node
	 */
	private String node_id;
//	private HSNode nd;
	
	
	
	/* Here the data of the node in an AVL tree */
	private AVLStringKeyNode s_left_son;
	private AVLStringKeyNode s_right_son;
	private AVLStringKeyNode s_father;
	private int s_left_h;
	private int s_right_h;
	private int position;
	
	public AVLStringKeyNode(String node_id){
//		this.nd=nd;
		s_father=null;
		s_left_son=null;
		s_right_son=null;
		s_left_h=0;
		s_right_h=0;
		position=-1;
		this.node_id=node_id;
	}
	
	public void resetNode(){
		s_father=null;
		s_left_son=null;
		s_right_son=null;
		s_left_h=0;
		s_right_h=0;
		position=-1;
	}
	
	public AVLStringKeyNode getS_left_son() {
		return s_left_son;
	}

	public void setS_left_son(AVLStringKeyNode s_left_son) {
		this.s_left_son = s_left_son;
	}

	public AVLStringKeyNode getS_right_son() {
		return s_right_son;
	}

	public void setS_right_son(AVLStringKeyNode s_right_son) {
		this.s_right_son = s_right_son;
	}

	public AVLStringKeyNode getS_father() {
		return s_father;
	}

	public void setS_father(AVLStringKeyNode s_father) {
		if (s_father!=null)
			this.s_father = s_father;
		else this.s_father=null;
	}

	public int getS_left_h() {
		return s_left_h;
	}

	public void setS_left_h(int s_left_h) {
		this.s_left_h = s_left_h;
	}

	public int getS_right_h() {
		return s_right_h;
	}

	public void setS_right_h(int s_right_h) {
		this.s_right_h = s_right_h;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public abstract void setNode(AVLStringKeyNode nd);
	/*
	public HSNode getNode(){
		return nd;
	}*/
	
	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}

	public String getNode_id(){
		return node_id;
	}
	
	public void decreasePosition(int offset){
		position-=offset;
		if (position<0)
			System.out.println("[ERROR] Errore nel decrementare la posizione di un elemento dell'array...");
		return;
	}
	

	
	
	
	
}
