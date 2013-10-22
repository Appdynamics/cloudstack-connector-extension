package com.singularity.ee.connectors.cloudstack;

import java.util.HashMap;
import java.util.Map;

import com.singularity.ee.cloudstack.CloudStackClient;
import com.singularity.ee.connectors.api.ConnectorException;
import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.entity.api.IProperty;

public class ClientLocator
{
	private static final ClientLocator INSTANCE = new ClientLocator();

	private final Map<String, CloudStackClient> cloudStackClients = new HashMap<String, CloudStackClient>();

	private final Object connectorLock = new Object();

	public static ClientLocator getInstance()
	{
		return INSTANCE;
	}

	public CloudStackClient getClient(IProperty[] props, IControllerServices controllerServices) throws ConnectorException
	{
		String apiKey = Utils.getApiKey(props, controllerServices);
		String secretKey = Utils.getSecretKey(props, controllerServices);
		String endPoint = Utils.getEndPoint(props, controllerServices);

		return getClient(secretKey, apiKey, endPoint);
	}

	public CloudStackClient getClient(String secretkey, String apiKey, String endPoint) throws ConnectorException
	{
		synchronized (connectorLock)
		{
			String identifier = secretkey + apiKey + endPoint;

			if (cloudStackClients.containsKey(identifier))
			{
				return cloudStackClients.get(identifier);
			}

			// for now there are only two connectors, Rackspace and HP; neither uses password for authentication.
			CloudStackClient client = new CloudStackClient(apiKey, secretkey, endPoint);

			if (!client.authenticate())
			{
				throw new ConnectorException("Invalid authentication credentials.");
			}

			cloudStackClients.put(identifier, client);

			return client;

		}

	}

}
