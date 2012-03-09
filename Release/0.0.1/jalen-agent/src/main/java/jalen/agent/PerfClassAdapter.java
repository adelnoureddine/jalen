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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

public class PerfClassAdapter extends ClassAdapter {
	private String className;

	public PerfClassAdapter(ClassVisitor visitor, String theClass) {
		super(visitor);
		this.className = theClass;
	}

	public MethodVisitor visitMethod(int arg, String name, String descriptor,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(arg, name, descriptor, signature,
				exceptions);
		MethodAdapter ma = new PerfMethodAdapter(mv, className, name);
		return ma;
	}

}
