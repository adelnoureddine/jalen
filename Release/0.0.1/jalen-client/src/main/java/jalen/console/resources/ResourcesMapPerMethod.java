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
import java.util.List;

public class ResourcesMapPerMethod {
	
	// Name is the method full name (package/class.method)
	private String name;
	
	// Resources is a the list of (resource + value) related to the method
	private List<Resource> resources;
	
	// List of all called methods since start
	private List<String> calledMethods;
	
	public ResourcesMapPerMethod(String name) {
		this.name = name;
		this.resources = new ArrayList<Resource>();
		this.calledMethods = new ArrayList<String>();
		this.populateResources();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the resources
	 */
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * @param resources the resources to set
	 */
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
	
	/**
	 * Populate resources
	 */
	private void populateResources() {
		this.resources.add(new Resource(Resources.START_TIME));
		this.resources.add(new Resource(Resources.END_TIME));
		this.resources.add(new Resource(Resources.RUNNING));
		this.resources.add(new Resource(Resources.DURATION));
		this.resources.add(new Resource(Resources.LAST_END_TIME));
		this.resources.add(new Resource(Resources.LAST_DURATION));
		this.resources.add(new Resource(Resources.THREAD));
		this.resources.add(new Resource(Resources.LAST_CPU_TIME_DURATION));
		this.resources.add(new Resource(Resources.CALLEE_METHOD));
		this.resources.add(new Resource(Resources.CPU_ENERGY));
		this.resources.add(new Resource(Resources.BYTES_READ));
		this.resources.add(new Resource(Resources.BYTES_WRITE));
		this.resources.add(new Resource(Resources.LAST_BYTES_READ));
		this.resources.add(new Resource(Resources.LAST_BYTES_WRITE));
		this.resources.add(new Resource(Resources.NETWORK_ENERGY));
	}
	
	/**
	 * Set value of resource
	 * @param resourceEnum
	 * @param value
	 */
	public void setResourceValue(Resources resourceEnum, String value) {
		for (Resource r : this.resources) {
			if (r.getName().equals(resourceEnum))
				r.setValue(value);
		}
	}
	
	/**
	 * Change current resources map to a new one
	 * @param rmp the new resources map
	 */
	public void changeRMP(ResourcesMapPerMethod rmp) {
		this.name = rmp.getName();
		this.resources = rmp.getResources();
	}
	
	/**
	 * Return value of resource
	 * @param resourceEnum the resource to search for
	 * @return value of resourceEnum, null if no resource found or resouce value is null
	 */
	public String getResourceValue(Resources resourceEnum) {
		for (Resource r : this.resources) {
			if (r.getName().equals(resourceEnum))
				return r.getValue();
		}
		return null;
	}

	/**
	 * @return the calledMethods
	 */
	public List<String> getCalledMethods() {
		return calledMethods;
	}

	/**
	 * @param calledMethods the calledMethods to set
	 */
	public void setCalledMethods(List<String> calledMethods) {
		this.calledMethods = calledMethods;
	}
	
	/**
	 * Add a called method to the current method list if it does not already exist
	 * And add it to the last called method list
	 * @param calledMethod the called method to add
	 */
	public void addCalledMethod(String calledMethod) {
		if (! this.calledMethods.contains(calledMethod))
			this.calledMethods.add(calledMethod);
	}
	
	/**
	 * Reset resources values
	 */
	public void reset() {
		for (Resource r : this.resources) {
			r.reset();
		}
	}
	
	@Override
	public String toString() {
		String value = "";
		for (Resource r : this.resources)
			value += "\t" + r.toString() + "\n";
		return value;
	}

}
