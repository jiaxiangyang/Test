package org.sklse.analysis.sideEffect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sklse.analysis.ProvenaceBox;
import org.sklse.analysis.ProvenanceAnalysis;

import soot.Local;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
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
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

public class SideEffectAnalysis {
	UnitGraph graph;

	public void init(UnitGraph graph) {
		this.graph = graph;
	}

	public boolean flowThrough(ProvenaceBox in, Stmt stmt, ProvenaceBox out) {
		FlowFactChange modification = new FlowFactChange();
		if (stmt.containsInvokeExpr()) {
			flowThroughInvoke(in, stmt, out, modification);
		}
		if (stmt instanceof AssignStmt) {
			Value leftOp = ((AssignStmt) stmt).getLeftOp();
			Value rightOp = ((AssignStmt) stmt).getRightOp();

			if (leftOp instanceof Local) {
				assignToLocal(in, (AssignStmt) stmt, modification, leftOp,
						rightOp);
			} else {
				assignToNoLocal(in, (AssignStmt) stmt, modification, leftOp);
			}
		} else if (stmt instanceof IdentityStmt) {
			flowThroughIdentity(in, (IdentityStmt) stmt, modification);
		}

		out.sideEffectflowFact.guardedLocals.union(modification.gen);
		out.sideEffectflowFact.guardedLocals.difference(modification.kill);
		out.sideEffectflowFact.signature.putAll(modification.signature);

		for (Iterator itr = modification.kill.iterator(); itr.hasNext();) {
			Local local = (Local) itr.next();
			out.sideEffectflowFact.signature.remove(local);
		}
		out.sideEffectflowFact.possiableGuradedLocal
				.union(modification.possiableGuradedLocal);
		if (modification.sideEffect != null) {
			out.sideEffectflowFact.sideEffects.put(stmt,
					modification.sideEffect);
		}
		if (modification.possiableSideEffect != null) {
			out.sideEffectflowFact.possiableSideEffects.put(stmt,
					modification.possiableSideEffect);
		}

		// if (modification.mutatedClass != null) {
		// SideEffect se = new SideEffect(modification.mutatedClass,
		// modification.mutatedField);
		// out.sideEffects.put(thisStep, se);
		// } else if (modification.mutatedLocal != null) {
		// SideEffect se = new SideEffect(modification.mutatedLocal,
		// modification.mutatedField);
		// out.sideEffects.put(thisStep, se);
		// }
		//
		System.out.println("flowThrough:" + stmt + " with sideEffects "
				+ out.sideEffectflowFact.sideEffects);

		return true;
	}

	private void flowThroughIdentity(ProvenaceBox in, IdentityStmt stmt,
			FlowFactChange modification) {
		Local left = (Local) stmt.getLeftOp();
		Value rightOp = stmt.getRightOp();
		if (rightOp instanceof ThisRef) {
			modification.gen.add(left);
			modification.signature.put(left, SideEffectFlowFact.DATAHEAD_THIS);
		} else if (rightOp instanceof ParameterRef) {
			ParameterRef p = (ParameterRef) rightOp;
			// ignore primitive types

			if (p.getType() instanceof RefLikeType) {
				modification.gen.add(left);
				modification.signature.put(left,
						SideEffectFlowFact.DATAHEAD_PARAM + p.getIndex());
			}
		} else if (rightOp instanceof CaughtExceptionRef) {
			// local = exception
			// out.localIsUnknown(left);
		}
	}

	private void assignToLocal(ProvenaceBox in, AssignStmt stmt,
			FlowFactChange modification, Value leftOp, Value rightOp)
			throws Error {
		Local left = (Local) leftOp;
		// remove optional cast
		if (rightOp instanceof CastExpr) {
			rightOp = ((CastExpr) rightOp).getOp();
		}
		// ignore primitive types
		if (!(left.getType() instanceof RefLikeType)) {
			// ??
		}
		// v = v
		else if (rightOp instanceof Local) {
			Local right = (Local) rightOp;
			if (in.sideEffectflowFact.guardedLocals.contains(right)) {
				modification.gen.add(left);
				modification.signature.put(left,
						in.sideEffectflowFact.signature.get(right));
			} else if (in.sideEffectflowFact.guardedLocals.contains(left)) {// left被赋值为不保护的对象
				modification.kill.add(left);
			}
		}
		// v = v[i]
		else if (rightOp instanceof ArrayRef) {
			Local right = (Local) ((ArrayRef) rightOp).getBase();
			if (in.sideEffectflowFact.guardedLocals.contains(right)) {
				modification.gen.add(left);
				Set<String> leftsigs = new HashSet<String>();
				Set<String> rightSigs = in.sideEffectflowFact.signature
						.get(right);
				for (String sig : rightSigs) {
					leftsigs.add(sig + "_" + "[]");
				}
				modification.signature.put(left, rightSigs);
			}
		}
		// v = v.f
		else if (rightOp instanceof InstanceFieldRef) {
			Local right = (Local) ((InstanceFieldRef) rightOp).getBase();
			String field = ((InstanceFieldRef) rightOp).getField().getName();
			if (in.sideEffectflowFact.guardedLocals.contains(right)) {
				modification.gen.add(left);
				Set<String> leftsigs = new HashSet<String>();
				Set<String> rightSigs = in.sideEffectflowFact.signature
						.get(right);
				for (String sig : rightSigs) {
					leftsigs.add(sig + "_" + "field");
				}
				modification.signature.put(left, rightSigs);
			}
		}
		// v = C.f
		else if (rightOp instanceof StaticFieldRef) {
			SootField field = ((StaticFieldRef) rightOp).getField();
			modification.gen.add(left);
			modification.signature.put(left, SideEffectFlowFact.DATAHEAD_GLOBAL
					+ "_" + field.getDeclaringClass() + "_" + field.getName());
		}
		// v = cst
		else if (rightOp instanceof Constant) {
			if (in.sideEffectflowFact.guardedLocals.contains(left)) {
				modification.kill.add(left);
			}
		}
		// v = new / newarray / newmultiarray
		else if (rightOp instanceof AnyNewExpr) {
			if (in.sideEffectflowFact.guardedLocals.contains(left)) {
				modification.kill.add(left);
			}
			// v=base.method(args)
		} else if (rightOp instanceof InvokeExpr) {
			// ignore primitive types
			if (left.getType() instanceof RefLikeType) {
				if (!in.sideEffectflowFact.guardedLocals.contains(left)) {
					modification.possiableGuradedLocal.add(left);
					modification.signature.put(left,
							SideEffectFlowFact.DATAHEAD_INSIDE + "_" + left);
				}
			}
		}
		// v = binary or unary operator
		else if (rightOp instanceof BinopExpr || rightOp instanceof UnopExpr
				|| rightOp instanceof InstanceOfExpr) {
			// do nothing...
		} else
			throw new Error("AssignStmt match failure (rightOp)" + stmt);
	}

	private void assignToNoLocal(ProvenaceBox in, AssignStmt stmt,
			FlowFactChange modification, Value leftOp) throws Error {
		// v[i] = ...
		if (leftOp instanceof ArrayRef) {
			Local left = (Local) ((ArrayRef) leftOp).getBase();
			recordSideEffect(in, modification, left, "[]");
		}
		// v.f = ...
		else if (leftOp instanceof InstanceFieldRef) {
			Local left = (Local) ((InstanceFieldRef) leftOp).getBase();
			SootField field = ((InstanceFieldRef) leftOp).getField();
			recordSideEffect(in, modification, left, field.getName());
		} // C.f = ...
		else if (leftOp instanceof StaticFieldRef) {
			SootField field = ((StaticFieldRef) leftOp).getField();
			boolean mutated = true;
			if (graph.getBody().getMethod().isEntryMethod()) {
				mutated = !field.getDeclaringClass().equals(
						graph.getBody().getMethod().getDeclaringClass());
			}
			if (mutated) {
				modification.sideEffect = new SideEffect(field
						.getDeclaringClass(), field.getName());
			}

		} else
			throw new Error("AssignStmt match failure (leftOp) " + stmt);
	}

	private void recordSideEffect(ProvenaceBox in, FlowFactChange modification,
			Local local, String field) {
		if (in.sideEffectflowFact.guardedLocals.contains(local)) {
			modification.sideEffect = new SideEffect(local, field,
					in.sideEffectflowFact.signature.get(local));
		} else if (in.sideEffectflowFact.guardedLocals.contains(local)) {
			modification.possiableSideEffect = new SideEffect(local, field,
					in.sideEffectflowFact.signature.get(local));
		}
	}

	// private void mutatedLocal(ProvenaceBox in, Modification modification,
	// Local local, String label) {
	// if (in.guardedLocals.contains(local)
	// || in.possiableGuradedLocal.contains(local)) {
	// modification.mutatedLocal = local;
	// modification.mutatedField = label;
	// }
	// }

	private boolean subClassOf(SootClass clazz, String className) {
		if (clazz == null || clazz.getName().equals("java.lang.Object"))
			return false;
		if (clazz.getName().equals(className))
			return true;
		if (subClassOf(clazz.getSuperclass(), className))
			return true;
		for (SootClass in : clazz.getInterfaces()) {
			if (subClassOf(in, className))
				return true;
		}
		return false;
	}

	private void flowThroughInvoke(ProvenaceBox in, Stmt stmt,
			ProvenaceBox out, FlowFactChange modification) {
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		SootMethod method = invokeExpr.getMethod();
		SootClass baseClass = method.getDeclaringClass();

		boolean isStatic = invokeExpr.getMethod().isStatic();

		// 持久化操作

		List<Value> args = invokeExpr.getArgs();
		Local base = null;
		if (!isStatic) {
			InstanceInvokeExpr expr = (InstanceInvokeExpr) invokeExpr;
			base = (Local) expr.getBase();
			if (!in.sideEffectflowFact.guardedLocals.contains(base)) {
				modification.possiableGuradedLocal.add(base);
			}
			if ((invokeExpr instanceof VirtualInvokeExpr)) {
				boolean isCollection = subClassOf(baseClass,
						"java.util.Collection");
				boolean isMap = subClassOf(baseClass, "java.util.Map");
				// Collection.add remove Map.put remove 当做数组操作
				if (subClassOf(baseClass, "java.util.Collection")) {
					if (method.getName().equals("add")
							|| method.getName().equals("remove")
							|| method.getName().equals("addAll")
							|| method.getName().equals("removeAll")
							|| method.getName().equals("clear")) {
						recordSideEffect(in, modification, base, "[]");
					}

				} else if (subClassOf(baseClass, "java.util.Map")) {
					if (method.getName().equals("put")
							|| method.getName().equals("remove")
							|| method.getName().equals("putAll")
							|| method.getName().equals("clear")) {
						recordSideEffect(in, modification, base, "[]");
					}
				}
				return;
			}
		} else {
			if (base != null
					&& !in.sideEffectflowFact.guardedLocals.contains(base)
					&& !in.sideEffectflowFact.possiableGuradedLocal
							.contains(base)) {
				modification.possiableGuradedLocal.add(base);
				modification.signature.put(base,
						SideEffectFlowFact.DATAHEAD_INSIDE + "_" + base);
			}

			for (Value arg : args) {
				if (!in.sideEffectflowFact.guardedLocals.contains(arg)
						&& !in.sideEffectflowFact.possiableGuradedLocal
								.contains(arg)) {
					modification.possiableGuradedLocal.add(arg);
					modification.signature.put(arg,
							SideEffectFlowFact.DATAHEAD_INSIDE + "_" + arg);
				}
			}
		}

	}

	private void flowThroughThrow(ProvenaceBox in, ThrowStmt stmt, FlowSet out) {
		// TODO Auto-generated method stub

	}

	private void flowThroughReturn(ProvenaceBox in, ReturnStmt stmt, FlowSet out) {
		// TODO Auto-generated method stub
	}

}
