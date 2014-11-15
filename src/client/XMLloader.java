package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import main_pack.data.IndexTableName;
import main_pack.data.Traveler;

/**
 * 
 * Utility class per il popolamento del sistema attraverso il caricamento di contenuti dal file travelers.xml 
 *
 */
public class XMLloader {

	public String filename = "travelers.xml";

	public XMLloader(String f)
	{
		this.filename = f;
	}
	/*
	 FORMATO file travelers.xml
	<?xml version="1.0" encoding="UTF-8"?>
	<Travelers>
	   <InfoTrav>
	     <traveler nome="" cognome="" cf=""/>
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	   </InfoTrav>
	   <InfoTrav>
	     <traveler nome="" cognome="" cf=""/>
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	   </InfoTrav>
	   <InfoTrav>
	     <traveler nome="" cognome="" cf=""/>
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	     <itn giorno="" tratta="" ora="">
	  </InfoTrav>  
	</Travelers>
	 */



	//*********************************************************	
	//********************** JDOM *****************************	
    /**
     * Metodo per creare un nuovo file xml vuoto, serve per inizializzare gli header xml
     */
	public void createEmptyFileXML()
	{

		Element root = new Element("Travelers");
		Document doc = new Document(root);

		XMLOutputter xml_out_file = new XMLOutputter();
		try {
			xml_out_file.setFormat(Format.getPrettyFormat());
			xml_out_file.output(doc, new FileOutputStream(filename));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    
	//aggiunge il traveler loggato nel file XML di travelers.xml
	/**
	 * Aggiunge al file xml, un nuovo Traveler insieme a tutte le sue tratte specificae nel parametro IndexTableName
	 * @param nome
	 * @param cognome
	 * @param cf
	 * @param info
	 * @param pwdHash
	 * @param itn
	 */
	public void addTravelerInFile(String nome, String cognome, String cf, String info, String pwdHash, ArrayList<IndexTableName> itn)
	{
		try {
			SAXBuilder saxb = new SAXBuilder();
			Document doc = saxb.build( new File(filename));
			Element root = doc.getRootElement();//Travelers
			//System.out.println(root.getName());

			Element _infoTrav = new Element("InfoTrav");// = root.getChild("InfoTrav");//InfoTrav 
			//System.out.println(_infoTrav.getName());

			Element _trav_attribute = new Element("traveler");

			//crea <traveler .....>
			_trav_attribute.setAttribute("CF", cf);
			_trav_attribute.setAttribute("nome", nome);
			_trav_attribute.setAttribute("cognome", cognome);
			_trav_attribute.setAttribute("info", info);
			_trav_attribute.setAttribute("pwdHash", pwdHash);
			
			_infoTrav.addContent(_trav_attribute);

			//crea <itn .....>
			for(IndexTableName itn_elem : itn)
			{
				Element _itn_element = new Element("itn");
				_itn_element.setAttribute("giorno", Integer.toString(itn_elem.getWeekday()) );
				_itn_element.setAttribute("tratta", Integer.toString(itn_elem.getRoute().getStart().getPlace()) 
						+"-"+Integer.toString(itn_elem.getRoute().getArrival().getPlace()) );
				_itn_element.setAttribute("ora", itn_elem.getTime().getOre()+":"+itn_elem.getTime().getMinuti());
				_infoTrav.addContent(_itn_element);
			}	


			root.addContent(_infoTrav);

			XMLOutputter xml_out_file = new XMLOutputter();
			xml_out_file.setFormat(Format.getPrettyFormat());
			xml_out_file.output(doc, new FileOutputStream(filename));

		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	/**
	 * Metodo che legge il file xml ed estrapola un ArrayList di oggetti InfoTrav
	 * @return ArrayList<InfoTrav> valore di ritorno 
	 */
	public ArrayList<InfoTrav> readTraveler()
	{
        
		ArrayList<InfoTrav> retval = new ArrayList<InfoTrav>();

		try {
			SAXBuilder saxb = new SAXBuilder();
			Document doc = saxb.build( new File(filename));
			Element root = doc.getRootElement();

			//System.out.println(root.getName());

			List children = root.getChildren(); 
			Iterator iterator = children.iterator(); 
			
			//iterator su InfoTrav
			while (iterator.hasNext())
			{ 
				//InfoTrav
				Element element = (Element)iterator.next(); 

				//System.out.println("--->"+element.getName());
				//System.out.println(element.getContentSize());
				//System.out.println(element.);
				List l = element.getChildren();
				Iterator it2 = l.iterator(); 

				InfoTrav infot = new InfoTrav();
				ArrayList<IndexTableName> array_itn = new ArrayList<IndexTableName>();
	
				int i=0;
				while(it2.hasNext())
				{   
		
					if(i==0)
					{	
     					Traveler traveler= null;
						Element traveler_attrib = (Element) it2.next();
					//	System.out.println("------->"+traveler_attrib.getName());
						
						//					
						traveler = new Traveler( traveler_attrib.getAttributeValue("CF"), 
								traveler_attrib.getAttributeValue("nome"), 
								traveler_attrib.getAttributeValue("cognome"),
								traveler_attrib.getAttributeValue("info"),
								traveler_attrib.getAttributeValue("pwdHash"));
	
	                  //  System.out.println("trav:+++"+traveler.getNome() +" "+ traveler.getCognome()+" "+traveler.getCF());
	                    infot.setT(traveler);
					}
                    
					else
					{
					
						Element _itn_element = (Element) it2.next();
						String g = _itn_element.getAttribute("giorno").getValue();
						String t = _itn_element.getAttribute("tratta").getValue();
						String o = _itn_element.getAttribute("ora").getValue();
						array_itn.add(new IndexTableName(g+"/"+t+"/"+o) );
						//System.out.println(g +" "+ t+ " "+o);
						infot.setAlitn(array_itn);
					}
					
					i++;
					
				}
				//infot = new InfoTrav(traveler, array_itn);
				retval.add(infot);
			} 

		} 	
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File XML non presente");
			return null;
		} 
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return retval;
	}


	//se readTraveler ritorna NULL allora si invoca createNewTraveler
	// per creare file XML tramite JDOM
	public void createNewFileTraveler(String nome, String cognome, String cf)
	{

		Element root = new Element("Client");
		Document doc = new Document(root);

		Element _traveler = new Element("Travelers");
		root.addContent(_traveler);

		Element _trav_attribute = new Element("traveler");

		_trav_attribute.setAttribute("CF", cf);
		_trav_attribute.setAttribute("nome", nome);
		_trav_attribute.setAttribute("cognome", cognome);

		_traveler.addContent(_trav_attribute);

		String NAME_FILE = cf+".xml";
		XMLOutputter xml_out_file = new XMLOutputter();
		try {
			xml_out_file.setFormat(Format.getPrettyFormat());
			xml_out_file.output(doc, new FileOutputStream(NAME_FILE));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	//************************************************
	//***************** FINE JDOM ********************	
/**
 * Main usato per la generazione del file traveler.xml e generazione dell'ArrayList<InfoTrav>
 * @param args
 */
	public static void main(String args[])
	{		
		String filename = "travelers.xml";
		XMLloader xl = new XMLloader(filename);
        
		//creazione file vuoto 
		xl.createEmptyFileXML();

		ArrayList<IndexTableName> array_viaggio1 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio2 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio3 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio4 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio5 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio6 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio7 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio8 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio9 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio10 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio11 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio12 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio13 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio14 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio15 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio16 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio17 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio18 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio19 = new ArrayList<IndexTableName>();
		ArrayList<IndexTableName> array_viaggio20 = new ArrayList<IndexTableName>();
		
		
		try {
			
			//array_viaggio1.add(new IndexTableName("1/1-2/11:11"));
			array_viaggio1.add(new IndexTableName("2/1-2/11:11"));
			array_viaggio1.add(new IndexTableName("3/1-3/11:11"));
			array_viaggio1.add(new IndexTableName("4/1-4/11:11"));
			array_viaggio1.add(new IndexTableName("5/1-5/11:11"));
			array_viaggio1.add(new IndexTableName("6/1-6/11:11"));
			array_viaggio1.add(new IndexTableName("7/1-7/11:11"));
			
			array_viaggio2.add(new IndexTableName("1/1-2/8:11"));
			array_viaggio2.add(new IndexTableName("4/7-8/19:30"));
			array_viaggio2.add(new IndexTableName("6/7-71/5:10"));
			
			array_viaggio3.add(new IndexTableName("3/6-8/13:30"));
			array_viaggio3.add(new IndexTableName("4/7-8/13:30"));
			array_viaggio3.add(new IndexTableName("5/8-9/13:30"));
			array_viaggio3.add(new IndexTableName("6/9-10/13:30"));
			
			array_viaggio4.add(new IndexTableName("2/15-31/16:10"));
			array_viaggio4.add(new IndexTableName("2/23-28/19:10"));
			array_viaggio4.add(new IndexTableName("3/85-87/21:40"));
			
			array_viaggio5.add(new IndexTableName("7/5-7/11:24"));
			array_viaggio5.add(new IndexTableName("3/2-7/18:42"));
			
			array_viaggio6.add(new IndexTableName("1/8-17/17:50"));
			
			array_viaggio7.add(new IndexTableName("5/20-40/16:15"));
			
			array_viaggio8.add(new IndexTableName("6/9-10/13:30"));
			array_viaggio8.add(new IndexTableName("3/85-87/21:40"));
			
			array_viaggio9.add(new IndexTableName("5/20-40/16:15"));
			array_viaggio9.add(new IndexTableName("3/1-3/11:11"));
			array_viaggio9.add(new IndexTableName("5/8-9/13:30"));
			
			array_viaggio10.add(new IndexTableName("5/20-40/16:15"));
			array_viaggio10.add(new IndexTableName("4/7-8/19:30"));
			
			array_viaggio11.add(new IndexTableName("5/20-40/16:15"));
			array_viaggio11.add(new IndexTableName("1/1-2/8:11"));
			
			array_viaggio12.add(new IndexTableName("6/21-40/16:15"));
			array_viaggio12.add(new IndexTableName("7/16-19/23:00"));
			array_viaggio12.add(new IndexTableName("7/6-9/13:30"));
			
			array_viaggio13.add(new IndexTableName("7/6-9/13:30"));
			array_viaggio13.add(new IndexTableName("5/16-99/10:30"));
			array_viaggio13.add(new IndexTableName("4/6-9/13:30"));
			
			array_viaggio14.add(new IndexTableName("5/6-19/13:30"));
			array_viaggio14.add(new IndexTableName("6/6-19/23:30"));
			
			array_viaggio15.add(new IndexTableName("7/16-19/23:00"));
			array_viaggio15.add(new IndexTableName("6/6-9/22:30"));
			
			array_viaggio16.add(new IndexTableName("5/80-90/8:30"));
			array_viaggio16.add(new IndexTableName("6/80-92/18:30"));
			
			array_viaggio17.add(new IndexTableName("6/65-87/18:30"));
			array_viaggio16.add(new IndexTableName("6/65-82/23:30"));
			
			array_viaggio18.add(new IndexTableName("7/16-19/23:00"));
			array_viaggio18.add(new IndexTableName("3/13-19/10:30"));
			array_viaggio18.add(new IndexTableName("3/13-19/18:30"));
			
			array_viaggio19.add(new IndexTableName("2/78-80/16:00"));
			array_viaggio19.add(new IndexTableName("5/20-40/16:15"));
			array_viaggio19.add(new IndexTableName("3/1-3/11:11"));
			
			array_viaggio20.add(new IndexTableName("1/67-89/13:30"));
			array_viaggio20.add(new IndexTableName("2/66-89/18:30"));
			
			
			
			
			
			
			
		} catch (NumberFormatException e) {
			System.out.println("error");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("error2");
			e.printStackTrace();
		}
		//caricamento dati nel file
		xl.addTravelerInFile("Daniele",  "Morittu",   "MRTDNL85B19B254V", "info_1_dan", "sdfsd",array_viaggio3);
		xl.addTravelerInFile("Emanuele", "Paracone",  "MNLPCN48A01H501C", "info_2_ema", "mnbvcx", array_viaggio1);
		xl.addTravelerInFile("Stefano",  "Olivieri",  "STFLVR85S12H501O", "info_3_ste", "rtyugd", array_viaggio4);
		xl.addTravelerInFile("Mario",    "Rossi",     "RSSMRA76A01A794N", "info_4_mario", "seemar", array_viaggio2);
		xl.addTravelerInFile("Sergio",   "Bianchi",   "BNCSRG73L01G482Y", "info_5_sergio", "pwhser", array_viaggio2);
		xl.addTravelerInFile("Otello",   "Rinaldi",   "RNLTLL40L01H501O", "info_6_otello", "pwhotel", array_viaggio2);
		xl.addTravelerInFile("Leslie",   "Lamport",   "LMPLSL41B07H501W", "info_7_leslie", "pwhlesl", array_viaggio1);
		xl.addTravelerInFile("Eddard",   "Stark",     "STRDRD70E17A345R", "info_8_eddard", "pwhedd", array_viaggio4);
		xl.addTravelerInFile("Charlize", "Theron",    "THRCRL75C48Z347X", "info_9_charlize", "pwhct", array_viaggio3);
		xl.addTravelerInFile("Homer",    "Simpson",   "SMPHMR65C14Z404R", "info_10_homer", "pwhhomer", array_viaggio1);
		xl.addTravelerInFile("Giuseppe", "Garibaldi", "GRBGPP00B04B354K", "info_11_guiseppe", "pwhgiu", array_viaggio2);
		xl.addTravelerInFile("Miroslav", "Klose",     "MRSKLS78H01H501L", "info_12_mirogol", "pwhmiro", array_viaggio4);
		xl.addTravelerInFile("Jon",      "Snow",      "SNWJNO85S13A345C", "info_13_jon", "pwhjon", array_viaggio4);
		xl.addTravelerInFile("Barak",    "Obama",     "BMOBRK62C20H501J", "info_14_barak", "pwhbarak", array_viaggio4);
		xl.addTravelerInFile("Luke",     "Skywalker", "SKYLKU65C18D704X", "info_15_luke", "pwhskyj", array_viaggio2);

		
		xl.addTravelerInFile("Giovanni", "Cantone",	  "CNTGNN50L29H703M", "info_16_giovcant", "pwhstatic", array_viaggio5);
		xl.addTravelerInFile("Antonio",  "Papa",      "PPANTN82P15B963C", "info_17_papat", "pwhanto", array_viaggio6);
		xl.addTravelerInFile("Giovanni", "Bernabei",  "BRNGNN86T27H501Y", "info_18_jojo", "pwhjojo", array_viaggio7);
		xl.addTravelerInFile("Alessandra","Cocozza",  "CCZLSN87C48H501K", "info_19_ale", "pwhalcoco", array_viaggio7);
		xl.addTravelerInFile("Francesca", "Ornitoringo", "FRNRTR88M56L840Q", "info_19_orni", "pwhrinco", array_viaggio6);
		xl.addTravelerInFile("Antonio",  "Elefante",  "LFNNTN80H20L424J", "info_20_ant", "pwhrelef", array_viaggio2);
		xl.addTravelerInFile("Daniel",   "Bovet",     "BVTDLP30C19Z110O", "info_21_dpb", "pwhbovt", array_viaggio7);
		xl.addTravelerInFile("Giggi",    "Buffone",   "BFFGGG74R26D193G", "info_22_gggg", "pwhbuffo", array_viaggio5);
		xl.addTravelerInFile("Antonella","Antonelli", "NTNNNL75C47A662V", "info_23_antant", "pwhant", array_viaggio5);
		
		xl.addTravelerInFile("Mario","Neri", "MRANRE50E01A001G", "info_24_mar", "pwhaneri", array_viaggio5);
		xl.addTravelerInFile("Francesca","Casa", "FRNCSA88A41Z152B", "info_25_fraca", "pwhcasa", array_viaggio5);
		xl.addTravelerInFile("Alessandro","Milano", "LSSMLN79D01D969R", "info_26_alx", "pwhmilan", array_viaggio5);
		xl.addTravelerInFile("Alessia","Siena", "LSSSNI86P58F257I", "info_27_ale", "pwhsien", array_viaggio5);
		xl.addTravelerInFile("Giovanni","Roma", "GVNRMO80L08A662R", "info_28_giov", "pwhrom", array_viaggio5);
		xl.addTravelerInFile("Remo","Nave", "RMENVA65L11H391W", "info_29_rem", "pwhnave", array_viaggio5);
		xl.addTravelerInFile("Tommaso","Crocera", "TMMCCR60A01D643J", "info_30_tommy", "pwhcruise", array_viaggio5);
		
		xl.addTravelerInFile("Serena","Giallo", "SRNGLL90A41H501M", "info_31_sere", "pwhyell", array_viaggio6);
		xl.addTravelerInFile("Luciana","Luciani", "LCNLCN89C58C820G", "info_32_lucia", "pwhluci", array_viaggio9);
		xl.addTravelerInFile("Alberto","Petto", "LBRPTT55P18D488L", "info_33_petto", "pwhalby", array_viaggio10);
		xl.addTravelerInFile("Fabio","Maria", "FBAMRA84S30F419G", "info_34_fab", "pwhmar", array_viaggio15);
		xl.addTravelerInFile("Simona","Corradi", "SMNCRD84P68D773M", "info_35_simo", "pwhcorr", array_viaggio15);
		xl.addTravelerInFile("Ilaria","Aria", "LRIRAI89A41A132D", "info_36_ila", "pwharia", array_viaggio18);
		xl.addTravelerInFile("Giulia","Francia", "GLIFNC90E45F074L", "info_37_giu", "pwhfrancia", array_viaggio20);
		xl.addTravelerInFile("Elisa","Pera", "LSEPRE85R51F839N", "info_38_eli", "pwhper", array_viaggio19);
		xl.addTravelerInFile("Ninna","Nanna", "NNNNNN87T45D612Y", "info_39_ninna", "pwhnnaoh", array_viaggio17);
		xl.addTravelerInFile("Piero","Pierino", "PRIPRN80D08B354M", "info_40_pier", "pwhpier", array_viaggio16);
		xl.addTravelerInFile("Mafalda","Falda", "MFLFLD75E48H501J", "info_41_maf", "pwhfalda", array_viaggio12);
		xl.addTravelerInFile("Marco","Forte", "MRCFRT87H05D773G", "info_42_marc", "pwhfort", array_viaggio11);
		xl.addTravelerInFile("Franco","Forte", "FRTFNC80A01D559D", "info_43_frank", "pwhforte", array_viaggio14);
		xl.addTravelerInFile("Paris","Vaffa", "VFFPRS80R41D612U", "info_44_paris", "pwhparv", array_viaggio16);
		xl.addTravelerInFile("Allegra","Triste", "TRSLGR85H48H501O", "info_45_alltr", "pwhtrtr", array_viaggio19);
		xl.addTravelerInFile("Chuck","Norris", "NRRCCK56E05Z404Q", "info_46_chuck", "pwhnorris", array_viaggio20);
		xl.addTravelerInFile("Marianna","Torino", "MRNTRN87D44D969K", "info_47_mar", "pwhtorm", array_viaggio19);
		xl.addTravelerInFile("Michele","Giordano", "GRDMHL72B17F205J", "info_48_mj", "pwhmic", array_viaggio18);
		xl.addTravelerInFile("Valentina","Napoli", "NPLVNT91A41G813D", "info_49_valn", "pwhnnovale", array_viaggio16);
		xl.addTravelerInFile("Silvia","Rosso", "RSSSLV81A41G478R", "info_50_silv", "pwhred", array_viaggio20);
		
		
		
		System.out.println("Lettura dati............");
		//lettura dei dati 
	    ArrayList<InfoTrav> it = xl.readTraveler();
		
	    for(int i=0; i<it.size(); i++)
			{
			   System.out.println(it.get(i).getT().signature());
			   
			   for(int j=0; j< it.get(i).getAlitn().size(); j++) 
				   	System.out.println(it.get(i).getAlitn().get(j).toString());
				   
			}

		
		
		
		
		
	}

}

