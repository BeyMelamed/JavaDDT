import java.util.ArrayList;

/**
 * Created by BeyMelamed on 7/02/2014.
 * Selenium Based Automation Project
 * Description
 *
 * =============================================================================
 * Copyright 2014 Avraham (Bey) Melamed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"});
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
 * This class is used to provide ('inline') a String[][] array from which TestItem instances are created by the TestStringsProvider class
 * This and other classes extending InlineTestStringsProvider can be developed independently of the main line of code and loaded dynamically
 * Most of the methods in this class demonstrate web testing of a tutorial web site created by Mr. Alan Richardson - a mentor specializing in test automation
 *
 * When        |Who            |What
 * ============|===============|========================================================
 * 07/02/2014  |Bey            |Initial Version
 * ============|===============|========================================================
 */
public class SampleTestStringsGenerator extends InlineTestStringsProvider {

   public SampleTestStringsGenerator() {

   }

   // ================================================================================================================
   //                               Test Item Strings Generating Methods
   // ================================================================================================================

   public void root(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"DDTDemo01", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculator", "Run the Calculator tests"});
      list.add(new String[] {"DDTDemo02", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainingFinders", "Run the ChainFinders tests"});
      list.add(new String[] {"DDTDemo03", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinders", "Run the CssFinders tests"});
      list.add(new String[] {"DDTDemo04", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!frameSwitching", "Run the Frame Switching tests"});
      list.add(new String[] {"DDTDemo05", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!nonBrowserPassingTests", "Run constant verification tests without web driver that should pass"});
      list.add(new String[] {"DDTDemo06", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!nonBrowserFailingTests", "Run constant verification tests without web driver that should fail"});
      list.add(new String[] {"DDTDemo07", "generateReport", "", "", "", "", "Description=Report For Inline Test Provider", "Generate the report for the demo inline items provider"});

      stringifyTestItems(list);
   }

   public void calculator(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"CalcPrep01", "setVars", "", "", "", "", "URL=http://compendiumdev.co.uk/selenium/calculate.php;BaseTitle=The " + Util.dq("Selenium Simplified") + " Calculator", "Set the browser and Base URL for calculator tests"});
      list.add(new String[] {"CalcPrep02", "createWebDriver", "", "", "", "", "URL={url}", "Launch the browser with base URL"});
      list.add(new String[] {"CalcPrep03", "setVars", "", "", "", "", "Number1=1;Number2=2;Action=plus;Answer=3;Function=GetText;CompareMode=NotBlank", "Verify a non blank answer"});
      list.add(new String[] {"CalcPrep04", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep05", "setVars", "", "", "", "", "Number1=2;Number2=1;Action=minus;Answer=1;Function=GetText;CompareMode=Is", "Verify minus operation with action {action}"});
      list.add(new String[] {"CalcPrep06", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep07", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=574;Function=GetText;CompareMode=EndsWith", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep08", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep09", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=1.*4;Function=GetText;CompareMode=Matches", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep10", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep11", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=true;Function=IsDisplayed;CompareMode=IsDisplayed", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep12", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep13", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=35;Function=GetText;CompareMode=Contains", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep14", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep15", "takeScreenShot", "", "", "", "", "", "Take a Screen Shot for this final test"});

      stringifyTestItems(list);
   }

   public void calculate(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"Calculate01", "sendKeys", "Id", "number1", "", "", "Value={number1}", "Enter {number1} in the Number1 text box"});
      list.add(new String[] {"Calculate02", "sendKeys", "Id", "number2", "", "", "Value={number2}", "Enter {number2} in the Number2 text box"});
      list.add(new String[] {"Calculate03", "findElement", "Id", "function", "", "", "", "Find the {function} web element"});
      list.add(new String[] {"Calculate04", "click", "Css", "option[value='{Action}']", "", "", "", "Click the '{action}' option in the Action drop down list"});
      list.add(new String[] {"Calculate05", "click", "Id", "calculate", "", "", "", "Click the Calculate button"});
      list.add(new String[] {"Calculate06", "verifyWebElement", "Id", "answer", "{function}", "", "Value={Answer};compareMode={CompareMode}", "Find the {function} web element"});

      stringifyTestItems(list);
   }

   public void chainingFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"ChainPrep01", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page"});
      list.add(new String[] {"ChainPrep02", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName3,a;Function=GetAttribute;Attribute=Id;FindValue=a3;CompareMode=Equals", "Set variables for this instance of chaining finders test case."});
      list.add(new String[] {"ChainPrep03", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainFinders", "Run this instance of of chaining finders test case."});
      list.add(new String[] {"ChainPrep04", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName9,a;Function=GetAttribute;Attribute=Id;FindValue=a9;CompareMode=Is", "Set variables for this instance of chaining finders test case."});
      list.add(new String[] {"ChainPrep05", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainFinders", "Run this instance of of chaining finders test case."});

      stringifyTestItems(list);
   }

   public void chainFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"Chain01", "verifyWebElement", "{hows}", "{values}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{hows}', locSpec of '{values}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'."});

      stringifyTestItems(list);
   }

   public void cssFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"CSSPrep01", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page"});
      list.add(new String[] {"CSSPrep02", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=#p31;Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep03", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep04", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=*[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep05", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep06", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep07", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep08", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep09", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep10", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep11", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep12", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep13", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep14", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep15", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep16", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep17", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep18", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=div.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep19", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep20", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep21", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep22", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep23", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep24", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep25", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep26", "setVars", "", "", "", "", "How=Css;Attribute=name;Value=li;Function=GetAttribute;FindValue=liName1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep27", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});

      stringifyTestItems(list);

   }

   public void cssFinder(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"CSSFind01", "verifyWebElement", "{how}", "{value}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{how}', locSpec of '{value}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'."});
      list.add(new String[] {"CSSFind02", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3;CompareMode=gt;QueryParam={Attribute};class=integer", "Verify the size of the attribute is > 3"});
      list.add(new String[] {"CSSFind03", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3.And.2000;CompareMode=between;QueryParam={Attribute};class=integer", "Verify the size of the attribute is between 3 and 50."});

      stringifyTestItems(list);
   }

   public void frameSwitching(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"SwitchFrames01",  "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/frames", "Launch the browser to the 'frames' page"});
      list.add(new String[] {"SwitchFrames02", "verifyWebDriver", "", "", "getTitle", "yes", "Value=Frameset Example Title (Example 6);CompareMode=Equals", "Verify the title of the 'frames' page."});
      list.add(new String[] {"SwitchFrames03", "switchToFrame", "", "", "", "yes", "Value=content", "Switch to the content frame."});
      list.add(new String[] {"SwitchFrames04", "click", "Css", "a[href='green.html']", "", "yes", "", "Click the link for the green page"});
      list.add(new String[] {"SwitchFrames05", "findElement", "Css", "h1[id='green']", "", "yes", "", "Find the green page"});
      list.add(new String[] {"SwitchFrames06", "click", "Css", "a[href='content.html']", "", "yes", "", "Click the link for the content page"});
      list.add(new String[] {"SwitchFrames07", "findElement", "xpath", "//h1[.='Content']", "", "yes", "", "Find the Content page again"});
      list.add(new String[] {"SwitchFrames08", "verifyWebElement", "xpath", "//h1[.='Content']", "GetText", "yes", "Value=Content;CompareMode=Is", "Find the Content page again"});

      stringifyTestItems(list);

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - all tests should pass
    * @return
    */

   public void nonBrowserPassingTests(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"Passing01",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=>=", "Verify 23456 is greater than or equal to 12345"});
      list.add(new String[] {"Passing02",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=GT", "Verify Long 23,456,456,789 is greater than -12345 with 1000's separator"});
      list.add(new String[] {"Passing03",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=!=", "Verify Decimal 23,456,456,789.00007 is not equal to -12345.0001 with 1000's separator"});
      list.add(new String[] {"Passing04",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=GreaterThan", "Verify Amount $23,456 is greater than $12,345"});
      list.add(new String[] {"Passing05",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=;Class=Decimal;CompareMode=empty", "Verify empty value is empty"});
      list.add(new String[] {"Passing06",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=ge;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is greater than or equal to Date of 02/03/2000"});
      list.add(new String[] {"Passing07",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2012;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2012"});
      list.add(new String[] {"Passing08",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100;Class=Decimal;CompareMode=Between", "Verify Decimal 100 is between -12,345.0001 and 23,456"});

      stringifyTestItems(list);

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - these tests should fail
    * @return
    */

   public void nonBrowserFailingTests(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "data", "description"});
      list.add(new String[] {"Failing01",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=<", "Verify 23456 is less than 12345"});
      list.add(new String[] {"Failing02",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=Equals", "Verify Long 23,456,456,789 equals -12345 with 1000's separator"});
      list.add(new String[] {"Failing03",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=LT", "Verify Decimal 23,456,456,789.00007 is less than -12345.0001 with 1000's separator"});
      list.add(new String[] {"Failing04",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=LessThan", "Verify Amount $23,456 is less than $12,345"});
      list.add(new String[] {"Failing05",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=before;Option=MM/dd/yyyy", "Verify 03/02/2010 falls before 02/03/2000"});
      list.add(new String[] {"Failing06",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=equals;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 falls on Date of 02/03/2000"});
      list.add(new String[] {"Failing07",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2009;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2009"});
      list.add(new String[] {"Failing08",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100,000.05;Class=Decimal;CompareMode=Between", "Verify Decimal -12,345.0001 is between 100 and 23,456"});

      stringifyTestItems(list);

   }
}
