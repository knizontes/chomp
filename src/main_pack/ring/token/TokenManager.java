package main_pack.ring.token;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main_pack.data.messaging.Request;
import main_pack.loadBalancer.LoadBalancerManager;
import main_pack.metrics.MetricsManager;
import main_pack.request_server.RequestServer;
import main_pack.ring.Chompeer;
import main_pack.ring.client.RingClient;
import main_pack.ring.client.RingClientSocketListener;
import main_pack.ring.common.RingPacket;
import main_pack.utils.Timestamp;

/**
 * 
 * Gestore per il token e per l'avvio della routine di migrazione
 * 
 * Implementa le politiche per la creazione e gestione (lettura e aggiornamento) del token, gestisce il bilanciamento attivo 
 * del carico del sistema  provocando la migrazione del Chompeer se si verifica la condizione di sbilanciamento 
 * (il Chompeer ha un load factor n volte inferiore rispetto a quello del nodo con carico maggiore con n > LOAD_BALANCER_RATIO_THRESOLD )
 *
 */
public class TokenManager implements Runnable{
	
	public final static int LOAD_BALANCER_RATIO_THRESOLD=2;
	private static final String TOKENMANAGERTAG="[TOKEN MANAGER]";

	private Chompeer chompeer;
	private RingClient ringClient;
	private MetricsManager metricsMan;
	private RequestServer requestServer;
	private TokenGenerator tokenGen;
	private Boolean active=false;
	private int record_id=Integer.MAX_VALUE;
	private Token t;
	private long presentTokenId=0;
	private LoadBalancerManager lb_man;
	
	private Boolean chompeerMigrating=false;

	private Boolean presentMigratingState=false;
	
	private Lock requestesLock = new ReentrantLock();
	private ArrayList<Request> requestes = new ArrayList<Request>();
	private Token lastToken= new Token();
	
	private Lock waitLock = new ReentrantLock();
	private Condition handlingToken= waitLock.newCondition();
	
	private Boolean verbose;
	

	public TokenManager(Chompeer chompeer, Boolean verbose){
		this.chompeer=chompeer;
		this.verbose=true;
		lb_man = new LoadBalancerManager(this.chompeer);
	}
	
	public void setTokenGenerator(TokenGenerator tokenGen){
		this.tokenGen=tokenGen;
	}
	
	public void setRingClient(RingClient ringClient){
		this.ringClient=ringClient;
	}
	
	private void  setMetricMan(MetricsManager metricsMan){
		this.metricsMan=metricsMan;
	}
	
	private void setRequestServer(RequestServer reqServer){
		requestServer=reqServer;
	}
	
	public void setup(RequestServer reqServer, MetricsManager metricsMan){
		setRequestServer(reqServer);
		setMetricMan(metricsMan);
		record_id=lastToken.addTokenRecord(new TokenRecord(chompeer.getChompeer_ip(), chompeer.getChompeer_id(), 0));
		active=true;
	}
	
	public Boolean active(){
		return active;
	}

	
	public void addRequest(Request req){
		if(!active)
			return;

		if (req.getReqType()==Request.PUT)
			requestServer.put(req);
		else if (req.getReqType()==Request.REMOVE_ALL)
			requestServer.removeAll(req);
		req.setRequestOwnerId(chompeer.getChompeer_id());
		requestesLock.lock();
		requestes.add(req);
		requestesLock.unlock();
	}
	
	private void setToken(Token t){
		if(!active)
			return;

		t.printRecords();
		ringClient.setToken(t);

	}
	
	private void updateRecord(Token t){
		
		ArrayList<TokenRecord>trs=t.getRecords();
		
		if((trs.size()>0)&&(trs.get(0).getChompeer_id()<0))
			t.removeTokenRecord(0);
		
		Boolean exit;
		
		for (int i=0; i<trs.size();++i){
			exit=false;
			while (chompeer.getPeerNode(trs.get(i).getChompeer_id())==null){
				trs.remove(i);
				System.out.print(".");
				if (i>=trs.size()){
					exit=true;
					break;
				}
			}
			if (exit)
				break;
		}
		
		println("Updating token, my id is:"+chompeer.getChompeer_id());
		if ((trs.size()>record_id)){
			if (trs.get(record_id).getChompeer_id()!=chompeer.getChompeer_id()){
				println("old record id:"+trs.get(record_id).getChompeer_id()+" is different than "+chompeer.getChompeer_id());
				for (int i=0; i<trs.size();++i){
					if (t.getRecords().get(i).getChompeer_id()==chompeer.getChompeer_id()){
						record_id=i;
						t.getRecords().get(i).setLoadFactor(metricsMan.getLoadFactor());
						println("Record updated");
						return;
					}
					println(" record id:"+trs.get(i).getChompeer_id()+" is different than "+chompeer.getChompeer_id());
				}
				
				println("adding new token record ");
				TokenRecord tr = new TokenRecord(chompeer.getChompeer_ip(), chompeer.getChompeer_id(), metricsMan.getLoadFactor());
				println("ip:"+tr.getChompeer_ip()+ " id:"+tr.getChompeer_id()+" lf:"+tr.getLoadFactor());
				record_id=t.addTokenRecord(tr);
				return;
			}
			else {
				t.getRecords().get(record_id).setLoadFactor(metricsMan.getLoadFactor());
				println("Record updated (2)");
			}
		}
		else{
			record_id=t.addTokenRecord(new TokenRecord(chompeer.getChompeer_ip(), chompeer.getChompeer_id(), metricsMan.getLoadFactor()));
			println("added new token record (2)");
		}
	}
	
	public void tokenRoutine(Token t){	
		waitLock.lock();
		this.t=t;
		handlingToken.signal();
		waitLock.unlock();
	}
	
	private void tokenRoutine(){
		if(!active)
			return;
		
		
		println(" old token:");
		t.printRecords();
		
		/*if the received token has an id which is minor than the current one discard it*/
		if (t.getTokenId()<presentTokenId)
			return;
		
		tokenGen.tokenReceived();
		
		/*if the received token has an id which is greater than the current one update the present token id*/
		if (t.getTokenId()>presentTokenId)
			presentTokenId=t.getTokenId();
		
		
		
		if (chompeerMigrating){
			/*if (chompeerMigrating2round){
				chompeerMigrating2round=false;
				
			}
			else */
			if (!presentMigratingState){
				if (t.getMigratingChompeerId()!=chompeer.getChompeer_id())
					println("[ERROR?]token migrating id:"+t.getMigratingChompeerId()+" present chompeer id:"+chompeer.getChompeer_id());
				t.resetMigratingChompeer();
				chompeerMigrating=false;
			}
			if (t.getMigratingChompeerId()==chompeer.getChompeer_id()){
				t.resetMigratingChompeer();
				chompeerMigrating=false;
			}
		}
		
		if (chompeer.getPeerNode(t.getMigratingChompeerId())==null)
				t.resetMigratingChompeer();
		
		long maxLoadFactor=0;
		ArrayList<TokenRecord> trs;
		trs=t.getRecords();
		int maxLoadedChompeerId=-1;
		lastToken=t;
		
		Boolean exit;
		for (int i=0; i<trs.size();++i){
			exit=false;
			while (chompeer.getPeerNode(trs.get(i).getChompeer_id())==null){
				trs.remove(i);
				System.out.print(":");
				if (i>=trs.size()){
					exit=true;
					break;
				}
			}			
			if (exit)
				break;
			
			if (maxLoadFactor<trs.get(i).getLoadFactor()){
				maxLoadFactor=trs.get(i).getLoadFactor();
				maxLoadedChompeerId=trs.get(i).getChompeer_id();
			}
		}
		for (int i=0; i<trs.size();++i){
			if (trs.get(i).getChompeer_id()==chompeer.getChompeer_id()){
				record_id=i;
				break;
			}
				
		}
		
//		System.out.println("[DEBUG TOKEN MAN] new token:");

		chompeer.setBusiestNodeId(maxLoadedChompeerId);
		if (((maxLoadFactor/(metricsMan.getLoadFactor()+1))>=LOAD_BALANCER_RATIO_THRESOLD)&&(!t.migratingChompeer())){
			chompeerMigrating=true;

			presentMigratingState=true;
			println(" max load factor:"+maxLoadFactor+" from node:"+maxLoadedChompeerId+ " my load factor:"+metricsMan.getLoadFactor()+" starting load balancer routine (id:"+chompeer.getChompeer_id()+")...");
			
			//implement the load balancer functions
			lb_man.transferChompeer(maxLoadedChompeerId);
			println("ended load balancer routine with id:"+chompeer.getChompeer_id());
			

			t.setMigratingChompeer(chompeer.getChompeer_id());
			
		}
		
		for (int i=0; i<t.getRequests().size();++i){
			/*deleting the piggybacked request if the owner is the peer itself*/
			if (t.getRequests().get(i).getRequestOwnerId()==chompeer.getChompeer_id())
				t.resetRequest(i);
			else {
				/*deleting the piggybacked request if the request has been red from all the peers*/
				if (t.getRequests().get(i).getRedFromPeersNum()>=chompeer.getPeersNum())
					t.resetRequest(i);
				/*executing the piggybacked request*/
				else {
					t.getRequests().get(i).requestRed();
					if (t.getRequests().get(i).getRequestType()==Request.PUT)
						requestServer.put(t.getRequests().get(i).getRequest());
					else if (t.getRequests().get(i).getRequestType()==Request.REMOVE_ALL)
						requestServer.removeAll(t.getRequests().get(i).getRequest());
				}
			}
				
		}
		requestesLock.lock();
		for (int i=0; i<requestes.size();++i){
			t.addRequest(new TokenRequest(requestes.get(i),chompeer.getChompeer_id()));
		}
		requestes.clear();
		requestesLock.unlock();
		
		
		updateRecord(t);

		setToken(t);
		println("token set");
	}
	
	public int getPeersNum(){
		return chompeer.getPeersNum();
	}
	
	public void generateToken(){
		if (chompeer.isFirstNode()){
			lastToken.increaseTokenId();
			setToken(lastToken);
			println( "\n________________\nGenerated a new token!!!\n________________");
		}
	}
	
	public void resetPresentMigratingState(){
		presentMigratingState=false;
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+TOKENMANAGERTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}


	public void run() {
		for(;;){
			waitLock.lock();
			try {
				handlingToken.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tokenRoutine();
			waitLock.unlock();
		}
	}

}
