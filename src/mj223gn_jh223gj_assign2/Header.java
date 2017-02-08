package mj223gn_jh223gj_assign2;

import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;

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
		ContentType ("Content-Type"),
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
		Unknown ("Unknown");					// default case for not included headers
		
		private String textFormat;
		
		public String getTextFormat(){
			return textFormat;
		}
		
		private HTTPHeader(String textFormat){
			this.textFormat = textFormat;
		}
	}
	
	// Format: type:content
	public static Header fromString(String headerLine) throws InvalidRequestFormatException{
		String[] split = headerLine.split(":");
		
		/* split header line into type and content */
		if (split.length != 2){
			throw new InvalidRequestFormatException("The header format ["+ split +"] is not correct.");
		}
		
		/* search corresponded header type */
		for (HTTPHeader h : HTTPHeader.values()) {
			if (h.textFormat.equals(split[0])) new Header(h, split[2]);
		}
		
		/* default return */
		return new Header(HTTPHeader.Unknown, split[2]);
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
