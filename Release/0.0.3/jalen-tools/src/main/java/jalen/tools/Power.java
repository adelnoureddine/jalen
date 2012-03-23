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

package jalen.tools;

public class Power {

	public double cpu;
	public double network;

	public Power() {
		this.cpu = 0.0;
		this.network = 0.0;
	}

	public Power(double cpu, double network) {
		this.cpu = cpu;
		this.network = network;
	}

	public double getCpu() {
		return cpu;
	}

	public double getNetwork() {
		return network;
	}

	public void setCpu(double cpu) {
		this.cpu = cpu;
	}

	public void setNetwork(double network) {
		this.network = network;
	}
	
	public void addCpu(double cpu) {
		this.cpu += cpu;
	}

	public void addNetwork(double network) {
		this.network += network;
	}

}
