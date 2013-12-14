/**
 * Copyright 2013 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.cloudstack;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CloudStackClient
{
	private final String DEPLOY_VIRTUALMACHINE = "deployVirtualMachine";
	private final String REBOOT_VIRTUALMACHINE = "rebootVirtualMachine";
	private final String DESTROY_VIRTUALMACHINE = "destroyVirtualMachine";
	private final String LIST_VIRTUALMACHINE = "listVirtualMachines";

	private String apiKey;
	private String secretKey;
	private String accessUrl;
	private String apiUrl;

	public CloudStackClient(String apiKey, String secretKey, String accessUrl)
	{
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.accessUrl = accessUrl;
		this.apiUrl = accessUrl + "/api?";
	}

	public boolean authenticate()
	{
		try
		{
			listServerIds();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}

	}

	// the network id must be included, the api is wrong
	public Server deployVirtualMachine(String serviceOfferingId, String templateId, String zoneId,
			CreateServerOptions options) throws Exception
	{
		CloudStackApiClient client = new CloudStackApiClient(DEPLOY_VIRTUALMACHINE, apiKey, secretKey, apiUrl);
		client.addParam("serviceofferingid", serviceOfferingId);
		client.addParam("templateid", templateId);
		client.addParam("zoneid", zoneId);
		client.addParam("account", options.getAccount());
		client.addParam("diskofferingid", options.getDiskOfferingId());
		client.addParam("displayname", options.getDisplayName());
		client.addParam("domainid", options.getDomainId());
		client.addParam("group", options.getGroup());
		client.addParam("hostid", options.getHostId());
		client.addParam("hypervisor", options.getHypervisor());
		client.addParam("ipaddress", options.getIpAddress());
		
		client.addParam("keyboard", options.getKeyboard());
		client.addParam("keypair", options.getKeyPair());
		client.addParam("name", options.getName());
		client.addParam("networkids", options.getNetWorkIds());
		client.addParam("projectid", options.getProjectId());
		client.addParam("securitygroupids", options.getSecurityGroupIds());
		client.addParam("securitygroupnames", options.getSize());
		client.addParam("userdata", options.getUserData());
		client.addParam("size", options.getSize());

		int i = 0;
		
		for(Map.Entry<String, String> entry: options.getIpToNetworkList().entrySet())
		{
			client.addParam("iptonetworklist[" + i + "].ip", entry.getKey());
			client.addParam("iptonetworklist[" + i + "].networkid", entry.getValue());
			i++;
		}
		
		String response = client.execute();

		if (containsErrorMessage(response))
		{
			throw new Exception("Error deploying virtual machine. Response: " + getErrorMessage(response));
		}

		Document doc = stringToXmlDocument(response);
		String id = doc.getElementsByTagName("id").item(0).getTextContent().trim();

		Server server = new Server();
		server.setId(id);
		return server;
	}

	public boolean rebootVirtualMachine(String id) throws Exception
	{
		CloudStackApiClient client = new CloudStackApiClient(REBOOT_VIRTUALMACHINE, apiKey, secretKey, apiUrl);
		client.addParam("id", id);
		String response = client.execute();

		Document doc = stringToXmlDocument(response);
		return doc.getElementsByTagName("jobid").getLength() > 0;
	}

	public boolean terminateVirtualMachine(String id) throws Exception
	{
		CloudStackApiClient client = new CloudStackApiClient(DESTROY_VIRTUALMACHINE, apiKey, secretKey, apiUrl);
		client.addParam("id", id);
		String response = client.execute();

		if (containsErrorMessage(response))
		{
			List<String> vms = listServerIds();

			if (vms.contains(id))
			{
				throw new Exception("Error terminating instance with id:" + id + " Response:" + getErrorMessage(response));
			}
			else
			{
				return true;
			}

		}

		Document doc = stringToXmlDocument(response);
		return doc.getElementsByTagName("jobid").getLength() > 0;
	}

	public String getServer(String id) throws Exception
	{
		CloudStackApiClient client = new CloudStackApiClient(LIST_VIRTUALMACHINE, apiKey, secretKey, apiUrl);
		client.addParam("id", id);
		String response = client.execute();

		return response;
	}

	public List<String> listServerIds() throws Exception
	{
		CloudStackApiClient client = new CloudStackApiClient(LIST_VIRTUALMACHINE, apiKey, secretKey, apiUrl);
		String response = client.execute();

		if (containsErrorMessage(response))
		{
			throw new Exception("Error retrieving Servers. Response:" + getErrorMessage(response));
		}

		Document doc = stringToXmlDocument(response);
		NodeList vms = doc.getElementsByTagName("virtualmachine");

		List<String> vmIds = new ArrayList<String>();

		for (int i = 0; i < vms.getLength(); i++)
		{
			vmIds.add(((Element) vms.item(i)).getElementsByTagName("id").item(0).getTextContent().trim());
		}

		return vmIds;

	}

	public List<String> listIpAddress(String id) throws Exception
	{
		String response = getServer(id);

		if (containsErrorMessage(response))
		{
			throw new Exception("Error retrieving ip address for instance id:" + id);
		}

		Document doc = stringToXmlDocument(response);

		NodeList list = doc.getElementsByTagName("ipaddress");
		List<String> ips = new ArrayList<String>();

		for (int i = 0; i < list.getLength(); i++)
		{
			ips.add(list.item(i).getTextContent().trim());
		}

		return ips;
	}

	public ServerStatus getServerStatus(String id) throws Exception
	{
		String response = getServer(id);

		if (containsErrorMessage(response))
		{
			if (!listServerIds().contains(id))
			{
				return null;
			}
			else
			{
				throw new Exception("Error retrieving instance status with id:" + id);
			}
		}

		Document doc = stringToXmlDocument(response);
		String status = doc.getElementsByTagName("state").item(0).getTextContent().trim();

		try
		{
			return ServerStatus.valueOf(status.toUpperCase().trim());
		}
		catch (Exception e)
		{
			return ServerStatus.UNKOWN;
		}

	}

	// error messages contains message tags in the format of i.e.<message> The resource could not be found. </message>
	public static boolean containsErrorMessage(String response) throws Exception
	{
		if (response != null && response.trim().length() != 0)
		{
			Document doc = stringToXmlDocument(response);

			return (doc.getElementsByTagName("errortext").getLength() != 0);
		}

		return true;
	}

	public static String getErrorMessage(String response) throws Exception
	{
		if (response == null || response.trim().length() == 0)
		{
			return "Null response from server";
		}

		Document doc = stringToXmlDocument(response);

		return doc.getElementsByTagName("errortext").item(0).getTextContent().trim();
	}

	public static Document stringToXmlDocument(String document) throws Exception
	{
		try
		{
			InputStream ouputStream = new ByteArrayInputStream(document.getBytes("UTF-8"));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(ouputStream);

			doc.getDocumentElement().normalize();

			return doc;
		}
		catch (Exception e)
		{
			// TODO:throw exceptions
			throw new Exception("Error reading xml response. Response Body:" + document + e);
		}
	}
}
