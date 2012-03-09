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
package jalen.console.util;

import java.util.ArrayList;
import java.util.List;

import jalen.agent.mxbean.ThreadData;
import jalen.console.resources.Resource;
import jalen.console.resources.Resources;
import jalen.console.resources.ResourcesMap;
import jalen.console.resources.ResourcesMapPerMethod;
import jalen.console.resources.Threads;
import jalen.console.resources.ThreadsMap;
import jalen.console.resources.Tree;

public class Computation {
	
	/**
	 * Compute collected data from agent
	 */
	public static synchronized void computeData() {
		Long agentLastDuration = ResourcesMap.getInstance().getAgentCurrentTime() - ResourcesMap.getInstance().getAgentLastTime();
		ResourcesMap.getInstance().setAgentLastDuration(agentLastDuration);
		
		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			Long lastDuration = 0L, endTime = null, startTime = null;
			String running = rmp.getResourceValue(Resources.RUNNING);
			String startTimeS = rmp.getResourceValue(Resources.START_TIME);
			if (startTimeS != null)
				startTime = Long.valueOf(startTimeS);
			
			String endTimeS = rmp.getResourceValue(Resources.END_TIME);
			if (endTimeS != null)
				endTime = Long.valueOf(endTimeS);
			
			if (startTime != null) {
				if (running.equals("false")) {
					if ((endTime > ResourcesMap.getInstance().getAgentLastTime()) && (startTime > ResourcesMap.getInstance().getAgentLastTime()))
						lastDuration = endTime - startTime;
					else
						if (endTime > ResourcesMap.getInstance().getAgentLastTime())
							lastDuration = endTime - ResourcesMap.getInstance().getAgentLastTime();
				}
				else
					if (running.equals("true")) {
						if (startTime > ResourcesMap.getInstance().getAgentLastTime())
							lastDuration = ResourcesMap.getInstance().getAgentCurrentTime() - startTime;
						else
							lastDuration = ResourcesMap.getInstance().getAgentDuration();
					}
			}
			
			ResourcesMap.getInstance().updateResourceRMP(rmp.getName(), Resources.LAST_DURATION, String.valueOf(lastDuration));
		}
	}
	
	/**
	 * Update threads
	 * @param ltd the list of thread data
	 */
	public static synchronized void updateThreads(List<ThreadData> ltd) {
		for (ThreadData td : ltd) {
			if (! ThreadsMap.getInstance().containsThread(td.getId())) {
				Threads t = new Threads(td.getId());
				ThreadsMap.getInstance().addToMap(t);
			}
		}
	}
	
	/**
	 * Update before the cycle thread cpu time
	 * @param ltd the list of thread data
	 */
	public static synchronized void addBeforeThreadData(List<ThreadData> ltd) {
		for (ThreadData td : ltd) {
			if (ThreadsMap.getInstance().containsThread(td.getId()))
				ThreadsMap.getInstance().getThreads(td.getId()).setBeforeCpuTime(td.getCpuTime());
			else {
				Threads t = new Threads(td.getId());
				ThreadsMap.getInstance().addToMap(t);
				ThreadsMap.getInstance().getThreads(td.getId()).setBeforeCpuTime(td.getCpuTime());
			}
		}
	}
	
	/**
	 * Update after the cycle thread cpu time
	 * @param ltd the list of thread data
	 */
	public static synchronized void addAfterThreadData(List<ThreadData> ltd) {
		for (ThreadData td : ltd) {
			if (ThreadsMap.getInstance().containsThread(td.getId()))
				// Set after cpu time and also calculate last cpu time in cycle
				ThreadsMap.getInstance().getThreads(td.getId()).setAfterCpuTime(td.getCpuTime());
			else {
				Threads t = new Threads(td.getId());
				ThreadsMap.getInstance().addToMap(t);
				ThreadsMap.getInstance().getThreads(td.getId()).setAfterCpuTime(td.getCpuTime());
			}
		}
	}
	
	
	/**
	 * Estimate CPU time utilization and energy consumption for each method of a thread in last duration cycle
	 * @param threadId the ID of the thread
	 * @param energyProcess the energy consumption of the java process
	 */
	public static synchronized void correlateMethodsToThread(Long threadId, double energyProcess) {
		
		// List of methods of a thread with a last duration > 0
		List<ResourcesMapPerMethod> list = Computation.getPositiveDurationMethodsOfThread(Computation.getMethodsOfThread(threadId));
				
		if (list.isEmpty())
			return;
		
		Tree<ResourcesMapPerMethod> tree = null;
		
		// Find head of tree (main method)
		for (ResourcesMapPerMethod rmp : list) {
			if (rmp.getName().equals(rmp.getResourceValue(Resources.CALLEE_METHOD))) {
				tree = new Tree<ResourcesMapPerMethod>(rmp);
				break;
			}
		}
		
		double lastCpuTimeDuration = ThreadsMap.getInstance().getThreads(threadId).getLastCpuTime();
		double perMethCpuTime = 0, perMethEnergy = 0;
		double threadEnergy = (lastCpuTimeDuration * energyProcess) / Computation.getAllThreadCpuTime();

		if (tree == null) {			
			Long sumLastDurationMeth = 0L;
			// First cycle, get sum of duration time of methods
			for (ResourcesMapPerMethod rmp : list) {
				if (rmp.getResourceValue(Resources.LAST_DURATION) != null)
					sumLastDurationMeth += Long.valueOf(rmp.getResourceValue(Resources.LAST_DURATION));
			}
			// sumLastDurationMeth == sum of duration of all methods
			
			for (ResourcesMapPerMethod rmp : list) {
				// cpu(m) = d(m) * (t(cpu) / sum(d(m)))
				perMethCpuTime = Double.valueOf(rmp.getResourceValue(Resources.LAST_DURATION)) * (lastCpuTimeDuration / sumLastDurationMeth);
				
				// E(m) = (cpu(m) / t(cpu)) * E(cpu)
				if (lastCpuTimeDuration > 0)
					perMethEnergy = (perMethCpuTime / lastCpuTimeDuration) * threadEnergy;

				rmp.setResourceValue(Resources.LAST_CPU_TIME_DURATION, String.valueOf(perMethCpuTime));
				rmp.setResourceValue(Resources.CPU_ENERGY, String.valueOf(perMethEnergy));
			}		
		}
		else {
			List<ResourcesMapPerMethod> list2 = new ArrayList<ResourcesMapPerMethod>();
			
			// Construct tree of calls
			for (ResourcesMapPerMethod rmp : list) {
				// Check that callee method is the head, and that called method is not the head
				if (rmp.getResourceValue(Resources.CALLEE_METHOD).equals(tree.getHead().getName()) && (! rmp.getName().equals(tree.getHead().getName())) ) {
						tree.addLeaf(rmp);
						tree.setAsParent(tree.getHead());
						list2.add(rmp);
				}
			}
			
			Long sumLastDurationMeth = 0L;
			// First cycle, get sum of duration time of methods
			for (ResourcesMapPerMethod rmp : list2) {
				if (rmp.getResourceValue(Resources.LAST_DURATION) != null)
					sumLastDurationMeth += Long.valueOf(rmp.getResourceValue(Resources.LAST_DURATION));
			}
			// sumLastDurationMeth == sum of duration of all methods
			
			for (ResourcesMapPerMethod rmp : list2) {
				// cpu(m) = d(m) * (t(cpu) / sum(d(m)))
				perMethCpuTime = Double.valueOf(rmp.getResourceValue(Resources.LAST_DURATION)) * (lastCpuTimeDuration / sumLastDurationMeth);
				
				// E(m) = (cpu(m) / t(cpu)) * E(cpu)
				if (lastCpuTimeDuration > 0)
					perMethEnergy = (perMethCpuTime / lastCpuTimeDuration) * threadEnergy;

				rmp.setResourceValue(Resources.LAST_CPU_TIME_DURATION, String.valueOf(perMethCpuTime));
				rmp.setResourceValue(Resources.CPU_ENERGY, String.valueOf(perMethEnergy));
			}
		}
	}
	
	/**
	 * Get the resources map of the thread id (get all methods executing in thread id)
	 * @param threadId the thread to check for
	 * @return list of resources map of methods of thread id
	 */
	private static synchronized List<ResourcesMapPerMethod> getMethodsOfThread(Long threadId) {
		List<ResourcesMapPerMethod> lrmp = new ArrayList<ResourcesMapPerMethod>();
		
		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			for (Resource rs : rmp.getResources()) {
				if (rs.getName().equals(Resources.THREAD) && (rs.getValue() != null) && rs.getValue().equals(String.valueOf(threadId)))
					lrmp.add(rmp);
			}
		}
		
		return lrmp;
	}
	
	/**
	 * Get the list of methods of a thread with a last duration > 0
	 * @param lrmp the list of resources of a thread
	 * @return the list of methods from lrmp with a last duration > 0
	 */
	private static synchronized List<ResourcesMapPerMethod> getPositiveDurationMethodsOfThread(List<ResourcesMapPerMethod> lrmp) {
		List<ResourcesMapPerMethod> lmeth = new ArrayList<ResourcesMapPerMethod>();
		
		if (lrmp.isEmpty())
			return lmeth;
		
		for (ResourcesMapPerMethod rmp : lrmp) {
			for (Resource rs : rmp.getResources()) {
				if (rs.getName().equals(Resources.LAST_DURATION) && (rs.getValue() != null) && (Long.valueOf(rs.getValue()) > 0))
					lmeth.add(rmp);
			}
		}
		return lmeth;
	}
	

	/**
	 * @return last cpu time of all threads
	 */
	public static Long getAllThreadCpuTime() {
		Long cpuTime = 0L;
		for (Threads t : ThreadsMap.getInstance().getMap()) {
			cpuTime += t.getLastCpuTime();
		}
		return cpuTime;
	}
	
	
	/*******************************************************************************
	 * NETWORK ENERGY COMPUTATION
	 ******************************************************************************/
	
	/**
	 * Calculate/Estimate network energy consumption of all instrumented methods
	 */
	public static synchronized void computeSocketsEnergy(double networkEnergy) {
		Long[] transmitted = Computation.getTotalTransmittedBytes();
		Long readTotal = transmitted[0];
		Long writeTotal = transmitted[1];
		Long total = readTotal + writeTotal;

		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			Long read = 0L, write = 0L, lastRead = 0L, lastWrite = 0L, diffRead = 0L, diffWrite = 0L;
			
			for (Resource rs : rmp.getResources()) {
				if ((rs.getName().equals(Resources.BYTES_READ)) && (rs.getValue() != null))
					read = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.BYTES_WRITE)) && (rs.getValue() != null))
					write = Long.valueOf(rs.getValue());
				
				if (rs.getName().equals(Resources.LAST_BYTES_READ)) {
					if ((rs.getValue() != null))
						lastRead = Long.valueOf(rs.getValue());
				}
				
				if (rs.getName().equals(Resources.LAST_BYTES_WRITE)) {
					if ((rs.getValue() != null))
						lastWrite = Long.valueOf(rs.getValue());
				}
			}
			
			// We will calculate the energy consumed in the last cycle only
			// So we get the number of bytes read/written since last cycle
			diffRead = read - lastRead;
			diffWrite = write - lastWrite;
			Long totalMethod = diffRead + diffWrite;
			
			// we will calculate supposing we only have total energy, total number of bytes transmitted (read & write), and number of bytes transmitted for the method
			double networkEnergyMethod = (totalMethod * networkEnergy) / total;
			rmp.setResourceValue(Resources.NETWORK_ENERGY, String.valueOf(networkEnergyMethod));
			
			// Set new values for last read/write
			rmp.setResourceValue(Resources.LAST_BYTES_READ, String.valueOf(read));
			rmp.setResourceValue(Resources.LAST_BYTES_WRITE, String.valueOf(write));
		}
	}
	
	/**
	 * Calculate the total number of transmitted (read and write) bytes in all methods
	 * @return the array of total number of bytes read (index 0) and written (index 1)
	 */
	private static synchronized Long[] getTotalTransmittedBytes() {
		Long[] transmitted = new Long[2];
		transmitted[0] = 0L;
		transmitted[1] = 0L;
		
		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			Long read = 0L, write = 0L;

			for (Resource rs : rmp.getResources()) {
				if ((rs.getName().equals(Resources.BYTES_READ)) && (rs.getValue() != null))
					read = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.BYTES_WRITE)) && (rs.getValue() != null))
					write = Long.valueOf(rs.getValue());
			}
			
			transmitted[0] += read;
			transmitted[1] += write;
		}
		return transmitted;
	}
	
}
