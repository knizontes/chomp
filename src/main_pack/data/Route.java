package main_pack.data;

import java.io.Serializable;

/**
 * 
 * Classe rappresentativa della tratta percorsa in termini di punto di partenza (start) e punto di destinazione (arrival).
 * 
 */
public class Route implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Place start;
	private Place arrival;
	
	public Route()
	{
		
	}
	
	public Route(Place start, Place arrival)
	{
		this.start = start;
		this.arrival = arrival;
	}
	
	/**
	 * Riordina la stringa identificativa della tratta anteponendo al posto di destinazione il posto di partenza
	 * @return String: partenza-destinazione
	 */
	public String toOrderedString(){
		if (start.getPlace()>=arrival.getPlace())
			return new String (start.getPlace()+"-"+arrival.getPlace());
		return new String(arrival.getPlace()+"-"+start.getPlace());
	}

	/**
	 * Restituisce il valore del punto di partenza (start) della tratta
	 * @return {@link Place}
	 */
	public Place getStart() {
		return start;
	}

	/**
	 * Inizializza il valore di partenza (start) della tratta al valore passato come parametro 
	 * @param start
	 */
	public void setStart(Place start) {
		this.start = start;
	}
	
	/**
	 * Restituisce il valore del punto di arrivo (arrival) della tratta
	 * @return arrival
	 */
	public Place getArrival() {
		return arrival;
	}

	/**
	 * Inizializza il valore di destinazione (arrival) della tratta al valore passato come parametro 
	 * @param arrival
	 */
	public void setArrival(Place arrival) {
		this.arrival = arrival;
	}
	
	

}