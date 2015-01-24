package it.polito.dp2.FDS.sol1;


import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FactoryConfigurationError;
import it.polito.dp2.FDS.FlightInstanceReader;
import it.polito.dp2.FDS.FlightMonitor;
import it.polito.dp2.FDS.FlightMonitorException;
import it.polito.dp2.FDS.FlightMonitorFactory;
import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.MalformedArgumentException;
import it.polito.dp2.FDS.PassengerReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class FDSInfoSerializer {
	
	private FlightMonitor monitor;
	DateFormat dateFormat;
	
	private FDSInfoSerializer() throws FlightMonitorException {
		FlightMonitorFactory factory = FlightMonitorFactory.newInstance();
		monitor = factory.newFlightMonitor();
		dateFormat = new SimpleDateFormat("dd/MM/yyyy z");
	}
	
	public static void main(String[] args) {
		FDSInfoSerializer f;
		
		try{
			String filename=args[0];
			f=new FDSInfoSerializer();
			f.createDOM(filename);
		}catch(ArrayIndexOutOfBoundsException aiobe)
		{
			aiobe.getMessage();
			System.exit(1);
		}catch(FlightMonitorException fme)
		{
			System.err.println("Could not instantiate flight monitor object");
			fme.printStackTrace();
			System.exit(1);
		}

	}
	
	private void createDOM(String filename)
	{
		try {
      		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance ();
			DocumentBuilder builder = factory.newDocumentBuilder ();

			// Create the document
			Document doc = builder.newDocument ();
			Element root = (Element) doc.createElement ("flightInfo");
			doc.appendChild (root);
			printAircraft(doc, root);
			printFlights(doc, root);
			
			
			// Serialize the document onto System.out
			TransformerFactory xformFactory = TransformerFactory.newInstance ();
			Transformer idTransform = xformFactory.newTransformer ();
			
			//Set the .dtd file
			idTransform.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "flightInfo.dtd");
			
			//Set the indent value
			idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
			idTransform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			Source input = new DOMSource (doc);
			Result output = new StreamResult (filename);
			Result output2 = new StreamResult ("dtd/flightinfo.xml");
			idTransform.transform (input, output);
			idTransform.transform (input, output2);
		}catch (FactoryConfigurationError e)
		{
		 	System.out.println ("Could not locate a JAXP factory class");
		 	e.printStackTrace();
		 	System.exit(1);
		 }
		catch (ParserConfigurationException e)
		{
			System.out.println ("Could not locate a JAXP DocumentBuilder class");
			e.printStackTrace();
			System.exit(1);
		}
		catch (DOMException e)
		 {
		 	System.out.println("Error while building the DOM tree");
			e.printStackTrace();
			System.exit(1);
		 }
		catch (TransformerException e)
		{
			System.out.println("Unexpected Error during serialization");
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Error during serialization");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void printPassengerLists(Document doc, Element FlightInstanceNode, FlightInstanceReader f) {
		
		Set<PassengerReader> passengerSet = f.getPassengerReaders(null);
		for(PassengerReader p:passengerSet)
		{
			Element passenger=doc.createElement("passenger");
			passenger.setAttribute("departureDate", FlightInstanceNode.getAttribute("departureDate"));
			passenger.setAttribute("seat", p.getSeat());
			passenger.setAttribute("flightID", FlightInstanceNode.getAttribute("flightID"));
			String boarded;
			if (p.boarded()==true)
				boarded="true";
			else
				boarded="false";
			passenger.setAttribute("boarded", boarded);
			Text text=doc.createTextNode(p.getName());
			passenger.appendChild(text);
			FlightInstanceNode.appendChild(passenger);
		}
	}

	private void printFlightInstances(Document doc, Element flightNode, String flightID) {

		List<FlightInstanceReader> l;
		try {
			l = monitor.getFlightInstances(flightID, null, null);
			for (FlightInstanceReader f:l) {
				Element flightInstanceNode=doc.createElement("flightInstance");

				flightInstanceNode.setAttribute("status", f.getStatus().toString());
				flightInstanceNode.setAttribute("delay", String.valueOf(f.getDelay()));
				flightInstanceNode.setAttribute("flightID", flightID);
				flightInstanceNode.setAttribute("aircraftModel", f.getAircraft().model);
				
				String gate=f.getDepartureGate();
				if (gate==null)
				{
					gate="-";
				}
				flightInstanceNode.setAttribute("departureGate", gate);
				
				GregorianCalendar date = f.getDate();
				dateFormat.setTimeZone(date.getTimeZone());
				flightInstanceNode.setAttribute("departureDate", dateFormat.format(date.getTime()));
				printPassengerLists(doc,flightInstanceNode, f);
				flightNode.appendChild(flightInstanceNode);
			}
			
		} catch (MalformedArgumentException e) {
			// this exception will never be thrown because getFlightInstances is called with null arguments
			System.err.println("Unexpected exception");
			e.printStackTrace();
		}
	}
	
	private void printAircraft(Document doc, Element root)
	{
		Element aircraftSetNode=doc.createElement("aircraftSet");
		
		Set<Aircraft> aircraftSet = monitor.getAircrafts();
		
		for (Aircraft a: aircraftSet)
		{
			Element aircraftNode = doc.createElement("aircraft");
			aircraftNode.setAttribute("aircraftModel", a.model);
			Set<String> seatList = a.seats;
			for (String s:seatList)
			{
				Element seat=doc.createElement("seat");
				Text text=doc.createTextNode(s);
				seat.appendChild(text);
				aircraftNode.appendChild(seat);
			}
			aircraftSetNode.appendChild(aircraftNode);
		}
		root.appendChild(aircraftSetNode);
	}

	private void printFlights(Document doc, Element root)
	{
		
		try {
			List<FlightReader> l = monitor.getFlights(null, null, null);
			for (FlightReader f:l) {
				Element flightNode=doc.createElement("flight");
				//Set the Attributes of flight
				
				flightNode.setAttribute("departureAirport", f.getDepartureAirport());
				flightNode.setAttribute("destinationAirport", f.getDestinationAirport());
				
				//flightNode.setAttribute("departureTimeZone", printTimeZone(f.getDepartureAirport()));
				
				//Convert Time into a human-readable format
				StringBuffer b = new StringBuffer();
				GregorianCalendar g = new GregorianCalendar();
				g.set(GregorianCalendar.HOUR_OF_DAY, f.getDepartureTime().getHour());
				g.set(GregorianCalendar.MINUTE, f.getDepartureTime().getMinute());	
				b.append(String.format("%1$2tH", g));
				b.append(':');
				b.append(String.format("%1$2tM", g));
				
				flightNode.setAttribute("departureTime", b.toString());
				flightNode.setAttribute("flightID", f.getNumber());
				
				printFlightInstances(doc, flightNode, f.getNumber());
				
				root.appendChild(flightNode);
			}
		} catch (MalformedArgumentException e) {
			// this exception will never be thrown because getFlights is called with null arguments
			System.err.println("Unexpected exception");
			e.printStackTrace();
		}
		
	}

}

