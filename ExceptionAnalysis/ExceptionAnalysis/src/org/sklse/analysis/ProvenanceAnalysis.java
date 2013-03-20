package org.sklse.analysis;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee <navindra@cs.mcgill.ca>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

import java.util.*;

import org.sklse.analysis.resource.ResourceAnaylysis;
import org.sklse.analysis.sideEffect.FlowFactChange;
import org.sklse.analysis.sideEffect.SideEffect;
import org.sklse.analysis.sideEffect.SideEffectAnalysis;

import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

/**
 * Flow analysis to determine all locals guaranteed to be defined at a given
 * program point.
 **/
public class ProvenanceAnalysis extends ForwardFlowAnalysis {

	public UnitGraph graph;
	public SideEffectAnalysis sideEffectAnalysis = new SideEffectAnalysis();
	public ResourceAnaylysis resourceAnalysis = new ResourceAnaylysis();

	public ProvenanceAnalysis(UnitGraph graph) {
		super(graph);
		this.graph = graph;
		sideEffectAnalysis.init(graph);

		// DominatorsFinder df = new MHGDominatorsFinder(graph);
		// unitToGenerateSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1,
		// 0.7f);
		//
		// pre-compute generate sets
		// for (Iterator unitIt = graph.iterator(); unitIt.hasNext();) {
		// Unit s = (Unit) unitIt.next();
		// FlowSet genSet = emptySet.clone();
		//		
		// for (Iterator domsIt = df.getDominators(s).iterator(); domsIt
		// .hasNext();) {
		// Unit dom = (Unit) domsIt.next();
		// for (Iterator boxIt = dom.getDefBoxes().iterator(); boxIt
		// .hasNext();) {
		// ValueBox box = (ValueBox) boxIt.next();
		// if (box.getValue() instanceof Local)
		// genSet.add(box.getValue(), genSet);
		// }
		// }
		//		
		// unitToGenerateSet.put(s, genSet);
		// }

		doAnalysis();
	}

	/**
	 * All INs are initialized to the empty set.
	 **/
	protected Object newInitialFlow() {
		return new ProvenaceBox();
	}

	/**
	 * IN(Start) is the empty set
	 **/
	protected Object entryInitialFlow() {
		return new ProvenaceBox();
	}

	/**
	 * OUT is the same as IN plus the genSet.
	 **/
	protected void flowThrough(Object inValue, Object unit, Object outValue) {

		ProvenaceBox in = (ProvenaceBox) inValue, out = (ProvenaceBox) outValue;
		Stmt stmt = (Stmt) unit;
		out.union(in);
		boolean  recorded = this.sideEffectAnalysis.flowThrough(in,
				stmt, out);
		

		// else if (unit instanceof ReturnStmt) {
		// flowThroughReturn(in, (ReturnStmt) unit,, gen,kill);
		// } else if (unit instanceof ThrowStmt) {
		// flowThroughThrow(in, (ThrowStmt) unit, out);
		// }
		//
		//buildOutFact(in, out, change, stmt);
	}

	/**
	 * All paths == Intersection.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		System.out.println("merge:{" + in1 + "} with {" + in2 + "}");
		ProvenaceBox inBox1 = (ProvenaceBox) in1, inBox2 = (ProvenaceBox) in2, outBox = (ProvenaceBox) out;
		outBox.union(inBox1);
		outBox.union(inBox2);
	}

	protected void copy(Object source, Object dest) {
		ProvenaceBox sourceBox = (ProvenaceBox) source, destBox = (ProvenaceBox) dest;
		destBox.union(sourceBox);
	}

}
