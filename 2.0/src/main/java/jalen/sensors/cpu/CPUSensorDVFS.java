/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.sensors.cpu;

import jalen.Agent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CPUSensorDVFS implements CPUSensorsInterface {

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
	 * Previous CPU total time
	 * Previous CPU PID time
	 */
	private Long previousTotalTime, previousPIDTime;

	/**
	 * Check if monitoring has already started or not
	 * Used in calculating cpu percentage for the first time
	 */
	private boolean firstRun;

	/**
	 * Path to the time_in_state file where CPU time data is stored
	 * Linux-systems only
	 */
	private String timeInStatePath = "/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state";

	/**
	 * Constructor
	 * @param pid Process PID to monitor
	 * @param frequenciesMap The map of frequencies and voltage
	 */
	public CPUSensorDVFS(int pid, Map<Double, Double> frequenciesMap) {
		this.pid = pid;
		this.timeInFrequencies = frequenciesMap;
		this.numberOfCores = Runtime.getRuntime().availableProcessors();
		this.previousPIDTime = 0L;
		this.previousTotalTime = 0L;
		this.firstRun = true;
	}

	/**
	 * Get CPU total time
	 * @return CPU total time
	 */
	private Long getTotalTime() {
		Long result = 0L;

		try (BufferedReader br = new BufferedReader(new FileReader("/proc/stat"))) {
			// Read first line where total cpu time is, and split it
			// Example of line: cpu  586994 19195 123650 2813135 107565 3 2017 0 0 0
			String[] firstLine = br.readLine().split(" ");

			// Sum up all values, except first one which is "cpu" (index 0)
			// Index 1 is another space, so start at index 2
			for (int i=2; i<11; i++) {
				result += Long.valueOf(firstLine[i]);
			}
		} catch (Exception e) {
			Agent.LOGGER.log(Level.WARNING, e.getMessage());
		}

		return result;
	}

	/**
	 * Get CPU PID time
	 * @return CPU PID time
	 */
	private Long getPIDTime() {
		Long result = 0L;

		try (BufferedReader br = new BufferedReader(new FileReader("/proc/" + this.pid + "/stat"))) {
			// Read first line where cpu time is, and split it
			// We only need utime and stime (user and system time)
			// Example of line: 25152 (java) S 12564 1685 1685 0 -1 1077960704 155132 412 478 2 11617 1816 0 0 20 0 61 0 2001362 3813126144 99139 18446744073709551615 4194304 4196724 140736365379696 140736365362368 140056419567211 0 0 4096 16796879 18446744073709551615 0 0 17 2 0 0 3 0 0 6294960 6295616 13131776 140736365387745 140736365388341 140736365388341 140736365391821 0
			String[] firstLine = br.readLine().split(" ");

			// Get utime and stime and sum them together
			// utime is at index 13, stime at index 14
			result = Long.valueOf(firstLine[13]) + Long.valueOf(firstLine[14]);
		} catch (Exception e) {
			Agent.LOGGER.log(Level.WARNING, e.getMessage());
		}

		return result;
	}

	@Override
	public double getProcessCPUUsagePercentage() {
		Long totalTime = this.getTotalTime();
		Long pidTime = this.getPIDTime();

		Long totalDiff = totalTime - this.previousTotalTime;
		Long pidDiff = pidTime - this.previousPIDTime;

		double result = 0.0;

		if (!this.firstRun)
			result = ((double) pidDiff) / totalDiff; // / this.numberOfCores;
		else
			this.firstRun = false;

		this.previousPIDTime = pidTime;
		this.previousTotalTime = totalTime;

		return result;
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
				Agent.LOGGER.log(Level.WARNING, e.getMessage());
			}
		}

		return timeInFrequenciesCache;
	}
}