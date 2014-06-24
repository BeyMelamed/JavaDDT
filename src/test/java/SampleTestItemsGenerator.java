import java.util.ArrayList;

/**
 * Created by BeyMelamed on 6/17/2014.
 * Selenium Based Automation Project
 * Description
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
 * This class is used to provide ('inline') a String[][] array from which TestItem instances are created by the testItemsProvider class
 * This and other classes extending InlineTestItemsProvider can be developed independently of the main line of code and loaded dynamically
 *
 * When        |Who            |What
 * ============|===============|========================================================
 * 06/17/2014  |Bey            |Initial Version
 * ============|===============|========================================================
 */
public class SampleTestItemsGenerator extends TestItemsProvider.InlineTestItemsProvider {
   public SampleTestItemsGenerator() {

   }

   // ================================================================================================================
   //                               Test Items Generating Methods
   // ================================================================================================================

   public ArrayList<String[]> root() {

      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("DDTDemo01", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculator", "Run the Calculator tests");
      addItem("DDTDemo02", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:chainingFinders", "Run the ChainFinders tests");
      addItem("DDTDemo03", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinders", "Run the CssFinders tests");
      addItem("DDTDemo04", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:frameSwitching", "Run the Frame Switching tests");
      addItem("DDTDemo05", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:nonBrowserPassingTests", "Run constant verification tests without web driver that should pass");
      addItem("DDTDemo06", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:nonBrowserFailingTests", "Run constant verification tests without web driver that should fail");
      addItem("DDTDemo07", "generateReport", "", "", "", "", "Description=Report For Inline Test Provider", "Generate the report for the demo inline items provider");

      return getDataList();
   }

   public ArrayList<String[]> calculator() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("CalcPrep01", "setVars", "", "", "", "", "URL=http://compendiumdev.co.uk/selenium/calculate.php;BaseTitle=The " + Util.dq("Selenium Simplified") + " Calculator", "Set the browser and Base URL for calculator tests");
      addItem("CalcPrep02", "createWebDriver", "", "", "", "", "URL={url}", "Launch the browser with base URL");
      addItem("CalcPrep03", "setVars", "", "", "", "", "Number1=1;Number2=2;Action=plus;Answer=3;Function=GetText;CompareMode=NotBlank", "Verify a non blank answer");
      addItem("CalcPrep04", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep05", "setVars", "", "", "", "", "Number1=2;Number2=1;Action=minus;Answer=1;Function=GetText;CompareMode=Is", "Verify minus operation with action {action}");
      addItem("CalcPrep06", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep07", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=574;Function=GetText;CompareMode=EndsWith", "Verify times operation with action {action}");
      addItem("CalcPrep08", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep09", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=1.*4;Function=GetText;CompareMode=Matches", "Verify times operation with action {action}");
      addItem("CalcPrep10", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep11", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=true;Function=IsDisplayed;CompareMode=IsDisplayed", "Verify times operation with action {action}");
      addItem("CalcPrep12", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep13", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=35;Function=GetText;CompareMode=Contains", "Verify times operation with action {action}");
      addItem("CalcPrep14", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculate", "Run the Calculator for the parameters set above");
      addItem("CalcPrep15", "takeScreenShot", "", "", "", "", "", "Take a Screen Shot for this final test");

      return getDataList();
   }

   public ArrayList<String[]> calculate() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("Calculate01", "sendKeys", "Id", "number1", "", "", "Value={number1}", "Enter {number1} in the Number1 text box");
      addItem("Calculate02", "sendKeys", "Id", "number2", "", "", "Value={number2}", "Enter {number2} in the Number2 text box");
      addItem("Calculate03", "findElement", "Id", "function", "", "", "", "Find the {function} web element");
      addItem("Calculate04", "click", "Css", "option[value='{Action}']", "", "", "", "Click the '{action}' option in the Action drop down list");
      addItem("Calculate05", "click", "Id", "calculate", "", "", "", "Click the Calculate button");
      addItem("Calculate06", "verifyWebElement", "Id", "answer", "{function}", "", "Value={Answer};compareMode={CompareMode}", "Find the {function} web element");

      return getDataList();
   }

   public ArrayList<String[]> chainingFinders() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("ChainPrep01", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page");
      addItem("ChainPrep02", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName3,a;Function=GetAttribute;Attribute=Id;FindValue=a3;CompareMode=Equals", "Set variables for this instance of chaining finders test case.");
      addItem("ChainPrep03", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:chainFinders", "Run this instance of of chaining finders test case.");
      addItem("ChainPrep04", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName9,a;Function=GetAttribute;Attribute=Id;FindValue=a9;CompareMode=Is", "Set variables for this instance of chaining finders test case.");
      addItem("ChainPrep05", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:chainFinders", "Run this instance of of chaining finders test case.");

      return getDataList();
   }

   public ArrayList<String[]> chainFinders() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("Chain01", "verifyWebElement", "{hows}", "{values}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{hows}', locSpec of '{values}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'.");
      return getDataList();
   }

   public ArrayList<String[]> cssFinders() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("CSSPrep01", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page");
      addItem("CSSPrep02", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=#p31;Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep03", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep04", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=*[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep05", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep06", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep07", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep08", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep09", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep10", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep11", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep12", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep13", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep14", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep15", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep16", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep17", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep18", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=div.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep19", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep20", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep21", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep22", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep23", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep24", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep25", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");
      addItem("CSSPrep26", "setVars", "", "", "", "", "How=Css;Attribute=name;Value=li;Function=GetAttribute;FindValue=liName1;CompareMode=Equals", "Set variables for this instance of css finders test case.");
      addItem("CSSPrep27", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinder", "Run this instance of of css finder test case.");

      return getDataList();

   }

   public ArrayList<String[]> cssFinder() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("CSSFind01", "verifyWebElement", "{how}", "{value}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{how}', locSpec of '{value}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'.");
      addItem("CSSFind02", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3;CompareMode=gt;QueryParam={Attribute};class=integer", "Verify the size of the attribute is > 3");
      addItem("CSSFind03", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3.And.2000;CompareMode=between;QueryParam={Attribute};class=integer", "Verify the size of the attribute is between 3 and 50.");

      return getDataList();
   }

   public ArrayList<String[]> frameSwitching() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("SwitchFrames01",  "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/frames", "Launch the browser to the 'frames' page");
      addItem("SwitchFrames02", "verifyWebDriver", "", "", "getTitle", "yes", "Value=Frameset Example Title (Example 6);CompareMode=Equals", "Verify the title of the 'frames' page.");
      addItem("SwitchFrames03", "switchToFrame", "", "", "", "yes", "Value=content", "Switch to the content frame.");
      addItem("SwitchFrames04", "click", "Css", "a[href='green.html']", "", "yes", "", "Click the link for the green page");
      addItem("SwitchFrames05", "findElement", "Css", "h1[id='green']", "", "yes", "", "Find the green page");
      addItem("SwitchFrames06", "click", "Css", "a[href='content.html']", "", "yes", "", "Click the link for the content page");
      addItem("SwitchFrames07", "findElement", "xpath", "//h1[.='Content']", "", "yes", "", "Find the Content page again");
      addItem("SwitchFrames08", "verifyWebElement", "xpath", "//h1[.='Content']", "GetText", "yes", "Value=Content;CompareMode=Is", "Find the Content page again");

      return getDataList();

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - all tests should pass
    * @return
    */
   public ArrayList<String[]> nonBrowserPassingTests() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("Passing01",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=>=", "Verify 23456 is greater than or equal to 12345");
      addItem("Passing02",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=GT", "Verify Long 23,456,456,789 is greater than -12345 with 1000's separator");
      addItem("Passing03",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=!=", "Verify Decimal 23,456,456,789.00007 is not equal to -12345.0001 with 1000's separator");
      addItem("Passing04",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=GreaterThan", "Verify Amount $23,456 is greater than $12,345");
      addItem("Passing05",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=;Class=Decimal;CompareMode=empty", "Verify empty value is empty");
      addItem("Passing06",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=ge;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is greater than or equal to Date of 02/03/2000");
      addItem("Passing07",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2012;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2012");
      addItem("Passing08",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100;Class=Decimal;CompareMode=Between", "Verify Decimal 100 is between -12,345.0001 and 23,456");

      return getDataList();

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - these tests should fail
    * @return
    */
   public ArrayList<String[]> nonBrowserFailingTests() {
      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("Failing01",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=<", "Verify 23456 is less than 12345");
      addItem("Failing02",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=Equals", "Verify Long 23,456,456,789 equals -12345 with 1000's separator");
      addItem("Failing03",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=LT", "Verify Decimal 23,456,456,789.00007 is less than -12345.0001 with 1000's separator");
      addItem("Failing04",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=LessThan", "Verify Amount $23,456 is less than $12,345");
      addItem("Failing05",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=before;Option=MM/dd/yyyy", "Verify 03/02/2010 falls before 02/03/2000");
      addItem("Failing06",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=equals;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 falls on Date of 02/03/2000");
      addItem("Failing07",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2009;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2009");
      addItem("Failing08",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100,000.05;Class=Decimal;CompareMode=Between", "Verify Decimal -12,345.0001 is between 100 and 23,456");

      return getDataList();

   }

}
