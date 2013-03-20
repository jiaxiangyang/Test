package org.sklse;

import java.io.InputStream;

public class MyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String a = "ss";
		String b = "cc";
		try {
			if (a.compareTo(b) > 0) {
				System.out.println(a);
			}
		} catch (RuntimeException e) {
			try{
				System.out.println(e);
			}catch (Exception e2){
				System.out.println(e2);
			}finally{
				System.out.println(e);
			}
		} finally {
			System.out.println(b);
		}
	
	
	}

}
