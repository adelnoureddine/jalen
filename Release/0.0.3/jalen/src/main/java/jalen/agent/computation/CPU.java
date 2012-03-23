/*
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 */

package jalen.agent.computation;

import jalen.agent.Agent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class CPU {
	
	public static Long cycleDuration = 0L;

	public static Double getProcessCPUPower() {
		return 10.0;
	}
	
	public static Map<String, Double> getMethCPUPower() {
		// Calculate cycle duration
		CPU.cycleDuration = System.currentTimeMillis() - Agent.lastCPUComputationTime;
		Agent.lastCPUComputationTime = System.currentTimeMillis();
		
		// Calculate Methods duration
		Agent.methCycleDuration = CPU.getLastMethDuration(Agent.methLastDuration);
		
		Map<String, Double> methCPUPowerMap = new HashMap<String, Double>();
		
		Map<Long, Double> threadCPUPower = CPU.getThreadCPUPower();
		Map<String, Double> methCPUTime = CPU.getMethCPUTime();
		
		for (Map.Entry<String, Long> entry : Agent.methCycleDuration.entrySet()) {
			String methName = entry.getKey();
			Long tid = CPU.getThreadIDFromMeth(methName);
			Double methCPUPower = 0.0;
			if (methCPUTime.containsKey(methName) && threadCPUPower.containsKey(tid) && (entry.getValue() > 0))
				methCPUPower = (methCPUTime.get(methName) * threadCPUPower.get(tid)) / CPU.cycleDuration;
			methCPUPowerMap.put(methName, methCPUPower);
		}
		
		return methCPUPowerMap;
	}
	
	public static Map<Long, Double> getThreadCPUPower() {
		Map<Long, Double> threadCPUPowerMap = new HashMap<Long, Double>();
		Double processCPUPower = CPU.getProcessCPUPower();
		
		ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
		if (mxbean != null) {
			for (Long id : mxbean.getAllThreadIds()) {
				Double newThreadCPUTime = mxbean.getThreadCpuTime(id) / 1000000.0;
				Double threadCPUTime = newThreadCPUTime;
				if (Agent.lastThreadCPUTime.containsKey(id))
					threadCPUTime = newThreadCPUTime - Agent.lastThreadCPUTime.get(id);
				Agent.threadCPUTime.put(id, threadCPUTime);
				Double threadCPUPower = (threadCPUTime * processCPUPower) / CPU.cycleDuration;
				threadCPUPowerMap.put(id, threadCPUPower);
//				System.out.printf("Thread Time: %-40.40s %-20.20s%n", id, threadCPUTime);
//				System.out.printf("Thread Power: %-40.40s %-20.20s%n", id, threadCPUPower);
				// Update map with new cpu time value
				Agent.lastThreadCPUTime.put(id, newThreadCPUTime);
			}
		}
		
		return threadCPUPowerMap;
	}
	
	public static Map<String, Double> getMethCPUTime() {
		Map<String, Double> methCPUTimeMap = new HashMap<String, Double>();
		Long allMethDuration = CPU.allMethDuration();
//		System.out.println("All meth duration: " + allMethDuration);
		
		for (Map.Entry<String, Long> entry : Agent.methCycleDuration.entrySet()) {
			Long tid = CPU.getThreadIDFromMeth(entry.getKey());
			Double methodCPUTime = 0.0;
			if (Agent.threadCPUTime.containsKey(tid))
				methodCPUTime = (entry.getValue() * Agent.threadCPUTime.get(tid)) / allMethDuration;
//			System.out.printf("CPU Time: %-40.40s %-20.20s%n", entry.getKey(), methodCPUTime);
			methCPUTimeMap.put(entry.getKey(), methodCPUTime);
		}
		
		return methCPUTimeMap;
	}
	
	public static Long allMethDuration() {
		Long allDuration = 0L;
		for (Map.Entry<String, Long> entry : Agent.methCycleDuration.entrySet()) {
			allDuration += entry.getValue();
		}
		return allDuration;
	}
	
	public static Long getThreadIDFromMeth(String methodName) {
		return Long.valueOf(methodName.substring(methodName.lastIndexOf('-') + 1, methodName.length()));
	}
	
	// Method duration since start of program
	public static Map<String, Long> getMethDuration() {
		Map<String, Long> duration = new HashMap<String, Long>();

		for (Map.Entry<String, Long> entry : Agent.methStart.entrySet()) {
			String methName = entry.getKey();
			Long startTime = entry.getValue();
			Long realDuration = 0L;
			Long nowTime = System.currentTimeMillis();

			if (! Agent.methExecDuration.containsKey(methName))
				realDuration = nowTime - startTime;
			else {
				realDuration = Agent.methExecDuration.get(methName);
				try {
				if (Agent.methExec.get(methName))
					realDuration += (nowTime - startTime);
				} catch (NullPointerException nex) {}
				//Agent.methExecDuration.put(methName, 0L);
			}
			
			duration.put(methName, realDuration);
			//Agent.methStart.put(methName, nowTime);
		}

		return duration;
	}
	
	public static Map<String, Long> getLastMethDuration(Map<String, Long> lastMethDuration) {
		Map<String, Long> lastDuration = new HashMap<String, Long>();
		Map<String, Long> methDuration = CPU.getMethDuration();
		
		for (Map.Entry<String, Long> entry : methDuration.entrySet()) {
			String methName = entry.getKey();
			Long newDuration = entry.getValue();
			if (lastMethDuration.containsKey(methName)) {
				newDuration -= lastMethDuration.get(methName);
			}
//			System.out.printf("Last Duration: %-40.40s %-20.20s%n", methName, newDuration);
			lastDuration.put(methName, newDuration);
		}
		
		Agent.methLastDuration = methDuration;
		return lastDuration;
	}

}
