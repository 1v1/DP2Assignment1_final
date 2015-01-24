package it.polito.dp2.FDS.sol1;

import it.polito.dp2.FDS.FlightMonitor;
import it.polito.dp2.FDS.FlightMonitorException;



public class FlightMonitorFactory extends it.polito.dp2.FDS.FlightMonitorFactory 
{
	public FlightMonitor newFlightMonitor()
		throws FlightMonitorException
	{
		FlightMonitor fm=new FlightMonitorSol();
		return fm;
	}
	
}
