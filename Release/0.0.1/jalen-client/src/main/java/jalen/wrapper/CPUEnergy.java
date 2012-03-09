/*******************************************************************************
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 ******************************************************************************/
package jalen.wrapper;

public interface CPUEnergy {

	/**
	 * Get the cpu energy consumption of a process for a certain duration
	 * @param pid the process pid
	 * @param duration the duration of the moniroting
	 * @return the cpu energy (in watt) of pid for duration
	 */
	public double getCPUEnergy(int pid, Long duration);

}
