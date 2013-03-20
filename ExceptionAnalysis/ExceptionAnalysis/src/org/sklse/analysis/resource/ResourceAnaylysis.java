package org.sklse.analysis.resource;

import java.util.HashSet;
import java.util.Set;

import org.sklse.analysis.ProvenanceAnalysis;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.toolkits.graph.UnitGraph;

public class ResourceAnaylysis {
	
	public static Set<String> RESOUCE_CLASSES = new HashSet<String>();
	{
		RESOUCE_CLASSES.add("java.io.Closeable");
		RESOUCE_CLASSES.add("java.sql.Connection");
		RESOUCE_CLASSES.add("java.sql.Statement");
		RESOUCE_CLASSES.add("java.sql.ResultSet");
	}

	public void init(UnitGraph graph) {
		for(Local l:graph.getBody().getLocals()){
			System.out.println(l.getType());
			if(l.getType() instanceof RefType){
				RefType type=(RefType)l.getType();
				if(isResource(type.getSootClass())){
					resourceLocal.add(l);	
				}
			}
		}
	}

	private boolean isResource(SootClass clazz){
		if (clazz == null || clazz.getName().equals("java.lang.Object"))
			return false;
		if (RESOUCE_CLASSES.contains(clazz.getName()))
			return true;
		if (isResource(clazz.getSuperclass()))
			return true;
		for (SootClass in : clazz.getInterfaces()) {
			if (isResource(in))
				return true;
		}
		return false;
	}

	public ResourceAnaylysis() {
		// TODO Auto-generated constructor stub
	}

	public Set<Local> resourceLocal=new HashSet<Local>();
	
	

}
