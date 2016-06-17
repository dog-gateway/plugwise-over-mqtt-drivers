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

import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PlugwiseMQTTMessage;

/**
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class Msg2DriverDeliveryTask implements Runnable
{

	//the target
	private PlugwiseMQTTDriverInstance target;
	
	//the message to deliver
	private PlugwiseMQTTMessage message;
	
	/**
	 * 
	 */
	public Msg2DriverDeliveryTask(PlugwiseMQTTDriverInstance target, PlugwiseMQTTMessage message)
	{
		//store variables
		this.target = target;
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		// deliver the message
		this.target.newMessageFromHouse(message);
	}

}
