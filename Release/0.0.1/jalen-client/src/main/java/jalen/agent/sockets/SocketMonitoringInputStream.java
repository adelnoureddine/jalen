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
package jalen.agent.sockets;

import jalen.agent.Agent;

import java.io.*;
import java.net.Socket;

public class SocketMonitoringInputStream extends InputStream {
	private final Socket socket;
	private final InputStream in;

	public SocketMonitoringInputStream(Socket socket,
			InputStream in)
					throws IOException {
		this.socket = socket;
		this.in = in;
	}

	public int read() throws IOException {
		int result = in.read();
		if (result != -1) {
			SocketMonitoringSystem.getInstance().read(socket, result);
		}    

		// Do not calculate RMI TCP thread bytes because it is due to the JMX server bundled in the agent
		if (! Thread.currentThread().getName().startsWith("RMI TCP")) {
			// Get method name from thread stack strace
			Throwable t = new Throwable(); 
			StackTraceElement[] elements = t.getStackTrace();

			String methodName = elements[elements.length - 1].toString();
			String correctMethodName = Utils.correctMethodName(methodName);

			// Store data to agent map
			if (Agent.methRead.containsKey(correctMethodName)) {
				int bytes = Integer.valueOf(Agent.methRead.get(correctMethodName)) + 4;
				Agent.methRead.put(correctMethodName, String.valueOf(bytes));
			}
			else {
				// An int is always 4 bytes
				Agent.methRead.put(correctMethodName, "4");
			}
		}

		return result;
	}

	public int read(byte[] b, int off, int len)
			throws IOException {
		int length = in.read(b, off, len);
		if (length != -1) {
			SocketMonitoringSystem.getInstance().
			read(socket, b, off, length);
		}

		// Do not calculate RMI TCP thread bytes because it is due to the JMX server bundled in the agent
		if (! Thread.currentThread().getName().startsWith("RMI TCP")) {
			// Get method name from thread stack strace
			Throwable t = new Throwable(); 
			StackTraceElement[] elements = t.getStackTrace();

			String methodName = elements[elements.length - 1].toString();
			String correctMethodName = Utils.correctMethodName(methodName);

			// Store data to agent map
			if (Agent.methRead.containsKey(correctMethodName)) {
				int bytes = Integer.valueOf(Agent.methRead.get(correctMethodName)) + length;
				Agent.methRead.put(correctMethodName, String.valueOf(bytes));
			}
			else {
				Agent.methRead.put(correctMethodName, String.valueOf(length));
			}
		}

		return length;
	}
}
