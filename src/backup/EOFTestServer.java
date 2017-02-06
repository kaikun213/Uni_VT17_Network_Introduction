package backup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EOFTestServer {

	public static void main(String[] args) {
		try {
			Socket s = new Socket();
			s.bind(new InetSocketAddress(0));
			s.connect(new InetSocketAddress("192.168.56.1", 4950));
			
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			
			out.writeBytes("test hello world");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
