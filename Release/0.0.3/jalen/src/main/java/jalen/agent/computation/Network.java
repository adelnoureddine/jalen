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

package jalen.agent.computation;

import jalen.agent.Agent;

import java.util.HashMap;
import java.util.Map;

public class Network {
	
	public static Double getProcessNetworkPower() {
		return 5.0;
	}
	
	public static Map<String, Double> getMethNetworkPower() {
		Map<String, Double> methNetworkPowerMap = new HashMap<String, Double>();
		Double processNetworkPower = Network.getProcessNetworkPower();
		
		for (Map.Entry<String, Long> entry : Agent.methTransmitted.entrySet()) {
			Double methNetworkPower = (entry.getValue() * processNetworkPower) / Agent.totalTransmitted;
			methNetworkPowerMap.put(entry.getKey(), methNetworkPower);
		}
		
		return methNetworkPowerMap;
	}

}