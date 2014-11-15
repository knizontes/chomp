package main_pack.ring.token;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenFuture implements Callable<Void>{

	private Lock lock = new ReentrantLock();
	private Condition gotATokenCondition= lock.newCondition();
	
	public void tokenReceived(){
		lock.lock();
		gotATokenCondition.signal();
		lock.unlock();
	}
	
	public Void call() throws Exception {
		lock.lock();
		gotATokenCondition.await();
		lock.unlock();
		return null;
	}

}
