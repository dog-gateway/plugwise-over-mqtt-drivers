package org.doggateway.drivers.plugwise.mqtt.network.messages;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A class wrapping information exchanged with Pulgwise-2-py over MQTT, state
 * information in particular.
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 *
 */
public class CircleStateMessage extends PlugwiseMQTTMessage
{
	/*
	 * { "powerts": 1405452834, "name": "circle4", "schedule": "off", "power1s":
	 * 107.897, "power8s": 109.218, "readonly": false, "interval": 10, "switch":
	 * "on", "mac": "000D6F00019nnnnn", "production": false, "monitor": false,
	 * "lastseen": 1405452834, "power1h": 8.228, "online": true, "savelog":
	 * true, "type": "circle", "schedname": "test-alternate", "location": "hal1"
	 * }
	 */
	private long powerts;
	private String name;
	private String schedule;
	private double power1s;
	private double power8s;
	private boolean readonly;
	private int interval;
	@JsonProperty(value = "switch")
	private String switchStatus;
	private String mac;
	private boolean production;
	private boolean monitor;
	private long lastseen;
	private double power1h;
	private boolean online;
	private boolean savelog;
	private String type;
	private String schedname;
	private String location;
	private String requid;
	private String switchreq;

	/**
	 * 
	 */
	public CircleStateMessage()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the powerts
	 */
	public long getPowerts()
	{
		return powerts;
	}

	/**
	 * @param powerts
	 *            the powerts to set
	 */
	public void setPowerts(long powerts)
	{
		this.powerts = powerts;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the schedule
	 */
	public String getSchedule()
	{
		return schedule;
	}

	/**
	 * @param schedule
	 *            the schedule to set
	 */
	public void setSchedule(String schedule)
	{
		this.schedule = schedule;
	}

	/**
	 * @return the power1s
	 */
	public double getPower1s()
	{
		return power1s;
	}

	/**
	 * @param power1s
	 *            the power1s to set
	 */
	public void setPower1s(double power1s)
	{
		this.power1s = power1s;
	}

	/**
	 * @return the power8s
	 */
	public double getPower8s()
	{
		return power8s;
	}

	/**
	 * @param power8s
	 *            the power8s to set
	 */
	public void setPower8s(double power8s)
	{
		this.power8s = power8s;
	}

	/**
	 * @return the readonly
	 */
	public boolean isReadonly()
	{
		return readonly;
	}

	/**
	 * @param readonly
	 *            the readonly to set
	 */
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * @return the interval
	 */
	public int getInterval()
	{
		return interval;
	}

	/**
	 * @param interval
	 *            the interval to set
	 */
	public void setInterval(int interval)
	{
		this.interval = interval;
	}

	/**
	 * @return the switchStatus
	 */
	@JsonProperty(value = "switch")
	public String getSwitchStatus()
	{
		return switchStatus;
	}

	/**
	 * @param switchStatus
	 *            the switchStatus to set
	 */
	@JsonProperty(value = "switch")
	public void setSwitchStatus(String switchStatus)
	{
		this.switchStatus = switchStatus;
	}

	/**
	 * @return the mac
	 */
	public String getMac()
	{
		return mac;
	}

	/**
	 * @param mac
	 *            the mac to set
	 */
	public void setMac(String mac)
	{
		this.mac = mac;
	}

	/**
	 * @return the production
	 */
	public boolean isProduction()
	{
		return production;
	}

	/**
	 * @param production
	 *            the production to set
	 */
	public void setProduction(boolean production)
	{
		this.production = production;
	}

	/**
	 * @return the monitor
	 */
	public boolean isMonitor()
	{
		return monitor;
	}

	/**
	 * @param monitor
	 *            the monitor to set
	 */
	public void setMonitor(boolean monitor)
	{
		this.monitor = monitor;
	}

	/**
	 * @return the lastseen
	 */
	public long getLastseen()
	{
		return lastseen;
	}

	/**
	 * @param lastseen
	 *            the lastseen to set
	 */
	public void setLastseen(long lastseen)
	{
		this.lastseen = lastseen;
	}

	/**
	 * @return the power1h
	 */
	public double getPower1h()
	{
		return power1h;
	}

	/**
	 * @param power1h
	 *            the power1h to set
	 */
	public void setPower1h(double power1h)
	{
		this.power1h = power1h;
	}

	/**
	 * @return the online
	 */
	public boolean isOnline()
	{
		return online;
	}

	/**
	 * @param online
	 *            the online to set
	 */
	public void setOnline(boolean online)
	{
		this.online = online;
	}

	/**
	 * @return the savelog
	 */
	public boolean isSavelog()
	{
		return savelog;
	}

	/**
	 * @param savelog
	 *            the savelog to set
	 */
	public void setSavelog(boolean savelog)
	{
		this.savelog = savelog;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the schedname
	 */
	public String getSchedname()
	{
		return schedname;
	}

	/**
	 * @param schedname
	 *            the schedname to set
	 */
	public void setSchedname(String schedname)
	{
		this.schedname = schedname;
	}

	/**
	 * @return the location
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}

	/**
	 * @return the requid
	 */
	public String getRequid()
	{
		return requid;
	}

	/**
	 * @param requid the requid to set
	 */
	public void setRequid(String requid)
	{
		this.requid = requid;
	}

	/**
	 * @return the switchreq
	 */
	public String getSwitchreq()
	{
		return switchreq;
	}

	/**
	 * @param switchreq the switchreq to set
	 */
	public void setSwitchreq(String switchreq)
	{
		this.switchreq = switchreq;
	}
	
	

}
