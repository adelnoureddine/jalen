/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen;

/**
 * OS validator class provides utility methods
 * to check the OS of the system
 */
public final class OSValidator {

	/**
	 * Private constructor
	 */
	private OSValidator() {}

	private static String os = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return OSValidator.os.contains("win");
	}

	public static boolean isMac() {
		return OSValidator.os.contains("mac");
	}

	public static boolean isUnix() {
		return (OSValidator.os.contains("nix") || OSValidator.os.contains("nux"));
	}

}