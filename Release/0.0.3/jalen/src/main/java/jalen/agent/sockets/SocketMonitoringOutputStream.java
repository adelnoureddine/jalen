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

package jalen.agent.sockets;

import jalen.agent.Agent;

import java.io.*;
import java.net.Socket;

public class SocketMonitoringOutputStream extends OutputStream {
	private final OutputStream out;
	private final Socket socket;

	public SocketMonitoringOutputStream(Socket socket,
			OutputStream out)
					throws IOException {
		this.out = out;
		this.socket = socket;
	}

	public void write(int b) throws IOException {
		out.write(b);
		SocketMonitoringSystem.getInstance().write(socket, b);

		// Do not calculate RMI TCP thread bytes because it is due to the JMX server bundled in the agent
		if (! Thread.currentThread().getName().startsWith("RMI TCP")) {
			// Get method name from thread stack strace
			Throwable t = new Throwable(); 
			StackTraceElement[] elements = t.getStackTrace();

			String methodName = elements[elements.length - 1].toString();
			String correctMethodName = Utils.correctMethodName(methodName);

			// Store data to agent map
			if (Agent.methTransmitted.containsKey(correctMethodName)) {
				Long bytes = Agent.methTransmitted.get(correctMethodName) + 4;
				Agent.methTransmitted.put(correctMethodName, bytes);
			}
			else {
				// An int is always 4 bytes
				Agent.methTransmitted.put(correctMethodName, 4L);
			}
			Agent.totalTransmitted += 4;
		}
	}

	public void write(byte[] b, int off, int length)
			throws IOException {
		out.write(b, off, length);
		SocketMonitoringSystem.getInstance().
		write(socket, b, off, length);

		// Do not calculate RMI TCP thread bytes because it is due to the JMX server bundled in the agent
		if (! Thread.currentThread().getName().startsWith("RMI TCP")) {
			// Get method name from thread stack strace
			Throwable t = new Throwable(); 
			StackTraceElement[] elements = t.getStackTrace();

			String methodName = elements[elements.length - 1].toString();
			String correctMethodName = Utils.correctMethodName(methodName);

			// Store data to agent map
			if (Agent.methTransmitted.containsKey(correctMethodName)) {
				Long bytes = Agent.methTransmitted.get(correctMethodName) + length;
				Agent.methTransmitted.put(correctMethodName, bytes);
			}
			else {
				Agent.methTransmitted.put(correctMethodName, Long.valueOf(length));
			}
			Agent.totalTransmitted += length;
		}
	}

}
