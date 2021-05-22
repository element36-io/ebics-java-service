package io.element36.cash36.ebics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TestTool {
	
	public static String pp(Object...objects) {
		String result="  ";
		if (objects==null) {
			result="..null";
		} else {
			for (Object o:objects) result+=o+"; ";
		}
		
		System.out.println(result);
		return result;
	}

	
	 public static String readLineByLineJava8(String filePath) 
	    {
		 String content = "";
		 
	        try
	        {
	            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	 
	        return content;
	    }
}
