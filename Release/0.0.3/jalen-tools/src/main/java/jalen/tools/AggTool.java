/*
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 */

package jalen.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class AggTool {

	public static Map<String, Power> methodsPowerMap = new HashMap<String, Power>();
	public static Map<String, Power> classesPowerMap = new HashMap<String, Power>();
	public static Map<String, Power> packagesPowerMap = new HashMap<String, Power>();

	public static double globalCpuPower = 0.0;
	public static double globalNetPower = 0.0;
	
	public static String statsType = "per";

	public static DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

	public static void main(String[] args) {
		System.out.println("+---------------------------------------------------+");
		System.out.println("| Jalen Tools Version 0.0.1                         |");
		System.out.println("+---------------------------------------------------+");

		System.out.println("\n\t Jalen Tools: Power Consumption in watt and/or percentage\n\n");

		readFile(args[0]);
		if (args[1] != null && (args[1].equals("per") || args[1].equals("watt")))
			statsType = args[1];

		System.out.println("\t Power Consumption of Methods\n");
		System.out.println(printPowerData(methodsPowerMap));

		System.out.println("\t Power Consumption of Classes\n");
		System.out.println(printPowerData(classesPowerMap));

		System.out.println("\t Power Consumption of Packages\n");
		System.out.println(printPowerData(packagesPowerMap));
	}

	public static void readFile(String fileName) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;

			while ((str = in.readLine()) != null) {
				String[] data = str.split(";");
				String name = (data[0].indexOf('-') != -1) ? data[0].substring(
						0, data[0].indexOf('-')) : data[0];

				String className = (name.indexOf('.') != -1) ? name.substring(
						0, name.indexOf('.')) : name;

				String packageName = (name.lastIndexOf('/') != -1) ? name.substring(
						0, name.lastIndexOf('/')) : name;

				double cpuPower = Double.valueOf(data[2]);
				double netPower = Double.valueOf(data[3]);

				globalCpuPower += cpuPower;
				globalNetPower += netPower;

				// Fill methods map
				if (methodsPowerMap.containsKey(name)) {
					methodsPowerMap.get(name).addCpu(cpuPower);
					methodsPowerMap.get(name).addNetwork(netPower);
				}
				else {
					Power powerData = new Power(cpuPower, netPower);
					methodsPowerMap.put(name, powerData);
				}

				// Fill classes map
				if (classesPowerMap.containsKey(className)) {
					classesPowerMap.get(className).addCpu(cpuPower);
					classesPowerMap.get(className).addNetwork(netPower);
				}
				else {
					Power powerData = new Power(cpuPower, netPower);
					classesPowerMap.put(className, powerData);
				}

				// Fill packages map
				if (packagesPowerMap.containsKey(packageName)) {
					packagesPowerMap.get(packageName).addCpu(cpuPower);
					packagesPowerMap.get(packageName).addNetwork(netPower);
				}
				else {
					Power powerData = new Power(cpuPower, netPower);
					packagesPowerMap.put(packageName, powerData);
				}
			}
			
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String printPowerData(Map<String, Power> powerMap) {
		String results = String.format("%-50.50s %-20.20s %-20.20s%n", "Name", "CPU", "Network");
		results += "--------------------------------------------------------------------------------\n";
		double cpuPer, netPer;

		for (Map.Entry<String, Power> entry : powerMap.entrySet()) {
			cpuPer = (entry.getValue().getCpu() * 100) / globalCpuPower;
			netPer = (entry.getValue().getNetwork() * 100) / globalNetPower;
			
			if (statsType.equals("per"))
				results += String.format("%-50.50s %-20.20s %-20.20s%n", entry.getKey(), twoDigit.format(cpuPer), twoDigit.format(netPer));
			else if (statsType.equals("watt"))
				results += String.format("%-50.50s %-20.20s %-20.20s%n", entry.getKey(), twoDigit.format(entry.getValue().getCpu()), twoDigit.format(entry.getValue().getNetwork()));
		}
		results += "--------------------------------------------------------------------------------\n";
		return results;
	}
}