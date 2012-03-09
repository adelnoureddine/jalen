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
package jalen.console.resources;

import java.util.Map;

public class FormatStatistics {

	/**
	 * Add resource value to the resource map of each method
	 * @param methListData
	 * @param resourceEnum
	 */
	public static synchronized void filterResourcesMap(Map<String, String> methListData, Resources resourceEnum) {
		for (Map.Entry<String, String> entry : methListData.entrySet()) {
			ResourcesMapPerMethod rmp = null;
			String methodName = entry.getKey();

			if (ResourcesMap.getInstance().containsMethod(methodName)) {
				// Method already have a resources map
				rmp = ResourcesMap.getInstance().getRMP(methodName);
			}
			else {
				// Create a new resources map for method
				rmp = new ResourcesMapPerMethod(methodName);
			}

			// Set resource value
			rmp.setResourceValue(resourceEnum, entry.getValue());

			// Update resources map
			ResourcesMap.getInstance().updateRMP(methodName, rmp);
		}
	}

	/**
	 * Get the callee method of each instrumented method, and fill in called methods for callee
	 * @param stackTrace the stacktrace map of all instrumented methods
	 */
	public static synchronized void filterMethodTree(Map<String, String[]> stackTrace) {
		for (Map.Entry<String, String[]> entry : stackTrace.entrySet()) {
			ResourcesMapPerMethod rmp = null;
			String methodName = entry.getKey();

			rmp = ResourcesMap.getInstance().getRMP(methodName);

			int length = entry.getValue().length;
			String calleeMethodUnformatted = entry.getValue()[length - 1];
			String calleeMethod = calleeMethodUnformatted.substring(0, calleeMethodUnformatted.indexOf('('));

			// Replace . with /, then replace last / with a .
			String newCalleeMethod = calleeMethod.replace('.', '/');
			int lastIndex = newCalleeMethod.lastIndexOf('/');		
			newCalleeMethod = newCalleeMethod.substring(0,lastIndex) + "." + newCalleeMethod.substring(lastIndex + 1);
			
			// Set resource value
			rmp.setResourceValue(Resources.CALLEE_METHOD, newCalleeMethod);

			if (ResourcesMap.getInstance().getRMP(newCalleeMethod) != null)
				ResourcesMap.getInstance().getRMP(newCalleeMethod).addCalledMethod(methodName);

			// Update resources map
			ResourcesMap.getInstance().updateRMP(methodName, rmp);
		}
	}
	
	/**
	 * Get thread id of instrumented method from its name (methodName-threadId)
	 */
	public static void formatThreadId() {
		for (ResourcesMapPerMethod rmp : ResourcesMap.getInstance().getMap()) {
			String threadId = rmp.getName().substring(rmp.getName().indexOf('-') + 1);
			rmp.setResourceValue(Resources.THREAD, threadId);
		}
	}
	
	/**
	 * Reset resources values for all instrumented methods
	 */
	public static void reset() {
		jalen.console.resources.ResourcesMap.getInstance().reset();
	}
	
	/**
	 * Reset energy values for all isntrumented methods
	 */
	public static synchronized void resetEnergy() {
		for (ResourcesMapPerMethod rmp : jalen.console.resources.ResourcesMap.getInstance().getMap()) {
			rmp.setResourceValue(Resources.CPU_ENERGY, null);
			rmp.setResourceValue(Resources.NETWORK_ENERGY, null);
		}
	}
	
}
