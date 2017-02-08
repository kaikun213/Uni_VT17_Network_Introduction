package mj223gn_jh223gj_assign2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import mj223gn_jh223gj_assign2.Header.HTTPHeader;
import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;

/** HTTP Request in the standard format. Reads in the MethodType, URL, HTTPHeaders and RequestBody.
 * 
 * @author kaikun
 *
 */

public class HTTPRequest {
	
	enum Method {
		GET, POST, PUT, DELETE, HEAD, CONNECT, OPTIONS, TRACE, PATCH;
		
		public static Method fromString(String method) throws InvalidRequestFormatException{
			try {
				return Method.valueOf(method);
			} catch (IllegalArgumentException e){
				throw new InvalidRequestFormatException("Not supported request method: [" + method + "]");
			}
		}
	}
	
	private Method type;
	private String url;
	private Map<Header.HTTPHeader, Header> headers;
	private String requestBody;
	
	public HTTPRequest(Method type, String url, Map<Header.HTTPHeader, Header> headers, String requestBody){
		this.type = type;
		this.url = url;
		this.headers = headers;
		this.requestBody = requestBody;
	}
	
	public Method getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}
	
	public String getRequestBody(){
		return requestBody;
	}


	static String readBody(String body, int contentLength) throws InvalidRequestFormatException{
		// gives the length of the body in number of octets (8-bit bytes)
		try {
			return body.substring(0, contentLength);
		} catch (IndexOutOfBoundsException e){
			throw new InvalidRequestFormatException("The Content-Length is not correct. IndexOutOfBounds: ["+body.length()+","+contentLength+"]");
		}
	}
	
	// parse HTTP request from String
	static HTTPRequest fromString(String request) throws InvalidRequestFormatException{
		Map<Header.HTTPHeader, Header> headers = new HashMap<Header.HTTPHeader, Header>();
		int contentLength = 0;
		
		// <CR><LF> carriage return and line feed at each end of a line 
		String[] lines = request.split("\r\n");
		String[] requestLine = lines[0].split(" ");
		
		if (requestLine.length != 3) throw new InvalidRequestFormatException("Invalid format of request line: [" + requestLine + "]");

		// read in all the headers [skip first line => i=1]
		int lineIndex;
		for (lineIndex=1; lineIndex<lines.length;lineIndex++){
			if (lines[lineIndex].isEmpty()) break;	// empty line => end of headers (optional message body follows)
			Header h = Header.fromString(lines[lineIndex]);
			headers.put(h.getType(), h);
		}
		
		if (headers.containsKey(HTTPHeader.ContentLength)) {
			try { contentLength = Integer.parseInt(headers.get(HTTPHeader.ContentLength).getContent()); 
			} catch (NumberFormatException e){
				throw new InvalidRequestFormatException("ParsingError. Content-Length has to be an Integer.");
			}
		}
		
		if (!headers.containsKey(HTTPHeader.Host)) throw new InvalidRequestFormatException("Error. No Host Header defined.");
		
		return new HTTPRequest(Method.fromString(requestLine[0]), requestLine[1], headers, readBody(Arrays.stream(lines, lineIndex+1, lines.length-1).collect(Collectors.joining()).toString(),contentLength));
	}

}
