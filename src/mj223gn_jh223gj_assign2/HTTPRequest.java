package mj223gn_jh223gj_assign2;

import java.util.HashMap;
import java.util.Map;

import mj223gn_jh223gj_assign2.Header.HTTPHeader;
import mj223gn_jh223gj_assign2.Header.MIMEType;
import mj223gn_jh223gj_assign2.exceptions.*;

/** HTTP Request in the standard format. Reads in the MethodType, URL, HTTPHeaders and RequestBody.
 * 
 * @author kaikun
 *
 */

public class HTTPRequest {
	
	enum Method {
		GET, POST, PUT;
		//, DELETE, HEAD, CONNECT, OPTIONS, TRACE, PATCH;
		
		public static Method fromString(String method) throws HTTPMethodNotImplementedException {
			try {
				return Method.valueOf(method);
			} catch (IllegalArgumentException e){
				throw new HTTPMethodNotImplementedException("Not supported request method: [" + method + "]");
			}
		}
	}
	
	private Method type;
	private String url;
	private Map<Header.HTTPHeader, Header> headers;
	private byte[] requestBody;
	private final static int MAX_NUMBER_OF_CHARACTERS_IN_URI = 2048;
	
	public HTTPRequest(Method type, String url, Map<Header.HTTPHeader, Header> headers){
		this.type = type;
		this.url = url;
		this.headers = headers;
		requestBody = null;
	}
	
	public Method getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}
	
	public byte[] getRequestBody(){
		return requestBody;
	}
	
	public void setRequestBody(byte[] requestBody){
		this.requestBody = requestBody;
	}
	
	/** Function to get the content length of a request.
	 * 
	 * @return Content length in number of chars. If no content is existing it returns 0.
	 * @throws ContentLengthRequiredException if the Content-Length header has no Integer as value
	 */
	public int getContentLength() throws ContentLengthRequiredException{
		if (headers.containsKey(HTTPHeader.ContentLength)) {
			try { 
				return Integer.parseInt(headers.get(HTTPHeader.ContentLength).getContent()); 
			} catch (NumberFormatException e){
				throw new ContentLengthRequiredException("ParsingError. Content-Length has to be an Integer.");
			}
		}
		else return 0;
	}
	/** Returns true if the client wants to close the connection.
	 * 
	 * @return true if header connection : close is sent. False if connection : alive, keep-alive, upgrade;
	 */
	public boolean closeConnection(){
		if (headers.containsKey(HTTPHeader.Connection)) {
			return ("close".equals(headers.get(HTTPHeader.Connection).getContent()));
		}
		return true;
	}
	
	public MIMEType getContentType(){
		MIMEType type = null;
		if (headers.containsKey(HTTPHeader.ContentType)) {
				/* conversion from text format to MIMEType enum */
				for (MIMEType m : Header.MIMEType.values()){
					if ((headers.get(HTTPHeader.ContentType).getContent().contains(m.getTextFormat()))){
						type = m;
					}
				}
		}
		return type;
	}
	
	public void setContentType(String type) throws UnsupportedMediaTypeException{
		MIMEType mType = null;
		for (MIMEType m : MIMEType.values()){
			if (type.contains(m.getTextFormat())) mType = m;
		}
		if (mType == null) throw new UnsupportedMediaTypeException("The given media type is not supported by the server!");
		headers.put(HTTPHeader.ContentType,new Header(HTTPHeader.ContentType, type));
	}
	
	public boolean isTransferEncodingChunked(){
		if (headers.containsKey(HTTPHeader.TransferEncoding)){
			return headers.get(Header.HTTPHeader.TransferEncoding).getContent().equals("chunked");
		}
		return false;
	}
	
	// parse HTTP request from String
	static HTTPRequest fromString(String request) throws InvalidRequestFormatException, HTTPMethodNotImplementedException, UnsupportedMediaTypeException, HTTPVersionIsNotSupportedException, RequestURIToLongException {
		Map<Header.HTTPHeader, Header> headers = new HashMap<Header.HTTPHeader, Header>();
		
		// <CR><LF> carriage return and line feed at each end of a line 
		String[] lines = request.split("\r\n");
		String[] requestLine = lines[0].split(" ");
		
		if (requestLine.length != 3) throw new InvalidRequestFormatException("Invalid format of request line: [" + requestLine + "]");

		//If URI is longer then MAX_NUMBER_OF_CHARACTERS_IN_URI we throw a 414.
		if(requestLine[1].length() > MAX_NUMBER_OF_CHARACTERS_IN_URI){
			throw new RequestURIToLongException("Requested URI is to long, sever limit = " + MAX_NUMBER_OF_CHARACTERS_IN_URI + " characters");
		}
		/* ******************** METHOD TO CHECK OUR 505 ERROR **************************************
		if(requestLine[2].equals("HTTP/1.1")){
			throw new HTTPVersionIsNotSupportedException("HTTP version 1.1 is not supported");
		}*/

		// read in all the headers [skip first line => i=1]
		for (int lineIndex=1; lineIndex<lines.length;lineIndex++){
			if (lines[lineIndex].isEmpty()) break;	// empty line => end of headers (optional message body follows)
			Header h = Header.fromString(lines[lineIndex]);
			headers.put(h.getType(), h);
		}

		if (!headers.containsKey(HTTPHeader.Host)) throw new InvalidRequestFormatException("Error. No Host Header defined.");
		
		return new HTTPRequest(Method.fromString(requestLine[0]), requestLine[1], headers);
	}

}
