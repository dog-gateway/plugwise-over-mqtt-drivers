/*
 * Dog - Plugwise Device Driver
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
package org.doggateway.drivers.plugwise.mqtt.device;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.doggateway.drivers.plugwise.mqtt.gateway.PlugwiseMQTTGatewayDriver;
import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriver;
import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDriverInfo;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Device;
import org.osgi.service.device.Driver;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceCostants;
import it.polito.elite.dog.core.library.model.devicecategory.Controllable;
import it.polito.elite.dog.core.library.util.LogHelper;

/**
 * @author bonino
 *
 */
public abstract class PlugwiseMQTTDeviceDriver extends PlugwiseMQTTDriver
		implements Driver
{
	// The OSGi framework context
	protected BundleContext context;

	// System logger
	protected LogHelper logger;

	// a reference to the network driver
	private AtomicReference<PlugwiseMQTTNetwork> network;

	// a reference to the gateway driver
	private AtomicReference<PlugwiseMQTTGatewayDriver> gateway;

	// the list of instances controlled / spawned by this driver
	protected Hashtable<String, PlugwiseMQTTDriverInstance> managedInstances;

	// the registration object needed to handle the life span of this bundle in
	// the OSGi framework (it is a ServiceRegistration object for use by the
	// bundle registering the service to update the service's properties or to
	// unregister the service).
	private ServiceRegistration<?> regDriver;

	// the filter query for listening to framework events relative to the
	// to the Plugwise gateway driver
	String filterQuery = String.format("(%s=%s)", Constants.OBJECTCLASS,
			PlugwiseMQTTGatewayDriver.class.getName());

	// what are the device categories that can match with this driver?
	protected Set<String> deviceCategories;

	// the driver instance class from which extracting the supported device
	// categories
	protected Class<?> driverInstanceClass;

	// the device class used for auto-configuration
	protected String deviceMainClass;
	
	//the device type
	protected String deviceType;

	/**
	 * 
	 */
	public PlugwiseMQTTDeviceDriver()
	{
		// initialize atomic references
		this.gateway = new AtomicReference<PlugwiseMQTTGatewayDriver>();
		this.network = new AtomicReference<PlugwiseMQTTNetwork>();

		// initialize the connected drivers list
		this.managedInstances = new Hashtable<String, PlugwiseMQTTDriverInstance>();

		// initialize the set of implemented device categories
		this.deviceCategories = new HashSet<String>();
	}

	/**
	 * Handle the bundle activation
	 */
	public void activate(BundleContext bundleContext)
	{
		// init the logger
		this.logger = new LogHelper(bundleContext);

		// store the context
		this.context = bundleContext;

		// store the driver info
		this.theDriverInfo = new PlugwiseMQTTDriverInfo();
		this.theDriverInfo.setDriverName(context.getBundle().getSymbolicName());
		this.theDriverInfo
				.setDriverVersion(context.getBundle().getVersion().toString());
		this.theDriverInfo.setMainDeviceClass(this.deviceMainClass);
		this.theDriverInfo.setType(this.deviceType);

		// fill the device categories
		this.properFillDeviceCategories(this.driverInstanceClass);

		// try registering the driver
		this.registerPlugwiseMQTTDeviceDriver();

	}

	public void deactivate()
	{
		// remove the service from the OSGi framework
		this.unRegisterPlugwiseMQTTDeviceDriver();
	}

	// ------- Handle dynamic service binding -------------------

	/**
	 * Called when an {@link PlugwiseMQTTGatewayDriver} becomes available and
	 * can be exploited by this driver
	 * 
	 * @param gatewayDriver
	 */
	public void gatewayAdded(PlugwiseMQTTGatewayDriver gatewayDriver)
	{
		this.gateway.set(gatewayDriver);
	}

	/**
	 * Called whe the given {@link PlugwiseMQTTGatewayDriver} ceases to exist in
	 * the framework; it triggers a disposal of corresponding references
	 * 
	 * @param gatewayDriver
	 */
	public void gatewayRemoved(PlugwiseMQTTGatewayDriver gatewayDriver)
	{
		if (this.gateway.compareAndSet(gatewayDriver, null))
			// unregisters this driver from the OSGi framework
			unRegisterPlugwiseMQTTDeviceDriver();
	}

	/**
	 * Called when a {@link PlugwiseMQTTNetwork} service becomes available and
	 * can be exploited by this driver.
	 * 
	 * @param networkDriver
	 */
	public void networkAdded(PlugwiseMQTTNetwork networkDriver)
	{
		this.network.set(networkDriver);
	}

	/**
	 * Called when the given {@link PlugwiseMQTTNetwork} services is no more
	 * available in the OSGi framework; triggers removal of any reference to the
	 * service.
	 * 
	 * @param networkDriver
	 */
	public void networkRemoved(PlugwiseMQTTNetwork networkDriver)
	{
		if (this.network.compareAndSet(networkDriver, null))
			// unregisters this driver from the OSGi framework
			unRegisterPlugwiseMQTTDeviceDriver();
	}

	/**
	 * Registers this driver in the OSGi framework, making its services
	 * available to all the other bundles living in the same or in connected
	 * frameworks.
	 */
	private void registerPlugwiseMQTTDeviceDriver()
	{
		if ((this.gateway.get() != null) && (this.network.get() != null)
				&& (this.context != null) && (this.regDriver == null))
		{
			// create a new property object describing this driver
			Hashtable<String, Object> propDriver = new Hashtable<String, Object>();
			// add the id of this driver to the properties
			propDriver.put(DeviceCostants.DRIVER_ID, this.getClass().getName());
			// register this driver in the OSGi framework
			regDriver = context.registerService(Driver.class.getName(), this,
					propDriver);

			// register the driver capability on the gateway
			this.gateway.get().addActiveDriverDetails(this.theDriverInfo);
		}
	}

	/**
	 * Handle the bundle de-activation
	 */
	protected void unRegisterPlugwiseMQTTDeviceDriver()
	{
		// TODO DETACH allocated Drivers
		if (regDriver != null)
		{
			regDriver.unregister();
			regDriver = null;

			// register the driver capability on the gateway
			this.gateway.get().removeActiveDriverDetails(this.theDriverInfo);
		}
	}

	/**
	 * Fill a set with all the device categories whose devices can match with
	 * this driver. Automatically retrieve the device categories list by reading
	 * the implemented interfaces of its DeviceDriverInstance class bundle.
	 */
	public void properFillDeviceCategories(Class<?> cls)
	{
		if (cls != null)
		{
			for (Class<?> devCat : cls.getInterfaces())
			{
				this.deviceCategories.add(devCat.getName());
			}
		}

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
		String manifacturer = (String) reference
				.getProperty(DeviceCostants.MANUFACTURER);

		// get the gateway to which the device is connected
		String gateway = (String) reference.getProperty(DeviceCostants.GATEWAY);

		// compute the matching score between the given device and
		// this driver
		if (deviceCategory != null)
		{
			if (manifacturer != null && (gateway != null)
					&& (manifacturer.equals(PlugwiseMQTTInfo.MANUFACTURER))
					&& (this.deviceCategories.contains(deviceCategory))
					&& (this.gateway.get() != null)
					&& (this.gateway.get().isGatewayAvailable(gateway)))
			{
				matchValue = Controllable.MATCH_MANUFACTURER
						+ Controllable.MATCH_TYPE;
			}

		}

		return matchValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String attach(ServiceReference reference) throws Exception
	{
		// get the referenced device
		ControllableDevice device = ((ControllableDevice) context
				.getService(reference));

		// check if not already attached
		if (!this.managedInstances.containsKey(device.getDeviceId()))
		{
			// create a new driver instance
			PlugwiseMQTTDriverInstance driverInstance = this
					.createPlugwiseMQTTDriverInstance(this.network.get(),
							device, context);

			// connect this driver instance with the device
			device.setDriver(driverInstance);

			// store a reference to the connected driver
			synchronized (this.managedInstances)
			{
				this.managedInstances.put(device.getDeviceId(), driverInstance);
			}
		}
		else
		{
			this.context.ungetService(reference);
		}

		return null;

	}

	public abstract PlugwiseMQTTDriverInstance createPlugwiseMQTTDriverInstance(
			PlugwiseMQTTNetwork plugwiseMQTTNetwork, ControllableDevice device,
			BundleContext context);

}
