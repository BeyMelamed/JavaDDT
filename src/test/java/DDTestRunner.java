import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by BeyMelamed on 2/13/14.
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
 * <p>
 *    The driver that interprets the input source, activates, interrogates and validates the AUT
 *    The processTestItems method is the main loop 'where the rubber meets the road'
 *    Note various done, fail, pass, skip counters - there are three sets of those:
 *    Instance level - these represent the count for a given test runner instance (a collection of steps at a given level in the hierarchy)
 *    Class level -    there represent the counters for an entire test session
 *    These come in two flavors (as the user may have set some non-verification steps as 'do not report me'
 *    rpt...      - This is the reportable items count
 *    non rpt...  - This flavor counts all steps, reported and not reported steps count
 * <p/>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 01/13/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class DDTestRunner {

   public enum TestEventType {INIT, INFO, PASS, FAIL, SKIP}

   // ======================================= Class properties section =====================================
   private static WebDriver driver;
   private static DDTReporter reporter;
   private static Long nSessionSteps = 0L;
   private static Long nReportedSessionSteps = 0L;
   private static boolean shouldQuitTestSession;
   private static Hashtable<String, Object> varsMap;

   // The test session variables hashtable is maintained by DDTestRunner instance.
   private static Hashtable<String, TestItem> currentTestItem = new Hashtable<String, TestItem>();

   // done, pass, fail, skip counters - on the Test Session level - reset when a new TestRunner session is launched
   private static int[] tsCounters = {0, 0, 0, 0};

   // done, pass, fail, skip counters - on the TestRunner instance level - reset when a new TestRunner instance is created
   private static int[] trCounters = {0, 0, 0, 0};

   // done, pass, fail, skip counters - on the Reporter instance level - reset when session is initiated and after a Reporter generates a report
   private static int[] rptCounters = {0, 0, 0, 0};

   // Most Recent Web element to be propagated across testItem instances for steps requiring previously found 'anchor' elements.
   // Static as it may be set by one instance and accessed by another instance of TestRunner
   private static WebElement mrElement;

   private static String sessionFailBlurb;
   private static String sessionPassBlurb;

   private static int nDone() {
      return trCounters[0];
   }

   private static int nPass() {
      return trCounters[1];
   }

   private static int nFail() {
      return trCounters[2];
   }

   private static int nSkip() {
      return trCounters[3];
   }

   private static int nRptDone() {
      return rptCounters[0];
   }

   private static int nRptPass() {
      return rptCounters[1];
   }

   private static int nRptFail() {
      return rptCounters[2];
   }

   private static int nRptSkip() {
      return rptCounters[3];
   }

   private static void setNextStep() {
      nSessionSteps++;
   }

   private static void setNextReportingStep() {
      nReportedSessionSteps++;
   }

   public static void setDriver(WebDriver aDriver) {
      driver = aDriver;
   }

   public static void setSessionFailBlurb(String value) {
      sessionFailBlurb = value;
   }

   public static String getSessionFailBlurb() {
      if (isBlank(sessionFailBlurb))
      setSessionFailBlurb("");
      return sessionFailBlurb;
   }

   public static void setSessionPassBlurb(String value) {
      sessionPassBlurb = value;
   }

   public static String getSessionPassBlurb() {
      if (isBlank(sessionPassBlurb))
         setSessionPassBlurb("");
      return sessionPassBlurb;
   }

   // ======================================= Class logic section =====================================

   public static void endSession() {
      if (isNotBlank(getSessionFailBlurb()))
         runFailed(getSessionFailBlurb());
      testEnded(getSessionPassBlurb());
   }

   private static void runFailed(String s) {
      System.out.println(s);
      System.exit(-1);
   }

   private static void testEnded(String s) {
      System.out.println(s);
      System.exit(0);
   }

   /**
    *
    * @param args [0] = provider type, [1] provider source, [2] test steps container (optional)
    */
   public static void runOn(String[] args) {
      // Populate the instance's dictionary with current date variables
      addVariable("datevars", "%date%");
      try {
         // Get a test items provider with test items based on these specified in args
         TestItemsProvider provider = TestItemsProvider.providerWithItems(args);
         if (provider instanceof TestItemsProvider) {
            TestItem.TestItems testItems = new TestItem.TestItems();
            testItems.setItems(provider.getTestItems());
            DDTestRunner runner = new DDTestRunner();
            runner.setTestItems(testItems);
            runner.processTestItems();
         } else
            System.out.println("Failed to start with default TestItemsProvider");
      }
      catch (Exception e) {
         System.out.println("Failed to start with default TestItemsProvider " + e.getMessage().toString());
      }
   }

   public static DDTestRunner invokeDefaults() {
      DDTSettings.reset();
      DDTestRunner inst = new DDTestRunner();

      // Use the default test items provider
      try {
         // Populate the instance's dictionary with current date variables
         addVariable("datevars", "%date%");
         // Get the initial test items specified in the properties file or the command line
         String tmp[] = DDTSettings.Settings().inputSpecsArray();
         runOn(tmp);
         generateReportIfNeeded();
      }
      catch (Exception ex) {
         System.out.println("Exception encountered running defaults: " + ex.getMessage().toString()) ;
      }
      finally {
         if (getDriver() instanceof WebDriver) {
            System.out.println("End of Test Session - Closing Driver");
            getDriver().close();
         }
         return inst;
      }
   }

   /**
    * This is called from the generateReport vocabulary entry
    * @param description    - Optional descripton of the report
    * @param emailBody      - Optional additional text the user set on this step to be displayed in the body of the email message to the recipients
    * @throws IOException
    * @throws TransformerException
    */
   public static void generateReport(String description, String emailBody) throws IOException, TransformerException {
      getReporter().resetCounters(nRptDone(), nRptPass(), nRptFail(), nRptSkip());
      getReporter().generateReport(description, emailBody);
   }

   /**
    *
    * @param testItem  - The current item at the current level
    * @param addOrRemove - Indicator whether the item should be added to the hashtable (do for each item at a given level) or removed from it (end of test case)
    */
   public static void maintainCurrentTestItem(TestItem testItem, String addOrRemove) {
      String key = testItem.getLevelKey();

      TestItem tmp = currentTestItem.get(key);
      if (tmp instanceof TestItem) {
         // Upon starting a new level, make sure the current testItem is reflected at that level - Need to remove the current item
         // Upon ending a level - Need to remove the current item
         currentTestItem.remove(key);
      }
      if (addOrRemove.equalsIgnoreCase("add")) {
         currentTestItem.put(key, testItem);
      }
   }

   public static WebDriver getDriver() {
      if (driver instanceof WebDriver) {
         if (driver.getWindowHandles().isEmpty())
            setDriver(null);
      }
      return driver;
   }

   public static void resetRptCounters() {
      rptCounters = new int[] {0, 0, 0, 0};
   }

   public static Hashtable<String, Object> getVarsMap() {
      if (varsMap == null)
         setVarsMap(new Hashtable<String, Object>());
      return varsMap;
   }

   public static void setVarsMap(Hashtable<String, Object> aMap) {
      varsMap = aMap;
   }

   public static void addVariable(String key, String value) {
      String result = "";
      try {
         if ((getVarsMap().get(key) != null))
            getVarsMap().remove(key);

         // Consider the special case of date variables used for resetting date-specific system variables
         if (value.toString().toLowerCase().startsWith("%date") &&(value.toString().toLowerCase().endsWith("%"))) {
            try {
               DDTDate referenceDate = new DDTDate();
               referenceDate.resetDateProperties(value.toString(), getVarsMap());
               if (referenceDate.hasException())
                  result = referenceDate.getException().getMessage().toString();
               else
                  result = referenceDate.getComments();
            }
            catch (Exception e) {
               result = "Error encountered in setting date variables: " + e.getCause().toString();
            }
         }
         else {
            getVarsMap().put(key.toLowerCase(), value);
            result = "Variable " + Util.sq(key.toLowerCase()) + " (re)set in the variables map to " +
                  Util.sq((value instanceof String) ? value : value.toString());
         }

      }
      catch (Exception e) {
         result = e.getCause().toString();
      }
      finally {
         System.out.println(result);
      }
   }

   //public static int nSessionPass, nSessionFail, nSessionDone, nSessionSkip;
   public static int nSessionDone() {
      return tsCounters[0];
   }

   public static int nSessionPass() {
      return tsCounters[1];
   }

   public static int nSessionFail() {
      return tsCounters[2];
   }

   public static int nSessionSkip() {
      return tsCounters[3];
   }

   public static void setNextReportingStep(String actionName) {
      if (isReportableAction(actionName))
         setNextReportingStep();
      setNextStep();
   }

   public static Long currentSessionStep() {
      return nSessionSteps;
   }

   public static Long currentReportedSessionStep() {
      return nReportedSessionSteps;
   }

   public static boolean isReportableAction(String actionName) {
      return DDTSettings.isReportableAction(actionName);
   }

   public static DDTReporter getReporter() {
      if (reporter == null) {
         // DDTReporter.DDTXmlReporter reporter = new DDTReporter.DDTXmlReporter();
         reporter = new DDTReporter();
      }
      return reporter;
   }

   public static boolean shouldGenerateReport() {
      return getReporter().shouldGenerateReport();
   }

   /**
    * Create an array of various elements describing the context in which the test session ran
    * @return
    */
   public static String[][] getEnvironmentItems() {
      String[][] result = new String[5][2];
      StringBuilder sb = new StringBuilder("Environment: ");
      result[0][0] = "os";
      result[0][1] = "OS Name: " + System.getProperty("os.name") + ", " +
            "OS Version: " + System.getProperty("os.version") + ", Browser: " + DDTSettings.Settings().browserName();

      result[1][0] = "env";
      result[1][1] = "Country: " + System.getProperty("user.country") +
            ", Language: " + System.getProperty("user.language") +
            ", Time Zone: " + System.getProperty("user.timezone");

      result[2][0] = "java";
      result[2][1] = "Version: " + System.getProperty("java.version") +
            ", Home: " + System.getProperty("java.home");

      result[3][0] = "user";
      result[3][1] = "Name: " + System.getProperty("user.name") +
            ", Home: " + System.getProperty("user.home") +
            ", Project Home: " + System.getProperty("user.dir");
      // The last element is left blank on purpose - to be used by DDTReported or other modules
      return result;
   }

   /**
    * JSON represetation of the environment
    * @return
    */
   public static String getEnvironmentItemsAsJSON() {
      String[][] items = getEnvironmentItems();
      StringBuilder sb = new StringBuilder("");
      for (int i = 0; i < 4; i++) {
         sb.append(Util.dq(items[i][0]) + ":" + (Util.dq(Util.jsonify(items[i][1]))) + (i < 3 ? "," : ""));
      }
      return Util.dq("environment") + ":{" + sb.toString() + "}";
   }

   public static String getSessionStatsAsJSON() {

      StringBuilder sb = new StringBuilder("");
      sb.append(Util.dq("passedSteps") + ":" + String.valueOf(nPass()) + ",");
      sb.append(Util.dq("failedSteps") + ":" + String.valueOf(nFail()) + ",");
      sb.append(Util.dq("skippedSteps") + ":" + String.valueOf(nSkip()) + ",");
      sb.append(Util.dq("totalSteps") + ":" + String.valueOf(nDone()) + ",");
      sb.append(Util.dq("sessionDuration") + ":" + Util.dq(getReporter().sessionDurationString()));
      return Util.dq("stats") + ":{" + sb.toString() + "}";
   }

   public static String getSessionSummaryAsJSON() {
      return "{" + getEnvironmentItemsAsJSON() + "," + getSessionStatsAsJSON() + "}";
   }

   public static void reportSessionSummaryAsJSON() {
      String fileName = getReporter().sessionTestsFolderName() + DDTSettings.Settings().fileSep() + "sessionSummary.json";
      Util.fileWrite(fileName, getSessionSummaryAsJSON());
   }

   /**
    * Generate the report only if it was not generated yet and there are some steps to report on.
    */
   public static void generateReportIfNeeded() {
      if (shouldGenerateReport())
         try {
            getReporter().generateReport("Final Report", "");
         } catch (Exception e) {
            e.printStackTrace();
         }
   }

   // ======================================= Instance properties =====================================

   private int level = 0;
   private Long parentStepNumber; // For all but level 0 items this is > 0;
   private String errors = "";

   private String inputSpecs;
   private TestItem.TestItems testItems;

   public void setParentStepNumber(Long value) {
      parentStepNumber = value;
   }

   public Long getParentStepNumber() {
      if (parentStepNumber == null)
         setParentStepNumber(0L);
      return parentStepNumber;
   }

   public DDTestRunner () {
      resetTrCounters();
   }

   public void setInputSpecs(String value) {
      inputSpecs = value;
   }

   public String getInputSpecs() {
      if (isBlank(inputSpecs)) {
         setInputSpecs(DDTSettings.Settings().inputSpecs());
      }
      return inputSpecs;
   }

   public int nItems() {
      return testItems.getSize();
   }

   private void resetTrCounters() {
      trCounters = new int[] {0, 0, 0, 0};
   }

   private void incrementDone() {
      tsCounters[0]++;
      trCounters[0]++;
      rptCounters[0]++;
   }

   private void incrementPass() {
      tsCounters[1]++;
      trCounters[1]++;
      rptCounters[1]++;
   }

   private void incrementFail() {
      tsCounters[2]++;
      trCounters[2]++;
      rptCounters[2]++;
   }

   private void incrementSkip() {
      tsCounters[3]++;
      trCounters[3]++;
      rptCounters[3]++;
   }

   private void setError(String value) {
      errors = value;
   }

   public void setLevel(int aNumber) {
      level = aNumber;
   }

   public int getLevel() {
      return level;
   }

   public void setTestItems(TestItem.TestItems items) {
      testItems = items;
   }

   public boolean failed() {
      return (nFail() > 0 || (!isBlank(errors)));
   }

   public boolean passed() {
      return !failed();
   }

   public String getErrors() {
      return errors;
   }

   /**
    * Executes each of the active TestItem instances in the current TestRunner instance.
    */
   public void processTestItems() throws InvocationTargetException, IllegalAccessException {
      boolean shouldQuitTestCase = false;
      String pad = "";
      String quitBlurb = "";

      if (testItems != null && nItems() > 0) {
         for (TestItem testItem : testItems.getItems())
         {
            // Skip empty rows in the data source
            if ((testItem == null) || testItem.isEmpty())
               continue;

            // Allow for developer's debugging
            if (testItem.shouldDebug())
            {
               // Place a breakpoint on the next statement...
               boolean isDebugging = true;
            }

            // Perform some initialization tasks...
            testItem.setLevel(getLevel());
            testItem.setParentStepNumber(getParentStepNumber());
            testItem.setParentTestItem(testItems.getParentItem());

            testItem.initialize();

            // Maintain the current test item table
            maintainCurrentTestItem(testItem, "add");

            if (mrElement instanceof WebElement)
               testItem.setMrElement(mrElement);

            // Add this step to the reporter and maintain reporter's first and last reporting and session numbers for this reporting session
            if (getReporter().firstSessionStep() < 1L)
               getReporter().setFirstSessionStep(currentSessionStep());  // Done only once
            getReporter().setLastSessionStep(currentSessionStep());      // Keeps incrementing

            if (isReportableAction(testItem.getAction())) {
               getReporter().addDDTest(testItem);
               if (getReporter().firstReportStep() < 1L)
                  getReporter().setFirstReportStep(currentReportedSessionStep());  // Done only once - reported steps
               getReporter().setLastReportStep(currentReportedSessionStep());      // Keeps incrementing - reported steps
            }

            // Process active or inactive steps as needed
            if (testItem.isActive()) {
               try {
                  Vocabulary.invoke(testItem);
               }
               catch (Exception e) {
                  if (!testItem.hasException())
                     testItem.setException(e);
               }
               finally {
                  incrementDone();
               }

               // If settings indicates pausing after a UI step, do it
               if (!isBlank(testItem.getLocSpecs()))
                  pause();

               if (isBlank(pad))
                  pad = testItem.pad();

               if (testItem.isFailure()) {
                  incrementFail();
                  testItem.addFailEvent("Step Failed");
                  // Handle screen shots based on settings - but only if this is a UI test
                  if (testItem.isUITest()) {
                     if (DDTSettings.Settings().takeImageOnFailedStep()) {
                        String imageFileOrError = Util.takeScreenImage(testItem.getDriver(), "", testItem.getId());
                        if (!(imageFileOrError.toLowerCase().contains("error:"))) {
                           // Add it to the error - not Comments as error has occured
                           testItem.addError("Screen Image " + Util.sq(imageFileOrError) + " taken!");
                           testItem.setScreenShotFileName(imageFileOrError);
                        }
                     }
                     else
                        testItem.addComment("'Screen Image on step failure' feature is turned off - no image taken.  To turn feature on, change the relevant Settings' (takeImageOnFailedStep) property");
                  }
               }
               else {
                  testItem.addPassEvent("Step Passed");
                  incrementPass();
                  if (testItem.getElement() instanceof WebElement) {
                     // Preserve the most recent web element
                     mrElement = testItem.getElement();
                  }
               }

               // Report the current step
               testItem.setCurrTime();
               System.out.println(testItem.report());
               // Consider Test and Session termination
               shouldQuitTestCase |= testItem.shouldQuitTestCase();
               shouldQuitTestSession |= testItem.shouldQuitTestSession();
            }  // if testItem.active
            else {
               testItem.addSkipEvent("Step Skipped");
               incrementSkip();
            }
            // Consider Termination at the test case or test session level
            if (shouldQuitTestCase || shouldQuitTestSession) {
               String ptpBlurb = "";
               if (testItem.getPostTestPolicy() instanceof PostTestPolicy) {
                  ptpBlurb = "Post Test Policy: " + Util.sq(testItem.getPostTestPolicy().toString());
               }

               if (shouldQuitTestCase)
                  quitBlurb = "Level " + testItem.getLevel() + " - Test Case Terminated at test item with ID: " + testItem.getId();

               if (shouldQuitTestSession) {
                  quitBlurb += (isBlank(quitBlurb) ? ("Level " + testItem.getLevel() + " - Test Session Terminated at test item with ID: " + testItem.getId()) : " (Test Session Terminated as well)");
               }

               quitBlurb += (isBlank(ptpBlurb) ? "" : " - " + ptpBlurb);

               if (failed()) {
                  testItem.addError(quitBlurb);
                  setError("Test Case failed at test item with ID: " + testItem.getId() + (isBlank(ptpBlurb) ? "" : " - " + ptpBlurb));
               }
               else
                  testItem.addComment(quitBlurb);

               break;
            } // Should Quit Test or Session

            maintainCurrentTestItem(testItem, "remove");
         } // For all test items.

         /**
          * Create a JSON file of the items that ran at this level - one JSON file for each level
          */
         testItems.reportAsJSON();

         // Test Case completed - take action based on level (level 0 means we are done)

         // Test case level stats
         String str;
         if (nFail() > 0)
            str = "Test Failed (level " + getLevel() + "): " + nFail() + " test step(s) out of " + nDone() + " step(s) failed (" + nSkip() + " step(s) skipped)\n";
         else {
            str = "Test Passed (level " + getLevel() + "): " + nPass() + " test step(s) out of " + nDone() + " step(s) passed (" + nSkip() + " step(s) skipped)\n";
         }

         str += quitBlurb;

         System.out.println(pad + str);

      }

      // Test session level stats
      if (getLevel() < 1)  {
         // Final Report ...
         reportSessionSummaryAsJSON();
         if (nSessionFail() > 0) {
            //This quits the process... - no return from here...
            setSessionFailBlurb("Test Session Failed: " + nSessionFail() + " test step(s) out of " + nSessionDone() + " step(s) failed (" + nSessionSkip() + " step(s) skipped)\n");
         }
         else {
            //This quits the process... - no return from here...
            setSessionPassBlurb("Test Session Pass: " + nSessionPass() + " test step(s) out of " + nSessionDone() + " steps passed (" + nSessionSkip() + " step(s) skipped)\n");
         }
      }
   }

   /**
    * The starting point of the test run - use the args array or use defaults from DDTSettings.Settings()
    * @param args
    */
   public static void main(String[] args) {
      DDTSettings.reset();
      DDTestRunner inst = new DDTestRunner();
      String[] tmp;
      // args[] should be a ':' delimited string indicating the type of source input
      // A delimted string indicating the type of input used (at present File and Inline are the two options) followed by one or two other values
      // Example 1: File;DDTRoot.xls;Root
      // Example 2: Inline;InputGeneratorClassName;rootMethodName
      if (args.length == 1) {
         tmp = args[0].split(":");
      }
      else {
         // Use the default input
         tmp = DDTSettings.Settings().inputSpecsArray();
      }

      try {
         // Populate the instance's dictionary with current date variables
         addVariable("datevars", "%date%");
         // Get the initial test items specified in the properties file or the command line
         runOn(tmp);
      }
      catch (Exception ex) {
         System.out.println(ex.getMessage().toString()) ;
      }
      finally {
         if (getDriver() instanceof WebDriver) {
            System.out.println("End of Test Session - Closing Driver");
            getDriver().close();
         }
         endSession();
      }
   }

   public void pause() {
      int millisToPause = DDTSettings.Settings().defaultPause();
      if (millisToPause > 0) {
         try {
            //System.out.println("Pausing for " + String.valueOf(millisToPause + " milliseconds"));
            Thread.sleep(millisToPause);
         }
         catch (Exception e) {
         }
      }
   }
}
