package backup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EOFTest {
	
	public static void main(String[] args){
		try {
			ServerSocket ss = new ServerSocket(4950);
			
			System.out.printf("Ip: %s ; PORT: %s ;\n", ss.getLocalSocketAddress(),ss.getLocalPort());
			//Socket s = new Socket("localhost", ss.getLocalPort());
			Socket s2 = ss.accept();

//			final DataOutputStream out =  new DataOutputStream(s.getOutputStream());
//			out.writeBytes("Hello World! This is a new longer text for muliple packages!");
//			out.close();

			final InputStream in = s2.getInputStream();
			byte[] data = new byte[100];
			String receivedString = "";
			int i;
			while ((i=in.read(data)) != -1){
				System.out.println("read: " + new String(data, 0, i));
				receivedString += new String(data, 0, i);
			}
			System.out.println(receivedString);
//			for (int b = 0; ((b = in.read()) >= 0);) {
//			    System.out.println(b + " " + (char) b);
//			}
			System.out.println("End of stream.");
			//s.close();
			s2.close();
			ss.close();
		} catch (IOException e){
			e.printStackTrace();
		}

	}
}
