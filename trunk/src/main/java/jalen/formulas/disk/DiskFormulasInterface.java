/*
 * Copyright (c) 2013, Adel Noureddine.
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
	 * Get CPU power for writing and reading to disk for pid
	 * @return CPU power for writing and reading to disk for pid
	 */
	public double getCPUPower();

}