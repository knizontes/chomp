package main_pack.ring.recovery.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;



import main_pack.chomp_engine.ChompEngine;
import main_pack.data.IndexTable;
import main_pack.data.IndexTableNode;
import main_pack.data.Traveler;
import main_pack.data.messaging.IndexValue;
import main_pack.ring.recovery.common.RecoveryObject;

/**
 * 
 * Thread istanziato da {@link RecoveryServer} per la gestione della connessione in entrata necessaria al trasferimento di {@link RecoveryObject}
 *
 */
public class RecoveryServerThread implements Runnable{

	private Socket s;
	private ChompEngine chomp_eng;
	
	public RecoveryServerThread (Socket s, ChompEngine chomp_eng){
		this.s = s;
		this.chomp_eng = chomp_eng;
	}
	
	public void run() {
		InputStream is;
		byte roLoadSignalType;
		try {
			is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			RecoveryObject ro = (RecoveryObject)ois.readObject();
			roLoadSignalType=ro.getLoadSignalType();
			switch (roLoadSignalType) {
			case RecoveryObject.LOAD_SIGNAL_NULL:
				try {
					indexTableArrayRoutine(ro);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			case RecoveryObject.LOAD_SIGNAL_1:
//				chomp_eng.pushBackTablesToNewNode(ro.getUntilId());
//				System.out.println("[REC SERVER] load sig 1 start");
				chomp_eng.loadSignal1Routine(ro.getNewNodeId(), ro.getNewNodeIp());
//				System.out.println("[REC SERVER] load sig 1 end");
				break;
			case RecoveryObject.LOAD_SIGNAL_2:
//				System.out.println("[REC SERVER] load sig 2 start");
				chomp_eng.loadSignal2Routine();
//				System.out.println("[REC SERVER] load sig 2 end");
				break;
			case RecoveryObject.LOAD_SIGNAL_3:
//				System.out.println("[REC SERVER] load sig 3 start");
				chomp_eng.loadSignal3Routine();
//				System.out.println("[REC SERVER] load sig 3 end");
				break;
			case RecoveryObject.SIGNAL_1_REPLY:
				try {
//					System.out.println("[REC SERVER] load sig 1 reply start");
					indexTableArrayRoutine(ro);
//					System.out.println("[REC SERVER] load sig 1 reply end");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			
			//if (ro.getObjectType()==RecoveryObject.INDEX_VALUE_ARRAY)
			
//			if (ro.getObjectType()==RecoveryObject.TRAVELER_ARRAY)
			s.close();
//			sstablePatchRoutine(ro);
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
//	public void sstablePatchRoutine(RecoveryObject ro){
//		
//	}
	
	public void indexTableArrayRoutine(RecoveryObject ro) throws Exception{
		InputStream is;
		IndexTable[] it_array=ro.getIndexTableArray();
		byte recLevel = ro.getRecoveryLevel();
		String [] cf_array = chomp_eng.missingValuesInHeaderTableFromIncomingIndexTables(it_array, recLevel);
		Traveler [] travelers;
		IndexTableNode itn;
//		PrintWriter out;
//		BufferedReader in;
		OutputStream os;
		try {
			os = s.getOutputStream();
			is = s.getInputStream();
//			in=new BufferedReader(new InputStreamReader(s.getInputStream()));
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(cf_array);
			oos.flush();
			
			
//			is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			travelers=(Traveler[])ois.readObject();
			Boolean added;
//			System.out.println("GGGGGGGGGG size array it:"+it_array.length+" travelers array size:"+travelers.length+" cf array length:"+cf_array.length);
//			for (int i=0; i<travelers.length;++i)
//				System.out.println(i+"."+travelers[i].getCF());
			for(int i=0; i<it_array.length;++i){
				for (int j=0; j<it_array[i].size();++j){
					added=false;
					if ((itn=it_array[i].getNode(j))==null)
						continue;
					for (int k=0;k<travelers.length;++k){
//						System.out.println("######GG traveler:"+travelers[k].getCF()+" it node:"+itn.getCF());
						if (travelers[k].getCF().equals(itn.getCF())){
//							System.out.println("traveler:"+travelers[k].getCF());
//							System.out.println("it object:"+it_array[i].toString());
//							System.out.println("[REC SERVER TH]it name:"+it_array[i].getIt_name().toString());
							chomp_eng.addIndexTableValue(travelers[k], it_array[i].getIt_name().toFormattedString(),it_array[i].getHash(), recLevel);
							added=true;
							break;
						}
						
					}
					if (!added){
//						System.out.println("[REC SERVER] adding "+itn.getCF()+" to it:"+it_array[i].getIt_name().toFormattedString()+" rec lev:"+ recLevel);
						chomp_eng.addIndexTableValue(chomp_eng.getHT().search(itn.getCF()), it_array[i].getIt_name().toFormattedString(),it_array[i].getHash(),recLevel);
					}
				}
			}
			oos.close();
			os.close();
			ois.close();
			is.close();
			
//			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

	
	
}
