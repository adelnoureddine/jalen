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
package jalen.console.util;

import jalen.console.Client;
import jalen.console.resources.Resource;
import jalen.console.resources.Resources;
import jalen.console.resources.ResourcesMap;
import jalen.console.resources.ResourcesMapPerMethod;
import jalen.console.resources.Threads;
import jalen.console.resources.ThreadsMap;
import jalen.console.ui.PieChart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class PrintStats {

	public static DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

	/**
	 * Print collected information from agent
	 */
	public static synchronized void printAgentStats() {
		// Agent collected informations

		double agentDurationSec = ResourcesMap.getInstance().getAgentDuration() / 1000000000.0;
		System.out.println("Agent duration: " + PrintStats.twoDigit.format(agentDurationSec) + " sec");

		System.out.println("Map size: " + jalen.console.resources.ResourcesMap.getInstance().getMap().size());

		System.out.printf("%-40.40s %-20.20s %-20.20s %-20.20s %-20.20s %-20.20s %-20.20s%n",
				"Method",
				"Running",
				"Duration (ns)",
				"Duration (sec)",
				"Total Percentage",
				"Last Duration (ns)",
				"Last End Time (ns)"
				);

		System.out.println("-------------------------------------------------------------------------------------------------------------------------");

		for (ResourcesMapPerMethod rmp : jalen.console.resources.ResourcesMap.getInstance().getMap()) {
			Long duration = null, lastDuration = null, lastEndTime = null, threadId = null;
			String running = null, calleeMethod = null;

			for (Resource rs : rmp.getResources()) {
				if ((rs.getName().equals(Resources.RUNNING)) && (rs.getValue() != null))
					running = rs.getValue();

				if ((rs.getName().equals(Resources.DURATION)) && (rs.getValue() != null))
					duration = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.LAST_DURATION)) && (rs.getValue() != null))
					lastDuration = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.LAST_END_TIME)) && (rs.getValue() != null))
					lastEndTime = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.THREAD)) && (rs.getValue() != null))
					threadId = Long.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.CALLEE_METHOD)) && (rs.getValue() != null))
					calleeMethod = rs.getValue();
			}

			if (duration != null) {
				double durationSec = (double) duration / 1000000000.0;
				double totalPercentage = (duration * 100) / ResourcesMap.getInstance().getAgentDuration();

				System.out.printf("%-40.40s %-20.20s %-20.20s %-20.20s %-20.20s %-20.20s %-20.20s%n",
						rmp.getName() + " (" + threadId + ")",
						running,
						duration,
						twoDigit.format(durationSec),
						twoDigit.format(totalPercentage) + "%",
						lastDuration,
						lastEndTime
						);

				System.out.println(calleeMethod);
				System.out.println(rmp.getCalledMethods());
			}
		}

		System.out.println("-------------------------------------------------------------------------------------------------------------------------");
	}

	/**
	 * Print thread statistics
	 */
	public static synchronized void printThreadStats() {
		System.out.printf("%-40.40s %-20.20s%n",
				"Thread ID",
				"Last CPU Time (ns)"
				);

		System.out.println("-------------------------------------------------------------------------------------------------------------------------");

		try {
			for (Threads t : ThreadsMap.getInstance().getMap()) {		
				System.out.printf("%-40.40s %-20.20s%n",
						t.getThreadId(),
						t.getLastCpuTime()
						);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("-------------------------------------------------------------------------------------------------------------------------");
	}

	/**
	 * Print energy information for instrumented methods
	 * This method will only print energy information if energy > 0
	 */
	public static synchronized void printEnergyStats(double cpuEnergyOfProcess, double networkEnergyOfProcess) {
//		int print = 0;

//		Client.w.resetTable();
		
		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			double cpuEnergy = 0.0, networkEnergy = 0.0;

			for (Resource rs : rmp.getResources()) {
				if ((rs.getName().equals(Resources.CPU_ENERGY)) && (rs.getValue() != null))
					cpuEnergy = Double.valueOf(rs.getValue());

				if ((rs.getName().equals(Resources.NETWORK_ENERGY)) && (rs.getValue() != null))
					networkEnergy = Double.valueOf(rs.getValue());
			}

			if ((cpuEnergy > 0) || (networkEnergy > 0)) {
//				print++;
//				double cpuPer = (cpuEnergy * 100) / cpuEnergyOfProcess;
//				double netPer = (networkEnergy * 100) / networkEnergyOfProcess;
				
//				System.out.printf("%-60.60s %-30.30s %-30.30s%n",
//						rmp.getName(),
//						cpuEnergy + " Watt" + " - " + twoDigit.format(cpuPer) + "%",
//						networkEnergy + " Watt" + " - " + twoDigit.format(netPer) + "%"
//						);
//				Object[][] data = {
//						{rmp.getName(), cpuEnergy, twoDigit.format(cpuPer), networkEnergy, twoDigit.format(netPer)}
//				};
				
				// String to save energy data of the method in CSV format
				String methData = rmp.getName() + ";" + ResourcesMap.getInstance().getAgentCurrentTime() + ";" + cpuEnergy + ";" + networkEnergy + "\n";
				PrintStats.appendToFile(methData);
				
				Object[][] dataCPU = {
						{rmp.getName(), cpuEnergy}
				};
				
				Object[][] dataNET = {
						{rmp.getName(), networkEnergy}
				};
				
				Client.wcpu.addData(dataCPU);
				Client.wnet.addData(dataNET);
			}
		}
		
		Client.wcpu.updateDataset();
		Client.wnet.updateDataset();

//		if (print > 0)
//			System.out.println();
	}
	
	/**
	 * Append methods statistics data into a file
	 * @param methData the energy consumption of a method
	 */
	public static synchronized void appendToFile(String methData) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(Client.dumpFile, true));
			out.write(methData);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Print methods energy consumptions as a Pie chart
	 */
	public static synchronized void printDumpStats() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("/Users/adel/workspace/jalen-maven/dumpData.txt"));
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
	 * Print all resources values for all instrumented methods
	 */
	public static synchronized void printMethodsResources() {
		for (ResourcesMapPerMethod rmp : jalen.console.resources.ResourcesMap.getInstance().getMap()) {
			System.out.println();
			System.out.println(rmp.getName());
			System.out.println();
			for (Resource rs : rmp.getResources()) {
				System.out.println(rs.getName() + ": " + rs.getValue());
			}
		}

		System.out.println("--------------------");
	}

	/**
	 * Print the name of all instrumented methods
	 */
	public static synchronized void printInstrumentedMethods() {
		for (ResourcesMapPerMethod rmp : jalen.console.resources.ResourcesMap.getInstance().getMap()) {
			System.out.println(rmp.getName());
		}
	}

}
