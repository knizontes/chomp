package main_pack.data;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * 
 * Elemento rappresentativo del giorno della settimana
 *
 */
public class Weekday implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static short LUN = 1;
	public static short MAR = 2;
	public static short MER = 3;
	public static short GIO = 4;
	public static short VEN = 5;
	public static short SAB = 6;
	public static short DOM = 7;
	
	private short day;

	/**
	 * Restituisce il giorno della settimana
	 * @return short
	 */
	public short getDay() {
		return day;
	}
	
	/**
	 * Inizializza il giorno della settimana al valore passato come parametro
	 * @param day
	 */
	public void setDay(short day) {
		this.day = day;
	}
	
	
	public static void main(String[] args) {
		
		Weekday w = new Weekday();
		
		StringTokenizer parser = new StringTokenizer("2/","/");
		
		while(parser.hasMoreTokens())
			{
				short wow = Short.parseShort(parser.nextToken());
				w.setDay((short)(wow));
			}
		
		
		w.setDay((short)2);
		
		System.out.println(w.getDay());
		
	}
	
}
