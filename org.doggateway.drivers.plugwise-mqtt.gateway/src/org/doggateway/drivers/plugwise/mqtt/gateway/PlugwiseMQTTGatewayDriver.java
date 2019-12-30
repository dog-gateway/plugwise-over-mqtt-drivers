/*
 * Dog - Plugwise Gateway Driver
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
package org.doggateway.drivers.plugwise.mqtt.gateway;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDriverInfo;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Device;
import org.osgi.service.device.Driver;

import it.polito.elite.dog.core.devicefactory.api.DeviceFactory;
import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceCostants;
import it.polito.elite.dog.core.library.model.devicecategory.PlugwiseGateway;
import it.polito.elite.dog.core.library.util.LogHelper;

/**
 * @author bonino
 *
 */
public class PlugwiseMQTTGatewayDriver implements Driver
{
	// The OSGi framework context
		protected BundleContext context;

		// System logger
		LogHelper logger;

		// String identifier for driver id
		public static final String DRIVER_ID = "org.doggateway.drivers.plugwise.mqtt.gateway";

		// a reference to the network driver (currently not used by this driver
		// version, in the future might be used to implement gateway-specific
		// functionalities.
		private AtomicReference<PlugwiseMQTTNetwork> network;

		// a reference to the device factory service used to handle run-time
		// creation of devices in dog as response to network association.
		private AtomicReference<DeviceFactory> deviceFactory;

		// the registration object needed to handle the life span of this bundle in
		// the OSGi framework (it is a ServiceRegistration object for use by the
		// bundle registering the service to update the service's properties or to
		// unregister the service).
		private ServiceRegistration<?> regDriver;

		// register this driver as a gateway used by device-specific drivers
		// not needed in this version as the enocean gateway is only one at time
		// (serial port)
		// might be different in the future.
		private ServiceRegistration<?> regPlugwiseMQTTGateway;

		// the set of currently connected gateways... indexed by their ids
		// for future use, now hosts only a single value
		private ConcurrentHashMap<String, PlugwiseMQTTGatewayDriverInstance> connectedGateways;

		// the dictionary containing the current snapshot of the driver
		// configuration, i.e., the list of supported devices and the corresponding
		// DogOnt types.
		private Set<PlugwiseMQTTDriverInfo> activeDriverDetails;


		/**
		 * The class constructor, initializes needed data structures, especially
		 * AtomicReferences to needed services.
		 */
		public PlugwiseMQTTGatewayDriver()
		{
			// initialize the map of connected gateways
			this.connectedGateways = new ConcurrentHashMap<String, PlugwiseMQTTGatewayDriverInstance>();

			// initialize the supported devices map
			this.activeDriverDetails = Collections
					.synchronizedSet(new HashSet<PlugwiseMQTTDriverInfo>());

			// initialize the network driver reference
			this.network = new AtomicReference<PlugwiseMQTTNetwork>();
			
			// initialize the device factory reference
			this.deviceFactory = new AtomicReference<DeviceFactory>();
		}

		/**
		 * Handle the bundle activation
		 */
		public void activate(BundleContext bundleContext)
		{
			// store the context
			context = bundleContext;

			// init the logger
			logger = new LogHelper(context);

			this.registerDriver();
		}

		/**
		 * 
		 */
		public void deactivate()
		{
			// remove the service from the OSGi framework
			this.unRegister();
		}

		/**
		 * Handle the bundle de-activation
		 */
		protected void unRegister()
		{
			// un-registers this driver
			if (this.regDriver != null)
			{
				this.regDriver.unregister();
				this.regDriver = null;
			}

			// un-register the gateway service
			if (this.regPlugwiseMQTTGateway != null)
			{
				this.regPlugwiseMQTTGateway.unregister();
				this.regPlugwiseMQTTGateway = null;
			}
		}

		/**
		 * Registers this driver in the OSGi framework, making its services
		 * available to all the other bundles living in the same or in connected
		 * frameworks.
		 */
		private void registerDriver()
		{
			if ((network.get() != null) && (this.context != null)
					&& (this.regDriver == null))
			{
				Hashtable<String, Object> propDriver = new Hashtable<String, Object>();
				propDriver.put(DeviceCostants.DRIVER_ID, DRIVER_ID);
				propDriver.put(DeviceCostants.GATEWAY_COUNT,
						this.connectedGateways.size());

				this.regDriver = this.context.registerService(
						Driver.class.getName(), this, propDriver);
				this.regPlugwiseMQTTGateway = this.context.registerService(
						PlugwiseMQTTGatewayDriver.class.getName(), this, null);
			}
		}

		// --------------- Handling service binding ---------------------

		/**
		 * 
		 * @param networkDriver
		 */
		public void addedNetworkDriver(PlugwiseMQTTNetwork networkDriver)
		{
			this.network.set(networkDriver);
		}

		/**
		 * 
		 * @param networkDriver
		 */
		public void removedNetworkDriver(PlugwiseMQTTNetwork networkDriver)
		{
			if (this.network.compareAndSet(networkDriver, null))
				// unregisters this driver from the OSGi framework
				unRegister();
		}

		/**
		 * 
		 * @param deviceFactory
		 */
		public void addedDeviceFactory(DeviceFactory deviceFactory)
		{
			this.deviceFactory.set(deviceFactory);

		}

		/**
		 * 
		 * @param deviceFactory
		 */
		public void removedDeviceFactory(DeviceFactory deviceFactory)
		{
			if (this.deviceFactory.compareAndSet(deviceFactory, null))
				// unregisters this driver from the OSGi framework
				unRegister();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public int match(ServiceReference reference) throws Exception
		{
			int matchValue = Device.MATCH_NONE;

			// get the given device category
			String deviceCategory = (String) reference
					.getProperty(DeviceCostants.DEVICE_CATEGORY);

			// get the given device manufacturer
			String manufacturer = (String) reference
					.getProperty(DeviceCostants.MANUFACTURER);

			// compute the matching score between the given device and this driver
			if (deviceCategory != null)
			{
				if (manufacturer != null
						&& manufacturer.equals(PlugwiseMQTTInfo.MANUFACTURER)
						&& (deviceCategory.equals(PlugwiseGateway.class.getName())))
				{
					matchValue = PlugwiseGateway.MATCH_MANUFACTURER
							+ PlugwiseGateway.MATCH_TYPE;
				}

			}
			return matchValue;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public String attach(ServiceReference reference) throws Exception
		{
			if (this.regDriver != null)
			{
				// get the controllable device to attach
				@SuppressWarnings("unchecked")
				ControllableDevice device = (ControllableDevice) this.context
						.getService(reference);

				// get the device id
				String deviceId = device.getDeviceId();

				if (!this.isGatewayAvailable(deviceId))
				{
					// create a new driver instance
					PlugwiseMQTTGatewayDriverInstance driverInstance = new PlugwiseMQTTGatewayDriverInstance(
							this.network.get(), this.deviceFactory.get(),
							this.activeDriverDetails, device, this.context);

					// associate device and driver
					device.setDriver(driverInstance);

					// store the just created gateway instance
					synchronized (connectedGateways)
					{
						// store a reference to the gateway driver
						connectedGateways.put(device.getDeviceId(), driverInstance);
					}

					// modify the service description causing a forcing the
					// framework to send a modified service notification
					final Hashtable<String, Object> propDriver = new Hashtable<String, Object>();
					propDriver.put(DeviceCostants.DRIVER_ID, DRIVER_ID);
					propDriver.put(DeviceCostants.GATEWAY_COUNT,
							connectedGateways.size());

					this.regDriver.setProperties(propDriver);
				}
			}

			return null;
		}

		/**
		 * check if the gateway identified by the given gateway id is currently
		 * registered with this driver
		 * 
		 * @param gatewayId
		 * @return true if the gateway corresponding to the given id is already
		 *         registered, false otherwise.
		 */
		public boolean isGatewayAvailable(String gatewayId)
		{
			return connectedGateways.containsKey(gatewayId);
		}

		/**
		 * Returns a live reference to the specific network driver service to which
		 * this {@link ZWaveGatewayDriver} is connected.
		 * 
		 * @return
		 */
		public PlugwiseMQTTNetwork getNetwork()
		{
			return network.get();
		}

		/**
		 * Registers the capabilities of an active driver.
		 * 
		 * @param the
		 *            {@link ZigBeeDriverInfo} instance describing the driver
		 */
		public void addActiveDriverDetails(PlugwiseMQTTDriverInfo driverInfo)
		{
			this.activeDriverDetails.add(driverInfo);
		}

		/**
		 * Unregisters the capabilities of an active driver
		 */
		public void removeActiveDriverDetails(PlugwiseMQTTDriverInfo driverInfo)
		{
			this.activeDriverDetails.remove(driverInfo);
		}


}
