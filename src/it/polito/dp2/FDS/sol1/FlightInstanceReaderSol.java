package it.polito.dp2.FDS.sol1;

import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FlightInstanceReader;
import it.polito.dp2.FDS.FlightInstanceStatus;
import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.PassengerReader;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

public class FlightInstanceReaderSol implements FlightInstanceReader{
	
	private FlightInstanceStatus status;
	private Aircraft aircraft;
	private GregorianCalendar date;
	private int delay;
	private String departureGate;
	private FlightReader flight;
	private Set<Passenger> passengerSet;
	
	public FlightInstanceReaderSol (FlightReader flight, GregorianCalendar date, int delay,
			Aircraft aircraft, FlightInstanceStatus status, String departureGate, Set<Passenger> passengerSet)
	{
		this.flight=flight;
		this.date=date;
		this.delay=delay;
		this.aircraft=aircraft;
		this.status=status;
		
		if (departureGate.equals("-")==true)
			this.departureGate=null;
		else
			this.departureGate=departureGate;
		
		this.passengerSet=passengerSet;
	}
	
	
	public FlightInstanceStatus getStatus()
	{
		return status;
	}
	
	public Aircraft getAircraft()
	{
		return aircraft;
	}
	
	public GregorianCalendar getDate()
	{
		return date;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public String getDepartureGate()
	{
		return departureGate;
	}
	
	public FlightReader getFlight()
	{
		return flight;
	}
	
	public Set<PassengerReader> getPassengerReaders(String namePrefix)
	{
		Set<PassengerReader> passengerReaderSet=new HashSet<PassengerReader>();
		for (Passenger p:passengerSet)
		{
			if (namePrefix==null)
			{
				PassengerReader pr=new PassengerReaderSol(p.isBoarded(), p.getName(), p.getSeat(), this);
				passengerReaderSet.add(pr);
			}else{
				if (p.getName().startsWith(namePrefix)==true)
				{
					PassengerReader pr=new PassengerReaderSol(p.isBoarded(), p.getName(), p.getSeat(), this);
					passengerReaderSet.add(pr);
				}
			}
		}
		return passengerReaderSet;
	}
	
}
