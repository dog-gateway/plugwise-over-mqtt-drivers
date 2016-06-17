/**
 * 
 */
package org.doggateway.drivers.plugwise.mqtt.meteringpoweroutlet;

import org.doggateway.drivers.plugwise.mqtt.device.PlugwiseMQTTDeviceDriver;
import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.osgi.framework.BundleContext;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.devicecategory.MeteringPowerOutlet;

/**
 * @author bonino
 *
 */
public class PlugwiseMQTTMeteringPowerOutletDriver
		extends PlugwiseMQTTDeviceDriver
{

	/**
	 * 
	 */
	public PlugwiseMQTTMeteringPowerOutletDriver()
	{
		// call the superclass constructor
		super();

		// set the driver instance class
		this.driverInstanceClass = PlugwiseMQTTMeteringPowerOutletDriverInstance.class;

		// set the main device class
		this.deviceMainClass = MeteringPowerOutlet.class.getSimpleName();
		
		//set the handled device type
		this.deviceType = "circle";

	}

	@Override
	public PlugwiseMQTTDriverInstance createPlugwiseMQTTDriverInstance(
			PlugwiseMQTTNetwork plugwiseMQTTNetwork, ControllableDevice device,
			BundleContext context)
	{
		return new PlugwiseMQTTMeteringPowerOutletDriverInstance(
				plugwiseMQTTNetwork, device, context);
	}

}
