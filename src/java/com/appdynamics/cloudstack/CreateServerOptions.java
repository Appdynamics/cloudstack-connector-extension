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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class CreateServerOptions
{
	String diskOfferingId;
	String displayName;
	String group;
	String hypervisor;
	String ipAddress;
	String keyPair;
	String hostId;
	String account;
	String securityGroupNames;
	String securityGroupIds;
	byte[] userData;
	String size;
	String domainId;
	String name;
	String netWorkIds;
	String projectId;
	String keyboard;
	Map<String, String> ipToNetworklist = new HashMap<String, String>();

	
	public Map<String, String> getIpToNetworkList()
	{
		return ipToNetworklist;
	}

	public String getProjectId()
	{
		return projectId;
	}
	
	
	public String getKeyboard()
	{
		return keyboard;
	}
	
	public String getDomainId()
	{
		return domainId;
	}

	public String getAccount()
	{
		return account;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public String getDiskOfferingId()
	{
		return diskOfferingId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getGroup()
	{
		return group;
	}

	public String getHypervisor()
	{
		return hypervisor;
	}

	public String getKeyPair()
	{
		return keyPair;
	}

	public String getHostId()
	{
		return hostId;
	}

	public String getName()
	{
		return name;
	}

	public String getSecurityGroupIds()
	{
		return securityGroupIds;
	}

	public String getSecurityGroupNames()
	{
		return securityGroupNames;
	}

	public String getUserData()
	{
		if (userData == null)
		{
			return null;
		}
		else
		{
			return new String(userData);
		}
	}

	public String getSize()
	{
		return size;
	}

	public String getNetWorkIds()
	{
		return netWorkIds;
	}

	public void setIpToNetworkList(Map<String, String> ipToNetworkList)
	{
		this.ipToNetworklist = ipToNetworkList;
	}
	
	public void setDomainId(String domainId)
	{
		this.domainId = domainId;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public void setDiskOfferingId(String diskOfferingId)
	{
		this.diskOfferingId = diskOfferingId;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public void setHypervisor(String hypervisor)
	{
		this.hypervisor = hypervisor;
	}

	public void setKeyboard(String keyBoard)
	{
		this.keyboard = keyBoard;
	}
	
	public void setKeyPair(String keyPair)
	{
		this.keyPair = keyPair;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setHostId(String hostId)
	{
		this.hostId = hostId;
	}

	public void setSecurityGroupIds(String securityGroupIds)
	{
		this.securityGroupIds = securityGroupIds;
	}

	public void setSecurityGroupNames(String securityGroupNames)
	{
		this.securityGroupNames = securityGroupNames;
	}

	public void setUserData(String userData)
	{
		this.userData = Base64.encodeBase64(userData.getBytes());
	}

	public void setUserData(byte[] userData)
	{
		this.userData = Base64.encodeBase64(userData);
	}

	public void setSize(String size)
	{
		this.size = size;
	}

	public void setNetworkId(String netWorkId)
	{
		this.netWorkIds = netWorkId;
	}
	
	public void setProjectId(String projectId)
	{
		this.projectId = projectId;
	}

}
