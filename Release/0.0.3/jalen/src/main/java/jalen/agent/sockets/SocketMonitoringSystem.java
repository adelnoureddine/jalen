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


import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class SocketMonitoringSystem {
  private final static SocketMonitoringSystem instance =
      new SocketMonitoringSystem();

  public static SocketMonitoringSystem getInstance() {
    return instance;
  }

  /**
   * This should only be called if we want to use the reflection
   * approach (Delegator).  If we use Aspects, we should not call
   * this method.
   */
  public static void initForDelegator() throws IOException {
    SocketImplFactory socketImplFactory =
        new MonitoringSocketFactory();
    Socket.setSocketImplFactory(socketImplFactory);
    ServerSocket.setSocketFactory(socketImplFactory);
  }

  private SocketMonitoringSystem() {
  }

  private final Collection<SocketMonitor> monitors =
      new CopyOnWriteArraySet<SocketMonitor>();

  public void add(SocketMonitor monitor) {
    monitors.add(monitor);
  }

  public void remove(SocketMonitor monitor) {
    monitors.remove(monitor);
  }

  public void write(Socket socket, int data)
      throws IOException {
    for (SocketMonitor monitor : monitors) {
      monitor.write(socket, data);
    }
  }

  public void write(Socket socket,
                    byte[] data, int offset, int length)
      throws IOException {
    for (SocketMonitor monitor : monitors) {
      monitor.write(socket, data, offset, length);
    }
  }

  public void read(Socket socket, int data)
      throws IOException {
    for (SocketMonitor monitor : monitors) {
      monitor.read(socket, data);
    }
  }

  public void read(Socket socket,
                   byte[] data, int offset, int length)
      throws IOException {
    for (SocketMonitor monitor : monitors) {
      monitor.read(socket, data, offset, length);
    }
  }
}
