package mj223gn_jh223gj_assign2;

import java.util.HashMap;
import java.util.Map;

public class HTTPResponseFactory {
	
	private HTTPRequest request;
	
	public HTTPResponseFactory(){
		
	}
	
	public HTTPResponse getHTTPResponse(HTTPRequest request){
		
		Map<Header.HTTPHeader, Header> headers = new HashMap<Header.HTTPHeader, Header>();
		//headers.put(Header.HTTPHeader.ContentLength, value)
		
		HTTPResponse response = new HTTPResponse(HTTPResponse.HTTPStatus.Success, headers);
		return null;
	}

}
