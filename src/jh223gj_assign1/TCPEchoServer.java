package jh223gj_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

public class TCPEchoServer {
	
	public static final int BUFSIZE= 1024;
    public static final int MYPORT= 4950;
	
	public static void main(String[] args) throws IOException{
		TCPEchoServer server = new TCPEchoServer();
		server.run(args);
	}
	
	public void run(String[] args){	
		try {
			/* Create Socket */
			ServerSocket socket = new ServerSocket(MYPORT);
			
			/* Endless loop waiting for client connections */
			while(true){
				/* Open new thread for each new client connection */
				new Thread(new ConnectionHandler(socket.accept())).start();
				
				/* Print out to investigate open threads */
				printThreadsInfo();
			}
		} catch (IOException e) {

		}
	}
	
	/* Prints the status of the currently active threads in the console */
	private void printThreadsInfo(){
		System.out.println("******************************************");
		System.out.println("Active Threads:" + Thread.activeCount());
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for (Thread t : threads)
		System.out.println(t.toString());
		System.out.println("******************************************");
	}
	
	/* Handles client connection */
	class ConnectionHandler implements Runnable{
		private Socket connection;
		private DataOutputStream sendPacket;
	    private DataInputStream receivePacket;
	    
		public ConnectionHandler(Socket connection){ 
			this.connection = connection;
		}
		@Override
		public void run() {
			try {
				/* Create input and output data stream */
				receivePacket = new DataInputStream(connection.getInputStream());
				sendPacket = new DataOutputStream(connection.getOutputStream());
				
				/* while client stays connected */
				while (true) {					
					byte[] data = new byte[BUFSIZE];
					
					Instant before = Instant.now();
					
					/* Read bytes until connection is closed down (sends packets out of order back) */
					int bytesRead = receivePacket.read(data);
					Duration timeReceive = Duration.between(before, Instant.now());

					/* EOF = End of file is reached (stream/connection is closed down) -> read returns -1 */
					if (bytesRead == -1) break;
					String receivedString = new String(data, 0, bytesRead);
					
					/* Send bytes back (echo) */
					sendPacket.writeBytes(receivedString);
					
					/* Print status of connection */
					System.out.printf("TCP echo request from %s", connection.getInetAddress().getHostAddress());
				    System.out.printf(" using port %d", connection.getPort());
				    System.out.printf(" handled from thread %d; Message: <%s>,", Thread.currentThread().getId(), receivedString);
				    System.out.printf(" time taken to receive: %d; total time: %d\n", timeReceive.toNanos(), Duration.between(before, Instant.now()).toNanos());

				}
				/* Tear down connection and print closing-status */
				System.out.printf("TCP Connection from %s using port %d handled by thread %d is closed.\n", connection.getInetAddress().getHostAddress(), connection.getPort(), Thread.currentThread().getId());
				connection.close();
			} catch (IOException e){
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	

}
