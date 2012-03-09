/*******************************************************************************
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 ******************************************************************************/
package jalen.console.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResourcesMap {
	
	// Singleton instance
	private static volatile ResourcesMap instance = null;
	
	// List of all resources of all methods
	private List<ResourcesMapPerMethod> map;
	
	private Long agentStartTime;
	private Long agentCurrentTime;
	private Long agentDuration;
		
	private Long agentLastTime;
	private Long cycleDuration;
	private Long agentLastDuration;
	
	/**
	 * Constructor
	 */
	private ResourcesMap() {
		this.map = Collections.synchronizedList(new ArrayList<ResourcesMapPerMethod>());
	}
	
	/**
	 * 
	 * @return the instance of the singleton ResourcesMap
	 */
	public final static synchronized ResourcesMap getInstance() {
		if (ResourcesMap.instance == null) {
			synchronized(ResourcesMap.class) {
				if (ResourcesMap.instance == null) {
					ResourcesMap.instance = new ResourcesMap();
				}
			}
		}
		return ResourcesMap.instance;
	}
	
	
	/**
	 * @return the map
	 */
	public synchronized List<ResourcesMapPerMethod> getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public synchronized void setMap(List<ResourcesMapPerMethod> map) {
		this.map = map;
	}
	
	/**
	 * Add resources map of a method to the list
	 * @param rmp resources map of a method
	 */
	public synchronized void addToMap(ResourcesMapPerMethod rmp) {
		this.map.add(rmp);
	}
	
	/**
	 * Checks if methods already have a resources map
	 * @param name full name of the method
	 * @return true if method already have a resources map, false if not
	 */
	public synchronized boolean containsMethod(String name) {
		for (ResourcesMapPerMethod rmp : this.map) {
			if (rmp.getName().equals(name))
				return true;
		}
		return false;
	}
	
	/**
	 * Return the resources map of the method
	 * @param name full name of the method
	 * @return resources map of the method, return null if resources map not found
	 */
	public synchronized ResourcesMapPerMethod getRMP(String name) {
		for (ResourcesMapPerMethod rmp : this.map) {
			if (rmp.getName().equals(name))
				return rmp;
		}
		return null;
	}
	
	/**
	 * Update resources map for a method if rmp exist, else add it
	 * @param method the name of the method
	 * @param rmp the new value of the resources map of a method
	 */
	public synchronized void updateRMP(String method, ResourcesMapPerMethod rmp) {
		if (this.containsMethod(method)) {
			this.getRMP(method).changeRMP(rmp);
		}
		else {
			this.addToMap(rmp);
		}
	}
	
	/**
	 * Change value of resource of a particular method
	 * @param method the method
	 * @param resEnum the resource to change
	 * @param value the new value of the resource
	 */
	public synchronized void updateResourceRMP(String method, Resources resEnum, String value) {
		for (ResourcesMapPerMethod rmp : this.map) {
			if (rmp.getName().equals(method)) {
				rmp.setResourceValue(resEnum, value);
			}
		}
	}

	/**
	 * @return the agentStartTime
	 */
	public synchronized Long getAgentStartTime() {
		return agentStartTime;
	}

	/**
	 * @param agentStartTime the agentStartTime to set
	 */
	public synchronized void setAgentStartTime(Long agentStartTime) {
		this.agentStartTime = agentStartTime;
	}

	/**
	 * @return the agentCurrentTime
	 */
	public synchronized Long getAgentCurrentTime() {
		return agentCurrentTime;
	}

	/**
	 * @param agentCurrentTime the agentCurrentTime to set
	 */
	public synchronized void setAgentCurrentTime(Long agentCurrentTime) {
		this.agentCurrentTime = agentCurrentTime;
	}

	/**
	 * @return the agentDuration
	 */
	public synchronized Long getAgentDuration() {
		return agentDuration;
	}

	/**
	 * @param agentDuration the agentDuration to set
	 */
	public synchronized void setAgentDuration(Long agentDuration) {
		this.agentDuration = agentDuration;
	}
	
	/**
	 * Reset resource values for the map (not clear it, but just reset values of resources of methods)
	 */
	public synchronized void reset() {
		for (ResourcesMapPerMethod rmp : this.map)
			rmp.reset();
	}
	
	@Override
	public synchronized String toString() {
		String value = "";
		for (ResourcesMapPerMethod rmp : this.map)
			value += rmp.getName() + "\n" + rmp.toString() + "\n";
		return value;
	}

	/**
	 * @return the agentLastTime
	 */
	public Long getAgentLastTime() {
		return agentLastTime;
	}

	/**
	 * @param agentLastTime the agentLastTime to set
	 */
	public void setAgentLastTime(Long agentLastTime) {
		this.agentLastTime = agentLastTime;
	}

	/**
	 * @return the cycleDuration
	 */
	public Long getCycleDuration() {
		return cycleDuration;
	}

	/**
	 * @param cycleDuration the cycleDuration to set
	 */
	public void setCycleDuration(Long cycleDuration) {
		this.cycleDuration = cycleDuration;
	}

	/**
	 * @return the agentLastDuration
	 */
	public Long getAgentLastDuration() {
		return agentLastDuration;
	}

	/**
	 * @param agentLastDuration the agentLastDuration to set
	 */
	public void setAgentLastDuration(Long agentLastDuration) {
		this.agentLastDuration = agentLastDuration;
	}

}
