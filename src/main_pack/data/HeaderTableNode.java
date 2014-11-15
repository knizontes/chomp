package main_pack.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import main_pack.chomp_engine.SST.SSTable;
import main_pack.chomp_engine.cache.CacheNode;
import main_pack.data.avl.string_key.AVLStringKeyNode;
import main_pack.data.avl.string_key.AVLStringKeyTree;

/**
 * 
 * Nodo dell'{@link HeaderTable} contenente le informazioni (SSTable, offset all'interno dell'SSTable, CacheNode,...) necessarie
 * al reperimento del viaggiatore cercato. 
 *
 */
public class HeaderTableNode extends AVLStringKeyNode{
	
	private long offset;
	private CacheNode CN;
	private Traveler t;
	private IndexTableNode it_node = null;
	private SSTable sst;
	
	
	public HeaderTableNode (String CF, long offset, SSTable sst){
		super (CF);
		this.offset = offset;
		this.sst=sst;
		CN=null;
		t = null;
	}

	/**
	 * Restituisce l'offset all'interno dell'SSTable necessario per indirizzare il dato strutturato ({@link Traveler}) associato al nodo 
	 * @return long
	 */
	public long getOffset() {
		return offset;
	}
	
	/**
	 * Restituisce l'indice di spiazzamento per il {@link Traveler} associato al nodo in esame
	 * @return indice di spiazzamento all'interno della SSTable per il Traveler in esame
	 */
	public int getIndex()
	{
		return (int)offset/Traveler.RECORD_SIZE;//WARNING!Da controllare!
	}

	
	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Resituisce il nodo della Cache associato all'{@link HeaderTableNode}
	 * @return {@link CacheNode}
	 */
	public CacheNode getCN() {
		return CN;
	}

	public void setCN(CacheNode CN) {
		this.CN = CN;
		if (this.CN==null)
			System.out.println("#CN NULL**");
	}
	
	/**
	 * Resetta il cache node associato all'{@link HeaderTableNode}
	 */
	public void resetCN() {
		CN = null;
	}

	/**
	 * Restituisce l'oggetto {@link Traveler} (in memoria) di cui l'{@link HeaderTableNode} e' competente
	 * @return {@link Traveler}
	 */
	public Traveler getTravelerRaw(){
		return t;
	}
	
	/**
	 * Restituisce l'oggetto {@link Traveler} (su disco) di cui l'{@link HeaderTableNode} e' competente
	 * @return {@link Traveler}
	 */
	public Traveler getTraveler() throws Exception {
		if (t!=null){
//			System.out.println("Record in cache");
			return t;
		}
		
		t = sst.readRecord(getIndex());
//		System.out.println("Fetched record from SSTable");
		return t;
		
	}
	
	/**
	 * Restituisce l'oggetto {@link Traveler} (su disco) di cui l'{@link HeaderTableNode} e' competente
	 * @return Traveler
	 */
	public Traveler getTravelerRecordFromSST() throws Exception{
		return sst.readRecord(getIndex());
	}
	
	/**
	 * Aggiorna l'oggetto {@link Traveler} (su disco, SSTable) di cui l'{@link HeaderTableNode} e' competente
	 * @param fieldValue
	 * @param fieldSize
	 * @param fieldOffset
	 * @throws Exception
	 */
	public void updateTravelerField(String fieldValue, int fieldSize, int fieldOffset) throws Exception{
		sst.updateRecord(getIndex(), fieldValue, fieldSize, fieldOffset);
		if (t!=null){
			if (fieldSize==Traveler.INFO_SIZE)
				t.setInfo(fieldValue);
			if (fieldSize==Traveler.PWD_HASH_SIZE)
				t.setPwdHash(fieldValue);
		}
			
	}

	/**
	 * Inizializza il valore dell'oggetto {@link Traveler} associato all'{@link HeaderTableNode}
	 * @param v
	 */
	public void setV(Traveler v) {
		this.t = v;
	}
	
	/**
	 * Resetta il valore dell'oggetto {@link Traveler} associato all'{@link HeaderTableNode}
	 */
	public void resetV(){
		t=null;
	}
	


	@Override
	public void setNode(AVLStringKeyNode nd) {
		// TODO Auto-generated method stub
		
	}

	public IndexTableNode getIt_node() {
		return it_node;
	}

	/**
	 * Inizializza il valore dell'{@link IndexTableNode} associato all'{@link HeaderTableNode}
	 * @param it_node
	 */
	public void setIt_node(IndexTableNode it_node) {
		this.it_node = it_node;
	}	

}
