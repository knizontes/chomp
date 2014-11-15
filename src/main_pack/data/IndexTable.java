package main_pack.data;

import java.io.IOException;
import java.io.Serializable;

import main_pack.data.avl.string_key.AVLStringKeyNode;
import main_pack.data.avl.string_key.AVLStringKeyTree;

/**
 * 
 * Specializzazione di AVLStringKeyNode. Nodo dell'AVL tree contenente l'associazione tra un Traveler (viaggiatore) 
 * identificato dal suo Codice Fiscale (CF) e l'Index Table in cui e' memorizzato.
 *
 */

public class IndexTable extends AVLStringKeyNode implements Serializable{

	
	private static final long serialVersionUID = 1L;
	private transient HeaderTable ht;
	private AVLStringKeyTree avl;
	private IndexTableName it_name;
	private int hash;
	private Boolean dirty=true;
	
	public IndexTable(IndexTableName it_name, HeaderTable ht, int hash)
	{
		super(it_name.toFormattedString());
		this.it_name=it_name;
		this.ht = ht;
		this.avl = new AVLStringKeyTree();
		this.hash = hash;
	}
	
	/**
	 * Specifica che il nodo e' stato "sporcato" (flag dirty attivato) 
	 */
	public void setDirty(){
		dirty=true;
	}
	
	/**
	 * Specifica che il nodo non e' stato "sporcato" (flag dirty disattivato)
	 */
	public void resetDirty(){
		dirty=false;
	}
	
	/**
	 * Verifica lo stato del nodo (dato "sporcato"?)
	 * @return Boolean
	 */
	public Boolean isDirty(){
		return dirty;
	}
	
	
	/**
	 * Restituisce l'{@link HeaderTable} associata all'{@link IndexTable}
	 * @return {@link HeaderTable}
	 */
	private HeaderTable getHt() {
		return ht;
	}

	/**
	 * Inizializza il valore dell'{@link HeaderTable} associata all'{@link IndexTable}
	 * @param ht
	 */
	private void setHt(HeaderTable ht) {
		this.ht = ht;
	}



	/**
	 * Restituisce l'AVL tree descrittivo dell'{@link IndexTable}
	 * @return {@link AVLStringKeyTree}
	 */
	private AVLStringKeyTree getAvl() {
		return avl;
	}



	/**
	 * Inizializza l'AVL tree associato all'{@link IndexTable} al valore passato come parametro
	 * @param avl
	 */
	private void setAvl(AVLStringKeyTree avl) {
		this.avl = avl;
	}



	
	public int getHash() {
		return hash;
	}




	private void setHash(int hash) {
		this.hash = hash;
	}




	/**
	 * Add a node in the avl tree
	 * @param nd the node to add
	 * @return 1 if the node is already present, 2 if the new node is the first node in the tree, 0 if the node is correctly 
	 * added in the avl tree, -1 if something wrong occurred
	 * @throws Exception 
	 */
	
	public int addNode(Traveler v) throws Exception
	{
		// la funzione non ritorna!!!
		HeaderTableNode ht_node = ht.getHTNode(v.getCF());
		IndexTableNode it_node = new IndexTableNode(v.getCF(),it_name);

		if(ht_node == null){
				ht_node = ht.addTraveler(v);
		}
		else 
			{
				ht_node.getIt_node().setPrev(it_node);
				it_node.setSucc(ht_node.getIt_node());
			}
		ht_node.setIt_node(it_node);
		int retval=avl.add_node(it_node);
		if (retval!=1)
			setDirty();
		return retval;
	}
	
	
	
	public int removeNode(IndexTableNode it_node)
	{
		IndexTableNode succNd,prevNd;
		succNd=it_node.getSucc();
		prevNd=it_node.getPrev();
		if (prevNd!=null)
			prevNd.setSucc(succNd);
		if (succNd!=null)
			succNd.setPrev(prevNd);
		int retval=avl.remove_node(it_node);
		if (retval!=0)
			setDirty();
		return retval;
	}
	
	public int removeNode(String CF)
	{
		
		IndexTableNode it_node,succNd,prevNd;
		it_node=search(CF);
		if (it_node==null)
			return -1;
		succNd=it_node.getSucc();
		prevNd=it_node.getPrev();
		if (prevNd!=null)
			prevNd.setSucc(succNd);
		if (succNd!=null)
			succNd.setPrev(prevNd);
		int retval=avl.remove_node(it_node);
		if (retval!=0)
			setDirty();
		return retval;
	}
	
	public IndexTableNode search(String CF)
	{
		return (IndexTableNode)avl.search(CF);
	}



	public IndexTableName getIt_name() {
		return it_name;
	}



	@Override
	public void setNode(AVLStringKeyNode nd) {
		setDirty();
		setNode_id(nd.getNode_id());
		setAvl(((IndexTable)nd).getAvl());
		setHash(((IndexTable)nd).getHash());
		setHt(((IndexTable)nd).getHt());
		
	}



	public int size() {
		// TODO Auto-generated method stub
		return avl.size();
	}
	
	public int sizeEffective(){
		return avl.size_effective();
	}



	public Traveler getViaggiatore(int index) throws Exception {
		// TODO Auto-generated method stub
		
		
		IndexTableNode ind = (IndexTableNode) avl.getNodeX(index);
		return ind.getHt_node().getTraveler();
		
	}



	public String getCF(int index)
	{
		IndexTableNode itn =(IndexTableNode) (avl.getNodeX(index));
		if(itn!=null)
			return itn.getCF();
		else 
			return null;
		
	}
	
	public IndexTableNode getNode(int index)
	{
		return (IndexTableNode) avl.getNodeX(index);
	}
	
	public void printTree(){
		avl.printTree();
	}

	public static void main(String[] args) {
		HeaderTable ht = new HeaderTable();
		IndexTableName itn;
		try {
			itn = new IndexTableName("1/1-2/11:11");
			IndexTable it = new IndexTable(itn, ht, 1);
			Traveler t;
			it.addNode(t=new Traveler("prcmnl87e03l219s", "Emanuele", "Paracone","simpatico","myhash"));
			it.getAvl().printTree();
			it.removeNode(t.getCF());
			it.getAvl().printTree();
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
