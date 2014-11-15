package main_pack.chomp_engine.SST;

/*
 * WARNING:
 * 	1.Se file molto grande valutare di assegnare il tipo long all'offset!  
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;

import main_pack.data.Traveler;

public class DataIO {
	
	private ArrayList<Integer> hole_list = new ArrayList<Integer>();//lista degli indici dei "buchi" sulla SSTable
	private final int size;//dimensione record
	private RandomAccessFile raf;
	private int record_counter = 0;//contatore record registrati
	
public DataIO(int size, String filename) throws FileNotFoundException
	{
		this.size = size;
		raf = new RandomAccessFile(filename, "rw");
	}
	

/**
 * Scrittura della stringa passata come parametro nel field opportuno specificato in termini di dimensione e spiazzamento all'interno del record
 * @param s
 * @param fieldSize
 * @param fieldOffset
 * @throws Exception
 */
public void writeFixedString(String s, int fieldSize, int fieldOffset) throws Exception{
		
		if(s.length() > fieldSize)//se il campo che si vuole rempire è maggiore della sua dimensione
		{
			throw new Exception("Field oversized!");
		}
		
		long offset;
		
		if(hole_list.size() > 0)//lista dei record eliminati non nulla
			{
			offset = hole_list.get(0)*size+fieldOffset*2;//inzializza offset alla posizione del campo su cui scrivere
//			System.out.println((fieldOffset*2+fieldSize*2)+"--"+(size*2));
			if ((fieldOffset*2+fieldSize*2)==(size))//se stiamo scrivendo sull'ultimo campo 
				hole_list.remove(0);//rimuovi primo elemento della lista dei "buchi".Primo record vuoto riempito!
			}
		else 
			{
			offset = raf.length();
			if (fieldOffset==0)
				record_counter++;
			}
		
		raf.seek(offset);
		
		int i;
		for (i = 0; i < fieldSize; i++) {
			char ch = 0;
			if(i < s.length())
			{
				ch = s.charAt(i);
				//System.out.println("[TEST] writeFixedString:"+ch);
			}
			raf.writeChar(ch);
		}
		
		
	}

/**
 * Aggiornamento di uno specifico campo del record specificato attraverso l'indice fornito come parametro
 * @param index
 * @param s
 * @param fieldSize
 * @param fieldOffset
 * @throws Exception
 */
public void updateFixedString(int index,String s, int fieldSize, int fieldOffset) throws Exception{
	
	if(s.length() > fieldSize)//se il campo che si vuole rempire è maggiore della sua dimensione
	{
		throw new Exception("Field oversized!");
	}
	
	raf.seek(index*size+ fieldOffset*2);//Mi posiziono sul record da modificare
	
	int i;
	for (i = 0; i < fieldSize; i++) {
		char ch = 0;
		if(i < s.length())
		{
			ch = s.charAt(i);
			//System.out.println("[TEST] writeFixedString:"+ch);
		}
		raf.writeChar(ch);
	}
	
	
}

/**
 * Lettura campo del record specificato tramite l'indice passato come parametro
 * @param index
 * @param fieldSize
 * @param fieldOffset
 * @return String
 * @throws Exception
 */
public String readFixedString(int index, int fieldSize, int fieldOffset) throws Exception{
		
		long offset = index*size+ fieldOffset*2;
		
		if(index < 0 || offset > raf.length())
			throw new Exception("Index Out of SSTable Bound!\noffset:"+offset+" raf length:"+raf.length());
		
		StringBuffer b = new StringBuffer(fieldSize);

		int i = 0;
		boolean more = true;
		
		raf.seek(offset);
		
		while ( more && i < fieldSize)
		{
			char ch = raf.readChar();
			//System.out.println("[TEST]"+ch);
			i++;
			if (ch == 0)
				more = false;
			else b.append(ch);
		}
		
		return b.toString();
		
	}
	

/**
 * Lettura record specificato dall'index passato come parametro
 * @param index
 * @return String
 * @throws Exception
 */
public String readRecord(int index) throws Exception{
	
	long offset = index*size;
	if(index < 0 || offset > raf.length())
		throw new Exception("Index Out of SSTable Bound!");
	StringBuffer b = new StringBuffer(size);

	int i = 0;
	
	raf.seek(offset);
	
	System.out.println("Size: "+size);
	
	while (i < size/2) //WARNING! Anche qui size/2 perchè devo contare i caratteri e NON il numero di bytes (NB: 1 char = 2 Bytes)
	{
		//WARNING!Ci possono essere dei campi non totalmente riempiti!Ad esempio la dimensione del campo è 20 ed il valore inserito è lungo 7 (|Daniele             |)!
		
		char ch = raf.readChar();
		
		i++;
		
		if (ch == 0);//Se il carattere che stai vedendo è nullo non appenderlo alla stringa!
		else b.append(ch);
		
	}
	
	return b.toString();
	
}

/**
 * Cancella il record specificato tramite l'indice passato come parametro del metodo
 * @param index
 * @throws Exception
 */
public void deleteFixedString(int index) throws Exception{
	
		//System.out.println("Cancellazione record #"+index+"...");
	
		long offset = index*size;
	
		if(index < 0 || offset > raf.length())
			throw new Exception("Index Out of SSTable Bound!");
		
		raf.seek(offset);
		
		int i;
		for (i = 0; i < size/2; i++) { //WARNING! size/2 perchè si contano i caratteri e NON i bytes!!!
			char ch = 0;
			raf.writeChar(ch);
		}
		
		//if(index < getRecord_counter() - 1)//se il record corrente NON è l'ultimo dell'SSTable... (SE fosse l'ultimo record settiamo tutti i caratteri a '\0' ma dobbiamo escluderli dal computo della lunghezza del file
			hole_list.add(index);
	}


/**
 * Procedura di compattamento per ridurre la dimensione del file in seguito alla comparsa di holes
 * @throws IOException
 * @throws Exception
 */
public void compact() throws IOException, Exception
{
	
	System.out.println("[SERVER] Procedura di compattamento avviata...");
	
	//WARNING! Valutare correttezza uso di getRecordCounter() per indivuazione spiazzamento ultimo record (Meglio un raf.length()-size che restituisca l'indice dell'ultimo record come differenza tra lunghezza del file e dimensione record? )
	//Controllare condizione di guardia del while qui di seguito tenendo in considerazione che i record cancellati in coda al file NON devono essere "visti" come buchi!!
	//Quando cancelliamo l'ultimo record dell'SSTable dobbiamo fare attenzione ai caratteri di terminazione '\0' che comunque vengono considerati dalla raf.length. In realtà noi dobbiamo considerare come primo byte utile in coda al file quello corrispondente al primo carattere '\0' e NON raf.length!!!
	
	int holes = hole_list.size();
	
		while(hole_list.size() > 0)
		{
			
			//System.out.println("#Holes = "+hole_list.size());
			
		//Riempimento con un record (dimensione fissa) uno dei buchi interni al file
		
		long offsetHole = hole_list.get(0)*size; //inizializzo l'offset del buco da riempire
		
		int index = getRecord_counter()-1;//WARNING!Devo decrementare il contatore del record ad ogni iterazione altrimenti agisco sempre sullo stesso indice
		
		if(index < 0 || offsetHole > raf.length())
			throw new Exception("Index Out of SSTable Bound!");
		
		String toTransfer = readRecord(index);//Copio stringa da trasferire da una zona del file all'altra (Variabile d'appoggio)
		
		//Trasferimento record da coda del file ad un buco presente in precedenza
		
		raf.seek(offsetHole);//Posizionamento all'inizio del record da trasferire
		 
		raf.writeChars(toTransfer);//Scrivo il record nel primo buco utile 
		
		int removed_index = hole_list.remove(0);//Rimuovo il primo indice dei buchi nella lista
		
		deleteFixedString(index);//Cancello record appena trasferito
		
		System.out.println("Spostamento record #"+index+" appena cancellato in posizione "+removed_index+"...");
		
//		record_counter--;//Decremento contatore dei record
		
		}
		
		System.out.println("Lunghezza del file: "+raf.length()+" Size: "+size);//TEST
		
		System.out.println("raf.length() - size: "+(raf.length() - size)+" Index:"+(getRecord_counter()-1));//TEST
		
		//if(raf.length() - size == (getRecord_counter()-1)*size)//se sei l'ultimo record in coda
			raf.setLength(raf.length()-holes*size);//Ridimensiono il file poichè sto cancellando l'ultimo record in coda
		
}

//WARNING! Controllare uso di questo metodo all'interno del codice. Sostituire con getRecordCounter()...più corretto! 
public int getRecord_counter() {
	return record_counter;
}

//WARNING! Valutare correttezza di questo metodo in alternativa al precedente!
//Restituisce il numero di record effettivi nell'SSTable
public long getRecordCounter() throws IOException
{
	return (raf.length()-hole_list.size()*size)/size;
}

public int getLastIndex() throws IOException
{
	return (int)raf.length()/size;
}

/**
 * Restituisce la lunghezza del file
 * @throws IOException
 */
public void getFileLength() throws IOException
{
	System.out.println("Lunghezza file:"+raf.length());
}

/**
 * Stampa i record
 * @throws IOException
 * @throws Exception
 */
public void printRecords() throws IOException, Exception
{
	System.out.println("--------------------------");
	for(int i=0;i < getLastIndex();i++){	
		System.out.print(readFixedString(i,Traveler.CF_SIZE,Traveler.CF_OFFSET));
		System.out.print(" ");
		System.out.print(readFixedString(i,Traveler.FIELD_SIZE,Traveler.NAME_OFFSET));
		System.out.print(" ");
		System.out.println(readFixedString(i,Traveler.FIELD_SIZE,Traveler.SURNAME_OFFSET));
	}
	System.out.println("--------------------------");
}

//WARNING! NON stiamo inserendo la close per il RandomAccessFile perchè vogliamo mantenerlo sempre aperto per read/write

	public static void main(String[] args) {
		
		try {
			DataIO io = new DataIO(Traveler.RECORD_SIZE, "dataIO.txt");
			
			io.writeFixedString("MRTDNL85B19B354V", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Daniele", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Morittu", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.writeFixedString("PRCMNL87F23C123G", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Emanuele", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Paracone", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.writeFixedString("LVRSTF85V10N432H", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Stefano", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Olivieri", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.writeFixedString("RSSMRC88I23D321F", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Marco", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Rossi", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.writeFixedString("BNCGRG81C12C444E", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Giorgio", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Bianchi", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.printRecords();
			
			io.deleteFixedString(3);
			
			io.printRecords();

			io.deleteFixedString(1);
			
			io.printRecords();			
			
			io.getFileLength();
			
			//WARNING! Problema: vengono cancellati anche record da mantenere (vedi 2,4). Inoltre vengono stampe differenti a seconda che si usi readFixedString o readRecord!
			
			io.printRecords();
			
			io.deleteFixedString(0);
			
			io.deleteFixedString(2);
			
			//System.in.read();
			
			//io.compact();
			
			io.getFileLength();
			
			io.printRecords();
			
			System.out.println("---- Riscrivo dei record cancellati precedentemente ----");
			
			io.writeFixedString("MRTDNL85B19B354V", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Daniele", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Morittu", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.writeFixedString("PRCMNL87F23C123G", Traveler.CF_SIZE, Traveler.CF_OFFSET);
			io.writeFixedString("Emanuele", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			io.writeFixedString("Paracone", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			
			io.printRecords();
			
			System.out.println("---- Sovrascrivo alcuni campi dei record ----");
			
			io.updateFixedString(1, "Morittu", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			io.updateFixedString(3, "Paracone", Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET);
			io.updateFixedString(4, "Bruno", Traveler.FIELD_SIZE, Traveler.NAME_OFFSET);
			
			io.printRecords();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
