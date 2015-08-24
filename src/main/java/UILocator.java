import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 10/02/14
 * Time: 12:20 AM
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
 * This abstract class is a template for subclasses that 'Locate' (find) a UI element in the supported framework
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 10/05/14    |Bey      |Initial Version
 * 10/26/14    |Bey      |Introduce DDTTestContext usage in addition to TestItem
 * 10/28/14    |Bey      |Inherit from DDTBase
 * 06/26/15    |Bey      |Remove references to initialization and locate based on TestItem instance
 * ============|=========|====================================
 */
public abstract class UILocator extends DDTBase{
   private static Long waitTimeSec = 1L;
   private static int waitPollTime = 50;

   public static void setWaitTimeSec(Long value) {
      waitTimeSec = value;
   }

   private static Long getWaitTimeSec() {
      return waitTimeSec;
   }

   public static void setWaitPollTime(int value) {
      waitPollTime = value;
   }

   private static int getWaitPollTime() {
      return waitPollTime;
   }

   private By[] byArray;
   private String locType;
   private String locSpecs;
   int waitPollPeriod;
   Long waitTimeInSeconds;

   public void setBy(By[] value) {
      byArray = value;
   }

   public By[] getBy() {
      return byArray;
   }

   public void setLocSpecs(String value) {
      locSpecs = value;
   }

   public String getLocSpecs() {
      return isBlank(locSpecs) ? "" : locSpecs;
   }

   public void setWaitPollPeriod(int value) {
      waitPollPeriod = value;
   }

   public int getWaitPollPeriod() {
      return waitPollPeriod < 50 ? getWaitPollTime() : waitPollPeriod;
   }

   public void setWaitTimeInSeconds(Long value) {
      waitTimeInSeconds = value;
   }

   public Long getWaitTimeInSeconds() {
      return waitTimeInSeconds < 1L ? getWaitTimeSec() : waitTimeInSeconds;
   }

   public void setLocType(String value) {
      locType = value;
   }

   public String getLocType() {
      return isBlank(locType) ? "" : locType;
   }

   public void setFromTestContext(DDTTestContext testContext) {
      setLocType((String)testContext.getString("locType"));
      setLocSpecs((String)testContext.getString("locSpecs"));
      setWaitTimeInSeconds((Long)testContext.getStringAsLong("waitTime"));
      setWaitPollPeriod((int)testContext.getStringAsInteger("waitInterval"));
   }

   public void setUpGeneric(String lType, String lSpecs, Long waitTimeSec, int waitInMilis) {
      setLocType(lType);
      setLocSpecs(lSpecs);
      setWaitTimeInSeconds(waitTimeSec);
      setWaitPollPeriod(waitInMilis);
   }

   public void clear() {
      super.clear();
      byArray = new By[0];
      locType = "";
      String locSpecs = "";
      waitPollPeriod = 50;
      waitTimeInSeconds = 5L;
   }
   /**
    * Locate a UI component as per the specifics and context of the TestItem instance
    * @param testItem
    */
   abstract void locate(DDTTestContext testContext);
   abstract WebElement locate();

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 10/02/14
    * Time: 12:20 AM
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
    * This class is the implementation of a Web UI element locator based on some form of test context
    * The test context contains information about the type and data needed to locate a web element in a page.
    * The information in the test context is used to create a locator array (or a locator instance)
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/05/14    |Bey      |Initial Version
    * 10/26/14    |Bey      |Introduce DDTTestContext usage in addition to TestItem
    * 10/28/14    |Bey      |Inherit from DDTBase
    * 06/26/15    |Bey      |Introduce TagLocator
    * 07/24/15    |Bey      |Changed Locator Array Delimiter to '^' to handle commas in locator value,
    *                       |Fixed message text bug (cosmetic)
    * 08/23/15    |Bey      |Changed Locator Array Delimiter to '`' from '^' which did not split
    * ============|=========|====================================
    */
   public static class WebUILocator extends UILocator {
      public WebUILocator() {
      }

      @Override
      /**
       * Locates a WebUI Element based on the context in testContext.
       * If found testItem.element is set to the located element
       * Else, testItem.errors and exceptions indicate the issue.
       */
      public void locate(DDTTestContext testContext) {
         setFromTestContext(testContext);
         if (!Driver.isInitialized()) {
            testContext.addError("Web Driver not initialized - Locator cannot commence - Web Item not located.");
            return;
         }


         try {
            WebElement el = locate();
            setElement(el);
            testContext.setProperty("Element", getElement());
            if (!(isBlank(getErrors()))) {
               testContext.addError(getErrors());
               return;
            }
            // Determine whether further drill-down is called for
            if (isTagLocator(testContext)) {
               // Create and invoke a tag locator that will search for the desired element as indicated in the testContext
               UILocatorByTag tagLocator = new UILocatorByTag();
               tagLocator.locate(testContext);
               // Set the result of the tagLocator in this instance
               setElement(tagLocator.getElement());
               if(tagLocator.hasErrors())
                  addError(tagLocator.getErrors());
               addComment(tagLocator.getComments());
            }
         } catch (Exception e) {
            setException(e);
            testContext.addError("Exception generated in Locator instance.");
            testContext.setProperty("Exception", e);
         }
      }

      @Override
      /**
       * Locates a WebUI Element based on the context in the instance whose locType and locSpecs were set properly.
       * If found the located element is returned
       * Else, errors and exceptions indicate the issue.
       */
      public WebElement locate() {

         WebElement element = null;

         if (!Driver.isInitialized()) {
            addError("Web Driver not initialized.  Locator failed.");
            return null;
         }

         createLocatorArray();
         if (!isBlank(getErrors()))
            return null;

         try {
            // Create a waiting web driver with delays specified - help handling Ajax pages
            if (getBy().length > 1)
               element = new WebDriverWait(Driver.getDriver(), getWaitTimeInSeconds(), getWaitPollPeriod()).until(ExpectedConditions.
                  visibilityOfElementLocated(new ByChained(getBy())));
            else {
               By by = getBy()[0];
               element = new WebDriverWait(Driver.getDriver(), getWaitTimeInSeconds(), getWaitPollPeriod()).until(ExpectedConditions.
                     visibilityOfElementLocated(by));
            }
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            addError("Exception generated in Locator instance. (" + e.getMessage().toString());
         }

         return element;

      }

      public void createLocatorArray() {

         By bySpecs;
         String searchValue;
         searchValue = getLocSpecs();    //comma delimited list of locator specifications (id, name, xpath, etc.)
         String how = getLocType();     // comma delimited list of 'By' specifications
         By[] chainedSpecs = new By[0];

         if (isBlank(how)) {
            addError("Locator Setup Error: 'LocType' property is null!");
            return;
         }

         if (isBlank(searchValue)) {
            addError("Locator Setup Error: 'LocSpecs' property is null!");
            return;
         }

         /**
          * Implement a version of Chaining made of comma delimited 'how' and 'searchValue' fragments.
          */

         String[] hows = how.split("`");
         String[] searchValues = searchValue.split("`");
         chainedSpecs = new By[hows.length];

         try {
            for (int i = 0; i < hows.length; i++) {
               // Protect against uneven array sizes...
               String thisHow = "";
               String thisValue = "";
               if (i < hows.length) thisHow = hows[i];
               if (i < searchValues.length) thisValue = searchValues[i];

               switch (thisHow.toLowerCase())  {
                  case "id": {
                     bySpecs = new By.ById(thisValue);
                     break;
                  }
                  case "css": {
                     bySpecs = new By.ByCssSelector(thisValue);
                     break;
                  }
                  case "name": {
                     bySpecs = new By.ByName(thisValue);
                     break;
                  }
                  case "classname": {
                     bySpecs = new By.ByClassName(thisValue);
                     break;
                  }
                  case "tagname": {
                     bySpecs = By.tagName(thisValue);
                     break;
                  }
                  case "linktext": {
                     bySpecs = new By.ByLinkText(thisValue);
                     break;
                  }
                  case "partiallinktext": {
                     bySpecs = new By.ByPartialLinkText(thisValue);
                     break;
                  }
                  case "xpath": {
                     bySpecs = new By.ByXPath(thisValue);
                     break;
                  }

                  default: {
                     addError("Locator Setup Error: Invalid 'How' search property indicated (" + Util.sq(thisHow) + ")");
                     return;
                  }
               }

               chainedSpecs[i] = bySpecs;
            }
         }
         catch (Exception e) {
            addError("Chained 'By' Creation Error: " + e.getCause().toString() + " Locator Array could not be constructed.");
         }

         setBy(chainedSpecs);

         return;

      }

      public boolean isTagLocator(DDTTestContext testContext) {
         return testContext.containsKey("tags");
      }

   }
   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 06/25/15
    * Time: 12:20 AM
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
    * This class is an implementation of a Web UI element locator based on some form of test context
    * The test context contains information about the type and data needed to locate a web element in a page along with a list of tags to traverse in an attempt to locate an element
    * Instances of this class are used when the particular web element location is not known but
    * a 'parent' element under which the sought element IS KNOWN as well as the list of tags along which to traverse the page.
    * The information in the test context is used to create a locator array (or a locator instance) as well as details about how to verify whether the sought element exists.
    * The list of tags is given as a comma delimited string using the 'key' Tags - e.g. Tags=ul[4],li[2],div,div.
    * The above translates to:
    * 1. Locate root element using the 'standard' locate() logic - this creates an 'anchor' for the traversal logic
    * 2. Traverse through fourth ul tag, second li tag, first div
    * 3. For each 'sibling' div element so discovered, verify the element using the appropriate 'verifier' (defined by 'standard' properties)
    *    Return the first item successfully verified
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 06/25/15    |Bey      |Initial Version
    * 07/14/15    |Bey      |Introduce instancesFoFind logic allowing for finding the nth element that matches search.
    * ============|=========|====================================
    */
   public class UILocatorByTag extends WebUILocator {
      private String[] tagsArray; // the array of tags to traverses through
      private boolean isInitialized = false;
      private WebElement foundElement = null;
      private UIQuery.WebElementQuery uiQuery = new UIQuery.WebElementQuery();
      private Verifier verifier = null;
      private DDTTestContext testContext;
      private int elementsInspected = 0;
      private int instanceToFind = 0;
      private int instancesFound = 0;
      private int iteration = 1;

      public UILocatorByTag() {

      }

      public UILocatorByTag(DDTTestContext context) {
         initializeFrom(testContext);
         isInitialized = !hasErrors();
      }

      public boolean foundElement() {
         return (foundElement instanceof WebElement);
      }

      public void initializeFrom(DDTTestContext context) {
         testContext = context;
         String tags = testContext.getString("tags");
         if (isBlank(tags)) {
            addError("Setup error: The 'tags' token is required but is missing from the test context");
            return;
         }
         setElement(context.getElement());

         tagsArray = tags.split(",");
         // Try to find the nTh instance (not, necessrily the first one)
         instanceToFind = testContext.getStringAsInteger("instance");
         // Consider only Nth instance greater than one (one is the default)
         if (instanceToFind < 2)
            instanceToFind = 0;
      }

      public int getElementsInspected() {
         return elementsInspected;
      }

      @Override
      public void locate(DDTTestContext context) {

         if (!isInitialized)
            initializeFrom(context);

         if (hasErrors())
            return;

         // Create a verifier for the (dis)qualification of an element.
         verifier = Verifier.getVerifier(context);
         // The web element to traverse from is the instance's element found at the root of the traversal
         traverseFromParentElement(getElement(), tagsArray, 0);

         if (foundElement()) {
            context.setProperty("element", foundElement);
            setElement(foundElement);
         }
         else
            addError("Element not found (inspected: " + elementsInspected + " elements.)");
      }

      private void traverseFromParentElement(WebElement element, String[] tags, int index) {
         // Terminate the traversal if the allowed depth of the traversal has been exceeded
         if (index >= tags.length)
            return;

         // Recursively traverse each of the elements at depth (index) lower than the length of the tags array minus one
         if (index < (tags.length -1)) {
            List<WebElement> traversalElements = element.findElements(By.tagName(tags[index]));
            for (WebElement e1 : traversalElements) {
               traverseFromParentElement(e1, tags, index + 1);
               if (foundElement()) {
                  instancesFound++;
                  if (instancesFound >= instanceToFind)
                     break;
               }
            }
         }
         else {
            // The traversal has reached its verification level
            // for each web element at this traversal level, setup a verifier and a WebElement Query in order to (dis)qualify the element
            List<WebElement> qualifyElements = element.findElements(By.tagName(tags[index]));
            // increment the number of iteration at the significant level
            iteration++;

            for (WebElement e2 : qualifyElements) {
               clearErrors();
               elementsInspected++;
               try {
                  // Set the element into the testContext of the instance
                  testContext.setProperty("element", e2);
                  // Get the actual value using a ui query and the web element from the context
                  String actualValue = uiQuery.query(testContext);

                  verifier.clearErrors();
                  verifier.setAv(actualValue);
                  //System.out.println("Actual: '" + actualValue + "\nExpected: '" + verifier.getEv() + "', Element Number: " + elementNumber +", Tag Level: " + (index + 1));
                  verifier.verify();
                  if (verifier.isPass()) {
                     foundElement = e2;
                     addComment("Element found (element number: " + elementsInspected + ", Tag Level: " + (index + 1) + ", iteration: " + iteration);
                     break;
                  }
               }
               catch(Exception ex) {
                  // Ignore errors @TODO handle more intellignently later.
                  continue;
               }
            }
         }
      }
   }
}
