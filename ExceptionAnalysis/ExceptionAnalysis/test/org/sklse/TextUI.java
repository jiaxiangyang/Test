package org.sklse;

import java.io.*;

class TextUI implements UI {
	private TextUI instance=null;
	TextUI next=null;
	
	public TextUI() {
		super();
	}
	
	public TextUI(TextUI next) {
		super();
		this.next = next;
	}


	public void display(String msg) {
		System.out.println(msg);
	}
}