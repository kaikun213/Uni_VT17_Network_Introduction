package mj223gn_jh223gj_assign2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import mj223gn_jh223gj_assign2.exceptions.*;

public class HTTPReader {
	
	BufferedReader in;
	int contentLength;
	
	public HTTPReader(InputStream in){
		this.in = new BufferedReader(new InputStreamReader(in));
	}
	
	private byte[] readBody() throws InvalidRequestFormatException, IOException{
		// Content-Length : the length of the body in number of octets (8-bit bytes)
		byte[] requestBody = new byte[contentLength];
		for (int i=0; i<contentLength;i++){
			requestBody[i] = (byte) in.read();
		}
		return requestBody;
	}

	private byte[] readChunkedBody() throws IOException{
		byte[] requestBody = new byte[0];
		/* Chunk size given as hex-code */
		int chunkSize = 0;
		int index;
		
		do{
			/* set new index (pointing at last filled position in array */
			index = requestBody.length;
			/* read the chunk size followed by \r\n */
			chunkSize = Integer.parseInt(in.readLine(), 16);
			/* resize byte array */
			requestBody = resize(requestBody, chunkSize);
			
			/* read each line (\r\n excluded) until chunk size is reached */
			System.out.println("Next chunk size: "+ chunkSize);
			for (int i=index;i<(index+chunkSize);i++){
				requestBody[i] = (byte) in.read();
				if (requestBody[i] == '\r' || requestBody[i] == '\n') i--;
				System.out.println("RequestBodyLength: "+ i + " : Content = " + new String(requestBody));
			}
			// read linebreak (\r\n)
			in.read();
			in.read();
			
		/* no more chunks to expect when a chunk with size zero is sent */
		}while(chunkSize != 0);

		System.out.println("Chunked data: " + requestBody);
		return requestBody;
	}
	
	private byte[] resize(byte[] arr, int length){
		if (length>= arr.length){
			byte[] temp = arr.clone();
			arr = new byte[length];
			for (int i=0;i<temp.length;i++){
				arr[i] = temp[i];
			}
			return arr;
		}
		else {
			byte[] temp = arr.clone();
			arr = new byte[length];
			for (int i=0;i<length;i++){
				arr[i] = temp[i];
			}
			return arr;
		}
		
	}
	
	private byte[] readBodyBase64() throws InvalidRequestFormatException, IOException{
		byte[] requestBody = new byte[contentLength];
		for (int i=0; i<contentLength;i++){
			requestBody[i] = (byte) in.read();
			/* convert the String format (for sending) back to original bytes */
			if (requestBody[i] == '%'){
				/* hex code is converted to two chars (String representing the hex number of the byte) => conversion back */
				requestBody[i] = (byte) Integer.parseInt((char)in.read()+""+ (char)in.read(), 16);
				contentLength -= 2;
			}
		}
		/* resize the array to not have empty bytes left (they would fail the decoding) */
		requestBody = resize(requestBody, contentLength);
		
		/* convert to string to find the start of data (Header includes type filename etc.) */
		String cuttedHeader = new String(requestBody);
		int index = cuttedHeader.indexOf("base64");
		// move pointer to start of image (base64.length = 6, +1 because of the encoded , sign)
		cuttedHeader = cuttedHeader.substring(index+7); 
		/* correct substring build. => Decode and save as requestBody */
		return Base64.getDecoder().decode(cuttedHeader);
	}


	public HTTPRequest read() throws IOException, UnsupportedMediaTypeException, HTTPMethodNotImplementedException,
			InvalidRequestFormatException, ContentLengthRequiredException, HTTPVersionIsNotSupportedException, RequestURIToLongException {

		StringBuilder request = new StringBuilder();
		String line;

		/* Read Headers */
		while (true){
			line = in.readLine();
			if (line == null || line.equals("") || line.equals("\r\n")) break;
			request.append(line + "\r\n");
		}

		/* error detection -> print out request once */
		System.out.println(request);

		/* Create HTTP Request */
		HTTPRequest result =  HTTPRequest.fromString(request.toString());

		/* Read Request Body (optional) */
		if ((contentLength = result.getContentLength()) != 0) {
			// If image/form => base64 decoding
			if (result.getContentType().equals(Header.MIMEType.png) || result.getContentType().equals(Header.MIMEType.jpg) || result.getContentType().equals(Header.MIMEType.xWWW)) result.setRequestBody(readBodyBase64());
			// otherwise raw data
			else result.setRequestBody(readBody());
		}

		/* If client sends data in chunks instead of contentLength raw data */
		if (result.isTransferEncodingChunked()){
			result.setRequestBody(readChunkedBody());
		}


		return result;
	}

}
