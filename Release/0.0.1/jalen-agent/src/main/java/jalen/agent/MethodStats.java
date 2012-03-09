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

public class MethodStats {
	public Long startTime, endTime = null, threadId;
	public String calleeMethod, methodName;

	public MethodStats() {}
	
	public String toString() {
		return this.methodName + " (" + this.calleeMethod + ", " + this.threadId + "): " 
				+ this.startTime + " -- " + this.endTime;
	}
}
