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
package jalen.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import jalen.agent.Transformer;
import jalen.agent.mxbean.Statistics;
import jalen.agent.sockets.SocketMonitoringSystem;

public class Agent {

	public static Map<String, String> methCallee = new ConcurrentHashMap<String, String>();
	public static Map<String, String> methDuration = new ConcurrentHashMap<String, String>();
	
	public static Map<String, List<MethodStats>> methStats = new ConcurrentHashMap<String, List<MethodStats>>();
	
	// For socket monitoring
	public static Map<String, String> methRead = new ConcurrentHashMap<String, String>();
	public static Map<String, String> methWrite = new ConcurrentHashMap<String, String>();
	
	public static Long startTime;

	/**
	 * JVM hook to statically load the javaagent at startup.
	 *
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 *
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void premain(String args, Instrumentation inst) {
		Thread.currentThread().setName("Jalen Agent");
		Agent.startTime = System.nanoTime();
		System.out.println("+---------------------------------------------------+");
		System.out.println("| Jalen Agent Version 0.0.1                         |");
		System.out.println("+---------------------------------------------------+");

		inst.addTransformer(new Transformer());

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name;
		try {
			name = new ObjectName("jalen.mxbean:type=Statistics");
			Statistics mbean = new Statistics();
			mbs.registerMBean(mbean, name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// For sockets stats
		try {
			SocketMonitoringSystem.initForDelegator();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("[Jalen Agent stopped]");
			}
		});

	}
}
