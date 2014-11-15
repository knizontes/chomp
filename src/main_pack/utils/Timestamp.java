package main_pack.utils;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Timestamp {

	public static Calendar calendar;
	
	
	
	public static String now(){
		return now("hh:mm:ss:SSS");
	}

	public static String now(String dateFormat) {
	    calendar = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(calendar.getTime());

	  }
	
	
	
	
}
