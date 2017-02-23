package mj223gn_jh223gj_assign2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class HTTPResponse {
	
	enum HTTPStatus{
		// Planned to be supported Response status.
		OK(200), Created(201), NoContent(204),
		Found(302),
		BadRequest(400),Forbidden(403), NotFound(404), LengthRequired(411), URIToLong(414), UnsupportedMediaType(415),
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
	
	public boolean hasResponseBody(){
		return (responseBody != null);
	}
	
	public String toString(){
		StringBuilder responseHeaders = new StringBuilder();
		// HTTP Status code
		responseHeaders.append("HTTP/" + TCPServer.HTTPVERSION + " " + status.getStatus() + " " + status + "\r\n");

		// Headers
		for (Header h : headers.values()){
			responseHeaders.append(h.getType().getTextFormat() + ": " +h.getContent() + "\r\n");
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

	public HTTPStatus getStatus() {
		return status;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}
	
	public ByteArrayOutputStream getHeaderAsByteArrayOutputStream() throws IOException{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(toString().getBytes());
		return stream;
	}
	
	/* Response Body as ByteArrayOutputStream */
	public ByteArrayOutputStream getBodyAsByteArrayOutputStream() throws IOException{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		/* If a response file exists */
		if (responseBody != null) {
			/* read into a byte stream */
			FileInputStream fis = new FileInputStream(responseBody);
			byte[] buf = new byte[1024];
	        for (int readNum; (readNum = fis.read(buf)) != -1;) {
	            stream.write(buf, 0, readNum);
	        }
	        fis.close();
		}
		
		/* returns empty stream if no file exists */
		return stream;
	}

}
