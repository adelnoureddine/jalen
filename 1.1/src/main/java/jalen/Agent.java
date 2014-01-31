/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen;

import jalen.formulas.cpu.CPUFormulaDVFS;
import jalen.formulas.cpu.CPUFormulaMaxFrequency;
import jalen.formulas.cpu.CPUFormulasInterface;
import jalen.formulas.disk.DiskFormulasInterface;
import jalen.formulas.disk.DiskFormulasProc;
import jalen.sensors.cpu.CPUSensorDVFS;
import jalen.sensors.cpu.CPUSensorSigarMaxFrequency;
import jalen.sensors.cpu.CPUSensorsInterface;
import jalen.sensors.disk.DiskSensorProc;
import jalen.sensors.disk.DiskSensorsInterface;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class Agent {

	// Samples statistics

	// List of samplerList collected during the execution of the program
	private static List<SamplerList> samplerList = new ArrayList<>();

	private static Map<Long, Long> lastCPUTime = new HashMap<>();

	// List of method data
	private static Map<String, Double> methNetCPUEnergy = new HashMap<>();
	private static Map<String, Double> methNetLibraryCPUEnergy = new HashMap<>();

	private static Map<String, Integer> methNetCalls = new HashMap<>();
	private static Map<String, Integer> methNetLibraryCalls = new HashMap<>();

	private static Map<String, Double> methNetDiskEnergy = new HashMap<>();
	private static Map<String, Double> methNetLibraryDiskEnergy = new HashMap<>();

	private static Map<String, String> methNet = new HashMap<>();
	private static Map<String, String> methNetLibrary = new HashMap<>();

	private static String filterMethodName = "";
	private static String userDir = System.getProperty("user.dir");

	// For cycle time
	protected static Long lastCPUComputationTime = System.currentTimeMillis();
	private static int appCycleDuration = 500; // In milliseconds
	private static int jalenCycleDuration = 10; // In milliseconds

	// Sensors and formulas
	private static CPUSensorsInterface cpuSensor;
	protected static CPUFormulasInterface cpuFormula;
	private static DiskSensorsInterface diskSensor;
	protected static DiskFormulasInterface diskFormula;

	// Hardware information
	private static Double cpuTDP, cpuTDPFactor = 0.7;
	private static String cpuFrequenciesVoltages;
	private static Double diskReadPower, diskReadRate, diskWritePower, diskWriteRate;

	public static final Logger LOGGER = Logger.getLogger(Agent.class.getName());


	/**
	 * Private constructor
	 */
	private Agent() {}

	/**
	 * JVM hook to statically load the java agent at startup.
	 * <p/>
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 */
	public static void premain(String args, Instrumentation inst) {

		Thread.currentThread().setName("Jalen Agent Threads");
		System.out.println("+---------------------------------------------------+");
		System.out.println("| Jalen Agent Version 1.1                           |");
		System.out.println("+---------------------------------------------------+");


		// Initialize logger
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new JalenFormatter());
		Agent.LOGGER.addHandler(ch);
		Agent.LOGGER.setLevel(Level.CONFIG);
		Agent.LOGGER.setUseParentHandlers(false);


		ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
		// Check if CPU Time measurement is supported by the JVM. Quit otherwise
		if (! mxbean.isThreadCpuTimeSupported()) {
			Agent.LOGGER.log(Level.SEVERE, "Thread CPU Time is not supported on this Java Virtual Machine");
			System.exit(1);
		}

		// Enable CPU Time measurement if it is disabled
		if (! mxbean.isThreadCpuTimeEnabled())
			mxbean.setThreadCpuTimeEnabled(true);

		Agent.LOGGER.log(Level.INFO, "Loading properties");

		// Read properties file
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("./config.properties");
			prop.load(fis);
		} catch (IOException e) {
			Agent.LOGGER.log(Level.SEVERE, "No config.properties file found in current directory: " + Agent.userDir);
			System.exit(1);
		} finally {
			try {
			if (fis != null)
				fis.close();
			} catch (IOException e) {
				Agent.LOGGER.log(Level.WARNING, e.getMessage());
			}
		}

		// Upate parameters from properties file
		Agent.filterMethodName = prop.getProperty("filter-method-name");

		// Hardware data
		Agent.cpuTDP = Double.valueOf(prop.getProperty("cpu-tdp"));
		Agent.cpuFrequenciesVoltages = prop.getProperty("cpu-frequencies-voltages");
		Agent.diskReadPower = Double.valueOf(prop.getProperty("disk-read-power"));
		Agent.diskReadRate = Double.valueOf(prop.getProperty("disk-read-rate"));
		Agent.diskWritePower = Double.valueOf(prop.getProperty("disk-write-power"));
		Agent.diskWriteRate = Double.valueOf(prop.getProperty("disk-write-rate"));

		// Fill frequencies and voltages map
		String[] cpuFrequenciesVoltagesArray = Agent.cpuFrequenciesVoltages.split(";");
		Map<Double, Double> frequenciesVoltages = new HashMap<Double, Double>();
		Map<Double, Double> frequenciesMap = new HashMap<Double, Double>();
		int iArray = 0;
		while (iArray < cpuFrequenciesVoltagesArray.length) {
			Double frequency = Double.valueOf(cpuFrequenciesVoltagesArray[iArray]);
			Double voltage = Double.valueOf(cpuFrequenciesVoltagesArray[iArray+1]);
			frequenciesVoltages.put(frequency, voltage);
			frequenciesMap.put(frequency, 0.0);
			iArray += 2;
		}


		// Get Process ID of current application
		String mxbeanName = ManagementFactory.getRuntimeMXBean().getName();
		final int appPid = Integer.valueOf(mxbeanName.substring(0, mxbeanName.indexOf('@')));

		Agent.addToJavaLibraryPath(new File(System.getProperty("user.dir") + "/lib/"));

		// Run sensors and formulas
		Agent.LOGGER.log(Level.INFO, "Loading energy modules");

		// CPU
		if (! OSValidator.isUnix()) {
			// All systems except Linux-based
			Agent.cpuSensor = new CPUSensorSigarMaxFrequency(appPid);
			Agent.cpuFormula = new CPUFormulaMaxFrequency(Agent.cpuTDP, Agent.cpuTDPFactor, Agent.cpuSensor);

			Agent.LOGGER.log(Level.INFO, "CPU...OK -- Max frequency");
			Agent.LOGGER.log(Level.WARNING, "Disk...Fail -- Only supported on Linux-based systems");
		} else {
			// Linux-based systems
			try {
				// First attempt to use DVFS
				Agent.cpuSensor = new CPUSensorDVFS(appPid, frequenciesMap);
				Agent.cpuFormula = new CPUFormulaDVFS(Agent.cpuTDP, Agent.cpuTDPFactor, Agent.cpuSensor, frequenciesVoltages);
				Agent.LOGGER.log(Level.INFO, "CPU...OK -- DVFS");
			} catch (Exception e) {
				// Fail to use DVFS, then use max frequency
				Agent.cpuSensor = new CPUSensorSigarMaxFrequency(appPid);
				Agent.cpuFormula = new CPUFormulaMaxFrequency(Agent.cpuTDP, Agent.cpuTDPFactor, Agent.cpuSensor);
				Agent.LOGGER.log(Level.INFO, "CPU...OK -- Max frequency");
			}

			// Run disk monitoring
			// Linux-based systems only
			Agent.diskSensor = new DiskSensorProc(appPid);
			Agent.diskFormula = new DiskFormulasProc(Agent.diskReadPower, Agent.diskReadRate, Agent.diskWritePower, Agent.diskWriteRate, Agent.diskSensor);

			Agent.LOGGER.log(Level.INFO, "Disk...OK");
		}

		/**
		 * Thread to calculate at runtime the power consumption per thread following a determined cycle duration
		 */
		new Thread() {
			public void run() {
				Thread.currentThread().setName("Jalen Agent Computation");

				Agent.LOGGER.log(Level.INFO, "Started monitoring application with ID " + appPid);

				while (true) { // While loop for application cycle
					Long totalExecTime = 0L, jalenExecTime = 0L;
					PowerModel.computeCycleDuration();

					SamplerList samList = new SamplerList();

					Double processCPUPower = 0.0, processCPUEnergy = 0.0;
					Double processDiskPower = 0.0, processDiskEnergy = 0.0;

					processCPUPower = PowerModel.getProcessCPUPower();
					processCPUEnergy = processCPUPower * (PowerModel.getCycleDuration() / 1000.0); // Divide by 1000 to have it in seconds

					processDiskPower = PowerModel.getProcessDiskPower();
					processDiskEnergy = processDiskPower * (PowerModel.getCycleDuration() / 1000.0); // Divide by 1000 to have it in seconds

					samList.cpuEnergy = processCPUEnergy;
					samList.cpuPower = processCPUPower;

					samList.diskEnergy = processDiskEnergy;
					samList.diskPower = processDiskPower;

					while (true) {
						Long startTime = System.currentTimeMillis();
						Sampler sam = new Sampler();

						Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();

						ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
						if (mxbean != null) {
							for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
								Thread key = entry.getKey();
								Long tid = key.getId();

								if (key.getState() == State.RUNNABLE) {
									// Do computiation only if thread is actually running
									// and not waiting or blocked
									Long lastCPUTimeValue = 0L, newCPUTimeValue = 0L;
									Long threadCPUTime = mxbean.getThreadCpuTime(key.getId());

									if (Agent.lastCPUTime.containsKey(tid)) {
										// Thread executed last time
										lastCPUTimeValue = Agent.lastCPUTime.get(tid);
										Agent.lastCPUTime.put(tid, threadCPUTime);
									} else
										Agent.lastCPUTime.put(tid, threadCPUTime);

									newCPUTimeValue = threadCPUTime - lastCPUTimeValue;
									ThreadData ti = new ThreadData(tid, key.getName(), key, newCPUTimeValue, entry.getValue());
									sam.threadList.add(ti);
								}
							}
						}

						synchronized (GLOBALLOCK) {
							samList.samplers.add(sam);
						}

						jalenExecTime = (System.currentTimeMillis() - startTime);
						totalExecTime += jalenExecTime;


						// Compare: Application cycle duration with
						// Total already executed time for sampling + time for next sleep of Jalen
						// +
						// Estimation of next Jalen cycle: last jalen execution time + next sleep time of Jalen
						if (((totalExecTime + Agent.jalenCycleDuration) + (jalenExecTime + Agent.jalenCycleDuration)) >= Agent.appCycleDuration) {
							// Stop Jalen cycle because duration exceeded application cycle
							break;
						}

						try {
							Thread.sleep(Agent.jalenCycleDuration);
						} catch (InterruptedException e) {
							Agent.LOGGER.log(Level.WARNING, e.getMessage());
						}
					}

					synchronized (GLOBALLOCK) {
						Agent.samplerList.add(samList);
					}

					try {
						Thread.sleep(Agent.appCycleDuration);
					} catch (InterruptedException e) {
						Agent.LOGGER.log(Level.WARNING, e.getMessage());
					}

				}

			}
		}.start();


		/**
		 * Code to execute when exiting the program and the agent
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Agent.LOGGER.log(Level.INFO, "Calculating and writing energy data");

				synchronized (GLOBALLOCK) {
					for (SamplerList samList : Agent.samplerList) {
						samList.calculateCPUTimeByID();
						samList.calculateEnergyByID();
						samList.calculateEnergyBySampler();

						samList.calculateDiskAccessNum();
						samList.calculateDiskEnergyBySampler();

						for (Sampler sam : samList.samplers) {
							for (ThreadData td : sam.threadList) {
								// Update here the energy per method
								int o = 0;
								String methName = "";
								for (StackTraceElement ste : td.stackTraces) {
									methName = ste.getClassName() + "." + ste.getMethodName();
									if (o == 0) {
										// Top of stack
										// Add energy to netEnergy for method on top of stack
										// Position 0 is top of stack
										if (Agent.methNetCPUEnergy.containsKey(methName))
											Agent.methNetCPUEnergy.put(methName, Agent.methNetCPUEnergy.get(methName) + td.cpuEnergy);
										else
											Agent.methNetCPUEnergy.put(methName, td.cpuEnergy);

										if (Agent.methNetCalls.containsKey(methName))
											Agent.methNetCalls.put(methName, Agent.methNetCalls.get(methName) + 1);
										else
											Agent.methNetCalls.put(methName, 1);

										if (Agent.methNetDiskEnergy.containsKey(methName))
											Agent.methNetDiskEnergy.put(methName, Agent.methNetDiskEnergy.get(methName) + td.diskEnergy);
										else
											Agent.methNetDiskEnergy.put(methName, td.diskEnergy);
									}

									o++;

									if (methName.startsWith(Agent.filterMethodName)) {
										// Library methods
										// Add energy to NetLibraryEnergy for first method of the library in the stack
										if (Agent.methNetLibraryCPUEnergy.containsKey(methName))
											Agent.methNetLibraryCPUEnergy.put(methName, Agent.methNetLibraryCPUEnergy.get(methName) + td.cpuEnergy);
										else
											Agent.methNetLibraryCPUEnergy.put(methName, td.cpuEnergy);

										if (Agent.methNetLibraryCalls.containsKey(methName))
											Agent.methNetLibraryCalls.put(methName, Agent.methNetLibraryCalls.get(methName) + 1);
										else
											Agent.methNetLibraryCalls.put(methName, 1);

										if (Agent.methNetLibraryDiskEnergy.containsKey(methName))
											Agent.methNetLibraryDiskEnergy.put(methName, Agent.methNetLibraryDiskEnergy.get(methName) + td.diskEnergy);
										else
											Agent.methNetLibraryDiskEnergy.put(methName, td.diskEnergy);

										break;
									}

								}
							}
						}
					}
				}

				String netCon = "", netLibraryCon = "";

				// CPU energy

				for (Map.Entry<String, Double> entry : Agent.methNetCPUEnergy.entrySet()) {
					String key = entry.getKey(); // Method name
					Double value = entry.getValue(); // Method energy

					methNet.put(key, value.toString());
				}


				for (Map.Entry<String, Double> entry : Agent.methNetLibraryCPUEnergy.entrySet()) {
					String key = entry.getKey(); // Method name
					Double value = entry.getValue(); // Method energy

					methNetLibrary.put(key, value.toString());
				}


				// Disk energy

				for (Map.Entry<String, Double> entry : Agent.methNetDiskEnergy.entrySet()) {
					String key = entry.getKey(); // Method name
					Double value = entry.getValue(); // Method energy

					if (methNet.containsKey(key))
						methNet.put(key, methNet.get(key) + ";" + value.toString());
					else
						methNet.put(key, "0.0;" + value.toString());
				}


				for (Map.Entry<String, Double> entry : Agent.methNetLibraryDiskEnergy.entrySet()) {
					String key = entry.getKey(); // Method name
					Double value = entry.getValue(); // Method energy

					if (methNetLibrary.containsKey(key))
						methNetLibrary.put(key, methNetLibrary.get(key) + ";" + value.toString());
					else
						methNetLibrary.put(key, "0.0;" + value.toString());
				}


				// Concatenated values

				StringBuffer buf = new StringBuffer();

				for (Map.Entry<String, String> entry : Agent.methNet.entrySet()) {
					String key = entry.getKey(); // Method name
					String value = entry.getValue(); // Method energy

					buf.append(key + ";" + value + "\n");
				}

				netCon = buf.toString();

				buf = new StringBuffer();

				for (Map.Entry<String, String> entry : Agent.methNetLibrary.entrySet()) {
					String key = entry.getKey(); // Method name
					String value = entry.getValue(); // Method energy

					buf.append(key + ";" + value + "\n");
				}

				netLibraryCon = buf.toString();


				// Concatenated values (CPU, Disk, Number of calls)
				Agent.appendToFile(Agent.userDir + "/net-" + appPid + ".csv", netCon, true);
				Agent.appendToFile(Agent.userDir + "/netLibrary-" + appPid + ".csv", netLibraryCon, true);
			}
		});
	}


	/**
	 * Append string to file
	 * @param fileName filename to add string to
	 * @param methData the string data to add
	 * @param append true to append to file, false to replace file with new data
	 */
	public static void appendToFile(String fileName, String methData, Boolean append) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, append));
			out.write(methData);
			out.close();
		} catch (IOException e) {
			Agent.LOGGER.log(Level.WARNING, e.getMessage());
		}
	}

	/**
	 * Add a new folder to java.library.path
	 * @param dir The new folder to add
	 */
	public static void addToJavaLibraryPath(File dir) {
		final String libraryPath = "java.library.path";
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory.");
		}
		String javaLibraryPath = System.getProperty(libraryPath);
		System.setProperty(libraryPath, javaLibraryPath + File.pathSeparatorChar + dir.getAbsolutePath());

		resetJavaLibraryPath();
	}

	/**
	 * Delete java.library.path cache.
	 * Therefore the classloader is forced to recheck its value at the next library loading.
	 * Only supported for Oracle's JVM
	 */
	public static void resetJavaLibraryPath() {
		synchronized(Runtime.getRuntime()) {
			try {
				Field field = ClassLoader.class.getDeclaredField("usr_paths");
				field.setAccessible(true);
				field.set(null, null);

				field = ClassLoader.class.getDeclaredField("sys_paths");
				field.setAccessible(true);
				field.set(null, null);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				Agent.LOGGER.log(Level.WARNING, e.getMessage());
			}
		}
	}

	/**
	 * Global monitor used to implement mutual-exclusion. In the future this single
	 * monitor may be broken up into many different monitors to reduce contention.
	 */
	static final Object GLOBALLOCK = new GlobalLock();
	public static class GlobalLock {}

}