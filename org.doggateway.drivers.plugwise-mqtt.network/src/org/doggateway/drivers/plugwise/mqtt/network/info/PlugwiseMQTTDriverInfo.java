/*
 * Dog - Plugwise Network Driver
 * 
 * Copyright 2016 Dario Bonino 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.doggateway.drivers.plugwise.mqtt.network.info;

/**
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class PlugwiseMQTTDriverInfo
{
	// the driver name
	private String driverName;

	// the driver version;
	private String driverVersion;

	// the "main" class
	private String mainDeviceClass;
	
	// the supported plugwise device type
	private String type;

	/**
	 * 
	 */
	public PlugwiseMQTTDriverInfo()
	{
		// Intentionally left empty
	}

	/**
	 * @return the driverName
	 */
	public String getDriverName()
	{
		return driverName;
	}

	/**
	 * @param driverName the driverName to set
	 */
	public void setDriverName(String driverName)
	{
		this.driverName = driverName;
	}

	/**
	 * @return the driverVersion
	 */
	public String getDriverVersion()
	{
		return driverVersion;
	}

	/**
	 * @param driverVersion the driverVersion to set
	 */
	public void setDriverVersion(String driverVersion)
	{
		this.driverVersion = driverVersion;
	}

	/**
	 * @return the mainDeviceClass
	 */
	public String getMainDeviceClass()
	{
		return mainDeviceClass;
	}

	/**
	 * @param mainDeviceClass the mainDeviceClass to set
	 */
	public void setMainDeviceClass(String mainDeviceClass)
	{
		this.mainDeviceClass = mainDeviceClass;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}
	
}
