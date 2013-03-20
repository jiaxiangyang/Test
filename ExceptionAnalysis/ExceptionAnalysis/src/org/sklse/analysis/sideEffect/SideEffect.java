package org.sklse.analysis.sideEffect;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.SootClass;
import soot.jimple.Stmt;

public class SideEffect {
	SootClass mutatedClass;
	String mutatedField;
	Local mutatedLocal;
	Set<String> signature;

	public SideEffect(Local mutatedLocal, String mutatedField,
			Set<String> signature) {
		super();
		this.mutatedField = mutatedField;
		this.mutatedLocal = mutatedLocal;
		this.signature = signature;
	}

	public SideEffect(SootClass mutatedClass, String mutatedField) {
		super();
		this.mutatedClass = mutatedClass;
		this.mutatedField = mutatedField;
		Set<String> signature = new HashSet<String>();
		signature.add(SideEffectFlowFact.DATAHEAD_GLOBAL + "_"
				+ mutatedClass.getName() + "_" + mutatedField);
		this.signature = signature;
	}

	public SootClass getMutatedClass() {
		return mutatedClass;
	}

	public void setMutatedClass(SootClass mutatedClass) {
		this.mutatedClass = mutatedClass;
	}

	public String getMutatedField() {
		return mutatedField;
	}

	public void setMutatedField(String mutatedField) {
		this.mutatedField = mutatedField;
	}

	public Local getMutatedLocal() {
		return mutatedLocal;
	}

	public void setMutatedLocal(Local mutatedLocal) {
		this.mutatedLocal = mutatedLocal;
	}

	

	public Set<String> getSignature() {
		return signature;
	}

	public void setSignature(Set<String> signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return signature != null ? signature.toString():"";
	}

}
