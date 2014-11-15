package main_pack.ring.recovery;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;



import main_pack.chomp_engine.ChompEngine;
import main_pack.data.Traveler;
import main_pack.data.messaging.IndexValue;
import main_pack.ring.recovery.common.RecoveryObject;
import main_pack.ring.recovery.server.RecoveryServer;
import main_pack.utils.Timestamp;

public class RecoveryEngineThread implements Runnable{
	
	
	public final static String RECOVERY_ENGINE_THREAD_TAG="[RECOVERY ENGINE THREAD]";
	private ChompEngine chomp_eng;
	private Socket s;
//	private byte recoveryLevel;
	private InetAddress dstAddress;
	private RecoveryObject robj;
	private Boolean verbose;

	public RecoveryEngineThread ( ChompEngine chomp_eng,InetAddress dstAddress, RecoveryObject ro, Boolean verbose){
		this.chomp_eng=chomp_eng;
//		this.s=s;
//		this.recoveryLevel=recoveryLevel;
		this.robj=ro;
		this.dstAddress = dstAddress;
		this.verbose=verbose;
	}

	public void run() {
		InputStream is;
		Boolean tryConnect=true;
		byte sigType= robj.getLoadSignalType();
		while (tryConnect){
			try {
//				System.out.println("[REC ENG TH DEB]try connect");
				s= new Socket(dstAddress, RecoveryServer.RECOVERY_SERVER_PORT);
//				System.out.println("[REC ENG TH DEB]connection successful");

				OutputStream os = s.getOutputStream();
				is = s.getInputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(robj);
				oos.flush();
//				System.out.println("[REC ENG TH DEB]data written");

				if ((robj.getLoadSignalType()==RecoveryObject.LOAD_SIGNAL_1)||(robj.getLoadSignalType()==RecoveryObject.LOAD_SIGNAL_2)||(robj.getLoadSignalType()==RecoveryObject.LOAD_SIGNAL_3)){
//					System.out.println("the signal is:"+robj.getLoadSignalType()+", exiting routine!");
					return;
				}

				ObjectInputStream ois = new ObjectInputStream(is);
				String [] cf_array = (String [])ois.readObject();
//				println("----------->>>>>cf_array length:"+cf_array.length);
				for (int i=0; i<cf_array.length; ++i)
					println(i+". cf:"+cf_array[i]);
				
				Traveler [] traveler_array = chomp_eng.makeTravelerArray(cf_array);
				//			Socket send_socket = new Socket(s.getInetAddress(), RecoveryServer.RECOVERY_SERVER_PORT);
				//			RecoveryObject ro = new RecoveryObject(traveler_array, RecoveryObject.TRAVELER_ARRAY, robj.getRecoveryLevel());
				os = s.getOutputStream();
				oos = new ObjectOutputStream(os);
				oos.writeObject(traveler_array);
				oos.flush();
				ois.close();
				is.close();
				oos.close();
				os.close();
				s.close();
				if (sigType==(RecoveryObject.SIGNAL_1_REPLY)){
//					System.out.println("[REC ENG TH DEB] sig 1 reply moving tables");
//					System.out.println("ppppppppppp Odio questi dietro");
					chomp_eng.moveIndexTableArrayToRecovery1(robj.getIndexTableArray());
//					System.out.println("[REC ENG TH DEB] sig 1 reply tables moved");

				}
//				System.out.println("pppppppppp routine ended..."+robj.getLoadSignalType()+","+RecoveryObject.SIGNAL_1_REPLY);
				tryConnect=false;
			}catch (Exception e) {
//				System.out.println("[REC ENG TH DEB]connection error, retryng to transmit in 10 second(maybe the successor is down)");

				println("connection error, retryng to transmit in 10 second(maybe the successor is down)");
				/*if an error occurred sleep 10 sec before retry to transmit tables*/
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+RECOVERY_ENGINE_THREAD_TAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}
	
	
	
}
