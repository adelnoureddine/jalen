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

import jalen.agent.mxbean.StatisticsMXBean;
import jalen.agent.mxbean.ThreadData;
import jalen.console.resources.FormatStatistics;
import jalen.console.resources.Resources;
import jalen.console.resources.ResourcesMap;
import jalen.console.resources.Threads;
import jalen.console.resources.ThreadsMap;
import jalen.console.ui.PieChart;
import jalen.console.util.Computation;
import jalen.console.util.PrintStats;
import jalen.wrapper.CPUEnergy;
import jalen.wrapper.CPUEnergyImpl;
import jalen.wrapper.NetworkEnergy;
import jalen.wrapper.NetworkEnergyImpl;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.management.*;
import javax.management.remote.*;
import javax.management.ObjectName;

public class Client {
	
	public static PieChart wcpu, wnet;
	public static int pid;
	public static String dumpFile;

	public static class ClientListener implements NotificationListener {
		public void handleNotification(Notification notification,
				Object handback) {
			System.out.println("\nReceived notification:");
			System.out.println("\tClassName: "
					+ notification.getClass().getName());
			System.out.println("\tSource: " + notification.getSource());
			System.out.println("\tType: " + notification.getType());
			System.out.println("\tMessage: " + notification.getMessage());
			if (notification instanceof AttributeChangeNotification) {
				AttributeChangeNotification acn = (AttributeChangeNotification) notification;
				System.out
				.println("\tAttributeName: " + acn.getAttributeName());
				System.out
				.println("\tAttributeType: " + acn.getAttributeType());
				System.out.println("\tNewValue: " + acn.getNewValue());
				System.out.println("\tOldValue: " + acn.getOldValue());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Client.dumpFile = args[0];
		System.out.println("Dump file: " + dumpFile);
		System.out.print("Connecting...");

		// Create an RMI connector client and
		// connect it to the RMI connector server
		JMXServiceURL url = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");

		Hashtable<String, String[]> env = new Hashtable<String, String[]>();
		String[] credentials = new String[] { "controlRole", "R&D" };
		env.put(JMXConnector.CREDENTIALS, credentials);
		final JMXConnector jmxc = JMXConnectorFactory.connect(url, env);

		System.out.println("OK");

		// Get an MBeanServerConnection
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		// Perform Operation on Statistics MXBean

		// Construct the ObjectName for the Statistics MXBean
		ObjectName mxbeanName = new ObjectName("jalen.mxbean:type=Statistics");

		// Create a dedicated proxy for the MXBean instead of going directly through the MXBean server connection
		final StatisticsMXBean stats = JMX.newMXBeanProxy(mbsc, mxbeanName, StatisticsMXBean.class);

		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setName("Jalen Monitoring JMX Client");

				CPUEnergy ce = new CPUEnergyImpl();
				NetworkEnergy ne = new NetworkEnergyImpl();
				Client.pid = stats.getPID();
				System.out.println("Java Process ID: " + Client.pid);
				
				// Creating graphic client
				Client.wcpu = new PieChart(Client.pid, "CPU");
				Client.wnet = new PieChart(Client.pid, "Network");
				
				Long cycleDuration = 1000L;
				ResourcesMap.getInstance().setCycleDuration(cycleDuration);
				ResourcesMap.getInstance().setAgentStartTime(stats.getStartTime());

				while (true) {
					try {
						double cpuEnergyOfProcess = ce.getCPUEnergy(Client.pid, cycleDuration);
						double networkEnergyOfProcess = ne.getNetworkEnergy(Client.pid, cycleDuration);
						
						// Set Agent Last Time
						ResourcesMap.getInstance().setAgentLastTime(stats.getCurrentTime());

						// Do the thread computation (before)
						List<ThreadData> ltd = stats.getThreadData();
						Computation.updateThreads(ltd);
						Computation.addBeforeThreadData(ltd);
						
						// Sleep
						Thread.sleep(cycleDuration);
						
						// Set Agent Current Time (after sleep)
						ResourcesMap.getInstance().setAgentCurrentTime(stats.getCurrentTime());
						ResourcesMap.getInstance().setAgentDuration(ResourcesMap.getInstance().getAgentCurrentTime() - ResourcesMap.getInstance().getAgentLastTime());
						
						// Format CPU Stats
						FormatStatistics.filterResourcesMap(stats.getMethDuration(ResourcesMap.getInstance().getAgentLastTime()), Resources.LAST_DURATION);
						
						FormatStatistics.formatThreadId();
											
						FormatStatistics.filterResourcesMap(stats.getMethCallee(), Resources.CALLEE_METHOD);
						
						// Format Network Stats
						FormatStatistics.filterResourcesMap(stats.getMethRead(), Resources.BYTES_READ);
						FormatStatistics.filterResourcesMap(stats.getMethWrite(), Resources.BYTES_WRITE);
						
						// Compute sockets energy
						Computation.computeSocketsEnergy(networkEnergyOfProcess);

						// Do the thread computation (after)
						ltd = stats.getThreadData();
						Computation.addAfterThreadData(ltd);
						
						// Compute cpu energy
						for (Threads t : ThreadsMap.getInstance().getMap()) {
							Computation.correlateMethodsToThread(t.getThreadId(), cpuEnergyOfProcess);
						}

						// Print Statistics data
						PrintStats.printEnergyStats(cpuEnergyOfProcess, networkEnergyOfProcess);
//						PrintStats.printMethodsResources();

						FormatStatistics.resetEnergy();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
//					PrintStats.printDumpStats();
				}
			}
		}.start();

		// Close MBeanServer connection
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {				
					jmxc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println("[Jalen JMX Client closed]");
			}
		});
	}

}
