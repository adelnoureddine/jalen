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

import jalen.agent.Agent;
import jalen.agent.MethodStats;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics implements StatisticsMXBean {
	
	@Override
	public synchronized Long getStartTime() {
		return Agent.startTime;
	}

	@Override
	public synchronized Long getCurrentTime() {		
		return System.nanoTime();
	}

	@Override
	public synchronized List<ThreadData> getThreadData() {
		List<ThreadData> ltd = new ArrayList<ThreadData>();
		
		ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
		if (mxbean != null) {
			for (Long id : mxbean.getAllThreadIds()) {
				ThreadData td = new ThreadData(id, mxbean.getThreadInfo(id).getThreadName(), mxbean.getThreadCpuTime(id));
				ltd.add(td);
			}
		}
		
		return ltd;
	}

	@Override
	public synchronized Map<String, String> getMethCallee() {
		return Agent.methCallee;
	}

	@Override
	public synchronized int getPID() {
		String mxbeanName = ManagementFactory.getRuntimeMXBean().getName();
    	return Integer.valueOf(mxbeanName.substring(0, mxbeanName.indexOf('@')));
	}

	@Override
	public synchronized Map<String, String> getMethRead() {
		return Agent.methRead;
	}

	@Override
	public synchronized Map<String, String> getMethWrite() {
		return Agent.methWrite;
	}
	
	@Override
	public synchronized Map<String, String> getMethDuration(Long agentLastTime) {
//		Long start = System.nanoTime();
		
		Map<String, String> duration = new HashMap<String, String>();
				
		for (Map.Entry<String, List<MethodStats>> entry : Agent.methStats.entrySet()) {
			Long lastDuration = 0L;
			
			// Calculate duration for all methods
			for (MethodStats ms : entry.getValue()) {
				if (ms.endTime != null) {
					if (ms.endTime > ms.startTime) {
						// Method ended, Running == false
						if ((ms.endTime > agentLastTime) && (ms.startTime > agentLastTime))
							lastDuration += ms.endTime - ms.startTime;
						else
							if (ms.endTime > agentLastTime)
								lastDuration += ms.endTime - agentLastTime;
					}
				}
				else {
					// Method still running, Running == true
					if (ms.startTime > agentLastTime)
						lastDuration += System.nanoTime() - ms.startTime;
					else
						lastDuration += System.nanoTime() - agentLastTime;
				}
			}
			
			duration.put(entry.getKey(), String.valueOf(lastDuration));
		}
		
		/* Now, map duration contains:
		 * Method name (in form of methodname-threadid) and the total duration of execution of the method in a particular thread
		 */
		
		// Now check for callee and called methods
		// Subtract from method duration the sum of the duration of its called methods
		
//		for (Map.Entry<String, String> entry : Agent.methDuration.entrySet()) {
//			
//		}
		
		// Then store duration in agent's map
		Agent.methDuration.putAll(duration); // For the moment no treatement
		
//		Long end = System.nanoTime();
//		System.out.println("Statistics Overhead: " + (end - start));
		
		return Agent.methDuration;
	}

}
