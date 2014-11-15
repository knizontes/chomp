package main_pack.chomp_engine.SST;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class testRAF {
	
	private RandomAccessFile raf;
	
	public testRAF(String filename) throws FileNotFoundException
	{
		raf = new RandomAccessFile(filename, "rw");
	}

	public static void main(String[] args) throws IOException {
		
		//Funzione truncate(nuova dimensione file) di FileChannel
		
		String filename = "raf.txt";
		testRAF traf = new testRAF(filename);
		
		System.out.println(traf.raf.length());
		
		traf.raf.writeChar('c');
		
		System.out.println(traf.raf.length());
		
//		traf.raf.seek(0);
//		
//		traf.raf.writeChars("");
		
		traf.raf.setLength(0);
		
		System.out.println(traf.raf.length());
		
	}
}
