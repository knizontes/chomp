package main_pack.data;

import java.io.FileNotFoundException;
import java.io.IOException;

import main_pack.chomp_engine.SST.DataIO;

public class Test {
	
	public static void main(String[] args) throws IOException, Exception {
		
		DataIO io = new DataIO(Traveler.RECORD_SIZE, "sst.dat");

		System.out.println(io.readFixedString(0, Traveler.CF_SIZE, Traveler.CF_OFFSET));
		System.out.println(io.readFixedString(0, Traveler.FIELD_SIZE, Traveler.NAME_OFFSET));
		System.out.println(io.readFixedString(0, Traveler.FIELD_SIZE, Traveler.SURNAME_OFFSET));
		
		System.out.println();
		
		System.out.println("readRecord...");
		
		System.out.println(io.readRecord(0));
		
	}

}
