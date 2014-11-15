package main_pack.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import main_pack.chomp_engine.SST.DataIO;

/**
 * Classe rappresentativa del dato strutturato Traveler (viaggiatore). Si compone dei parametri distintivi:
 * Codice Fiscale (identificativo univoco), nome, cognome, informazioni sul viaggiatore, password. 
 * Contiene i parametri di spiazzamento (FIELD_SIZE, CF_SIZE, ..., CF_OFFSET, NAME_OFFSET, ...) per la ricerca 
 * del singolo campo (field) all'interno del dato strutturato Traveler memorizzato all'interno dell'SSTable.
 *
 */
public class Traveler implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String nome;
	private String cognome;
	private String info;
	private String pwdHash;
	private String CF;
	
	public static final int FIELD_SIZE = 20;
	public static final int CF_SIZE = 16;
	public static final int INT_SIZE = 4;
	public static final int INFO_SIZE=160;
	public static final int PWD_HASH_SIZE=20;
	
	public static final int CF_OFFSET=0;
	public static final  int NAME_OFFSET=CF_SIZE;
	public static final int SURNAME_OFFSET=NAME_OFFSET+FIELD_SIZE;
	public static final int INFO_OFFSET=SURNAME_OFFSET+FIELD_SIZE;
	public static final int PWD_HASH_OFFSET=INFO_OFFSET+INFO_SIZE;

	public static final int RECORD_SIZE = 2*CF_SIZE + 2 * FIELD_SIZE * 2+INFO_SIZE*2+PWD_HASH_SIZE*2;//int + 4*char[20] Nota: 1 char = 2 Bytes
	
	public Traveler()
	{
		
	}
	
	public Traveler(String CF, String nome, String cognome, String info, String pwdHash) {
		
		this.CF = CF;
		
		this.nome = nome;
		this.cognome = cognome;
		this.info=info;
		this.pwdHash=pwdHash;
	}
	
	
	/**
	 * Restituisce il nome del {@link Traveler}
	 * @return nome
	 */
	public String getNome() {
		return nome;
	}
	
	/**
	 * Inizializza il nome del {@link Traveler} al valore passato come parametro 
	 * @param nome
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Restituisce il cognome del Traveler
	 * @return cognome
	 */
	public String getCognome() {
		return cognome;
	}

	/**
	 * Inizializza il campo cognome del {@link Traveler} al valore passato come parametro
	 * @param cognome
	 */
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	
	/**
	 * Restituisce le informazioni associate al {@link Traveler}
	 * @return info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Inizializza il campo info di {@link Traveler} contenente le informazioni associate al viaggiatore
	 * @param info
	 */
	public void setInfo(String info) {
		this.info = info;
	}
	
	/**
	 * Restituisce l'hash della password d'accesso associata al {@link Traveler}
	 * @return String
	 */
	public String getPwdHash() {
		return pwdHash;
	}
	
	/**
	 * Inizializza il campo della password d'accesso associata al {@link Traveler}
	 * @param pwdHash
	 */
	public void setPwdHash(String pwdHash) {
		this.pwdHash = pwdHash;
	}
	
	/**
	 * Restituisce il Codice Fiscale (CF) del {@link Traveler}. Rappresenta l'identificativo univoco di ogni Traveler!
	 * @return String
	 */
	public String getCF() {
		return CF;
	}

	/**
	 * Inizializza il Codice Fiscale (CF) del {@link Traveler} al valore passato come parametro.
	 * @param CF
	 */
	public void setCF(String CF) {
		this.CF = CF;
	}
	
	/**
	 * Stampa i campi del {@link Traveler}
	 */
	public void print() {
		System.out.println(CF+": "+nome+" "+cognome+" info:"+info);
	}

	/**
	 * Stampa i campi del {@link Traveler} ivi compresi i campi relativi a dati sensibili
	 */
	public void privatePrint(){
		System.out.println(CF+": "+nome+" "+cognome+" info:"+info+ " hash:"+pwdHash);
	}
	
	/**
	 * Signature del {@link Traveler} che desume il sesso del viaggiatore dal formato del codice fiscale
	 * @return String
	 */
	public String signature()
	{
		String sex;
		if (Integer.parseInt(CF.substring(9, 11))>31)
			sex=new String ("Donna");
		else sex=new String ("Uomo");
		return this.CF+":"+this.nome+" "+this.cognome+ " "+sex+" info:"+info;
	}
	
	/**
	 * Signature del {@link Traveler} che desume il sesso del viaggiatore dal formato del codice fiscale e comprende la stampa dei dati sensibili
	 * @return String
	 */
	public String privateSignature()
	{
		String sex;
		if (Integer.parseInt(CF.substring(9, 11))>31)
			sex=new String ("Donna");
		else sex=new String ("Uomo");
		return this.CF+":"+this.nome+" "+this.cognome+ " "+sex+" info:"+info+ " hash:"+pwdHash;
	}
	
	  public static void main(String[] args) {
		    Traveler[] travelers = new Traveler[2];

		    travelers[0] = new Traveler("MRTDNL85B19B354V","Daniele","Morittu","sono simpatico","myhash");
		    //travelers[1] = new Viaggiatore(2,"Stefano","Olivieri",2,3);
		    travelers[1] = new Traveler("PRCMNL87E03L219S","Emanuele","Paracone","sono simpatico anch'io","myhash");
		    try {
		      DataOutputStream out = new DataOutputStream(new FileOutputStream("travelers.txt"));
		      for (int i = 0; i < travelers.length; i++)
//		        travelers[i].writeData(out);//WARNING!Commentata per evitare errori!!!Sbloccare per testing
		      out.close();
		    } catch (IOException e) {
		      System.out.print("Error: " + e);
		      System.exit(1);
		    }

		    try {
		      RandomAccessFile in = new RandomAccessFile("travelers.txt", "r");
		      int count = (int) (in.length() / Traveler.RECORD_SIZE);
		      System.out.println(in.length()+"-"+Traveler.RECORD_SIZE);
		      System.out.println(count+ " records founded!");
		      Traveler[] newTraveler = new Traveler[count];

		      for (int i = count - 1; i >= 0; i--) {
		        newTraveler[i] = new Traveler();
		        in.seek(i * Traveler.RECORD_SIZE);
//		        newTraveler[i].readData(in);//WARNING!Commentata per evitare errori!!!Sbloccare per testing
		      }
		      for (int i = 0; i < newTraveler.length; i++)
		        newTraveler[i].print();
		    } catch (IOException e) {
		      System.out.print("Error: " + e);
		      System.exit(1);
		    }
		  }
	
}
