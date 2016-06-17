package org.doggateway.drivers.plugwise.mqtt.network.interfaces;

import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;

public interface PlugwiseMQTTNetwork
{
	/**
	 * Adds a new device-specific driver for the device identified by the given
	 * {@link PlugwiseMQTTDeviceInfo} instance.
	 * 
	 * @param devInfo
	 *            The device info instance providing data for unique device
	 *            identification
	 * @param driver
	 *            The driver to which dispatch device updates (listener)
	 */
	public void addDriver(PlugwiseMQTTDeviceInfo devInfo,
			PlugwiseMQTTDriverInstance driver);

	/**
	 * Removes a given device-specific driver from the set of drivers
	 * "connected" to the network driver. This implies that all devices being
	 * connected to the removed driver only will also be "removed" from the set
	 * managed by the network driver and will not be reachable anymore by other
	 * platform bundles.
	 * 
	 * @param driver
	 *            The driver to remove.
	 */
	public void removeDriver(PlugwiseMQTTDriverInstance driver);

	/**
	 * Sends a command to the device identified by the given
	 * {@link PlugwiseMQTTDeviceInfo} instance.
	 * 
	 * @param devInfo
	 *            The device to which sending the command
	 * @param cmdName
	 *            The name of the command to send
	 * @param cmdValue
	 *            The value of the command
	 */
	public void sendCommand(PlugwiseMQTTDeviceInfo devInfo, String cmdName,
			String cmdValue);

	/**
	 * Adds a listener to discovery of new Plugwise Circle device, typically the
	 * only listener is the Plugwise gateway driver
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addCircleDiscoveryListener(CircleDiscoveryListener listener);

	/**
	 * Removes the given listener from the set of listeners notified of a new
	 * device discovery.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeCircleDiscoveryListener(CircleDiscoveryListener listener);
}
