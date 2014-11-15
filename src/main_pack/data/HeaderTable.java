package main_pack.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;




import main_pack.chomp_engine.SST.SSTable;
import main_pack.chomp_engine.cache.CacheManager;
import main_pack.chomp_engine.cache.CacheNode;
import main_pack.data.avl.string_key.AVLStringKeyNode;
import main_pack.data.avl.string_key.AVLStringKeyTree;
import main_pack.data.messaging.IndexValue;


/**
 * 
 * Classe di gestione dell'insieme di viaggiatori contenuti in Cache o su disco. Si compone di un AVL tree il cui singolo
 * nodo ({@link HeaderTableNode}) contiene le informazioni necessarie al reperimento del viaggiatore cercato, da cui e' possibile
 * estrarre le informazioni puntuali ad esso associate. 
 *
 */
public class HeaderTable {

	private AVLStringKeyTree avl;
	private SSTable sst;
	private CacheManager cm;
	
	public HeaderTable() {
		avl= new AVLStringKeyTree();
		sst = new SSTable("sst.dat",Traveler.RECORD_SIZE);
		cm = new CacheManager();
	}
	
	/**
	 * Restituisce il nodo dell&#39{@link HeaderTable} in cui sono contenute le informazioni associate al Codice Fiscale (CF) passato come parametro 
	 * @param CF
	 * @return {@link HeaderTableNode}
	 */
	public HeaderTableNode getHTNode(String CF)
	{
		return (HeaderTableNode) avl.search(CF);
	}
	
	/**
	 * Restituisce il {@link Traveler} identificato dal Codice Fiscale (CF) passato come parametro
	 * @param CF
	 * @return {@link Traveler}
	 * @throws Exception
	 */
	public Traveler search(String CF) throws Exception{
		HeaderTableNode htn = (HeaderTableNode) avl.search(CF);
		
		if (htn==null)
			return null;
		Traveler v = htn.getTravelerRaw();
		if (v==null){
			v = sst.readRecord(htn.getIndex());
			htn.setCN(cm.addNode(htn));
		}
		else {
			refreshNode(htn);
			
		}
		return v;
	}
	
	
	/**
	 * Restituisce il nodo dell&#39{@link HeaderTable} contenente informazioni associate al {@link Traveler} passato come parametro
	 * @param v
	 * @return {@link HeaderTableNode}
	 */
	public HeaderTableNode getHTNode(Traveler v)
	{
		return (HeaderTableNode)avl.search(v.getCF());
	}
	
	/**
	 * Aggiunge il {@link Traveler} passato in input all&#39{@link HeaderTable}(dato in memoria) ed all'SSTable(dato su disco)
	 * @param v
	 * @return {@link HeaderTableNode}
	 * @throws Exception
	 */
	public HeaderTableNode addTraveler(Traveler v) throws Exception
	{
		
		CacheNode cn;
		long vIndex = sst.writeRecord(v);
		
		sst.flush();
		
		HeaderTableNode ht_node = new HeaderTableNode(v.getCF(), vIndex,sst);
		ht_node.setV(v);
		avl.add_node(ht_node);
		cn=cm.addNode(ht_node);
		
		ht_node.setCN(cn);
		
		return ht_node;
	
	}
	
	/**
	 * Stampa tutti i {@link Traveler} che compongono l'AVL tree dell'{@link HeaderTable}
	 * @throws Exception
	 */
	public void printTravelers() throws Exception{
		for (int i=0; i<avl.size(); ++i){
			HeaderTableNode htn= (HeaderTableNode)avl.getNodeX(i);
			if (htn==null)
				continue;
			if(htn.getTraveler()==null) 
				System.out.print(i+" :CF:"+htn.getNode_id()+"V:null");
			else{
				System.out.print(i+" :CF:"+htn.getNode_id());
				htn.getTraveler().print();
			}
			System.out.println();
			
		}
	}
	
	/**
	 * Stampa l'AVL tree dell'{@link HeaderTable}
	 */
	public void printHeaderTable(){
		System.out.println("HT nodes - #"+avl.getNodesNum());
		avl.printTree();
		System.out.println("Cache - #"+cm.getNode_counter());
		cm.printCache();
	}
	
	public void printHeaderTable(int i){
		if ((i%2)==0){
			System.out.println("HT nodes - #"+avl.getNodesNum());
			avl.printTree();
		}
		else{
			System.out.println("Cache - #"+cm.getNode_counter());
			cm.printCache();
		}
	}
	
	/**
	 * Rimuove il nodo ({@link HeaderTableNode}) specificato come parametro (htn) in input dall'{@link HeaderTable}
	 * @param htn
	 * @return 1: node removed; 0: if no nodes with argument address is matched.
	 */
	public int removeHeaderTableNode(HeaderTableNode htn){
		return avl.remove_node(htn);
	}
	
	
	
	private void refreshNode(HeaderTableNode htn){
		CacheNode cn = htn.getCN();
		cm.refreshNode(cn);
	}
	
	
	
	
	public static void main(String [] args){
		String s,input;
		input=new String("/home/knizontes/stringAvl_input");
		int lastOffset=0;
		HeaderTable ht;
		try { 
			ht = new HeaderTable();
		
			s= new String("-1");
			try {
				BufferedReader in = new BufferedReader( new FileReader(input));
				while (true){
					if (( s=in.readLine())== null)
						break;
//					s = JOptionPane.showInputDialog("Inserisci un edge o \"end\" per terminare");
					if (s.equals("end")) break;
					System.out.println("Adding "+s+" peer...");
					ht.addTraveler(new Traveler(s, "nome", "cognome","simpatico","myhash"));
					++lastOffset;
				}
				
			}catch(Exception e){}

			ht.printHeaderTable();
			ht.search("DJOSÈAMDOVNFODKE").print();
			System.out.println();
			ht.printHeaderTable(1);
			ht.search("UAULALLAMUIDSAND").print();
			System.out.println();
			ht.printHeaderTable(1);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String key = "PRCMNL87E03L219P";
		return;
	}
	
	
}
