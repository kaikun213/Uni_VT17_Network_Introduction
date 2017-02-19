package mj223gn_jh223gj_assign2;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mj223gn_jh223gj_assign2.Header.HTTPHeader;
import mj223gn_jh223gj_assign2.exceptions.ResourceNotFoundException;

public class HTTPResponseFactory {
	
	Map<Header.HTTPHeader, Header> headers;
	
	public HTTPResponseFactory(){
		
	}
	
	public HTTPResponse getHTTPResponse(HTTPRequest request){
		HTTPResponse response = null;
		
		/* HTTP GET Method -> retrieve a resource */
		if (request.getType().equals(HTTPRequest.Method.GET)){
			try {
				/* GET Requested File */
				File file = getResource(request.getUrl());
				
				/* Add file specific headers Length Header */
				headers = new HashMap<Header.HTTPHeader, Header>();
				headers.put(Header.HTTPHeader.ContentLength, new Header(Header.HTTPHeader.ContentLength, Long.toString(file.length())));
				headers.put(Header.HTTPHeader.ContentType, new Header(Header.HTTPHeader.ContentType, Header.MIMEType.fromFileName(file.getName()).getTextFormat()));

				/* Add Standard Server Headers */
				addServerHeaders();
				
				/* Create Response */
				response = new HTTPResponse(HTTPResponse.HTTPStatus.OK, headers);
				response.setResponseBody(file);
			} catch (ResourceNotFoundException e) {
				/* TASK: 404 NOT FOUND SHOULD BE RETURNED */
				return null;
			}
		}
		
		return response;
	}
	
	/** Returns the file of the given path. The Path is relative to the resources folder in the working directory.
	 * 
	 * @param path to the file
	 * @return File which is found in the path or index.html/index.htm if the path is pointing to a directory containing this file.
	 * @throws ResourceNotFoundException thrown if the file or directory does not exist.
	 */
	private File getResource(String path) throws ResourceNotFoundException{
		File file = null;
	      try {
	    	  file = new File(TCPServer.BASEPATH + path);
	    	  
	    	  if (!file.exists()) throw new ResourceNotFoundException("No Resource <"+ path + "> could be found.");

	    	  /* Path refers to a directory => search for index file */
	    	  if (file.isDirectory()) {
	    		  for (File f : file.listFiles()){
	    			  if (f.getName().equals("index.html") || f.getName().equals("index.htm")){
	    				  file = f;
	    			  }
	    		  }
	    	  }
	      } 
	      catch(Exception e) {
	    	  e.printStackTrace();
	      }
	   return file;
	}
	
	/* TASK: NEED TO ADD MORE STANDARD Headers! */
	private void addServerHeaders(){
		headers.put(HTTPHeader.Date, new Header(HTTPHeader.Date, new Date().toString()));
	}
	
	
	
	

}
