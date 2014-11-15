package main_pack.chomp_engine;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.omg.CORBA.CharHolder;





import main_pack.data.HeaderTable;
import main_pack.data.HeaderTableNode;
import main_pack.data.IndexTable;
import main_pack.data.IndexTableManager;
import main_pack.data.IndexTableName;
import main_pack.data.IndexTableNode;
import main_pack.data.Traveler;
import main_pack.data.avl.SizeIndexTableNode;
import main_pack.data.avl.SizeIndexTableTree;
import main_pack.data.messaging.IndexValue;
import main_pack.ring.Chompeer;
import main_pack.ring.recovery.RecoveryEngine;
import main_pack.utils.Timestamp;

/**
 * Classe di gestione dei dati sul peer. Implementa le operazioni per il mantenimento e l'interrogazione del db 
 * column-oriented e per la gestione dei 2 livelli di replicazione.
 * Dichiara ed istanzia le classi {@link HeaderTable} ed {@link IndexTableManager} responsabili rispettivamente del 
 * reperimento del viaggiatore richiesto e dell'individuazione delle tratte in cui e' coinvolto un determinato viaggiatore.
 * 
 */
public class ChompEngine {

	private final static String CHOMP_ENGINE_TAG="[CHOMP ENGINE]";

	
	private HeaderTable ht = new HeaderTable();
	private IndexTableManager itm= new IndexTableManager(ht);
	private int updates =0;
	private RecoveryEngine rec_eng;
	private Boolean verbose;
	
	public ChompEngine (Chompeer chompeer, Boolean verbose){
//		this.rec_eng=new RecoveryEngine(this, chompeer, verbose);
		this.verbose = verbose;
//		this.rec_eng=rec_eng;
	}
	
	/**
	 * Inizializza l'oggetto {@link RecoveryEngine} al valore passato come parametro in input
	 * @param rec_eng
	 */
	public void setRecoveryEngine(RecoveryEngine rec_eng){
		this.rec_eng=rec_eng;
//		this.rec_eng.run();
	}
	
	/**
	 * Stampa tutti gli {@link IndexTableName} relativi alle {@link IndexTable} per cui il peer e' autoritativo 
	 */
	public void printTables(){
		itm.printTables();
	}
	
	
	/**
	 * Add a value in the correct index table
	 * @param t the traveler to add
	 * @param it_name the IndexTableName which identifies the IndexTable
	 * @return 1 if the node is already present, 2 if the new node is the first node in the tree, 0 if the {@link IndexTable} is correctly 
	 * added in the avl tree of {@link IndexTableManager}, 3 if t is correctly inserted in the {@link IndexTable}, <0 if something wrong occurred
	 * @throws Exception 
	 */
	
	public int addIndexTableValue(Traveler t, IndexTableName it_name, int hash) throws Exception{
		++updates;
		IndexTable it = itm.search(it_name);
		int retval= 3;
		if (it==null){
			retval=itm.add(it_name, hash);
			it = itm.search(it_name);
		}
		try {
			it.addNode(t);
			
		} catch (IOException e) {
			e.printStackTrace();
			retval=-2;
		}
		return retval;
	}
	/**
	 * 
	 * @param t
	 * @param it_name
	 * @param itHash
	 * @param recoveryLevel
	 * @return
	 */
	public int addIndexTableValue(Traveler t, String it_name,int itHash, byte recoveryLevel){
		IndexTable it = itm.search(it_name,recoveryLevel);
		int retval= 3;
		println("add index table value "+t.getCF());
		println(it_name+" to recovery level:"+recoveryLevel);
		try {
			if (it==null){
				retval=itm.add(new IndexTableName(it_name),itHash,recoveryLevel);
//				itm.printTables(recoveryLevel);
				println("it name:"+it_name);
				it = itm.search(it_name,recoveryLevel);
			}
			if (it==null)
				println("BBBUUUUUGGGGGG");
			it.addNode(t);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			retval=-2;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}
	
	/**
	 * Rimuove un {@link Traveler} da tutte le {@link IndexTable} in cui e' incluso un record che 
	 * vi fa riferimento (funzione remove all del database)
	 * @param CF
	 */
	public void removeTravelerFromAllTables(String CF){
		++updates;
		try {
			HeaderTableNode htn = ht.getHTNode(CF);
			if (htn==null)
				return;
			IndexTableNode itn=htn.getIt_node();
			IndexTableNode itnTmp;
			while (itn!=null){
				itnTmp=itn.getSucc();
				itm.removeIndexTableNode(itn.getIndexTableName(), CF);
				itn=itnTmp;
			}
			ht.removeHeaderTableNode(htn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Rimuove un traveler dalla {@link IndexTable} identificata dall'{@link IndexTableName}
	 * passato per parametro (funzione remove del database)
	 * @param CF
	 * @param itn
	 */
	public void removeTravelerFromIndexTable(String CF, IndexTableName [] itn){
		++updates;
		for (int i=0; i<itn.length;++i)
			itm.removeIndexTableNode(itn[i], CF);
	}
	
	
	/**
	 * Confronta i record delle tabelle identificate dagli {@link IndexTableName} inclusi 
	 * nell'array passato per parametro per ritornare l'array dei {@link Traveler} presenti 
	 * in ognuna di esse (funzione get del database)
	 * @param it_name_array
	 * @param CF_filter
	 * @return ArrayList<Traveler>
	 */
	public ArrayList<Traveler> finalQuery(IndexTableName [] it_name_array, String CF_filter){
		ArrayList<Traveler>retval= new ArrayList<Traveler>();
		IndexValue iv = query( it_name_array,  CF_filter);
		for (int i=0; i<iv.size();++i){
			try {
				retval.add(ht.search(iv.getCf(i)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval;
	}
	
	public IndexValue query (IndexTableName [] it_name_array, String CF_filter){
		ArrayList<IndexTable> its = new ArrayList<IndexTable>();
		IndexTable [] it_array; //= new IndexTable [it_name_array.length];
		IndexTable it;
		for (int i=0; i<it_name_array.length; ++i){
			if ((it=itm.search(it_name_array[i]))!=null)
				its.add( it );
		}
		it_array= new IndexTable[its.size()];
		for (int i=0; i<its.size();++i)
			it_array[i]=its.get(i);
		return query(it_array, CF_filter);
	}
	

	
	public IndexValue query (IndexTable [] it_array, String CF_filter){
		SizeIndexTableTree s_it_tree = new SizeIndexTableTree();
		for (int i=0; i<it_array.length;++i)
			s_it_tree.add_node(it_array[i], i);
		return execQuery(s_it_tree, it_array, CF_filter);
	}
	
	private IndexValue execQuery(SizeIndexTableTree it_tree,IndexTable [] it_array, String CF_filter){
		IndexValue retval=null;
		int successorId=0;
		
		/*take index table group from lowest size to maximum one*/
		for (int i=0; i<it_tree.size();++i){
			/*take the strictly greater size group of index table*/
			SizeIndexTableNode it = (SizeIndexTableNode)it_tree.keySuccessor(successorId);
			ArrayList<Integer> list = it.getArrayIndexList();
			
			/*use an index table from the group of the same size*/
			for (int j=0; j< list.size();++j){
				if (retval==null){
					retval=getListFromIndexTable(it_array[list.get(j)], CF_filter);
					continue;
				}
				/*search each value in the old list in the current index table, then return a list of matches*/
				retval = matchListInIndexTable(retval, it_array[list.get(j)]);
				if (retval.size()==0)
					return retval;
			}
		}
		return retval;
	}
	
	public int getUpdates(){
		return updates;
	}
	
	public void resetUpdates(){
		updates=0;
	}
	
	public IndexTableManager getIndexTableManager(){
		return itm;
	}
	
	private IndexValue getListFromIndexTable(IndexTable it, String CF_filter){
		IndexTableNode tmp=null;
		IndexValue retval = new IndexValue("");
		for (int i=0; i<it.size();++i){
			tmp=it.getNode(i);
			if ((tmp!=null) && (!(tmp.getCF().equals(CF_filter))))
				retval.addCF(tmp.getCF());
		}
		return retval;
	}
	
	private IndexValue matchListInIndexTable(IndexValue list, IndexTable it){
		IndexTableNode tmp = null;
		IndexValue retval = new IndexValue("");
		for (int i=0; i<list.size();++i){
			tmp=it.search(list.getCf(i));
			if (tmp==null)
				continue;
			retval.addCF(tmp.getCF());
		}
		return retval;
	}
	

	public String [] missingValuesInHeaderTableFromIncomingIndexTables(IndexTable [] it_array, byte recoveryLevel) throws Exception{
		ArrayList<String>  missingValues = new ArrayList<String>();
		String [] retval;
		IndexTable it;
		Traveler t;
		IndexTableNode it_node;
		int addVal;
		for(int i=0; i<it_array.length;++i){
			for (int j=0; j<it_array[i].size();++j){
				if ((it_node=it_array[i].getNode(j))!=null){
					t=ht.search(it_node.getCF());
					if (t==null){
						println("can't find cf:"+it_node.getCF());
						Boolean cf_found=false;
						for (int l=0; l<missingValues.size();++l)
							if (it_node.getCF().equals(missingValues.get(l))){
								println("cf "+it_node.getCF()+" already in "+missingValues.get(l));
								cf_found=true;
								break;
							}
						if (cf_found)
							continue;
						println("adding "+it_node.getCF());
						missingValues.add(it_node.getCF());	
					}
					
					else
						println("cf:"+t.getCF()+" found");
					
				}
					
			}
		}
		retval= new String[missingValues.size()];
		for (int i=0; i<missingValues.size();++i)
			retval[i]=missingValues.get(i);
		
		
		return retval;
		
	}
	
	public Traveler [] makeTravelerArray(String [] cf_array) throws Exception{
		Traveler [] retval= new Traveler[cf_array.length];
		int size=0;
		for (int i=0; i<retval.length;++i){
			retval[i]=ht.search(cf_array[i]);
			if (retval[i]==null)
				println("[DEBUG]can't find a cf which should be here");
		}

		return retval;
	}
	
	
	/**
	 * 
	 * Implementa la funzione put del database, consente l'aggiornamento del 
	 * campo specificato per il dato traveler
	 * 
	 */
	public void updateTraveler(Traveler t){
		try {
			Traveler originalT;
			HeaderTableNode htn = ht.getHTNode(t.getCF());
			if (htn==null)
				return;
			
			originalT=htn.getTravelerRecordFromSST();
			if (!(originalT.getInfo().equals(t.getInfo())))
				htn.updateTravelerField(t.getInfo(), Traveler.INFO_SIZE, Traveler.INFO_OFFSET);
			if (!(originalT.getPwdHash().equals(t.getPwdHash())))
				htn.updateTravelerField(t.getPwdHash(), Traveler.PWD_HASH_SIZE, Traveler.PWD_HASH_OFFSET);	

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Ritorna la media tra i valori delle chiavi hash delle tabelle gestite, viene 
	 * invocata quando un nodo richiede un Chompeer id che gli consenta di 
	 * diventare autoritativo per circa meta' delle risorse per le quali il nodo 
	 * di riferimento e' competente
	 * @return int 
	 */
	public int getHashValueAverage (){
		return itm.getHashValueAverage();
	}
	
	public int getHashValueForFirstNodeAverage (int chompeer_id, int keySpace) {
		return itm.getHashValueForFirstNodeAverage( chompeer_id, keySpace);
	}
	
	
	
	public HeaderTable getHT()
	{
		return ht;
	}
	
	/**
	 * Meccanismo di fusione tra livelli. Comportamento differente a seconda del flag (recoveryLevel) in input
	 */
	public void mergeLevel(byte recoveryLevel){
		itm.mergeLevel(recoveryLevel);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	private String tag(){
		return (Timestamp.now()+"-"+CHOMP_ENGINE_TAG);
	}

	public void removeIndexTableArray(IndexTable[] indexTableArray) {
		itm.removeIndexTableArray(indexTableArray);		
	}
	
	
	public void loadSignal1Routine(int untilIndex, InetAddress newNodeIp){
		itm.loadSignal1Routine();
		rec_eng.pushBackTablesToNewNode(untilIndex, newNodeIp);
	}
	
	public void loadSignal2Routine(){
		itm.loadSignal2Routine();
	}
	
	public void loadSignal3Routine(){
		itm.loadSignal3Routine();
	}

	public void moveIndexTableArrayToRecovery1(IndexTable[] indexTableArray) {
		itm.moveIndexTableArrayToRecovery1(indexTableArray);
	}
	
}
