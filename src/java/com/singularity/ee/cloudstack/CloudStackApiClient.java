package com.singularity.ee.cloudstack;

import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.singularity.ee.util.JavaLogging.JavaLogger;

class CloudStackApiClient
{
	private Map<String, String> params = new HashMap<String, String>();
	private String command;
	private String apiKey;
	private String secretKey;
	private String apiUrl;

	public CloudStackApiClient(String command, String apiKey, String secretKey, String apiUrl)
	{
		this.command = command;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.apiUrl = apiUrl;
	}

	public void addParam(String field, String value) throws Exception
	{
		if (value != null && value.trim().length() != 0)
		{
			params.put(field, encodeUrl(value));
		}
	}

	public String encodeUrl(String value) throws Exception
	{
		try
		{
			if (value != null)
			{
				return URLEncoder.encode(value.trim(), "UTF-8").replaceAll("\\+", "%20");
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private static final JavaLogger logger = new JavaLogger(Logger.getLogger(CloudStackClient.class.getName()));

	public String execute() throws Exception
	{

		HttpClient client = null;
		GetMethod get = null;

		try
		{

			String requestUrl = generateRequestUrl();
			System.out.println(requestUrl);
			client = new HttpClient();
			get = new GetMethod(requestUrl);
			client.executeMethod(get);

			return get.getResponseBodyAsString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			get.releaseConnection();
		}

	}

	public String generateRequestUrl() throws Exception
	{
		int i = 0;
		StringBuilder commandString = new StringBuilder();

		commandString.append("apiKey=").append(apiKey);
		commandString.append("&").append("command=").append(command);

		Map<String, String> paramsLowerCase = new HashMap<String, String>();
		paramsLowerCase.put("apiKey".toLowerCase(), apiKey.toLowerCase());
		paramsLowerCase.put("command".toLowerCase(), command.toLowerCase());

		List<String> fields = new ArrayList<String>();
		fields.add("apiKey".toLowerCase());
		fields.add("command".toLowerCase());

		for (Map.Entry<String, String> e : params.entrySet())
		{
			commandString.append("&");

			String field = e.getKey();
			String value = e.getValue();

			commandString.append(encodeUrl(field)).append("=").append((value));

			paramsLowerCase.put(field.toLowerCase(), value.toLowerCase());
			fields.add(field.toLowerCase());
			i++;
		}

		Collections.sort(fields);

		StringBuilder signatureData = new StringBuilder();

		for (String s : fields)
		{
			if (signatureData.length() != 0)
			{
				signatureData.append("&");
			}

			signatureData.append(s).append("=").append(paramsLowerCase.get(s));
		}

		String signature = calculateRFC2104HMAC(signatureData.toString().toLowerCase(), secretKey);

		return apiUrl + commandString.toString() + "&signature=" + encodeUrl(signature);
	}

	public String calculateRFC2104HMAC(String data, String key) throws Exception
	{
		String result;
		try
		{

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			// result = Base64.encode(rawHmac);
			result = new String(Base64.encodeBase64(rawHmac));

			return result.trim();
		}
		catch (Exception e)
		{
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}

		// return encodeUrl(result);
	}
}
