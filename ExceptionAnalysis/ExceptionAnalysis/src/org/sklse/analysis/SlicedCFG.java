package org.sklse.analysis;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.Stmt;

public class SlicedCFG {
	Set<Stmt> steps = new HashSet<Stmt>();
	Set<Arc> arcs = new HashSet<Arc>();

	public void union(SlicedCFG other) {
		this.steps.addAll(other.steps);
		this.arcs.addAll(other.arcs);
	}

	public class Arc {
		Stmt source;
		Stmt target;
		String label;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Arc) {
				Arc other = (Arc) obj;
				return source.equals(other.source)
						&& target.equals(other.target)&&label.equals(other.label);
			}
			return false;
		}
		
	}

}
