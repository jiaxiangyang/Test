package org.sklse.analysis;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 2008 Eric Bodden
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
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Singletons;
import soot.Transform;
import soot.Trap;
import soot.jimple.toolkits.annotation.purity.MyPurityIntraproceduralAnalysis;
import soot.jimple.toolkits.annotation.purity.PurityAnalysis;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;

public class ExcepionSaftyAnalysis {

	public static void main(String[] args) {
		PackManager.v().getPack("jtp").add(
				new Transform("jtp.myTransform", new BodyTransformer() {
					protected void internalTransform(Body body, String phase, Map options) {
						System.out.println(body.getMethod());
						new ProvenanceAnalysis(new ConciseExceptionalUnitGraph(body,new ConciseUnitThrowAnalysis()));;
						//new  MyPurityIntraproceduralAnalysis(new ConciseExceptionalUnitGraph(body,new ConciseUnitThrowAnalysis()));;
//						for(Iterator<Trap> itr = body.getTraps().iterator();itr.hasNext();){
////							Trap trap=itr.next();
////							System.out.println(trap.getException());
////							System.out.println(trap.getUnitBoxes());
////							System.out.println(trap.getBeginUnit().getTag("LineNumberTag"));
////							System.out.println(trap.getEndUnit().getTag("LineNumberTag"));
////							System.out.println(trap.getHandlerUnit().getTag("LineNumberTag"));
//						}
						
						//System.out.println(body.getUseBoxes());
				      //new IntraProceduralAnalysis(new ConciseExceptionalUnitGraph(body,new ConciseUnitThrowAnalysis()));
				      }
				}));
		
		
		//ExceptionChecker
		
//		String setting="";
//		//"-src-prec","java",
//		soot.options.Options.v().set_soot_classpath(setting);
		//-p jb use-original-names:true
		String[] arg={"org.sklse.HelloWorld","-keep-line-number","-p", "jb", "use-original-names:true","-f","J"};
		//String[] arg={"-w","org.sklse.HelloWorld","-p","cg.spark","verbose:true,on-fly-cg:true"};
		soot.Main.main(arg);
	}
	
}