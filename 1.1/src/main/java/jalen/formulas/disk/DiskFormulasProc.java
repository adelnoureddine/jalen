/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.formulas.disk;

import jalen.sensors.disk.DiskSensorsInterface;

public class DiskFormulasProc implements DiskFormulasInterface {

	/**
	 * Disk read and write power, and read and write rate
	 * Data from configuration file (from hardware specifications)
	 */
	private Double diskReadPower, diskReadRate, diskWritePower, diskWriteRate;

	/**
	 * Power for reading/writing one byte
	 */
	private Double powerPerReadByte, powerPerWriteByte;

	/**
	 * Disk sensor
	 */
	private DiskSensorsInterface diskSensor;

	/**
	 * Constructor
	 * @param diskReadPower Disk read power
	 * @param diskReadRate Disk read rate
	 * @param diskWritePower Disk write power
	 * @param diskWriteRate Disk write rate
	 * @param diskSensor Disk sensor
	 */
	public DiskFormulasProc(Double diskReadPower, Double diskReadRate, Double diskWritePower, Double diskWriteRate, DiskSensorsInterface diskSensor) {
		this.diskReadPower = diskReadPower;
		this.diskReadRate = diskReadRate;
		this.diskWritePower = diskWritePower;
		this.diskWriteRate = diskWriteRate;
		this.diskSensor = diskSensor;
	}

	@Override
	public double getCPUPower() {
		this.powerPerReadByte = this.diskReadPower / (this.diskReadRate * 1000000);
		this.powerPerWriteByte = this.diskWritePower / (this.diskWriteRate * 1000000);

		Double[] readWriteBytes = this.diskSensor.getProcesDiskReadWriteBytes();
		return (readWriteBytes[0] * this.powerPerReadByte) + (readWriteBytes[1] * this.powerPerWriteByte);
	}

}