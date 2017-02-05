package jh223gj_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPEchoServer {
	
	public static final int BUFSIZE= 1024;
	public static final int MESSAGE_SIZE = 16;
    public static final int MYPORT= 4950;
    public static final int nTHREADS = 10;
	
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
				new Thread(new ConnectionHandler(socket.accept())).start();
				System.out.println("Active Threads:" + Thread.activeCount());
				Thread[] threads = new Thread[Thread.activeCount()];
				Thread.enumerate(threads);
				for (Thread t : threads)
				System.out.println(t.toString());
			}
		} catch (IOException e) {
		}
		
		
	}
	
	class ConnectionHandler implements Runnable{
		private Socket connection;
		private DataOutputStream sendPacket;
	    private DataInputStream receivePacket;
	    private int count = 0;
		public ConnectionHandler(Socket connection){ 
			this.connection = connection;
		}
		@Override
		public void run() {
			try {
				receivePacket = new DataInputStream(connection.getInputStream());
				sendPacket = new DataOutputStream(connection.getOutputStream());
				/* stay connected until client closes connection */
				while (true) {					
					byte[] data = new byte[BUFSIZE];
					Instant before = Instant.now();
					/* End of stream has been reached => EOF => Closed connection */
					try {
						receivePacket.readFully(data, 0, MESSAGE_SIZE);
					} catch (EOFException e){
						break;
					}
					Duration timeReceive = Duration.between(before, Instant.now());
					String receivedString = new String(data, 0, MESSAGE_SIZE);
					/* Send package back (echo) */
					sendPacket.writeBytes(receivedString);
					
					System.out.printf("TCP echo request from %s", connection.getInetAddress().getHostAddress());
				    System.out.printf(" using port %d", connection.getPort());
				    System.out.printf(" handled from thread %d; Message: %s", Thread.currentThread().getId(), receivedString);
				    System.out.printf(" receiving time: %d; total time: %d\n", timeReceive.toNanos(), Duration.between(before, Instant.now()).toNanos());

				}
				/* close connection and thread */
				System.out.printf("TCP Connection from %s using port %d handled by thread %d is closed.\n", connection.getInetAddress().getHostAddress(), connection.getPort(), Thread.currentThread().getId());
				connection.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
	}
	
	

}
