/*
 * Copyright (c) 2013, Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sampler {

	/**
	 * List of data threads collected during one snapshot
 	 */
	public List<ThreadData> threadList = new ArrayList();

	/**
	 * Number of threads where a method has accessed disk
	 * <br />
	 * Disk access is considered true if method is from packages java.io or java.nio
	 */
	public int diskAccessNum = 0;

	/**
	 * Constructor
 	 */
	public Sampler() {
	}

	/**
	 * Calculate a map with thread ID and its CPU time
	 * <br />
	 * Results are for one snapshot
	 * @return a map of thread ID and CPU time
	 */
	public Map<Long, Long> calculateCPUTimeByID() {
		Map<Long, Long> results = new HashMap();

		for (ThreadData td : this.threadList) {
			results.put(td.id, td.cpuTime);
		}

		return results;
	}

	/**
	 * Get ThreadData by its ID
	 * @param id thread ID
	 * @return ThreadData with ID id, null if no id found
	 */
	public ThreadData getThreadDataByID(Long id) {
		for (ThreadData td : this.threadList) {
			if (td.id == id)
				return td;
		}
		return null;
	}

	/**
	 * Update CPU energy for threadData
	 * @param id thread ID
	 * @param totalCPUEnergy total CPU energy for thread during PowerAPI cycle
	 * @param totalCPUTime total CPU time for thread during PowerAPI cycle
	 */
	public void updateCPUEnergy(Long id, Double totalCPUEnergy, Long totalCPUTime) {
		for (ThreadData td : this.threadList) {
			if (td.id == id) {
				//System.out.println(totalCPUTime + " -- " + td.cpuTime + " -- " + totalCPUEnergy);
				Double result = (td.cpuTime * totalCPUEnergy) / totalCPUTime;
				if (result.isNaN())
					result = 0.0;
				td.cpuEnergy = result;
			}
		}
	}

	/**
	 * Calculate the number of threads with disk access
	 */
	public void calculateDiskAccessNum() {
		for (ThreadData td : this.threadList) {
			if (td.diskAccessed()) {
				this.diskAccessNum++;
			}
		}
	}

	/**
	 * Update disk energy for threadData
	 * @param diskEnergyPerTD disk energy for each thread during PowerAPI cycle
	 */
	public void updateDiskEnergy(Double diskEnergyPerTD) {
		for (ThreadData td : this.threadList) {
			if (td.diskAccess) {
				td.diskEnergy = diskEnergyPerTD;
			}
		}
	}

}