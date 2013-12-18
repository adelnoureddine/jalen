/*
 * Copyright (c) 2013, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.sensors.cpu;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPUSensorDVFS implements CPUSensorsInterface {

	/**
	 * Sigar object
	 */
	private Sigar sigar;

	/**
	 * Number of cores in CPU, collected from sigar
	 */
	private int numberOfCores;

	/**
	 * Process PID to monitor
	 */
	private int pid;

	/**
	 * Map of CPU frequencies and the CPU time they spend in each
	 */
	private Map<Double, Double> timeInFrequencies;

	/**
	 * Path to the time_in_state file where CPU time data is stored
	 * Linux-systems only
	 */
	private String timeInStatePath = "/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state";

	/**
	 * Constructor
	 * @param pid Process PID to monitor
	 * @param frequenciesMap
	 */
	public CPUSensorDVFS(int pid, Map<Double, Double> frequenciesMap) {
		this.sigar = new Sigar();
		this.pid = pid;
		this.timeInFrequencies = frequenciesMap;
		this.populateSigar();
	}

	/**
	 * Calculate number of cores in CPU
	 */
	private void populateSigar() {
		try {
			this.numberOfCores = this.sigar.getCpuInfoList()[0].getTotalCores();
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	@Override
	public double getProcessCPUUsagePercentage() {
		try {
			return (this.sigar.getProcCpu(this.pid).getPercent() / this.numberOfCores);
		} catch (SigarException e) {
			e.printStackTrace();
			return 0.0;
		}
	}

	@Override
	public Map<Double, Double> getTimeInFrequencies() {
		Map<Double, Double> timeInFrequenciesUpdated = new HashMap<Double, Double>();
		Map<Double, Double> timeInFrequenciesNew = this.getTimeInFrequenciesFromPath();

		for (Map.Entry<Double, Double> entry : this.timeInFrequencies.entrySet()) {
			Double frequency = entry.getKey();
			Double timeFreq = entry.getValue();
			if (timeInFrequenciesNew.get(frequency) != null)
				timeInFrequenciesUpdated.put(frequency, timeFreq - timeInFrequenciesNew.get(frequency));
			else
				timeInFrequenciesUpdated.put(frequency, timeFreq);
		}

		return timeInFrequenciesUpdated;
	}

	/**
	 * Calculate CPU time for each frequency
	 * This function only report the data contained in @timeInStatePath file
	 * This means, this is an aggregated data since start of CPU monitoring by Linux (and not for monitoring cycle)
	 * @return Map of CPU frequencies and the CPU time spend for each
	 */
	private Map<Double, Double> getTimeInFrequenciesFromPath() {
		Map<Double, Double> timeInFrequenciesCache = new HashMap<Double, Double>();

		for (int i = 0; i<this.numberOfCores; i++) {
			String timeInStatePathReal = timeInStatePath.replace("%?", "" + i);

			try (BufferedReader br = new BufferedReader(new FileReader(timeInStatePathReal))) {
				String currentLine = "";
				while ((currentLine = br.readLine()) != null) {
					String[] freqAndtime = currentLine.split(" ");
					Double frequency = Double.valueOf(freqAndtime[0]);
					Double timeFreq = Double.valueOf(freqAndtime[1]);

					if (timeInFrequenciesCache.containsKey(frequency))
						timeInFrequenciesCache.put(frequency, timeInFrequenciesCache.get(frequency) + timeFreq);
					else
						timeInFrequenciesCache.put(frequency, timeFreq);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return timeInFrequenciesCache;
	}
}