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

import java.util.List;
import java.util.Map;

public interface StatisticsMXBean {
	
	/**
	 * @return the global start time of the agent in nanoseconds
	 */
	public Long getStartTime();
	
	/**
	 * @return the current time of the agent in nanoseconds
	 */
	public Long getCurrentTime();
	
	/**
	 * @return the list of threads with their data
	 */
	public List<ThreadData> getThreadData();
	
	/**
	 * @return the callee method of all instrumented methods
	 */
	public Map<String, String> getMethCallee();
	
	/**
	 * @return the process ID of the Java VM
	 */
	public int getPID();
	
	/**
	 * @return the number of bytes read from sockets for all instrumented methods
	 */
	public Map<String, String> getMethRead();
	
	/**
	 * @return the number of bytes written to sockets for all instrumented methods
	 */
	public Map<String, String> getMethWrite();
	
	/**
	 * @param agentLastTime the agent time of the last cycle
	 * @return the execution duration of all instrumented methods since last cycle
	 */
	public Map<String, String> getMethDuration(Long agentLastTime);
	
}
