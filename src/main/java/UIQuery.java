import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 10/05/14
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
 * This abstract class is a template for subclasses that locate a UI element in the supported UI frameworkand get some value based on a function, attribute or property
 * The result returned is typically used for subsequent verification and / or storage as variable for further use / comparison
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 10/02/14    |Bey      |Initial Version
 * 10/26/14    |Bey      |Introduce DDTTestContext functionality
 * 10/28/14    |Bey      |Inherit from DDTBase
 * ============|=========|====================================
 */
public abstract class UIQuery extends DDTBase{
   private String functionName;
   private String saveAs;

   public void setFunctionName(String value) {
      functionName = value;
   }

   public String getFunctionName() {
      return functionName;
   }

   public void setSaveAs(String value) {
      saveAs = value;
   }

   public String getSaveAs() {
      return saveAs;
   }

   public void clear() {
      super.clear();
      functionName = "";
      saveAs = "";
   }

   public void saveVariableIfNeeded (String result, String varName) {
      if (isBlank(getSaveAs()))
         return;

      DDTTestContext tmp = new DDTTestContext();
      tmp.setProperty(varName, result);
      String blurb = (String) Util.populateDictionaryFromHashtable(tmp, DDTTestRunner.getVarsMap());
   }

   // The abstract query method to be overridden by subclasses
   abstract String query(TestItem testItem);
   abstract String query(DDTTestContext testContext);

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
    * Query a WebDriver for UI element contents
    * The result is used for verification purposes as well as saving the value obtained from the UI as a variable for subsequent verification
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/02/14    |Bey      |Initial Version
    * 10/26/14    |Bey      |Introduce DDTTestContext functionality
    * 06/12/15    |Bey      |Fixed bug with query from testContext (was not setting up)
    * ============|=========|====================================
    */
   public static class WebDriverQuery extends UIQuery {

      public WebDriverQuery() {
      }

      /**
       * Set up the instance from a TestItem instance
       * @param testItem
       */
      //@Override
      public void setUpFrom(TestItem testItem) {
         setFunctionName(testItem.getQryFunction());
         setSaveAs(testItem.getDataProperties().getString("saveAs"));
      }

      /**
       * Set up the instance from a DDTTestContext instance
       * @param testContext
       */
      //@Override
      public void setUpFrom(DDTTestContext testContext) {
         String s = (String) testContext.getString("qryFunction");
         setFunctionName(s);
         s = (String) testContext.getString("saveAs");
         setSaveAs(s);
      }

      @Override
      /**
       * 'Query' a web driver instance (a property of testItem)
       */
      public String query(TestItem testItem) {
         setUpFrom(testItem);
         String result = doTheQuery();
         // Have the context save a variable from the result if so indicated
         if (!hasErrors())
            saveVariableIfNeeded(testItem.getDataProperties().getString("saveAs"), result);

         return result;
      }

      public String query(String fName, String saveAs) {
         setFunctionName(fName);
         setSaveAs(saveAs);

         String result = "";

         try {
            result = doTheQuery();
            if (hasErrors()) {
               return "";
            }
            String blurb = "Web Driver Query Successful Found: " + Util.sq(result);
            if (!isBlank(getSaveAs()))
               blurb += " - Variable Named: " + getSaveAs() + "Saved";
            addComment(blurb);
         }
         catch (Exception e) {
            setException(e);
            addError("Web Driver Query Failed.");
         }
         finally {
            return result;
         }
      }

      /**
       * This method does the actual query appropriate for this class
       * @return
       */
      public String doTheQuery() {
         String result = "";
         WebDriver driver = Driver.getDriver();
         if (!(driver instanceof WebDriver)) {
            addError("Invalid WebDriver Query: WebDriver not present!");
            return result;
         }

         if (isBlank(getFunctionName())){
            addError("Invalid (empty) specs for functionName and attributeName for web element interrogation - Only one, but not both, may be empty");
            return result;
         }
         try {
            switch (getFunctionName().toLowerCase()) {
               case "gettitle" : result = driver.getTitle(); break;
               case "getcurrenturl" : result = driver.getCurrentUrl(); break;
               case "getpagesource" : result = driver.getPageSource(); break;
               case "getwindowhandle" : result = driver.getWindowHandle().toString(); break;

               default : {
                  addError("Invalid 'Query' function found: " + Util.sq(getFunctionName()) + " - UI Element query failed.");
                  return result;
               }
            }
         }
         catch (Exception e)  {
            addError("Invalid 'Query' function (" + Util.sq(getFunctionName()) + " specs for web driver interrogation - applying function generated exception");
         }
         finally {
            return result;
         }
      }

      @Override
      /**
       * 'Query' a web driver instance (a property of testItem)
       */
      public String query(DDTTestContext testContext) {
         setUpFrom(testContext);
         String result = doTheQuery();

         return result;
      }
   }

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
    * Query a WebElement for some component contents
    * The result is used for verification purposes as well as saving the value obtained from the UI as a variable for subsequent verification
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/02/14    |Bey      |Initial Version
    * 10/26/14    |Bey      |Introduce DDTTestContext functionality
    * ============|=========|====================================
    */
   public static class WebElementQuery extends UIQuery {
      private WebElement element;
      private String queryParam;

      public WebElementQuery() {

      }

      public void clear() {
         super.clear();
         element = null;
         queryParam = null;
      }
      public void setElement(WebElement value) {
         element = value;
      }

      public WebElement getElement() {
         return element;
      }

      public void setQueryParam(String value) {
         queryParam = value;
      }

      public String getQueryParam() {
         return queryParam;
      }

      public String query(String fName, String qryParam, String saveAs, WebElement webElement) {
         setFunctionName(fName);
         setQueryParam(qryParam);
         setElement(webElement);
         setSaveAs(saveAs);

         String result = "";

         try {
            result = doTheQuery();
            if (hasErrors())
               return "";
            String blurb = "Element Query Successful Found: " + Util.sq(result);
            if (!isBlank(getSaveAs()))
               blurb += " - Variable Named: " + getSaveAs() + "Saved";
            addComment(blurb);
         }
         catch (Exception e) {
            setException(e);
            addError("Element Query Failed.");
         }
         finally {
            return result;
         }
      }

      /**
       * Set up the instance from a TestItem instance
       * @param testItem
       */

      public void setUpFrom(TestItem testItem) {
         String s = testItem.getQryFunction();
         setFunctionName(isBlank(s) ? "" : s);

         s = testItem.getQueryParam();
         setQueryParam(isBlank(s) ? "" : s);

         setElement(testItem.getElement());
      }

      /**
       * Set up the instance from a DDTTestContext instance
       * @param testContext
       */
      public void setUpFrom(DDTTestContext testContext) {
         String s = testContext.getString("qryFunction");
         setFunctionName(s);
         s = testContext.getString("queryParam");
         setQueryParam(s);
         s = testContext.getString("saveAs");
         setSaveAs(s);
         WebElement e = (WebElement)testContext.getProperty("element");
         setElement(e);
      }

      @Override
      /**
       * 'Query' a web element instance (a property of testItem)
       */
      public String query(TestItem testItem) {
         setUpFrom(testItem);
         String result = doTheQuery();
         // Check whether the testContext contains the SaveAs key.
         // If so, this indicates the result of the query should be saved as a variable
         if (!hasErrors()) {
            String saveAs = testItem.getDataProperties().getString("SaveAs");
            saveVariableIfNeeded(result, saveAs);
         }

         return result;
      }

      @Override
      /**
       * 'Query' a web driver instance (a property of testItem)
       */
      public String query(DDTTestContext testContext) {
         setUpFrom(testContext);
         String result = doTheQuery();
         // Have the context save a variable from the result if so indicated
         if (!hasErrors())
            testContext.saveVariableIfNeeded(result);

         return result;
      }

      /**
       * Do the actual Querying of the web element
       * @return
       */
      public String doTheQuery() {

         String result = "";
         WebElement e = getElement();
         if (!(e instanceof WebElement)) {
            addError("Setup Error: Element is missing 'Query'");
            return result;
         }

         boolean bool;

         if (isBlank(getFunctionName()) ){
            addError("Invalid (empty) specs for 'Query' functionName - functionName may NOT be empty");
            return result;
         }
         try {
            switch (getFunctionName().toLowerCase()) {
               case "gettext" : result = e.getText(); break;
               case "isenabled" : bool = e.isEnabled(); result = (bool) ? "true" : "false"; break;
               case "isdisplayed" : bool = e.isDisplayed(); result = (bool) ? "true" : "false"; break;
               case "isselected" : bool = e.isSelected(); result = (bool) ? "true" : "false"; break;
               case "getlocation" : result = e.getLocation().toString(); break;
               case "getsize" : result = e.getSize().toString(); break;
               case "gettagname" : result = e.getTagName(); break;
               case "getclass" : result = e.getClass().toString(); break;
               case "getcssvalue" : {
                  if (isBlank(getQueryParam())) {
                     addError("Invalid (empty) specs for Property Name (queryParam) provided for web element 'getCssValue' interrogation - Property Name is required.");
                  }
                  else
                     result = e.getCssValue(getQueryParam());
                  break;
               }
               case "getattribute" : {
                  if (isBlank(getQueryParam())) {
                     addError("Invalid (empty) specs for Attribute Name (queryParam)provided for web element 'getAttribute' interrogation - Attribute Name is required.");
                  }
                  else
                     result = e.getAttribute(getQueryParam());
                  break;
               }

               default :
                  addError("Invalid 'interrogation' function found: " + Util.sq(getFunctionName()) + " - element interrogation failed.");
            }
         }
         catch (Exception ex)  {
            addError("Invalid function ("+ Util.sq(getFunctionName()) + ") specs for web element 'getAttribute' interrogation - applying function generated exception");
         }
         finally {
            return result;
         }
      }
   }
}
