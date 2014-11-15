package main_pack.ring.recovery.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.zip.CRC32;



import main_pack.data.IndexTable;
import main_pack.data.Traveler;
import main_pack.data.messaging.IndexValue;
import main_pack.data.messaging.ListIndexValue;

/**
 * 
 * Classe rappresentativa dell'oggetto di recovery scambiato a livello di recovery layer tra i processi coinvolti e necessario
 * al mantenimento coerente delle IndexTable e dei Traveler con un grado di replicazione pari a 3.
 *
 */
public class RecoveryObject implements Serializable{
	
	
	
	public final static byte NULL_OBJECT=0;
	public final static byte CF_ARRAY=1;
//	public final static byte LIST_INDEX_VALUE=1;
	public final static byte TRAVELER_ARRAY=2;
	public final static byte INDEX_TABLE_ARRAY=3;
	
	public final static byte LOAD_SIGNAL_NULL=0;
	public final static byte LOAD_SIGNAL_1=1;
	public final static byte LOAD_SIGNAL_2=2;
	public final static byte LOAD_SIGNAL_3=3;
	public final static byte SIGNAL_1_REPLY=4;
//	public final static byte RECOVERY1=1;
//	public final static byte RECOVERY2=2;
//	
	
	private byte objectType;
	private byte recoveryLevel;
	private byte loadSignalType=LOAD_SIGNAL_NULL;
	
	private Object object;
	private int untilId;
	private InetAddress newNodeIp;
	
	public RecoveryObject(Object object, byte objectType, byte recoveryLevel){
		this.object=object;
		this.objectType=objectType;
		this.recoveryLevel=recoveryLevel;
	}
	
	public RecoveryObject(Object object, byte objectType, byte recoveryLevel, byte loadSignalType){
		this.object=object;
		this.objectType=objectType;
		this.recoveryLevel=recoveryLevel;
		this.loadSignalType=loadSignalType;
	}
	
	public RecoveryObject( byte loadSignalType){
		this.loadSignalType=loadSignalType;
	}
	
	public byte getObjectType(){
		return objectType;
	}
	
	public byte getRecoveryLevel(){
		return recoveryLevel;
	}
	
	public byte getLoadSignalType(){
		return loadSignalType;
	}
	
	public String[] getCFArray(){
		if (objectType!=CF_ARRAY)
			return null;
		return (String[]) object;
	}
	
	public IndexTable[] getIndexTableArray(){
		if (objectType!=INDEX_TABLE_ARRAY)
			return null;
		return (IndexTable[]) object;
	}
	
	public void setUntilId(int untilId){
		this.untilId=untilId;
	}
	
	public int getNewNodeId(){
		return untilId;
	}

	
	
	
//	public ListIndexValue getListIndexValue(){
//		if (objectType!=LIST_INDEX_VALUE)
//			return null;
//		return (ListIndexValue) object;
//	}
	
	public InetAddress getNewNodeIp() {
		return newNodeIp;
	}

	public void setNewNodeIp(InetAddress newNodeIp) {
		this.newNodeIp = newNodeIp;
	}

	public Traveler [] getTravelersArray(){
		if (objectType!=TRAVELER_ARRAY)
			return null;
		return (Traveler []) object;
	}
	
	

	
	
	
}
