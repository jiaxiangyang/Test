package org.sklse;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import soot.PointsToAnalysis;

public class HelloWorld {
	public String ss = "dd";
	public static String v = "33";
	public static ArrayList a = new ArrayList();;

	public static void main(String[] arg) {
		TextUI ui = new TextUI();
		TextUI b = new TextUI();
		TextUI c = new TextUI(b);
        ui.next=b;
		if (v != null) {
			v = v + "ss";
			b = ui;
		} else {
			int m = 0;
			v = v + m;
			ui.next=c;
		}
		a.add(b);
		ui.display("Hello World");
	}

	public void close(Connection cn) {
		try {
			int b = 1;
			if(b>0){
				throw new SQLException();
			}
			a.add(b);
			cn.close();
		} catch (SQLException e) {
			int p = 2;
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			int m = 3;
		}
	}

}
