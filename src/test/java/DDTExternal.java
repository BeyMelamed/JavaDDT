import java.util.HashMap;
import java.util.Map;

/**
 * Created by BeyMelamed on 6/14/2015.
 * Selenium Based Automation Project
 *
 * Selenium Based Automation Project
 *
 * =============================================================================
 * Copyright 2014 Avraham (Bey) Melamed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Description
 * This class is a 'sand box' for implementation of methods not provided by the original project.
 * Methods that are called as a step (or part thereof) by the DDTTestRunner via the part of the are assumed to either return a boolean indicating pass or fail or throw in instance of throwable.
 * If the method is to accept a
 * <p/>
 * =============================================================================
 *
 * When      |Who            |What
 * ==========|===============|========================================================
 * 16/14/2015 |Bey            |Initial Version
 * ==========|===============|========================================================

 */

public class DDTExternal {
   public DDTExternal() {}
   public DDTExternal(DDTTestContext context) {}

   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding a comment, without an error means something was done successfull!
    * This is a static method but an instance method will do as well
    * @param context
    */
   public static void trivialPass(DDTTestContext context) {
      context.addComment("Hey, I passed!");
      return;
   }

    // Given two integers, a and b return the highest integer that is less then or equal to a that have a common divisor (with no remainder) with respect to both, a and b.
    // If None, return 1
    // NOTE: This logic DOES NOT handle the case of a = 0! 
	public static int functionOne(int a, int b) {
		System.out.println("\nUsing a = " + a + ", b = " + b);
		for (int x = a; x >=0; x--) {
			System.out.println("a = " + a + ", b = " + b + ", x = " + x + " - " + a + " modulo " + x + " = " + a % x + ", " + b + " modulo " + x + " = " + b % x);
			if (a % x == 0 && b % x == 0)
				return x; 			
		}
		return 1;
	}

   // DDTTestContext is a specialized class enabling passing of variables throughout the test session.
   // The variables of interest for this test are the a string and b string each of which are comma delimited list of integers
   // For simplicity sake, no verification of this assumption is included
   // Each pair of corresponding integers in the a, b numbers constitute one call to the FunctionOne function. 
   public static void interviewTestFunctionOne(DDTTestContext context) {
 	   // populate the a, b strings from the context
	   String a = context.getString("aNums");
	   if (a.isEmpty())
		   a = "21,-18,-15,12,9,6,3";
 	   String b = context.getString("bNums");
	   if (b.isEmpty())
		   b = "6,-3,3,6";
	   String e = context.getString("expect");
	   if (e.isEmpty())
		   e = "3,1,1,6";

	   // Create two string arrays to hold those integers
 	   String aStrings[] = a.split(",");
 	   String bStrings[] = b.split(",");
 	   String eStrings[] = e.split(",");
 	   
 	   // Determine the shorter array rank to prevent out of bounds exception
 	   int lowestRank = aStrings.length > bStrings.length ? bStrings.length : aStrings.length;
 	   if (lowestRank > eStrings.length)
 		   lowestRank = eStrings.length;
 	  
 	   // Display information in the console.
 	   System.out.println("\nTesting of FunctionOne\n");
 	   System.out.println("a Numbers " + a);
 	   System.out.println("b Numbers " + b);
 	   System.out.println("Expected Results " + e);
 	   
 	   // Comment the test case (will be reported if the test passed!)
 	   context.addComment("Testing of FunctionOne");
 	   context.addComment("a Numbers " + a);
 	   context.addComment("b Numbers " + b);
 	   context.addComment("Expected Results " + e);
 	   
 	   // Main logic.
 	   // Parse the numeric values and call the functionOne logic...
 	   int result;
 	   for (int i = 0; i < lowestRank; i++) {
 		   
 		   // Prepare integer values from the input string(s)
 		   int thisA = Integer.valueOf(aStrings[i]);
 		   int thisB = Integer.valueOf(bStrings[i]);
 		   int thisE = Integer.valueOf(eStrings[i]);
 		  try {
 			  // Call the actual function logic being tested
 			  result = functionOne(thisA, thisB);
 			  
 			  // parse the results to determine success or failure of each instance.
 			  // any instance' failure FAILs the entire test case using the .addError(..) method.
 			  if (thisE == result)
 		 		  context.addComment("a = " + thisA + ", b = " + thisB + " result = " + result);
 			  else 
 		 		  context.addError("a = " + thisA + ", b = " + thisB + " result = " + result + ", expected: " + thisE);
 				  
 		  } catch (Throwable t) {
 			  // Trap all exceptions and fail the instance' result unconditionally
 			  result = -1;
		 	  context.addError("a = " + thisA + ", b = " + thisB + " result = " + result + ", expected: " + thisE + " Exception( " + t.getMessage()+ " )");
 		  }
 		  System.out.println("a = " + thisA + ", b = " + thisB + " result = " + result + ", expected: " + thisE);
 	   }
 	   
 	   System.out.println("\n*** End Testing of FunctionOne*** \n");

   }

   // functionTwo constructs a one - item Map<int[], int[]) by iterating over the size of input parameter a 
   // ... and creating an <int[], int[]> composed of the a and b int[] arrays.
   // As the key (a) remains the same, the Map remains of size 1
   public static Map<int[], int[]> functionTwo(int[] a, int[] b) {
	    Map<int[], int[]> m = new HashMap<>();
	    for(int i = 0; i < a.length; i++) {
	        m.put(a,b);
	    }
	    return m;
   }

   // DDTTestContext is a specialized class enabling passing of variables throughout the test session.
   // The variables of interest for this test are the a string and b string each of which are comma delimited list of integers
   // For simplicity sake, no verification of this assumption is included
   public static void interviewTestFunctionTwo(DDTTestContext context) {

 	   System.out.println("\nTesting of FunctionTwo\n");

	   // populate the a, b strings from the context
	   String a = context.getString("aNums");
 	   String b = context.getString("bNums");
 	   
 	   String[] empty = new String[0];
	   // Create two string arrays to hold those integers
 	   String aStrings[] = a.isEmpty() ? empty : a.split(",");
 	   String bStrings[] = b.isEmpty() ? empty : b.split(",");
 	   
 	   // Display information in the console.
 	   System.out.println("a Numbers " + a);
 	   System.out.println("b Numbers " + b + "\n");
 	   
 	   // Comment the test case (will be reported if the test passed!)
 	   context.addComment("Testing of FunctionOne");
 	   context.addComment("a Numbers " + a);
 	   context.addComment("b Numbers " + b);
 	   
 	   // Construct two int[] arrays and populate them with integer values.
 	   
 	   int[] aNums = new int[aStrings.length];
 	   int[] bNums = new int[bStrings.length];
 	   
 	   for (int i = 0; i < aNums.length; i++) {
 		   aNums[i] = Integer.valueOf(aStrings[i]);
 	   }
 	   
 	   for (int i = 0; i < bNums.length; i++) {
 		   bNums[i] = Integer.valueOf(bStrings[i]); 		   
 	   }
 	   
 	   // Main logic.
	   try {
 			  Map <int[], int[]> result = functionTwo(aNums, bNums);
 				  
 			  int expectedSize = (aNums.length == 0) ? 0 : 1;
 			  
 			  String template = "First Size Expectation met: the result size (" + result.size() +") and the result expected size are equal (" + expectedSize + ")";
 			  if (result.size() == expectedSize) {
 				  context.addComment(template);
 				  System.out.println(template);
 			  }
 			  else {
 				  template.replace("met:",  "NOT met:");
 				  template.replace("equal",  "NOT equal"); 				  
 				  context.addError(template);
 				  System.out.println(template);
 			  }
			  if (result.size() == 0) {
 				 System.out.println("With empty list of 'a' numbers - no further testing will take place.");
 			  }
 					  

 			 int entryNo = 0;
 			 // Iterate over the keyset of the result and validate the following:
 			 // 1. The size of the key of the entry is the same as the size of the a array
 			 // 2. The size of the value of the entry is the same as the size of the b array
 			 // 3. If 1. above is true then, all the entries in the a array are identical to the entries in the key of the entry
 			 // 4. If 2. above is true then, all the entries in the b array are identical to the entries in the value of the entry
 			 
 			 for (Map.Entry<int[], int[]> entry : result.entrySet())
 			 {
 			    entryNo++;
 			    
 			    // Obtain 
 			    int[] aKeys = entry.getKey();
 	 			// 1. The size of the key of the entry is the same as the size of the a array
 			    template = "Second Size Expectation met: the size of the key at entry no. " + entryNo + " (" + aKeys.length +") and the size of the 'a' Array are equal (" + aNums.length + ")"; 
 			    if (aKeys.length == aNums.length) {
 	 				context.addComment(template);
 			    	System.out.println(template);
 			    }
 	 			else {
 	 				  template.replace("met:",  "NOT met:");
 	 				  template.replace("equal",  "NOT equal"); 				  
 	 				  context.addError(template);
 	 				  System.out.println(template);
 	 			}
 			    	
 			    int[] bValues = entry.getValue(); 			    

 	 			// 2. The size of the value of the entry is the same as the size of the b array
 			    template = "Third Size Expectation met: the size of the value at entry no. " + entryNo + " (" + bValues.length +") and the size of the 'b' Array are equal (" + bNums.length + ")";
 			    if (bValues.length == bNums.length) {
 	 				context.addComment(template);
 	 				System.out.println(template);
 			    }
 	 			else {
	 				  template.replace("met:",  "NOT met:");
	 				  template.replace("equal",  "NOT equal"); 				  
	 				  context.addError(template);
	 				  System.out.println(template);
 	 			}

 			    // 3. If 1. above is true then, all the entries in the a array are identical to the entries in the key of the entry
 			    if (aKeys.length == aNums.length) {
 				   for (int i = 0; i < aNums.length; i++) {
 					   template = "Value Expectation #1 met: item no " + i + " of the keys array at entry no. " + entryNo + " (" + aKeys[i] + ") is equal to the corresponding item in the 'a' array (" + aNums[i] + ")";
 					   if (aNums[i] == aKeys[i]) {
 		 	 				context.addComment(template);
 		 	 				System.out.println(template);
 					   }
 		 	 			else {
 			 				  template.replace("met:",  "NOT met:");
 			 				  template.replace("equal",  "NOT equal"); 				  
 			 				  context.addError(template);
 			 				  System.out.println(template);
 		 	 			}
 				   }
 			    }

 	 		    // 4. If 2. above is true then, all the entries in the b array are identical to the entries in the value of the entry
 			    if (bValues.length == bNums.length) {
 				   for (int i = 0; i < bNums.length; i++) {
 					   template = "Value Expectation #2 met: item no " + i + " of the values array at entry no. " + entryNo + " (" + bValues[i] + ") is equal to the corresponding item in the 'b' array (" + bNums[i] + ")";
 					   if (bNums[i] == bValues[i]) { 
 		 	 				context.addComment(template);
 		 	 				System.out.println(template);
 					   }
 		 	 			else {
			 				  template.replace("met:",  "NOT met:");
			 				  template.replace("equal",  "NOT equal"); 				  
			 				  context.addError(template);
			 				  System.out.println(template);
 		 	 			}
 				   }
 			    }
 			   
 			 } 			  
 		  } catch (Throwable t) {
 			  // Trap all exceptions and fail the instance' result unconditionally
 			  System.out.println("FunctionTwo Exception: " + t.getMessage());
 	   }
 	   
 	   System.out.println("\n*** End Testing of FunctionTwo*** \n");

   }
      
   
   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding an error (possible, an exception) means something failed!
    * This is a static method but an instance method will do as well
    * @param context
    */
   public static void trivialFail(DDTTestContext context) {
      context.addError("Ooops, I failed!");
   }

   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding an error (possible, an exception) means something failed!
    * This is an instance method but a static method will do as well
    * @param context
    */
   public void instanceFailure(DDTTestContext context) {
      context.addError("Ooops, Instance failure failed!");
   }
}
