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

package jalen.agent;

import jalen.agent.computation.CPU;
import jalen.agent.computation.Network;
import jalen.agent.sockets.SocketMonitoringSystem;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Agent {

	// For time monitoring
	public static Map<String, Long> methStart = new ConcurrentHashMap<String, Long>();
	public static Map<String, Boolean> methExec = new ConcurrentHashMap<String, Boolean>();
	public static Map<String, Long> methExecDuration = new HashMap<String, Long>();
	public static Map<String, Long> methCycleDuration = new HashMap<String, Long>();
	public static Map<String, Long> methLastDuration = new HashMap<String, Long>();
	
	// For socket monitoring
	public static Map<String, Long> methTransmitted = new HashMap<String, Long>();
	public static Long totalTransmitted = 0L;
	
	// For cycle time
	public static Long lastCPUComputationTime = System.currentTimeMillis();
	
	// For thread cpu time
	public static Map<Long, Double> lastThreadCPUTime = new HashMap<Long, Double>();
	public static Map<Long, Double> threadCPUTime = new HashMap<Long, Double>();
	
	public static String dumpFile = "/Users/adel/Desktop/dumpData.csv";
	public static DecimalFormat twoDigit = new DecimalFormat("#,##0.00");
	
	/**
	 * JVM hook to statically load the java agent at startup.
	 *
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 */
	public static void premain(String args, Instrumentation inst) {
		Thread.currentThread().setName("Jalen Agent");
		System.out.println("+---------------------------------------------------+");
		System.out.println("| Jalen Agent Version 0.0.3                         |");
		System.out.println("+---------------------------------------------------+");

		inst.addTransformer(new Transformer());

		// For sockets stats
		try {
			SocketMonitoringSystem.initForDelegator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		new Thread() {
			public void run(){
				Properties prop = new Properties();

				try {
					//load a properties file
					prop.load(new FileInputStream("config.properties"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				Agent.dumpFile = prop.getProperty("dump-file");
				int tsleep = Integer.valueOf(prop.getProperty("cycle-duration"));
				String outputData = prop.getProperty("output-data");

				while (true) {
					try {
						Thread.sleep(tsleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Map<String, Double> methCPUPower = CPU.getMethCPUPower();
					Map<String, Double> methNetworkPower = Network.getMethNetworkPower();
					Long timeStamp = System.currentTimeMillis();

					for (Map.Entry<String, Long> entry : Agent.methStart.entrySet()) {
						String methName = entry.getKey(), methData = "";
						Double cpuPower = 0.0;
                        Double networkPower = 0.0;

                        if (methCPUPower.containsKey(methName))
							cpuPower = methCPUPower.get(methName);
						if (methNetworkPower.containsKey(methName))
							networkPower = methNetworkPower.get(methName);

						methData = methName + ";" + timeStamp + ";" + Agent.twoDigit.format(cpuPower) + ";" + Agent.twoDigit.format(networkPower) + "\n";

						if (outputData.equals("none"))
							continue;
						else if (outputData.equals("file"))
							Agent.appendToFile(methData);
						else if (outputData.equals("console"))
							System.out.println(methData);
						else if (outputData.equals("file-console")) {
							System.out.println(methData);
							Agent.appendToFile(methData);
						}
					}

				}
			}
		}.start();
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("[Jalen Agent stopped]");
			}
		});

	}
	
	public static void appendToFile(String methData) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(Agent.dumpFile, true));
			out.write(methData);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
