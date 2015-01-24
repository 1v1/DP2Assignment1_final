package it.polito.dp2.FDS.sol1;

import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FlightInstanceReader;
import it.polito.dp2.FDS.FlightInstanceStatus;
import it.polito.dp2.FDS.FlightMonitor;
import it.polito.dp2.FDS.FlightMonitorException;
import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.MalformedArgumentException;
import it.polito.dp2.FDS.Time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class FlightMonitorSol implements FlightMonitor
{
	private String filename;
	private InputValidator validator;
	private File XMLfile;
	private Document doc;

	private Set<Aircraft> aircraftSet;
	private List<FlightReader> flightReaderList;
	private List<FlightInstanceReader> flightInstanceReaderList;
	private HashMap<String, Aircraft> aircraftMap = new HashMap<String, Aircraft>();

	public FlightMonitorSol()
	{
		filename=System.getProperty("it.polito.dp2.FDS.sol1.FlightInfo.file");
		validator=new InputValidator();
		try{

			XMLfile = new File(filename);

			if ((!XMLfile.exists()) || (XMLfile.isDirectory()))
				throw new FileNotFoundException("Invalid file");

			// Check if the XML file is empty
			BufferedReader br = new BufferedReader(new FileReader(filename));     
			if (br.readLine() == null)
			{
				br.close();
				throw new EmptyFileException("The requested file is empty");
			}
			br.close();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.setErrorHandler(new ErrorHandler() {
				@Override
				public void error(SAXParseException exception) throws SAXException {
					exception.printStackTrace();
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					exception.printStackTrace();
				}

				@Override
				public void warning(SAXParseException exception) throws SAXException {
					exception.printStackTrace();
				}
			});


			doc = dBuilder.parse(XMLfile);
			doc.getDocumentElement().normalize();

			// Create and fill the lists
			aircraftSet = new HashSet<Aircraft>();
			createAircrafts();

			flightReaderList = new ArrayList<FlightReader>();
			createFlights();

			flightInstanceReaderList = new ArrayList<FlightInstanceReader>();
			createFlightInstances();

		}catch (ParserConfigurationException e)
		{
			e.printStackTrace();
			System.out.println("Parser Configuration Exception");
			System.exit(1);
		}catch (SAXException e)
		{
			e.printStackTrace();
			System.out.println("SAX Exception");
			System.exit(1);
		}catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.out.println("Invalid file");
			System.exit(1);
		}catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("IO Exception");
			System.exit(1);
		}catch(EmptyFileException e)
		{
			e.printStackTrace();
			System.exit(1);
		}catch(FlightMonitorException e)
		{
			e.printStackTrace();
			System.exit(1);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void createAircrafts() throws FlightMonitorException
	{
		NodeList aircraftNodeList = doc.getElementsByTagName("aircraft");
		for (int i=0; i< aircraftNodeList.getLength(); i++)
		{
			Node aircraftNode = aircraftNodeList.item(i);
			if (aircraftNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element aircraftElement = (Element) aircraftNode;
				String model = aircraftElement.getAttribute("aircraftModel");

				if ((model == null) || (model.isEmpty()))
					throw new FlightMonitorException("Invalid aircraft model");


				NodeList seatNodeList = aircraftElement.getElementsByTagName("seat");
				Set<String> seatSet = new HashSet<String>();

				for (int j=0; j < seatNodeList.getLength(); j++)
				{
					Node seatNode = seatNodeList.item(j);
					if (seatNode.getNodeType() == Node.ELEMENT_NODE)
					{
						Element seatElement = (Element) seatNode;
						String seat = seatElement.getTextContent();

						// Check if the seat is well formed before adding
						if (!validator.validateSeat(seat))
							throw new FlightMonitorException("Seat format is not valid");

						// Check if the set is not already in the set, before adding it.
						// This avoids duplicates in the set
						boolean seatAlreadyInTheSet=false;
						for (String s:seatSet)
						{
							if (s.equals(seat))
							{
								seatAlreadyInTheSet = true;
								break;
							}
						}

						// Add seat in the seatSet if the seat is not in the set
						if (!seatAlreadyInTheSet)
							seatSet.add(seat);
					}
				}


				// Check if the aircraft is already in the set
				// This avoids duplicates in the set
				Boolean isInTheSet = false;

				for(Aircraft air:aircraftSet)
					if ((air.model.equals(model)) && (air.seats.equals(seatSet)))
						isInTheSet=true;

				//Add the model and the free seats to the aircraft set
				if (isInTheSet==false)
				{
					//The aircraft is not already in the set, so it'll be included
					Aircraft aircraft = new Aircraft(model, seatSet);
					if (validator.validateAircraft(aircraft))
						aircraftSet.add(aircraft);
					else
						throw new FlightMonitorException("Invalid aircraft");
					aircraftMap.put(aircraft.model, aircraft);
				}
			}
		}

	}

	public Set<Aircraft> getAircrafts()
	{
		return aircraftSet;
	}

	private void createFlights() throws FlightMonitorException
	{
		NodeList flightList = doc.getElementsByTagName("flight");
		try
		{
			for (int i = 0; i < flightList.getLength(); i++)
			{
				Node flightNode = flightList.item(i);
				if (flightNode.getNodeType() == Node.ELEMENT_NODE)
				{
					//Parse the flight
					Element flightElement = (Element) flightNode;

					String departureAirport=flightElement.getAttribute("departureAirport");
					if (validator.validateAirport(departureAirport)==false)
						throw new FlightMonitorException("Invalid departure airport");

					String destinationAirport=flightElement.getAttribute("destinationAirport");
					if (validator.validateAirport(destinationAirport)==false)
						throw new FlightMonitorException("Invalid destination airport");

					String departureTimeString=flightElement.getAttribute("departureTime");
					if ((departureTimeString==null) || (departureTimeString.isEmpty()))
						throw new FlightMonitorException("Invalid departure time");
					DateFormat df = new SimpleDateFormat("HH:mm");
					Date depTime = df.parse(departureTimeString);
					GregorianCalendar cal=new GregorianCalendar();
					cal.clear();
					cal.setTime(depTime);
					Time departureTime = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

					//The flight respects the input parameters, so it's added to the FlightReaderList
					String flightNumber=flightElement.getAttribute("flightID");

					if (validator.validateFlightNumber(flightNumber) == false)
						throw new FlightMonitorException("Invalid flight number");

					FlightReader fr= new FlightReaderSol(departureAirport, departureTime, destinationAirport, flightNumber);
					flightReaderList.add(fr);
				}
			}
		}catch(ParseException e)
		{
			throw new FlightMonitorException("Failed to parse the departure time");
		}
	}


	public FlightReader getFlight(String flightNumber) throws MalformedArgumentException
	{
		// Check if the argument is valid
		if (validator.validateFlightNumber(flightNumber) == false)
			throw new MalformedArgumentException("Invalid flight number");

		for (FlightReader f:flightReaderList)
			if (f.getNumber().equals(flightNumber))
				return f;

		return null;
	}

	public List<FlightReader> getFlights(String dep, String arr, Time startTime) throws MalformedArgumentException
	{
		// Check if the arguments are valid
		if ((validator.validateAirport(dep)==false) && (dep!=null))
			throw new MalformedArgumentException("Invalid departure airport argument");
		if ((validator.validateAirport(arr) == false) && (arr!=null))
			throw new MalformedArgumentException("Invalid destination airport argument");
		if ( (validator.validateTime(startTime) == false ) && ( startTime != null) )
			throw new MalformedArgumentException("Invalid departure time argument");
		if ( (dep!=null) && (arr!=null) && (dep.equals(arr)) )
			throw new MalformedArgumentException("Departure airport is equal to destination airport");

		Time timeToSearch;
		if (startTime == null)
			// If startTime == null, set the startTime equal to 00:00
			timeToSearch = new Time (0,0);
		else
			timeToSearch = startTime;

		List<FlightReader> returnList = new LinkedList<FlightReader>();
		returnList.clear();

		for (FlightReader f:flightReaderList)

			if ( ( ( f.getDestinationAirport().equals(dep) ) || ( dep == null) ) &&
					( ( f.getDestinationAirport().equals(arr) ) || ( arr == null ) ) &&
					( timeToSearch.precedes( f.getDepartureTime() ) ) )

				// This flight respects the requested parameters. So it's appended to the list
				returnList.add(f);

		return returnList;
	}

	private void createFlightInstances() throws FlightMonitorException
	{
		try
		{
			NodeList flightInstanceList=doc.getElementsByTagName("flightInstance");
			for (int j = 0; j < flightInstanceList.getLength(); j++)
			{
				Node flightInstanceNode = flightInstanceList.item(j);
				if (flightInstanceNode.getNodeType() == Node.ELEMENT_NODE)
				{
					//Parse status and departureDate in order to check the input parameters
					Element flightInstanceElement = (Element) flightInstanceNode;

					String flightNumber = flightInstanceElement.getAttribute("flightID");
					if (validator.validateFlightNumber(flightNumber) == false)
						throw new FlightMonitorException("Invalid flight number");

					String flightStatusString=flightInstanceElement.getAttribute("status");
					if (validator.validateStatus(flightStatusString)==false)
						throw new FlightMonitorException("Invalid status");
					FlightInstanceStatus flightStatus=FlightInstanceStatus.valueOf(flightStatusString);

					String depDateString=flightInstanceElement.getAttribute("departureDate");
					if (validator.validateDate(depDateString)==false)
						throw new FlightMonitorException("Invalid departure date");
					DateFormat df = new SimpleDateFormat("dd/MM/yyyy Z");
					Date depDate = df.parse(depDateString);
					GregorianCalendar cal=new GregorianCalendar();
					cal.setTime(depDate);

					String gate=flightInstanceElement.getAttribute("departureGate");
					if ((gate==null)||(gate.isEmpty()))
						gate="-";

					String delayString=flightInstanceElement.getAttribute("delay");
					if ((delayString==null)||(delayString.isEmpty()))
						delayString="0";
					if (validator.validateDelay(delayString)==false)
						throw new FlightMonitorException();
					int delay=Integer.parseInt(delayString);

					String model = flightInstanceElement.getAttribute("aircraftModel");
					

					if ((model==null)||(model.isEmpty()))
						throw new FlightMonitorException("Invalid aircraft model");
					
					if (aircraftMap.isEmpty())
						createAircrafts();

					Set<Passenger> pList=new HashSet<Passenger>();
					// Get the passenger set of the FlightInstance
					NodeList passengerList=flightInstanceElement.getElementsByTagName("passenger");
					for (int k=0; k < passengerList.getLength(); k++ )
					{
						Node passengerNode=passengerList.item(k);
						// Get the set of the free seats
						if (passengerNode.getNodeType() == Node.ELEMENT_NODE)
						{
							Element passengerElement=(Element) passengerNode;
							String name=passengerElement.getTextContent();
							if ((name==null) || (name.isEmpty()))
								throw new FlightMonitorException("Invalid passenger name");

							boolean boarded=Boolean.valueOf(passengerElement.getAttribute("boarded"));

							String passengerSeat=passengerElement.getAttribute("seat");

							if (validator.validateSeat(passengerSeat) == false)
							{
								if ( boarded == true )
									throw new FlightMonitorException("Invalid seat");
								if ( ( !passengerSeat.isEmpty() ) && ( passengerSeat != null ) )
									throw new FlightMonitorException("Invalid seat");
							}

							Passenger passenger=new Passenger(name, boarded, passengerSeat);
							pList.add(passenger);
						}
					}
					Aircraft aircraft = aircraftMap.get(model);
					FlightInstanceReader fr=new FlightInstanceReaderSol(getFlight(flightNumber), cal, delay, aircraft, flightStatus, gate, pList);
					flightInstanceReaderList.add(fr);
				}
			}
		}catch (MalformedArgumentException e)
		{
			e.printStackTrace();
		}catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	public FlightInstanceReader getFlightInstance(String number, GregorianCalendar date) throws MalformedArgumentException
	{
		// Check the arguments
		if  ( validator.validateFlightNumber(number) == false )
			throw new MalformedArgumentException("Invalid flight number argument");
		if ( validator.validateDate(date) == false )
			throw new MalformedArgumentException("Invalid departure date argument");

		for (FlightInstanceReader f:flightInstanceReaderList)
			if ( ( f.getFlight().getNumber().equals(number) ) && ( f.getDate().equals(date)) )
				return f;
		return null;
	}



	public List<FlightInstanceReader> getFlightInstances(String number, GregorianCalendar startDate,
			FlightInstanceStatus status) throws MalformedArgumentException
			{
		// Check the arguments
		if ( ( validator.validateFlightNumber(number) == false ) && ( number != null ) )
			throw new MalformedArgumentException("Invalid flight number argument");
		if ( (validator.validateStatus(status) == false ) && ( status != null ) )
			throw new MalformedArgumentException("Invalid flight status argument");
		if ( ( validator.validateDate(startDate) == false ) && ( startDate != null ) )
			throw new MalformedArgumentException("Invalid date arguments");


		List<FlightInstanceReader> returnList = new LinkedList<FlightInstanceReader>();
		returnList.clear();

		for (FlightInstanceReader f:flightInstanceReaderList)

			if ( ( ( number == null ) ||  ( f.getFlight().getNumber().equals(number) ) ) &&
					( ( status == null ) || ( f.getStatus().equals(status) ) ) &&
					( ( startDate == null ) || ( isBefore( startDate , f.getDate() ) ) ) )

				// This flight instances respects the requested parameters
				returnList.add(f);

		return returnList;
			}

	private boolean isBefore(GregorianCalendar startDate, GregorianCalendar flightDate)
	{
		//Check if the startDate is before the flight date
		if (flightDate.get(Calendar.YEAR) > startDate.get(Calendar.YEAR))
			return true;
		if(flightDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR))
		{
			if (flightDate.get(Calendar.MONTH) > startDate.get(Calendar.MONTH))
				return true;
			if (flightDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH))
				if (flightDate.get(Calendar.DAY_OF_MONTH) >= startDate.get(Calendar.DAY_OF_MONTH))
					return true;
		}
		return false;
	}

}
