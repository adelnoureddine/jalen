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

public class Utils {
	
	public static String correctMethodName(String name) {
		String newName = name.substring(0, name.indexOf('('));

		// Replace . with /, then replace last / with a .
		newName = newName.replace('.', '/');
		int lastIndex = newName.lastIndexOf('/');		
		newName = newName.substring(0,lastIndex) + "." + newName.substring(lastIndex + 1);
		return newName;
	}
}
