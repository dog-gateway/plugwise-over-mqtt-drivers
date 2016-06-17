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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PlugwiseMQTTMessage;

import it.polito.elite.dog.core.library.model.CNParameters;
import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.util.ElementDescription;

/**
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public abstract class PlugwiseMQTTDriverInstance
{
	// a reference to the network driver interface to allow network-level access
	// for sub-classe
	protected PlugwiseMQTTNetwork network;

	// the state of the device associated to this driver
	protected DeviceStatus currentState;

	// the device associated to the driver
	protected ControllableDevice device;

	// the set of notifications associated to the driver
	protected HashMap<String, CNParameters> notifications;

	// the set of commands associated to the driver
	protected HashMap<String, CNParameters> commands;
	
	// the managed device
	protected PlugwiseMQTTDeviceInfo theManagedDevice;
	
	/**
	 * Class constructor, takes a reference to the network driver to exploit for
	 * communication and to the Dog device instance to handle.
	 * 
	 * @param network The network driver service to use
	 * @param device The Dog device to handle
	 */
	public PlugwiseMQTTDriverInstance(PlugwiseMQTTNetwork network,
			ControllableDevice device)
	{
		// store a reference to the network driver
		this.network = network;

		// store a reference to the associate device
		this.device = device;

		// initialize datastructures
		this.notifications = new HashMap<String, CNParameters>();
		this.commands = new HashMap<String, CNParameters>();

		// fill the data structures depending on the specific device
		// configuration parameters
		this.fillConfiguration();

		// call the specific configuration method, if needed
		this.specificConfiguration();

		// associate the device-specific driver to the network driver...
		this.addToNetworkDriver(this.theManagedDevice);
	}
	
	/**
	 * Extending classes might implement this method to provide driver-specific
	 * configurations to be done during the driver creation process, before
	 * associating the device-specific driver to the network driver
	 */
	protected abstract void specificConfiguration();

	/**
	 * Abstract method to be implemented by extending classes; performs the
	 * association between the device-specific driver and the underlying network
	 * driver using the appliance data as binding.
	 * 
	 * @param serial
	 */
	protected abstract void addToNetworkDriver(PlugwiseMQTTDeviceInfo device);

	private void fillConfiguration()
	{
		// gets the properties shared by almost all EnOcean devices, i.e. the
		// high-level UID, the low-level address, etc.
		// specified for the whole device
		Map<String, Set<String>> deviceConfigurationParams = this.device
				.getDeviceDescriptor().getSimpleConfigurationParams();

		// check not null
		if (deviceConfigurationParams != null)
		{
			// get the device uid
			Set<String> macAddresses = deviceConfigurationParams
					.get(PlugwiseMQTTInfo.MAC);

			// the only mandatory information is the uid
			if ((macAddresses != null) && (macAddresses.size() == 1))
			{
				// get the UID
				String mac = macAddresses.iterator().next();

				// initialize the inner PlugwiseMQTTDeviceInfo
				// TODO handle type parameter
				this.theManagedDevice = new PlugwiseMQTTDeviceInfo(mac,"circle");

			}
		}

		// gets the properties associated to each device commmand/notification,
		// if any. E.g.,
		// the unit of measure associated to meter functionalities.

		// get parameters associated to each device command (if any)
		Set<ElementDescription> commandsSpecificParameters = this.device
				.getDeviceDescriptor().getCommandSpecificParams();

		// get parameters associated to each device notification (if any)
		Set<ElementDescription> notificationsSpecificParameters = this.device
				.getDeviceDescriptor().getNotificationSpecificParams();

		// --------------- Handle command specific parameters ----------------
		for (ElementDescription parameter : commandsSpecificParameters)
		{

			// the parameter map
			Map<String, String> params = parameter.getElementParams();
			if ((params != null) && (!params.isEmpty()))
			{
				// the name of the command associated to this device...
				String commandName = params.get(PlugwiseMQTTInfo.COMMAND_NAME);

				if (commandName != null)
					// store the parameters associated to the command
					this.commands.put(commandName, new CNParameters(
							commandName, params));
			}

		}

		// --------------- Handle notification specific parameters
		// ----------------
		for (ElementDescription parameter : notificationsSpecificParameters)
		{
			// the parameter map
			Map<String, String> params = parameter.getElementParams();
			if ((params != null) && (!params.isEmpty()))
			{
				// the name of the command associated to this device...
				String notificationName = params
						.get(PlugwiseMQTTInfo.NOTIFICATION_NAME);

				if (notificationName != null)
					// store the parameters associated to the command
					this.notifications.put(notificationName, new CNParameters(
							notificationName, params));
			}

		}

	}


	public abstract void newMessageFromHouse(PlugwiseMQTTMessage message);

}
