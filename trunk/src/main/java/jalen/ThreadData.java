/*
 * Copyright (c) 2013, Adel Noureddine, Inria, University Lille 1.
  * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen;


public class ThreadData {

	/**
	 * Thread
	 */
	public Thread thread;

	/**
	 * Thread ID
	 */
	public Long id;

	/**
	 * Thread name
 	 */
	public String name;

	/**
	 * Thread CPU Time as given by the JVM
	 */
	public Long cpuTime = 0L;

	/**
	 * CPU Energy consumed by this thread
	 * <br />
	 * This is calculated after a full cycle of PowerAPI
	 * and therefore after multiple cycles of Jalen
	 */
	public Double cpuEnergy = 0.0;

	/**
	 * Disk Energy consumed by this thread
	 * <br />
	 * This is calculated after a full cycle of PowerAPI
	 * and therefore after multiple cycles of Jalen
	 */
	public Double diskEnergy = 0.0;

	/**
	 * Stracktrace of thread at the snapshot time
 	 */
	public StackTraceElement[] stackTraces;

	/**
	 * Tell if a method with disk access is present in the stack trace
	 * <br />
	 * True if method in package java.io or java.nio is present, else otherwise
	 */
	public boolean diskAccess = false;

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param thread
	 * @param cpuTime
	 * @param stackTraces
	 */
	public ThreadData(Long id, String name, Thread thread, Long cpuTime, StackTraceElement[] stackTraces) {
		this.id = id;
		this.name = name;
		this.thread = thread;
		this.cpuTime = cpuTime;
		this.stackTraces = stackTraces;
	}

	public boolean diskAccessed() {
		// Loop through stacktrace to search for java.io or java.nio
		String methName = "";

		for (StackTraceElement ste : this.stackTraces) {
			methName = ste.getClassName() + "." + ste.getMethodName();
			if (methName.startsWith("java.io") || methName.startsWith("java.nio")) {
				this.diskAccess = true;
				return true;
			}
		}

		return false;
	}

}
