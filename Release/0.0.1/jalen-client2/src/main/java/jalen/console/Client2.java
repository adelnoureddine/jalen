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
package jalen.console;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Client2 {
	
	/**
	 * Print methods energy consumptions as a Pie chart
	 */
	public static synchronized void printDumpStats(String filepath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filepath));
			String str;
			Map<String, Double> cpuMap = new HashMap<String, Double>();
			Map<String, Double> netMap = new HashMap<String, Double>();
			
			while ((str = in.readLine()) != null) {
				// Process each line
				// Data is in form of: methodName;agentTime;cpuEnergy;networkEnergy
				String[] data = str.split(";");
				
				// For cpu (index 2)
				if (cpuMap.containsKey(data[0])) {
					// Method already exist
					cpuMap.put(data[0], cpuMap.get(data[0]) + Double.valueOf(data[2]));
				}
				else {
					// First encounter of the method
					cpuMap.put(data[0], Double.valueOf(data[2]));
				}
				
				// For network (index 3)
				if (netMap.containsKey(data[0])) {
					// Method already exist
					netMap.put(data[0], netMap.get(data[0]) + Double.valueOf(data[3]));
				}
				else {
					// First encounter of the method
					netMap.put(data[0], Double.valueOf(data[3]));
				}
			}
			
			in.close();
			
			// Now in cpuMap and netMap is the total energy consumption of each method
			// Let's print them in a nice graphical UI
			PieChart cpuChart = new PieChart(0, "Total CPU");
			PieChart netChart = new PieChart(0, "Total Network");
			
			// Let's start with the cpu
			for (Map.Entry<String, Double> entry : cpuMap.entrySet()) {
				Object[][] dataCPU = {
						{entry.getKey(), entry.getValue()}
				};
				
				cpuChart.addData(dataCPU);
			}
			
			// Then the network
			for (Map.Entry<String, Double> entry : netMap.entrySet()) {
				Object[][] dataNET = {
						{entry.getKey(), entry.getValue()}
				};
				
				netChart.addData(dataNET);
			}
			
			cpuChart.updateDataset();
			netChart.updateDataset();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Client2.printDumpStats("/Users/adel/workspace/jalen-maven/dumpData.txt");
		Client2.printDumpStats(args[0]);
	}

}
