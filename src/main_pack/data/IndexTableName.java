package main_pack.data;

import java.io.Serializable;
import java.util.StringTokenizer;

import main_pack.data.avl.string_key.AVLStringKeyNode;

/**
 * 
 * Classe di definizione della singola Index Table. Ogni singola Index Table e' descritta in termini di:
 * <p> giorno della settimana in cui e' percorsa una certa tratta;
 * <p> tratta percorsa;
 * <p> orario di percorrenza della tratta.
 *
 */
public class IndexTableName implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private short weekday;
	private Route route;
	private Time time;
	
	
	public IndexTableName(String name) throws NumberFormatException, Exception
	{
		parse(name);
	}
	
	/**
	 * Parsing della stringa in input per il riconoscimento dei campi (weekday,route,time) utili all'inizializzazione di IndexTableName 
	 * @param name
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	private void parse(String name) throws NumberFormatException, Exception
	{
		//Es: WeekDay(short)/Route(int-int)/Time(Short:Short)
		
		StringTokenizer parser = new StringTokenizer(name, "/");
		
		String[] parameters = new String[3];
		int i=0;
		
		while(parser.hasMoreTokens())
		{

			weekday = Short.parseShort(parser.nextToken());
			
			route = new Route();
			
			StringTokenizer routeParser = new StringTokenizer(parser.nextToken(), "-");
			int start = Integer.parseInt(routeParser.nextToken());
			
			route.setStart(new Place(start));
			int arrival = Integer.parseInt(routeParser.nextToken());
			
			route.setArrival(new Place(arrival));
			
			StringTokenizer timeParser = new StringTokenizer(parser.nextToken(), ":");
			
			short ore = Short.parseShort((timeParser.nextToken()));
			
			time = new Time();
			
			time.setOre(ore);
			
			short minuti = Short.parseShort((timeParser.nextToken())); 
			
			time.setMinuti(minuti);
		}
			
		
	}
	
	/**
	 * Restituisce il giorno della settimana in cui e' compiuto il viaggio
	 * @return {@link Weekday}
	 */
	public short getWeekday() {
		return weekday;
	}

	/**
	 * Restituisce la tratta del viaggio
	 * @return {@link Route}
	 */
	public Route getRoute() {
		return route;
	}

	/**
	 * Restituisce l'orario in cui e' compiuto il viaggio
	 * @return {@link Time}
	 */
	public Time getTime() {
		return time;
	}
	
	/**
	 * Restituisce la signature di {@link IndexTableName}
	 */
	public String toString()
	{
		return weekday+" "+route.getStart().getPlace()+"-"+route.getArrival().getPlace()+" "+time.getOre()+":"+time.getMinuti();
	}
	
	public String toFormattedString()
	{
		return weekday+"/"+route.getStart().getPlace()+"-"+route.getArrival().getPlace()+"/"+time.getOre()+":"+time.getMinuti();
	}
	

	public static void main(String[] args) throws NumberFormatException, Exception {
		
		IndexTableName itn = new IndexTableName("1/1-2/11:11");
		
		System.out.println(itn.toString());
		System.out.println(itn.toFormattedString());
		
	}

	
	
}
