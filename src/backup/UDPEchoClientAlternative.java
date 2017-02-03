package backup;
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

import jh223gj_assign1.AbstractNetworkingLayer;


public class UDPEchoClientAlternative extends jh223gj_assign1.AbstractNetworkingLayer{
    public static final String MSG= "An Echo Message!";

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
	
	/* Send messages per second according to message transfer rate */
	
	Instant before = Instant.now();
	Instant after;
	Duration timePassed;
	Duration remainingTime;
	
	/* Make sure to send a message when transferRate is zero */
	if (transferRateValue == 0) transferRateValue = 1;
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
			after = Instant.now();
			timePassed = Duration.between(before, after);
			/* 999ms (to take execution time into account) - [passed time] = [Remaining time]. [Remaining time] divided by [rest messages]  */
			remainingTime = timePassed.minus(Duration.ofMillis(999)).abs();
			long messagesLeft = (transferRateValue-i);
			if (remainingTime.toNanos() <= 0) { 
				System.out.printf("Could only sent %d messages of %d. Time passed: \n",i, transferRateValue, timePassed);
				break;
			}
			/* calculate sleepTime = time Threads stops execution */
			Duration sleepTime = remainingTime.dividedBy(messagesLeft);
			LockSupport.parkNanos(sleepTime.toNanos());
	}
	System.out.println("Before adjustment: " + Duration.between(before, Instant.now()).toNanos());
	/* time adjustment for nanoseconds */
	if (Duration.between(before, Instant.now()).toNanos()<1000000000) {
		LockSupport.parkNanos(1000000000-Duration.between(before, Instant.now()).toNanos());
	}
	timePassed = Duration.between(before, Instant.now());
	System.out.println("Time taken in nanoseconds: " + timePassed.toNanos());
	socket.close();
	return timePassed.toNanos();
    }
    
    /* measure average RTT (round trip time)  */
	//if (averageTime == null) averageTime = timePassed;
	/* Formula: RTT = (α · Old_RTT) + ((1 − α) · New_Round_Trip_Sample) */
	//else averageTime = Duration.ofNanos((long) (( (factorRTT*averageTime.toNanos())+((1-factorRTT)*(Duration.between(beforeNew, after).toNanos())) ) /2));

    
}
