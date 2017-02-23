package mj223gn_jh223gj_assign2;

import mj223gn_jh223gj_assign2.Header.HTTPHeader;
import mj223gn_jh223gj_assign2.Header.MIMEType;
import mj223gn_jh223gj_assign2.exceptions.AccessRestrictedException;
import mj223gn_jh223gj_assign2.exceptions.ContentLengthRequiredException;
import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;
import mj223gn_jh223gj_assign2.exceptions.ResourceNotFoundException;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponseFactory {

    private Map<Header.HTTPHeader, Header> headers;
    private String DefaultErrorPath = TCPServer.BASEPATH + "/errors/";

    public HTTPResponseFactory() {

    }

    public HTTPResponse getHTTPResponse(HTTPRequest request) throws ContentLengthRequiredException, InvalidRequestFormatException, IOException, ResourceNotFoundException, AccessRestrictedException {
        HTTPResponse response = null;
        boolean updated = false;
        boolean created = false;

		/* HTTP GET Method -> retrieve a resource */
        if (request.getType().equals(HTTPRequest.Method.GET) || request.getType().equals(HTTPRequest.Method.POST) || (request.getType().equals(HTTPRequest.Method.PUT))) {

            /* Content Type of request (for creation of resource) */
            MIMEType type = request.getContentType();
            // Only supports PNG from form-data currently.
            if (request.getContentType() == MIMEType.xWWW) type = MIMEType.png;

            String filePath = TCPServer.BASEPATH + request.getUrl();

            //IF test.jpg we send that we found the file in another place.
            if(request.getUrl().equals("/test.jpg")){
                return this.createFoundResponse(HTTPResponse.HTTPStatus.Found);
            }

            /* Check for request body */
            if (request.getContentLength() == 0 && !request.getType().equals(HTTPRequest.Method.GET))
                throw new ContentLengthRequiredException("POST/PUT Requests need a body!");
            /* POST => Insert/Update File (up to server) */
            else if (request.getType().equals(HTTPRequest.Method.POST)) {
                int hash = filePath.hashCode();
                filePath = handlePOST(filePath, request.getRequestBody(), type);
                if(filePath.hashCode() != hash){
                    created = true;
                }
                else{
                    updated = true;
                }
            }
            
            /* PUT => Insert/Update File in specified location */
            else if (request.getType().equals(HTTPRequest.Method.PUT)) {
            	if (new File(filePath).exists() && !(new File(filePath).isDirectory())) updated = true;
            	else created = true;
                filePath = createOrUpdateResource(filePath, request.getRequestBody(), type);
            }
            
            /* GET Requested File */
            File file = getResource(filePath);
            
            System.out.println("**************************" + file.getPath());
				
            /* Create Response Headers */
            headers = new HashMap<Header.HTTPHeader, Header>();

            /* Add Standard Server Headers */
            addServerHeaders(file);
				
            /* Create Response */
            /* 201 - Created */
            if(created){
                response =  new HTTPResponse(HTTPResponse.HTTPStatus.Created, headers);
            }
            /* 204 - No Content */
            else if(file.length() == 0){
                response =  new HTTPResponse(HTTPResponse.HTTPStatus.NoContent, headers);
            }
            /* 200 - OK */
            else {
                response = new HTTPResponse(HTTPResponse.HTTPStatus.OK, headers);
            }
            response.setResponseBody(file);

        }

        return response;
    }

    /* POST requests are handled by the server */
    private String handlePOST(String path, byte[] resource, MIMEType type) throws ResourceNotFoundException, AccessRestrictedException, IOException {
        File file = new File(path);
		/* If file/directory does not exist -> create file in the home directory */
        if (!file.exists()) return createOrUpdateResource("", resource, type);
		/* If it is a directory -> create file in the given directory */
        else if (file.isDirectory()) return createOrUpdateResource(path, resource, type);
		/* If it is a file -> update it */
        else return updateResource(path, resource);

    }

    private String createOrUpdateResource(String path, byte[] resource, MIMEType type) throws ResourceNotFoundException, AccessRestrictedException, IOException {
        File file = new File(path);
		/* if file in given path does not exist => create it */
        if (!file.exists()) {
			/* create the file in the resource folder */
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // => If directory then create a new file (hashOfResource.type)
        if (file.isDirectory()) {
            path = path + "/" + resource.hashCode() + "." + type;
            file = new File(path);
            file.createNewFile();
        }
        // No Ending => append type
        else if (path.lastIndexOf(".") < 0) {
            path = path + "." + type;
            file.renameTo(new File(path));
        }

		/* update the existing/created file with new content */
        return updateResource(path, resource);
    }

    private String updateResource(String path, byte[] resource) throws ResourceNotFoundException, AccessRestrictedException, IOException {
        File file = getResource(path);
        FileOutputStream out = new FileOutputStream(file);
        out.write(resource);
        out.close();
        return path;
    }

    /**
     * Returns the file of the given path. The Path is relative to the resources folder in the working directory.
     *
     * @param path to the file
     * @return File which is found in the path or index.html/index.htm if the path is pointing to a directory containing this file.
     * @throws ResourceNotFoundException thrown if the file or directory does not exist.
     */
    private File getResource(String path) throws ResourceNotFoundException, AccessRestrictedException, FileNotFoundException {
        File file = new File(path);

        /* Check if file/dir exists on the server */
        if (!file.exists()) {
			/* file probably referenced without type ending */
			
			/* Subtract parentDirectory and filename from path */
            int index = path.lastIndexOf(File.separator);
            String parentDir = path.substring(0, index + 1);
            String fileName = path.substring(index + 1);

            if (new File(parentDir).isDirectory()) {
				/* Check through all the files */
                for (File f : new File(parentDir).listFiles()) {
                    if (f.getName().contains(fileName + ".")) {
                        file = f;
                        break;
                    }
                }
            }
			/* If NOT FOUND => Exception thrown */
            else if (!file.exists()) throw new ResourceNotFoundException("No Resource <" + path + "> could be found.");
        }

        if (path.contains("../") || path.contains("/secret")) {
            throw new AccessRestrictedException("Access restricted");
        }

		/* Path refers to a directory => search for index file */
        if (file.isDirectory()) {
			/* ALWAYS create a new index page for that folder. (even if it exists) */
            if (!file.isFile()) {
                //Create a new file and a printwriter to write to it.
                File index = new File(path + "/index.html");
                PrintWriter pw = new PrintWriter(index);

                //Simple HTML code for showing the files in the folder.
                pw.write("<!DOCTYPE html> \n <html> \n <body>");

                for (File f : file.listFiles()) {
                    if (!f.getName().contains("index.")) {
                    	// link address of resource 
                    	if (path.equals(TCPServer.BASEPATH+"/") )pw.write("<p><a href=\"http://localhost:4950/" + f.getName()+"\">" );
                    	// if not base directory -> slash is missing
                    	else pw.write("<p><a href=\"http://localhost:4950/" + path.replace(TCPServer.BASEPATH+"/", "")+ "/" + f.getName()+"\">" );
                        // name of the file (clickable)
                    	pw.write(f.getName());
                    	// finish link tag
                        pw.write("</a></p>\n");
                    }
                }
                pw.write("</body> \n </html> \n");
                pw.close();

                //sets this index page as the response file.
                file = index;
            }

        }
        return file;
    }

	/* TASK: NEED TO ADD MORE STANDARD Headers! */
	private void addServerHeaders(File file) {
		headers.put(HTTPHeader.Date, new Header(HTTPHeader.Date, new Date().toString()));
		headers.put(Header.HTTPHeader.ContentLength, new Header(Header.HTTPHeader.ContentLength, Long.toString(file.length())));
		headers.put(Header.HTTPHeader.ContentType, new Header(Header.HTTPHeader.ContentType, Header.MIMEType.fromFileName(file.getName()).getTextFormat()));
		String filePath = "localhost:4950"+ file.getPath().replace(TCPServer.BASEPATH, "");
		headers.put(Header.HTTPHeader.Location, new Header(Header.HTTPHeader.Location, filePath));
		headers.put(HTTPHeader.AccessControlOrigin, new Header(Header.HTTPHeader.AccessControlOrigin, filePath));
	}

    /**
     * Creates a Found 302 page with a link to were the resource is found now.
     * @param status 302 Found
     * @return an HTTP response page.
     * @throws FileNotFoundException
     */
    public HTTPResponse createFoundResponse(HTTPResponse.HTTPStatus status) throws FileNotFoundException {
        HTTPResponse response;

        System.out.println("kalle");
        File found = new File(TCPServer.BASEPATH + "/errors/302.html");
        PrintWriter pw = new PrintWriter(found);

        //Simple HTML code for showing the files in the folder.
        pw.write("<!DOCTYPE html> \n <html> \n <title> 302 </title> \n <body>");
        pw.write("<h1>302</h1>\n");
        pw.write("<h2>Found</h2>\n");
        pw.write("<p>Found the requested resource under a new path, follow link.<p>\n");
        pw.write("<link href=\"images/test.jpg\"> \n");
        pw.write("</body> \n </html> \n");
        pw.close();

        headers = new HashMap<Header.HTTPHeader, Header>();

        /* Add Standard Server Headers */
        addServerHeaders(found);

        /* Create Response */
        response =  new HTTPResponse(status, headers);
        response.setResponseBody(found);

        System.out.println("Kuken");
        return response;
    }

    /**
     * Method thats creates error response from status codes.
     * Default response is 500 internalServerError.
     *
     * @param status code of what error occurred
     * @return HTTPResponse with error message
     */
    public HTTPResponse getErrorResponse(HTTPResponse.HTTPStatus status) {
        File file = new File(DefaultErrorPath + "500.html");
        HTTPResponse response;
        headers = new HashMap<Header.HTTPHeader, Header>();

        switch (status) {
            case BadRequest:
                file = new File(DefaultErrorPath + "400.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.BadRequest, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case Forbidden:
                file = new File(DefaultErrorPath + "403.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.Forbidden, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case NotFound:
                file = new File(DefaultErrorPath + "404.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.NotFound, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case UnsupportedMediaType:
                file = new File(DefaultErrorPath + "415.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.UnsupportedMediaType, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case NotImplemented:
                file = new File(DefaultErrorPath + "501.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.NotImplemented, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case HTTPVersionNotSupported:
                file = new File(DefaultErrorPath + "505.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.HTTPVersionNotSupported, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            case URIToLong:
                file = new File(DefaultErrorPath + "414.html");
                response = new HTTPResponse(HTTPResponse.HTTPStatus.URIToLong, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
            default:
                response = new HTTPResponse(HTTPResponse.HTTPStatus.InternalServerError, headers);
                addServerHeaders(file);
                response.setResponseBody(file);
                return response;
        }
    }


}
