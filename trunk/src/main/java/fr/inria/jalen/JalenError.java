/*
 * Copyright (c) 2013 Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package fr.inria.jalen;

public class JalenError extends Error {
	private static final long serialVersionUID = 1L;

	public JalenError(String s) {
		super(s);
	}
	
	public JalenError(String s, Throwable t) {
		super(s, t);
	}
	
	public JalenError(Throwable t) {
		super(t);
	}
}
