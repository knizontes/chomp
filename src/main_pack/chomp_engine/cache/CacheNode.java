package main_pack.chomp_engine.cache;

import main_pack.data.HeaderTable;
import main_pack.data.HeaderTableNode;

/**
 * Singolo elemento della cache. Incapsula le informazioni relative al nodo successore e predecessore oltreche'
 * all&#39indice dell&#39elemento in cache
 *
 */
public class CacheNode {

	private CacheNode prev;
	private CacheNode succ;
	private int cache_index;
	
	private HeaderTableNode htNode;
	
	public CacheNode(HeaderTableNode HTNode)
	{
		this.htNode = HTNode;
	}
	/**
	 * Costruttore della classe CacheNode
	 * @param HTNode
	 * @param succ successore dell'elemento che si crea
	 * @param cache_index indice dell'elemento
	 */
	public CacheNode(HeaderTableNode HTNode, CacheNode succ, int cache_index)
	{
		this.cache_index = cache_index;
		this.prev = null;
		this.succ = succ;
		this.htNode = HTNode;
	}
	
	/**
	 * 
	 * @return il nodo precedente al chiamante
	 */
	public CacheNode getPrev() {
		return prev;
	}


    /**
     * Setta il nodo predecessore
     * @param prev nuovo CacheNode successore
     */
	public void setPrev(CacheNode prev) {
		this.prev = prev;
	}
	
	/**
	 * 
	 * @return il nodo successore all'oggetto chiamante
	 */
	public CacheNode getSucc() {
		return succ;
	}

    /**
     * Setta il nodo successore
     * @param succ nuovo CacheNode successore
     */
	public void setSucc(CacheNode succ) {
		this.succ = succ;
	}

/**
 * Ritorna l&#39{@link HeaderTableNode} associato all&#39oggetto
 * @return
 */
	public HeaderTableNode getHtNode() {
		return htNode;
	}


	public int getCache_index() {
		return cache_index;
	}


	public void setCache_index(int cache_index) {
		this.cache_index = cache_index;
	}	
	
	public void printCacheNode(){
		if (prev==null){
			if(succ==null)
				System.out.print("HT node:"+htNode.getNode_id()+"\tprev:null"+"\tsucc:null");
			else
				System.out.print("HT node:"+htNode.getNode_id()+"\tprev:null"+"\tsucc:"+succ.getHtNode().getNode_id());
		}
		else if(succ==null)
			System.out.print("HT node:"+htNode.getNode_id()+"\tprev:"+prev.getHtNode().getNode_id()+"\tsucc:null");
		else
			System.out.print("HT node:"+htNode.getNode_id()+"\tprev:"+prev.getHtNode().getNode_id()+"\tsucc:"+succ.getHtNode().getNode_id());
	}
	
	
}
