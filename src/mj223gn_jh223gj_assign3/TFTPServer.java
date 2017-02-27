package mj223gn_jh223gj_assign3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TFTPServer 
{
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	public static final String READDIR = "resources/tftp/read/"; //custom address at your PC
	public static final String WRITEDIR = "resources/tftp/write/"; //custom address at your PC
	// OP codes
	public static final int OP_RRQ = 1;
	public static final int OP_WRQ = 2;
	public static final int OP_DAT = 3;
	public static final int OP_ACK = 4;
	public static final int OP_ERR = 5;

	public static void main(String[] args) {
		if (args.length > 0) 
		{
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		//Starting the server
		try 
		{
			TFTPServer server= new TFTPServer();
			server.start();
		}
		catch (SocketException e) 
			{e.printStackTrace();}
	}
	
	private void start() throws SocketException 
	{
		byte[] buf= new byte[BUFSIZE];
		
		// Create socket
		DatagramSocket socket= new DatagramSocket(null);
		
		// Create local bind point 
		SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

		// Loop to handle client requests 
		while (true) 
		{        
			
			final InetSocketAddress clientAddress = receiveFrom(socket, buf);
			
			// If clientAddress is null, an error occurred in receiveFrom()
			if (clientAddress == null) 
				continue;

			final StringBuffer requestedFile= new StringBuffer();
			final int reqtype = ParseRQ(buf, requestedFile);

			new Thread() 
			{
				public void run() 
				{
					try 
					{
						DatagramSocket sendSocket= new DatagramSocket(0);

						// Connect to client
						sendSocket.connect(clientAddress);						
						
						System.out.printf("%s request for %s from %s using port %d\n",
								(reqtype == OP_RRQ)?"Read":"Write", requestedFile.toString(),
								clientAddress.getHostName(), clientAddress.getPort());  
								
						// Read request
						if (reqtype == OP_RRQ) 
						{      
							requestedFile.insert(0, READDIR);
							HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
						}
						// Write request
						else if (reqtype == OP_WRQ)
						{                       
							requestedFile.insert(0, WRITEDIR);
							HandleRQ(sendSocket,requestedFile.toString(),OP_WRQ);  
						}
						else {
							send_ERR(sendSocket, 4, "opcode=" + reqtype + " is invalid.");
							System.err.println("Invalid request. Sending an error packet.");
						}
						sendSocket.close();
					} 
					catch (SocketException e) 
						{e.printStackTrace();}
				}
			}.start();
		}
	}
	
	/**
	 * Reads the first block of data, i.e., the request for an action (read or write).
	 * @param socket (socket to read from)
	 * @param buf (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 */
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) 
	{	// return null if error occurs 
		InetSocketAddress socketAddress = null;
		
		// Create datagram packet
		DatagramPacket requestPacket = new DatagramPacket(buf, buf.length);
		
		// Receive packet
		try {
			socket.receive(requestPacket);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// Get client address and port from the packet
		socketAddress = new InetSocketAddress(requestPacket.getAddress(), requestPacket.getPort());
		
		return socketAddress;
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf (received request)
	 * @param requestedFile (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	private int ParseRQ(byte[] buf, StringBuffer requestedFile) 
	{
		
		// First two bytes define the OP-Code
		ByteBuffer wrap= ByteBuffer.wrap(buf);
		int opcode = wrap.getShort();
		
		// File name followed by a zero byte
			// Get index of zero bytes
			int filenameIndex = 0;
			int requestIndex = 0;
			for (int i=0; i<buf.length; i++){
				if (buf[i] == 0){
					// first zero byte 
					if (filenameIndex == 0) filenameIndex = i;
					// 2nd zero byte => break to cut of empty buffer
					else {
						requestIndex = i;
						break;
					}
				}
			}
		// 2 Bytes opcode offset
		requestedFile.append(new String(buf, 2, filenameIndex-2));
		
		// Mode followed by a zero byte
		String mode = new String(buf, filenameIndex+1, requestIndex-filenameIndex-1);
		
		// only octet mode supported (not case sensitive)
		if (!mode.toLowerCase().equals("octet")) throw new IllegalArgumentException("transfer mode not supported!");
				
		return opcode;
	}

	/**
	 * Handles RRQ and WRQ requests 
	 * 
	 * @param sendSocket (socket used to send/receive packets)
	 * @param requestedFile (name of file to read/write)
	 * @param opcode (RRQ or WRQ)
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) 
	{		
		if(opcode == OP_RRQ)
		{
			try {
				
				// Read file from directory
				File file = new File(requestedFile);
				// Check if file exists
				if (!file.exists() || file.isDirectory()){
					send_ERR(sendSocket, 1, "File not found!");
					return;
				}
				
				// read the file to a byte array
				FileInputStream fis;
				byte[] buf = new byte[(int) file.length()];
	
				fis = new FileInputStream(file);
				fis.read(buf);
					        	        
				// send all packages
				int retransmissionCounter = 0;
				for (int i=0; i< (file.length()/512+1);i++){
					boolean result = false;
					// Not the last packet
					if (i+1< (file.length()/512+1)){
						result = send_DATA_receive_ACK(sendSocket, i+1,Arrays.copyOfRange(buf,i*512, (i+1)*512));
						retransmissionCounter = 0;
					}
					// Last transmission (length of packet < 512)
					else {
						result = send_DATA_receive_ACK(sendSocket, i+1,Arrays.copyOfRange(buf,i*512, (int) file.length()));
						retransmissionCounter = 0;
					}
					
					// Re-sent or Timeout if no ACK received
					if (!result) {
						if (retransmissionCounter == 5){
							send_ERR(sendSocket, 0, "Timeout. To many retransmissions.");
							break;
						}
						i--;
						retransmissionCounter++;
					}
				}
		        fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				send_ERR(sendSocket, 1, "File not found");
			} catch (IOException e) {
				// should not occur! 
				e.printStackTrace();
				send_ERR(sendSocket, 0, e.getMessage());
			}

		}
		else if (opcode == OP_WRQ) 
		{
			byte[] buf = new byte[512];
			boolean result = true;			
			while (result){
				
			//	result = receive_DATA_send_ACK(sendSocket, buf, );
			}
		}
			
		
	}
	
	// Method to concatenate byte arrays 
	private byte[] concatenateByteArrays(byte[] a, byte[] b) {
	    byte[] result = new byte[a.length + b.length]; 
	    System.arraycopy(a, 0, result, 0, a.length); 
	    System.arraycopy(b, 0, result, a.length, b.length); 
	    return result;
	} 
	
	
	/**
	To be implemented
	*/
	
	private boolean send_DATA_receive_ACK(DatagramSocket sendSocket, int packetNumber, byte[] data){
		try {
			// Normally: 512 + 4 = 516 except for the termination packet.
			ByteBuffer packet = ByteBuffer.allocate(data.length+4);
			
			// convert int to short (cutting of)
			short shortOP = OP_DAT;
			short shortNR = (short) packetNumber;
			
			// Create packet
			packet.putShort(shortOP);
			packet.putShort(shortNR);
			packet.put(data);

			// send the packet
			sendSocket.send(new DatagramPacket(packet.array(), packet.position()));
			
			// wait for ACK
			byte[] recBuf = new byte[4];
			DatagramPacket receivePacket= new DatagramPacket(recBuf, recBuf.length);
			// Timeout if ACK does not arrive after 150ms
			sendSocket.setSoTimeout(150);
			sendSocket.receive(receivePacket);
			
			// First two bytes define the OP-Code
			ByteBuffer wrap= ByteBuffer.wrap(recBuf);
			int opcode = wrap.getShort();
			int blockNr = wrap.getShort();
			
			// ERROR: Wrong OP_CODE or Block#
			if (opcode != OP_ACK || blockNr != packetNumber){
				send_ERR(sendSocket, 4, "Illegal TFTP operation.");
			}
			
			// ERROR: transferId changed => no retransmission
			if (receivePacket.getPort() !=  sendSocket.getPort()){
				send_ERR(sendSocket, 5, "Unknown transfer ID.");
			}
		// In case ACK is not received => not sucessfully transmitted
		} catch (SocketException e){
			System.out.printf("Retransmission block %d - No ACK received.\n", packetNumber);
			return false;
		// Any other error E.g. access violation
		} catch (IOException e) {
			System.out.println("Access Violation or other IO-Problems");
			send_ERR(sendSocket, 2, "Access violation.");
			e.printStackTrace();
			return true;
		}
		return true;
	}
	
	private boolean receive_DATA_send_ACK(DatagramSocket sendSocket, byte[] buffer, int packetNumber){
		return true;
	}
	
	private void send_ERR(DatagramSocket sendSocket, int errCode, String errMessage){
	}
	
}



