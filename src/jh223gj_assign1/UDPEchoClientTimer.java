package jh223gj_assign1;
/*
  UDPEchoClient.java
  A simple echo client with error handling in the abstract class.
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.LockSupport;

/** Implementation of a simple UDPEchoClient using a Timer to schedule the tasks precicly for the message transfer rate.
 * 
 * @author kaikun
 *
 */
public class UDPEchoClientTimer extends AbstractNetworkingLayer{
    public static String MSG= "An Echo Message!";
    private DatagramSocket socket = null;
    DatagramPacket sendPacket;
    DatagramPacket receivePacket;


    public long run(String[] args) {
	/* Check correct command line input parameters */
    correctInputs(args);	
    
	/* Initialize all variables with the command line parameters */
    initializeVariables(args);
    
	/* Create socket */
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
	sendPacket = new DatagramPacket(MSG.getBytes(), MSG.length(), remoteBindPoint);
	
	/* Create datagram packet for receiving echoed message */
	receivePacket = new DatagramPacket(buf, bufSize);
	
	/* Send messages per second according to message transfer rate */
	/* Make sure to send a message when transferRate is zero */
	if (transferRateValue == 0) transferRateValue = 1;
	/* creating timer task, timer for scheduled execution*/
    task = new SendReceiveTimer();
    timer = new Timer();
	
	before = Instant.now();
	/* scheduling the tasks at a constant rate => 999ms is 1sec minus an estimated execution time.  */
	int sleepTime = (999/transferRateValue);
	
	/* we do not want to run it concurrently so we set minimum time between on 1ms and print a message how many messages we could sent */
	if (sleepTime<=0) sleepTime = 1;
	timer.scheduleAtFixedRate(task,0, sleepTime);      
	synchronized (task){
		try {
			task.wait();
			timer.cancel();
			if (messagesSent<transferRateValue) System.out.printf("Could only sent %d messages of %d. \n",messagesSent, transferRateValue);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/* time adjustment for nanoseconds */
	if (Duration.between(before, Instant.now()).toNanos()<1000000000) {
		LockSupport.parkNanos(1000000000-Duration.between(before, Instant.now()).toNanos());
	}
	after = Instant.now();
	timePassed = Duration.between(before, after);
	System.out.println("Time taken in nanoseconds: " + timePassed.toNanos());
	socket.close();
	return timePassed.toNanos();
    }
    
    
    /* Implementation of sending, receiving and comparing messages task */
    class SendReceiveTimer extends TimerTask{    		
		   @Override
			public void run() {
			   synchronized (this){
				   /* As long as there is time left */
				   if (Duration.between(before, Instant.now()).toNanos()<1000000000){
					   /* if all messages sent notify thread */
					   if (++messagesSent >= transferRateValue) {
						   this.cancel();
						   this.notify();
					   }
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
						    System.out.printf("Sent and received msg not equal! Received string: %s\n", receivedString); 
				   }
				   else {
					   this.cancel();
					   this.notify();
				   }
			   }
			}
	}
    
}
