package main_pack.data;

import main_pack.data.avl.string_key.AVLStringKeyNode;

/**
 * 
 * Specializzazione di AVLStringKeyNode. Nodo AVL descrittivo della singola Index Table inglobata nell'Header Table (AVL)
 * di competenza. 
 *
 */
public class IndexTableNode extends AVLStringKeyNode{
	
//	private String cf;
	private HeaderTableNode ht_node;
	private IndexTableName it_name;
	private IndexTableNode prev = null;
	private IndexTableNode succ = null;
	
	
	public IndexTableNode(String CF, IndexTableName it_name){
		super(CF);
		this.it_name=it_name;
	}

	/**
	 * Restituisce il Codice Fiscale (CF) associato all'IndexTableNode
	 * @return String
	 */
	public String getCF() {
		return getNode_id();
	}

	/**
	 * Restituisce il nome della IndexTable di cui questo nodo (IndexTableNode) fa parte
	 * @return {@link IndexTableName}
	 */
	public IndexTableName getIndexTableName(){
		return it_name;
	}
	
	
	/**
	 * Restituice l'HeaderTableNode a cui e' associato questo IndexTableNode
	 * @return {@link HeaderTableNode}
	 */
	public HeaderTableNode getHt_node() {
		return ht_node;
	}

	/**
	 * Inizializza l'HeaderTableNode al valore passato come parametro
	 * @param ht_node
	 */
	public void setHt_node(HeaderTableNode ht_node) {
		this.ht_node = ht_node;
	}

	@Override
	public void setNode(AVLStringKeyNode nd) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Restituisce il nodo predecessore di questo {@link IndexTableNode} sull'AVL tree
	 * @return {@link IndexTableNode}
	 */
	public IndexTableNode getPrev() {
		return prev;
	}

	/**
	 * Inizializza il nodo predecessore di questo {@link IndexTableNode} sull'AVL tree
	 * @param prev
	 */
	public void setPrev(IndexTableNode prev) {
		this.prev = prev;
	}

	/**
	 * Restituisce il nodo predecessore di questo {@link IndexTableNode} sull'AVL tree
	 * @return {@link IndexTableNode}
	 */
	public IndexTableNode getSucc() {
		return succ;
	}

	/**
	 * Inizializza il nodo successore di questo {@link IndexTableNode} sull'AVL tree
	 * @param succ
	 */
	public void setSucc(IndexTableNode succ) {
		this.succ = succ;
	}
	

	

}
