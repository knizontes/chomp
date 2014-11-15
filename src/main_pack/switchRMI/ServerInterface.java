package main_pack.switchRMI;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

import main_pack.data.IndexTableName;
import main_pack.data.Traveler;


/**
 * 
 * Interfaccia usata dal ClientEngine
 * 
 * Espone i metodi RMI necessari all'invocazione remota delle operazioni di base quali:
 * <p> POST;
 * <p> GET;
 * <p> PUT;
 * <p> REMOVE;
 * <p> REMOVE_ALL.
 * 
 * Ciascun metodo ritorna un tipo String contenente la conferma della presa in carico da parte del sistema della richiesta
 * sottomessa lato Client attraverso l'invocazione remota del motodo opportuno (RMI).
 * 
 *
 */
//INTERFACCIA CON METODI CHE IL SERVER ESPONE AL CLIENT
public interface ServerInterface extends Remote {
	
	public String getEcho(String echo) throws RemoteException;
	
	//passo al server un identificativo del numero della richiesta
	public String post(Traveler myself, IndexTableName[] itn) throws RemoteException, UnknownHostException, IOException, ServerNotActiveException;
	public String get(Traveler myself, IndexTableName[] itn, int idReq)throws RemoteException, UnknownHostException, IOException;
//	public String get(InetAddress clientIp, int port,Traveler myself, IndexTableName itn)throws RemoteException, UnknownHostException, IOException;
	public String put(Traveler myself)throws RemoteException, UnknownHostException, IOException;
	public String remove(Traveler myself, IndexTableName []itm) throws RemoteException;
	public String removeAll(Traveler myself) throws RemoteException;

}