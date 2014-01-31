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

public interface DiskFormulasInterface {

	/**
	 * Get disk power for writing and reading to disk for pid
	 * @return Disk power for writing and reading to disk for pid
	 */
	double getCPUPower();

}