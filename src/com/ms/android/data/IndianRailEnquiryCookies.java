/**
 * 
 */
package com.ms.android.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

/**
 * @author Manoj Srivatsav
 * Retrieves cookies need for connecting to Indian Rail Enquiry server and also fetch a 
 * valid captcha ID.
 * Sep 10, 2013
 */
public class IndianRailEnquiryCookies 
{
	private static final String tag = IndianRailEnquiryCookies.class.getName();
	private CookieStore mCookieStore;
	private CookieSpecRegistry mCookieSpecRegistry;
	private BasicNameValuePair mHiddenNameValue;
	private String mCaptchaId;
	private static long timeInMillis;
	private static IndianRailEnquiryCookies mInstance;
	private static final String INDIAN_RAIL_ENQUIRY = "http://enquiry.indianrail.gov.in/mntes";
	private static final String CAPTCHA_SERVLET_URL = "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=getNewCaptchaId&t=";
	
	private IndianRailEnquiryCookies()
	{
		timeInMillis = 0;		
	}
	
	public static IndianRailEnquiryCookies getInstance()
	{
		if(mInstance != null)
		{
			long cookieDuration = System.currentTimeMillis() - timeInMillis;
			if(cookieDuration > (6 * 3600 * 1000))
			{
				mInstance = getIndianRailEnquiryCookies();
				//mInstance.setCaptchaId(fetchCaptchaId());
			}
		}
		else
		{
			mInstance = getIndianRailEnquiryCookies();
			//mInstance.setCaptchaId(fetchCaptchaId());
		}
		
		return mInstance;
	}
	
	public static IndianRailEnquiryCookies getInstance(boolean captcha)
	{
		if(mInstance != null)
		{
			long cookieDuration = System.currentTimeMillis() - timeInMillis;
			if(cookieDuration > (6 * 3600 * 1000))
			{
				mInstance = getIndianRailEnquiryCookies();
				//mInstance.setCaptchaId(fetchCaptchaId());
			}
		}
		else
		{
			mInstance = getIndianRailEnquiryCookies();
			mInstance.setHiddenNameValuePair(fetchCaptchaId());
		}
		
		return mInstance;
	}
	
	private static IndianRailEnquiryCookies getIndianRailEnquiryCookies () 
	{
		String railyatriUrl = INDIAN_RAIL_ENQUIRY;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(railyatriUrl);
		IndianRailEnquiryCookies railyatriCookies = null;
		try 
		{
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if(httpResponse.getStatusLine().getStatusCode() == 200) 
			{
				railyatriCookies = new IndianRailEnquiryCookies();
				railyatriCookies.setCookieSpecRegistry(httpClient.getCookieSpecs());
				railyatriCookies.setCookieStore(httpClient.getCookieStore());

				//For debugging only
				/*
				List<Cookie> cookies = railyatriCookies.getCookieStore().getCookies();
				if(cookies.isEmpty()) {
					System.out.println("No Cookies!!!");
				} else {
					for(int i = 0; i < cookies.size(); i++) {
						System.out.println("-" + cookies.get(i).toString());
					}
				}
				*/
			}
		} 
		catch (ClientProtocolException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timeInMillis = System.currentTimeMillis();
		return railyatriCookies;
	}
	
	private static BasicNameValuePair fetchCaptchaId()
	{
		String captchaUrl = CAPTCHA_SERVLET_URL + (System.currentTimeMillis() / 1000);
		StringBuilder captchaId = new StringBuilder();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(captchaUrl);
		((DefaultHttpClient)httpClient).setCookieSpecs(IndianRailEnquiryCookies.mInstance.getCookieSpecRegistry());
		((DefaultHttpClient)httpClient).setCookieStore(IndianRailEnquiryCookies.mInstance.getCookieStore());
				
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		//httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpGet.setHeader("Host", "enquiry.indianrail.gov.in");
		httpGet.setHeader("Referer", "http://enquiry.indianrail.gov.in/mntes/");
		httpGet.setHeader("User-Agent", "Android");
		
		try 
		{
			HttpResponse httpResponse = httpClient.execute(httpGet);
			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if(statusCode == 200)
			{
				InputStream inputStream = httpResponse.getEntity().getContent();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));				
				String line;
				while ((line = bufferedReader.readLine()) != null)
				{
					captchaId.append(line);
				}
				
				// For debugging only
				Log.i(tag, "captcha id obtained from indian rail enquiry server: " + captchaId.toString());

			}			
		} 
		catch (ClientProtocolException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(captchaId != null)
		{
			return HtmlParser.extractHiddenData(captchaId.toString());
		}
		else
		{
			return null;
		}
		
	}
	
	public void setCookieStore(CookieStore cookieStore) 
	{
		mCookieStore = cookieStore;
	}

	public CookieStore getCookieStore() 
	{
		return mCookieStore;
	}

	public void setCookieSpecRegistry(CookieSpecRegistry cookieSpecRegistry) 
	{
		mCookieSpecRegistry = cookieSpecRegistry;
	}

	public CookieSpecRegistry getCookieSpecRegistry() 
	{
		return mCookieSpecRegistry;
	}
	
	public void setCaptchaId(String captchaId)
	{
		mCaptchaId = captchaId;
	}
	
	public String getCaptchaId()
	{
		return mCaptchaId;
	}
	
	public void setHiddenNameValuePair(BasicNameValuePair nameValue)
	{
		mHiddenNameValue = nameValue;
	}
	
	public BasicNameValuePair getHiddenNameValuePair()
	{
		return mHiddenNameValue;
	}

}

