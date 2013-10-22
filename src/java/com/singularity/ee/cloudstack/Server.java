package com.singularity.ee.cloudstack;

//simple java bean used to relay info back to the connector
public class Server
{
	private String name;
	private String id;	

	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}

}
