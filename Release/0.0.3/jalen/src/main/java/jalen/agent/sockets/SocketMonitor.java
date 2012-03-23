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
import java.net.Socket;

public interface SocketMonitor {
  void write(Socket socket, int data) throws IOException;

  void write(Socket socket, byte[] data, int off, int len)
      throws IOException;

  void read(Socket socket, int data) throws IOException;

  void read(Socket socket, byte[] data, int off, int len)
      throws IOException;
}
