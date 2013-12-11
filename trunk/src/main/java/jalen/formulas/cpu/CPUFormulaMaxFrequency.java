/*
 * Copyright (c) 2013, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.formulas.cpu;

import jalen.sensors.cpu.CPUSensorsInterface;

public class CPUFormulaMaxFrequency implements CPUFormulasInterface {

	/**
	 * Thermal Design Power of the CPU (given by constructors)
	 */
	private double TDP;

	/**
	 * TDP factor for CMOS formula
	 * By default, it is at 0.7
	 */
	private double TDPFactor = 0.7;

	/**
	 * The CPU sensor used to collect data from the CPU
	 */
	private CPUSensorsInterface cpuSensor;

	/**
	 * Constructor
	 * @param TDP The TDP of the CPU
	 * @param TDPFactor The TDP factor of the CMOS formula
	 * @param cpuSensor The CPU sensor
	 */
	public CPUFormulaMaxFrequency(double TDP, double TDPFactor, CPUSensorsInterface cpuSensor) {
		this.TDP = TDP;
		this.TDPFactor = TDPFactor;
		this.cpuSensor = cpuSensor;
	}

	@Override
	public double getCPUPower() {
		return (this.TDP * this.TDPFactor * this.cpuSensor.getProcessCPUUsagePercentage());
	}

}
