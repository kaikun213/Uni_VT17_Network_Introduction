package mj223gn_jh223gj_assign2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;

public class HTTPReader {
	
	BufferedReader in;
	int contentLength;
	
	public HTTPReader(InputStream in){
		this.in = new BufferedReader(new InputStreamReader(in));
	}
	
	private String readBody() throws InvalidRequestFormatException, IOException{
		// Content-Length : the length of the body in number of octets (8-bit bytes)
		StringBuilder requestBody = new StringBuilder();
		for (int i=0; i<contentLength;i++){
			requestBody.append((char)(in.read()));
		}
		return requestBody.toString();
	}
	
	public HTTPRequest read() throws IOException, InvalidRequestFormatException{
		StringBuilder request = new StringBuilder();
		String line;
		
		/* Read Headers */
		while (true){
			line = in.readLine();
			if (line == null || line.equals("") || line.equals("\r\n")) break;
			request.append(line + "\r\n");
			System.out.println(request); // error detection
		}
		
		/* Create HTTP Request */
		HTTPRequest result =  HTTPRequest.fromString(request.toString());
		
		/* Read Request Body (optional) */
		if ((contentLength = result.getContentLength()) != 0) {
			result.setRequestBody(readBody());
		}
			
		
		return result;
	}

}
