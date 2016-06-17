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
 * A class representing all the information needed about a plugwise device.
 * Although in general such information can be quite dense, in our case it is
 * limited to the device MAC address as it is the only information needed to
 * interface a plugwise device through the Plugwise-2-py server.
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class PlugwiseMQTTDeviceInfo
{
	// the device MAC address, actually the only information needed
	private String macAddress;

	// a flag to identify if a device is a circle (probably useless)
	private String type;

	/**
	 * Empty constructor, fulfills the bean instantiation pattern
	 */
	public PlugwiseMQTTDeviceInfo()
	{
		// initialize internal variables
		this.macAddress = "";
		this.type = "circle";
	}

	/**
	 * Creates a new device info instance representing the given values.
	 * 
	 * @param macAddress
	 *            The plugwise device mac address
	 * @param type
	 * 				The device type as a String
	 */
	public PlugwiseMQTTDeviceInfo(String macAddress, String type)
	{
		this.macAddress = macAddress;
		this.type = type;
	}

	/**
	 * Provides the device MAC address
	 * @return the macAddress
	 */
	public String getMacAddress()
	{
		return macAddress;
	}

	/**
	 * Sets the device MAC address
	 * @param macAddress
	 *            the macAddress to set
	 */
	public void setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
	}

	/**
	 * Provides the plugwise type of the device described by this info object
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the plugwise type of the device described by this info object
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	
}
