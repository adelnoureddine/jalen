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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jalen.agent.sockets.Utils;

public class Profile {

	private static MethodStats ms = new MethodStats();
	
	/**
	 * To call when a methods starts
	 * 
	 * @param className
	 * @param methodName
	 */
	public static void start(String className, String methodName) {
//		Long start = System.nanoTime();
		
		String threadId = "-" + String.valueOf(Thread.currentThread().getId());

		ms.methodName = className.concat(".").concat(methodName).concat(threadId);
		ms.startTime = System.nanoTime();
		ms.threadId = Thread.currentThread().getId();
		
		// Get call tree of method
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		String callee = elements[elements.length - 1].toString();
		callee = Utils.correctMethodName(callee);
		ms.calleeMethod = callee.concat(threadId);
		Agent.methCallee.put(ms.methodName, ms.calleeMethod);
		
		if (Agent.methStats.containsKey(ms.methodName)) {
			// Method have started before, add the ms stats to its list
			Agent.methStats.get(ms.methodName).add(ms);
		}
		else {
			// First call to the method
			List<MethodStats> lms = new CopyOnWriteArrayList<MethodStats>();
			lms.add(ms);
			Agent.methStats.put(ms.methodName, lms);
		}
		
//		Long end = System.nanoTime();
//		System.out.println("Start Overhead: " + (end - start));
	}

	/**
	 * To call when a methods ends
	 * 
	 * @param className
	 * @param methodName
	 */
	public static void end(String className, String methodName) {
//		Long start = System.nanoTime();
		
		ms.endTime = System.nanoTime();
		Agent.methStats.get(ms.methodName).add(Agent.methStats.get(ms.methodName).indexOf(ms), ms);
		
//		Long end = System.nanoTime();
//		System.out.println("Stop Overhead: " + (end - start));
	}

}
