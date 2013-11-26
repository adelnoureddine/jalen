/*
 * Copyright (c) 2013, Adel Noureddine, Inria, University Lille 1.
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

import java.util.Map;

public class CPUSensorSigarMaxFrequency implements CPUSensorsInterface {

	/**
	 * Sigar object
	 */
	public Sigar sigar;

	/**
	 * Number of cores in CPU, collected from sigar
	 */
	public int numberOfCores;

	/**
	 * Process PID to monitor
	 */
	public int pid;

	/**
	 * Constructor
	 * @param pid Process PID to monitor
	 */
	public CPUSensorSigarMaxFrequency(int pid) {
		this.sigar = new Sigar();
		this.pid = pid;
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
		return null;
	}

}
