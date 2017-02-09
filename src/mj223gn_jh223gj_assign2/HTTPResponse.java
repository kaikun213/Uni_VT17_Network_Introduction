package mj223gn_jh223gj_assign2;

import java.util.Map;

public class HTTPResponse {
	
	enum HTTPStatus{
		// Planned to be supported Response status.
		Success(200), Created(201), NoContent(204),
		Redirected(303), NotModified(304),	
		Forbidden(403), NotFound(404), NotAllowed(405), LengthRequired(411), UnsupportedMediaType(415), 
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
	private String responseBody;
	private HTTPStatus status; 
	
	public HTTPResponse(HTTPStatus status, Map<Header.HTTPHeader, Header> headers){
		this.status = status;
		this.headers = headers;
	}
	
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	public String toString(){
		StringBuilder responseHeaders = new StringBuilder();
		// HTTP Status code
		responseHeaders.append("status" + ": " + status.getStatus() + "\r\n");

		// Headers
		for (Header h : headers.values()){
			responseHeaders.append(h.getType() + ": " +h.getContent() + "\r\n");
		}
		// return Status + an empty line + response content
		return responseHeaders.toString() + "\r\n" + responseBody;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

}
