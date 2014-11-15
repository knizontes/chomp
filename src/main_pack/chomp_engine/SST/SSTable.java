package main_pack.chomp_engine.SST;

/*
 * SSTable Class
 * 
 * RECORD: [CF|Nome|Cognome]
 */


import java.io.FileNotFoundException;
import java.io.IOException;

import main_pack.data.Traveler;

/**
 * Rappresenta la classe che gestisce il file SStable per la memorizzazione su disco dei dati del sistema
 *
 */
public class SSTable {
	
	private String PATH = "SSTable.chomp";
	private long nextIndex = 0;
	private DataIO dataIo;
	
	public SSTable(String filename, int recordSize) 
	{
		
		try {
			dataIo = new DataIO(recordSize, filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	}

	/**
	 * Scrittura del record {@link Traveler} passato come parametro
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public long writeRecord(Traveler t) throws Exception{
//		DataOutputStream out = new DataOutputStream(file_out);
		dataIo.writeFixedString(t.getCF(), Traveler.CF_SIZE, Traveler.CF_OFFSET);
		dataIo.writeFixedString(t.getNome(), Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
		dataIo.writeFixedString(t.getCognome(), Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
		dataIo.writeFixedString(t.getInfo(),Traveler.INFO_SIZE,Traveler.INFO_OFFSET);
		dataIo.writeFixedString(t.getPwdHash(),Traveler.PWD_HASH_SIZE,Traveler.PWD_HASH_OFFSET);
//		out.flush();
//		out.close();
		
		nextIndex++;
		
		return nextIndex - 1;//Ritorna la posizione corrente dell'ultimo record registrato
	}
	/**
	 * Legge il Traveler usando l'indice di memorizzazione nel file 
	 * @param index
	 * @return Trveler corrispondente al record letto
	 * @throws Exception
	 */
	public Traveler readRecord(int index) throws Exception {
	
		//Istanziazione oggetto viaggiatore
		return new Traveler(dataIo.readFixedString(index, Traveler.CF_SIZE, Traveler.CF_OFFSET),
				dataIo.readFixedString(index, Traveler.FIELD_SIZE, Traveler.NAME_OFFSET), 
				dataIo.readFixedString(index, Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET),
				dataIo.readFixedString(index, Traveler.INFO_SIZE,Traveler.INFO_OFFSET),
				dataIo.readFixedString(index, Traveler.PWD_HASH_SIZE,Traveler.PWD_HASH_OFFSET));
	}
	
	/**
	 * Ritorna il numero di record nella SSTable
	 * @return
	 */
	public int getRecordCount()
	{
		return dataIo.getRecord_counter();
	}
	
	public long getNumRecFilled() throws IOException
	{
		return dataIo.getRecordCounter();
	}
	
	/**
	 * Aggiorna il record in posizione index 
	 * @param index
	 * @param s
	 * @param fieldSize
	 * @param fieldOffset
	 * @throws Exception
	 */
	public void updateRecord(int index,String s, int fieldSize, int fieldOffset) throws Exception
	{
		dataIo.updateFixedString(index, s, fieldSize, fieldOffset);
	}
	
	/**
	 *  Cancella il record in posizione index
	 * @param index
	 * @throws Exception
	 */
	public void deleteRecord(int index) throws Exception{
		dataIo.deleteFixedString(index);
	}
	
	public void flush()
	{
		
	}
	
	public DataIO getDataIO()
	{
		return dataIo;
	}

	public static void main(String[] args) throws Exception {
		
		SSTable editor = new SSTable("SST.txt",Traveler.RECORD_SIZE);
		
		editor.writeRecord(new Traveler("PRCMNL87E03L219S","Emanuele","Paracone","simpatico","myhash"));
		//editor.readRecord(0).print();
		
		editor.writeRecord(new Traveler("MRTDNL85B19B354V","Daniele","Morittu","simpatico","myhash"));
		//editor.readRecord(1).print();
		
		editor.writeRecord(new Traveler("MRTDNL85B19B354V","Stefano","Olivieri","simpatico","myhash"));
		//editor.readRecord(2).print();
		
		System.out.println("Records founded: "+editor.getRecordCount());
		
		for (int i=0; i<editor.getRecordCount();++i)
			editor.readRecord(i).privatePrint();
		
		System.out.println();
		System.out.println("E se alla nascita avessero scambiato i vostri nomi?!");
		System.out.println();
		
		editor.updateRecord(0, "Stefano", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
		editor.updateRecord(2, "Emanuele", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
		
		for (int i=0; i<editor.getRecordCount();++i)
			editor.readRecord(i).privatePrint();
		
		System.out.println();
		System.out.println("Ma $oprattutto...$iamo $icuri che io non $ia il figlio di $teve Job$?!");
		System.out.println();
		
		editor.updateRecord(1, "Job$", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);		
		editor.updateRecord(1, "JBSDNL50E23L219$", Traveler.CF_SIZE, Traveler.CF_OFFSET);
		
		for (int i=0; i<editor.getRecordCount();++i)
			editor.readRecord(i).privatePrint();

		
		editor.deleteRecord(2);
		
		System.out.println("Records founded: "+editor.getNumRecFilled());
		
		for (int i=0; i<editor.getRecordCount();++i)
			editor.readRecord(i).privatePrint();
		
		editor.writeRecord(new Traveler("JBSDNL50E23L219$","Franco","Baresi","simpatico","myhash"));
		
		System.out.println("Records founded: "+editor.getNumRecFilled());
		
		for (int i=0; i<editor.getRecordCount();++i)
			editor.readRecord(i).privatePrint();
		
	}
	
}
