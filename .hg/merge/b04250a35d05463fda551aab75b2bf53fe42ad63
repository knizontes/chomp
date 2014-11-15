package main_pack.chomp_engine.cache;

import java.util.ArrayList;

import main_pack.data.HeaderTableNode;


public class CacheManager {

	private final static int SIZE = 10000000;
	private int node_counter = 0;
	
	private CacheNode[] nodeList;
	
	private CacheNode firstNode;
	private CacheNode lastNode;
	
	
	public CacheManager()
	{
		this.nodeList = new CacheNode[SIZE];
		this.firstNode = null;
		this.lastNode = null;
	}

	
	private void addNode(CacheNode cn)
	{
		
		if(node_counter >= SIZE)
		{
			lastNode.getHtNode().resetCN();
			lastNode.getHtNode().resetV();

			CacheNode tmpNode = lastNode;
			
			lastNode = lastNode.getPrev();
			
			nodeList[tmpNode.getCache_index()] = cn;
			
			cn.setCache_index(tmpNode.getCache_index());

			cn.setPrev(null);
			cn.setSucc(firstNode);

			firstNode.setPrev(cn);
			firstNode = cn;
			 
			return;
		}
		
		nodeList[node_counter] = cn;
//		System.out.println("node counter:"+node_counter);
		if (node_counter!=0){
			cn.setPrev(null);
			cn.setSucc(firstNode);
			firstNode.setPrev(cn);
		}
		else 
			lastNode=nodeList[node_counter];
		
		firstNode = nodeList[node_counter];
		node_counter++;
		
	}
	
	public CacheNode addNode(HeaderTableNode ht_node)
	{
		CacheNode retVal;
		addNode(retVal = new CacheNode(ht_node));
		ht_node.setCN(retVal);
		return retVal;
	}
	
	
	

	public void refreshNode(CacheNode cn)
	{
		
		
		if(cn.equals(firstNode))
			return;
		
		if(cn.equals(lastNode))
			{
				cn.getPrev().setSucc(null);
				lastNode = cn.getPrev();
				
			}
		else
			{
			
			cn.getPrev().setSucc(cn.getSucc());
			
			cn.getSucc().setPrev(cn.getPrev());
			
			}
		
		cn.setPrev(null);
		cn.setSucc(firstNode);
		firstNode.setPrev(cn);
		firstNode = cn;
		
	}	
	
	

//	public void removeNode(CacheNode cn)
//	{
//		// TO DO
//		
//		//Cancellare i riferimenti del nodo e del puntatore all'oggetto presso la HeaderTable
//		//Meccanismo di invalidazione dei nodi .... tutti a null tranne il predecessore (lastNode)
//		//Il CacheNode viene retrocesso a lastNode
//		if (cn.equals(firstNode)){
//			firstNode=cn.getSucc();
//			cn.getSucc().setPrev(null);
//		}
//		else if (cn.equals(lastNode)){
//			lastNode=cn.getPrev();
//			cn.getPrev().setSucc(null);
//		}
//		else{
//			cn.getPrev().setSucc(cn.getSucc());
//			cn.getSucc().setPrev(cn.getPrev());
//		}
//		nodeList[cn.getCache_index()]=null;
//		return;
//	}
	
	public int getNode_counter() {
		return node_counter;
	}


	public void printCache(){
		
		CacheNode cn = firstNode;
//		firstNode.printCacheNode();
		int count=0;
		do{
			System.out.print(count+":");
			cn.printCacheNode();
			System.out.println();
			cn=cn.getSucc();
			++count;
		}while (cn!=lastNode);
		System.out.println("count:"+count+ "\tnode_counter"+node_counter);
	}
	
}