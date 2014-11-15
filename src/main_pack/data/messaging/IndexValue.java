package main_pack.data.messaging;

import java.io.Serializable;
import java.util.ArrayList;

import main_pack.data.IndexTable;
import main_pack.data.Traveler;

/**
 * 
 * Classe rappresentativa del messaggio di scambio tra peer contenente il nome della {@link IndexTable} ed i {@link Traveler} 
 * di cui la {@link IndexTable} in esame e' competente identificati univocamente per mezzo del loro Codice Fiscale (CF).
 * Esprime l'associazione tra {@link IndexTable} <--> Insieme di CF (Codici Fiscali) identificativi dei {@link Traveler} associati
 * alla {@link IndexTable}
 *
 */
public class IndexValue implements Serializable
{
	private String nameIndexTable;
	private ArrayList<String> CFS;

	public IndexValue(String name )
	{
		this.nameIndexTable=name;
		CFS= new ArrayList<String>();
	}

	public String getNameIndexTable() {
		return nameIndexTable;
	}

	public void setNameIndexTable(String nameIndexTable) {
		this.nameIndexTable = nameIndexTable;
	}

	public ArrayList<String> getCFS() {
		return CFS;
	}

	public void setCFS(ArrayList<String> cFS) {
		CFS = cFS;
	}
	
	public String getCf(int index){
		return CFS.get(index);
	}
	
	public void addCF(String CF){
		CFS.add(CF);
	}
	
	public int size(){
		return CFS.size();
	}

    

}
