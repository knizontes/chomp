package main_pack.data.messaging;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Classe di accorpamento in un ArrayList di piu' messaggi IndexValue per lo scambio di conoscenze sulle Index Table da parte
 * dei peer coinvolti nella comunicazione.
 *
 */
public class ListIndexValue implements Serializable
{
   private ArrayList<IndexValue> list;
 
   public ListIndexValue()
   {
	  list = new ArrayList<IndexValue>();
   }
   
   public void add(IndexValue i)
   {
	   list.add(i);
   }
   
   public IndexValue getListElement(int index)
   {
	   return list.get(index);
   }
       
   public int size()
   {
	   return list.size();
   }
  
}
