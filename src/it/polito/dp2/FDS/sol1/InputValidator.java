package it.polito.dp2.FDS.sol1;

import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FlightInstanceStatus;
import it.polito.dp2.FDS.Time;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

public class InputValidator {
	
	//private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private static final String FLIGHTID_PATTERN = "^[A-Z]{2}[0-9]{1,4}$"; //two char and three numbers
	private static final String	AIRPORT_PATTERN = "^[A-Z]{3}$"; //three letters
	private static final String DELAY_PATTERN = "\\d+"; //numbers only
	private static final String SEAT_PATTERN = "^[0-9]{1,}[A-Z]{1}$"; //at least one digit and only one letter
	
	public boolean validateFile(String filename)
	{
		File f=new File(filename);
		if(f.exists() && !f.isDirectory())
			return true;
		else
			return false;
	}
	
	public boolean validateFlightNumber(String flightID)
	{
		if ((flightID == null) || (flightID.isEmpty()))
			return false;
		return flightID.matches(FLIGHTID_PATTERN);
	}
	
	public boolean validateDate(String date)
	{
		if ((date==null) || (date.isEmpty()))
			return false;
		try
		{
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy z");
		    df.setLenient(false); 
		    df.parse(date);
		}catch (ParseException e){
		     return false;
		}catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	/*
	public boolean validateHour(String time)
	{
		return time.matches(TIME24HOURS_PATTERN);
	}
	 */
	
	public boolean validateAirport(String airportCode)
	{
		if ((airportCode==null) || (airportCode.isEmpty()))
			return false;
		return airportCode.matches(AIRPORT_PATTERN);
	}
	
	public boolean validateDate(GregorianCalendar gc)
	{
		if (gc==null)
			return false;
		
		try
		{
			Date date = gc.getTime();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy z");
		    df.format(date);
		}catch (IllegalArgumentException e) {
			return false;
		}catch (Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean validateDelay(String delay)
	{
		return delay.matches(DELAY_PATTERN);
	}
	
	public boolean validateStatus(FlightInstanceStatus Status)
	{
		if (Status == null)
			return false;
		
		String status = Status.toString();
		
		if (status.equals("ARRIVED"))
			return true;
		if (status.equals("BOARDING"))
			return true;
		if (status.equals("BOOKING"))
			return true;
		if (status.equals("CANCELLED"))
			return true;
		if (status.equals("CHECKINGIN"))
			return true;
		if (status.equals("DEPARTED"))
			return true;
		return false;
		
	}
	
	public boolean validateStatus(String status)
	{
		if ( ( status==null ) || ( status.isEmpty() ) )
			return false;
		
		//Check if status is equal to one of these (ARRIVED|BOARDING|BOOKING|CANCELLED|CHECKINGIN|DEPARTED)
		if (status.equals("ARRIVED"))
			return true;
		if (status.equals("BOARDING"))
			return true;
		if (status.equals("BOOKING"))
			return true;
		if (status.equals("CANCELLED"))
			return true;
		if (status.equals("CHECKINGIN"))
			return true;
		if (status.equals("DEPARTED"))
			return true;
		return false;
	}
	
	public boolean validateAircraft (Aircraft aircraft)
	{
		if ( (aircraft.model == null) || (aircraft.seats == null) )
			return false;
		
		Set<String> seatSet = aircraft.seats;
		
		for (String s:seatSet)
			if ( (s==null) || (s.matches(SEAT_PATTERN) == false) )
				return false;
		
		return true;
	}
	
	public boolean validateTime(Time time)
	{
		if ((time != null) && (time.getMinute() < 60) && (time.getMinute() >= 0) &&
				(time.getHour()>=0) && (time.getHour() < 24))
		{
			/*
			int Hour = time.getHour();
			String hourStr = Integer.toString(Hour);
			System.out.println("Hour="+hourStr);
			*/
			return true;
		}else
			return false;
			
	}
	
	public boolean validateSeat(String s)
	{
		if ( ( s == null ) || ( s.isEmpty() ) )
			return false;
		
		return s.matches(SEAT_PATTERN);
		
	}
	
	
}
