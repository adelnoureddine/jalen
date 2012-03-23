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

package jalen.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class Transformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		// can only profile classes that will be able to see
		// the Profile class which is loaded by the application classloader
		if (loader != ClassLoader.getSystemClassLoader()) {
			return classfileBuffer;
		}

		// can't profile yourself
		// className is package/className
		if (className.startsWith("jalen") || className.startsWith("java")) {
			return classfileBuffer;
		}

		byte[] result = classfileBuffer;
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassAdapter adapter = new PerfClassAdapter(writer, className);
		reader.accept(adapter, ClassReader.SKIP_DEBUG);
		result = writer.toByteArray();
		return result;
	}
}
