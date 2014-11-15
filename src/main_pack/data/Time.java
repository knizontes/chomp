package main_pack.data;

import java.io.Serializable;

/**
 * 
 * Classe descrittiva dell'orario di viaggio nel formato hh:mm (ore:minuti).
 *
 */
public class Time implements Serializable{

	private static final long serialVersionUID = 1L;
	private char ore;
	private char minuti;
	
	/**
	 * Restituisce il valore dell'ora in cui si compie la tratta
	 * @return ore
	 */
	public short getOre() {
		return (short)ore;
	}
	
	/**
	 * Inizializza il valore dell'ora in cui si compie la tratta. Sono ammessi valori interi x: 0 < x < 23 !
	 * @param ore
	 * @throws Exception
	 */
	public void setOre(int ore) throws Exception {
		if(ore > 23 || ore < 0)
			throw new Exception("Ora:"+(short)ore+" non valida!");
		this.ore = (char)ore;
	}
	
	/**
	 * Restituisce il valore dei minuti in cui si compie la tratta
	 * @return minuti
	 */
	public short getMinuti() {
		return (short)minuti;
	}
	
	/**
	 * Inizializza il valore dei minuti in cui si compie la tratta. Sono ammessi valori interi x: 0 < x < 59 !
	 * @param minuti
	 * @throws Exception
	 */
	public void setMinuti(int minuti) throws Exception {
		if(minuti > 59 || minuti < 0)
			throw new Exception("Minuti:"+(short)minuti+" non validi!");
		this.minuti = (char)minuti;
	}
	
	public static void main(String[] args) throws Exception {
		Time orario = new Time();
		
		orario.setOre(11);
		
		System.out.println(orario.getOre());
		
		orario.setMinuti(-23);
		
		System.out.println(orario.getMinuti());
		
	}
	
	
}
