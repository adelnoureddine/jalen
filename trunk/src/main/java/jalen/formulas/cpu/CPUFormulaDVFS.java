/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.formulas.cpu;

import jalen.sensors.cpu.CPUSensorsInterface;

import java.util.HashMap;
import java.util.Map;

public class CPUFormulaDVFS implements CPUFormulasInterface {

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
	 * The map of CPU frequencies and the corresponding CPU voltage
	 * <Frequency, Voltage>
	 */
	private Map<Double, Double> frequenciesVoltages;

	/**
	 * Array with the maximum frequency and voltage supported by CPU
	 */
	private Double[] frequencyVoltageMax;

	/**
	 * The map of CPU frequencies and their relevant CPU time
	 */
	private Map<Double, Double> frequenciesTimes;

	/**
	 * The map of CPU frequencies and their power consumption
	 */
	private Map<Double, Double> frequenciesPower;

	/**
	 * Constructor
	 * @param TDP The TDP of the CPU
	 * @param TDPFactor The TDP factor of the CMOS formula
	 * @param cpuSensor The CPU sensor
	 * @param frequenciesVoltages Map of CPU frequencies and their voltage
	 */
	public CPUFormulaDVFS(double TDP, double TDPFactor, CPUSensorsInterface cpuSensor, Map<Double, Double> frequenciesVoltages) {
		this.TDP = TDP;
		this.TDPFactor = TDPFactor;
		this.cpuSensor = cpuSensor;
		this.frequenciesVoltages = frequenciesVoltages;
		this.frequenciesTimes = new HashMap<Double, Double>();
		this.frequenciesPower = new HashMap<Double, Double>();
		this.compute();
	}

	private void compute() {
		this.frequencyVoltageMax = this.getMaxFrequencyVoltage();
	}

	/**
	 * Calculate the maximum frenquency and voltage of the CPU
	 * @return an array with 0 the maximum frequency and 1 the maximum voltage
	 */
	private Double[] getMaxFrequencyVoltage() {
		Double frequency = 0.0, voltage = 0.0;
		Double[] result = new Double[2];
		for (Map.Entry<Double, Double> entry : this.frequenciesVoltages.entrySet()) {
			if (entry.getKey() > frequency) {
				frequency = entry.getKey();
				voltage = entry.getValue();
			}
		}
		result[0] = frequency;
		result[1] = voltage;
		return result;
	}

	@Override
	public double getCPUPower() {
		// (TDP * TDPFactor) / (FrequencyMax * VoltageMax^2)
		double constant = (this.TDP * this.TDPFactor) / (this.frequencyVoltageMax[0] * Math.pow(this.frequencyVoltageMax[1], 2));

		Double powerFrequency;
		for (Map.Entry<Double, Double> entry : this.frequenciesVoltages.entrySet()) {
			powerFrequency = constant * entry.getKey() * (Math.pow(entry.getValue(), 2));
			this.frequenciesPower.put(entry.getKey(), powerFrequency);
		}

		// Fill or get from sensors the frequencies times
		this.frequenciesTimes= this.cpuSensor.getTimeInFrequencies();
		double totalTimes = 0.0;
		for (Map.Entry<Double, Double> entry : this.frequenciesTimes.entrySet()) {
			totalTimes += entry.getValue();
		}

		// Total power
		double totalPower = 0.0;
		for (Map.Entry<Double, Double> entry : this.frequenciesPower.entrySet()) {
			totalPower += entry.getValue() * this.frequenciesTimes.get(entry.getKey());
		}

		// Power CPU
		if (totalPower == 0)
			return 0;
		else
			return (totalPower / totalTimes) * this.cpuSensor.getProcessCPUUsagePercentage();
	}

}
