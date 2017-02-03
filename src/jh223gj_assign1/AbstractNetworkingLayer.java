package jh223gj_assign1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * An abstract class that provides basic support for 
 * networking layers (UDP or TCP).
 */

public abstract class AbstractNetworkingLayer {
    public static final int MYPORT= 0;
	
    protected byte[] buf;
	protected int transferRateValue;
	protected SocketAddress localBindPoint;
	protected SocketAddress remoteBindPoint;
	/* For time measuring and scheduling => Message Transfer Rate */
    TimerTask task;    		
    Timer timer;
    Instant before;
    Instant after;
    Duration timePassed;
    int messagesSent = 0;
	
	/* Checks correct inputs in the format: IP_Address PORT Buffer_Size Message_Transfer_Rate */
	protected void correctInputs(String[] args){
		/* Check correct command line input parameters */
		if (args.length != 4) {
		    System.err.printf("Error.Please provide valid input parameters.\n usage: server_name port buffer_size message_transfer_rate\n");
		    System.exit(1);
		}
		else if (!isCorrectIP(args[0])) {
			System.err.println("Error. Provide a valid IP-Address - which is reachable from your network - in the correct format. E.g. 192.168.2.10");
			System.exit(1);
		}
		else if (!isCorrectPort(args[1])) {
			System.err.println("Error. Provide a valid PORT as Integer.");
			System.exit(1);
		}
		else if (!isValidBufferSize(args[2])) {
			System.err.println("Error. Provide a valid buffer size as Integer. The maximum Buffersize is 65,507 for a UDP-Datagram. If you take zero it will not be able to receive any messages.");
			System.exit(1);
		}
		else if (!isPositiveInteger(args[3])) {
			System.err.println("Error. Provide a valid message transfer rate as Integer.");
			System.exit(1);
		}
	}
	
	protected void initializeVariables(String[] args){
		/* Read in buffer size */
		int bufSize = Integer.parseInt(args[2]);
		buf= new byte[bufSize];
		
		/* Read in message transfer rate */
		transferRateValue = Integer.parseInt(args[3]);
		
		/* Create local endpoint using bind() */
		localBindPoint= new InetSocketAddress(MYPORT);
		
		/* Create remote endpoint */
		remoteBindPoint= new InetSocketAddress(args[0], Integer.valueOf(args[1]));
	}
	
	
	
	/* Check if input parameter is a String */
    protected boolean isPositiveInteger(String s) {
        @SuppressWarnings("resource")
		Scanner sc = new Scanner(s.trim());
        /* fixed radix = 10 ; decimal system */
        if(!sc.hasNextInt(10)) return false;
        /* if the first next is an Int, make sure it is positive and there is nothing else in the String left. */
        if (sc.nextInt(10)<0) return false;
        return !sc.hasNext();
    }
    
    /* Check if IP address is in correct format and reachable */
    protected boolean isCorrectIP(String s){
    	Pattern pattern = Pattern.compile("^\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");
    	Matcher matcher = pattern.matcher(s);
    	try {
			InetAddress ipAddress = InetAddress.getByName(s);
	    	if (!ipAddress.isReachable(3000)) return false;
		} catch (IOException e) {
			return false;
		}
    	return matcher.matches();
    }
    
	/* The port number is an unsigned 16-bit integer, so 65535. */
    protected boolean isCorrectPort(String s){
    	return (isPositiveInteger(s) && (Integer.parseInt(s)<=65535));
    }
    
    /* Checks if BufferSize is valid and does not exceed VM Heap limit */ 
    protected boolean isValidBufferSize(String s){
    	/* Default JVM MaxHeapSize = 3072327680, 2,147,483,647 is max Integer, One Integer is 32Bit long. */
    	/* Tested with 204483642 Buffer size and it worked, but it depends on other code how much is allocated. */
    	/* However even it does not complain about the construction of such a datagram we can not send any greater than 64kb (65,507Bytes + 28Header) when using UDP Protocol */
    	return (isPositiveInteger(s) && (Integer.parseInt(s)<=65507));
    }

}
