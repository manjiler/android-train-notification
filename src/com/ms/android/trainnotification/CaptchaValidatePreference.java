/**
 * 
 */
package com.ms.android.trainnotification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.ms.android.data.IndianRailEnquiryCookies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Manoj Srivatsav
 *
 * Sep 11, 2013
 */
public class CaptchaValidatePreference extends DialogPreference 
{
	private static final String tag = CaptchaValidatePreference.class.getName();
	private static final String CAPTCHA_ID_URL = "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=getNewCaptchaId&t=";
	private static final String CAPTCHA_IMAGE_URL = "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=getNewCaptchaImg&t=";													 
	private static final String CAPTCHA_VALIDATE_URL = "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=validateCaptchaText";
	
	private Context mContext;
	private String mCaptchaId;
	private boolean validCaptcha;
	private EditText mCaptchaInputText;
	private TextView mCaptchaValidTextView;
	private TextView mCaptchaDetailsTextView;
	private ProgressBar mProgressBar;
	private ImageView mCaptchaImage;
	private Button mVerifyCaptchaButton;

	public CaptchaValidatePreference(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		mContext = context;
		setDialogLayoutResource(R.layout.captcha_validate_layout);
	}
	
	protected void onBindDialogView(View view)
	{
		mCaptchaImage = (ImageView) view.findViewById(R.id.captchaImageView);
		mVerifyCaptchaButton = (Button) view.findViewById(R.id.captchaVerifyButton);
		mCaptchaInputText = (EditText) view.findViewById(R.id.inputCaptchaEditText);
		mCaptchaValidTextView = (TextView) view.findViewById(R.id.validCaptchaText);
		mCaptchaDetailsTextView = (TextView) view.findViewById(R.id.captchaDetailsText);
		mProgressBar = (ProgressBar) view.findViewById(R.id.imageLoadingProgressBar);
		
		String captchaImageUrl = CAPTCHA_IMAGE_URL + System.currentTimeMillis();
		new DownloadImgTask(mCaptchaImage, mProgressBar).execute(captchaImageUrl);
		
		mVerifyCaptchaButton.setOnClickListener(new OnClickListener() 
		{
			
			public void onClick(View v) 
			{
				String userInput = mCaptchaInputText.getText().toString();
				new ValidateCaptchaTask(mCaptchaValidTextView, 
						mCaptchaDetailsTextView).execute(userInput, CAPTCHA_VALIDATE_URL);				
			}
		});
	}
	
	protected void onDialogClosed(boolean positiveResult)
	{
		if(positiveResult)
		{
			if(validCaptcha)
			{
				String validCaptchaId = mCaptchaId.split(";")[0].split("=")[1];
				persistString(validCaptchaId);
			}
		}
	}
	
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		
	}

	private class DownloadImgTask extends AsyncTask<String, Integer, Bitmap>
	{
		private ImageView imgView;
		private ProgressBar progressBar;
		
		public DownloadImgTask(ImageView imgView, ProgressBar progressBar) 
		{
			this.imgView = imgView;
			this.progressBar = progressBar;			
		}
		
		@Override
		protected Bitmap doInBackground(String... params) 
		{
			String url = params[0];
			publishProgress(10);
			Bitmap image = null;
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			IndianRailEnquiryCookies indianRailCookies = IndianRailEnquiryCookies.getInstance(true);
			if(indianRailCookies != null) 
			{
				((DefaultHttpClient)httpClient).setCookieSpecs(indianRailCookies.getCookieSpecRegistry());
				((DefaultHttpClient)httpClient).setCookieStore(indianRailCookies.getCookieStore());
			}
			httpGet.addHeader("Accept", "image/webp,*/*;q=0.8");
			//httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			httpGet.addHeader("Accept-Language", "en-US,en;q=0.8");
			httpGet.addHeader("Connection", "keep-alive");
			httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
			httpGet.addHeader("Host", "enquiry.indianrail.gov.in");
			httpGet.addHeader("Referer", "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=getNewCaptchaId&t=" + (System.currentTimeMillis() - 2 * 1000));
			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
			
			try
			{				
				HttpResponse response = httpClient.execute(httpGet);
				if(response.getStatusLine().getStatusCode() == 200)
				{
					InputStream in = response.getEntity().getContent();
					image = BitmapFactory.decodeStream(in);
				}
					
			}
			catch(Exception ex)
			{
				Log.e(tag, ex.getMessage());
			}
			
			return image;
		}
		
		protected void onProgressUpdate(Integer... values)
		{
			progressBar.setVisibility(ProgressBar.VISIBLE);
		}
		
		protected void onPostExecute(Bitmap result)
		{
			imgView.setImageBitmap(result);
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}

	}
	
	private class ValidateCaptchaTask extends AsyncTask<String, Void, String>
	{
		
		private TextView captchaValidText;
		private TextView captchaDetailText;
		
		public ValidateCaptchaTask(TextView captchaValidText, TextView captchaDetailText) 
		{
			this.captchaValidText = captchaValidText;
			this.captchaDetailText = captchaDetailText;
		}
		
		protected String doInBackground(String... params)
		{
			String userInput = params[0];
			String captchaValidateURL = params[1];
			StringBuilder isCaptchaValid = new StringBuilder();
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(captchaValidateURL);
			IndianRailEnquiryCookies indianRailCookies = IndianRailEnquiryCookies.getInstance();
			if(indianRailCookies != null) 
			{
				((DefaultHttpClient)httpClient).setCookieSpecs(indianRailCookies.getCookieSpecRegistry());
				((DefaultHttpClient)httpClient).setCookieStore(indianRailCookies.getCookieStore());
			}
			
			httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			//httpPost.setHeader("Accept-Encoding", "gzip,deflate,sdch");
			httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
			httpPost.setHeader("Connection", "keep-alive");
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setHeader("Pragma", "no-cache");
			httpPost.setHeader("Host", "enquiry.indianrail.gov.in");
			httpPost.setHeader("Origin", "http://enquiry.indianrail.gov.in");
			httpPost.setHeader("Referer", "http://enquiry.indianrail.gov.in/mntes/CaptchaServlet?action=getNewCaptchaId&t=" + (System.currentTimeMillis() - 2*1000));
			httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
			
			try
			{
				
				List<NameValuePair> formData = new ArrayList<NameValuePair>();
				formData.add(indianRailCookies.getHiddenNameValuePair());
				formData.add(new BasicNameValuePair("userInput", userInput));
				httpPost.setEntity(new UrlEncodedFormEntity(formData));
				HttpResponse response = httpClient.execute(httpPost);
				if(response.getStatusLine().getStatusCode() == 200)
				{
					Header cookieHeader = response.getFirstHeader("Set-Cookie");
					if(cookieHeader != null)
					{
						mCaptchaId = cookieHeader.getValue();
						isCaptchaValid.append("1");
					}
					else
					{
						isCaptchaValid.append("0");
					}
				}
			}
			catch (ClientProtocolException clientProtocolException)
			{
				Log.e(tag, clientProtocolException.getMessage());				
			}
			catch (IOException ioException)
			{
				Log.e(tag, ioException.getMessage());			
			}
			
			return isCaptchaValid.toString();
		}
		
		protected void onPostExecute(String result)
		{
			captchaValidText.setVisibility(TextView.VISIBLE);
			captchaDetailText.setVisibility(TextView.VISIBLE);
			if(result.equals("1"))
			{
				captchaValidText.setText(R.string.captcha_valid_text);
				captchaDetailText.setText(mCaptchaId.split(";")[1]);
				validCaptcha = true;
			}
			else
			{
				captchaValidText.setText(R.string.captcha_invalid_text);
				validCaptcha = false;
			}
		}
	}
}