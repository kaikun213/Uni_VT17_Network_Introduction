package backup;
/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPEchoClient {
    public static final int MYPORT= 0;
    public static final String MSG= "An Echo Message!";

    public static void main(String[] args) {
    /* Check correct command line input parameters */
	if (args.length != 4) {
	    System.err.printf("Error.Please provide valid input parameters.\n usage: server_name port buffer_size message_transfer_rate\n");
	    System.exit(1);
	}
	else if (!isCorrectIP(args[0])) {
		System.err.println("Error. Provide a valid IP-Address - which is reachable from your network - in the correct format. E.g. 192.168.2.10");
		System.exit(1);
	}
	else if (!isInteger(args[1])) {
		System.err.println("Error. Provide a valid PORT as Integer.");
		System.exit(1);
	}
	else if (!isInteger(args[2])) {
		System.err.println("Error. Provide a valid buffer size as Integer.");
		System.exit(1);
	}
	else if (!isInteger(args[3])) {
		System.err.println("Error. Provide a valid message transfer rate as Integer.");
		System.exit(1);
	}
	
	/* Read in buffer size */
	int bufSize = Integer.parseInt(args[2]);
	byte[] buf= new byte[bufSize];
	
	/* Read in message transfer rate */
	int transferRateValue = Integer.parseInt(args[3]);
	
	/* Create socket */
	DatagramSocket socket = null;
	try {
		socket = new DatagramSocket(null);
	} catch (SocketException e) {
		System.err.println("SocketException the socket could not be opened. " + e.getMessage());
		System.exit(1);
	}
	
	/* Create local endpoint using bind() */
	SocketAddress localBindPoint= new InetSocketAddress(MYPORT);
	try {
		socket.bind(localBindPoint);
	} catch (SocketException e) {
		System.err.println("SocketException the socket could not be opened or the socket could not bind to the specified local port. " + e.getMessage());
		System.exit(1);
	}
	
	/* Create remote endpoint */
	SocketAddress remoteBindPoint= new InetSocketAddress(args[0], Integer.valueOf(args[1]));
	
	/* Set time out in case server is unreachable (E.g. no Server running on port) */
	try {
		socket.setSoTimeout(3000);
	} catch (SocketException e1) {
		System.err.printf("Failure: port=%s;ip-address=%s; could not connect to a server.", args[1], args[0]);
		System.exit(1);
	}
	
	/* Create datagram packet for sending message */
	DatagramPacket sendPacket= new DatagramPacket(MSG.getBytes(), MSG.length(), remoteBindPoint);
	
	/* Create datagram packet for receiving echoed message */
	DatagramPacket receivePacket= new DatagramPacket(buf, buf.length);
	
	Instant before = Instant.now();
	for (int i=0; i<=transferRateValue; i++ ){
		/* Send and receive message*/
		try {
			socket.send(sendPacket);
			socket.receive(receivePacket);
		} catch (IOException e) {
			System.err.println("Exception sending or receiving packages. "+ e.getMessage());
			System.exit(1);
		}
		
		/* Compare sent and received message */
		String receivedString= new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
		if (receivedString.compareTo(MSG) == 0)
		    System.out.printf("%d bytes sent and received\n", receivePacket.getLength());
		else
		    System.out.printf("Sent and received msg not equal!\n");
		
		/* Delay the execution to have proper message transfer rate */
		try {
			Thread.sleep(1000/transferRateValue);
		} catch (InterruptedException e) {
			System.err.println("Interrupt Exception when putting the Thread to sleep." + e.getMessage());
			System.exit(1);
		}
	}
	Instant after = Instant.now();
	Duration timePassed = Duration.between(before, after);
	System.out.println("Time taken in nanoseconds: " + timePassed.toNanos());
	socket.close();
		
    }
    
    
    /* Check if input parameter is a String */
    public static boolean isInteger(String s) {
        @SuppressWarnings("resource")
		Scanner sc = new Scanner(s.trim());
        /* fixed radix = 10 ; decimal system */
        if(!sc.hasNextInt(10)) return false;
        /* if the first next is an Int, make sure there is nothing else in the String left. */
        sc.nextInt(10);
        return !sc.hasNext();
    }
    
    /* Check if IP address is in correct format and reachable */
    public static boolean isCorrectIP(String s){
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
    
}
