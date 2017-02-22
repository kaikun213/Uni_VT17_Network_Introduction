package mj223gn_jh223gj_assign2;

import java.util.Arrays;

import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;
import mj223gn_jh223gj_assign2.exceptions.UnsupportedMediaTypeException;

/** Simple Header class for HTTP
 * 
 * @author kaikun
 *
 */

public class Header {
	
	/* List of all standard HTTP headers - supported ones marked */
	enum HTTPHeader{
		Accept ("Accept"), 
		AcceptCharset ("Accept-Charset"), 
		AcceptEncoding ("Accept-Encoding"),
		AcceptLanguage ("Accept-Language"),
		AcceptDateTime ("Accept-Datetime"),
		Authorization ("Authorization"),
		CacheControl ("Cache-Control"),
		Connection ("Connection"),
		Cookie ("Cookie"),
		ContentLength ("Content-Length"),
		ContentMD5 ("Content-MD5"),
		ContentType ("Content-Type"), 			// supported
		Date ("Date"),
		Expect ("Expect"),
		Forwarded ("Forwarded"),
		From ("From"),
		Host ("Host"),							// supported
		IfMatch ("If-Match"),
		IfModifiedSince ("If-Modified-Since"),
		IfNoneMatch ("If-None-Match"),
		IfRange ("If-Range"),
		IfUnmodifiedSince ("If-Unmodified-Since"),
		MaxForwards ("Max-Forwards"),
		Origin ("Origin"),
		Pragma ("Pragma"),
		ProxyAuthorization ("Proxy-Authorization"),
		Range ("Range"),
		Referer ("Referer"),
		TE ("TE"),
		UserAgent ("User-Agent"),
		Upgrade ("Upgrade"),
		Via ("Via"),
		Warning ("Warning"),
		Unknown ("Unknown"),					// default case for not included headers
		TransferEncoding("Transfer-Encoding"),
		
		// Response specific
		Server("Server"),
		LastModified("Last-Modified"),
		Location("Location");
		
		private String textFormat;
		
		public String getTextFormat(){
			return textFormat;
		}
		
		private HTTPHeader(String textFormat){
			this.textFormat = textFormat;
		}
	}
	
	enum MIMEType {
	    js("application/javascript"),
	    json("application/json"),
	    pdf("application/pdf"),
	    xml("application/xml"),
	    zip("application/zip"),
	    mpeg("audio/mpeg"),
	    css("text/css"),
	    html("text/html"),
	    htm("text/html"),
	    png("image/png"),
	    jpg("image/jpeg"),
	    gif("image/gif"),
	    
	    // not supported
	    //vorbis("audio/vorbis"),
	    //formData("multipart/form-data"),
	    xWWW("application/x-www-form-urlencoded"),
	    
	    // Default
	    txt("text/plain");

	    
	    private String textFormat;
	    
	    public String getTextFormat(){
			return textFormat;
		}
	    
	    private MIMEType(String textFormat){
	    	this.textFormat = textFormat;
	    }
	    
	    // MIMEType from file extension => Not very good conversion Apache library function
	    // is a much better solution to pay attention to all the little tricks. However should be in scope
	    public static MIMEType fromFileName(String fileName){
	    	// Default type = text/plain
	    	MIMEType type = MIMEType.txt;
	    	int index = fileName.lastIndexOf('.');
	    	// file extension needs to be correct => e.g. js, png, html
			if (index > 0) type = MIMEType.valueOf(fileName.substring(index+1));
	    	return type;
	    }
	}
	
	// Format: type:content
	public static Header fromString(String headerLine) throws InvalidRequestFormatException, UnsupportedMediaTypeException{
		String[] split = headerLine.split(": ");

		/* split header line into type and content */
		if (split.length != 2){
			throw new InvalidRequestFormatException("The header format ["+ Arrays.toString(split) +"] is not correct.");
		}
		
		/* search corresponded header type */
		for (HTTPHeader h : HTTPHeader.values()) {
			if (h.textFormat.equals(split[0])) {
				if (h.equals(HTTPHeader.ContentType)){
					for (MIMEType m : MIMEType.values()){
						if (split[1].contains(m.textFormat)) return new Header(h, split[1]);
					}
					throw new UnsupportedMediaTypeException("The given media type is not supported by the server!");
				}
				else return new Header(h, split[1]);
			}
		}
		
		/* default return */
		return new Header(HTTPHeader.Unknown, split[1]);
	}
	
	private HTTPHeader type;
	private String content;
	
	public Header(HTTPHeader type, String content){
		this.type = type;
		this.content = content;
	}
	
	public HTTPHeader getType(){
		return type;
	}
	
	public String getContent(){
		return content;
	}

}
