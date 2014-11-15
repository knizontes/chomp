package main_pack.data;

import java.io.Serializable;

/**
 * 
 * Identificativo numerico del punto di partenza/arrivo per la tratta
 *
 */
public class Place implements Serializable{

	private static final long serialVersionUID = 1L;
	private int place;
	public static final int MAX_PLACES_NUM=100;
	
	public Place(int place) throws Exception
	{
		setPlace(place);
	}

	/**
	 * Restituisce l'identificativo numerico associato al punto geografico di riferimento 
	 * @return int
	 */
	public int getPlace() {
		return place;
	}
	
	
	/**
	 * Inizializza il campo place con il valore numerico passato come parametro. Sono ammessi solo valori interi x: 0 < x < 100 ! 
	 * @param place
	 * @throws Exception
	 */
	private void setPlace(int place) throws Exception {
		if(place > MAX_PLACES_NUM || place < 0)
			throw new Exception(place+" is not a valid place!");
		this.place = place;
	}
	
	
	
}