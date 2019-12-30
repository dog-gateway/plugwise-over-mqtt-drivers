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

import java.util.HashMap;
import java.util.Set;

import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDriverInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.CircleDiscoveryListener;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PlugwiseMQTTMessage;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import it.polito.elite.dog.core.devicefactory.api.DeviceFactory;
import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceDescriptor;
import it.polito.elite.dog.core.library.model.DeviceDescriptorFactory;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.model.devicecategory.PlugwiseGateway;

/**
 * @author bonino
 *
 */
public class PlugwiseMQTTGatewayDriverInstance
        extends PlugwiseMQTTDriverInstance
        implements PlugwiseGateway, CircleDiscoveryListener
{

    // the driver logger
    private Logger logger;

    // the set of currently connected drivers
    private Set<PlugwiseMQTTDriverInfo> activeDrivers;

    // the device factory reference
    private DeviceFactory deviceFactory;

    // the device descriptor factory reference
    private DeviceDescriptorFactory descriptorFactory;

    public PlugwiseMQTTGatewayDriverInstance(
            PlugwiseMQTTNetwork plugwiseMQTTNetwork,
            DeviceFactory deviceFactory,
            Set<PlugwiseMQTTDriverInfo> activeDrivers,
            ControllableDevice device, BundleContext context)
    {
        super(plugwiseMQTTNetwork, device);

        // store the device factory reference
        this.deviceFactory = deviceFactory;

        // create a logger
        // create a logger
        this.logger = context
                .getService(context.getServiceReference(LoggerFactory.class))
                .getLogger(PlugwiseMQTTDriverInstance.class);

        // store the active drivers
        this.activeDrivers = activeDrivers;

        // create the device descriptor factory
        try
        {
            this.descriptorFactory = new DeviceDescriptorFactory(
                    context.getBundle().getEntry("/deviceTemplates"));
        }
        catch (Exception e)
        {

            this.logger.error("Error while creating DeviceDescriptorFactory ",
                    e);
        }

        // create a new device state (according to the current DogOnt model, no
        // state is actually associated to a Plugwise gateway)
        this.currentState = new DeviceStatus(device.getDeviceId());

        // initialize the current state
        // this.initializeStates();

    }

    // TODO: handle calls to this method to support automatic device discovery
    // from MQTT messages
    @Override
    public void circleDeviceDiscovered(PlugwiseMQTTDeviceInfo devInfo)
    {
        // log
        this.logger.info("Device discovered at the network level, "
                + "starting the device discovery process");

        // check if the device is already known
        if (!this.isKnownDevice(devInfo))
        {
            // new device
            this.logger.info("Found new device: " + devInfo.getMacAddress());

            // search for the best match with available drivers.
            // currently the case in which multiple drivers match the same EEP
            // is assumed to be "sporadic"
            // and drivers are simply selected on the basis of the first match.
            // This can be improved with better heuristics in subsequent
            // versions of this driver.
            for (PlugwiseMQTTDriverInfo driverInfo : this.activeDrivers)
            {
                // this performs an "equals" match, it might be worth verifying
                // if it is sufficient
                if (driverInfo.getType().equals(devInfo.getType()))
                {
                    this.createDevice(devInfo, driverInfo.getMainDeviceClass());

                    // break
                    break;
                }
            }
        }

    }

    @Override
    public DeviceStatus getState()
    {
        return this.currentState;
    }

    @Override
    public void updateStatus()
    {
        ((PlugwiseGateway) this.device).updateStatus();
    }

    @Override
    protected void specificConfiguration()
    {
        // Nothing to do

    }

    @Override
    protected void addToNetworkDriver(PlugwiseMQTTDeviceInfo device)
    {
        // attach as device discovery listener
        this.network.addCircleDiscoveryListener(this);
    }

    /**
     * Checks if the device represented by the given
     * {@link PlugwiseMQTTDeviceInfo} instance is already registered in the
     * framework or not.
     * 
     * @param devInfo
     *            The device to check
     * @return true if the device is already known in the framework, false
     *         otherwise.
     */
    private boolean isKnownDevice(PlugwiseMQTTDeviceInfo devInfo)
    {
        // TODO: should check if no device exists having the same properties
        return false;
    }

    /**
     * Builds a device descriptor representing the device modeled by the given
     * {@link PlugwiseMQTTDeviceInfo} instance.
     * 
     * @param mainDeviceClass
     *            The Dog device class discovered for the device.
     * @param devInfo
     *            The {@link PlugwiseMQTTDeviceInfo} instance representing the
     *            device
     * @return
     */
    private DeviceDescriptor buildDeviceDescriptor(String deviceClass,
            PlugwiseMQTTDeviceInfo devInfo)
    {
        // the device descriptor to return
        DeviceDescriptor descriptor = null;

        if (this.descriptorFactory != null)
        {

            // normal workflow...
            if ((deviceClass != null) && (!deviceClass.isEmpty()))
            {
                // create a descriptor definition map
                HashMap<String, Object> descriptorDefinitionData = new HashMap<String, Object>();

                // store the device name
                descriptorDefinitionData.put(DeviceDescriptorFactory.NAME,
                        deviceClass + "_" + devInfo.getMacAddress());

                // store the device description
                descriptorDefinitionData.put(
                        DeviceDescriptorFactory.DESCRIPTION,
                        "New Device of type " + deviceClass);

                // store the device gateway
                descriptorDefinitionData.put(DeviceDescriptorFactory.GATEWAY,
                        this.device.getDeviceId());

                // store the device location
                descriptorDefinitionData.put(DeviceDescriptorFactory.LOCATION,
                        "");

                // store the device address
                descriptorDefinitionData.put("macAddress",
                        devInfo.getMacAddress());

                // get the device descriptor
                try
                {
                    descriptor = this.descriptorFactory.getDescriptor(
                            descriptorDefinitionData, deviceClass);
                }
                catch (Exception e)
                {
                    this.logger.error("Error while creating DeviceDescriptor "
                            + "for the just added device ", e);
                }

                // debug dump
                this.logger.error("Detected new device: \n\tdeviceUniqueId: "
                        + devInfo.getMacAddress() + "\n\tdeviceClass: "
                        + deviceClass);
            }
        }

        // return
        return descriptor;

    }

    /**
     * Creates a new Dog device given the PlugwiseMQTTDevice info representing
     * the device and the main Dog device class to be used for modeling such a
     * device.
     * 
     * @param devInfo
     *            The device information.
     * @param mainClass
     *            The class to use for representing the device inside Dog.
     */
    private void createDevice(PlugwiseMQTTDeviceInfo devInfo, String mainClass)
    {
        // match found, build the device descriptor
        DeviceDescriptor descriptorToAdd = this.buildDeviceDescriptor(mainClass,
                devInfo);

        // check not null
        if (descriptorToAdd != null)
        {
            // create the device
            // cross the finger
            this.deviceFactory.addNewDevice(descriptorToAdd);

            // log the new appliance installation
            this.logger.info("New appliance successfully identified...");
        }
    }

    @Override
    public void newMessageFromHouse(PlugwiseMQTTMessage message)
    {
        // Do nothing as the gateway is not a real device
    }
}
