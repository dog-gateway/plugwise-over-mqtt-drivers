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
package org.doggateway.drivers.plugwise.mqtt.network;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDriverInfo;

/**
 * abstract class specifying the minimum set of information that shall be made
 * available by any PlugwiseMQTT driver
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public abstract class PlugwiseMQTTDriver
{
	// the driver information object describing peculiar driver features, e.g.,
	// supported classes
	protected PlugwiseMQTTDriverInfo theDriverInfo;

	/**
	 * Gets the driver info object associated to this driver
	 * @return the theDriverInfo
	 */
	public PlugwiseMQTTDriverInfo getTheDriverInfo()
	{
		return theDriverInfo;
	}

	/**
	 * Sets the driver info object for this driver
	 * @param theDriverInfo the theDriverInfo to set
	 */
	public void setTheDriverInfo(PlugwiseMQTTDriverInfo theDriverInfo)
	{
		this.theDriverInfo = theDriverInfo;
	}
	
	
}
