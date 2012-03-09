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

public class Threads {
	
	private Long threadId;
	private Long beforeCpuTime;
	private Long afterCpuTime;
	private Long lastCpuTime;
	
	/**
	 * Constructor
	 * @param threadId the thread id
	 */
	public Threads(Long threadId) {
		this.threadId = threadId;
		this.lastCpuTime = 0L;
	}
	
	/**
	 * @return the beforeCpuTime
	 */
	public Long getBeforeCpuTime() {
		return beforeCpuTime;
	}

	/**
	 * @param beforeCpuTime the beforeCpuTime to set
	 */
	public void setBeforeCpuTime(Long beforeCpuTime) {
		this.beforeCpuTime = beforeCpuTime;
	}

	/**
	 * @return the afterCpuTime
	 */
	public Long getAfterCpuTime() {
		return afterCpuTime;
	}

	/**
	 * Update after cpu time and last cpu time
	 * @param afterCpuTime the afterCpuTime to set
	 */
	public void setAfterCpuTime(Long afterCpuTime) {
		this.afterCpuTime = afterCpuTime;
		if (this.beforeCpuTime != null)
			this.lastCpuTime = this.afterCpuTime - this.beforeCpuTime;
	}

	/**
	 * @return the lastCpuTime
	 */
	public Long getLastCpuTime() {
		return lastCpuTime;
	}

	/**
	 * @param lastCpuTime the lastCpuTime to set
	 */
	public void setLastCpuTime(Long lastCpuTime) {
		this.lastCpuTime = lastCpuTime;
	}

	/**
	 * @return the threadId
	 */
	public Long getThreadId() {
		return threadId;
	}

	/**
	 * @param threadId the threadId to set
	 */
	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}	
	
}
