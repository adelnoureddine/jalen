/*
 * Copyright (c) 2013 Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package fr.inria.jalen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class PowerModel {
	// Cycle duration in milliseconds
	public static Long cycleDuration = 0L;
	
	/**
	 * Get CPU power for the current executing process in watt for the monitoring cycle
	 * @return CPU power of the current executing process in watt
	 */
	public static Double getProcessCPUPower() {
		//return 10.0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(Agent.powerAPICPUFile));
			String str = in.readLine();
			in.close();

			if (str != null) {
				try {
					return Double.valueOf(str);
				} catch (NumberFormatException ex) {
					//System.out.println("[WARNING] Can't read value from PowerAPI (NumberFormatException)");
					return 0.0;
				}
			}
			else {
				//System.out.println("[WARNING] Invalid or No value from PowerAPI (null value)");
				return 0.0;
			}
		} catch (IOException e) {
			//System.out.println("[WARNING] Can't read value from PowerAPI (IOException)");
			return 0.0;
		}
	}

	/**
	 * Get disk power for the current executing process in watt for the monitoring cycle
	 * @return disk power of the current executing process in watt
	 */
	public static Double getProcessDiskPower() {
		//return 10.0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(Agent.powerAPIDiskFile));
			String str = in.readLine();
			in.close();

			if (str != null) {
				try {
					return Double.valueOf(str);
				} catch (NumberFormatException ex) {
					//System.out.println("[WARNING] Can't read value from PowerAPI (NumberFormatException)");
					return 0.0;
				}
			}
			else {
				//System.out.println("[WARNING] Invalid or No value from PowerAPI (null value)");
				return 0.0;
			}
		} catch (IOException e) {
			//System.out.println("[WARNING] Can't read value from PowerAPI (IOException)");
			return 0.0;
		}
	}
	
	/**
	 * Calculate the last cycle duration (current time - last time cpu computation time)
	 */
	public static void computeCycleDuration() {
		// Calculate cycle duration
		PowerModel.cycleDuration = System.currentTimeMillis() - Agent.lastCPUComputationTime;
		Agent.lastCPUComputationTime = System.currentTimeMillis();
	}

	/**
	 * Two digit function to transform a double into a double with only two digits precision after .
	 */
	public static DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

}
