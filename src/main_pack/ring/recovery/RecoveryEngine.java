package main_pack.ring.recovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.text.StyledEditorKit.ItalicAction;




import main_pack.chomp_engine.ChompEngine;
import main_pack.data.HeaderTable;
import main_pack.data.IndexTable;
import main_pack.data.IndexTableManager;
import main_pack.data.IndexTableName;
import main_pack.data.Traveler;
import main_pack.data.messaging.ListIndexValue;
import main_pack.ring.Chompeer;
import main_pack.ring.recovery.common.RecoveryObject;
import main_pack.ring.recovery.server.RecoveryServer;
//import trasferimentoTCP.ClientPushList;
import main_pack.utils.Timestamp;

/**
 * 
 * Il client per le operazioni di recovery, gestisce l’inoltro delle tabelle ai nodi che ne possiedono le repliche secondarie.
 * Provvede all'inoltro verso altri due nodi del sistema delle copie (dette repliche secondarie) delle risorse per le
 * quali il peer è autoritativo (repliche primarie).
 *
 */
public class RecoveryEngine implements Runnable{
	
	private final static String RECOVERY_ENGINE_TAG="[RECOVERY ENGINE]";

	
	private Boolean verbose;
	private Lock conditionLock;
	private Condition updateOccurredCondition;
	private ChompEngine chomp_eng;
	private Chompeer chompeer;
	private Boolean sendTables=false;
//	private RecoveryServer rec_server;



	public RecoveryEngine(ChompEngine chomp_eng,Chompeer chompeer,Boolean verbose){
		this.verbose=verbose;
		conditionLock = new ReentrantLock();
		updateOccurredCondition = conditionLock.newCondition();
		this.chomp_eng=chomp_eng;
		this.chompeer=chompeer;
		chompeer.setRecoveryEngine(this);
//		this.rec_server=rec_server;
//		this.rec_server = new RecoveryServer(verbose,chomp_eng);
	}

	
	public Condition getUpdateOccurredCondition(){
		return updateOccurredCondition;
	}
	
	
	public void recoveryRoutine(RecoveryObject ro, InetAddress dstAddress){
		println("sending index tables to successor");
		RecoveryEngineThread ret = new RecoveryEngineThread(chomp_eng, dstAddress, ro, verbose);
		Thread recEngTh = new Thread (ret,"recovery engine thread");
		recEngTh.start();
	}
	
	public void pushBackTablesToNewNode(int untilIndex,InetAddress newNodeIp){
//		System.out.println("[REC ENG DEB]sending index tables to new node:"+untilIndex); 
		println("sending index tables to new node:"+untilIndex);
		IndexTable [] it_array;
//		System.out.println("[REC ENG DEB]making index tables array"); 
		it_array = chomp_eng.getIndexTableManager().getIndexTablesArrayUntilIndex(untilIndex,chompeer.getChompeer_id());
		for (int i=0; i<it_array.length;++i){
			println(" table to push back:"+it_array[i].getNode_id()+" hash:"+ it_array[i].getHash());
		}
//		System.out.println("[REC ENG DEB]made index tables array"); 

		RecoveryObject ro=new RecoveryObject(it_array, RecoveryObject.INDEX_TABLE_ARRAY, IndexTableManager.AUTHORITATIVE, RecoveryObject.SIGNAL_1_REPLY);
		RecoveryEngineThread ret;

		ret = new RecoveryEngineThread(chomp_eng, newNodeIp, ro, verbose);

		Thread recEngTh = new Thread (ret,"recovery engine thread");
		recEngTh.start();

	}

	public void sendNewNodeJoinSignals(){
		RecoveryObject signal1, signal2, signal3;
		signal1=new RecoveryObject(RecoveryObject.LOAD_SIGNAL_1);
		signal1.setUntilId(chompeer.getChompeer_id());
		signal1.setNewNodeIp(chompeer.getChompeer_ip());
		signal2=new RecoveryObject(RecoveryObject.LOAD_SIGNAL_2);
		signal3=new RecoveryObject(RecoveryObject.LOAD_SIGNAL_3);
		
		try{
//			System.out.println("recovery engine sending signals...");
			recoveryRoutine(signal1, chompeer.getSuccessorAddress());
			recoveryRoutine(signal2, chompeer.getSuccessor2Address());
			recoveryRoutine(signal3, chompeer.getSuccessor3Address());
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}
		
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	private String tag(){
		return (Timestamp.now()+"-"+RECOVERY_ENGINE_TAG);
	}
	
	public void sendTables(){
		sendTables=true;
	}
	
	public void run(){
		println("recovery engine started");
		IndexTable [] it_array;
		RecoveryObject toSendObject1;
		RecoveryObject toSendObject2;

		for(;;){
			conditionLock.lock();
			try {
				updateOccurredCondition.await(30000, TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {
				
			}
			conditionLock.unlock();
			if ((chomp_eng.getUpdates()==0)&&(!sendTables)){
				println("no changes occurred");
				continue;
			}
			sendTables=false;
			println(chomp_eng.getUpdates()+" some changes occurred, timeout expired, sending tables anyway...");
			chomp_eng.resetUpdates();
			it_array = chomp_eng.getIndexTableManager().getIndexTablesArray();
			toSendObject1 = new RecoveryObject(it_array, RecoveryObject.INDEX_TABLE_ARRAY, IndexTableManager.RECOVERY1);
			toSendObject2 = new RecoveryObject(it_array, RecoveryObject.INDEX_TABLE_ARRAY, IndexTableManager.RECOVERY2);
			try {
				recoveryRoutine(toSendObject1, chompeer.getSuccessorAddress());
				recoveryRoutine(toSendObject2, chompeer.getSuccessor2Address());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
		
	}


	

}
