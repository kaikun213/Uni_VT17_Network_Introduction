package jh223gj_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

public class TCPEchoClient extends AbstractClient{
	
    public static final String MSG= "An Echo Message!";
    public static final long LATENCY = 1000000;	// estimate for RTT and OS-Thread scheduling uncertainty in nano seconds
    private Socket socket;
	private DataOutputStream sendPacket;
	private DataInputStream receivePacket;
	
	public long run(String[] args){
		/* Check CL-Parameters and initialize vairables */
	    setup(args);
	    
	    /* Setup connection to server and create Input/Output stream */
	    setupConnection();
		
	    Instant before = Instant.now();
		
		/* Send messages according to message transfer rate */
		for (int i=0; i<transferRateValue; i++ ){
			try {
				/* send and receive packets */
				sendPacket.writeBytes(MSG);
				String receivedString = readMessage();
				
				/* Compare sent and received message */							
				if (receivedString.compareTo(MSG) == 0)
				    System.out.printf("%d bytes sent and received from PORT %d \n", receivedString.length(), socket.getLocalPort());
				else
				    System.out.printf("Sent and received msg not equal! Received string(%d): %s != %s\n", receivedString.length(),receivedString, MSG);
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/* Check if remaining time is still enough to sleep & send/receive(LATENCY = expected, no RTT taken */
			if (1000000000 - LATENCY - Duration.between(before, Instant.now()).toNanos() <= 0) { 
				System.out.printf("Could only sent %d messages of %d. \n",i, transferRateValue);
				break;
			}
			
			/* Thread stops execution. Time = remaining time divided by messages left */
			LockSupport.parkNanos((1000000000-Duration.between(before, Instant.now()).toNanos())/(transferRateValue-i));
		}
		/* Time adjustment for nanoseconds to hit perfectly 1sec */
		if (Duration.between(before, Instant.now()).toNanos()<1000000000) {
			LockSupport.parkNanos(1000000000-Duration.between(before, Instant.now()).toNanos());
		}
		Duration timePassed = Duration.between(before, Instant.now());
		System.out.println("Time taken in nanoseconds: " + timePassed.toNanos());
		
		/* close down connection */
	    try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return timePassed.toNanos();
	}
	
	private String readMessage() throws IOException{
		String receivedString = "";
    	buf = new byte[bufSize];
    	
    	/* 
    	 * Read the echoed bytes until the complete message is received (MSG.size reached)
    	 * Then there should be no more packets left in the underlying window buffer 
    	 * => available() checks this. (optional)
    	*/
		do {
	        int bytesRead = receivePacket.read(buf);
	        receivedString += new String(buf, 0, bytesRead);
	        //System.out.println("received: " +receivedString);
	    } while(receivedString.length() != MSG.length() || receivePacket.available() != 0);
		
		return receivedString;
	}
	
	/* Initializes a socket with the given local, remote end point and the buffer size. */ 
	private void setupConnection(){
	    socket = new Socket();
	    try {
	    	/* bind to local port */
	    	socket.bind(localBindPoint);
	    	/* connect to server IP:PORT */
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
			System.err.printf("Timeout Failure: port=%s;ip-address=%s; could not connect to a server.", ((InetSocketAddress) remoteBindPoint).getPort(), ((InetSocketAddress) remoteBindPoint).getAddress());
			System.exit(1);
		}
	}
}
