package it.polito.dp2.FDS.sol1;

import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.Time;

public class FlightReaderSol implements FlightReader{
	
	private String departureAirport;
	private Time departureTime;
	private String destinationAirport;
	private String number;
	
	public FlightReaderSol(String departureAirport, Time departureTime, String destinationAirport, String number)
	{
		this.departureAirport=departureAirport;
		this.departureTime=departureTime;
		this.destinationAirport=destinationAirport;
		this.number=number;
	}
	
	public Time getDepartureTime()
	{
		return departureTime;
	}
	
	public String getNumber()
	{
		return number;
	}
	
	public String getDepartureAirport()
	{
		return departureAirport;
	}
	
	public String getDestinationAirport()
	{
		return destinationAirport;
	}
}
