package edu.colorado.cs.cirrus.domain.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

@Order(elements={"setpoint",  "mode", "temperatureScale", "currentTemp", "activeLoadControlEvent"})
public class Result {
	
	public Result(){}
	
	@Attribute(required=false)
	private String type;
	
	@Element(required=false)
	private String setpoint;

	@Element(required=false)
	private String mode;

	@Element(required=false)
	private String temperatureScale;
	
	@Element(required=false)
	private String currentTemp;
	
	@Element(required=false)
	private boolean activeLoadControlEvent;
	
	public String getSetpoint() {
		return setpoint;
	}

	public void setSetpoint(String setpoint) {
		this.setpoint = setpoint;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getTemperatureScale() {
		return temperatureScale;
	}

	public void setTemperatureScale(String temperatureScale) {
		this.temperatureScale = temperatureScale;
	}

	public String getCurrentTemp() {
		return currentTemp;
	}

	public void setCurrentTemp(String currentTemp) {
		this.currentTemp = currentTemp;
	}

	public boolean isActiveLoadControlEvent() {
		return activeLoadControlEvent;
	}

	public void setActiveLoadControlEvent(boolean activeLoadControlEvent) {
		this.activeLoadControlEvent = activeLoadControlEvent;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Result [type=" + type + ", setpoint=" + setpoint + ", mode="
				+ mode + ", temperatureScale=" + temperatureScale
				+ ", currentTemp=" + currentTemp + ", activeLoadControlEvent="
				+ activeLoadControlEvent + "]";
	}

	
	
	
}
