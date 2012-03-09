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

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class PerfMethodAdapter extends MethodAdapter {
	private String _className, _methodName;

	public PerfMethodAdapter(MethodVisitor visitor, String className,
			String methodName) {
		super(visitor);
		_className = className;
		_methodName = methodName;
	}

	public void visitCode() {	
		this.visitLdcInsn(_className);
		this.visitLdcInsn(_methodName);
		
		this.visitMethodInsn(INVOKESTATIC, "jalen/agent/Profile", "start",
				"(Ljava/lang/String;Ljava/lang/String;)V");

		super.visitCode();
	}

	public void visitInsn(int inst) {
		switch (inst) {
		case Opcodes.ARETURN:
		case Opcodes.DRETURN:
		case Opcodes.FRETURN:
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			this.visitLdcInsn(_className);
			this.visitLdcInsn(_methodName);
			this.visitMethodInsn(INVOKESTATIC, "jalen/agent/Profile", "end",
					"(Ljava/lang/String;Ljava/lang/String;)V");
			break;
		default:
			break;
		}

		super.visitInsn(inst);
	}

}
