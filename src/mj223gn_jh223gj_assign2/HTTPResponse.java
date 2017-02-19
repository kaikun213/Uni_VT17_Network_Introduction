package mj223gn_jh223gj_assign2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;

public class HTTPResponse {
	
	enum HTTPStatus{
		// Planned to be supported Response status.
		OK(200), Created(201), NoContent(204),
		Redirected(303), NotModified(304),	
		BadRequest(400),Forbidden(403), NotFound(404), NotAllowed(405), LengthRequired(411), UnsupportedMediaType(415),
		InternalServerError(500), NotImplemented(501), HTTPVersionNotSupported(505);
		
		private int status;
		
		public int getStatus(){
			return status;
		}
		
		private HTTPStatus(int status){
			this.status = status;
		}
	}
	
	private Map<Header.HTTPHeader, Header> headers;
	private File responseBody;
	private HTTPStatus status; 
	
	public HTTPResponse(HTTPStatus status, Map<Header.HTTPHeader, Header> headers){
		this.status = status;
		this.headers = headers;
	}
	
	public File getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(File responseBody) {
		this.responseBody = responseBody;
	}
	
	public String toString(){
		StringBuilder responseHeaders = new StringBuilder();
		// HTTP Status code
		responseHeaders.append("HTTP/" + TCPServer.HTTPVERSION + " " + status.getStatus() + " " + status + "\r\n");

		// Headers
		for (Header h : headers.values()){
			responseHeaders.append(h.getType() + ": " +h.getContent() + "\r\n");
		}
		// return Status + an empty line + response content
		return responseHeaders.toString() + "\r\n";
	}
	
	public byte[] toBytes(){
		byte[] headersInBytes = toString().getBytes();
		byte[] responseInBytes;
		/* If response has a body */
		if (responseBody != null) {
			responseInBytes = new byte[(int) (responseBody.length() + headersInBytes.length)];
			/* read bytes from file to end of array */
			try {
				new FileInputStream(responseBody).read(responseInBytes, headersInBytes.length, (int) responseBody.length());
			} catch (IOException e) {
				e.printStackTrace();
			}
			/* append header bytes in front */
			for (int i=0; i<headersInBytes.length;i++){
				responseInBytes[i] = headersInBytes[i];
			}
			return responseInBytes;
		}
		/* else return headers only */
		else return headersInBytes;
	}
	
	/* TASK : NICER SOLUTION THEN CREATING NEW BYTE ARRAY -> Intstream of the bytes from file directly */
	public IntStream toIntStream(){
		return null;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

}
