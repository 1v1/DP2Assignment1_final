package it.polito.dp2.FDS.sol1;


public class Passenger {
	
	private String name;
	private boolean boarded;
	private String seat;
	
	public Passenger(String name, boolean boarded, String seat)
	{
		this.name=name;
		this.boarded=boarded;
		this.seat=seat;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSeat()
	{
		return seat;
	}
	
	public boolean isBoarded()
	{
		return boarded;
	}
}
