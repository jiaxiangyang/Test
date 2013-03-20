package org.sklse.analysis;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Body;
import soot.RefType;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.baf.Inst;
import soot.baf.NewInst;
import soot.baf.StaticGetInst;
import soot.baf.StaticPutInst;
import soot.baf.ThrowInst;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.exceptions.ThrowableSet;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.ArraySet;

public class ConciseExceptionalUnitGraph  extends ExceptionalUnitGraph{

	public ConciseExceptionalUnitGraph(Body body, ThrowAnalysis throwAnalysis,
			boolean omitExceptingUnitEdges) {
		super(body, throwAnalysis, omitExceptingUnitEdges);
	}

	
	public ConciseExceptionalUnitGraph(Body body, boolean ignoredBogusParameter) {
		super(body, ignoredBogusParameter);
		// TODO Auto-generated constructor stub
	}


	public ConciseExceptionalUnitGraph(Body body, ThrowAnalysis throwAnalysis) {
		super(body, throwAnalysis);
		// TODO Auto-generated constructor stub
	}

	public ConciseExceptionalUnitGraph(Body body) {
		super(body);
		// TODO Auto-generated constructor stub
	}



	@Override
	protected Set<Unit> buildExceptionalEdges(ThrowAnalysis throwAnalysis,
			Map<Unit, Collection<ExceptionDest>> unitToExceptionDests,
			Map<Unit, List<Unit>> unitToSuccs,
			Map<Unit, List<Unit>> unitToPreds, boolean omitExceptingUnitEdges) {
		// TODO Auto-generated method stub
		Set<Unit> trapsThatAreHeads = new ArraySet<Unit>();
		Unit entryPoint = (Unit) unitChain.getFirst();

		for (Iterator<Entry<Unit, Collection<ExceptionDest>>> it = unitToExceptionDests.entrySet().iterator();
		     it.hasNext(); ) {
			Entry<Unit, Collection<ExceptionDest>> entry = it.next();
		    Unit thrower = (Unit) entry.getKey();
		    List<Unit> throwersPreds = getUnexceptionalPredsOf(thrower);
		    Collection<ExceptionDest> dests = entry.getValue();

		   
		    boolean alwaysAddSelfEdges = ((! omitExceptingUnitEdges) || 
						  mightHaveSideEffects(thrower));
		    ThrowableSet predThrowables = null;
		    ThrowableSet selfThrowables = null;
		    if (thrower instanceof ThrowInst) {
			ThrowInst throwInst = (ThrowInst) thrower;
			predThrowables = throwAnalysis.mightThrowImplicitly(throwInst);
			selfThrowables = throwAnalysis.mightThrowExplicitly(throwInst);
		    } else if (thrower instanceof ThrowStmt) {
			ThrowStmt throwStmt = (ThrowStmt) thrower;
			predThrowables = throwAnalysis.mightThrowImplicitly(throwStmt);
			selfThrowables = throwAnalysis.mightThrowExplicitly(throwStmt);
		    }

		    for (Iterator<ExceptionDest> destIt = dests.iterator(); destIt.hasNext(); ) {
			ExceptionDest dest = destIt.next();
			if (dest.getTrap() != null) {
			    Unit catcher = dest.getTrap().getHandlerUnit();
			    RefType trapsType = dest.getTrap().getException().getType();
			    if (predThrowables == null ||
				predThrowables.catchableAs(trapsType)) {
				// Add edges from the thrower's predecessors to the catcher.
				if (thrower == entryPoint) {
				    trapsThatAreHeads.add(catcher);
				}
//				for (Iterator<Unit> p = throwersPreds.iterator(); p.hasNext(); ) {
//				    Unit pred = p.next();
//				    addEdge(unitToSuccs, unitToPreds, pred, catcher);
//				}
			    }
			    if (alwaysAddSelfEdges ||
				(selfThrowables != null &&
				 selfThrowables.catchableAs(trapsType))) {
				addEdge(unitToSuccs, unitToPreds, thrower, catcher);
			    }
			}
		    }
		}
				
		// Now we have to worry about transitive exceptional
		// edges, when a handler might itself throw an exception
		// that is caught within the method.  For that we need a
		// worklist containing CFG edges that lead to such a handler.
		class CFGEdge {
		    Unit head;		// If null, represents an edge to the handler
					// from the fictitious "predecessor" of the 
					// very first unit in the chain. I.e., tail
					// is a handler which might be reached as a
					// result of an exception thrown by the
					// first Unit in the Body.
		    Unit tail;

		    CFGEdge(Unit head, Unit tail) {
			if (tail == null)
			    throw new RuntimeException("invalid CFGEdge(" 
						       + head.toString() + ',' 
						       + "null" + ')');
			this.head = head;
			this.tail = tail;
		    }

		    public boolean equals(Object rhs) {
			if (rhs == this) {
			    return true;
			}
			if (! (rhs instanceof CFGEdge)) {
			    return false;
			}
			CFGEdge rhsEdge = (CFGEdge) rhs;
			return ((this.head == rhsEdge.head) && 
				(this.tail == rhsEdge.tail));
		    }

		    public int hashCode() {
			// Following Joshua Bloch's recipe in "Effective Java", Item 8:
			int result = 17;
			result = 37 * result + this.head.hashCode();
			result = 37 * result + this.tail.hashCode();
			return result;
		    }
		}

		LinkedList<CFGEdge> workList = new LinkedList<CFGEdge>();

		for (Iterator<Trap> trapIt = body.getTraps().iterator(); trapIt.hasNext(); ) {
		    Trap trap = trapIt.next();
		    Unit handlerStart = trap.getHandlerUnit();
		    if (mightThrowToIntraproceduralCatcher(handlerStart)) {
			List<Unit> handlerPreds = getUnexceptionalPredsOf(handlerStart);
			for (Iterator<Unit> it = handlerPreds.iterator(); it.hasNext(); ) {
			    Unit pred = it.next();
			    workList.addLast(new CFGEdge(pred, handlerStart));
			}
			handlerPreds = getExceptionalPredsOf(handlerStart);
			for (Iterator<Unit> it = handlerPreds.iterator(); it.hasNext(); ) {
			    Unit pred = it.next();
			    workList.addLast(new CFGEdge(pred, handlerStart));
			}
			if (trapsThatAreHeads.contains(handlerStart)) {
			    workList.addLast(new CFGEdge(null, handlerStart));
			}
		    }
		}

		// Now for every CFG edge that leads to a handler that may
		// itself throw an exception catchable within the method, add
		// edges from the head of that edge to the unit that catches
		// the handler's exception.
		while (workList.size() > 0) {
		    CFGEdge edgeToThrower = workList.removeFirst();
		    Unit pred = edgeToThrower.head;
		    Unit thrower = edgeToThrower.tail;
		    Collection<ExceptionDest> throwerDests = getExceptionDests(thrower);
		    for (Iterator<ExceptionDest> i = throwerDests.iterator(); i.hasNext(); ) {
			ExceptionDest dest = i.next();
			if (dest.getTrap() != null) {
			    Unit handlerStart = dest.getTrap().getHandlerUnit();
			    boolean edgeAdded = false;
			    if (pred == null) {
				if (! trapsThatAreHeads.contains(handlerStart)) {
				    trapsThatAreHeads.add(handlerStart);
				    edgeAdded = true;
				}
			    } else {
				if (! getExceptionalSuccsOf(pred).contains(handlerStart)) {
				    addEdge(unitToSuccs, unitToPreds, pred, handlerStart);
				    edgeAdded = true;
				}
			    }
			    if (edgeAdded && 
				mightThrowToIntraproceduralCatcher(handlerStart)) {
				workList.addLast(new CFGEdge(pred, handlerStart));
			    }
			}
		    }
		}
		return trapsThatAreHeads;
	}
	
	/**
     * Utility method for checking if a Unit might throw an exception which
     * may be caught by a {@link Trap} within this method.  
     *
     * @param u     The unit for whose exceptions are to be checked
     *
     * @return whether or not <code>u</code> may throw an exception which may be
     *              caught by a <code>Trap</code> in this method,
     */
    private boolean mightThrowToIntraproceduralCatcher(Unit u) {
	Collection<ExceptionDest> dests = getExceptionDests(u);
	for (Iterator<ExceptionDest> i = dests.iterator(); i.hasNext(); ) {
	    ExceptionDest dest = i.next();
	    if (dest.getTrap() != null) {
		return true;
	    }
	}
	return false;
    }
    
    static boolean mightHaveSideEffects(Unit u) {
    	if (u instanceof Inst) {
    	    Inst i = (Inst) u;
    	    return (i.containsInvokeExpr() || 
    		    (i instanceof StaticPutInst) || 
    		    (i instanceof StaticGetInst) || 
    		    (i instanceof NewInst));
    	} else if (u instanceof Stmt) {
    	    for (Iterator<ValueBox> it = u.getUseBoxes().iterator(); it.hasNext(); ) {
    		Value v = it.next().getValue();
    		if ((v instanceof StaticFieldRef) || 
    		    (v instanceof InvokeExpr) ||
    		    (v instanceof NewExpr)) {
    		    return true;
    		}
    	    }
    	}
    	return false;
        }
	

}
