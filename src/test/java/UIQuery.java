import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
 * This abstract class is a template for subclasses that 'Query' a UI element and get some value based on a function, attribute or property
 * The result returned is typically used for subsequent verification and / or storage as variable for further use / comparison
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 10/02/14    |Bey      |Initial Version
 * ============|=========|====================================
 */
public abstract class UIQuery {
   private static String functionName;

   public void setFunctionName(String value) {
      functionName = value;
   }

   public String getFunctionName() {
      return functionName;
   }

   /**
    * Determine whether saving the testItem indicates the result should be saved as variable - if so, save it
    * @param testItem
    */
   public static void saveVariableIfNeeded(TestItem testItem, String result) {
      if (!testItem.isFailure()) {
         String varName = testItem.getSaveAs();
         if (isNotBlank(varName)) {
            // test item may have a variable saving request in form of "saveas" variable name... - if so, add variables so named with the result
            testItem.addSavedProperty(varName, (isBlank(result)) ? "" : result);
            testItem.saveProperties();
         }
      }
   }
   // The abstract query method to be overridden by subclasses
   abstract String query(TestItem testItem);

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
    * Query a WebDriver UI element
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class WebDriverQuery extends UIQuery {

      public WebDriverQuery() {
      }

      @Override
      /**
       * 'Query' a web driver instance (a property of testItem)
       */
      public String query(TestItem testItem) {
         String result = "";

         WebDriver driver = testItem.getDriver();
         if (!(driver instanceof WebDriver)) {
            testItem.addError("Invalid WebDriver Query: WebDriver is of wrong type!");
            return result;
         }

         setFunctionName(testItem.getQryFunction());

         if (isBlank(getFunctionName())){
            testItem.addError("Invalid (empty) specs for functionName and attributeName for web element interrogation - Only one, but not both, may be empty");
            return result;
         }
         try {
            switch (getFunctionName().toLowerCase()) {
               case "gettitle" : result = driver.getTitle(); break;
               case "getcurrenturl" : result = driver.getCurrentUrl(); break;
               case "getpagesource" : result = driver.getPageSource(); break;
               case "getwindowhandle" : result = driver.getWindowHandle().toString(); break;

               default : {
                  testItem.addError("Invalid 'Query' function found: " + Util.sq(getFunctionName()) + " - UI Element query failed.");
                  return result;
               }
            }
            // See if result should be saved as variable - if so, save it
            saveVariableIfNeeded(testItem, result);
         }
         catch (Exception e)  {
            testItem.addError("Invalid 'Query' function ("+ Util.sq(getFunctionName()) + " specs for web element 'getAttribute' interrogation - applying function generated exception");
         }
         finally {
            return result;
         }
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
    * Query a WebElement UI element
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 10/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class WebElementQuery extends UIQuery {
      public WebElementQuery() {
      }


      @Override
      /**
       * 'Query' a web element instance (a property of testItem)
       */
      public String query(TestItem testItem) {
         String queryParam = testItem.getQueryParam();
         WebElement element = testItem.getElement();

         String result = "";

         if (!(element instanceof WebElement)) {
            testItem.addError("TestItem contains invalid web element for 'Query'");
            return result;
         }

         setFunctionName(testItem.getQryFunction());
         boolean bool;

         if (isBlank(getFunctionName()) ){
            testItem.addError("Invalid (empty) specs for 'Query' functionName - functionName may NOT be empty");
            return result;
         }
         try {
            switch (getFunctionName().toLowerCase()) {
               case "gettext" : result = element.getText(); break;
               case "isenabled" : bool = element.isEnabled(); result = (bool) ? "true" : "false"; break;
               case "isdisplayed" : bool = element.isDisplayed(); result = (bool) ? "true" : "false"; break;
               case "isselected" : bool = element.isSelected(); result = (bool) ? "true" : "false"; break;
               case "getlocation" : result = element.getLocation().toString(); break;
               case "getsize" : result = element.getSize().toString(); break;
               case "gettagname" : result = element.getTagName(); break;
               case "getclass" : result = element.getClass().toString(); break;
               case "getcssvalue" : {
                  if (isBlank(queryParam)) {
                     testItem.addError("Invalid (empty) specs for Property Name (queryParam) provided for web element 'getCssValue' interrogation - Property Name is required.");
                  }
                  else
                     result = element.getCssValue(queryParam);
                  break;
               }
               case "getattribute" : {
                  if (isBlank(queryParam)) {
                     testItem.addError("Invalid (empty) specs for Attribute Name (queryParam)provided for web element 'getAttribute' interrogation - Attribute Name is required.");
                  }
                  else
                     result = element.getAttribute(queryParam);
                  break;
               }

               default :
                  testItem.addError("Invalid 'interrogation' function found: " + Util.sq(getFunctionName()) + " - element interrogation failed.");
            }
         }
         catch (Exception e)  {
            testItem.addError("Invalid function ("+ Util.sq(getFunctionName()) + ") specs for web element 'getAttribute' interrogation - applying function generated exception");
         }
         finally {
            saveVariableIfNeeded(testItem, result);
            return result;
         }
      }
   }
}
