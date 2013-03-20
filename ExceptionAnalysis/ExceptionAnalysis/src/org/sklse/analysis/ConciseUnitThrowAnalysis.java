package org.sklse.analysis;

import soot.AnySubType;
import soot.RefType;
import soot.Singletons.Global;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.baf.ThrowInst;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.exceptions.AbstractThrowAnalysis;
import soot.toolkits.exceptions.ThrowableSet;
import sun.tools.tree.IfStatement;

public class ConciseUnitThrowAnalysis extends AbstractThrowAnalysis {
	RefType RUNTIME_EXCEPTION = 
			Scene.v().getRefType("java.lang.RuntimeException");
	
	@Override
	public ThrowableSet mightThrow(Unit u) {
		ThrowableSet t=ThrowableSet.Manager.v().EMPTY;
		if(u instanceof InvokeStmt){
			//System.out.println("mightThrow() of "+u);
			InvokeStmt s=(InvokeStmt)u;
			SootMethod m = s.getInvokeExpr().getMethod();
			for(SootClass c:m.getExceptions()){
				t=t.add(AnySubType.v(c.getType()));
			}
			//System.out.print(t.toAbbreviatedString());
		}
		if(u instanceof ThrowStmt){
			ThrowStmt s=(ThrowStmt)u;
			t=t.add(mightThrowExplicitly(s));
		}
		return t;
	}

	@Override
	public ThrowableSet mightThrowImplicitly(ThrowInst t) {
		return ThrowableSet.Manager.v().EMPTY;
	}

	@Override
	public ThrowableSet mightThrowImplicitly(ThrowStmt t) {
		return ThrowableSet.Manager.v().EMPTY;
	}

}
