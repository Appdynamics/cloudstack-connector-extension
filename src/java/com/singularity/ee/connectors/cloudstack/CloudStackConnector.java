package com.singularity.ee.connectors.cloudstack;

import static com.singularity.ee.controller.KAppServerConstants.CONTROLLER_SERVICES_HOST_NAME_PROPERTY_KEY;
import static com.singularity.ee.controller.KAppServerConstants.CONTROLLER_SERVICES_PORT_PROPERTY_KEY;
import static com.singularity.ee.controller.KAppServerConstants.DEFAULT_CONTROLLER_PORT_VALUE;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.singularity.ee.agent.resolver.AgentResolutionEncoder;
import com.singularity.ee.cloudstack.CloudStackClient;
import com.singularity.ee.cloudstack.CreateServerOptions;
import com.singularity.ee.cloudstack.Server;
import com.singularity.ee.cloudstack.ServerStatus;
import com.singularity.ee.connectors.api.ConnectorException;
import com.singularity.ee.connectors.api.IConnector;
import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.api.InvalidObjectException;
import com.singularity.ee.connectors.entity.api.IAccount;
import com.singularity.ee.connectors.entity.api.IComputeCenter;
import com.singularity.ee.connectors.entity.api.IImage;
import com.singularity.ee.connectors.entity.api.IImageStore;
import com.singularity.ee.connectors.entity.api.IMachine;
import com.singularity.ee.connectors.entity.api.IMachineDescriptor;
import com.singularity.ee.connectors.entity.api.IProperty;
import com.singularity.ee.connectors.entity.api.MachineState;

public class CloudStackConnector implements IConnector
{
	private IControllerServices controllerServices;
	private static final Object counterLock = new Object();
	private static volatile long counter;
	private final Logger logger = Logger.getLogger(CloudStackConnector.class.getName());

	@Override
	public IMachine createMachine(IComputeCenter computeCenter, IImage image, IMachineDescriptor machineDescriptor)
			throws InvalidObjectException, ConnectorException
	{

		boolean succeeded = false;
		Exception createFailureRootCause = null;
		Server server = null;

		CloudStackClient client = ClientLocator.getInstance().getClient(computeCenter.getProperties(), controllerServices);

		try
		{
			String controllerHost = System.getProperty(CONTROLLER_SERVICES_HOST_NAME_PROPERTY_KEY, InetAddress
					.getLocalHost().getHostAddress());

			int controllerPort = Integer.getInteger(CONTROLLER_SERVICES_PORT_PROPERTY_KEY, DEFAULT_CONTROLLER_PORT_VALUE);

			IAccount account = computeCenter.getAccount();
			String accountName = account.getName();
			String accessKey = account.getAccessKey();

			AgentResolutionEncoder agentResolutionEncoder = new AgentResolutionEncoder(controllerHost, controllerPort,
					accountName, accessKey);

			server = createServer(agentResolutionEncoder, image,  machineDescriptor.getProperties(), client);

			IMachine machine = controllerServices.createMachineInstance(server.getId(),
					agentResolutionEncoder.getUniqueHostIdentifier(), computeCenter, machineDescriptor, image,
					getAgentPort());

			logger.info(computeCenter.getType().getName() + " instance created; machine id:" + machine.getId()
					+ "; server id:" + server.getId());

			succeeded = true;
			return machine;
		}
		catch (Exception e)
		{
			createFailureRootCause = e;
			throw new ConnectorException(e.getMessage(), e);
		}
		finally
		{
			if (!succeeded && server != null)
			{
				try
				{
					client.terminateVirtualMachine(server.getId());
				}
				catch (Exception e)
				{
					throw new ConnectorException("Machine create failed, but terminate failed as well! "
							+ "We have an orphan " + computeCenter.getType().getName() + " instance with id: "
							+ server.getId() + " that must be shut down manually. Root cause for machine "
							+ "create failure is following: ", createFailureRootCause);
				}
			}
		}

	}

	protected Server createServer(AgentResolutionEncoder agentResolutionEncoder, IImage image, IProperty[] machineProps,
			CloudStackClient client) throws Exception
	{
		String serviceOfferingId = Utils.getServiceOfferingId(machineProps, controllerServices);
		String templateId = Utils.getTemplateId(image.getProperties(), controllerServices);
		String zoneId = Utils.getZoneId(machineProps, controllerServices);
		String account = Utils.getAccount(machineProps, controllerServices);
		String diskOfferingId = Utils.getDiskOfferingId(machineProps, controllerServices);
		String domainId = Utils.getDomainId(machineProps, controllerServices);
		String name = Utils.getName(machineProps, controllerServices);
		String networkIds = Utils.getNetworkid(machineProps, controllerServices);
		String group = Utils.getGroup(machineProps, controllerServices);
		String hostId = Utils.getHostId(machineProps, controllerServices);
		String hypervisor = Utils.getHypervisor(machineProps, controllerServices);
		String keyPair = Utils.getKeypair(machineProps, controllerServices);
		String securityGroupNames = Utils.getSecurityGroupNames(machineProps, controllerServices);
		String securityGroupIds = Utils.getSecurityGroupId(machineProps, controllerServices);
		String size = Utils.getSize(machineProps, controllerServices);
		String ipAddress= Utils.getIpAddress(machineProps, controllerServices);
		String keyboard= Utils.getKeyboard(machineProps, controllerServices);
		String projectId = Utils.getProjectId(machineProps, controllerServices);
		Map<String, String> ipToNetworkList = Utils.getIpToNetworkList(machineProps, controllerServices);
		
		Server server = null;

		long count;

		synchronized (counterLock)
		{
			count = counter++;
		}
		
		CreateServerOptions options = new CreateServerOptions();
		options.setAccount(account);
		options.setDomainId(domainId);
		options.setName(name);
		options.setIpToNetworkList(ipToNetworkList);
		options.setDiskOfferingId(diskOfferingId);
		options.setDisplayName("AD_" + System.currentTimeMillis() + count);
		options.setGroup(group);
		options.setHostId(hostId);
		options.setHypervisor(hypervisor);
		options.setIpAddress(ipAddress);
		options.setKeyPair(keyPair);
		options.setSize(size);
		options.setUserData(agentResolutionEncoder.encodeAgentResolutionInfo());
		options.setNetworkId(networkIds);
		options.setSecurityGroupNames(securityGroupNames);
		options.setSecurityGroupIds(securityGroupIds);
		options.setKeyboard(keyboard);
		options.setProjectId(projectId);
	
		server = client.deployVirtualMachine(serviceOfferingId, templateId, zoneId, options);

		logger.info("Cloudstack Instance created with instance id: " + server.getId());
		
		return server;
	}

	public String getIpAddress(IMachine machine, CloudStackClient client)
			throws Exception
	{
		List<String> ipAddress = client.listIpAddress(machine.getName());
		return ipAddress.iterator().next();

	}

	@Override
	public void refreshMachineState(IMachine machine) throws InvalidObjectException, ConnectorException
	{
		try
		{
			String serverId = machine.getName();
			MachineState currentState = machine.getState();

			if (currentState == MachineState.STARTING)
			{
				CloudStackClient client = ClientLocator.getInstance().getClient(machine.getComputeCenter().getProperties(),
						controllerServices);

				ServerStatus serverStatus = client.getServerStatus(serverId);

				if (serverStatus == null)
				{
					machine.setState(MachineState.STOPPED);
				}
				else if (serverStatus == ServerStatus.STOPPED)
				{
					// machine is created. Power on the machine
					client.rebootVirtualMachine(machine.getName());
				}
				else if (serverStatus == ServerStatus.RUNNING)
				{

					String ipAddress = getIpAddress(machine, client);

					String currentIpAddress = machine.getIpAddress();

					if (ipAddress != null && !currentIpAddress.equals(ipAddress))
					{
						machine.setIpAddress(ipAddress);
					}

					machine.setState(MachineState.STARTED);
				}
				else if(serverStatus == ServerStatus.ERROR || serverStatus == ServerStatus.EXPUNGING)
				{
					machine.setState(MachineState.NOT_RESPONDING);
				}
				else if(serverStatus == ServerStatus.DESTROYED)
				{
					machine.setState(MachineState.STOPPED);
				}
			}
			else if (currentState == MachineState.STOPPING)
			{
				CloudStackClient client = ClientLocator.getInstance().getClient(machine.getComputeCenter().getProperties(),
						controllerServices);

				ServerStatus serverStatus = null;

				try
				{
					serverStatus = client.getServerStatus(serverId);
				}
				catch (Exception e)
				{
					machine.setState(MachineState.STOPPED);
					logger.log(Level.FINE, "Exception occurred while checking machine "
							+ "state on STOPPING instance. Assume instance is STOPPED.", e);
				}

				if (serverStatus == null)
				{
					machine.setState(MachineState.STOPPED);
				}
				else if(serverStatus == ServerStatus.ERROR || serverStatus == ServerStatus.EXPUNGING)
				{
					machine.setState(MachineState.NOT_RESPONDING);
				}
				else if (serverStatus == ServerStatus.STOPPED || serverStatus == ServerStatus.DESTROYED)
				{
					machine.setState(MachineState.STOPPED);
				}
				else if (serverStatus == ServerStatus.UNKOWN)
				{
					machine.setState(MachineState.STOPPED);
				}
			}
		}
		catch (Exception e)
		{
			throw new ConnectorException("Unable to retrieve Server Status. Error:" + e.getMessage(), e);
		}
	}

	@Override
	public void terminateMachine(IMachine machine) throws InvalidObjectException, ConnectorException
	{
		try
		{
			CloudStackClient client = ClientLocator.getInstance().getClient(machine.getComputeCenter().getProperties(),
					controllerServices);

			client.terminateVirtualMachine(machine.getName());
			
			
		}
		catch (Exception e)
		{
			throw new ConnectorException("Error Terminating " + machine.getComputeCenter().getType().getName()
					+ " Instance Id:" + machine.getId() + ". Error:" + e.getMessage(),e);
		}
	}

	@Override
	public void restartMachine(IMachine machine) throws InvalidObjectException, ConnectorException
	{
		try
		{
			CloudStackClient client = ClientLocator.getInstance().getClient(machine.getComputeCenter().getProperties(),
					controllerServices);

			client.rebootVirtualMachine(machine.getName());
		}
		catch (Exception e)
		{
			throw new ConnectorException("Error Rebooting " + machine.getComputeCenter().getType().getName()
					+ " Instance Id:" + machine.getName() + ". Error:" + e.getMessage(), e);
		}
	}

	@Override
	public void validate(IComputeCenter computeCenter) throws InvalidObjectException, ConnectorException
	{
		try
		{
			String apiKey = Utils.getApiKey(computeCenter.getProperties(), controllerServices);
			String secretKey = Utils.getSecretKey(computeCenter.getProperties(), controllerServices);
			String endPoint = Utils.getEndPoint(computeCenter.getProperties(), controllerServices);
			logger.info(apiKey + " " + secretKey + " " + endPoint);
			ClientLocator.getInstance().getClient(computeCenter.getProperties(), controllerServices);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "", e);

			throw new InvalidObjectException("Failed to validate the " + computeCenter.getType().getName()
					+ " connector properties. Error:" + e.getMessage(), e);
		}
	}

	@Override
	public void validate(IImageStore imageStore) throws InvalidObjectException, ConnectorException
	{
		try
		{
			String apiKey = Utils.getApiKey(imageStore.getProperties(), controllerServices);
			String secretKey = Utils.getSecretKey(imageStore.getProperties(), controllerServices);
			String endPoint = Utils.getEndPoint(imageStore.getProperties(), controllerServices);
			logger.info(apiKey + " " + secretKey + " " + endPoint);
			
			ClientLocator.getInstance().getClient(imageStore.getProperties(), controllerServices);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "", e);

			throw new InvalidObjectException("Failed to validate the " + imageStore.getImageStoreType().getName()
					+ " store properties. Error:" + e.getMessage(), e);
		}
	}

	@Override
	public void setControllerServices(IControllerServices controllerServices)
	{
		this.controllerServices = controllerServices;
	}

	@Override
	public int getAgentPort()
	{
		return controllerServices.getDefaultAgentPort();
	}

	@Override
	public void validate(IImage image) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void unconfigure(IComputeCenter computeCenter) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void unconfigure(IImageStore imageStore) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void unconfigure(IImage image) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void configure(IComputeCenter computeCenter) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void configure(IImageStore imageStore) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void configure(IImage image) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void deleteImage(IImage image) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

	@Override
	public void refreshImageState(IImage image) throws InvalidObjectException, ConnectorException
	{
		// do nothing
	}

}
