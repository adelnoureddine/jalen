/*
 * Copyright (c) 2013 Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package fr.inria.jalen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SamplerList {

	/**
	 * List of samplers (List of thread data collected during one snapshot)
	 * collected during all snapshots of one PowerAPI cycle
 	 */
	public List<Sampler> samplers = new ArrayList();

	/**
	 * CPU Power of all threads
 	 */
	public Double cpuPower = 0.0;

	/**
	 * CPU Energy of all threads
	 */
	public Double cpuEnergy = 0.0;

	/**
	 * Total CPU time of all threads
	 */
	public Long totalCPUTime = 0L;

	/**
	 * Disk Power of all threads
	 */
	public Double diskPower = 0.0;

	/**
	 * Disk Energy of all threads
	 */
	public Double diskEnergy = 0.0;

	/**
	 * Number of threads where a method has accessed disk
	 * <br />
	 * Disk access is considered true if method is from packages java.io or java.nio
	 */
	public int diskAccessNum = 0;

	public Map<Long, Long> CPUTimeByID = new HashMap();

	public Map<Long, Double> CPUEnergyByID = new HashMap();

	/**
	 * Constructor
 	 */
	public SamplerList() {
	}

	/**
	 * Calculate a map with thread ID and its CPU time
	 * <br />
	 * Results are for the duration of PowerAPI cycle
	 * <br />
	 * Also update totalCPUTime of all threads
	 */
	public void calculateCPUTimeByID() {
		for (Sampler sam : this.samplers) {
			for (Map.Entry<Long, Long> entry : sam.calculateCPUTimeByID().entrySet()) {
				Long key = entry.getKey();
				Long value = entry.getValue();

				this.totalCPUTime += value;

				if (this.CPUTimeByID.containsKey(key)) {
					// Thread ID already present in map, so update value
					this.CPUTimeByID.put(key, this.CPUTimeByID.get(key) + value);
				}
				else {
					// Add thread ID and its CPU time to map
					this.CPUTimeByID.put(key, value);
				}
			}
		}
	}

	/**
	 * Calculate a map with thread ID and its CPU energy
	 * <br />
	 * Results are for the duration of PowerAPI cycle
	 */
	public void calculateEnergyByID() {
		for (Map.Entry<Long, Long> entry : this.CPUTimeByID.entrySet()) {
			Long key = entry.getKey(); // Thread ID
			Long value = entry.getValue(); // Thread CPU time

			Double threadEnergy = (value * this.cpuEnergy) / this.totalCPUTime;
			//System.out.println(key + " Energy: "  + threadEnergy);
			this.CPUEnergyByID.put(key, threadEnergy);
		}
	}


	/**
	 * Calculate the energy consumed by each thread snapshot (threadData of sampler)
	 * <br />
	 * The calculation is done by distributing the total energy consumed by the thread
	 * during the PowerAPI cycle, to each snapshot based on its CPU time
	 */
	public void calculateEnergyBySampler() {
		for (Map.Entry<Long, Double> entry : this.CPUEnergyByID.entrySet()) {
			// For each thread ID, update each of its threadData in its samplers
			// with its energy consumption proportional to its CPU time
			for (Sampler sam : this.samplers) {
				Long id = entry.getKey();
				sam.updateCPUEnergy(id, entry.getValue(), this.CPUTimeByID.get(id));
			}
		}
	}

	/**
	 * Calculate the number of threads with disk access in list
	 */
	public void calculateDiskAccessNum() {
		for (Sampler sam : this.samplers) {
			sam.calculateDiskAccessNum();
			this.diskAccessNum += sam.diskAccessNum;
		}
	}

	/**
	 *
	 */
	public void calculateDiskEnergyBySampler() {
		Double result = this.diskEnergy / this.diskAccessNum;
		if (result.isNaN())
			result = 0.0;

		for (Sampler sam : this.samplers) {
			sam.updateDiskEnergy(result);
		}
	}

}