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

public class Resource {
	
	// Name of the resource
	private Resources name;
	
	// Value of the resource
	private String value;
	
	/**
	 * Constructor
	 * @param name the name of the resource
	 */
	public Resource(Resources name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public Resources getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(Resources name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Reset resource value
	 */
	public void reset() {
		this.value = null;
	}
	
	@Override
	public String toString() {
		return this.name + " : " + this.value;
	}

}
