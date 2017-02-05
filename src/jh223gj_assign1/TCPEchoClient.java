package jh223gj_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

public class TCPEchoClient extends AbstractNetworkingLayer{
	
    public static final String MSG= "An Echo Message!";
    public static final long LATENCY = 1000000;	// estimate for RTT and OS-Thread scheduling uncertainty 
    private Socket socket;
	private DataOutputStream sendPacket;
	private DataInputStream receivePacket;
	
	public long run(String[] args){
		/* Check correct command line input parameters */
	    correctInputs(args);	
	    
		/* Initialize all variables with the command line parameters */
	    initializeVariables(args);
	    
	    
	    socket = new Socket();
	    try {
	    	socket.bind(localBindPoint);
			socket.connect(remoteBindPoint);
			/* set Buffer size */
		    socket.setReceiveBufferSize(buf.length);
		    sendPacket = new DataOutputStream(socket.getOutputStream());
		    receivePacket = new DataInputStream(socket.getInputStream());
	    } catch (IOException e){
	    	e.printStackTrace();
	    }
		
	    /* Set time out in case server is unreachable (E.g. no Server running on port) */
		try {
			socket.setSoTimeout(3000);
		} catch (SocketException e1) {
			System.err.printf("Failure: port=%s;ip-address=%s; could not connect to a server.", args[1], args[0]);
			System.exit(1);
		}
		
	    Instant before = Instant.now();
		Duration timePassed;
		
		/* Make sure to send a message when transferRate is zero */
		if (transferRateValue == 0) transferRateValue = 1;
		
		/* Send messages per second according to message transfer rate */
		for (int i=0; i<transferRateValue; i++ ){
			/* reset buffer */
	    	buf = new byte[buf.length];
	    	boolean end = false;
			int bytesRead = 0;
			String receivedString = "";
			
			/* send and receive packets */
			try {
				sendPacket.writeBytes(MSG);

			    while(!end)
			    {
			        bytesRead = receivePacket.read(buf);
			        receivedString += new String(buf, 0, bytesRead);
			        if (receivedString.length() == MSG.length())
			        {
			            end = true;
			        }
			    }
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/* Compare sent and received message */							
			if (receivedString.compareTo(MSG) == 0)
			    System.out.printf("%d bytes sent and received from PORT %d \n", receivedString.length(), socket.getLocalPort());
			else
			    System.out.printf("Sent and received msg not equal! Received string(%d): %s != %s\n", receivedString.length(),receivedString, MSG);
			 
			
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
		
		/* close down connection */
	    try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return timePassed.toNanos();
	}
}
