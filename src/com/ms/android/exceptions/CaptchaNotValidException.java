/**
 * 
 */
package com.ms.android.exceptions;

/**
 * @author Manoj Srivatsav
 *
 * Sep 14, 2013
 */
public class CaptchaNotValidException extends Exception 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CaptchaNotValidException(String message) 
	{
		super(message);
	}

}
