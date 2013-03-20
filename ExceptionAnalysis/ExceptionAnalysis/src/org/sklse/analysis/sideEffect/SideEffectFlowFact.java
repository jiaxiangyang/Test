package org.sklse.analysis.sideEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.jimple.Stmt;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

public class SideEffectFlowFact {
	public static final String DATAHEAD_THIS="$this";
	public static final String DATAHEAD_PARAM="$param";
	public static final String DATAHEAD_GLOBAL="$global";
	public static final String DATAHEAD_INSIDE="$inside";
	
  
	FlowSet guardedLocals=new ArraySparseSet();
	FlowSet possiableGuradedLocal=new ArraySparseSet();
	MultiMap  signature=new HashMultiMap();
	
	Map<Stmt,SideEffect> sideEffects=new HashMap<Stmt,SideEffect>();
	Map<Stmt,SideEffect> possiableSideEffects=new HashMap<Stmt,SideEffect>();
	
	public void union(SideEffectFlowFact other){
		this.possiableGuradedLocal.union(other.possiableGuradedLocal);
		this.guardedLocals.union(other.guardedLocals);
		this.signature.putAll(other.signature);
		this.sideEffects.putAll(other.sideEffects);
		this.possiableSideEffects.putAll(other.possiableSideEffects);
	}
	
}
