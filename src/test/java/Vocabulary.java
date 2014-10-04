import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
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
 * This class represents the repertoire of actions supported by com.DynaBytes.jddt.
 * Method signature is such that each method works in context of a TestItem object that contains all the information needed for navigation, interrogation and activation
 * Errors (what went wrong) as well as comments (what was done successfully) are collected in the TestItem object.
 *
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/10/13    |Bey      |Initial Version
 * 06/23/14    |Bey      |recommit
 * 06/28/14    | Bey     |Remove references to getDriverIfNeeded() - moved to TestItem.Initialize()
 * ============|=========|====================================
 */
public class Vocabulary {

   private static final String uiMethods = ",click,clickCell,ensurePageLoaded,findCell,findElement,findOption,handleAlert,maximize,saveElementProperty,scrollWebPage,selectOption,sendKeys,switchToFrame,takeScreenShot,toggle,verifyOption,verifyWebDriver,";
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
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
      catch (Exception e) {
         if (StringUtils.isBlank(testItem.getErrors()))
            // Do not overwrite previous exceptions!
            if (!testItem.hasException())
               throw new VocabularyException(testItem, "Unknown Method Invocation exception ");
      }
   }

   /**
    * The distinguished construction
    */
   public Vocabulary() {}

   /**
    * Catch-all message to indicate a method is pending implementation
    */
   private static void notImplemented(TestItem testItem) throws Exception {
      throw new VocabularyException(testItem, "*** Not Implemented Yet ***");
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
    * @throws InvalidPropertiesFormatException
    * @throws ElementNotFoundException
    * @throws AssertionFailedError
    */
   public static void handleAlert(TestItem testItem) throws Exception {

      // Get the expected response from the data properties structure.
      // If exists, ensure it is valid, if blank, assume Accept.
      String userAction = testItem.getDataProperties().get("response"); // if present, should be Accept or Reject
      if (isBlank(userAction)) {
         userAction = "accept";
      }
      else {
         if (!"##accept##reject##".contains(userAction.toLowerCase())) {
            testItem.addError("Invalid Alert Response type encountered "+Util.sq(userAction) + " - only 'Accept' or 'Reject' are allowed.");
            return;
         }
      }

      // Get a verifier instance from the TestItem instance
      Verifier verifier = Verifier.getVerifier(testItem);

      WebDriver driver = testItem.getDriver();

      try {
         Alert alert = driver.switchTo().alert();
         String actualMessage = alert.getText();
         String expectedMessage = testItem.getDataProperties().get("value");
         if (isBlank(expectedMessage))
            expectedMessage = "";
         if (StringUtils.isNotBlank(expectedMessage))   {
            verifier.setAv(actualMessage);
            verifier.verify();
            if (verifier.isPass())
               testItem.addComment(verifier.getComments());
            else testItem.addError(verifier.getErrors());
         }

         if (userAction.equalsIgnoreCase("accept"))
            alert.accept();
         else
            alert.dismiss();
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * Main find element logic
    * Verify search criteria,
    * Construct a Locator Array (of By objects),
    * Invoke findElement using the Locator Array so constructed and a WebDriverWait chaining one or more By specs
    */
   public static void findElement(TestItem testItem) throws Exception{

      By[] chainedSpecs = new By[0];
      String how = testItem.getLocType();

      // Support for saving elements in TestRunner's elements Map
      String eKey = testItem.getDataProperties().get("saveelementas");
      if (isBlank(eKey))
         eKey = "";

      /**
       * chainedSpecs (LocatorArray) get created from comma delimited 'How', 'Value' and 'Modifier' specs
       */
      try {
         chainedSpecs = Util.createLocatorArray(testItem);
      }
      catch (Exception e) {
         testItem.setException(e);
         testItem.addError("Exception generated in createLocateArray method.");
         return;
      }

      if (chainedSpecs.length > 0 && !testItem.hasErrors()) {
         long waitInSeconds = testItem.getWaitTime();
         int waitIntervalMillis = testItem.getWaitInterval();
         WebElement element;

         if (isBlank(how)) {
            testItem.addError("'How' search property is null!");
            return;
         }

         WebDriver driver = testItem.getDriver();

         try {
            // we always wait for at least one second (may be less if element satisfies the expected conditions sooner)
            element = new WebDriverWait(driver, waitInSeconds, waitIntervalMillis).until(ExpectedConditions.
                  visibilityOfElementLocated(new ByChained(chainedSpecs)));

            if ((element instanceof WebElement)) {
               testItem.setElement(element);
               // This is needed to create web elements like Select, etc. once an element was found
               testItem.addComment("Element Found");
               testItem.addInfoEvent("Element Found");
               // If so indicated, save the element in the DDTestRunner elements Map
               if (!isBlank(eKey))
                  DDTestRunner.addElement(eKey, element);
            } else {
               testItem.addError("Web Element not found using 'By' specs of: " + testItem.getLocType() + " / " + testItem.getLocSpecs());
               testItem.addFailEvent("Web Element not found using 'By' specs of: " +  testItem.getLocType() + " / " + testItem.getLocSpecs());
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!testItem.hasException())
               throw new VocabularyException(testItem, "");
         }
      }
      else {
         // Should never get here...
         testItem.addError("Action " + Util.sq(testItem.getAction()) + " Failed to obtain 'By' specs - Action failed");
      }
   }

   /**
    * Find an option descendant off of an element with list of <option></option>.
    * The locator points to the parent.
    * testItem.getDataProperties() indicates what and how to find a given option
    * @param TestItem
    * @throws java.security.Exception
    *
    */
   public static void findOption(TestItem testItem) throws Exception {

      // Get a template verifier from the TestItem object
      Verifier verifier = Verifier.getVerifier(testItem);

      // User can specify either ItemValue or ItemText - ItemText has priority
      // ItemText
      String textToSelectBy = testItem.getDataProperties().get("itemtext");
      if (isBlank(textToSelectBy))
         textToSelectBy = "";

      // ItemValue
      String valueToSelectBy = testItem.getDataProperties().get("itemvalue");
      if (isBlank(valueToSelectBy))
         valueToSelectBy = "";

      // User may want to get a specific (1 based as opposed to 0 based) item!
      String itemNo =  testItem.getDataProperties().get("itemno");
      int itemIndex = -1;
      if (!StringUtils.isBlank(itemNo)) {
         try {
            itemIndex = Integer.valueOf(itemNo);
         }
         catch (Exception e) {
            throw new VocabularyException(testItem, "Invalid (non-numeric) Item No indicated - please inquire.");
         }
         if (itemIndex < 1) {
            testItem.addError("Invalid Item No specified - Item No must be greater than 0");
            return;
         }
      }

      // Determine the type of selection
      String selectBy = "itemtext";
      if (org.apache.commons.lang.StringUtils.isBlank(textToSelectBy) && !org.apache.commons.lang.StringUtils.isBlank(valueToSelectBy))
         selectBy = "itemvalue";

      // indicates whether to select option by its value property or text property
      boolean selectByText = (selectBy.equalsIgnoreCase("itemtext"));

      try {
         // Successful find of element results in storage of byChain in testItem...
         findElement(testItem);
         if (testItem.hasErrors())
            return;

         if ((testItem.getElement() instanceof WebElement)) {
            int optionNo = 0;
            String optionText="";
            String optionValue="";
            WebElement theOption = null;
            List<WebElement> options = testItem.getElement().findElements(By.tagName("option"));
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
                        throw new VocabularyException(testItem, "Failed to get value of option No. " + optionNo);
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
                  verifier.setErrors("");
                  verifier.verify();
                  foundOption = verifier.isPass();
               }

               if (foundOption) {
                  theOption = item;
                  break;
               }
            }

            if (theOption instanceof WebElement) {
               testItem.setElement(theOption);
               testItem.addComment("Option Found: " + (selectByText ? Util.sq(textToSelectBy) : Util.sq(valueToSelectBy)));
            }
            else {
               testItem.addError("Option Not Found: " + (selectByText ? Util.sq(textToSelectBy) : Util.sq(valueToSelectBy)));
            }
         }
         else
            testItem.addError("Failed to find web element");
      } //Try finding the element and processing it
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * Generic click mechanism
    */
   public static void click(TestItem testItem) throws Exception {
      try {
         findElement(testItem);
         if (testItem.hasErrors())
            return;
         if ((testItem.getElement() instanceof WebElement)) {
            if (testItem.getElement().isEnabled()) {
               new Actions(DDTestRunner.getDriver()).moveToElement(testItem.getElement()).perform();
               testItem.getElement().click();
               testItem.addComment("Element Clicked");
            }
            else
               testItem.addError("Element not enabled - click() failed");
         }
         else testItem.addError("Failed to find Web Element - Element not clicked!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Element not found or not visible");
      }
   }

   /**
    * Generic table cell clicking mechanism
    */
   public static void clickCell(TestItem testItem) throws Exception {
      try {
         findCell(testItem);
         if (testItem.hasErrors())
            return;
         if ((testItem.getElement() instanceof WebElement)) {
            // When a cell is found but the element to click is of a tag that is different than the cell's, a click on the cell may not do
            // Need to find the element with the tag
            String desiredTag = testItem.getDataProperties().get("tag");
            boolean foundElement = true;
            if (!StringUtils.isBlank(desiredTag) && !desiredTag.equalsIgnoreCase(testItem.getElement().getTagName()))  {
               // Find the element to click within the cell.
               // It must be an element with the desired tag AND get verified for the appropriate expected value.
               List<WebElement> alternateElements = testItem.getElement().findElements(By.tagName(desiredTag));
               String actualValue="";
               Verifier verifier = Verifier.getVerifier(testItem);

               foundElement = false;
               for (WebElement alternateElement : alternateElements) {
                  testItem.setElement(alternateElement);
                  UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
                  actualValue = weq.query(testItem);
                  if (testItem.hasErrors()) {
                     continue;
                  }

                  // Verify the present cell for specified value.
                  verifier.setAv(actualValue);
                  verifier.setErrors("");
                  verifier.verify();
                  if (verifier.isPass()) {
                     foundElement = true;
                  } // Verifier.isPass()
               } // for alternate element
            }  // else (alternate tag search)

            if (foundElement && testItem.getElement().isEnabled()) {
               new Actions(DDTestRunner.getDriver()).moveToElement(testItem.getElement()).build().perform();
               testItem.getElement().click();
               testItem.addComment("Cell Clicked");
            }
            else
               testItem.addError("(Cell) Element not enabled or cell's sub element not found - clickCell() failed");
         }
         else testItem.addError("Failed to find (cell) Web Element - Element not clicked!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Element not found or not visible");
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

      // User can specify either ItemValue or ItemText - ItemText has priority
      String textToSelectBy = testItem.getDataProperties().get("itemtext");
      if (isBlank(textToSelectBy))
         textToSelectBy = "";

      String valueToSelectBy = testItem.getDataProperties().get("itemvalue");
      if (isBlank(valueToSelectBy))
         valueToSelectBy = "";
      // If both, textToSelectBy and valueToSelectBy are present, use textToSelectBy else, use valueToSelectBy if it is not blank.
      // All these shenanigans are meant to support selection of a blank value when needed.
      String selectionValue = (isBlank(valueToSelectBy)) ? textToSelectBy : (isBlank(textToSelectBy) ? valueToSelectBy :textToSelectBy);

      try {
         // Successful find of element results in storage of found option in testItem.getElement()
         findOption(testItem);
         if (testItem.hasErrors())
            return;

         if ((testItem.getElement() instanceof WebElement)) {
            if (testItem.getElement().isEnabled())  {
               testItem.getElement().click();
               testItem.addComment("Option Selected: " + Util.sq(selectionValue));
            }
            else
               testItem.addError("Option not enabled - selectOption() failed");
         }
         else
            testItem.addError("Failed to find Web Element - Option not selected!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Element not visible (not found) - Options not selected");
      }
   }

   /**
    * Switching to a destination frame of the
    * @param TestItem
    */
   public static void switchToFrame(TestItem testItem) throws Exception {

      String frameName = testItem.getDataProperties().get("value");
      if (isNotBlank(frameName))
      {
         try {
            testItem.getDriver().switchTo().frame(frameName);
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!testItem.hasException())
               throw new VocabularyException(testItem, "Frame switching failed.");
         }
      }
   }

   /**
    * Takes a screen shot to be stored in the images folder (see DDTSettings.Settings() using a time-stamped file name based on the testItem.getId() string
    * NOTE: The DD
    * @param DDTSettings.Settings() indicates the output type of the file based on the locus of the test machine.
    * @throws IOException
    */
   public static void takeScreenShot(TestItem testItem) throws Exception {

      try {
         String result = Util.takeScreenImage(testItem.getDriver(), "", testItem.getId());
         File tmp = new File(result);
         if (tmp.length() > 1L) {
            testItem.addComment("Screen shot image stored at: " + Util.sq(result));
            testItem.setScreenShotFileName(result);
         }
         else {
            testItem.addError(result);
         }
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Screen shot not taken.");
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
         findElement(testItem);
         if (testItem.hasErrors())
            return;
         if ((testItem.getElement() instanceof WebElement)) {
            if (testItem.getElement().isEnabled()) {
               String toggleSpecs = testItem.getDataProperties().get("value");

               boolean shouldBeToggled = isBlank(toggleSpecs) ? true : Util.asBoolean(toggleSpecs);
               // The user should have indicated  attribute of 'checked' or 'selected' or whatever is appropriate
               UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
               String actualValue = weq.query(testItem);
               if (testItem.hasErrors())
                  return;

               if (isBlank(actualValue))
                  actualValue="false";

               boolean elementSelectedOrOn =  Util.asBoolean(actualValue.toLowerCase());
               boolean shouldClick = (elementSelectedOrOn != shouldBeToggled);
               if (shouldClick) {
                  new Actions(DDTestRunner.getDriver()).moveToElement(testItem.getElement()).perform();
                  testItem.getElement().click();
                  testItem.addComment((elementSelectedOrOn ? "Toggled": "Un-Toggled") + " element Toggled");
               }
               else {
                  testItem.addComment((elementSelectedOrOn ? "Toggled": "Un-Toggled") + " element already at desired state - Not Toggled");
               }
            }
            else
               testItem.addError("Element not enabled - toggle() failed");
         }
         else testItem.addError("Failed to find Web Element - Element not toggled!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Element not visible (not found) - Toggle failed");
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

         ensurePageLoaded(testItem);
         if (testItem.hasErrors())
            return;
         Verifier verifier = Verifier.getVerifier(testItem);

         // Interrogate the web driver based on the data properties structure that contain any of:
         // FunctionName - e.g. GetTitle, GetCurrentUrl, etc.
         // AttributeName - Used when the actual value is obtained by the getAttribute function
         // PropertyName - Used when the function is getCssValue to get the value of the css property

         UIQuery.WebDriverQuery wdq = new UIQuery.WebDriverQuery();
         String actualValue = wdq.query(testItem);
         if (testItem.hasErrors()) return;

         verifier.setAv(actualValue);
         verifier.verify();
         if (verifier.isPass())
            testItem.addComment(verifier.getComments());
         else testItem.addError(verifier.getErrors());
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Webdriver verification failed");
      }
   }

   /**
    * Force refreshing of the settings
    * @param testItem
    */
   public static void refreshSettings(TestItem testItem) throws Exception {
      try {
         DDTSettings.reset();
         testItem.addComment("Settings Reset");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Settings not refreshed.");
      }
   }

   /**
    * Run some command file, batch file, executable or shell command with or without parameters
    */
   public static void runCommand(TestItem testItem) throws Exception {

      String file = testItem.getDataProperties().get("filename");
      if (isBlank(file)) {
         testItem.addError("runCommand action requires file name - runCommand aborted.");
         return;
      }

      // Enable short-hand reference to the scripts folder - facilitate organization of script files.
      if (file.toLowerCase().startsWith("%script%"))
         file = DDTSettings.Settings().scriptsFolder()+ file.substring(8);
      if (!file.contains("\\") && !file.contains("/")) {
         // User implies invoke a file in the project's Scripts folder
         file = DDTSettings.Settings().scriptsFolder() + file;
      }

      File f = new File(file);
      if(!(f.exists()) || f.isDirectory()) {
         testItem.addError("File " + Util.sq(file) + " is not a valid script or executable file - runCommand aborted.");
         return;
      }

      // Create a parameters array, the first of which is the (executable) file name with optional parameters
      ArrayList<String> params = new ArrayList<String>();
      params.add(file);
      int i = 0;
      if (testItem.getDataProperties().size() > 0) {
         // Build the args array from dataProperties.
         // Parameters to executable are optional and are of the form ParamX={value}
         // Example:
         // Param1=some value;Param2=Another Value;Param3=Etc.
         while (true) {
            i++;
            String thisKey = "param" + String.valueOf(i);
            String thisParam = testItem.getDataProperties().get(thisKey);
            if (StringUtils.isBlank(thisParam)) {
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

      String execAndArgs = Util.asString(args, ", ");
      // Execute the command with the optional parameters (and cross your fingers)
      try {
         Runtime rt = Runtime.getRuntime();
         Process pr = rt.exec(args);
         testItem.addComment("External Command (" + execAndArgs + ") executed.");
         // See if user specified some wait time - if so, wait
         String waitTime = testItem.getDataProperties().get("waittime");
         if (!isBlank(waitTime))
            wait(testItem);
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "in running a command - Check command file and arguments: "  + execAndArgs.toString());
      }
   }

   /**
    * Save a property of a web element for further reference - after finding it using the Query and Data properties in the TestItem instance
    * @param TestItem
    * @throws Exception
    */
   public static void saveElementProperty(TestItem testItem) throws Exception {

      try {
         // Find the element to verify based on the locator in TestItem
         findElement(testItem);
         if (testItem.hasErrors())
            return;

         // Get the name of the variable to save element property under
         String varName = testItem.getSaveAs();
         if (isBlank(varName)) {
            testItem.addError("saveElementProperty - (required) 'SaveAs' element missing from data property!");
            return;
         }
         if ((testItem.getElement() instanceof WebElement) || testItem.getDriver() instanceof WebDriver) {

            // Interrogate the web element based on the data properties structure that contain any of:
            // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
            // Attribute - Used when the actual value is obtained by the getAttribute function
            // Property - Used when the function is getCssValue to get the value of the css property

            UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
            String actualValue = weq.query(testItem);
            if (testItem.hasErrors()) {
               testItem.addError("Property not saved.");
               return;
            }

            testItem.addSavedProperty(varName.toLowerCase(), actualValue);
            testItem.saveProperties();

         }  // WebElement or WebDriver exist
         else
            testItem.addError("Failed to find Web Element - verifyProperty failed!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Web Element verification failed.");
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

      if (testItem.getDriver() == null) {
         testItem.addError("Setup Error: WebDriver not present but is required!");
         return;
      }

      try {

         String scrollType = testItem.getDataProperties().get("type");
         if (StringUtils.isBlank(scrollType) ) {
            testItem.addError("Setup Error: ScrollType is missing!");
            return;
         }

         String x = testItem.getDataProperties().get("x");
         if (isBlank(x))
            x = "";

         String y = testItem.getDataProperties().get("y");
         if (isBlank(y))
            y = "";

         // Verify the scroll type definition is appropriate - X and Y components are present, are integer and are not 'max'
         if (!scrollType.toLowerCase().equals("scrollintoview")) {
            if (isBlank(x))
               testItem.addError("X (pixels) parameter is mandatory for this scroll type but is missing.");
            if (isBlank(y))
               testItem.addError("Y (pixels) parameter is mandatory for this scroll type but is missing.");
            if (testItem.hasErrors())
               return;
         }

         // Create a Java Executor Driver from the testItem.getDriver()
         JavascriptExecutor jsDriver = ((JavascriptExecutor) testItem.getDriver());

         switch (scrollType.toLowerCase()) {
            case "scrollby" : {
               // Both x and y must be numeric.
               try {
                  int intX = Integer.valueOf(x) + 0;
                  int intY = Integer.valueOf(y) + 0;
               }
               catch (Exception ex) {
                  testItem.addError("Setup Error: X parameter and Y parameters must be numeric but either or both are not!");
                  return;
               }

               // Execute the scrollBy specified by the user
               jsDriver.executeScript("javascript:window.scrollBy(" + x + "," + y + ");");
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
               jsDriver.executeScript("window.scrollTo(" + xExpression + "," + yExpression + ");");
               break;
            }
            case "scrollintoview" : {
              if (testItem.isUITest()) {
                 findElement(testItem);
                 if (testItem.hasErrors())
                    return;

                 // Execute the scrollIntoView specified by the user to the found element
                 jsDriver.executeScript("arguments[0].scrollIntoView(true);", testItem.getElement());
              }
               else {
                 testItem.addError("TestDriver Error: Test Item expected to be a UI item but is not.  Contact technician.");
                 return;
              }
              break;
            }

            default: {
               testItem.addError("Setup Error: Invalid Scroll Type: " + Util.sq(scrollType) + " encountered.  Valid types are: 'ScrollBy', 'ScrollTo' or 'ScrollIntoView'.");
               return;
            }
         }
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "ScrollWebPage failed.");
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
      // User can specify either ItemValue or ItemText - ItemText has priority
      String textToSelectBy = testItem.getDataProperties().get("itemtext");
      if (isBlank(textToSelectBy))
         textToSelectBy = "";

      String valueToSelectBy = testItem.getDataProperties().get("itemvalue");
      if (isBlank(valueToSelectBy))
         valueToSelectBy = "";
      // If both, textToSelectBy and valueToSelectBy are present, use textToSelectBy else, use valueToSelectBy if it is not blank.
      // All these shenanigans are meant to support selection of a blank value when needed.
      String selectionValue = (isBlank(valueToSelectBy)) ? textToSelectBy : (isBlank(textToSelectBy) ? valueToSelectBy :textToSelectBy);

      // Determine the type of selection
      String selectBy = "itemtext";
      if (isBlank(textToSelectBy) && !isBlank(valueToSelectBy))
         selectBy = "itemvalue";

      boolean selectByText = (selectBy.equalsIgnoreCase("itemtext"));

      try {
         // Successful find of element results in storage of found option in testItem.getElement()
         findOption(testItem);

         if (testItem.hasErrors())
            return;

         if (testItem.hasErrors())
            return;

         if ((testItem.getElement() instanceof WebElement)) {
            verifyWebElement(testItem);
         }
         else
            testItem.addError("Failed to find Selection Element - Option not found!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Web Element not found or not visible - Option not verified.");
      }
   }

   /**
    * verify a web element after finding it using the Query and Data properties in the TestItem instance
    * @param TestItem
    * @throws Exception
    */
   public static void verifyWebElement(TestItem testItem) throws Exception {

      try {
         // If requested, find the element to verify based on the locator in TestItem
         // The element may have already been found or function relates to Driver...
         if (!(testItem.getElement() instanceof WebElement))
            findElement(testItem);
         if (testItem.hasErrors())
            return;

         // Get a template verifier from the TestItem object
         Verifier verifier = Verifier.getVerifier(testItem);

         if ((testItem.getElement() instanceof WebElement) || testItem.getDriver() instanceof WebDriver) {

            // Interrogate the web element based on the data properties structure that contain any of:
            // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
            // Attribute - Used when the actual value is obtained by the getAttribute function
            // Property - Used when the function is getCssValue to get the value of the css property

            UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
            String actualValue = weq.query(testItem);
            if (testItem.hasErrors())
               return;

            verifier.setAv(actualValue);
            verifier.verify();
            if (verifier.isPass())
               testItem.addComment(verifier.getComments());
            else
               testItem.addError(verifier.getErrors());
         }
         else testItem.addError("Failed to find Web Element - verifyProperty failed!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * verify any two values (value is the expected value, actualValue is the actual value) bypassing web elements, webdriver, etc.
    * @param TestItem
    * @throws Exception
    */
   public static void verify(TestItem testItem) throws Exception {

      try {
         Verifier verifier = Verifier.getVerifier(testItem);
         String actualValue = testItem.getDataProperties().get("actualvalue");

         verifier.setAv(actualValue);
         verifier.verify();
         if (verifier.isPass())
            testItem.addComment(verifier.getComments());
         else
            testItem.addError(verifier.getErrors());
      } catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * verify the size of a web element's property (typically getText()
    * @param TestItem
    * @throws Exception
    */
   public static void verifyElementSize(TestItem testItem) throws Exception {

      try {
         // If requested, find the element to verify based on the locator in TestItem
         // The element may have already been found or function relates to Driver...
         if (!(testItem.getElement() instanceof WebElement))
            findElement(testItem);

         // Bail out on failure to find element
         if (testItem.hasErrors())
            return;

         // Get a template verifier from the TestItem object
         Verifier verifier = Verifier.getVerifier(testItem);

         if ((testItem.getElement() instanceof WebElement) || testItem.getDriver() instanceof WebDriver) {

            // Interrogate the web element based on the data properties structure that contain any of:
            // Function - e.g. GetText, GetTitle, getTagname, getAttribute, etc.
            // Attribute - Used when the actual value is obtained by the getAttribute function
            // Property - Used when the function is getCssValue to get the value of the css property

            UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
            String actualValue = weq.query(testItem);
            if (testItem.hasErrors())
               return;

            if (StringUtils.isBlank(actualValue))
               actualValue = "";

            // Represent the actual value as a numeric string that is the length of the actualValue returned above.
            // The int data type is a 32-bit signed two's complement integer. It has a minimum value of -2,147,483,648 and a maximum value of 2,147,483,647 (inclusive)
            // With the above definition, we feel safe with the following statement as opposed to trying both, (Long and int).valueOf()...
            actualValue = String.valueOf(actualValue.length());

            // Get the comparison mode for verification.
            String compareMode = testItem.getDataProperties().get("comparemode");
            if (isBlank(compareMode))
               compareMode = "equals";

            verifier.setComp(compareMode);

            // Commute the class of the length we are trying to verify to either int or long
            String cls = testItem.getDataProperties().get("class");
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
               testItem.addComment(verifier.getComments());
            else
               testItem.addError(verifier.getErrors());
         }
         else
            testItem.addError("Failed to find Web Element - verifySize failed!");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
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
         if (!(testItem.getElement() instanceof WebElement)) {
            // 'Standalone' findCell case - use findElement to locate the top element (table or tbody) of the table to be used as an 'anchor' for cell search.
            findElement(testItem);
            if (testItem.hasErrors())
               return;
         }
         phase ++;

         boolean reportEachTableCell = DDTSettings.Settings().reportEachTableCell();
         // Get a template verifier
         Verifier verifier = Verifier.getVerifier(testItem);

         // Determine the (optionally) requested column in the table to look at
         col =  testItem.getDataProperties().get("col");
         if (!StringUtils.isBlank(col))
            colNo = Integer.valueOf(col);

         // Determine the (optionally) requested row in the table to look at
         row =  testItem.getDataProperties().get("row");
         if (!StringUtils.isBlank(row))
            rowNo = Integer.valueOf(row);

         // Get the alternative tag name to look for if the element is to be found on an item other than <tr> / <td> option needed for verification (ignorecase, ...) @TODO See if more needed
         alternateTag = testItem.getDataProperties().get("tag"); // use this tag if the element is not found in a cell on the <td> tag
         if (StringUtils.isBlank(alternateTag))
            alternateTag = "";

         // Get the number of cells to find requested by the user - if invalid - set it to 1.
         cellsToFind = testItem.getDataProperties().get("cellstofind");
         if (isBlank(cellsToFind))
            cellsToFind = "";

         phase ++;
         if (!isBlank( cellsToFind)) {
            try {
               nCellsToFind = Integer.valueOf(cellsToFind);
               if (nCellsToFind < 1) {
                  nCellsToFind = 0;
                  testItem.addComment("Invalid specs: Number of cells to find should be larger than zero.  Number of Cells to Find set to 1.");
               }
            }
            catch (Exception e) {
               testItem.addComment("Invalid Specs for Number of Cells to Find.  Number of Cells to Find set to 1.");
            }
         }

         // Get the specification for range of rows in which to to find cells - if any.
         // A range of rows to examine (may conflict with "row" specs)

         rowRange = testItem.getDataProperties().get("rowrange");
         if (isBlank(rowRange))
            rowRange = "";

         String[] rowRangeSpecs;

         phase ++;

         try {
            if (!(isBlank(rowRange)) && rowRange.contains("-")) {
               rowRangeSpecs = rowRange.split("-");
               if (rowRangeSpecs.length ==2) {
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
                  testItem.addError("Invalid input ignored - too many dashes in range specification: " + Util.sq(rowRange));
                  return;
               }
            } // if establishing range of rows to examine
         } // Try to convert row range to integers
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!testItem.hasException())
               throw new VocabularyException(testItem, "Invalid (row range) input ignored - invalid or non numeric input in Row Range specification: " +  Util.sq(rowRange) + ".  Cell search aborted.");
         } // catch trying to get a valid range of rows.

         if ((testItem.getElement() instanceof WebElement) || testItem.getDriver() instanceof WebDriver) {

            // Assume all rows are represented by "tr" tag.
            List<WebElement> rows = testItem.getElement().findElements(By.tagName("tr"));
            nRows = rows.size();
            if (nRows > 0)
            {
               startCol = (colNo > 0) ? colNo : 0;
               startRow = (rowNo > 0) ? rowNo : 0;
               if (startRow > 0 && (findRange && firstRowToExamine == lastRowToExamine) && firstRowToExamine != startRow) {
                  // Abandon Search
                  testItem.addError("Conflicting 'Row' and 'RowRange' specification (Single row-range specified that is different than row to examine).  Find cell aborted.");
                  return;
               }

               if (lastRowToExamine > nRows) {
                  testItem.addError("Invalid 'RowRange' specification (last row to examine " + Util.sq(String.valueOf(lastRowToExamine)) + " exceeds number of rows in range " + Util.sq(String.valueOf(nRows)) + ".  Search cell aborted.");
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
                           // Replace the current cell in TestItem so it can be interrogated and qualified or disqualified

                           // Uncomment when testing to see in console which cells were actually examined
                           //testItem.addComment("Examine Cell [" + rowIndex + "," + colIndex + "]");

                           nCellsExamined++;
                           testItem.setElement(theCell);

                           UIQuery.WebElementQuery weq = new UIQuery.WebElementQuery();
                           String actualValue = weq.query(testItem);
                           if (testItem.hasErrors()) {
                              testItem.addError(" - Table Cell [" + rowIndex + "," + colIndex + "]. Cell search aborted." );
                              return;
                           }
                           verifier.setErrors("");
                           verifier.setAv(actualValue);
                           verifier.verify();
                           if (verifier.isPass()) {
                              foundCell = true;
                              nCellsFound++;

                              if (reportEachTableCell) {
                                 testItem.addComment("Cell Found - " + verifier.getComments() + " - Found table cell with specified value at [" + rowIndex + "," + colIndex + "].");
                              }

                              if (nCellsFound >= nCellsToFind) {
                                 doneSearching = true;
                                 break;
                              } // doneSearching - main logic
                           }  // Verifier.isPass()
                           else {
                              if (reportEachTableCell) {
                                 testItem.addComment("Cell Not Found - " + verifier.getErrors() + " - at table cell [" + rowIndex + "," + colIndex + "].");
                              }
                              // Look for alternate elements 'deeper' in the cell to locate the desired element
                              List<WebElement> alternateElements = theCell.findElements(By.tagName(alternateTag));
                              for (WebElement alternateElement : alternateElements) {
                                 testItem.setElement(alternateElement);
                                 actualValue = weq.query(testItem);
                                 if (testItem.hasErrors()) {
                                    testItem.addError(" - Table Cell (Alternate Search) [" + rowIndex + "," + colIndex + "]");
                                    return;
                                 }

                                 // Verify the present cell for specified value.
                                 verifier.setErrors("");
                                 verifier.setAv(actualValue);
                                 verifier.verify();
                                 if (verifier.isPass()) {
                                    if (reportEachTableCell) {
                                       testItem.addComment("Cell Found - " + verifier.getComments() + " - Found table cell with specified value at [" + rowIndex + "," + colIndex + "].");
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
                              testItem.addComment("Specified cell number (" + startCol + ") is greater than table's number of columns at row: " + rowIndex + ", nCols: " + nCols + ". Row skipped, Cell search continued.");
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
                     comment += nCellsExamined + " " + ((nCellsExamined > 1)? " cells " : " cell ") + "examined. ";
                     comment += nCellsFound + " " + ((nCellsFound > 1)? " cells " : " cell ") + "found. ";

                     testItem.addComment(comment);
                  }  // doneSearching
                  else {
                     // All rows searched but number of cells to find is less than number of cells found

                     String error = "Cell search failed. ";

                     error += nCellsExamined + " " + ((nCellsExamined > 1)? " cells " : " cell ") + "examined. ";
                     error += nCellsFound + " " + ((nCellsFound != 1)? " cells " : " cell ") + "found. ".replace("1", "no");

                     if (nCellsToFind > 0) {
                        error += nCellsToFind + ((nCellsToFind != 1)? " cells " : " cell ") + " should have been found. ";
                     }

                     testItem.addError(error);
                  } // cell not found in this row.
               } // startRow <= nRows
               else {
                  if (nRows > 0) {

                     testItem.addError("Specified row number (" + startRow + ") is greater than table's number of rows (" + nRows + "). Cell search aborted.");
                     return;
                  }
               } // handle invalid number of rows (startRow > nRows).
            } // nRows > 0
            else
               testItem.addError("No rows found in table - Cell search aborted.");
         }
         else
            testItem.addError("Failed to find table Web Element - Cell search aborted.");
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "Table element Not Found or Verification Failed." + " " + phase +"  cell [" + rowIndex + "," + colIndex + "].");
      }
   }

   /**
    * Close the Web Driver if it exists
    * @param TestItem
    */
   public static void quit(TestItem testItem) throws Exception {
      WebDriver d = testItem.getDriver();
      if (testItem.hasErrors()) {
         // Not having a driver when calling for a quit should not be considered an error.
         testItem.addComment(testItem.getErrors());
         testItem.setErrors("");
      }
      else {
         try {
            if (d != null && !d.getWindowHandles().isEmpty()) {
               d.close();
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            if (!testItem.hasException())
               throw new VocabularyException(testItem, "");
         }
         finally {
            DDTestRunner.setDriver(null);
         }
      }
   }

   /**
    * Generic data entry / typing mechanism using sendKeys
    */
   public static void sendKeys(TestItem testItem) throws Exception{

      String keys = testItem.getDataProperties().get("value");  // The value to enter
      String mode = testItem.getDataProperties().get("append"); // Should data be appended? (default is no)
      boolean append = Util.asBoolean(mode);                  // If data contains token: Append=Yes - element will not be cleared
      String shouldTabOut = testItem.getDataProperties().get("tabout");
      boolean tabOut;
      if (StringUtils.isBlank(shouldTabOut))
         tabOut = DDTSettings.Settings().tabOut();
      else tabOut = Util.asBoolean(shouldTabOut);

      if (tabOut)
         keys += "\t";

      try {
         if (isNotBlank(testItem.getLocSpecs())) {
            findElement(testItem);
            if (testItem.hasErrors())
               return;
            if ((testItem.getElement() instanceof WebElement))
               if (testItem.getElement().isEnabled()) {
                  if (!append) testItem.getElement().clear();
                  testItem.getElement().sendKeys(keys);
               }
               else testItem.addError("Element not enabled - sendKeys() failed");
            else testItem.addError("Failed to find Web Element - sendKeys failed!");
         }
         else
         {
            String typedKeys = KeyboardEmulator.type(keys, true);
            if (keys.equals(typedKeys)) {
               testItem.addComment("Typed: " + Util.sq(typedKeys));
            }
            else
               testItem.addError("Typed: " + Util.sq(typedKeys) + " instead of " + Util.sq(keys));

         }
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * generate report for the current test case.
    */
   public static void generateReport(TestItem testItem) throws Exception {
      String description = testItem.getDataProperties().get("description");
      String emailBody = testItem.getDataProperties().get("emailbody");
      if (isBlank(description))
         description = testItem.getDescription();
      TestEvent event = new TestEvent(DDTestRunner.TestEventType.INFO, "Generating Report");
      testItem.addEvent(event);
      try {
         DDTestRunner.generateReport(description, emailBody);
      }
      catch (IOException e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
      finally {
         DDTestRunner.resetRptCounters();
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
         throw new VocabularyException(testItem, "Invalid Action Method");
      }
      return m;
   }

   /**
    * Maximize / restore page (@TODO - change total dimension of page)
    * @param TestItem
    */
   public static void setPageSize(TestItem testItem)  throws Exception {

      try {
         String option = testItem.getDataProperties().get("value");
         if (isBlank(option))
            option = "";
         if (option.toLowerCase().equals("maximize")) {
            testItem.getDriver().manage().window().maximize();
         }
         else {
            // Try to get a point specification and resize the page to it.
            String x = testItem.getDataProperties().get("x");
            String y = testItem.getDataProperties().get("y");
            if (x.length()> 0 && y.length() > 0 && (Integer.valueOf(x) > 0) && (Integer.valueOf(y) > 0) ) {
               Dimension d = new Dimension(Integer.valueOf(x), Integer.valueOf(y));
               testItem.getDriver().manage().window().setSize(d);
               testItem.addComment("Page resized to " + d.toString());
            }
            else
               testItem.addError("Both 'x' and 'y' (numeric & positive) coordinates must be provided as page size");
         }
      }
      catch(Exception e){
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
      }
   }

   /**
    * Maximize the browser display
    * @param testItem
    * @throws Exception
    */
   public static void maximize(TestItem testItem) throws Exception {

      try {
         testItem.getDriver().manage().window().maximize();
      }
      catch (Exception e) {
         throw new VocabularyException(testItem, "Failed to Maximize page.");
      }
   }

   /**
    * Variable setting mechanism.
    */
   public static void setVars(TestItem testItem) throws Exception {
      if (isBlank(testItem.getData())) {
         testItem.addError("Action " + Util.sq(testItem.getAction()) + " requires non-empty data ");
         return;
      }

      try {
         String comment = Util.populateDictionaryFromHashtable(testItem.getDataProperties(), DDTestRunner.getVarsMap());
      }
      catch (Exception e) {
         throw new VocabularyException(testItem, "Variable(s) not set.");
      }
   }

   /**
    *
    * @param testItem
    * @throws Exception
    */
   public static void navigateToPage(TestItem testItem) throws Exception {
      String url = testItem.getDataProperties().get("url");
      if (isBlank(url)) {
         testItem.addError("URL is required for this action");
         return;
      }

      try {
         testItem.getDriver().navigate().to(url);
      }
      catch (Exception e) {
         throw new VocabularyException(testItem, "Page navigation failed.");
      }
   }

   /**
    * Start a new TestRunner instance using the input specs indicated in testItem.data
    */
   public static void newTest(TestItem testItem) throws Exception {

      String[][] testItemStrings;
      String inputSpecs = testItem.getDataProperties().get("inputspecs");

      // Test Items Strings provider - String[n][] where each of the n rows is a collection of strings making up a TestItem instance.
      // stringProviderSpecs contains information about the test item strings provider - err out if it is invalid.
      TestStringsProviderSpecs stringProviderSpecs = new TestStringsProviderSpecs(inputSpecs);
      if (!stringProviderSpecs.isSetupValid()) {
         testItem.addError("Invalid Test Strings Provided - " + stringProviderSpecs.getErrors());
         return;
      }

      // With valid stringProviderSpecs, attempt to get the strings making up the new test and err out if not successful
      testItemStrings = TestStringsProvider.provideTestStrings(stringProviderSpecs, DDTSettings.Settings().dataFolder());
      if (testItemStrings.length < 1) {
         testItem.addError("Failed to get test item strings from Test Strings Provider");
         return;
      }

      try {
            // With test item strings provided, start a sub test runner, assemble the test items and have the sub test runner process those.
            DDTestRunner runner = new DDTestRunner();
            runner.setLevel(testItem.getLevel() + 1); // Recursion level propagated

            TestItem.TestItems testItems = new TestItem.TestItems();
            testItems.setItems(TestItem.assembleTestItems(testItemStrings));
            testItems.setParentItem(testItem);
            runner.setTestItems(testItems);
            runner.setParentStepNumber(testItem.getSessionStepNumber());
            runner.processTestItems();
            if (runner.failed()) {
               String error = (isBlank(testItem.getErrors()) ? "" : testItem.getErrors()) + (" " + runner.getErrors().trim());
               if (isBlank(error)) {
                  error = "Test Case with ID of " + testItem.getId() + " failed";
               }
               // Do not consider an aggregate test runner step as failure but note there were errors in this 'test case'
               // (Only if the thing raises exception it is considered as failure)
               testItem.addComment(error);
            }
      }
      catch (Exception e) {
         throw new VocabularyException(testItem, "Failure in starting a new TestRunner (General Exception - Check Data in input source)");
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }
   }

   public static void createWebDriver(TestItem testItem) throws Exception {
      String browserName = testItem.getDataProperties().get("browser");
      if (isBlank(browserName))
         browserName = (String) DDTestRunner.getVarsMap().get("browser");

      if (isBlank(browserName)) browserName = DDTSettings.Settings().browserName();

      Driver.BrowserName browserType = Driver.asBrowserName(browserName);
      if (browserType == null) browserType = Driver.BrowserName.FIREFOX;
      Driver.set(browserType);
      String url = testItem.getDataProperties().get("url");
      if (isBlank(url)) {
         testItem.addError("URL must be a non-empty string.");
         return;
      }

      try {
         WebDriver driver = Driver.get(url);
         if (!(driver instanceof WebDriver)) {
            testItem.addError("Unable to navigate to page: " + Util.sq(url) + " Please check URL.");
            return;
         }

         DDTestRunner.setDriver(driver);
      }
      catch (Exception e) {
         throw new VocabularyException(testItem,"Web driver not created.");
      }
   }

   /**
    * Waiting mechanism.
    */
   public static void wait(TestItem testItem) throws Exception {
      Long timeInSeconds = testItem.getWaitTime();
      if (timeInSeconds > 0)
      {
         try {
            Thread.sleep((timeInSeconds * 1000));
            testItem.addComment("Waited for " + timeInSeconds + " seconds");
         }
         catch (Exception e) {
            throw new VocabularyException(testItem, "Invalid wait period specified.  Wait aborted." );
         }
      }
      else {
         testItem.addComment("Wait action specified without time period - wait not performed");
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

      String searchTitle;
      // Get the function to use in verification
      String functionName = testItem.getQryFunction();
      // Get the expected value from the data properties structure.
      String expectedValue = testItem.getDataProperties().get("value"); // User expects this value - it may serve for comparison with GetTitle or other property
      if (functionName.equalsIgnoreCase("getTitle"))
         searchTitle = expectedValue;
      else
         searchTitle = testItem.getDataProperties().get("pagetitle");

      if (isBlank(searchTitle)) {
         testItem.addError("No value provided for page title search for ensuring page was loaded");
         return;
      }

      long waitInSeconds = testItem.getWaitTime();
      int waitIntervalMillis = testItem.getWaitInterval();

      WebDriver driver = testItem.getDriver();

      try {
         // we always wait for at least one second (may be less if element satisfies the expected conditions sooner)
         boolean loaded = new WebDriverWait(driver, waitInSeconds, waitIntervalMillis).until(ExpectedConditions.
               titleContains(searchTitle));

         if (loaded) {
            testItem.addComment("Page with title containing " + Util.sq(searchTitle) + " Loaded");
         } else {
            testItem.addError("Page with title containing " + Util.sq(searchTitle) + " Not Loaded (? Timeout expired ?");
         }
      }
      catch (Exception e) {
         // Do not overwrite previous exceptions!
         if (!testItem.hasException())
            throw new VocabularyException(testItem, "");
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
         testItem.addEvent(new TestEvent(DDTestRunner.TestEventType.FAIL, error));

      }
   }
}
