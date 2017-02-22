package mj223gn_jh223gj_assign2.exceptions;

public class HTTPMethodNotImplementedException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String methodType;

	public HTTPMethodNotImplementedException(String type, String s){
		super(s);
		methodType = type;
	}

	public String getMethodType() {
		return methodType;
	}

}
