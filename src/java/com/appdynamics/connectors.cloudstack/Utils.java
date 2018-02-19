/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.connectors.cloudstack;

import java.util.HashMap;
import java.util.Map;

import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.entity.api.IProperty;

public class Utils
{

	public static final String API_KEY = "Api Key";
	public static final String SECRET_KEY = "Secret Key";
	public static final String END_POINT = "Client End Point Url";

	public static final String SERVICE_OFFERING_ID = "Service Offering Id";
	public static final String ZONE_ID = "Zone Id";
	public static final String TEMPLATE_ID = "Template Id";
	public static final String NETWORK_ID = "Network Ids";
	public static final String DISK_OFFERING_ID = "Disk Offering Id";
	public static final String GROUP = "Group";

	public static final String HOST_ID = "Host Id";
	public static final String HYPERVISOR = "Hypervisor";
	public static final String KEYPAIR = "Key Pair";
	public static final String SECURITY_GROUP_NAMES = "Security Group Names";
	public static final String SECURITY_GROUP_ID = "Security Group Ids";
	public static final String SIZE = "Size";
	public static final String IP_ADDRESS = "Ip Address";
	public static final String DOMAIN_ID = "Domain Id";
	public static final String ACCOUNT = "Account";
	public static final String NAME = "Name";

	public static final String KEYBOARD = "Keyboard";
	public static final String PROJECT_ID = "Project Id";
	public static final String IP_TO_NETWORKLIST = "Ip To Network List";

	public static Map<String, String> getIpToNetworkList(IProperty[] properties, IControllerServices controllerServices)
			throws Exception
	{

		String value = getValue(controllerServices.getStringPropertyValueByName(properties, IP_TO_NETWORKLIST));

		Map<String, String> ipToNetworkList = new HashMap<String, String>();

		if (value != null)
		{

			String[] list = value.split(",|;");

			if ((list.length % 2) != 0)
			{
				throw new Exception("Error, Ip to Network List must consist of ip and networkid value pairs");
			}

			int i = 0;

			while (i < list.length)
			{
				ipToNetworkList.put(list[i], list[++i]);
				i++;
			}
		}
		return ipToNetworkList;
	}

	public static String getDiskOfferingId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, DISK_OFFERING_ID));
	}

	public static String getProjectId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, PROJECT_ID));
	}

	public static String getKeyboard(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, KEYBOARD));
	}

	public static String getIpAddress(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, IP_ADDRESS));
	}

	public static String getGroup(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, GROUP));
	}

	public static String getHostId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, HOST_ID));
	}

	public static String getHypervisor(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, HYPERVISOR));
	}

	public static String getKeypair(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, KEYPAIR));
	}

	public static String getSecurityGroupNames(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, SECURITY_GROUP_NAMES));
	}

	public static String getSecurityGroupId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, SECURITY_GROUP_ID));
	}

	public static String getSize(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, SIZE));
	}

	public static String getApiKey(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, API_KEY));
	}

	public static String getSecretKey(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, SECRET_KEY));
	}

	public static String getEndPoint(IProperty[] properties, IControllerServices controllerServices)
	{
		String url = getValue(controllerServices.getStringPropertyValueByName(properties, END_POINT));

		if (url.substring(url.length() - 1).equals("/"))
		{
			return url.substring(0, url.length() - 1);
		}
		else
		{
			return url;
		}
	}

	public static String getAccount(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, ACCOUNT));
	}

	public static String getDomainId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, DOMAIN_ID));
	}

	public static String getName(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, ACCOUNT));
	}

	public static String getServiceOfferingId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, SERVICE_OFFERING_ID));
	}

	public static String getTemplateId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, TEMPLATE_ID));
	}

	public static String getZoneId(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, ZONE_ID));
	}

	public static String getNetworkid(IProperty[] properties, IControllerServices controllerServices)
	{
		return getValue(controllerServices.getStringPropertyValueByName(properties, NETWORK_ID));
	}

	private static String getValue(String value)
	{
		return (value == null || value.trim().length() == 0) ? null : value.trim();
	}

}
