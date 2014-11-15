package main_pack.ring.token;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import main_pack.ring.client.RingClient;

//import main_pack.ring.common.RingPacket;


/**
 * 
 * Generatore del token che si attiva nel caso di scomparsa del token
 * 
 */
public class TokenGenerator implements Runnable{

	public final int TOKEN_WAIT_SECONDS;
	private final static int OVERHEAD_ROUNDTRIP_TIME_PER_PEER=7;
	private TokenManager tokenManager;
	private TokenFuture tf;
	
	
	public TokenGenerator(TokenManager tokenManager){
		this.tokenManager=tokenManager;
		TOKEN_WAIT_SECONDS=this.tokenManager.getPeersNum()*(RingClient.PING_INTERLEAVING_TIME_MS/1000)+
				this.tokenManager.getPeersNum()*OVERHEAD_ROUNDTRIP_TIME_PER_PEER;
	}
	
	public void tokenReceived(){
		tf.tokenReceived();
	}
	
	public void run() {
		ExecutorService executor;
		Future<Void> future;
		
		
		
		for(;;){
			tf=new TokenFuture();
			/*the executor allows to wait until timeout*/
			executor = Executors.newSingleThreadExecutor();
			/*initialize the future with the token future*/
			future = executor.submit(tf);
			try {
				future.get(TOKEN_WAIT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				generateToken();
			}
		}
	}
	
	private void generateToken(){
		tokenManager.generateToken();
	}
	
	

}
