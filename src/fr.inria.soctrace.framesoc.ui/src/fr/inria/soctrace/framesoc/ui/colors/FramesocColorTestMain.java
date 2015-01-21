/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.colors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FramesocColorTestMain {

	private static final int N = 10;
	private static final List<String> names = new ArrayList<>(N);
	private static final String T1 = "MPI_Send";
	private static final String T2 = "MPI_Receive";
	
	static {
		for (int i = 0; i < N; i++) {
			names.add(UUID.randomUUID().toString());
		}
	}

	public static void main(String[] args) {
		System.out.println("---");
		for (String n : names) {
			generate(n);
		}
		System.out.println("---");
		generate(T1);
		generate(T1);
		generate(T1);
		System.out.println("---");
		generate(T2);
		generate(T2);
		generate(T2);
	}

	private static void generate(String s) {
		System.out.println(s + " : " + FramesocColor.generateFramesocColor(s));
	}
	
}
