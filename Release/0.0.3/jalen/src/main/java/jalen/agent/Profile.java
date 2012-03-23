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

public class Profile {

	private static String methName = "";
	private static Long startTime = 0L;
	
	/**
	 * To call when a methods starts
	 * 
	 * @param className
	 * @param methodName
	 */
	public static void start(String className, String methodName) {
		StringBuilder fullMethodName = new StringBuilder(className).append('.').append(methodName).append('-').append(Thread.currentThread().getId());
		methName = fullMethodName.toString();
		startTime = System.currentTimeMillis();
		Agent.methStart.put(methName, startTime);
		Agent.methExec.put(methName, true);
	}

	/**
	 * To call when a methods ends
	 * 
	 * @param className
	 * @param methodName
	 */
	public static void end(String className, String methodName) {
		Agent.methExec.put(methName, false);
		Long endTime = System.currentTimeMillis();
		
		if (! Agent.methExecDuration.containsKey(methName)) {
			Agent.methExecDuration.put(methName, endTime - startTime);
		}
		else {
			Long newDuration = Agent.methExecDuration.get(methName) + (endTime - startTime);
			Agent.methExecDuration.put(methName, newDuration);
		}
	}

}
