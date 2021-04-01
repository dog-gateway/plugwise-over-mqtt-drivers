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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.CircleDiscoveryListener;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.doggateway.drivers.plugwise.mqtt.network.messages.CircleStateMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.CmdMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.EnergyStateMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PlugwiseMQTTMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PowerStateMessage;
import org.doggateway.drivers.plugwise.mqtt.network.tasks.DeviceDiscoveryDeliveryTask;
import org.doggateway.drivers.plugwise.mqtt.network.tasks.Msg2DriverDeliveryTask;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.polito.elite.dog.addons.mqtt.library.transport.MqttAsyncDispatcher;
import it.polito.elite.dog.addons.mqtt.library.transport.MqttMessageListener;
import it.polito.elite.dog.addons.mqtt.library.transport.MqttQos;

/**
 * Plugwise network driver based on the MQTT service offered by the XXXX Python
 * library which provides the currently most complete support to plugwise
 * devices without proprietary software. It is a pre-requisite that the XXXX is
 * installed and running, and that a suitable MQTT broker is available on the
 * machine. It is currently under evaluation if it makes sense for Dog to act as
 * MQTT broker in very small installation cases.
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class PlugwiseMQTTNetworkDriverImpl
        implements ManagedService, MqttMessageListener, PlugwiseMQTTNetwork
{
    // -------- the configuration parameters ---------

    // mqtt broker url
    public static final String BROKER_URL_KEY = "broker";

    // mqtt root topic
    public static final String ROOT_TOPIC_KEY = "rootTopic";

    // ------------------------------------------------

    // the command topic relative to the root
    private static final String CMD = "cmd/";

    // the state topic relative to the root
    private static final String STATE = "state/";

    // the bundle context
    private BundleContext bundleContext;

    // the service registration handle
    private ServiceRegistration<?> regServicePlugWiseMQTTDriverImpl;

    // the driver logger
    private Logger logger;

    // the device to driver map
    private Map<PlugwiseMQTTDeviceInfo, PlugwiseMQTTDriverInstance> dev2Driver;

    // the mqtt client to use for exchanging messages with Plugwise-2-py
    private MqttAsyncDispatcher plugwiseClient;

    // the MQTT Quality of Service to adopt
    private MqttQos qos;

    // the root topic to which listen
    private String rootTopic;

    // the URL of the mqtt broker over which messages are received
    private String brokerUrl;

    // the Jackson Object mapper needed to read/write JSON values
    private ObjectMapper mapper;

    // the ExecutorService used to dispatch messages to drivers
    private ExecutorService execService;

    // the set of device discovery listeners that must be notified if a new
    // circle device is discovered.
    private Set<CircleDiscoveryListener> deviceDiscoveryListeners;

    // the log service used for logging
    private AtomicReference<LogService> logService;

    /**
     * Initializes the inner data structures, while not instantiating the
     * underlying {@link MqttAsyncDispatcher} object, which instead can be
     * prepared only after actual configuration occurs.
     */
    public PlugwiseMQTTNetworkDriverImpl()
    {
        // build the references to other services
        this.logService = new AtomicReference<>();

        // initialize the inner data structures
        this.dev2Driver = new HashMap<PlugwiseMQTTDeviceInfo, PlugwiseMQTTDriverInstance>();

        // the applied qos
        this.qos = MqttQos.AT_MOST_ONCE;

        // initialize the instance-wide object mapper
        this.mapper = new ObjectMapper();
        // set the mapper pretty printing
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // avoid empty arrays and null values
        this.mapper.configure(
                SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        this.mapper.setSerializationInclusion(Include.NON_NULL);

        // the executor service
        // grants that all messages are delivered in order
        this.execService = Executors.newSingleThreadExecutor();

        // the set of discovery listeners
        this.deviceDiscoveryListeners = new HashSet<CircleDiscoveryListener>();
    }

    /**
     * Called when the bundle is activated by the OSGi framework
     * 
     * @param context
     *            The bundle context to use for activation and registration of
     *            bundle services.
     */
    public void activate(BundleContext context)
    {
        // store the bundle context
        this.bundleContext = context;

        // debug: signal activation...
        this.logger.debug("Activated...");

        // register the service
        this.registerNetworkService();
    }

    /**
     * Called upon bundle deactivation, enables to accomplish all tasks needed
     * to perform a clean shutdown of the bundle and of relative services.
     */
    public void deactivate()
    {
        // unregister the service
        this.unregisterNetworkService();

        // TODO: perform house keeping stuff here...

        // log
        this.logger.info("Deactivated...");
    }

    public void setLogService(LogService logService)
    {
        this.logService.set(logService);
        this.logger = logService.getLogger(PlugwiseMQTTNetworkDriverImpl.class);
    }

    public void unsetLogService(LogService logService)
    {
        if (this.logService.compareAndSet(logService, null))
        {
            this.logger = null;
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage)
    {
        // the prefix of topics that can be handled
        String prefix = this.rootTopic + PlugwiseMQTTNetworkDriverImpl.STATE;
        // handle different topics
        if (topic.startsWith(prefix))
        {
            // trim common prefix
            String actualTopic = topic.substring(prefix.length());

            // act differently depending on topic
            PlugwiseMQTTMessage msg = null;
            try
            {
                if (actualTopic.startsWith("power"))
                    msg = this.mapper.readValue(mqttMessage.getPayload(),
                            PowerStateMessage.class);
                else if (actualTopic.startsWith("energy"))
                    msg = this.mapper.readValue(mqttMessage.getPayload(),
                            EnergyStateMessage.class);
                else if (actualTopic.startsWith("circle"))
                    msg = this.mapper.readValue(mqttMessage.getPayload(),
                            CircleStateMessage.class);
            }
            catch (IOException e)
            {
                this.logger
                        .error("Unable to parse data received over MQTT" + e);
            }

            // if msg not null, can dispatch it
            if (msg != null)
            {
                boolean found = false;
                for (PlugwiseMQTTDeviceInfo devInfo : this.dev2Driver.keySet())
                {
                    if (devInfo.getMacAddress().equals(msg.getMac()))
                    {
                        // dispatch the message
                        this.execService.submit(new Msg2DriverDeliveryTask(
                                this.dev2Driver.get(devInfo), msg));
                        found = true;
                    }
                }

                // if not found, generate a new discovery event
                if (!found)
                {
                    // TODO check if any hint in messages identify the device
                    // type
                    PlugwiseMQTTDeviceInfo discoveredDeviceInfo = new PlugwiseMQTTDeviceInfo(
                            msg.getMac(), "circle");

                    for (CircleDiscoveryListener listener : this.deviceDiscoveryListeners)
                    {
                        // trigger a device discovery
                        this.execService.submit(new DeviceDiscoveryDeliveryTask(
                                discoveredDeviceInfo, listener));
                    }
                }
            }
        }

    }

    @Override
    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException
    {
        // get the bundle configuration parameters, e.g., the broker address to
        // which "connect", the port and the root topic to subscribe to.
        if (properties != null)
        {
            // debug log
            logger.debug("Received configuration properties");

            // broker url
            String brokerUrl = (String) properties.get(BROKER_URL_KEY);

            // root topic
            String rootTopic = (String) properties.get(ROOT_TOPIC_KEY);

            // if both are not null, store the value and init the mqtt client,
            // then register the service
            if ((brokerUrl != null) && (!brokerUrl.isEmpty())
                    && (rootTopic != null) && (!rootTopic.isEmpty()))
            {
                // store the topic and url
                this.rootTopic = rootTopic;
                this.brokerUrl = brokerUrl;

                // create the mqtt client
                this.plugwiseClient = new MqttAsyncDispatcher(this.brokerUrl,
                        UUID.randomUUID().toString(), "", "", true,
                        this.logger);

                // register this driver as listener
                this.plugwiseClient.addMqttMessageListener(this);

                // connect synchronously: the driver cannot be registered if it
                // cannot connect to the broker...
                this.plugwiseClient.syncConnect();

                // adjust the root topic
                if (!rootTopic.endsWith("#"))
                {
                    if (!rootTopic.endsWith("/"))
                    {
                        // not a valid topic
                        this.rootTopic = this.rootTopic + "/";
                        this.logger.warn("The specified topic is not / ended, "
                                + "automatically added trailing /");
                    }
                }
                else
                {
                    // remove the #
                    this.rootTopic = this.rootTopic.substring(0,
                            this.rootTopic.length() - 1);
                }

                // at this point the subscribe topic is obtained by
                // concatenating the wildcard # with the root topic
                String topicToSubscribe = this.rootTopic + "#";

                // subscribe to the topic
                if (this.plugwiseClient.isConnected())
                {
                    this.plugwiseClient.subscribe(topicToSubscribe,
                            this.qos.getQoS());
                }

                // TODO handle subscription in case of missed connection.

                // the driver is ready to register services
                this.registerNetworkService();
            }
        }

    }

    @Override
    public void addDriver(PlugwiseMQTTDeviceInfo devInfo,
            PlugwiseMQTTDriverInstance driver)
    {
        // adds an entry to the device info / device driver mapping
        if ((devInfo != null) && (driver != null))
        {
            this.dev2Driver.put(devInfo, driver);
        }
    }

    @Override
    public void removeDriver(PlugwiseMQTTDriverInstance driver)
    {
        // the list of entries to delete
        ArrayList<PlugwiseMQTTDeviceInfo> toDelete = new ArrayList<PlugwiseMQTTDeviceInfo>();

        // iterate over dev2Driver entries and delete all the ones referring to
        // the given driver instance
        // deletion cannot be done here as iterator would change generating a
        // concurrent modification exception
        for (PlugwiseMQTTDeviceInfo dInfo : this.dev2Driver.keySet())
        {
            if (this.dev2Driver.get(dInfo).equals(driver))
                toDelete.add(dInfo);
        }

        // actually delete
        for (PlugwiseMQTTDeviceInfo dInfoToDel : toDelete)
        {
            this.dev2Driver.remove(dInfoToDel);
        }

    }

    @Override
    public void sendCommand(PlugwiseMQTTDeviceInfo devInfo, String cmdName,
            String cmdValue)
    {
        // check that the devInfo is not null
        if (devInfo != null)
        {
            // check that the device is actually handled by this driver
            if (this.dev2Driver.containsKey(devInfo))
            {
                // prepare the command message
                CmdMessage cmd = new CmdMessage();
                cmd.setCmd(cmdName);
                cmd.setVal(cmdValue);
                cmd.setMac(devInfo.getMacAddress());

                // prepare the topic
                String topic = this.rootTopic
                        + PlugwiseMQTTNetworkDriverImpl.CMD + cmdName + "/"
                        + devInfo.getMacAddress();

                // send the command
                try
                {
                    this.plugwiseClient.publish(topic,
                            this.mapper.writeValueAsBytes(cmd));
                }
                catch (IOException e)
                {
                    // log the error
                    this.logger.error("Unable to publish command "
                            + "on the given MQTT broker: " + e);
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.doggateway.drivers.plugwise.mqtt.network.interfaces.
     * PlugwiseMQTTNetwork#addCircleDiscoveryListener(org.doggateway.drivers.
     * plugwise.mqtt.network.interfaces.CircleDiscoveryListener)
     */
    @Override
    public void addCircleDiscoveryListener(CircleDiscoveryListener listener)
    {
        // add the given listener to the set of "registered" listeners
        this.deviceDiscoveryListeners.add(listener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.doggateway.drivers.plugwise.mqtt.network.interfaces.
     * PlugwiseMQTTNetwork#removeCircleDiscoveryListener(org.doggateway.drivers.
     * plugwise.mqtt.network.interfaces.CircleDiscoveryListener)
     */
    @Override
    public void removeCircleDiscoveryListener(CircleDiscoveryListener listener)
    {
        // remove the given listener
        this.deviceDiscoveryListeners.remove(listener);

    }

    /*************************************************
     *
     * PRIVATE METHODS
     *
     ************************************************/

    /**
     * Registers the services described by the {@link EnOceanNetwork} interface
     * and provided by this class as "available" in the OSGi framework.
     */
    private void registerNetworkService()
    {
        // simple registration stuff

        // avoid multiple registrations
        if (this.regServicePlugWiseMQTTDriverImpl == null)
        {
            // register the service, with no properties
            this.regServicePlugWiseMQTTDriverImpl = this.bundleContext
                    .registerService(PlugwiseMQTTNetwork.class.getName(), this,
                            null);
        }

    }

    /**
     * Unregisters the services provided by this class from the OSGi framework
     */
    private void unregisterNetworkService()
    {
        // performs service de-registration from the framework
        if (this.regServicePlugWiseMQTTDriverImpl != null)
        {
            // de-register
            this.regServicePlugWiseMQTTDriverImpl.unregister();
        }

    }
}
