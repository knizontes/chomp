package main_pack.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



import main_pack.data.avl.string_key.AVLStringKeyTree;
import main_pack.data.messaging.IndexValue;
import main_pack.data.messaging.ListIndexValue;
import main_pack.metrics.MetricsManager;
import main_pack.utils.Timestamp;

public class IndexTableManager {

//	imposta le pagine come dirty
	
	private static final String INDEXTABLEMANAGERTAG= "[INDEX TABLE MANAGER]";
	
	public final static byte AUTHORITATIVE=0;
	public final static byte RECOVERY1=1;
	public final static byte RECOVERY2=2;
	private AVLStringKeyTree avl = new AVLStringKeyTree();
	private AVLStringKeyTree avlRecovery1 = new AVLStringKeyTree();
	private AVLStringKeyTree avlRecovery2 = new AVLStringKeyTree();
	private HeaderTable ht;
	private ArrayList<IndexTable> toRemoveIndexTables= new ArrayList<IndexTable>();
	private Boolean verbose=true;
	private short printNums=0;
	private Lock avlLock = new ReentrantLock();
	
	public void printTables(){
		IndexTable it;
		println("printing index table tree (size:"+avl.size_effective()+")");
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			printlnNoTag((i+1)+"."+it.getIt_name().toString());
		}
		println();
	}
	
	
	
	/**
	 * The function returns the hash average of the authoritative level,
	 * if no tables are in the authoritative level returns -1
	 * @return the hash average of the authoritative level, -1 if no tables are in the authoritative level */
	
	
	private void _printHashFuncCompare(int chompeer_id, int keySpace){
		IndexTable it;
		ArrayList<Integer> hashList=new ArrayList<Integer>();
		int hash;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			if((hash=it.getHash())<=chompeer_id)
				hashList.add(new Integer(hash+keySpace));
			else
				hashList.add(new Integer(hash));
		}
		printlnNoTag("=========================\n[INDEX TABLE MANAGER DEBUG]study on the hash choice for new id\n=========================\n");
		printlnNoTag("printing hashes:");
		for(int i=0; i<hashList.size();++i)
			printlnNoTag(i+". "+hashList.get(i).intValue());
		Collections.sort(hashList);
		printlnNoTag("printing hashes after sorting:");
		for(int i=0; i<hashList.size();++i)
			printlnNoTag(i+". "+hashList.get(i).intValue());
		printlnNoTag("\n the choice with getHashMiddleValue() is :"+(hashList.get(hashList.size()/2).intValue()%keySpace));
		printlnNoTag("\n the choice with getHashValueForFirstNodeAverage() is :"+getHashValueForFirstNodeAverage2(chompeer_id,keySpace));
		
	}
	
	
	public int getHashMiddleValue(){
		IndexTable it;
		ArrayList<Integer> hashList=new ArrayList<Integer>();
		int index;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			hashList.add(new Integer(it.getHash()));
		}
		Collections.sort(hashList);
		return hashList.get(hashList.size()/2).intValue();
	}
	
	public int getHashMiddleValueForFirstNode(int chompeer_id, int keySpace){
		IndexTable it;
		ArrayList<Integer> hashList=new ArrayList<Integer>();
		int hash;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			if((hash=it.getHash())<=chompeer_id)
				hashList.add(new Integer(hash+keySpace));
			else
				hashList.add(new Integer(hash));
		}
		Collections.sort(hashList);
		return (hashList.get(hashList.size()/2).intValue()%keySpace);
	}
	
	
	public int getHashValueAverage(){
		int sum=0;
		int itsNum=0;
		IndexTable it;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			sum+=it.getHash();
			++itsNum;
		}
		if (itsNum==0)
			return -1;
		return sum/itsNum;
	}
	
	public int getHashValueForFirstNodeAverage(int chompeer_id, int keySpace) {
//		printHashFuncCompare(chompeer_id, keySpace);
		int sum=0;
		int itsNum=0;
		IndexTable it;
		int retval;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			sum+=it.getHash();
			if(it.getHash()<=chompeer_id)
				sum+=keySpace;
			++itsNum;
		}
		if (itsNum==0)
			return -1;
		
		return ((sum/itsNum)%keySpace);
	}
	
	public int getHashValueForFirstNodeAverage2(int chompeer_id, int keySpace) {
		int sum=0;
		int itsNum=0;
		IndexTable it;
		int retval;
		for (int i=0; i<avl.size();++i){
			if ((it=(IndexTable)avl.getNodeX(i))==null)
				continue;
			sum+=it.getHash();
			if(it.getHash()<=chompeer_id)
				sum+=keySpace;
			++itsNum;
		}
		if (itsNum==0)
			return -1;
		
		return ((sum/itsNum)%keySpace);
	}
	
	public void printTables(byte recoveryLevel){
		IndexTable it;
		if (recoveryLevel==AUTHORITATIVE){
			println("[INDEX TABLE MANAGER]printing authoritative level tree (size:"+avl.size_effective()+")");
			for (int i=0; i<avl.size();++i){
				if ((it=(IndexTable)avl.getNodeX(i))==null)
					continue;
				printlnNoTag((i+1)+". "+it.getNode_id());
			}
			println();
		}
		if (recoveryLevel==RECOVERY1){
			println("[INDEX TABLE MANAGER]printing recovery level 1 tree (size:"+avlRecovery1.size_effective()+")");
			for (int i=0; i<avlRecovery1.size();++i){
				if ((it=(IndexTable)avlRecovery1.getNodeX(i))==null)
					continue;
				printlnNoTag((i+1)+". "+it.getNode_id());
			}
			println();
		}
		if (recoveryLevel==RECOVERY2){
//			System.out.println("[INDEX TABLE MANAGER]printing recovery level 2 tree (size:"+avlRecovery2.size_effective()+")");
			for (int i=0; i<avlRecovery2.size();++i){
				if ((it=(IndexTable)avlRecovery2.getNodeX(i))==null)
					continue;
				printlnNoTag((i+1)+". "+it.getNode_id());
			}
			println();
		}
		return;
		
	}
	
	public int getTablesNum(){
		return avl.size_effective();
	}
	
	public int getTablesNum(byte recoveryLevel){
		if (recoveryLevel==RECOVERY1)
			return avlRecovery1.size_effective();
		else
			return avlRecovery2.size_effective();
	}
	
	public void printTablesNum(){
		println("Index tables num:"+getTablesNum());
		IndexTable it;
		for (int i=0; i<avl.size();++i)
			if ((it=(IndexTable)avl.getNodeX(i))!=null)
				println("hash key"+i+ " :"+it.getHash());
	}

	public void printTablesNum(byte recoveryLevel){
		println("Recovery level "+recoveryLevel+" tables num:"+getTablesNum(recoveryLevel));
		AVLStringKeyTree tmpAvl;
		IndexTable it;
		if (recoveryLevel==RECOVERY1)
			tmpAvl=avlRecovery1;
		else if (recoveryLevel==RECOVERY2)
			tmpAvl=avlRecovery2;
		else
			tmpAvl=avl;
		for (int i=0; i<tmpAvl.size();++i)
			if ((it=(IndexTable)tmpAvl.getNodeX(i))!=null)
				println("hash key"+i+ " :"+it.getHash());
	}
	
	public void _printAllTablesNum(){
		if (((printNums) %=10)==0){
			printlnNoTag("*****************************");
			printTablesNum();
			printlnNoTag("-----------------------------");
			printTablesNum(RECOVERY1);
			printTablesNum(RECOVERY2);
			printlnNoTag("*****************************");
		}
		++printNums;
	}
	
	public ArrayList<IndexTable> getToRemoveIndexTables(){
		return toRemoveIndexTables;
	}
	
	public void resetToRemoveIndexTables(){
		toRemoveIndexTables= new ArrayList<IndexTable>();
	}
	public void setHtX(HeaderTable ht) {
		this.ht = ht;
	}
	
	public AVLStringKeyTree getAvlX() {
		return avl;
	}

	public HeaderTable getHtX() {
		return ht;
	}

	
	public IndexTableManager(HeaderTable ht)
	{
         this.ht = ht;		
	}
	
	/**
	 * * add an indexTable to the {@link IndexTable} tree
	 * @param n the name which identifies the {@link IndexTable}
	 * @return the result of adding an {@link IndexTable}
	 */
	
	public int add(IndexTableName n,int hash)
	{
		avlLock.lock();
//		if (n.toFormattedString().equals("6/1-6/11:11"))
//			avl.printTree();
		int retval= avl.add_node(new IndexTable(n, ht, hash));
		avlLock.unlock();

		return retval;
	}
	
	/**
	 * add an indexTable to the correct recovery tree
	 * @param n the name which identifies the {@link IndexTable}
	 * @param recovery the recovery {@link IndexTable} tree
	 * @return the result of adding an {@link IndexTable}, <0 if something wrong occurred
	 */
	public int add(IndexTable it, byte recovery)
	{
		IndexTable itNew;
		Traveler t;
		avlLock.lock();

		int retval=-2;
//		it.resetNode();
//		System.out.println("[ITM DEBUG] ADD it:"+it.getNode_id()+" rec level:"+ recovery);
		if (recovery==AUTHORITATIVE){
			retval = avl.add_node(new IndexTable(it.getIt_name(), ht, it.getHash()));
			itNew=(IndexTable)avl.search(it.getNode_id());
			try {
				for (int i=0; i<it.size();++i){
//					System.out.println("[ITM DEB]add i:"+i);
					if (it.getCF(i)!=null){
//						System.out.println("[ITM DEB]ck i:"+i);
						t=ht.search(it.getCF(i));
						if (t==null)
							println("unknown traveler with CF:"+it.getCF(i));
						itNew.addNode(t);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (retval==0)
				avl.search(it.getNode_id()).setNode(it);
			
		}
		else if (recovery==RECOVERY1){
//			if (avlRecovery1==null)
//				System.out.println("[DEBUG ITM] rec 1 null!!!");
//			if (it==null)
//				System.out.println("[DEBUG ITM] it null!!!");
			retval = avlRecovery1.add_node(new IndexTable(it.getIt_name(), ht, it.getHash()));
			itNew=(IndexTable)avlRecovery1.search(it.getNode_id());
			try {
				for (int i=0; i<it.size();++i){
//					System.out.println("[ITM DEB]add i:"+i);
					if (it.getCF(i)!=null){
//						System.out.println("[ITM DEB]ck i:"+i);
						t=ht.search(it.getCF(i));
						if (t==null)
							println("unknown traveler with CF:"+it.getCF(i));
						itNew.addNode(t);
					}
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
			if (retval==0){
				avlRecovery1.search(it.getNode_id()).setNode(it);
			}
		}
		else if (recovery==RECOVERY2){
			retval = avlRecovery2.add_node(new IndexTable(it.getIt_name(), ht, it.getHash()));
			itNew=(IndexTable)avlRecovery2.search(it.getNode_id());
			try {
				for (int i=0; i<it.size();++i){
//					System.out.println("[ITM DEB]add i:"+i);
					if (it.getCF(i)!=null){
//						System.out.println("[ITM DEB]ck i:"+i);
						t=ht.search(it.getCF(i));
						if (t==null)
							println("unknown traveler with CF:"+it.getCF(i));
						itNew.addNode(t);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (retval==0)
				avlRecovery2.search(it.getNode_id()).setNode(it);
			
		}
		avlLock.unlock();

		return retval;
	}
	
	public int add(IndexTableName it,int hash, byte recoveryLevel){
		avlLock.lock();

		int retval=-2;
		if (recoveryLevel==AUTHORITATIVE)
			retval = avl.add_node(new IndexTable(it, ht, hash));
		
		if (recoveryLevel==RECOVERY1)
			retval = avlRecovery1.add_node(new IndexTable(it, ht, hash));
			
		if (recoveryLevel==RECOVERY2)
			retval = this.avlRecovery2.add_node(new IndexTable(it, ht, hash));
//			if (retval==0)
//				this.avlRecovery2.search(it.getNode_id()).setNode(it);
			
		avlLock.unlock();

		return retval;
	}
	
	public IndexValue [] makeIndexValue(byte recoveryLevel){
		IndexTable temp;
		IndexValue [] retval = null;
		if (recoveryLevel==RECOVERY1){
			retval = new IndexValue [avlRecovery1.size_effective()];
			for (int i=0; i<avlRecovery1.size();++i){
				temp = (IndexTable)avlRecovery1.getNodeX(i);
				if (temp==null)
					continue;
				retval[i]= new IndexValue(temp.getNode_id());
				for (int j=0; j<temp.size();++j){
					String cf = temp.getCF(j);
					if (cf==null)
						continue;
					retval[i].addCF(cf);
				}
			}
		}
		else if (recoveryLevel==RECOVERY2){
			retval = new IndexValue [avlRecovery2.size_effective()];
			for (int i=0; i<avlRecovery2.size();++i){
				temp = (IndexTable)avlRecovery2.getNodeX(i);
				if (temp==null)
					continue;
				retval[i]= new IndexValue(temp.getNode_id());
				for (int j=0; j<temp.size();++j){
					String cf = temp.getCF(j);
					if (cf==null)
						continue;
					retval[i].addCF(cf);
				}
			}
		}
		return retval;
	}
	
	public AVLStringKeyTree getAVL()
	{
		return this.avl;
	}
	
	public IndexTable search(String s)
	{
		return (IndexTable) avl.search(s);
	}
	
	public IndexTable search(IndexTableName itn)
	{
		return (IndexTable) avl.search(itn.toFormattedString());
	}
	
	public IndexTable search(IndexTableName itn, byte recoveryLevel)
	{
		if (recoveryLevel==RECOVERY1)
			return (IndexTable) avlRecovery1.search(itn.toFormattedString());
		else if (recoveryLevel==RECOVERY1)
			return (IndexTable) avlRecovery2.search(itn.toFormattedString());
		return null;
	}
	
	public IndexTable search(String s, byte recoveryLevel){
		if (recoveryLevel==AUTHORITATIVE)
			return (IndexTable) avl.search(s);
		if (recoveryLevel==RECOVERY1)
			return (IndexTable) avlRecovery1.search(s);
		if (recoveryLevel==RECOVERY2)
			return (IndexTable) avlRecovery2.search(s);
		
		println("[ERROR] recovery level"+recoveryLevel+" not valid");
		return null;
	}
	
	public IndexTable getIndexTable(int index)
	{
		return (IndexTable) avl.getNodeX(index);
	}
	
	public IndexTable [] getIndexTablesArray(){
		IndexTable [] retval = new IndexTable[avl.size_effective()];
		IndexTable it;
		int count=0;
		for (int i=0; i<avl.size();++i){
			it = (IndexTable)avl.getNodeX(i);
			if (it!=null){
				retval[count]= it;
				++count;
			}
		}
		return retval;
	}
	
	
	public ListIndexValue getListIndexTable()
	{
		ListIndexValue list = new ListIndexValue();
		for(int i=0; i<avl.size(); i++)
		{   
			IndexTable it = getIndexTable(i);
//			ArrayList<String> al = new ArrayList<String>();
			
		    //RICONTROLLARE ISTRUZIONE 
			
			list.add(new IndexValue( getIndexTable(i).getNode_id()));	
			for(int k=0; k<it.size();k++)
				list.getListElement(i).addCF(it.getNode(k).getNode_id());
		}
	    return list;
	}
	
	
	public int addIndexTable(IndexTable it)
	{
		avlLock.lock();
//		System.out.println("[ITM DEBUG]lock4");

		it.setDirty();
		int retval= avl.add_node(it);
		avlLock.unlock();
//		System.out.println("[ITM DEBUG]unlock4");

		return retval;
	}
	
	public void removeIndexTableNode(IndexTableName itn, String CF){
		avlLock.lock();

		IndexTable it = (IndexTable) avl.search(itn.toFormattedString());
//		it.printTree();
		if (it==null){
//			System.out.println("[ITM DEB] it "+it.getNode_id()+" null!");
			avlLock.unlock();
			return;
		}
//		toRemoveIndexTables.add(it);
//		System.out.println("[ITM DEB] it "+it.getNode_id()+" size:"+it.sizeEffective());
		it.removeNode(CF);
//		System.out.println("[ITM DEB] it "+it.getNode_id()+" size:"+it.sizeEffective());

//		System.out.println("=======>[INDEX TABLE MANAGER][DEBUG]index table size:"+it.sizeEffective());
		if (it.sizeEffective()<=0){
//			System.out.println("[ITN DEB] removing it "+it.getNode_id());
			avl.remove_node(it);
//			System.out.println("[ITN DEB] removed it "+it.getNode_id());
//			it.printTree();
		}
		avlLock.unlock();

//		it.printTree();
	}
	
	public void loadSignal1Routine(){
		avlRecovery2=avlRecovery1;
		avlRecovery1= new AVLStringKeyTree();
	}
	
	public void loadSignal2Routine(){
		avlRecovery1= new AVLStringKeyTree();
		avlRecovery2 = new AVLStringKeyTree();
	}
	
	public void loadSignal3Routine(){
		avlRecovery2= new AVLStringKeyTree();
	}
	
	

	
	/**
	 * Gives the number of elements IndexTables for which the ChompServer is authoritative.
	 * Used by the {@link MetricsManager} to compute the load of each node in the overlay.
	 * @return the number of elements IndexTables for which the ChompServer is authoritative.
	 */
	
	public int getIndexTablesCardinality(){
		return avl.size_effective();
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+INDEXTABLEMANAGERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	private void printlnNoTag(String s){
		if (verbose)
			System.out.println(s);
	}
	
	private void println(){
		if (verbose)
			System.out.println();
	}
	
	public void mergeLevel(byte recoveryLevel){
//		System.out.println("[ITM DEB]taking lock");
		avlLock.lock();
//		System.out.println("[ITM DEB]lock token");

		IndexTable it;
		if (recoveryLevel==RECOVERY1){
			println("merging recovery 1 to authoritative");
//			avl.printTree();
			println();
			for (int i=0; i<avlRecovery1.size();++i){
				if ((it=(IndexTable)avlRecovery1.getNodeX(i))==null)
					continue;
				println("adding it:"+it.getNode_id());
				it.resetNode();
				avl.add_node(it);
//				avl.printTree();
				println();

			}
			avlRecovery1=avlRecovery2;
			avlRecovery2= new AVLStringKeyTree();
			avlLock.unlock();

			return;
		}

		println("merging recovery 2 to recovery 1");
		
		for (int i=0; i<avlRecovery2.size();++i){
			if ((it=(IndexTable)avlRecovery2.getNodeX(i))==null)
				continue;
			println("adding it:"+it.getNode_id());
			it.resetNode();
			avlRecovery1.add_node(it);
//			avlRecovery1.printTree();
			println();

		}
		avlRecovery2= new AVLStringKeyTree();	
		avlLock.unlock();

	}


	public IndexTable[] getIndexTablesArrayUntilIndex(int untilIndex, int chompeer_id) {
//		Boolean more=true;
		IndexTable it;
		IndexTable [] retval;
		ArrayList<IndexTable> itArray = new ArrayList<IndexTable>();
		if(untilIndex<chompeer_id){
			for (int i=0; i<avl.size();++i){
				if ((it=(IndexTable)avl.getNodeX(i))==null)
					continue;
				if ((it.getHash()<=untilIndex)||(it.getHash()>chompeer_id))
					itArray.add(it);
			}
		}
		else{
			for (int i=0; i<avl.size();++i){
				if ((it=(IndexTable)avl.getNodeX(i))==null)
					continue;
				if ((it.getHash()>chompeer_id)&&(it.getHash()<=untilIndex))
					itArray.add(it);
			}
		}
		retval = new IndexTable[itArray.size()];
		for(int i=0; i<retval.length;++i)
			retval[i]= itArray.get(i);
		for (int i=0; i<retval.length;++i){
//			System.out.println("òòòòòòòòòòò[ITM][DEBUG] table to push back:"+retval[i].getNode_id()+" hash:"+ retval[i].getHash());
//			println("[DEBUG] table to push back:"+retval[i].getNode_id()+" hash:"+ retval[i].getHash());
		}
		return retval;
		
	}


	public void removeIndexTableArray(IndexTable[] indexTableArray) {
		for(int i=0; i<indexTableArray.length;++i){
			for(int j=0; j<indexTableArray[i].size();++j){
				if (indexTableArray[i].getNode(j)!=null)
					removeIndexTableNode(indexTableArray[i].getIt_name(), indexTableArray[i].getNode(j).getCF());
			}
		}
	}
	
	private void removeIndexTable(IndexTableName itn, byte recLevel){
		IndexTable it;
		ArrayList<IndexTableNode>it_nodes;
		IndexTableNode itNode;
		
		if (recLevel==AUTHORITATIVE){
			it=(IndexTable) avl.search(itn.toFormattedString());
			it_nodes=new ArrayList<IndexTableNode>();
			
			for (int i=0; i<it.size();++i){
				if ((itNode=(it.getNode(i)))!=null)
					it_nodes.add(itNode);
			}
			for (int i=0; i<it_nodes.size();++i){
//				it.printTree();
				it.removeNode(it_nodes.get(i));
			}
			if (it.sizeEffective()>0){
//				System.out.println("[ITM DEB] error in remove index table:"+it.sizeEffective());
//				it.printTree();
//				System.out.println("[ITM DEB] Nodi rimossi:");
//				for (int i=0; i<it_nodes.size();++i)
//					System.out.println("i:"+i+" CF:"+it_nodes.get(i).getCF());
			}
			else {
//				System.out.println("[ITM DEB] removing it:"+it.getNode_id());
				avl.remove_node(it);
//				System.out.println("[ITM DEB] removed it:"+it.getNode_id());
			}
		}
		else if (recLevel==RECOVERY1){
			it=(IndexTable) avlRecovery1.search(itn.toFormattedString());
			it_nodes=new ArrayList<IndexTableNode>();
			
			for (int i=0; i<it.size();++i){
				if ((itNode=(it.getNode(i)))!=null)
					it_nodes.add(itNode);
			}
			for (int i=0; i<it_nodes.size();++i){
				it.removeNode(it_nodes.get(i));
			}
			if (it.sizeEffective()>0)
				println("[DEB] errore in remove index table");
			else 
				avlRecovery2.remove_node(it);
		}
		else if (recLevel==RECOVERY2){
			it=(IndexTable) avlRecovery2.search(itn.toFormattedString());
			it_nodes=new ArrayList<IndexTableNode>();
			
			for (int i=0; i<it.size();++i){
				if ((itNode=(it.getNode(i)))!=null)
					it_nodes.add(itNode);
			}
			for (int i=0; i<it_nodes.size();++i){
				it.removeNode(it_nodes.get(i));
			}
			if (it.sizeEffective()>0)
				println("[ DEB] errore in remove index table");
			else 
				avlRecovery2.remove_node(it);
		}
	}


	public void moveIndexTableArrayToRecovery1(IndexTable[] indexTableArray) {
//		avlRecovery1= new AVLStringKeyTree();
//		System.out.println("3333333333333333333[ITM] indexTableArray size:"+indexTableArray.length);
//		for(int i=0; i<indexTableArray.length;++i)
//			System.out.println("[ITM DEB]"+i+":"+indexTableArray[i].getIt_name().toFormattedString());
		
		for(int i=0; i<indexTableArray.length;++i){
//			System.out.println("[ITM DEB] adding it"+indexTableArray[i].getIt_name().toFormattedString()+" of size:"+indexTableArray[i].size()+ " effective:"+indexTableArray[i].sizeEffective());
//			indexTableArray[i].printTree();
			add(indexTableArray[i], RECOVERY1);
//			System.out.println("[ITM DEB] added it array");

			removeIndexTable(indexTableArray[i].getIt_name(),AUTHORITATIVE);
			
//			for(int j=0; j<indexTableArray[i].size();++j){
//				System.out.println("[ITM DEB]j:"+j);
//				if (indexTableArray[i].getNode(j)!=null){
//					System.out.println("[ITM DEB]avl size:"+avl.size_effective()+" it:"+indexTableArray[i].getNode_id()+" item:"+j+" of:"+indexTableArray[i].size());
//					removeIndexTableNode(indexTableArray[i].getIt_name(), indexTableArray[i].getNode(j).getCF());
//					System.out.println("[ITM DEB]avl size:"+avl.size_effective());
//				}
////				while ((j<indexTableArray[i].size())&&(indexTableArray[i].getNode(j)!=null)){
////					System.out.println("[ITM DEB DEB DEB]removing traveler:"+indexTableArray[i].getNode(j).getCF()+" from index table:"
////							+indexTableArray[i].getIt_name()+"9999999999999999");
////					System.out.println("[ITM DEB]avl size:"+avl.size_effective());
////					removeIndexTableNode(indexTableArray[i].getIt_name(), indexTableArray[i].getNode(j).getCF());
////					
////					System.out.println("[ITM DEB]avl size:"+avl.size_effective());
////				}
//			}
		}
	}


	public void resetTables() {
		avlLock.lock();
		avl=new AVLStringKeyTree();
		avlRecovery1= new AVLStringKeyTree();
		avlRecovery2= new AVLStringKeyTree();
		avlLock.unlock();
	}

	


	
	
	/*
	 * index table ricavate tramite itm con get() da 0 a avl.size()
	 * creare una lista che contiene il nome del index table seguita da tutti i valori di CF al suo interno
	 
	 
	  in ricezione un parser si legge lo straam e si ricrea lo index table
	 
	 * usare object input stream e object output stream 
	 * con get object
	 */
	
	

}
