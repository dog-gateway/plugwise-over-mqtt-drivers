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
package org.doggateway.drivers.plugwise.mqtt.network.messages;

/**
 * A class representing the format of commands that can be sent to Plugwise-2-py
 * over MQTT
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class CmdMessage extends PlugwiseMQTTMessage
{
	/*
	 * {"mac":"","cmd":"reqstate","val":"1"}
	 */
	private String cmd;
	private String val;

	/**
	 * 
	 */
	public CmdMessage()
	{
		super();
	}


	/**
	 * @return the cmd
	 */
	public String getCmd()
	{
		return cmd;
	}

	/**
	 * @param cmd
	 *            the cmd to set
	 */
	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}

	/**
	 * @return the val
	 */
	public String getVal()
	{
		return val;
	}

	/**
	 * @param val
	 *            the val to set
	 */
	public void setVal(String val)
	{
		this.val = val;
	}

}
