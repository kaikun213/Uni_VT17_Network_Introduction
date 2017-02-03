

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPEchoClient extends AbstractNetworkingLayer{
	
    public static final String MSG= "An Echo Message!";
    private Socket socket;
	private DataOutputStream sendPacket;
	private BufferedReader receivePacket;
	
	public void run(String[] args){
		/* Check correct command line input parameters */
	    correctInputs(args);	
	    
		/* Initialize all variables with the command line parameters */
	    initializeVariables(args);
	    
	    socket = new Socket();
	    try {
	    	socket.bind(localBindPoint);
			socket.connect(remoteBindPoint);
		    sendPacket = new DataOutputStream(socket.getOutputStream());
		    receivePacket = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		    
		    /* send and receive packets */
		    sendPacket.writeBytes(MSG + "\n");
		    
		    /* Compare sent and received message */
			String receivedString= receivePacket.readLine();
			if (receivedString.compareTo(MSG) == 0)
			    System.out.printf("%d bytes sent and received\n", receivedString.length());
			else
			    System.out.printf("Sent and received msg not equal! Received string: %s\n", receivedString); 
			
			
		    socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    
	}

}
