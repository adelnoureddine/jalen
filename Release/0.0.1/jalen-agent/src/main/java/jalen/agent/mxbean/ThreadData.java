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
package jalen.agent.mxbean;

public class ThreadData {
	
	private Long id;
	private String name;
	private Long cpuTime;
	
	/**
	 * Constructor
	 */
	public ThreadData() {}
	
	/**
	 * Constructor
	 * @param id thread id
	 * @param name thread name
	 * @param cpuTime thread current cpu time since starting
	 */
	public ThreadData(Long id, String name, Long cpuTime) {
		this.id = id;
		this.name = name;
		this.cpuTime = cpuTime;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @return the cpuTime
	 */
	public Long getCpuTime() {
		return cpuTime;
	}

	/**
	 * @param cpuTime the cpuTime to set
	 */
	public void setCpuTime(Long cpuTime) {
		this.cpuTime = cpuTime;
	}
	
}
