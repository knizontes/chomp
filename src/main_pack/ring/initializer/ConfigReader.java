package main_pack.ring.initializer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


import main_pack.ring.FingerTable;
import main_pack.ring.PeerNode;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * 
 * Utility Class per l'inizializzazione del sistema a partire dal file di configurazione generato attraverso la classe:
 * ChompeerConfigMaker.
 *
 */
public class ConfigReader {

	private FingerTable ft;
	private String configFilePath;
	
	public ConfigReader (FingerTable ft, String configFilePath){
		this.ft=ft;
		this.configFilePath=configFilePath;
	}
	
	@SuppressWarnings("finally")
	public int init(){
		SAXBuilder builder = new SAXBuilder(); 
		Document document;
		int retval=0, tmp;
		try {
			document = builder.build(new File(configFilePath));
			Element root = document.getRootElement();
			List rootChildren = root.getChildren(); 
			Iterator iterator = rootChildren.iterator();
			Element keyspace = (Element)iterator.next();
			ft.setKeyspace(Integer.parseInt(keyspace.getAttributeValue("dim")));
			Element peersEl = (Element)iterator.next();
			List peers = peersEl.getChildren();
			Iterator peers_iterator = peers.iterator();
			while(peers_iterator.hasNext()){
				Element item = (Element)peers_iterator.next(); 
				tmp=ft.addPeer(Integer.parseInt(item.getAttributeValue("id")), item.getAttributeValue("ip"));
				if ((tmp>=0)&&(retval>=0))
					retval=tmp;
			}
			tmp=closeRing();
			if ((tmp>=0)&&(retval>=0))
				retval=tmp;
			return retval;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			return retval;
		}
		
	}
	
	private int closeRing(){
		PeerNode firstNode = ft.keySuccessor(0);
		return ft.addPeer(ft.getKeyspace(),firstNode.getIp().ipToString());
	}
	
}
