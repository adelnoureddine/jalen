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

import java.util.Map;

public interface CPUSensorsInterface {

	/**
	 * Calculate percentage of CPU usage for PID
	 * @return the percentage of CPU usage by PID
	 */
	double getProcessCPUUsagePercentage();

	/**
	 * Calculate CPU time for each frequency
	 * @return Map of CPU frequencies and the CPU time spend for each
	 */
	Map<Double, Double> getTimeInFrequencies();

}