/*
 * Dog - Plugwise Metering Power Outlet Driver
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
package org.doggateway.drivers.plugwise.mqtt.meteringpoweroutlet;

import java.util.HashSet;

import javax.measure.DecimalMeasure;
import javax.measure.Measure;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.doggateway.drivers.plugwise.mqtt.network.PlugwiseMQTTDriverInstance;
import org.doggateway.drivers.plugwise.mqtt.network.info.PlugwiseMQTTDeviceInfo;
import org.doggateway.drivers.plugwise.mqtt.network.interfaces.PlugwiseMQTTNetwork;
import org.doggateway.drivers.plugwise.mqtt.network.messages.CircleStateMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.EnergyStateMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PlugwiseMQTTMessage;
import org.doggateway.drivers.plugwise.mqtt.network.messages.PowerStateMessage;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.model.devicecategory.Controllable;
import it.polito.elite.dog.core.library.model.devicecategory.EnergyMeteringPowerOutlet;
import it.polito.elite.dog.core.library.model.devicecategory.MeteringPowerOutlet;
import it.polito.elite.dog.core.library.model.devicecategory.OnOffOutput;
import it.polito.elite.dog.core.library.model.devicecategory.PowerMeteringPowerOutlet;
import it.polito.elite.dog.core.library.model.state.OnOffState;
import it.polito.elite.dog.core.library.model.state.SinglePhaseActiveEnergyState;
import it.polito.elite.dog.core.library.model.state.SinglePhaseActivePowerMeasurementState;
import it.polito.elite.dog.core.library.model.statevalue.ActiveEnergyStateValue;
import it.polito.elite.dog.core.library.model.statevalue.ActivePowerStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OffStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OnStateValue;
import it.polito.elite.dog.core.library.util.LogHelper;

public class PlugwiseMQTTMeteringPowerOutletDriverInstance
		extends PlugwiseMQTTDriverInstance implements MeteringPowerOutlet
{
	public static final String SWITCH_CMD = "switch";
	public static final String ON_VALUE = "on";
	public static final String OFF_VALUE = "off";

	// the class logger
	private LogHelper logger;

	// the group set
	private HashSet<Integer> groups;

	// the scene set
	private HashSet<Integer> scenes;

	// the first state flag
	private boolean firstState;

	public PlugwiseMQTTMeteringPowerOutletDriverInstance(
			PlugwiseMQTTNetwork plugwiseMQTTNetwork, ControllableDevice device,
			BundleContext context)
	{
		super(plugwiseMQTTNetwork, device);

		// initially no state has been detected from the newtork
		this.firstState = true;

		// build inner data structures
		this.groups = new HashSet<Integer>();
		this.scenes = new HashSet<Integer>();

		// create a logger
		logger = new LogHelper(context);

		// initialize states
		this.initializeStates();
	}

	@Override
	public Measure<?, ?> getActiveEnergyValue()
	{
		return (Measure<?, ?>) currentState
				.getState(SinglePhaseActiveEnergyState.class.getSimpleName())
				.getCurrentStateValue()[0].getValue();
	}

	@Override
	public Measure<?, ?> getReactiveEnergyValue()
	{
		// not supported
		return null;
	}

	@Override
	public Measure<?, ?> getPowerFactor()
	{
		// not supported
		return null;
	}

	@Override
	public Measure<?, ?> getActivePower()
	{
		return (Measure<?, ?>) currentState.getState(
				SinglePhaseActivePowerMeasurementState.class.getSimpleName())
				.getCurrentStateValue()[0].getValue();
	}

	@Override
	public void on()
	{
		// plugwise2py/cmd/switch/000D6F0001Annnnn
		// {"mac":"","cmd":"switch","val":"on"}

		// send the command
		this.network.sendCommand(this.theManagedDevice,
				PlugwiseMQTTMeteringPowerOutletDriverInstance.SWITCH_CMD,
				PlugwiseMQTTMeteringPowerOutletDriverInstance.ON_VALUE);

	}

	@Override
	public void off()
	{
		// plugwise2py/cmd/switch/000D6F0001Annnnn
		// {"mac":"","cmd":"switch","val":"off"}

		// send the command
		this.network.sendCommand(this.theManagedDevice,
				PlugwiseMQTTMeteringPowerOutletDriverInstance.SWITCH_CMD,
				PlugwiseMQTTMeteringPowerOutletDriverInstance.OFF_VALUE);
	}

	@Override
	public void storeScene(Integer sceneNumber)
	{
		// Store the given scene id
		this.scenes.add(sceneNumber);

		// notify
		this.notifyStoredScene(sceneNumber);
	}

	@Override
	public void deleteScene(Integer sceneNumber)
	{
		// Remove the given scene id
		this.scenes.remove(sceneNumber);

		// notify
		this.notifyDeletedScene(sceneNumber);
	}

	@Override
	public void deleteGroup(Integer groupID)
	{
		// remove the given group id
		this.groups.remove(groupID);

		// notify
		this.notifyLeftGroup(groupID);
	}

	@Override
	public void storeGroup(Integer groupID)
	{
		// Store the given group id
		this.groups.add(groupID);

		this.notifyJoinedGroup(groupID);
	}

	@Override
	public void notifyStoredScene(Integer sceneNumber)
	{
		// send the store scene notification
		((OnOffOutput) this.device).notifyStoredScene(sceneNumber);

	}

	@Override
	public void notifyDeletedScene(Integer sceneNumber)
	{
		// send the delete scene notification
		((OnOffOutput) this.device).notifyDeletedScene(sceneNumber);

	}

	@Override
	public void notifyJoinedGroup(Integer groupNumber)
	{
		// send the joined group notification
		((OnOffOutput) this.device).notifyJoinedGroup(groupNumber);

	}

	@Override
	public void notifyLeftGroup(Integer groupNumber)
	{
		// send the left group notification
		((OnOffOutput) this.device).notifyLeftGroup(groupNumber);

	}

	@Override
	public DeviceStatus getState()
	{
		return currentState;
	}

	@Override
	public void notifyNewActivePowerValue(Measure<?, ?> powerValue)
	{
		// notify the new measure
		((PowerMeteringPowerOutlet) device)
				.notifyNewActivePowerValue(powerValue);

	}

	@Override
	public void notifyNewReactiveEnergyValue(Measure<?, ?> value)
	{
		// not supported

	}

	@Override
	public void notifyNewPowerFactorValue(Measure<?, ?> powerFactor)
	{
		// not supported

	}

	@Override
	public void notifyOff()
	{
		((OnOffOutput) this.device).notifyOff();

	}

	@Override
	public void notifyOn()
	{
		((OnOffOutput) this.device).notifyOn();

	}

	@Override
	public void notifyNewActiveEnergyValue(Measure<?, ?> value)
	{
		// notify the new measure
		((EnergyMeteringPowerOutlet) device).notifyNewActiveEnergyValue(value);

	}

	@Override
	public void updateStatus()
	{
		((Controllable) this.device).updateStatus();

	}

	@Override
	protected void specificConfiguration()
	{
		// prepare the device state map
		currentState = new DeviceStatus(device.getDeviceId());

	}

	@Override
	protected void addToNetworkDriver(PlugwiseMQTTDeviceInfo device)
	{
		this.network.addDriver(device, this);

	}

	@Override
	public void newMessageFromHouse(PlugwiseMQTTMessage message)
	{
		// interpret the message and handle notification and status update
		if (message instanceof CircleStateMessage)
		{
			// handle circle state messages
			this.handleCircleStateMessage((CircleStateMessage) message);
		}
		else if (message instanceof PowerStateMessage)
		{
			// handle power state messages
			this.handlePowerStateMessage((PowerStateMessage) message);
		}
		else if (message instanceof EnergyStateMessage)
		{
			// handle energy state messages
			this.handleEnergyStateMessage((EnergyStateMessage) message);
		}

	}

	private void initializeStates()
	{
		// create the var and va units
		Unit<Power> VAR = SI.WATT.alternate("var");

		// add unit of measure aliases (to fix notation problems...)
		UnitFormat uf = UnitFormat.getInstance();
		uf.alias(SI.WATT.times(NonSI.HOUR), "Wh");
		uf.label(SI.KILO(SI.WATT.times(NonSI.HOUR)), "kWh");
		uf.alias(VAR.times(NonSI.HOUR), "Varh");
		uf.label(SI.KILO(VAR.times(NonSI.HOUR)), "kVarh");

		// initialize the state
		this.currentState.setState(OnOffState.class.getSimpleName(),
				new OnOffState(new OffStateValue()));

		// initialize the energy state value
		ActiveEnergyStateValue energyStateValue = new ActiveEnergyStateValue();
		energyStateValue.setValue(DecimalMeasure.valueOf(
				"0.0 " + SI.KILO(SI.WATT.times(NonSI.HOUR)).toString()));
		this.currentState.setState(
				SinglePhaseActiveEnergyState.class.getSimpleName(),
				new SinglePhaseActiveEnergyState(energyStateValue));

		// initialize the power state value
		ActivePowerStateValue powerStateValue = new ActivePowerStateValue();
		powerStateValue
				.setValue(DecimalMeasure.valueOf("0.0 " + SI.WATT.toString()));
		this.currentState.setState(
				SinglePhaseActivePowerMeasurementState.class.getSimpleName(),
				new SinglePhaseActivePowerMeasurementState(powerStateValue));
	}

	private void handleEnergyStateMessage(EnergyStateMessage message)
	{
		// log the message
		this.logger.log(LogService.LOG_INFO,
				"Received energy state: " + message.toString());

		// get the power value
		DecimalMeasure<Power> power = DecimalMeasure
				.valueOf(message.getPower() + " W");

		// get the energy value
		DecimalMeasure<Energy> energy = DecimalMeasure
				.valueOf(message.getEnergy() + " Wh");

		// if not on, turn it on
		if ((this.currentState.getState(OnOffState.class.getSimpleName())
				.getCurrentStateValue()[0] instanceof OffStateValue)
				&& (message.getPower() > 0.0) && this.firstState)
		{
			OnOffState onOffState = new OnOffState(new OnStateValue());

			// update the on-off state
			this.currentState.setState(OnOffState.class.getSimpleName(),
					onOffState);
			// turn off the first state flag
			this.firstState = false;
		}

		// update the power state
		this.updatePowerState(power);

		// update the energy state
		this.updateEnergyState(energy);

		// forward the status update
		this.updateStatus();
	}

	private void handlePowerStateMessage(PowerStateMessage message)
	{
		// log the message
		this.logger.log(LogService.LOG_INFO,
				"Received power state: " + message.toString());

		// get the power value
		DecimalMeasure<Power> power = DecimalMeasure
				.valueOf(message.getPower() + " W");

		// if not on, turn it on
		if ((this.currentState.getState(OnOffState.class.getSimpleName())
				.getCurrentStateValue()[0] instanceof OffStateValue)
				&& (message.getPower() > 0.0) && this.firstState)
		{
			OnOffState onOffState = new OnOffState(new OnStateValue());

			// update the on-off state
			this.currentState.setState(OnOffState.class.getSimpleName(),
					onOffState);

			// turn off the first state flag
			this.firstState = false;
		}

		// update the power state
		this.updatePowerState(power);

		// forward the device status
		this.updateStatus();

	}

	private void handleCircleStateMessage(CircleStateMessage message)
	{
		// log the message
		this.logger.log(LogService.LOG_INFO,
				"Received circle state: " + message.toString());

		// get the state and power 1s
		DecimalMeasure<Power> power = DecimalMeasure
				.valueOf(message.getPower1s() + " W");

		String status = message.getSwitchStatus();

		// compute the current on-off state
		OnOffState onOffState = new OnOffState(new OffStateValue());

		if (status
				.equals(PlugwiseMQTTMeteringPowerOutletDriverInstance.ON_VALUE))
			onOffState = new OnOffState(new OnStateValue());

		// update the on-off state
		this.currentState.setState(OnOffState.class.getSimpleName(),
				onOffState);

		// update the power state
		this.updatePowerState(power);

		// seems redundant but notification shall happen only after status
		// update
		if (status
				.equals(PlugwiseMQTTMeteringPowerOutletDriverInstance.ON_VALUE))
			this.notifyOn();
		else
			this.notifyOff();

		// update the status
		this.updateStatus();
	}

	private void updatePowerState(DecimalMeasure<Power> power)
	{
		// update the power state
		this.currentState.getState(
				SinglePhaseActivePowerMeasurementState.class.getSimpleName())
				.getCurrentStateValue()[0].setValue(power);

		// notify
		this.notifyNewActivePowerValue(power);
	}

	private void updateEnergyState(DecimalMeasure<Energy> energy)
	{
		// update the power state
		this.currentState
				.getState(SinglePhaseActiveEnergyState.class.getSimpleName())
				.getCurrentStateValue()[0].setValue(energy);

		// notify
		this.notifyNewActiveEnergyValue(energy);
		;

	}
}
