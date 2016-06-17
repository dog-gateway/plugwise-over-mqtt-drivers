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
package org.doggateway.drivers.plugwise.mqtt.network.tasks;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.CircleDiscoveryListener;

/**
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class DeviceDiscoveryDeliveryTask implements Runnable
{
	// a PlugwiseMQTTDeviceInfo object representing the discovered device
	private PlugwiseMQTTDeviceInfo discoveredDeviceInfo;

	// the listeners to which deliver information about the discovered device
	private CircleDiscoveryListener targetListener;

	/**
	 * Creates a new instance of delivery task, aimed at providing the given
	 * device information to the given listener
	 * 
	 * @param discoveredDeviceInfo
	 * @param targetListener
	 */
	public DeviceDiscoveryDeliveryTask(
			PlugwiseMQTTDeviceInfo discoveredDeviceInfo,
			CircleDiscoveryListener targetListener)
	{
		super();
		this.discoveredDeviceInfo = discoveredDeviceInfo;
		this.targetListener = targetListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		// deliver data
		this.targetListener.circleDeviceDiscovered(discoveredDeviceInfo);
	}

}
