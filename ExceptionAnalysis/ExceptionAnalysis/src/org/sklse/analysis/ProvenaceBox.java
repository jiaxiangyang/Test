package org.sklse.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sklse.analysis.resource.ResourceFlowFact;
import org.sklse.analysis.sideEffect.SideEffectFlowFact;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

public class ProvenaceBox {
	public Set<Stmt> allStep=new HashSet<Stmt>();
	public Map<Stmt,List<Stmt>> stepToSuccs=new HashMap<Stmt, List<Stmt>>();
	public Map<Stmt,List<Stmt>> stepToPreds=new HashMap<Stmt, List<Stmt>>();
    
	public SideEffectFlowFact sideEffectflowFact=new SideEffectFlowFact();
	public ResourceFlowFact resouceFlowFact=new ResourceFlowFact();
	
	public void union(ProvenaceBox other){
		this.allStep.addAll(other.allStep);
		this.stepToPreds.putAll(other.stepToPreds);
		this.stepToSuccs.putAll(other.stepToSuccs);
		this.sideEffectflowFact.union(other.sideEffectflowFact);
		this.resouceFlowFact.union(other.resouceFlowFact);
	}
	
	
	
		
	
}
