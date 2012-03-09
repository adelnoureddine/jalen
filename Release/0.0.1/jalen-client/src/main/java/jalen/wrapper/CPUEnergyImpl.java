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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class CPUEnergyImpl implements CPUEnergy {
	
	/**
	 * Constructor
	 */
	public CPUEnergyImpl() {
		super();
	}
	
	@Override
	public double getCPUEnergy(int pid, Long duration) {
		double value = 0.0;
		try {
			FileInputStream st = new FileInputStream("/Users/adel/workspace/Jalen2/cpu");
			DataInputStream in = new DataInputStream(st);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				value = Double.valueOf(line);
				break;
			}
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}

}
