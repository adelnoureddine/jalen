/*
 * Copyright (c) 2013, Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen.sensors.disk;

public interface DiskSensorsInterface {

	/**
	 * Get number of bytes read and written by pid
	 * @return An array with number of bytes read and written
	 * double[0] = read
	 * double[1] = write
	 */
	public Double[] getProcesDiskReadWriteBytes();

}