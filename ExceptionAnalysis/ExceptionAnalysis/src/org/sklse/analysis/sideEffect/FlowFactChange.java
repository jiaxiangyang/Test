package org.sklse.analysis.sideEffect;

import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

public class FlowFactChange {
	
	FlowSet gen = new ArraySparseSet();
	FlowSet kill = new ArraySparseSet();
	MultiMap signature = new HashMultiMap();
	FlowSet possiableGuradedLocal = new ArraySparseSet();

	SideEffect sideEffect=null;
	SideEffect possiableSideEffect=null;
	
	public FlowSet getGen() {
		return gen;
	}
	public void setGen(FlowSet gen) {
		this.gen = gen;
	}
	public FlowSet getKill() {
		return kill;
	}
	public void setKill(FlowSet kill) {
		this.kill = kill;
	}
	public MultiMap getSignature() {
		return signature;
	}
	public void setSignature(MultiMap signature) {
		this.signature = signature;
	}
	public FlowSet getPossiableGuradedLocal() {
		return possiableGuradedLocal;
	}
	public void setPossiableGuradedLocal(FlowSet possiableGuradedLocal) {
		this.possiableGuradedLocal = possiableGuradedLocal;
	}
	public SideEffect getSideEffect() {
		return sideEffect;
	}
	public void setSideEffect(SideEffect sideEffect) {
		this.sideEffect = sideEffect;
	}
	public SideEffect getPossiableSideEffect() {
		return possiableSideEffect;
	}
	public void setPossiableSideEffect(SideEffect possiableSideEffect) {
		this.possiableSideEffect = possiableSideEffect;
	}
	
	

}
