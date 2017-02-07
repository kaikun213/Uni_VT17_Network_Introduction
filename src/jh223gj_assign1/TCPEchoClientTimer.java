package jh223gj_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.LockSupport;

public class TCPEchoClientTimer extends AbstractClient{
	
    public static final String MSG= "An Echo Message!";
    private Socket socket;
	private DataOutputStream sendPacket;
	private DataInputStream receivePacket;
	private TimerTask task;    		
    private Timer timer;
    private Instant before, after;
    private Duration timePassed;
    private int messagesSent;
	
	public long run(String[] args){
		/* Check correct command line input parameters */
	    checkInputs(args);	
	    
		/* Initialize all variables with the command line parameters */
	    setup(args);
	    
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
			
			/* close down connection */
		    try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
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
					    try {
					    	/* reset buffer */
					    	buf = new byte[bufSize];
					    	
							/* send and receive packets */
							sendPacket.writeBytes(MSG);
							
							receivePacket.readFully(buf, 0, MSG.length());
							String receivedString = new String(new String(Arrays.copyOfRange(buf, 0, MSG.length())));
							
							/* Compare sent and received message */							
							if (receivedString.compareTo(MSG) == 0)
							    System.out.printf("%d bytes sent and received\n", receivedString.length());
							else
							    System.out.printf("Sent and received msg not equal! Received string(%d): %s != %s\n", receivedString.length(),receivedString, MSG);
							 
						} catch (IOException e) {
							e.printStackTrace();
						}
				   }
				   else {
					   this.cancel();
					   this.notify();
				   }
			   }
			}
	}

}
