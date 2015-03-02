import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

   public void setFromTestContext(TestItem testItem) {
      setLocType(testItem.getLocType());
      setLocSpecs(testItem.getLocSpecs());
      setWaitTimeInSeconds(testItem.getWaitTime());
      setWaitPollPeriod(testItem.getWaitInterval());
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
   abstract void locate(TestItem testItem);
   abstract void locate(DDTTestContext testContext);
   abstract WebElement locate();

   public static class WebUILocator extends UILocator {
      public WebUILocator() {

      }

      @Override
      /**
       * Locates a WebUI Element based on the context in TestItem.
       * If found testItem.element is set to the located element
       * Else, testItem.errors and exceptions indicate the issue.
       */
      public void locate(TestItem testItem) {
         if (!Driver.isInitialized()) {
            testItem.addError("Web Driver not initialized - Locator cannot commence - Web Element not located.");
            return;
         }

         setFromTestContext(testItem);

         try {
            testItem.setElement(locate());
            if (!(isBlank(getErrors())))
               testItem.addError(getErrors());
         }
         catch (Exception e) {
            // Do not overwrite previous exceptions!
            testItem.addError("Exception generated in Locator instance.");
            testItem.setException(e);
         }
      }

      /**
       * Locates a WebUI Element based on the context in TestItem.
       * If found testItem.element is set to the located element
       * Else, testItem.errors and exceptions indicate the issue.
       */
      public void locate(String lType, String lSpecs, Long waitTime, int pollPeriod) {

         setLocType(lType);
         setLocSpecs(lSpecs);
         setWaitTimeInSeconds(waitTime);
         setWaitPollPeriod(pollPeriod);
         if (!Driver.isInitialized()) {
            addError("Web Driver not initialized - Locator cannot commence - Web Item not located.");
            return;
         }

         try {
            setElement(locate());
         }
         catch (Exception e) {
            setException(e);
            addError("Exception generated in Locator instance.");
         }
      }

      @Override
      /**
       * Locates a WebUI Element based on the context in TestItem.
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
            setElement(locate());
            testContext.setProperty("Element", getElement());
            if (!(isBlank(getErrors())))
               testContext.addError(getErrors());
         }
         catch (Exception e) {
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
            element = new WebDriverWait(Driver.getDriver(), getWaitTimeInSeconds(), getWaitPollPeriod()).until(ExpectedConditions.
                  visibilityOfElementLocated(new ByChained(getBy())));
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
          * Implement a version of Chaining by parsing the 'how' and 'searchValue' to sub elements
          */

         String[] hows = how.split(",");
         String[] searchValues = searchValue.split(",");
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
                     addError("Locator Setup Error: Invalid 'How' search property indicated (" + Util.sq(thisHow + ")"));
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
   }
}
