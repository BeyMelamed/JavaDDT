
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 10/29/14
 * Time: 1:44 PM
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
 * =============================================================================
 *
 * Description
 * This class contains verbs in the DDT dialect.
 * Each action is implemented as a sub class of Verb and corresponds to a method in the Dialect class
 * A given method in the Dialect class instantiate the corresponding instance of a Verb sub-class
 *
 * For example:
 *
 * Action            Verb Class
 * ================= =================================
 * find              Find
 * click             Click
 * ================= =================================
 *
 * An instance has a Hashtable<String, Object> instance where the parameters / context of a test are stored to support the various flavors of a 'verb'
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 10/29/14    |Bey      |Initial Version
 * 06/06/15    |Bey      |Introduce WaitUntil and BranchOnValue, verb names are now case-insensitive
 * 10/16/16    |Bey      |Adjust ddtSettings getters.
 * 10/17/16    |Bey      |Enable table cell DoubleClick
 * 01/01/17    |Bey      |NewTest verb: Provide for default test id (items container name)
 * 01/14/17    |Bey      |DragAndDrop Implementation
 * ============|=========|====================================
 */

public abstract class Verb extends DDTBase {

   private static Hashtable<String, Verb> verbs;

   private DDTTestContext testContext = null;
   private String id;

   private static Hashtable<String, Verb> getVerbs() {
      if (verbs == null)
         initializeVerbs();
      return verbs;
   }

   public static void initialize() {
      initializeVerbs();
   }

   /**
    * @TODO - Use reflection to discover all Verb subclasses instead of hard code those
    * Initialize the verbs structure - need to be modified when a new verb is introduced.
    */
   private static void initializeVerbs() {
      //Class<?>[] subClasses = Verb.class.getDeclaredClasses();

      verbs = new Hashtable<String, Verb>();

      java.lang.Class<?>[] subClasses = Verb.class.getDeclaredClasses();
      int nClasses = subClasses.length;
      System.out.println(nClasses -1 + " Verbs found in test harness.");  // We exclude the VerbException instance
      Verb inst;
      String name;
      String key;
      for (int i = 0; i < nClasses; i++) {
         if (subClasses[i].toString().toLowerCase().contains("verbexception"))
            continue;

         try {
            java.lang.Class<?> c =  (Class<?>) subClasses[i];
            name = c.getClass().getName();
            inst = (Verb) c.newInstance();
            name = c.getName();
            key = Util.lastPart(name, ".").replace("Verb$", "");

            try {
               inst = (Verb) c.newInstance();
               verbs.put(key.toLowerCase(), inst);
               System.out.println("Added Verb '" + key + "' to the Verbs 'Dictionary'");
            }
            catch (Exception e) {
               System.out.println("*** - Failed instantiating '" + name + "' ***");
               continue;
            }
         }
         catch (IllegalAccessException e) {
            System.out.println("*** - Failed instantiating instance No. " + i+1 + " *** " + subClasses[i].toString() + " *** (IllegalAccessException)");
            continue;
         }
         catch (InstantiationException e) {
            System.out.println("*** - Failed instantiating instance No. " + i+1 + " *** " + subClasses[i].toString() + " *** (InstantiationException)");
            continue;
         }
      }

      System.out.println(verbs.size() + " Verbs loaded into the 'Dictionary'");
   }

   public static void invokeForTestItem(TestItem testItem) {
      // Ensure testItem's action is not blank
      if(testItem.getAction().isEmpty()) {
         testItem.addError("Invalid TestItem - 'Action' is required but is blank!");
         return;
      }

      // Ensure testItem's action is represented in the getVerbs() hashMap
      if (!getVerbs().containsKey(testItem.getAction().toLowerCase())) {
         testItem.addError("Invalid TestItem - 'Action' ('" + testItem.getAction() + "') is not implemented yet!");
         return;
      }

      // Ensure testItem's action is indeed, a valid Verb
      // It is OK to check for null as the hashtable's values are Verb instances.
      if ((getVerbs().get(testItem.getAction().toLowerCase()) == null)) {
         testItem.addError("Invalid TestItem - 'Action' ('" + testItem.getAction() + "') does not correspond to a valid 'Verb'");
         return;
      }

      // invoke this verb - basicDoIt catches errors too
      try {
         getVerbs().get(testItem.getAction().toLowerCase()).basicDoIt(testItem);
      }
      catch (Exception e) {
         if (!testItem.hasErrors())
            testItem.addError("Exception encountered in Action '" + testItem.getAction() + "'.");
         testItem.setException(e);
      }
   }

   /**
    * Indicates whether the verb keys by the action string is a UI verb.
    * @param action
    * @return
    */
   public static boolean isUIVerb(String action) {
      try {
         return getVerbs().get(action.toLowerCase()).isUIVerb();
      }
      catch (Exception e) {
         return false;
      }
   }

   /**
    * Indicate whether the verb is a UI verb or not.
    */

   public abstract boolean isUIVerb();

   public void clear() {
      super.clear();
      setContext(null); // Will be lazily initialized to an empty context
      setElement(null);
      getContext().removeErrors();
   }

   /**
    * Initialize the instance from a TestItem instance
    * @param testItem
    */
   public void initializeFromTestItem(TestItem testItem) {
      setContext(testItem.getDataProperties());
      getContext().setProperty("stepId", testItem.getId());
      getContext().setProperty("locType", testItem.getLocType());
      getContext().setProperty("locSpecs", testItem.getLocSpecs());
      getContext().setProperty("qryFunction", testItem.getQryFunction());
      getContext().setProperty("Description", testItem.getDescription());
      String active = testItem.getStatus(); // PASS, FAIL, SKIP - we are just initializing - no room for FAIL yet, so, really, SKIP (inactive) or PASS (active)
      getContext().setProperty("isActive", (active == "SKIP"));

      if (getContext().getString("Description").toLowerCase().contains(":debug:"))
         getContext().setProperty("debug", true);
      if (testItem.getElement() instanceof WebElement)
         getContext().setProperty("element", testItem.getElement());
      getContext().setProperty("testItem", testItem);

   }

   /**
    * Perform the action implied and pass the results to the corresponding testItem instance.
    * @param testItem
    * @throws VerbException
    */
   public void basicDoIt(TestItem testItem) throws VerbException{
      clear();
      getContext().removeErrors();
      initializeFromTestItem(testItem);

      try {
         doIt();
      }
      catch (VerbException e) {
         Verb.basicAddError(this, "Verb Exception Error encountered during 'doIt' method");
         setException(e);
      }
      catch (Exception e) {
         Verb.basicAddError(this, "Exception Error encountered during 'doIt' method");
         setException(e);
      }
      catch (Throwable e) {
          Verb.basicAddError(this, "Throwable Error encountered during 'doIt' method");
          setException(e);    	  
      }

      if (hasComments()) {
         testItem.addComment(getComments());
      }

      if (hasErrors())
         testItem.addError(getErrors());

      if (hasException())
         testItem.setException(getException());

      if (getElement() instanceof WebElement)
         testItem.setElement(getElement());
   }

   public void setContext(DDTTestContext value) {
      testContext = value;
   }

   public DDTTestContext getContext() {
      if (!(testContext instanceof DDTTestContext))
         setContext(new DDTTestContext());
      return testContext;
   }

   public void setId(String value) {
      id = value;
   }

   public String getId() {
      return id;
   }

   public static void debug(Verb verb) {
      boolean shouldDebug = verb.getContext().getBoolean("debug");
      if (shouldDebug) {
         String pleaseNote = "This is a debugging spot for all of us, Verbs - Just keep debugging...";
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(getStatus() + " - " + myName() + ": ");

      String id = getContext().getString("stepid");
      if (!isBlank(id))
         sb.append(", " + "ID: " + id);
      if (!this.getErrors().isEmpty()) {
         sb.append(", " + "Errors: " + this.getErrors());
      }
      if (!this.getComments().isEmpty()) {
         sb.append(", " + "Comments: " + this.getComments());
      }
      return sb.toString();
   }

   public boolean isActive() {
      if (getContext().containsKey("isActive"))
         return getContext().getBoolean("isActive");
      return false;
   }

   public String getStatus() {
      if (hasErrors() || hasException())
         return "FAIL";
      if (!isActive())
         return "SKIP";
      return "PASS";
   }

   public boolean isPass() {
      return !(hasErrors() || hasException());
   }

   public boolean isFail() {
      return (hasErrors() || hasException());
   }

   public boolean hasElement() {
      if (super.hasElement())
         return true;
      if (!getContext().containsKey("element"))
         return false;
      WebElement element = (WebElement) getContext().getElement();
      if (element instanceof WebElement)
         return true;
      return false;
   }

   /**
    * Convenience method to improve readabiity of reports and outputs - answer the name of the verb object's class
    *
    * @return
    */
   public String myName() {
      String myName = myFullName();
      return myName.replace("Verb ", "");
   }

   /**
    * Convenience method to improve readabiity of reports and outputs - answer the name of the verb object's class
    *
    * @return
    */
   public String myFullName() {
      return getClass().getCanonicalName().replace(".", " ");
   }

   /**
    * Standard validation of a verb - Must have valid test context
    *
    * @param verb
    */
   static void basicValidation(Verb verb, boolean requiresDriver) {

      if (!(verb.getContext() instanceof DDTTestContext)) {
         Verb.basicAddError(verb, "Action " + Util.sq(verb.myName()) + " requires non-empty test context data ");
      } else {
         if (verb.getContext().size() < 1)
            Verb.basicAddError(verb, "Action " + Util.sq(verb.myName()) + " requires non-empty test context data ");
      }

      if (requiresDriver) {
         WebDriver d = Driver.getDriver();
         if (!(d instanceof WebDriver)) {
            Verb.basicAddError(verb, "Web Driver is required but absent!");
            return;
         }
         if (!Driver.isInitialized()) {
            Verb.basicAddError(verb, "Action requires Web Driver - but it is not initialized.  Action Failed");
         }
      }
   }

   public static void basicAddError(Verb verb, String blurb) {
      verb.addError(verb.myName() + " Error: " + blurb);
   }

   public static void basicAddComment(Verb verb, String blurb) {
      verb.addComment(verb.myName() + ": " + blurb);
   }


   /**
    * The doIt method is where the actual action of the framework happens to happen.
    * Each action verb (a sub-class of Verb) implements its own logic.
    * This mechanism enables invoking the same code either via DDTTestRunner or one's own code as one would with any non-DDT approach
    */
   public abstract void doIt() throws VerbException;

   // ====================================================================================================
   // ======================================     VERBS     ===============================================
   // ======================== Various implementations of the abstract methods ===========================
   // ====================================================================================================

   /**
    * Description
    * BranchOnValue is used in scenarios where one needs to branch to various portions of a given test harness based on several different values in a given web element.
    * An example is a carouselle like web page that rotates values in a particular element
    * The logic requires presence of a WebDriver instance.
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 06/06/15    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class BranchOnElementValue extends Verb {

      public boolean isUIVerb() { return true;}
      private String[] branches;
      private String[] values;
      private String[] queries;
      private String[] data;
      private String[] comparisons;
      private String[] classes;
      private int iterations;

      private int nBranches = 1;

      /**
       * Overrides the super classes' basic validation.
       * Validate this instance's Context and construct the needed arrays for the branching logic.
       * The context map should have:
       * Values           - values to branch on
       * Tags         - InputSpecs to be invoked based on values
       * Queries          - Query functions to be used by WebQuery
       * Params           - Query parameter to be used by WebQuery
       * Compares         - comparison methods to be used by WebQuery
       * Classses         - object class to be used by Verifier
       * These are strings delimited by "|" (vertical bars.
       *
       * @param verb
       */
      private void basicValidation(BranchOnElementValue verb) {
         super.basicValidation(verb, isUIVerb());
         if (this.hasErrors())
            return;

         // Determine the number of times to try the logic
         iterations = getContext().getInt("iterations");
         if (iterations < 1)
            iterations = 1;

         // Discover the size of branches array - this size will be used by all arrays
         // Assume the maximum size is MaxBranches for now (@TODO - Parameterize it - @LATER)
         // Since branch values must exist for all instances, use this to drive the logic.

         String tmp = getContext().getString("Branches");
         if (isBlank(tmp)) {
            Verb.basicAddError(this, "Setup error:  Must have at least two branches for this verb to work!");
            return;
         }

         branches = split(tmp.toString(), "|");
         if (branches.length < 2) {
            Verb.basicAddError(this, "Setup error:  Must have at least two branches for this verb to work!");
            return;
         }

         tmp = getContext().getString("Queries");
         if (isBlank(tmp)) {
            Verb.basicAddError(this, "Setup error:  Must have at least two queries for this verb to work!");
            return;
         }

         queries = split(tmp.toString(), "|");
         if (queries.length < 2) {
            Verb.basicAddError(this, "Setup error:  Must have at least two query settings for this verb to work!");
            return;
         }

         if (queries.length != branches.length) {
            Verb.basicAddError(this, "Setup error:  Must have the same number of branches and queries for this verb to work!");
            return;
         }

         nBranches = branches.length;

         // Verify there are no blank values in either branches or queries arrays

         String errors = "";
         String prefix = "";
         for (int i = 0; i < nBranches; i++) {
            if (isBlank(branches[i])) {
               errors += prefix + "Branches no: " + i + " is blank!";
               prefix = ", ";
            }
            if (isBlank(queries[i])) {
               errors += prefix + "Query no: " + i + " is blank!";
               prefix = ", ";
            }
         }

         if (isNotBlank(errors)) {
            Verb.basicAddError(this, "Setup Errors encountered: " + errors);
            return;
         }

         String key = "";
         String value = "";

         // Reasonable validations carried, fill up the other arrays
         // Knowing the number of parameters' array size - create those arrays and fill them with the appropriate values (or blanks)

         values = new String[nBranches];
         data = new String[nBranches];
         classes = new String[nBranches];
         comparisons = new String[nBranches];

         for (int i = 0; i < nBranches; i++) {
            values[i] = "";
            data[i] = "";
            classes[i] = "";
            comparisons[i] = "";
         }

         String[] tmpValues = split(getContext().getString("Values"), "|");
         if (tmpValues == null || tmpValues.length == 0)
            tmpValues = new String[]{"","","","","","","","","",""};
         String[] tmpParams = split(getContext().getString("Params"), "|");
         if (tmpParams == null || tmpParams.length == 0)
            tmpParams = new String[]{"","","","","","","","","",""};
         String[] tmpClasses = split(getContext().getString("Classes"), "|");
         if (tmpClasses == null || tmpClasses.length == 0)
            tmpClasses = new String[]{"","","","","","","","","",""};
         String[] tmpCompares = split(getContext().getString("Comparisons"), "|");
         if (tmpCompares == null || tmpCompares.length == 0)
            tmpCompares = new String[]{"","","","","","","","","",""};

         // Fill all defined values in each of the emaining parameter arrays ensuring only relevant values within the appropriate size are used.
         // Branches and Queries have already been handled

         for (int i = 0; i < nBranches; i++) {
            if (i < tmpValues.length && isNotBlank(tmpValues[i]))
               values[i] = tmpValues[i].toUpperCase().equals("@BLANK@") ? "" : tmpValues[i];
            if (i < tmpParams.length && isNotBlank(tmpParams[i]))
               data[i] = tmpParams[i].toUpperCase().equals("@BLANK@") ? "" : tmpParams[i];
            if (i < tmpClasses.length && isNotBlank(tmpClasses[i]))
               classes[i] = tmpClasses[i].toUpperCase().equals("@BLANK@") ? "" : tmpClasses[i];
            if (i < tmpCompares.length && isNotBlank(tmpCompares[i]))
               comparisons[i] = tmpCompares[i].toUpperCase().equals("@BLANK@") ? "" : tmpCompares[i];
         }
      }

      /**
       * Following initialization, perform this logic:
       * 1. Find WebElement - if element not found, quit with error.
       * 2. Query the WebElement and get actual value
       * 3. Iterating over the parameeter arrays, verify each instance - the first instance to match - perform the branch to it!
       *    If none of the parameter sets were sucessfully verified, return with an error.
       *
       * @throws VerbException
       */
      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this);
         if (this.hasErrors())
            return;

         // Upon return from the basic validation method, the arrays for driving the logic are set up correctly.
         // Branches and Queries array have values at each index, other arrays may have blanks

         // Iterate {iterations} times over the logic
         boolean found = false;
         for(int iteration = 1; iteration <= iterations; iteration++) {
            if (found)
               break;
            clearComments();
            System.out.println("*** Iteration: " + iteration + " ***");
            try {

               clearErrors();
               setElement(null);
               FindElement.findElement(this);

               if (hasErrors()) {
                  clearErrors();
                  Verb.basicAddComment(this,"Errors encountered while trying to find common element!  Terminating action.");
               }
               else {
                  String actualText = this.getElement().getText();
                  String thisBranch = "";
                  String thisQuery = "";
                  String thisParam = "";
                  String thisComparison = "";
                  String thisValue = "";
                  String thisClass = "";

                  for (int i = 0; i < nBranches; i++) {
                     found = false;
                     thisBranch = branches[i];
                     thisQuery = queries[i];
                     thisComparison = comparisons[i];
                     thisValue = values[i];
                     thisClass = classes[i];

                     // Reconstruct the test context of the instance in order to carry interrogation of the element, verifications and branching
                     this.getContext().setProperty("InputSpecs", thisBranch);
                     this.getContext().setProperty("qryFunction", thisQuery);
                     this.getContext().setProperty("qryParam", thisParam);
                     this.getContext().setProperty("Value", thisValue);
                     this.getContext().setProperty("CompareMode", thisComparison);
                     this.getContext().setProperty("Class", thisClass);

                     //Verifier verifier = Verifier.getVerifier(getContext());
                     VerifyWebElement.verifyWebElement(this);
                     if (isPass()) {
                        // Upon successful verification results in creation of a new test case...
                        found = true;
                        try {
                           NewTest branch = new NewTest();
                           branch.getContext().setProperty("InputSpecs", thisBranch);
                           TestItem testItem = new TestItem("BranchOnElementValue.001", "newTest", "", "", "", "", "InputSpecs=" + thisBranch, "Branching via " + Util.sq(thisBranch) + ", Iteration: " + iteration);
                           testItem.initialize(1);
                           branch.getContext().setProperty("TestItem", testItem);
                           int thisLevel = this.getContext().getInt("level") + 1;
                           branch.getContext().setProperty("level", thisLevel);
                           System.out.println("Verification succeeded for option no: " + (i + 1) + ", branching using inputSpecs of: " + Util.sq(thisBranch) + ", Iteration: " + iteration);
                           NewTest.newTest(branch);
                           System.out.println("Back From Branching for option no: " + (i + 1) + ", branching using inputSpecs of: " + Util.sq(thisBranch) + ", Iteration: " + iteration);
                        } catch (Throwable e) {
                           Verb.basicAddComment(this, "Verification succeeded for option no: " + (i + 1) + ", but test case branched to using inputSpecs of: " + Util.sq(thisBranch) + ", Iteration: " + iteration + " FAILED!");
                           setException(e);
                           System.out.println(this.getComments());
                        }
                        finally {
                           System.out.println("Breaking on iteration " + iteration);
                           break;
                        }
                     } else
                        System.out.println("Did not match option no: " + (i + 1) + ", Branch: " + thisBranch + ", Iteration: " + iteration +", actual: " + actualText);

                     // Reset errors from verifications, etc. as this is just a relay mechanism, not an application testing mechanism
                     clearErrors();
                     if (found) {
                        Verb.basicAddComment(this, "Branched via: " + thisBranch + ",  Iteration: " + iteration);
                        break;
                     } else {
                        try {
                           System.out.println("Sleeeping after option no: " + (i + 1) + ", Branch: " + thisBranch + ", Iteration: " + iteration);
                           Thread.sleep(1000);
                        }
                        catch(Exception e) {
                           Verb.basicAddComment(this, "Failed on Thread.sleep! Testing commences.  Iteration: " + iteration);
                        }
                     }
                  }
                  if (!found)
                     Verb.basicAddComment(this, "None of the " + nBranches + " branch options were matched following " + iterations  + " iterations! Actual Text: " + actualText);
               }
            } catch (Exception e) {
               setException(e);
               Verb.basicAddError(this, "Errors encountered in branching logic.  Iteration: " + iteration);
            }
         }
      }

   }

   /**
    * Description
    * Click instances provide a generic clicking mechanism for web elements
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 02/23/17    |Bey      |Add optional hover prior to clicking
    * ============|=========|====================================
    */

   public static class Click extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         boolean doubleClick = getContext().getBoolean("double");
         String prefix = doubleClick ? "(Double) " : "";

         boolean hover = getContext().getBoolean("hover");
         prefix = hover ? (prefix + " (pre-hover) ") : prefix;
         try {
            FindElement.findElement(this);
            if (hasErrors())
               return;
            if ((getElement() instanceof WebElement)) {
            	if (hover) {
                    TestItem tmpItem = (TestItem) this.getContext().getProperty("testItem");
                    Hover hoverVerb = new Hover();
                    hoverVerb.basicDoIt(tmpItem);
            	}
            		
            	if (getElement().isEnabled()) {
                  new Actions(Driver.getDriver()).moveToElement(getElement()).perform();
                  getElement().click();
                  String blurb = "";
                  if (doubleClick) {
                      Thread.sleep(100);
                      getElement().click();
                     blurb = "Double ";
                  }
                  Verb.basicAddComment(this, ("Element " + prefix + blurb + "Clicked").replaceAll("  ", " "));
               } else
                  Verb.basicAddError(this, "Element not enabled - action failed");
            } else Verb.basicAddError(this, "Failed to find Web Element - Element not clicked!");
         } catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }

      }
   }

   /**
    * Description
    * ClickCell instances provide a generic clicking mechanism for 'cell' web elements where a 'cell' is an element contained in a <tbody>collection of <tr><td>elements</td></tr></tbody>
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class ClickCell extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;
         try {
            FindCell.findCell(this);
            if (hasErrors())
               return;
            if ((getElement() instanceof WebElement)) {
               // When a cell is found but the element to click is of a tag that is different than the cell's, a click on the cell may not do
               // Need to find the element with the tag
               String desiredTag = getContext().getString("tag");
               boolean foundElement = true;
               boolean doubleClick = getContext().getBoolean("double");
               if (!StringUtils.isBlank(desiredTag) && !desiredTag.equalsIgnoreCase(getElement().getTagName())) {
                  // Find the element to click within the cell.
                  // It must be an element with the desired tag AND get verified for the appropriate expected value.
                  List<WebElement> alternateElements = getElement().findElements(By.tagName(desiredTag));
                  String actualValue = "";
                  Verifier verifier = Verifier.getVerifier(getContext());

                  foundElement = false;
                  for (WebElement alternateElement : alternateElements) {
                     setElement(alternateElement);
                     UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
                     actualValue = weq.query(getContext());
                     if (hasErrors()) {
                        continue;
                     }

                     // Verify the present cell for specified value.
                     verifier.setAv(actualValue);
                     verifier.clearErrors();
                     verifier.verify();
                     if (verifier.isPass()) {
                        foundElement = true;
                     } // Verifier.isPass()
                  } // for alternate element
               }  // else (alternate tag search)

               if (foundElement && getElement().isEnabled()) {
                  new Actions(Driver.getDriver()).moveToElement(getElement()).build().perform();
                  getElement().click();
                  String blurb = "";
                  if (doubleClick) {
                     Thread.sleep(100);
                     getElement().click();
                     blurb = "Double ";
                  }
                  Verb.basicAddComment(this, "Cell " + blurb + "Clicked");
               } else
                  Verb.basicAddError(this, "(Cell) Element not enabled or cell's sub element not found - Action failed");
            } else Verb.basicAddError(this, "Failed to find (cell) Web Element - Action failed");
         } catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * CreateWebDriver starts in instance of the session's web driver type and navigates to the specified URL
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * 01/20/17    |Bey      |Support creation of web driver without URL
    * ============|=========|====================================
    */

   public static class CreateWebDriver extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         String browserName = this.getContext().getString("browser");
         if (isBlank(browserName))
            browserName = (String) DDTTestRunner.getVarsMap().get("browser");

         if (isBlank(browserName)) browserName = DDTSettings.Settings().getBrowserName();

         Driver.BrowserName browserType = Driver.asBrowserName(browserName);
         if (browserType == null) browserType = Driver.BrowserName.FIREFOX;
         Driver.set(browserType);
         String url = this.getContext().getString("url");
         if (isBlank(url)) {
            Verb.basicAddComment(this, "URL Not Provided!");
            //return;
         }

         try {
            WebDriver driver = Driver.get(url);
            if (!(driver instanceof WebDriver)) {
               Verb.basicAddError(this, "Unable to navigate to page: " + Util.sq(url) + " Please check URL.");
               return;
            }
            Verb.basicAddComment(this, url.isEmpty() ? "Web Driver Created" : "Web Driver created for URL " + Util.sq(url));

         } catch (Exception e) {
            setException(e);
         }
         catch (Throwable e) {
            setException(e);
         }

      }
   }

   /**
    * Description
    * DragAndDrop instances provide a generic "Drag And Drop" mechanism between two elements (fromElement and toElement).
    * The context has the two elements separated by some (String) separator - the default is ";"
    * The instance's DDTTestContext contains the necessary information in the form of "separator=..." where ... stands for some string value.
    * The separator is used to split the instance's locType and locSpecs enabling the specification of the fromElement and toElement
    * For example dragging from locator of id=theId and class=theClass with separator of "!!!"
    *    locType = id...class
    *    locSpecs = theId...theClass
    * And the DDTTestContext (data element) will have separator=!!!
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 01/13/17    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class DragAndDrop extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);

         // Drag and Drop validations...

         String theSeparator = getContext().getString("separator");
         if ((theSeparator == null) || (isBlank(theSeparator))) {
            theSeparator = ";";
            addComment("Separator not specified, using ';'");
         }

         String[] locTypes = (getContext().getProperty("loctype")).toString().split(theSeparator);
         String[] locSpecs = (getContext().getProperty("locspecs")).toString().split(theSeparator);

         if (locTypes.length < 1 ) {
            addError("Invalid TestItem - 'locTypes' is a single item - expecting two items ('from and to') = " + locTypes);
         }

         if (locSpecs.length < 1 ) {
            addError("Invalid TestItem - 'locSpecs' is a single item - expecting two items ('from and to') = " + locSpecs);
         }

         if (this.hasErrors())
            return;

         try {

            // We will use this as the basis for finding the 'from' and 'to' elements
            TestItem tmpItem = (TestItem) this.getContext().getProperty("testItem");
            tmpItem.setLocSpecs(locSpecs[0]);
            tmpItem.setLocType(locTypes[0]);

            FindElement fromVerb = new FindElement();

            fromVerb.basicDoIt(tmpItem);
            if (fromVerb.hasErrors())
               addError("'From Action' Errors: " + fromVerb.getErrors());
            else
               addComment("'From Action' - Element Found!");

            FindElement toVerb = new FindElement();
            tmpItem.setLocSpecs(locSpecs[1]);
            tmpItem.setLocType(locTypes[1]);

            toVerb.basicDoIt(tmpItem);
            if (toVerb.hasErrors())
               addError("'To Action' Errors: " + toVerb.getErrors());
            else
               addComment("'To Action' - Element Found!");

            if (hasErrors())
               return;

            // With both Elements (from and to) found, create the action

            WebElement fromElement = fromVerb.getElement();
            fromElement.click();
            WebElement toElement = toVerb.getElement();
            Actions builder = new Actions(Driver.getDriver());
            builder.dragAndDrop(fromElement, toElement).build().perform();
            String blurb = "Dragged from: " + fromElement.toString() + " to: " + toElement.toString();
            addComment(blurb);
         } catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * EnsurePageLoaded instances ensures the expected WebDriver page is loaded
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class EnsurePageLoaded extends Verb {

      public boolean isUIVerb() { return true;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static EnsurePageLoaded copy(Verb original) {
         EnsurePageLoaded copy = new EnsurePageLoaded();
         copy.setContext(original.getContext());
         return copy;
      }

      /**
       * EnsurePageLoaded is used for ensuring a page is loaded
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent for which to ensure driver is loaded
       * @throws VerbException
       */
      public static void ensurePageLoaded(Verb verb) throws VerbException{
         EnsurePageLoaded copy = new EnsurePageLoaded();
         copy.setContext((verb.getContext()));
         copy.doIt();
      }

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         String searchTitle;
         // Get the function to use in verification
         String functionName = getContext().getString("QryFunction");
         // Get the expected value from the data properties structure.
         String expectedValue = getContext().getString("value"); // User expects this value - it may serve for comparison with GetTitle or other property
         boolean loaded;
         if (functionName.equalsIgnoreCase("getTitle"))
            searchTitle = expectedValue;
         else
            searchTitle = getContext().getString("pagetitle");

         if (isBlank(searchTitle)) {
            Verb.basicAddError(this, "No value provided for page title search for ensuring page was loaded");
            return;
         }

         Long waitInSeconds = getContext().getStringAsLong("WaitTime");
         if (0L == waitInSeconds)
            waitInSeconds = DDTSettings.Settings().getWaitTime();
         int waitIntervalMillis = getContext().getStringAsInteger("WaitInterval");
         if (waitIntervalMillis ==0)
            waitIntervalMillis = DDTSettings.Settings().getWaitInterval();

         WebDriver driver = Driver.getDriver();

         try {
            if (driver instanceof WebDriver) {
               String actualTitle = driver.getTitle();
               loaded = (actualTitle.toLowerCase().contains(searchTitle.toLowerCase()));
               if (!loaded)
                  Verb.basicAddError(this, "Page Title (" + actualTitle + ") does not contain '" + searchTitle + "'");
            }
            else {
               // we always wait for at least one second (may be less if element satisfies the expected conditions sooner)
               loaded = new WebDriverWait(driver, waitInSeconds, waitIntervalMillis).until(ExpectedConditions.
                       titleContains(searchTitle));

               if (loaded) {
                  Verb.basicAddComment(this, "Page with title containing " + Util.sq(searchTitle) + " Loaded");
               } else {
                  Verb.basicAddError(this, "Page with title containing " + Util.sq(searchTitle) + " Not Loaded (? Timeout expired ?");
               }
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!this.hasException())
               throw new VerbException(this, "Failed ensuring web page loaded.");
         }
      }
   }

   /**
    * Description
    * FindCell locates a cell in a web table structure using various options provided in the instant's testContext
    * The instance's DDTTestContext contains the necessary information to instantiate a Locator and locate the element first
    * FindCell first finds the table where the cell is the top element in the table or tbody) using the Query and Data properties in the DDTTestContext instance
    * The locator(s) in DDTTestContext are to the top table or tbody element
    * This may be called as a verb (action) in its own right or as a precursor to (say) click - with the intention of clicking a found cell
    * In the former case (FindCell as a standalone action) first, use the FindElement verb.
    * In the latter case (precursor to clicking) the top (table or tbody) element was already found and the FindElement find the children of this element
    * If the table is found, the tokens in the DDTTestContext are used to (potentially) restrict the search to some row, or column or row/column combination
    * Note: if the cell is found, it remains as the DDTTestContext.getElement() and can be clicked on (or acted upon) subsequently
    * <p/>
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class FindCell extends Verb {

      public boolean isUIVerb() { return true;}


      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       *
       * @param original - the original FindElement verb
       * @return
       */
      public static FindCell copy(Verb original) {
         FindCell copy = new FindCell();
         copy.setContext(original.getContext());
         copy.setElement(original.getElement());
         return copy;
      }

      /**
       * findCell is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       *
       * @param verb the parent FindeCell for which to find element
       * @throws VerbException
       */
      public static void findCell(Verb verb) throws VerbException {
         FindCell actor = FindCell.copy(verb);
         actor.doIt();
         verb.setElement(actor.getElement());
         verb.addComment(actor.getComments());
         verb.addError(actor.getErrors());
         verb.setException(actor.getException());

      }

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         String col = "";
         int colNo = -1;
         String row = "";
         int startCol = 0;
         int startRow = 0;
         int rowNo = -1;
         int firstRowToExamine = -1;
         int lastRowToExamine = -1;
         int nCellsExamined = 0;
         int nCellsFound = 0;
         int nRows = -1;
         int rowIndex = 0;
         int colIndex = 0;
         String alternateTag = "";
         String rowRange = "";
         String cellsToFind = "";
         boolean doneSearching = false;
         boolean findRange = false;
         boolean foundCell = false;
         boolean atRow = false;
         boolean atCol;
         int phase = 0;
         int nCellsToFind = -1;

         try {
            // The top element may have already been found by caller(s) such as 'click'
            // In this case, the element is (by convention) the top (table or tbody) element of a table and the cell logic may commence
            if (!(getElement() instanceof WebElement)) {
               // 'Standalone' findCell case - use findElement to locate the top element (table or tbody) of the table to be used as an 'anchor' for cell search.
               FindElement.findElement(this);
               if (hasErrors())
                  return;
            }
            phase++;

            boolean reportEachTableCell = DDTSettings.Settings().getReportEachTableCell();
            // Get a template verifier
            Verifier verifier = Verifier.getVerifier(getContext());

            // Determine the (optionally) requested column or row in the table to look at
            colNo = getContext().getStringAsInteger("col");
            rowNo = getContext().getStringAsInteger("row");

            // Get the alternative tag name to look for if the element is to be found on an item other than <tr> / <td> option needed for verification (ignorecase, ...) @TODO See if more needed
            alternateTag = getContext().getString("tag"); // use this tag if the element is not found in a cell on the <td> tag
            if (StringUtils.isBlank(alternateTag))
               alternateTag = "";

            phase++;

            // Get the number of cells to find requested by the user - if invalid - set it to 1.
            nCellsToFind = getContext().getStringAsInteger("cellstofind");

            // Get the specification for range of rows in which to to find cells - if any.
            // A range of rows to examine (may conflict with "row" specs)

            rowRange = getContext().getString("rowrange");
            if (isBlank(rowRange))
               rowRange = "";

            String[] rowRangeSpecs;

            phase++;

            try {
               if (!(isBlank(rowRange)) && rowRange.contains("-")) {
                  rowRangeSpecs = rowRange.split("-");
                  if (rowRangeSpecs.length == 2) {
                     firstRowToExamine = Integer.valueOf(rowRangeSpecs[0]);
                     lastRowToExamine = Integer.valueOf(rowRangeSpecs[1]);
                     if (firstRowToExamine > lastRowToExamine) {
                        // Silly definition - switche'm
                        int tmp = firstRowToExamine;
                        lastRowToExamine = firstRowToExamine;
                        firstRowToExamine = tmp;
                     }
                     findRange = true;
                  } // if valid range specified...
                  else {
                     // Give up, user is not following range conventions  - used multiple dashes
                     Verb.basicAddError(this, "Invalid input ignored - too many dashes in range specification: " + Util.sq(rowRange));
                     return;
                  }
               } // if establishing range of rows to examine
            } // Try to convert row range to integers
            catch (Exception e) {
               // Do not overwrite previous exceptions!
               if (!hasException())
                  setException(e);
            } // catch trying to get a valid range of rows.

            if ((getElement() instanceof WebElement)) {

               // Assume all rows are represented by "tr" tag.
               List<WebElement> rows = getElement().findElements(By.tagName("tr"));
               nRows = rows.size();
               if (nRows > 0) {
                  startCol = (colNo > 0) ? colNo : 0;
                  startRow = (rowNo > 0) ? rowNo : 0;
                  if (startRow > 0 && (findRange && firstRowToExamine == lastRowToExamine) && firstRowToExamine != startRow) {
                     // Abandon Search
                     Verb.basicAddError(this, "Conflicting 'Row' and 'RowRange' specification (Single row-range specified that is different than row to examine).  Find cell aborted.");
                     return;
                  }

                  if (lastRowToExamine > nRows) {
                     Verb.basicAddError(this, "Invalid 'RowRange' specification (last row to examine " + Util.sq(String.valueOf(lastRowToExamine)) + " exceeds number of rows in range " + Util.sq(String.valueOf(nRows)) + ".  Search cell aborted.");
                     return;
                  }

                  if (startRow <= nRows) {
                     // From the available specs, determine the number of rows to examine.
                     // A range of rows represented by the formula lastRowToExamine - firstRowToExamine + 1
                     // A specific row - 1 row should be examined
                     // Otherwise, examine all rows.
                     if (findRange) {
                        startRow = firstRowToExamine;
                     }

                     for (WebElement theRow : rows) {
                        rowIndex++;
                        atRow = atRow || ((startRow > 0 && rowIndex == startRow) || (startRow == 0));
                        // skip rows until the start row - or look at each row - if no row limit specified.
                        if (!atRow && startRow > 0)
                           continue;

                        // Iterate over the cells of this row, using logic similar to  rows above...
                        List<WebElement> cells = theRow.findElements(By.tagName("td"));
                        int nCols = cells.size();
                        colIndex = 0;
                        if (startCol <= nCols) {
                           for (WebElement theCell : cells) {
                              colIndex++;
                              foundCell = false;
                              // skip cells until the start cell - or look at each cell - if no cell specified.
                              atCol = ((startCol > 0 && colIndex == startCol) || (startCol == 0));
                              if (!atCol && startCol > 0)
                                 continue;
                              // We are now positioned at a cell to be examined.
                              // Replace the current test context so it can be interrogated and qualified or disqualified

                              // Uncomment when testing to see in console which cells were actually examined
                              //testContext.addComment("Examine Cell [" + rowIndex + "," + colIndex + "]");

                              nCellsExamined++;
                              setElement(theCell);

                              UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
                              String actualValue = weq.query(getContext());
                              if (hasErrors()) {
                                 Verb.basicAddError(this, "Table Cell [" + rowIndex + "," + colIndex + "]. Cell search aborted.");
                                 return;
                              }
                              verifier.clearErrors();
                              verifier.setAv(actualValue);
                              verifier.verify();
                              if (verifier.isPass()) {
                                 foundCell = true;
                                 nCellsFound++;

                                 if (reportEachTableCell) {
                                    Verb.basicAddComment(this, "Cell Found - " + verifier.getComments() + " - Found table cell with specified value at [" + rowIndex + "," + colIndex + "].");
                                 }

                                 if (nCellsFound >= nCellsToFind) {
                                    doneSearching = true;
                                    break;
                                 } // doneSearching - main logic
                              }  // Verifier.isPass()
                              else {
                                 if (reportEachTableCell) {
                                    Verb.basicAddComment(this, "Cell Not Found - " + verifier.getErrors() + " - at table cell [" + rowIndex + "," + colIndex + "].");
                                 }
                                 // Look for alternate elements 'deeper' in the cell to locate the desired element
                                 List<WebElement> alternateElements = theCell.findElements(By.tagName(alternateTag));
                                 for (WebElement alternateElement : alternateElements) {
                                    setElement(alternateElement);
                                    actualValue = weq.query(getContext());
                                    if (hasErrors()) {
                                       Verb.basicAddError(this, "Table Cell (Alternate Search) [" + rowIndex + "," + colIndex + "] Failed");
                                       return;
                                    }

                                    // Verify the present cell for specified value.
                                    verifier.clearErrors();
                                    verifier.setAv(actualValue);
                                    verifier.verify();
                                    if (verifier.isPass()) {
                                       if (reportEachTableCell) {
                                          Verb.basicAddComment(this, "Cell Found - " + verifier.getComments() + " - Found table cell with specified value at [" + rowIndex + "," + colIndex + "].");
                                       }
                                       foundCell = true;
                                       nCellsFound++;
                                       if (nCellsFound >= nCellsToFind) {
                                          doneSearching = true;
                                          break;
                                       } // doneSearching - Alternate (deep) search logic
                                    } // Verifier.isPass()
                                 } // for alternate element
                              }  // else (alternate tag search)
                           } // for cell in cells (column in this row's columns)

                           if (doneSearching)
                              break; // All cells requested found - Comment added above
                        } // startCol in range
                        else {
                           // Handle invalid number of columns in a row.
                           if (nCols > 0) {
                              if (reportEachTableCell)
                                 Verb.basicAddComment(this, "Specified cell number (" + startCol + ") is greater than table's number of columns at row: " + rowIndex + ", nCols: " + nCols + ". Row skipped, Cell search continued.");
                              continue;
                           }
                        } // startCol > ncols


                        // Evaluate search results at end of this row - if all cells to find were found - we're done!
                        if (foundCell) {
                           doneSearching = (doneSearching || (nCellsFound >= nCellsToFind));
                           if (doneSearching) {
                              break;
                           }
                        }
                        // If row range was indicated and the range have been exceeded - terminate search!
                        if (rowIndex >= lastRowToExamine && lastRowToExamine > 0)
                           break;
                     } // for row in rows (row search)

                     // All rows searched - evaluate results.
                     if (doneSearching) {
                        String comment = "Cell search succeeded. ";
                        comment += nCellsExamined + " " + ((nCellsExamined > 1) ? " cells " : " cell ") + "examined. ";
                        comment += nCellsFound + " " + ((nCellsFound > 1) ? " cells " : " cell ") + "found. ";

                        Verb.basicAddComment(this, comment);
                     }  // doneSearching
                     else {
                        // All rows searched but number of cells to find is less than number of cells found

                        String error = "Cell search failed. ";

                        error += nCellsExamined + " " + ((nCellsExamined > 1) ? " cells " : " cell ") + "examined. ";
                        error += nCellsFound + " " + ((nCellsFound != 1) ? " cells " : " cell ") + "found. ".replace("1", "no");

                        if (nCellsToFind > 0) {
                           error += nCellsToFind + ((nCellsToFind != 1) ? " cells " : " cell ") + " should have been found. ";
                        }

                        Verb.basicAddError(this, error);
                     } // cell not found in this row.
                  } // startRow <= nRows
                  else {
                     if (nRows > 0) {

                        Verb.basicAddError(this, "Specified row number (" + startRow + ") is greater than table's number of rows (" + nRows + "). Cell search aborted.");
                        return;
                     }
                  } // handle invalid number of rows (startRow > nRows).
               } // nRows > 0
               else
                  Verb.basicAddError(this, "No rows found in table - Cell search aborted.");
            } else
               Verb.basicAddError(this, "Failed to find table Web Element - Cell search aborted.");
         } catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * FindElement locates a Web Element within the current driver's page
    * The instance's DDTTestContext contains the necessary information to instantiate a Locator and locate the element
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class FindElement extends Verb {

      public boolean isUIVerb() { return true;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static FindElement copy(Verb original) {
         FindElement copy = new FindElement();
         copy.setContext(original.getContext());
         copy.setElement(original.getElement());
         return copy;
      }

      /**
       * findElement is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent FindeElement for which to find element
       * @throws VerbException
       */
      public static void findElement(Verb verb) throws VerbException{
         FindElement actor = FindElement.copy(verb);
         actor.doIt();
         verb.setElement(actor.getElement());
         verb.addComment(actor.getComments());
         verb.addError(actor.getErrors());
         verb.setException(actor.getException());
      }

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         UILocator.WebUILocator locator = new UILocator.WebUILocator();
         locator.locate(getContext());
         addError(locator.getErrors());
         setException(locator.getException());
         if (this.hasErrors())
            return;

         this.setElement(locator.getElement());
         Verb.basicAddComment(this, "Element Found!");

         // Support for saving elements in TestRunner's elements Map
         String eKey = getContext().getString("saveelementas");
         if (!isBlank(eKey))
            DDTTestRunner.addElement(eKey, this.getElement());
      }
   }

   /**
    * Description
    * FindOption locates an option in a drop down or a list
    * The instance's DDTTestContext contains the necessary information to instantiate a Locator and locate the element
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class FindOption extends Verb {

      public boolean isUIVerb() { return true;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static FindOption copy(Verb original) {
         FindOption copy = new FindOption();
         copy.setContext(original.getContext());
         copy.setElement(original.getElement());
         return copy;
      }

      /**
       * findElement is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent FindeElement for which to find element
       * @throws VerbException
       */
      public static void findOption(Verb verb) throws VerbException{
         FindOption actor = FindOption.copy(verb);
         actor.doIt();
         // Pass context up
         verb.setElement(actor.getElement());
         verb.getContext().setProperty("element", actor.getElement());
         verb.addComment(actor.getComments());
         verb.addError(actor.getErrors());
         verb.setException(actor.getException());
      }

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         // Get a template verifier from the testContext object
         Verifier verifier = Verifier.getVerifier(getContext());

         // User can specify either ItemValue or ItemText - ItemText has priority
         // ItemText
         String textToSelectBy = getContext().getString("itemtext");
         if (isBlank(textToSelectBy))
            textToSelectBy = "";

         // ItemValue
         String valueToSelectBy = getContext().getString("itemvalue");
         if (isBlank(valueToSelectBy))
            valueToSelectBy = "";

         // User may want to get a specific (1 based as opposed to 0 based) item!
         int itemIndex = getContext().getStringAsInteger("itemno");
         if (itemIndex < 1)
            itemIndex = -1;

         // Determine the type of selection
         String selectBy = "itemtext";
         if (org.apache.commons.lang.StringUtils.isBlank(textToSelectBy) && !org.apache.commons.lang.StringUtils.isBlank(valueToSelectBy))
            selectBy = "itemvalue";

         // indicates whether to select option by its value property or text property
         boolean selectByText = (selectBy.equalsIgnoreCase("itemtext"));

         try {
            // *** Successful find of element results in storage of byChain in testContext...
            FindElement.findElement(this);
            if (hasErrors())
               return;

            if ((getElement() instanceof WebElement)) {
               int optionNo = 0;
               String optionText="";
               String optionValue="";
               WebElement theOption = null;
               List<WebElement> options = getElement().findElements(By.tagName("option"));
               boolean foundOption=false;
               for (WebElement item : options) {
                  optionNo++;
                  if (itemIndex == optionNo) {
                     foundOption = true;
                  }
                  else {
                     optionText = item.getText();
                     optionValue = "";
                     if (selectBy.equalsIgnoreCase("itemvalue"))  {
                        // item should respond to css "value"
                        try {
                           optionValue = item.getCssValue("value");
                        }
                        catch (Exception e) {
                           setException(e);
                           Verb.basicAddError(this, "Failed to get value of option No. " + optionNo);
                           return;
                        }
                     }

                     if (isBlank(optionValue + optionText) && !(isBlank(valueToSelectBy + textToSelectBy)))
                        continue;

                     // Set the expected and actual values in the preset verifier
                     if (selectBy.equalsIgnoreCase("itemvalue"))  {
                        verifier.setEv(valueToSelectBy);
                        verifier.setAv(optionValue);
                     }
                     else if (selectBy.equalsIgnoreCase("itemtext")) {
                        verifier.setEv(textToSelectBy);
                        verifier.setAv(optionText);
                     }
                     verifier.clearErrors();
                     verifier.verify();
                     foundOption = verifier.isPass();
                  }

                  if (foundOption) {
                     theOption = item;
                     break;
                  }
               }

               if (theOption instanceof WebElement) {
                  setElement(theOption);
                  Verb.basicAddComment(this, "Option Found: " + (selectByText ? Util.sq(textToSelectBy) : Util.sq(valueToSelectBy)));
               }
               else {
                  Verb.basicAddError(this, "Option Not Found: " + (selectByText ? Util.sq(textToSelectBy) : Util.sq(valueToSelectBy)));
               }
            }
            else
               Verb.basicAddError(this, "Failed to find web element.");
         } //Try finding the element and processing it
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }

      }
   }

   /**
    * Description
    * GenerateReport instances generate the DDTTestRunner's report
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/30/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class GenerateReport extends Verb {

      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            String description = getContext().getString("Title");
            String emailBody = getContext().getString("EmailBody");

            DDTTestRunner.generateReport(description, emailBody);

            Verb.basicAddComment(this, "Generated report titled: " + Util.sq(description));
         }
         catch (IOException e) {
            throw new VerbException(this, "Report generation failed. (" + e.getMessage().toString() + ")");
         }
         catch (Exception e) {
            throw new VerbException(this, "Report generation failed. (" + e.getMessage().toString() + ")");
         }
         finally {
            DDTTestRunner.resetRptCounters();
         }
      }
   }

   /**
    * Description
    * HandleAlert instances are used to handle alerts put up by the application
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/30/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class HandleAlert extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         // Get the expected response from the data properties structure.
         // If exists, ensure it is valid, if blank, assume Accept.
         String userAction = getContext().getString("response"); // if present, should be Accept or Reject
         if (isBlank(userAction)) {
            userAction = "accept";
         }
         else {
            if (!"##accept##reject##".contains(userAction.toLowerCase())) {
               Verb.basicAddError(this, "Invalid Alert Response type encountered " + Util.sq(userAction) + " - only 'Accept' or 'Reject' are allowed.");
               return;
            }
         }

         // Get a verifier instance from the testContext instance
         Verifier verifier = Verifier.getVerifier(getContext());

         WebDriver driver = Driver.getDriver();

         try {
            Alert alert = driver.switchTo().alert();
            String actualMessage = alert.getText();
            String expectedMessage = getContext().getString("value");
            if (isBlank(expectedMessage))
               expectedMessage = "";
            if (StringUtils.isNotBlank(expectedMessage))   {
               verifier.setAv(actualMessage);
               verifier.verify();
               if (verifier.isPass())
                  Verb.basicAddComment(this, verifier.getComments());
               else
                  Verb.basicAddError(this, verifier.getErrors());
            }

            if (userAction.equalsIgnoreCase("accept")) {
               alert.accept();
               Verb.basicAddComment(this, "Alert Accepted");
            }
            else {
               alert.dismiss();
               Verb.basicAddComment(this, "Alert Dismissed");
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * Hover instances are used to hover over a web element for some period of time - typically used to get drop-down menus or options displayed
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 07/14/15    |Bey      |Initial Version
    * 08/16/15    |Bey      |Introduce action.release() after wait
    * 01/26/17    |Bey      |Improve error trapping & Try to improve mouse hovet (which still does not work...)
    * ============|=========|====================================
    */

   public static class Hover extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         // Get the expected response from the data properties structure.
         // If exists, ensure it is valid, if blank, assume Accept.
         String waitTime = getContext().getString("waittime");
         if (isBlank(waitTime))
            waitTime = "1";
         // Make the seconds to long milliseconds
         waitTime += "000";

         Long longWaitTime = 1000L; // One second
         try {
            longWaitTime = Long.parseLong(waitTime);
         }
         catch (Exception e) {
            // Do nothing - just use one second
         }

         // Find the element to hover over
         FindElement.findElement(this);
         if (hasErrors())
            return;
         WebElement element = getElement();
         WebDriver driver = Driver.getDriver();
         if ((element instanceof WebElement)) {
/* *** Try this... - abstract it to a UI verb where {driver} and {webElement} are valid
String javaScript = "var evObj = document.createEvent('MouseEvents');" +
                    "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
                    "arguments[0].dispatchEvent(evObj);";
((JavascriptExecutor)driver).executeScript(javaScript, webElement);
**/
            Actions action = new Actions(driver);
            // This doesn't work...
            action.moveToElement(element).build().perform();
            // This doesn't work either
            action.moveToElement(element, element.getLocation().getX()+2, element.getLocation().getY()+2).build().perform();
            // This doesn't work either
            Locatable hoverItem = (Locatable) getElement();
            Mouse mouse = ((HasInputDevices) driver).getMouse();
            // For some reason, this does not work... The mouse does not move anywhere
            mouse.mouseMove(hoverItem.getCoordinates(),2L, 2L);
            System.out.println(hoverItem.getCoordinates().getAuxiliary().toString());
            try {
               Thread.sleep(longWaitTime);
               action.release();
            }
            catch (InterruptedException t) {System.out.println(t.getMessage());}
         }
      }
   }

   /**
    * Description
    * Maximize instances Maximize the current web page displayed by the driver
    * This verb should be used 'safely' only within the context of a DDTTestRunner test session.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class Maximize extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         try {
            Driver.getDriver().manage().window().maximize();
         }
         catch (Exception e) {
            setException(e);
         }

      }
   }

   /**
    * Description
    * NavigateToPage instances navigate to a web page that is specified in the verb's testContext
    * The instance's DDTTestContext contains the necessary information - a page url to navigate to
    * This verb should be used 'safely' only within the context of a DDTTestRunner test session.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 06/11/15    |Bey      |Implement Back, Forward, Refresh
    * ============|=========|====================================
    */

   public static class NavigateToPage extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         String url = getContext().getString("url");
         if (isBlank(url)) {
            Verb.basicAddError(this, "URL is required for this action");
            return;
         }

         WebDriver driver = Driver.getDriver();

         try {
            // Determine where user wants to navigate to...
            switch(url.toLowerCase()) {
               case "back" : driver.navigate().back(); break;
               case "forward" : driver.navigate().forward(); break;
               case "refresh" : driver.navigate().refresh(); break;
               default:      if (!url.isEmpty())
                              driver.navigate().to(url);
            }
            Verb.basicAddComment(this, "Navigated to " + Util.sq(url));
         }
         catch (Exception e) {
            setException(e);
         }
      }
   }

   /**
    * Description
    * NewTest instances create a new test case represented as an instance of DDTTestRunner.
    * The instance's DDTTestContext contains the necessary information - essentially a InputSpecs instance
    * This verb should be used 'safely' only within the context of a DDTTestRunner test session.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/30/14    |Bey      |Initial Version
    * 06/06/15    |Bey      |Added recursion mechanism (newTest and copy)
    * 07/24/15    |Bey      |Fix issues with level propagation
    * 01/01/17    |Bey      |Provide for default test id (items container name)
    * ============|=========|====================================
    */

   public static class NewTest extends Verb {

      public boolean isUIVerb() { return false;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static NewTest copy(Verb original) {
         NewTest copy = new NewTest();
         copy.setContext(original.getContext());
         return copy;
      }

      /**
       * newTest is used mainly for handling the recursive nature of this product where recursion to new test is needed.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent NewTest for which to run on some  input specs
       * @throws VerbException
       */
      public static void newTest(Verb verb) throws VerbException{
         NewTest actor = NewTest.copy(verb);
         actor.doIt();
         verb.addComment(actor.getComments());
         verb.addError(actor.getErrors());
         verb.setException(actor.getException());
      }

      public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         try {
            TestItem testItem = (TestItem) getContext().getProperty("testItem");
            int level = 1 + testItem.getLevel();
            String[][] testItemStrings;
            String inputSpecs = getContext().getString("InputSpecs");
            getContext().setProperty("level", level);

            // Test Items Strings provider - String[n][] where each of the n rows is a collection of strings making up a TestItem instance.
            // stringProviderSpecs contains information about the test item strings provider - err out if it is invalid.
            TestStringsProviderSpecs stringProviderSpecs = new TestStringsProviderSpecs(inputSpecs);
            if (!stringProviderSpecs.isSetupValid()) {
               Verb.basicAddError(this, "Invalid Test Strings Provided - " + stringProviderSpecs.getErrors());
               return;
            }

            // With valid stringProviderSpecs, attempt to get the strings making up the new test and err out if not successful
            testItemStrings = TestStringsProvider.provideTestStrings(stringProviderSpecs, DDTSettings.Settings().getDataFolder());
            if (testItemStrings.length < 1) {
               Verb.basicAddError(this, "Failed to get test item strings from Test Strings Provider.");
               return;
            }

            // With test item strings provided, start a sub test runner, assemble the test items and have the sub test runner process those.
            DDTTestRunner runner = new DDTTestRunner();
            runner.setLevel(level); // Recursion level propagated

            TestItem.TestItems testItems = new TestItem.TestItems();
            testItems.setItems(TestItem.assembleTestItems(testItemStrings, stringProviderSpecs.getItemsContainerName()));
            testItems.setParentItem(testItem);
            runner.setTestItems(testItems);
            runner.setParentStepNumber(testItem.getSessionStepNumber());
            runner.processTestItems();
            if (TestItem.isNestedReporting) {
               for (TestItem item : testItems.getItems()) {
                  if (!item.isEmpty()) {
                     //item.finalizeExtentTest();
                     testItem.getExtentTest().appendChild(item.getExtentTest());
                     item.finalizeExtentTest();
                  }
               }

               testItem.finalizeExtentTest();
            }
            if (runner.failed()) {
               String error = (isBlank(testItem.getErrors()) ? "" : testItem.getErrors()) + (" " + runner.getErrors().trim());
               if (isBlank(error)) {
                  error = "Test Case with ID of " + testItem.getId() + " failed";
               }
               // Do not consider an aggregate test runner step as failure but note there were errors in this 'test case'
               // (Only if the thing raises exception it is considered as failure)
               Verb.basicAddComment(this, error);
            }
            else Verb.basicAddComment(this, "Test Case Passed.");
         }
         catch (NullPointerException e) {
            Verb.basicAddError(this, "Failed . (" + e.getMessage().toString() + ")");
         }
         catch (Exception e) {
            Verb.basicAddError(this, "Failed . (" + e.getMessage().toString() + ")");
         }
      }
   }

   /**
    * Description
    * Not Implemented instances just returns an error
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class NotImplemented extends Verb{

      public boolean isUIVerb() { return false;}

      public void doIt()  throws VerbException {
         basicValidation(this, false);
         if (this.hasErrors())
            return;

         String error = getContext().getString("Error");

         Verb.basicAddError(this, isBlank(error) ? "Action not implemented" : error);
      }
   }

   /**
    * Description
    * Quit just closes the current web driver if it exists
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * 08/21/15    |Bey      |Bug in doIt fixed - ensure both, driver is instance of WebDriver AND has handles
    * ============|=========|====================================
    */

   public static class Quit extends Verb {

      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            WebDriver d = Driver.getDriver();
            if (d instanceof WebDriver  && (d.getWindowHandles().size() > 0)) {
               d.close();
               Verb.basicAddComment(this, "Web Driver Closed.");
            }
            else
               Verb.basicAddComment(this, "Driver not present - Action aborted.");
         }
         catch (Exception e) {
            setException(e);
         }
      }
   }

   /**
    * Description
    * RefreshSettings just resets the DDTSettings
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class RefreshSettings extends Verb {

      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            DDTSettings.reset();
            Verb.basicAddComment(this, "Settings Reset");
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * RunCommand runs an OS command with information regarding the module to invoke and optional parameters
    * The module to invoke can be an exe, batch file, shell script, etc.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * 07/26/15    |Bey      |Fix bug with output buffer
    * ============|=========|====================================
    */

   public static class RunCommand extends Verb{

      public boolean isUIVerb() { return false;}

      public void doIt()  throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         String file = getContext().getString("filename");
         if (isBlank(file)) {
            Verb.basicAddError(this, "File Name is required but is missing - action aborted.");
            return;
         }

         // Enable short-hand reference to the scripts folder - facilitate organization of script files.
         if (file.toLowerCase().startsWith("%script%"))
            file = DDTSettings.Settings().getScriptsFolder()+ file.substring(8);
         if (!file.contains("\\") && !file.contains("/")) {
            // User implies invoke a file in the project's Scripts folder
            file = DDTSettings.Settings().getScriptsFolder() + file;
         }

         // @TODO Figure out how to handle Linux / Unix executables more elegantly (the code below works with fully qualified path as in "/bin/sh" instead of "sh" for system commands
         // String os = System.getProperty("os.name").toLowerCase();
         File f = new File(DDTSettings.asValidOSPath(file, true));
         if(!(f.exists()) || f.isDirectory()) {
             Verb.basicAddError(this, "File " + Util.sq(file) + " is not a valid script or executable file - Action aborted.");
             return;
         }

         // Create a parameters array, the first of which is the (executable) file name with optional parameters
         ArrayList<String> params = new ArrayList<String>();
         params.add(file);
         int i = 0;
         // Check for and (if any) build arguments string
         if (this.getContext().size() > 0) {
            // Build the args array from dataProperties.
            // Parameters to executable are optional and are of the form ParamX={value}
            // Example:
            // Param1=some value;Param2=Another Value;Param3=Etc.
            while (true) {
               i++;
               String thisKey = "param" + String.valueOf(i);
               String thisParam = getContext().getString(thisKey);
               if (isBlank(thisParam)) {
                  break;
               }
               else
                  params.add(thisParam);
            }
         }

         i = params.size();

         String[] args = new String[i];
         int j = 0;
         for (String param : params)  {
            args[j] = param;
            j++;
         }

         String execAndArgs = DDTSettings.asValidOSPath(Util.asString(args, ", "), true);
         // Execute the command with the optional parameters (and cross your fingers)
         try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(args);
            StringBuilder sbE = new StringBuilder (pr.getErrorStream().read());
            String errors = sbE.toString();
            OutputStream sbO = pr.getOutputStream();
            String output = sbO.toString();

            if (!isBlank(errors)) {
               Verb.basicAddError(this, "Command Process terminated abnormally with errors: " + errors);
            }

            if (isBlank(output))
               Verb.basicAddComment(this, "Command process (" + execAndArgs + ") executed.");
            else
               Verb.basicAddComment(this, "Command process (" + execAndArgs + ") executed with output: " + Util.sq(output));
         }
         catch (Exception e) {
            setException(e);
         }
      }
   }

   /**
    * Description
    * RunExternalMethod instances provide an extesibility to the DDT project
    * For this verb to work, the class in which to run and the method to invoke must be within the class path of the project.
    * The method must  be a boolean method.
    * The method can either accept no parameters (if it needs no context to work with) or an instance instance of DDTTestContext...
    * A method must be provided.
    * If a class is not provided, the DDTExternal class within the DDT project is the developer sandbox - it is bereft of any methods except for two methods that are used for demo purposes
    * iPass - always passes
    * iFail - always fails
    * The instance's DDTTestContext contains the necessary information (class, method)
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 06/14/15    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class RunExternalMethod extends Verb {
      private String methodName = "";
      private String className = "DDTExternal"; // Default
      private String constructorTypeName = "DDTTestContext";  // Default
      private boolean isDriverRequired = true;  // More exploration of this feature will indicate more clearly what the default should be...

      // If the external method is, indeed, a UI method, it has to take care of its own handling of a web driver
      // or use the instance's context where the DDT (singleton) driver will be passed)
      // To avoid clashing with DDT design we set this to false...
      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException {

         debug(this);

         methodName = this.getContext().getString("method");
         if (isBlank(methodName))  {
            Verb.basicAddError(this, "Faulty Setup.  Method Name is required but is missing for this verb.  Action failed.");
            return;
         }

         className = this.getContext().getString("class");
         if (isBlank(className))  {
            Verb.basicAddComment(this, "No class name provided, using project's default class name (DDTExternal).");
         }

         constructorTypeName = this.getContext().getString("type");
         if (isBlank(constructorTypeName))  {
            Verb.basicAddComment(this, "No constructor type name provided, using the default constructor type of (DDTTContext).");
         }

         isDriverRequired =  this.getContext().getBoolean("driverRequired");

         basicValidation(this, isDriverRequired);
         if (this.hasErrors())
            return;

         try {
            if (isDriverRequired)
               this.getContext().setProperty("driver", Driver.getDriver());

            invokeMethod(className, methodName, constructorTypeName, this.getContext());

         } catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }

      /**
       * Invokes an action whose name is specified by className
       * Use reflection to convert className to a method in the DDTExternal class that, by convention uses a DDTTestContext parameter as its input parameter.
       * TODO Make it more generic (handle classes other than DDTExternal, do not insist on DDTTestContext as a parameter to the invoked method ad find a way ?listener? to get results.)
       */
      private boolean invokeMethod(String className, String methodName, String typeName, DDTTestContext testContext) throws Exception {

         Method m = null;
         Class c = null;
         Class cc = null;

         if (isBlank(className)) {
            Verb.basicAddError(this, "Faulty Setup: Class name is required for this verb.");
            return false;
         }

         try {
            c = Class.forName(className);
         }
         catch (Exception e ) {
            Verb.basicAddError(this, "Failed to get class for class named " + Util.sq(className));
            setException(e);
            return false;
         }

         if (typeName.toLowerCase() == "null") {
            // do nothing it is already so defined
         }
         else {
            try {
               cc = Class.forName(typeName);
            }
            catch (Exception e ) {
               Verb.basicAddError(this, "Failed to get class for constructor named " + Util.sq(constructorTypeName));
               setException(e);
               return false;
            }
         }

         try {
            m = getMethod(c, cc, methodName);
            if (m == null) {
               Verb.basicAddError(this, "Failed to get method for class " + Util.sq(className) + ", method " + Util.sq(methodName));
               return false;
            }

            m.setAccessible(true);
            m.invoke(new DDTExternal(), testContext);

            if (testContext.containErrors()) {
               Verb.basicAddError(this, testContext.getString("errors"));
               return false;
            }

            String comments = testContext.getString("comments");
            if (isBlank(comments))
               comments = "Method " + Util.sq(methodName) + " in class " + Util.sq(className) + " invoked successfully and passed!";
            Verb.basicAddComment(this, comments);
            return true;
         }
         catch (NoSuchMethodException e) {
            Verb.basicAddError(this, "Failed to get method for class " + Util.sq(className) + ", method " + Util.sq(methodName) + " is an invalid method for the class.");
            setException(e);
            return false;
         }
      }
      
      /**
       * Gets a method from the DDTExternal class (for now, hard-coded!) with a parameter of class DDTTestContext (for now, hard coded)
       */
      private Method getMethod (Class cls, Class parameterType, String methodName) throws NoSuchMethodException {
         Method m = null;
         Method[] methods;

         if (isBlank(methodName))
            return m;


         try {
            // TODO - figure out how to avoid hard-coding here of both, class name and parameterType class.
            // As is, we basically ignore cls and parameterType parameters
            m = DDTExternal.class.getDeclaredMethod(methodName, new Class[] {DDTTestContext.class});
         }
         catch (NoSuchMethodException e) {
            setException(e);
            return m;
         }
         catch(Exception e) {
            setException(e);
         }
         return m;
      }

   }

   /**
    * Description
    * RunJS instances run JavaScript code in the browser's page
    * The instance's test context has the details of where the code is
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class RunJS extends Verb {

      public boolean isUIVerb() { return true;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static RunJS copy(Verb original) {
         RunJS copy = new RunJS();
         copy.setContext(original.getContext());
         copy.setElement(original.getElement());
         return copy;
      }

      /**
       * runJS is used mainly for handling the recursive nature of this product where RunJS needs to be used by other verbs
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent FindeElement for which to find element
       * @throws VerbException
       */
      public static void runJS(Verb verb) throws VerbException{
         RunJS copy = new RunJS();
         copy.setContext((verb.getContext()));
         copy.doIt();
      }

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (hasErrors())
            return;

         String fileName = getContext().getString("filename");
         String jsCode = "";
         if (!isBlank(fileName)) {
            jsCode = Util.readFile(fileName);
            if (isBlank(jsCode)) {
               Verb.basicAddError(this, "Invalid or empty Java Script file: " + Util.sq(fileName) + " - JavaScript not executed.");
               return;
            }
         }
         else {
            jsCode = getContext().getString("jscode");
            if (StringUtils.isBlank(jsCode)) {
               Verb.basicAddError(this, "Empty Java Script parameter! JavaScript not executed.");
               return;
            }
            // Replace the sequence of "\n" with end of statement
            jsCode = jsCode.replace("\\n",";");
         }

         JavascriptExecutor jsDriver = ((JavascriptExecutor) Driver.getDriver());
         try {
            if (!jsCode.startsWith("javascript:"))
               jsCode = "javascript:" + jsCode;
            jsDriver.executeScript(jsCode);
            Verb.basicAddComment(this, "Java Script code executed (" + Util.sq(jsCode) + ")");
         }
         catch (Exception e) {
            Verb.basicAddError(this, "Failed to execute Java Script: " + Util.sq(jsCode));
         }
      }
   }

   /**
    * Description
    * SaveElmentProperty saves the value of some property in a web element to the variables hashtable of the JavaDDT
    * The instance's test context has the details of the property to save and the key to the hashtable.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 02/23/17    |Bey      |Fixed bug in var setting...
    * ============|=========|====================================
    */

   public static class SaveElementProperty extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         try {
            // Find the element to verify based on the locator in the test context
            FindElement.findElement(this);
            if (hasErrors())
               return;

            // Get the name of the variable to save element property in the test context
            String varName = getContext().getString("SaveAs");
            if (isBlank(varName)) {
               Verb.basicAddError(this, "saveElementProperty - (required) 'SaveAs' element missing from the test context!");
               return;
            }
            if ((getElement() instanceof WebElement)) {

               // Interrogate the web element based on the data properties structure that contain any of:
               // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
               // Attribute - Used when the actual value is obtained by the getAttribute function
               // Property - Used when the function is getCssValue to get the value of the css property
               // The UIQuery object 'knows' to save the property if it is found...

               UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
               String actualValue = weq.query(getContext());

               if (hasErrors() || weq.hasErrors()) {
                  Verb.basicAddError(this, weq.getErrors() + " - Action Failed");
                  return;
               }
               
               DDTTestRunner.addVariable(varName, actualValue);

               Verb.basicAddComment(this, "Query results (" + Util.sq(actualValue) + ") Saved as variable named " + Util.sq(getContext().getString("SaveAs")));

            }  // WebElement and WebDriver exist
            else
               Verb.basicAddError(this, "Failed to find Web Element - Action failed!");
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
               Verb.basicAddError(this, "Web Element verification failed.");
         }

      }
   }

   /**
    * Description
    * SaveWebDriverProperty saves the value of some property in a web page to the variables hashtable of the JavaDDT
    * The test item instance's test context has the details of the property to save and the key to the hashtable.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 06/12/15    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class SaveWebDriverProperty extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         try {

            // Get the name of the variable to save element property in the test context
            String varName = getContext().getString("SaveAs");
            if (isBlank(varName)) {
               Verb.basicAddError(this, "Setup Problem: 'SaveAs' (required) property is missing from the test context!");
               return;
            }

            // Interrogate the web driver based on the data properties structure that contain any of:
            // Function - e.g. GetTTitle, GetCurrentURL, etc.
            // The UIQuery object 'knows' to save the property if it is found...

            UIQuery.WebDriverQuery weq = new UIQuery.WebDriverQuery();
            String actualValue = weq.query(getContext());

            if (hasErrors() || weq.hasErrors()) {
               Verb.basicAddError(this, weq.getErrors() + " - Action Failed");
               return;
            }

            Verb.basicAddComment(this, "Query results (" + Util.sq(actualValue) + ") Saved as variable named " + Util.sq(getContext().getString("SaveAs")));

         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
            Verb.basicAddError(this, "Web Driver verification failed.");
         }

      }
   }

   /**
    * Description
    * ScrollWebPage implements several types of scrolling - page and object into view
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/31/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class ScrollWebPage extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (hasErrors())
            return;

         try {

            String scrollType = getContext().getString("type");
            if (StringUtils.isBlank(scrollType) ) {
               Verb.basicAddError(this, "Setup Error: ScrollType is missing - Action failed.");
               return;
            }

            String x = getContext().getString("x");
            if (isBlank(x))
               x = "";

            String y = getContext().getString("y");
            if (isBlank(y))
               y = "";

            // Verify the scroll type definition is appropriate - X and Y components are present, are integer and are not 'max'
            if (!scrollType.toLowerCase().equals("scrollintoview")) {
               if (isBlank(x))
                  Verb.basicAddError(this, "X (pixels) parameter is mandatory for this scroll type but is missing - Action failed");
               if (isBlank(y))
                  Verb.basicAddError(this, "Y (pixels) parameter is mandatory for this scroll type but is missing - Action failed");
               if (hasErrors())
                  return;
            }

            // Create a Java Executor Driver from Driver.getDriver()
            JavascriptExecutor jsDriver = ((JavascriptExecutor) Driver.getDriver());

            switch (scrollType.toLowerCase()) {
               case "scrollby" : {
                  // Both x and y must be numeric.
                  try {
                     int intX = Integer.valueOf(x) + 0;
                     int intY = Integer.valueOf(y) + 0;
                  }
                  catch (Exception ex) {
                     Verb.basicAddError(this, "Setup Error: X parameter and Y parameters must be numeric but either or both are not! - Action failed");
                     return;
                  }

                  // Execute the scrollBy specified by the user
                  //jsDriver.executeScript("javascript:window.scrollBy(" + x + "," + y + ");");
                  getContext().setProperty("jscode", "window.scrollBy(" + x + "," + y + ")\\n");
                  RunJS.runJS(this);
                  Verb.basicAddComment(this, "Page Scrolled By X: "+ x + ", Y: " + y);
                  break;
               }
               case "scrollto" : {
                  // Consider user indicated scrolling to 'max' - either width or height.
                  // ("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));"

                  String xExpression = "";
                  if (x.equalsIgnoreCase("max"))
                     xExpression = "Math.max(document.documentElement.scrollWidth,document.body.scrollWidth,document.documentElement.clientWidth)";
                  else
                     xExpression = x;

                  String yExpression = "";
                  if (y.equalsIgnoreCase("max"))
                     yExpression = "Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight)";
                  else
                     yExpression = y;

                  // Execute the scrollTo specified by the user
                  //jsDriver.executeScript("window.scrollTo(" + xExpression + "," + yExpression + ");");
                  getContext().setProperty("jscode", "window.scrollTo(" + xExpression + "," + yExpression + ")\\n");
                  RunJS.runJS(this);
                  Verb.basicAddComment(this, "Paged Scrolled To: X: " + xExpression + ", Y: " + yExpression);

                  break;
               }
               case "scrollintoview" : {
                     FindElement.findElement(this);
                     if (hasErrors()) {
                        Verb.basicAddError(this, "Element to scroll to not found - Action failed");
                        return;
                     }

                     // Execute the scrollIntoView specified by the user to the found element
                     jsDriver.executeScript("arguments[0].scrollIntoView(true);", this.getElement());
                     RunJS.runJS(this);
                     Verb.basicAddComment(this, "Element Scrolled To View");
                     break;
                  }

               default: {
                  Verb.basicAddError(this, "Setup Error: Invalid Scroll Type: " + Util.sq(scrollType) + " encountered.  Valid types are: 'ScrollBy', 'ScrollTo' or 'ScrollIntoView' - Action Failed");
                  return;
               }
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * SelectOption is used to select a single item from a list of <option> code : description </option>Emulates objects
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class SelectOption extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (hasErrors())
            return;

         // User can specify either ItemValue or ItemText - ItemText has priority
         String textToSelectBy = getContext().getString("itemtext");
         if (isBlank(textToSelectBy))
            textToSelectBy = "";

         String valueToSelectBy = getContext().getString("itemvalue");
         if (isBlank(valueToSelectBy))
            valueToSelectBy = "";
         // If both, textToSelectBy and valueToSelectBy are present, use textToSelectBy else, use valueToSelectBy if it is not blank.
         // All these shenanigans are meant to support selection of a blank value when needed.
         String selectionValue = (isBlank(valueToSelectBy)) ? textToSelectBy : (isBlank(textToSelectBy) ? valueToSelectBy :textToSelectBy);

         try {
            // Successful find of element results in storage of found option in testContext
            FindOption.findOption(this);

            if (hasErrors())
               return;

            if ((getElement() instanceof WebElement)) {
               if (getElement().isEnabled())  {
                  getElement().click();
                  Verb.basicAddComment(this, "Option Selected: " + Util.sq(selectionValue));
               }
               else
                  Verb.basicAddError(this, "Option not enabled - Action Failed!");
            }
            else
               Verb.basicAddError(this, "Failed to find Web Element - Action Failed!");
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }

      }

   }

   /**
    * Description
    * SetPageSize is used to resize or maximize a web page
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class SetPageSize extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         WebDriver driver = Driver.getDriver();

         try {
            String option = getContext().getString("value");
            if (isBlank(option))
               option = "";
            if (option.toLowerCase().equals("maximize")) {
               driver.manage().window().maximize();
               Verb.basicAddComment(this, "Page Maximized");
            }
            else {
               // Use a dimension object with coordinates in the instance's test context
               int xPixels = getContext().getStringAsInteger("x");
               int yPixels = getContext().getStringAsInteger("y");
               if (xPixels > 0 && yPixels > 0) {
                  Dimension d = new Dimension(xPixels, yPixels);
                  driver.manage().window().setSize(d);
                  Verb.basicAddComment(this, "Page re-sized to " + d.toString());
               }
               else
                  Verb.basicAddError(this, "Both 'x' and 'y' (numeric & positive) coordinates must be provided as page size - Action Failed");
            }
         }
         catch(Exception e){
            // Do not overwrite previous exceptions!
            if (!hasException())
               this.setException(e);
         }
      }
   }

      /**
    * Description
    * SetVars instances set variables in the framework's hashtable
    * The instance's DDTTestContext contains the necessary information
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/29/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class SetVars extends Verb {

         public boolean isUIVerb() { return false;}

         public void doIt() throws VerbException{

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            String ignoreMe = Util.populateDictionaryFromHashtable(getContext(), DDTTestRunner.getVarsMap());
            //Verb.basicAddComment(this, ignoreMe);
            Verb.basicAddComment(this, "Variables Set (details omitted)");
         }
         catch (Exception e) {
            Verb.basicAddError(this, "Variable(s) not set. (" + e.getMessage().toString() + ")");
            setException(e);
         }
      }
   }

   /**
    * Description
    * SwitchTo instances switch to the specified frame or window off the current web driver window
    * The instance's DDTTestContext contains the necessary information
    * Type        Designates the type of item to switch to (Frame or Window) - Window handles forms or other targets
    * ItemNo      Designates the (1 based) window / frame number (if known)
    * Value       A string indicating the name of the window or frame.
    * If Type is provided, default to a Frame and the logic becomes: find the first one (if any) as though ItemNo=1 was used.
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 06/10/15    |Bey      |Renamed method and generalized it to frame or window
    * ============|=========|====================================
    */

   public static class SwitchTo extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         if (!Driver.isInitialized()) {
            Verb.basicAddError(this, "Web Driver not initialized.  Action Failed");
            return;
         }

         WebDriver driver = Driver.get();

         String itemType = getContext().getString("type").toLowerCase();
         if (isBlank(itemType))
            itemType = "frame";

         // If exists, itemName is used to qualify one of many
         String itemName = getContext().getString("value");
         if (isBlank(itemName))
            itemName = "";
         if (itemType == "frame")
            switchFrames(driver, itemName);
         else
            switchWindows(driver, itemName);
      }

      /**
       * Try to find a frame by its name, number or both
       * @param driver
       * @param name
       * @param item
       */
      private void switchFrames(WebDriver driver, String name) {
         // Try to switch to the named frame - only if item is 0

         if (isNotBlank(name)) {
            try {
               driver.switchTo().frame(name);
               Verb.basicAddComment(this, "Switched to frame: " + Util.sq(name));
            } catch (Exception e) {
               // Do not overwrite previous exceptions!
               Verb.basicAddError(this, "Error encountered trying to switch to frame named: " + Util.sq(name));
               if (!hasException())
                  setException(e);
            }
         } else {
            try {
               // Try locating frame by its name
               WebElement page = driver.findElement(By.xpath("/html"));
               List<WebElement> elements = page.findElements(By.tagName("iframe"));
               int nTried = 0;
               if (elements.size() > 0) {
                  int index = 1;
                  String tmpName = name.toLowerCase();
                  for (WebElement element : elements) {
                     String itemName = element.getText();
                     String checkName = itemName.toLowerCase();
                     if (isNotBlank(checkName) && isNotBlank(tmpName) && checkName.contains(tmpName)) {
                        try {
                           driver.switchTo().frame(itemName);
                           break;
                        } catch (Exception e) {
                           Verb.basicAddError(this, "Failed to switch to frame named: " + Util.sq(name));
                           setException(e);
                        }
                     }
                     nTried += 1;
                  }
                  // If we made it here - no frames were found but some existed...
                  Verb.basicAddError(this, "Failed to switch to frame named: " + Util.sq(name) + " (tried " + nTried + ").");

               } else {
                  Verb.basicAddError(this, "Current page has no frame items!");
               }
            } catch (Exception e) {
               Verb.basicAddError(this, "Failed to find any target items off of current page.");
            }
         }
      }

      /**
       * Try to find a window by its name or number
       * @param driver
       * @param name
       */
      private void switchWindows(WebDriver driver, String name) {
         String parentWindow = driver.getWindowHandle();
         Set<String> handles = driver.getWindowHandles();
         int nTried = 0;

         for (String windowHandle : handles) {
            if (!windowHandle.equals(parentWindow)) {
               try {
                  driver.switchTo().window(windowHandle);
                  String title = driver.getTitle().toLowerCase();
                  boolean verified = (isBlank(title) || isBlank(name) || title.toLowerCase().contains(name));
                  if ( verified ) {
                     break;
                  }
                  nTried += 1;
               }
               catch (Exception e) {
                  Verb.basicAddError(this, "Exception while switching Windows!");
                  setException(e);
               }
            }
         }

         if (nTried > 0) {
            Verb.basicAddError(this, "Failed to switch to child window with title containing: " + Util.sq(name));
         }
      }

   }

   /**
    * Description
    * TakeScreenShot is used to get an image of the displayed page and save it to the Images folder of the project
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class TakeScreenShot extends Verb {

      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         WebDriver driver = Driver.getDriver();

         try {
            String stepId = getContext().getString("StepId");
            if (isBlank(stepId))
               stepId = myName();
            String result = Util.takeScreenImage(driver, "", stepId);
            File tmp = new File(DDTSettings.asValidOSPath(result, true));
            if (tmp.length() > 1L) {
               Verb.basicAddComment(this, "Screen shot image stored at: " + Util.sq(result));
               getContext().setProperty("ScreenShotFileName", result);
            }
            else {
               Verb.basicAddError(this, result + " - Action Failed");
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * Toggle changes the state of a web element to the opposite state
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class Toggle extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         WebDriver driver = Driver.getDriver();

         try {
            FindElement.findElement(this);
            if (hasErrors())
               return;

            if ((getElement() instanceof WebElement)) {
               if (getElement().isEnabled()) {
                  String toggleSpecs = getContext().getString("value");

                  boolean shouldBeToggled = isBlank(toggleSpecs) ? true : Util.asBoolean(toggleSpecs);
                  // The user should have indicated  attribute of 'checked' or 'selected' or whatever is appropriate for the element - this is a name of a property
                  UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
                  String actualValue = weq.query(getContext());
                  if (weq.hasErrors()) {
                     Verb.basicAddError(this, weq.getErrors() + " - Action Failed");
                     return;
                  }

                  if (isBlank(actualValue))
                     actualValue="false";

                  boolean elementSelectedOrOn =  Util.asBoolean(actualValue.toLowerCase());
                  boolean shouldClick = (elementSelectedOrOn != shouldBeToggled);
                  if (shouldClick) {
                     new Actions(driver).moveToElement(getElement()).perform();
                     getElement().click();
                     Verb.basicAddComment(this, (elementSelectedOrOn ? "'Toggled' element 'Un-Toggled" : "'Un-Toggled' element 'Toggled'"));
                  }
                  else {
                     Verb.basicAddComment(this, (elementSelectedOrOn ? "Toggled": "Un-Toggled") + " element already at desired state - Not Toggled");
                  }
               }
               else
                  Verb.basicAddComment(this,"Element not enabled - Action Failed");
            }
            else Verb.basicAddComment(this,"Failed to find Web Element - Action Failed");

         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * TypeKeys Emulates data typing into some control
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 09/18/16    |Bey      |Added Encryption / Decryption logic
    * 01/23/17    |Bey      |Added multiple tabs option 'ntabs"
    * ============|=========|====================================
    */

   public static class TypeKeys extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         String keys = getContext().getString("value");  // The value to enter
         String origKeys = keys;
         boolean append = getContext().getStringAsBoolean("append"); // Should data be appended? (default is no)
         boolean encrypt = getContext().getStringAsBoolean("encrypt"); // Should data be encrypted? (default is no)
         boolean decrypt = getContext().getStringAsBoolean("decrypt");
         boolean tabOutSpecified = !isBlank(getContext().getString("tabout"));
         boolean enter = getContext().getStringAsBoolean("enter"); // Should {newLine} be appended? (default is no)

         boolean clipboardSpecified = isNotBlank(getContext().getString("clipboard"));
         String clipboardAction = clipboardSpecified ? getContext().getString("clipboard") : "";

         if (clipboardSpecified) {
            switch (clipboardAction.toLowerCase()) {
               case "copy" : KeyboardEmulator.copyToClipboard(); break;
               case "paste" : KeyboardEmulator.pasteFromClipboard();  break;
               default: {
                  Verb.basicAddComment(this, "Faulty setup: Invalid clipboard choice: " + Util.sq(clipboardAction));
               }
            }
            return;
         }

         boolean tabOut;
         int nTabs = 0;
         if (!tabOutSpecified) {
            tabOut = DDTSettings.Settings().getTabOut();
            nTabs = tabOut ? 1 : 0;
         } else {
            tabOut = getContext().getStringAsBoolean("tabout");
            if (tabOut) {
               // See if user wants to tab out more than once...
               int nTabsSpecified = getContext().getInt("nTabs");
            }
         }

         try {
            if (encrypt)
               keys = Util.encrypt(keys);

            if (decrypt)
               keys = Util.decrypt(keys);

            if (tabOut) {
               for (int i=0; i<nTabs;i++) {
                  keys += "\t";
               }
            }

            if (enter)
               keys += "\n";

            if (isNotBlank(getContext().getString("LocSpecs"))) {
               // Recursive Find - notice the case of this call findElement is a static call!
               FindElement.findElement(this);
               if (hasErrors())
                  return;
               if ((getElement() instanceof WebElement))
                  if (getElement().isEnabled()) {
                     if (!append)
                        getElement().clear();
                     getElement().sendKeys(keys);
                     Verb.basicAddComment(this, "Keys: " + Util.sq(origKeys) + " " + (nTabs < 1 ? "" : "(" + nTabs + " tabs)" ));
                  }
                  else Verb.basicAddError(this, "Element not enabled - Action failed");
               else Verb.basicAddError(this, "Element not found - Action failed");
            }
            else
            {
               // No element specified - assume the cursor is at some input field and type keys
               String typedKeys = KeyboardEmulator.type(keys, enter);
               if (keys.equals(typedKeys)) {
                  Verb.basicAddComment(this, "Typed: " + Util.sq(typedKeys) + " " + (nTabs < 1 ? "" : "(" + nTabs + " tabs)" ));
               }
               else
                  Verb.basicAddError(this, "Typed: " + Util.sq(typedKeys) + " instead of " + Util.sq(keys));
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * Verify implements verification outside of the context of a Web driver and UI
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * 09/18/16    |Bey      |Add encryption option
    * ============|=========|====================================
    */

   public static class Verify extends Verb {

      public boolean isUIVerb() { return false;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            Verifier verifier = Verifier.getVerifier(getContext());
            String actualValue = getContext().getString("actualvalue");
            boolean encrypt = getContext().getStringAsBoolean("encrypt"); // Should data be encrypted? (default is no)
            boolean decrypt = getContext().getStringAsBoolean("decrypt"); // Should data be decrypted? (default is no)

            if (encrypt)
               actualValue = Util.encrypt(actualValue);

            if (decrypt)
               actualValue = Util.decrypt(actualValue);

            verifier.setAv(actualValue);
            verifier.verify();
            if (verifier.isPass())
               Verb.basicAddComment(this, verifier.getComments());
            else
               Verb.basicAddError(this, verifier.getErrors());
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * VerifyElementSize verifies the size of a control that has a collection of elements
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class VerifyElementSize extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, true);
         if (this.hasErrors())
            return;

         try {

            // If needed, find the element to verify based on the locator in testContext
            // The element may have already been found or function relates to Driver...
            if (!(getElement() instanceof WebElement))
               FindElement.findElement(this);

            // Bail out on failure to find element
            if (hasErrors()) {
               Verb.basicAddError(this, getErrors() + " - Action Failed");
               return;
            }

            // Get a template verifier from the testContext object
            Verifier verifier = Verifier.getVerifier(getContext());

            if ((getElement() instanceof WebElement)) {

               // Interrogate the web element based on the data properties structure that contain any of:
               // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
               // Attribute - Used when the actual value is obtained by the getAttribute function
               // Property - Used when the function is getCssValue to get the value of the css property

               UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
               String actualValue = weq.query(getContext());
               if (hasErrors()) {
                  Verb.basicAddError(this, weq.getErrors() + " - Action Failed");
                  return;
               }

               if (StringUtils.isBlank(actualValue))
                  actualValue = "";

               // Represent the actual value as a numeric string that is the length of the actualValue returned above.
               // The int data type is a 32-bit signed two's complement integer. It has a minimum value of -2,147,483,648 and a maximum value of 2,147,483,647 (inclusive)
               // With the above definition, we feel safe with the following statement as opposed to trying both, (Long and int).valueOf()...
               actualValue = String.valueOf(actualValue.length());

               // Get the comparison mode for verification.
               String compareMode = getContext().getString("comparemode");
               if (isBlank(compareMode))
                  compareMode = "equals";

               verifier.setComp(compareMode);

               // Commute the class of the length we are trying to verify to either int or long
               String cls = getContext().getString("class");
               if (StringUtils.isBlank(cls))
                  cls = "int";
               else
                  cls = (cls.toLowerCase() == "long") ? cls : "int";

               // To verify size, a numeric class is needed - set up the one
               verifier.setAv(actualValue);
               verifier.setCls(cls);

               // Finally, verify that string's length
               verifier.verify();
               if (verifier.isPass())
                  Verb.basicAddComment(this, verifier.getComments());
               else
                  Verb.basicAddError(this, verifier.getErrors() + " - Action Failed");
            }
            else
               Verb.basicAddError(this, "Failed to find Web Element - Action Failed");
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }
      }
   }

   /**
    * Description
    * VerifyOption implements verification of an option in a drop-down or similar control
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * 08/21/15    |Bey      |Bug fixed - preserve option that was found in Context
    * ============|=========|====================================
    */

   public static class VerifyOption extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         try {
            FindOption.findOption(this);
            if (hasErrors())
               return;
            VerifyWebElement.verifyWebElement(this);
         }
         catch (Exception e) {
            setException(e);
         }
         return;

      }
   }

   /**
    * Description
    * VerifyWeb Element implements verification of a web element on a web page
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/03/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class VerifyWebElement extends Verb {

      public boolean isUIVerb() { return true;}

      /**
       * copy is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param original  - the original FindElement verb
       * @return
       */
      public static VerifyWebElement copy(Verb original) {
         VerifyWebElement copy = new VerifyWebElement();
         copy.setContext(original.getContext());
         copy.setElement(original.getElement());
         return copy;
      }

      /**
       * findElement is used mainly for handling the recursive nature of this product where finding elements often happens within parent elements.
       * In such cases a clone (provided by this method) is used to handle the recursion
       * @param verb the parent FindeElement for which to find element
       * @throws VerbException
       */
      public static void verifyWebElement(Verb verb) throws VerbException{
         VerifyWebElement verifier = VerifyWebElement.copy(verb);
         verifier.doIt();
         verb.setElement(verifier.getElement());
         if (verifier.hasErrors())
            Verb.basicAddError(verb, verifier.getErrors());
         else
            Verb.basicAddComment(verb, verifier.getComments());

      }


      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         try {
            // If requested, find the element to verify based on the locator in testContext
            // The element may have already been found or function relates to Driver...
            if (!(getElement() instanceof WebElement))
               FindElement.findElement(this);
            if (hasErrors())
               return;

            // Get a template verifier from the testContext
            Verifier verifier = Verifier.getVerifier(getContext());


            // Interrogate the web element based on the data properties structure that contain any of:
            // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
            // Attribute - Used when the actual value is obtained by the getAttribute function
            // Property - Used when the function is getCssValue to get the value of the css property

            UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
            String actualValue = weq.query(getContext());
            if (hasErrors())
               return;

            verifier.setAv(actualValue);
            verifier.verify();
            if (verifier.isPass())
               Verb.basicAddComment(this, verifier.getComments());
            else
               Verb.basicAddError(this, verifier.getErrors());
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }

      }
   }

   /**
    * Description
    * VerifyWebDriver implements verification of a Web Driver's page
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 11/06/14    |Bey      |Initial Version
    * ============|=========|====================================
    */

   public static class VerifyWebDriver extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, this.isUIVerb());
         if (this.hasErrors())
            return;

         try {

            EnsurePageLoaded.ensurePageLoaded(this);
            if (hasErrors())
               return;
            Verifier verifier = Verifier.getVerifier(getContext());

            // Interrogate the web driver based on the data properties structure that contain any of:
            // FunctionName - e.g. GetTitle, GetCurrentUrl, etc.
            // AttributeName - Used when the actual value is obtained by the getAttribute function
            // PropertyName - Used when the function is getCssValue to get the value of the css property

            UIQuery.WebDriverQuery wdq = new UIQuery.WebDriverQuery();
            wdq.setUpFrom(getContext());
            String actualValue = wdq.query(getContext());
            if (hasErrors()) return;

            verifier.setAv(actualValue);
            verifier.verify();
            if (verifier.isPass())
               Verb.basicAddComment(this, verifier.getComments());
            else
               Verb.basicAddError(this, verifier.getErrors());
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!hasException())
               setException(e);
         }

      }
   }

   /**
     * Description
     * Wait implements process wait for the specified number of seconds
     * History
     * When        |Who      |What
     * ============|=========|====================================
     * 11/03/14    |Bey      |Initial Version
     * ============|=========|====================================
     */

   public static class Wait extends Verb {

      public boolean isUIVerb() { return true;}

      public void doIt() throws VerbException {

         debug(this);

         basicValidation(this, false);
         if (this.hasErrors())
            return;

         try {
            Long timeInSeconds = getContext().getStringAsLong("WaitTime");
            if (timeInSeconds > 0)
            {
               try {
                  Thread.sleep((timeInSeconds * 1000));
                  Verb.basicAddComment(this, "Waited for " + timeInSeconds + " seconds");
               }
               catch (Exception e) {
                  setException(e);
               }
            }
            else {
               Verb.basicAddComment(this, "Wait action specified without time period - wait not performed");
            }
         }
         catch (Exception e) {
            setException(e);
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 10/29/14
    * Time: 10:04 PM
    * Selenium Based Automation Project
    * Description
    * Class for handling verb exceptions generically.
    * Methods in the Verb class should throw an instance of this class with the DDTTestContext instance and an extra blurb if any.
    *
    * History
    * Date        Who      |What
    * =========== |======= |====================================
    * 10/29/14    |Bey     |Initial Version
    * =========== |======= |====================================
    */
   public static class VerbException extends Exception {
      /**
       * Handles errors thrown by the various verbs in the vocabulary
       *
       * @param Verb
       * @param blurb    - Each verb may or may not have their own blurbs...
       */
      public VerbException(Verb verb, String blurb) {
         String prefix = "Action " + Util.sq(verb.myName()) + " generated exception.";
         String error = "";
         verb.setException(this);
         if (!verb.getErrors().startsWith(prefix))
            error = prefix;

         if (getCause() != null) {
            error = error + "  " + Util.sq(getCause().toString());
         }
         if (org.apache.commons.lang.StringUtils.isNotBlank(blurb)) {
            error = error + "  " + blurb;
         }
         verb.addError(error);
      }
   }
}

