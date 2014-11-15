package main_pack.loadBalancer;

import java.io.IOException;

import main_pack.ring.Chompeer;

/**
 * Classe che istanzia l'oggetto in grado di monitorare il sistema e capire quando ci sono situazioni di sbilanciamento di
 * carico. In questo caso il nodo piu' scarico prende un nuovo id e si sposta per prendere in gestione tabelle dal nodo
 * piu' carico. Viene azionato il meccanismo di spostamento delle tabelle.
 * 
 */
public class LoadBalancerManager {
	
	/*Wait time before transfer function return*/
	public final static int SETUP_WAIT_TIME=10000;
	private Chompeer chompeer;
	
	public LoadBalancerManager(Chompeer chompeer){
		this.chompeer=chompeer;
	}
	
	public void transferChompeer(int busiestNodeId){
		try {
			chompeer.pingOff();
			chompeer.removeChompeerFromRing();
			chompeer.getNewId(busiestNodeId);
			chompeer.sendNewNodeJoinSignals();
			chompeer.join();
			chompeer.pingOn();
			chompeer.resetMigratingState();
			Thread.sleep(SETUP_WAIT_TIME);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}
