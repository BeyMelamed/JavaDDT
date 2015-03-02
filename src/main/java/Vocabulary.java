import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 12/2/13
 * Time: 10:04 PM
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
 * This class represents the repertoire of actions supported by the product
 * This class is basically a Proxy for the Dialect class where the logic of the product actually 'unravels'
 * Method signature is such that each method works in context of a TestItem object that is a test step context.
 * TestItem instance contains all the information needed for navigation, element location, interrogation and activation
 * Errors (what went wrong) as well as comments (what was done successfully) are collected in the TestItem object.
 *
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/10/13    |Bey      |Initial Version
 * 06/23/14    |Bey      |recommit
 * 06/28/14    | Bey     |Remove references to getDriverIfNeeded() - moved to TestItem.Initialize()
 * 10/29/14    | Bey     |Move action code to Dialect (TODO - Find a way to factor out the pattern of invoking a verb)
 * ============|=========|====================================
 */
public class Vocabulary {

   private static final String uiMethods = ",click,clickCell,ensurePageLoaded,findCell,findElement,findOption,handleAlert,maximize,saveElementProperty,scrollWebPage,selectOption,sendKeys,switchToFrame,takeScreenShot,toggle,verifyOption,verifyWebDriver,";

   /**
    * The distinguished construction
    */
   public Vocabulary() {}

   /**
    * basicDoIt runs a verb and passes the results to the testItem that instantiated it to begin with
    * This is appropriate for all Vocabulary methods that only use a DDTTestContext based exclusively on the testItem.dataProperties;
    * @param testItem
    * @param verb
    */
   public static void basicDoIt(TestItem testItem, Verb verb) throws Verb.VerbException{

      verb.doIt();

      if (verb.hasComments()) {
         testItem.addComment(verb.getComments());
         testItem.addPassEvent(verb.myName() + " performed successfully.");
      }

      if (verb.hasErrors())
         testItem.addError(verb.getErrors());

      if (verb.hasException())
         testItem.setException(verb.getException());

      if (verb.hasErrors() || verb.hasException())
         testItem.addFailEvent(verb.myName() + " failed.");
      if (verb.getElement() instanceof WebElement)
         testItem.setElement(verb.getElement());

   }

   public static String getUIMethods() {
      return uiMethods;
   }

    /**
    * Invokes an action whose name is specified by testItem.action attribute.
    * Use reflection to convert testItem.action to a method in the Vocabulary class.
    */
   public static void invoke(TestItem testItem) throws Exception {
      Method m = null;
      try {
         m = getMethod(testItem);
      }
      catch (NoSuchMethodException e) {
         throw new VocabularyException(testItem, "Empty (or invalid) Action indicated in test item.");
      }

      if (!(m instanceof Method))
      {
         throw new VocabularyException(testItem, "Empty (or invalid) Action indicated in test item.");
      }

      try {
         m.setAccessible(true);
         m.invoke(new Vocabulary(), testItem);
      }
      catch (IllegalAccessException e) {
            testItem.setException(e);
      }
      catch (Exception e) {
            testItem.setException(e);
      }

   }

   /**
    * Gets a method indicated by testItem.getAction() using reflection.
    */
   public static Method getMethod (TestItem testItem) throws Exception {
      Method m = null;
      if ((isBlank(testItem.getAction()))) {return m;}

      try {
         m = Vocabulary.class.getDeclaredMethod(testItem.getAction(), TestItem.class);
      }
      catch(Exception e) {
         testItem.setException(e);
      }
      return m;
   }

   /**
    * Sets up a Verb instance from a TestItem instance's
    * @param testItem
    * @param verb
    * @return
    */
   public static Verb setupFromTestItem(TestItem testItem, Verb verb) {
      // All settings are case insensitive - using some caps for readability
      verb.setContext(testItem.getDataProperties());
      verb.getContext().setProperty("stepId", testItem.getId());
      verb.getContext().setProperty("locType", testItem.getLocType());
      verb.getContext().setProperty("locSpecs", testItem.getLocSpecs());
      verb.getContext().setProperty("qryFunction", testItem.getQryFunction());
      verb.getContext().setProperty("Description", testItem.getDescription());
      if (verb.getContext().getString("Description").toLowerCase().contains(":debug:"))
         verb.getContext().setProperty("debug", true);
      if (testItem.getElement() instanceof WebElement)
         verb.getContext().setProperty("element", testItem.getElement());
      verb.getContext().setProperty("testItem", testItem);
      return verb;
   }

   /**
    * Generic click mechanism
    */
   public static void click(TestItem testItem) throws Exception {

      try {
         Verb.Click verb;
         verb = (Verb.Click) setupFromTestItem(testItem, new Verb.Click());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Generic table cell clicking mechanism
    */
   public static void clickCell(TestItem testItem) throws Exception {
      try {
         Verb.ClickCell verb;
         verb = (Verb.ClickCell) setupFromTestItem(testItem, new Verb.ClickCell());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * verify a web driver element by one of the valid functions for a web driver
    * The TestItem object has information enabling both, the verification of some web driver property
    * As well as synchronization criteria to ensure the page loaded prior to interrogating it.
    * Ensuring the page is loaded is accomplished by ensurePageLoaded that uses the page title for satisfying an ExpectedConditions object.
    * If the value of 'function' is not GetTitle then the search value is derived from dataProperties.get("pagetitle") instead of queryProperties.get("value")
    * @param TestItem
    * @throws Exception
    */
   public static void verifyWebDriver(TestItem testItem) throws Exception {

      try {

            Verb.VerifyWebDriver verb;
            verb = (Verb.VerifyWebDriver) setupFromTestItem(testItem, new Verb.VerifyWebDriver());
            Vocabulary.basicDoIt(testItem, verb);
         }
         catch (Exception e) {
            if (!testItem.hasException())
               testItem.setException(e);
         }
   }

   /**
    * verify a web element after finding it using the Query and Data properties in the TestItem instance
    * @param TestItem
    * @throws Exception
    */
   public static void verifyWebElement(TestItem testItem) throws Exception {

      try {
         Verb.VerifyWebElement verb;
         verb = (Verb.VerifyWebElement) setupFromTestItem(testItem, new Verb.VerifyWebElement());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Creates a WebDriver object as per the context in testItem
    * @param testItem
    * @throws Exception
    */
   public static void createWebDriver(TestItem testItem) throws Exception {
      try {
         Verb.CreateWebDriver verb;
         verb = (Verb.CreateWebDriver) setupFromTestItem(testItem, new Verb.CreateWebDriver());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }
   /**
    * Used after creation of a web driver and in context (prior to) verifying a web page.
    * Handles a driver that redirects or a page that is slow to load by using the verification data.
    * Ensuring the page is loaded is accomplished by ensurePageLoaded that uses the page title for satisfying an ExpectedConditions object.
    * If the value of 'function' is not GetTitle then the search value is derived from dataProperties.get("pagetitle") instead of queryProperties.get("value")
    * The ExpectedConditions object always uses a String 'contains' function applied to the page's title
    * This verb is used in two scenarios.
    * 1. verifyWebDriver which includes verification of properties other than the page's title but must also provide for page title search.
    * 2. As a step with action of ensurePageLoaded in which case the value for the title is specified in the pagetitle proprty of the dataProperties
    * @param TestItem
    */
   public static void ensurePageLoaded(TestItem testItem) throws Exception{

      try {
         Verb.EnsurePageLoaded verb;
         verb = (Verb.EnsurePageLoaded) setupFromTestItem(testItem, new Verb.EnsurePageLoaded());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Finds a cell in a table after finding the table's top element (table or tbody) using the Query and Data properties in the TestItem instance
    * The locator(s) in TestItem are to the top table or tbody element
    * This may be called as a verb (action) in its own right or as a precursor to (say) click - with the intention of clicking a found cell
    * In the former case (findCell as a standalone action) first, use the findElement function.
    * In the latter case (precursor to clicking) the top (table or tbody) element was already found and the findElement set testItem.getElement()
    * If the table is found, the tokens in the testItem.getDataProperties() are used to (potentially) restrict the search to some row, or column or row/column combination
    * Note: if the cell is found, it remains as the testItem.getElement() and can be clicked on (or acted upon) subsequently
    * @param TestItem
    * @throws Exception
    */
   public static void findCell(TestItem testItem) throws Exception {
      try {
         Verb.FindCell verb;
         verb = (Verb.FindCell) setupFromTestItem(testItem, new Verb.FindCell());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Main find element logic using the context set in testItem
    */
   public static void findElement(TestItem testItem) throws Exception{

      try {
         Verb.FindElement verb;
         verb = (Verb.FindElement) setupFromTestItem(testItem, new Verb.FindElement());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
         return;
      }
   }

   /**
    * Find an option descendant off of an element with list of <option></option>.
    * The locator (testItem.locSpecs) points to the parent.
    * testItem.getDataProperties() indicates what and how to find a given option
    * @param TestItem
    * @throws java.security.Exception
    *
    */
   public static void findOption(TestItem testItem) throws Exception {

      try {
         Verb.FindOption verb;
         verb = (Verb.FindOption) setupFromTestItem(testItem, new Verb.FindOption());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * generate report for the current test case.
    */
   public static void generateReport(TestItem testItem) throws Exception {
      try {
         Verb.GenerateReport verb;
         verb = (Verb.GenerateReport) setupFromTestItem(testItem, new Verb.GenerateReport());
         verb.getContext().setProperty("DefaultReportDescription", testItem.getDescription());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Handles alert as per the information in TestItem instance.
    * 1. Execute driver.switchTo().alert()
    * 2. Based on TestItem input...
    *    If Message exists - verify message (property named Value) text (record error but no exception if message mismatches)
    *    If Response exists - either reject or accept as per content of TestItem
    *    If neither Message nor Response are present - attempt to accept (the default)
    *
    * @param TestItem   -  TestItem instance with test step context.
    *                 -  dataProperties has the following properties:
    *                    Response=Accept or Response=Reject
    *                    Message={some text}
    * @throws java.util.InvalidPropertiesFormatException
    * @throws com.gargoylesoftware.htmlunit.ElementNotFoundException
    * @throws AssertionFailedError
    */
   public static void handleAlert(TestItem testItem) throws Exception {

      try {
         Verb.HandleAlert verb;
         verb = (Verb.HandleAlert) setupFromTestItem(testItem, new Verb.HandleAlert());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Maximize the browser display
    * @param testItem
    * @throws Exception
    */
   public static void maximize(TestItem testItem) throws Exception {
      try {
         Verb.Maximize verb;
         verb = (Verb.Maximize) setupFromTestItem(testItem, new Verb.Maximize());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    *
    * @param testItem
    * @throws Exception
    */
   public static void navigateToPage(TestItem testItem) throws Exception {
      try {
         Verb.NavigateToPage verb;
         verb = (Verb.NavigateToPage) setupFromTestItem(testItem, new Verb.NavigateToPage());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Start a new TestRunner instance using the input specs indicated in testItem.data
    */
   public static void newTest(TestItem testItem) throws Exception {
      try {
         Verb.NewTest verb;
         verb = (Verb.NewTest) setupFromTestItem(testItem, new Verb.NewTest());
         verb.getContext().setProperty("Level", testItem.getLevel() + 1);
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Catch-all message to indicate a method is pending implementation
    */
   private static void notImplemented(TestItem testItem) throws Exception {
      Verb.NotImplemented verb;
      verb = (Verb.NotImplemented) setupFromTestItem(testItem, new Verb.NotImplemented());
      verb.getContext().setProperty("Error", "Not Implemented Yet!");
      Vocabulary.basicDoIt(testItem, verb);
   }

   /**
    * Close the Web Driver if it exists
    * @param TestItem
    */
   public static void quit(TestItem testItem) throws Exception {
      try {
         Verb.Quit verb;
         verb = (Verb.Quit) setupFromTestItem(testItem, new Verb.Quit());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Force refreshing of the settings
    * @param testItem
    */
   public static void refreshSettings(TestItem testItem) throws Exception {
      try {
         Verb.RefreshSettings verb;
         verb = (Verb.RefreshSettings) setupFromTestItem(testItem, new Verb.RefreshSettings());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Run some command file, batch file, executable or shell command with or without parameters
    */
   public static void runCommand(TestItem testItem) throws Exception {

      try {
         Verb.RunCommand verb;
         verb = (Verb.RunCommand) setupFromTestItem(testItem, new Verb.RunCommand());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Run some JavaScript using either a hard coded string or an external file containing (hopefully valid) java script;
    * If the java script is a hard coded string, the ';' (statement separators) are represented by the sequence of "\n"
    */
   public static void runJS(TestItem testItem) throws Exception {
      try {
         Verb.RunCommand verb;
         verb = (Verb.RunCommand) setupFromTestItem(testItem, new Verb.RunCommand());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Save a property of a web element for further reference - after finding it using the Query and Data properties in the TestItem instance
    * @param TestItem
    * @throws Exception
    */
   public static void saveElementProperty(TestItem testItem) throws Exception {
      try {
         Verb.SaveElementProperty verb;
         verb = (Verb.SaveElementProperty) setupFromTestItem(testItem, new Verb.SaveElementProperty());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Scrolls the web page as per the parameters indicated in testItem.cataProperties
    * Type - Type=ScrollTo; x={pixels}; y={pixels} (x, y are absolute pixels or the string 'max')
    *      - Type=ScrollBy; x={pixels}; y={pixels} (x, y are offset pixels)
    *      - Type=IntoView; must have element finding params.
    * NOTE - Selenium has no Scrolling functionality - thus, a JavaScript Executing Driver is used to run snippets of JavaScript
    *        The assumption is that javascript can be executed by the deriver.
    * Based on the type of scrolling definition - java script snippets are created, the driver is cast to JavascriptExecutor driver
    * @param TestItem
    * @throws Exception
    */
   public static void scrollWebPage(TestItem testItem) throws Exception {
      try {
         Verb.ScrollWebPage verb;
         verb = (Verb.ScrollWebPage) setupFromTestItem(testItem, new Verb.ScrollWebPage());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Selects an option descendant of a parent web element with list of <option></option> elements.
    * The locator points to the parent.
    * testItem.getDataProperties() indicate whether to select by value or by text using either ItemValue={someString} or ItemText={anotherString}
    * @param TestItem
    * @throws Exception
    *
    */
   public static void selectOption(TestItem testItem) throws Exception {

      try {
         Verb.SelectOption verb;
         verb = (Verb.SelectOption) setupFromTestItem(testItem, new Verb.SelectOption());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Generic data entry / typing mechanism using sendKeys
    */
   public static void sendKeys(TestItem testItem) throws Exception{
      try {
         Verb.TypeKeys verb;
         verb = (Verb.TypeKeys) setupFromTestItem(testItem, new Verb.TypeKeys());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      }
   }

   /**
    * Maximize / restore page (@TODO - change total dimension of page)
    * @param TestItem
    */
   public static void setPageSize(TestItem testItem)  throws Exception {
      try {
         Verb.SetPageSize verb;
         verb = (Verb.SetPageSize) setupFromTestItem(testItem, new Verb.SetPageSize());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         testItem.setException(e);
      } catch (Throwable throwable) {
         testItem.setException(throwable);
      }
   }

   /**
    * Variable setting mechanism.
    */
   public static void setVars(TestItem testItem) throws Exception {
      try {
         Verb.SetVars verb;
         verb = (Verb.SetVars) setupFromTestItem(testItem, new Verb.SetVars());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Switching to a destination frame of the
    * @param TestItem
    */
   public static void switchToFrame(TestItem testItem) throws Exception {
      try {
         Verb.SwitchToFrame verb;
         verb = (Verb.SwitchToFrame) setupFromTestItem(testItem, new Verb.SwitchToFrame());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Takes a screen shot to be stored in the images folder (see DDTSettings.Settings() using a time-stamped file name based on the testItem.getId() string
    * NOTE: The DD
    * @param DDTSettings.Settings() indicates the output type of the file based on the locus of the test machine.
    * @throws java.io.IOException
    */
   public static void takeScreenShot(TestItem testItem) throws Exception {

      try {
         Verb.TakeScreenShot verb;
         verb = (Verb.TakeScreenShot) setupFromTestItem(testItem, new Verb.TakeScreenShot());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Toggles a binary element on or off
    * If no toggling specified, assumed toggle on
    * testItem.getQryParam() should evaluate to the appropriate attribute for an element to interrogate.
    * For checkbox the attribute is 'checked' for radio button it is 'selected'
    */

   public static void toggle(TestItem testItem) throws Exception {
      try {
         Verb.Toggle verb;
         verb = (Verb.Toggle) setupFromTestItem(testItem, new Verb.Toggle());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }

   }

   /**
    * verify any two values (value is the expected value, actualValue is the actual value) bypassing web elements, webdriver, etc.
    * @param TestItem
    * @throws Exception
    */
   public static void verify(TestItem testItem) throws Exception {
      try {
         Verb.Verify verb;
         verb = (Verb.Verify) setupFromTestItem(testItem, new Verb.Verify());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * verify the size of a web element's property (typically getText()
    * @param TestItem
    * @throws Exception
    */
   public static void verifyElementSize(TestItem testItem) throws Exception {
      try {
         Verb.VerifyElementSize verb;
         verb = (Verb.VerifyElementSize) setupFromTestItem(testItem, new Verb.VerifyElementSize());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Verify property of an option descendant of a parent element with list of <option></option> elements.
    * The locator points to the parent.
    * testItem.getDataProperties() indicate whether to select by value or by text using either ItemValue={someString} or ItemText={anotherString}
    * @param TestItem
    * @throws Exception
    *
    */
   public static void verifyOption(TestItem testItem) throws Exception {
      try {
         Verb.VerifyOption verb;
         verb = (Verb.VerifyOption) setupFromTestItem(testItem, new Verb.VerifyOption());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            testItem.setException(e);
      }
   }

   /**
    * Waiting mechanism.
    */
   public static void wait(TestItem testItem) throws Exception {
      try {
         Verb.Wait verb;
         verb = (Verb.Wait) setupFromTestItem(testItem, new Verb.Wait());
         Vocabulary.basicDoIt(testItem, verb);
      }
      catch (Verb.VerbException throwable) {
         testItem.setException(throwable);
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 05/11/14
    * Time: 10:04 PM
    * Selenium Based Automation Project
    * Description
    * Class for handling vocabulary exceptions generically.
    * Methods in the Vocabulary class should throw an instance of this class with the TestItem instance and an extra blurb if any.
    *
    * History
    * Date        Who      |What
    * =========== |======= |====================================
    * 05/11/13    |Bey     |Initial Version
    * =========== |======= |====================================
    */
   public static class VocabularyException extends Exception {
      /**
       * Handles errors thrown by the various verbs of the vocabulary
       *
       * @param testItem
       * @param blurb    - Each verb may or may not have their own blurbs...
       */
      public VocabularyException(TestItem testItem, String blurb) {
         String prefix = "Action " + Util.sq(testItem.getAction()) + " generated exception.";
         String error = "";
         testItem.setException(this);
         if (!testItem.getErrors().startsWith(prefix))
            error = prefix;

         if (this.getCause() != null) {
            error = error + "  " + Util.sq(this.getCause().toString());
         }
         if (org.apache.commons.lang.StringUtils.isNotBlank(blurb)) {
            error = error + "  " + blurb;
         }
         testItem.addError(error);
         testItem.addEvent(new TestEvent(DDTTestRunner.TestEventType.FAIL, error));

      }
   }


   }
