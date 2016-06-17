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
package org.doggateway.drivers.plugwise.mqtt.network.interfaces;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;

/**
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public interface CircleDiscoveryListener
{
	/**
	 * Handle discovery of a new Plugwise circle device represented by the given
	 * {@link PlugwiseMQTTDeviceInfo}.
	 * 
	 * @param devInfo
	 *            A device info instance representing the discovered device.
	 */
	public void circleDeviceDiscovered(PlugwiseMQTTDeviceInfo devInfo);
}
