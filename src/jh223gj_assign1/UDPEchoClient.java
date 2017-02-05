package jh223gj_assign1;
/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;
/**
 * Implementation of a simple UDPEchoClient with error handling, buffer size and message transfer rate. 
 * Sends the same message redundantly. Message transfer rate is as exact as the Network Latency estimate. (Higer Latency => more exact/less messages) 
 * 
 * @author kaikun
 *
 */

public class UDPEchoClient extends jh223gj_assign1.AbstractNetworkingLayer{
    public static final String MSG= "An Echo Message!";
    public static final long LATENCY = 1000000;	// estimate for RTT and OS-Thread scheduling uncertainty 

    public long run(String[] args) {
	/* Check correct command line input parameters */
    correctInputs(args);	
    
	/* Initialize all variables with the command line parameters */
    initializeVariables(args);
    
	/* Create socket */
	DatagramSocket socket = null;
	try {
		socket = new DatagramSocket(null);
	} catch (SocketException e) {
		System.err.println("SocketException the socket could not be opened. " + e.getMessage());
		System.exit(1);
	}
	
	/* Bind local endpoint */
	try {
		socket.bind(localBindPoint);
	} catch (SocketException e) {
		System.err.println("SocketException the socket could not be opened or the socket could not bind to the specified local port. " + e.getMessage());
		System.exit(1);
	}
	
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
	Duration timePassed;
	
	/* Make sure to send a message when transferRate is zero */
	if (transferRateValue == 0) transferRateValue = 1;
	
	/* Send messages per second according to message transfer rate */
	for (int i=0; i<transferRateValue; i++ ){
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
		if (receivedString.compareTo(MSG) == 0) System.out.print("");
		    //System.out.printf("%d bytes sent and received\n", receivePacket.getLength());
		else
		    System.out.printf("Sent and received msg not equal! Received string: %s\n", receivedString);
		
		/* Delay the execution to have proper message transfer rate (adjust by average transfer time) */
		if (1000000000 - LATENCY - Duration.between(before, Instant.now()).toNanos() <= 0) { 
			System.out.printf("Could only sent %d messages of %d. \n",i, transferRateValue);
			break;
		}
		
		/* calculate sleepTime = time Threads stops execution. Remaining time divided by messages left */
		LockSupport.parkNanos((1000000000-Duration.between(before, Instant.now()).toNanos())/(transferRateValue-i));
		
	}
	/* time adjustment for nanoseconds */
	if (Duration.between(before, Instant.now()).toNanos()<1000000000) {
		LockSupport.parkNanos(1000000000-Duration.between(before, Instant.now()).toNanos());
	}
	timePassed = Duration.between(before, Instant.now());
	System.out.println("Time taken in nanoseconds: " + timePassed.toNanos());
	socket.close();
	return timePassed.toNanos();
    }
    
}
