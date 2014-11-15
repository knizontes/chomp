package xml_builder;

import org.jdom.*; 
import org.jdom.output.*; 

import java.io.*; 
import java.util.Scanner;


/**
 * 
 * Utility Class per la generazione del file di configurazione iniziale necessario all'avviamento del sistema.
 *
 */
public class ChompeerConfigMaker {

	private Element root;
	private Element peers;
	private Element keyspace;
	private Document document;
	
	public ChompeerConfigMaker (){
		init();
	}
	
	private void init(){
		try{
			root = new Element("FingerTable");
			peers = new Element("Peers");
			keyspace = new Element("Keyspace");
			document = new Document(root);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void setKeyspace(String keyspace){
		this.keyspace.setAttribute("dim",keyspace);
	}
	
	public void addChompeer(String ip, String id){
		Element el = new Element("peer");
		el.setAttribute("id",id);
		el.setAttribute("ip",ip);
		peers.addContent(el);
	}
	
	public void addChompeer(String ip, String id, String area){
		Element el = new Element("peer");
		el.setAttribute("id",id);
		el.setAttribute("ip",ip);
		el.setAttribute("area",area);
		peers.addContent(el);
	}
	
	public void flush(){
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		root.addContent(keyspace);
		root.addContent(peers);
		try {
			outputter.output(document, new FileOutputStream("peer.config.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		String ip,id,keyspace;
		ChompeerConfigMaker ccm= new ChompeerConfigMaker();
		Scanner sc = new Scanner(System.in);
		System.out.println("Inserire la dimensione del keyspace (circa il cubo del numero di nodi presenti nella rete ):");
		keyspace = sc.next();
		ccm.setKeyspace(keyspace);
		while (true){
			System.out.print("Inserisci l'ip di un peer o \"end\" per terminare:");
			ip = sc.next();
			if (ip.equals("end")) break;
			System.out.print("Inserisci l'id del peer:");
			id = sc.next();
			ccm.addChompeer(ip, id);
		}
		ccm.flush();
	}
	
	
	
}
