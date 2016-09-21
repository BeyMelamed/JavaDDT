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
 * 10/31/15    |Bey            |Change the name of "Data" element to "Vars" - improved readability
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
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "vars", "description"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculator", "Run the Calculator tests"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainingFinders", "Run the ChainFinders tests"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinders", "Run the CssFinders tests"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!frameSwitching", "Run the Frame Switching tests"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!nonBrowserPassingTests", "Run constant verification tests without web driver that should pass"});
      list.add(new String[] {"DDTDemo#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!nonBrowserFailingTests", "Run constant verification tests without web driver that should fail"});
      list.add(new String[] {"DDTDemo#", "generateReport", "", "", "", "", "Description=Report For Inline Test Provider", "Generate the report for the demo inline items provider"});

      stringifyTestItems(list);
   }

   public void calculator(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "URL=http://compendiumdev.co.uk/selenium/calculate.php;BaseTitle=The " + Util.dq("Selenium Simplified") + " Calculator", "Set the browser and Base URL for calculator tests"});
      list.add(new String[] {"CalcPrep#", "createWebDriver", "", "", "", "", "URL={url}", "Launch the browser with base URL"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=1;Number2=2;Action=plus;Answer=3;Function=GetText;CompareMode=NotBlank", "Verify a non blank answer"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=2;Number2=1;Action=minus;Answer=1;Function=GetText;CompareMode=Is", "Verify minus operation with action {action}"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=574;Function=GetText;CompareMode=EndsWith", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=1.*4;Function=GetText;CompareMode=Matches", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=true;Function=IsDisplayed;CompareMode=IsDisplayed", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "setVars", "", "", "", "", "Number1=1234;Number2=11;Action=times;Answer=35;Function=GetText;CompareMode=Contains", "Verify times operation with action {action}"});
      list.add(new String[] {"CalcPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!calculate", "Run the Calculator for the parameters set above"});
      list.add(new String[] {"CalcPrep#", "takeScreenShot", "", "", "", "", "", "Take a Screen Shot for this final test"});

      stringifyTestItems(list);
   }

   public void calculate(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"Calculate#", "sendKeys", "Id", "number1", "", "", "Value={number1}", "Enter {number1} in the Number1 text box"});
      list.add(new String[] {"Calculate#", "sendKeys", "Id", "number2", "", "", "Value={number2}", "Enter {number2} in the Number2 text box"});
      list.add(new String[] {"Calculate#", "findElement", "Id", "function", "", "", "", "Find the {function} web element"});
      list.add(new String[] {"Calculate#", "click", "Css", "option[value='{Action}']", "", "", "", "Click the '{action}' option in the Action drop down list"});
      list.add(new String[] {"Calculate#", "click", "Id", "calculate", "", "", "", "Click the Calculate button"});
      list.add(new String[] {"Calculate#", "verifyWebElement", "Id", "answer", "{function}", "", "Value={Answer};compareMode={CompareMode}", "Find the {function} web element"});

      stringifyTestItems(list);
   }

   public void chainingFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"ChainPrep#", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page"});
      list.add(new String[] {"ChainPrep#", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName3,a;Function=GetAttribute;Attribute=Id;FindValue=a3;CompareMode=Equals", "Set variables for this instance of chaining finders test case."});
      list.add(new String[] {"ChainPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainFinders", "Run this instance of of chaining finders test case."});
      list.add(new String[] {"ChainPrep#", "setVars", "", "", "", "", "Hows=id,name,tagName;Values=div1,pName9,a;Function=GetAttribute;Attribute=Id;FindValue=a9;CompareMode=Is", "Set variables for this instance of chaining finders test case."});
      list.add(new String[] {"ChainPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!chainFinders", "Run this instance of of chaining finders test case."});

      stringifyTestItems(list);
   }

   public void chainFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"Chain#", "verifyWebElement", "{hows}", "{values}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{hows}', locSpec of '{values}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'."});

      stringifyTestItems(list);
   }

   public void cssFinders(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"CSSPrep#", "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/find_by_playground.php", "Launch the browser to the find_by_playground.php page"});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=#p31;Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=*[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=Name;Value=[id='p31'];Function=GetAttribute;FindValue=pName31;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name=\"ulName1\"];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=[name='ulName1'];Function=GetAttribute;FindValue=ul1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=div.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=id;Value=*.specialDiv;Function=GetAttribute;FindValue=div1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});
      list.add(new String[] {"CSSPrep#", "setVars", "", "", "", "", "How=Css;Attribute=name;Value=li;Function=GetAttribute;FindValue=liName1;CompareMode=Equals", "Set variables for this instance of css finders test case."});
      list.add(new String[] {"CSSPrep#", "newTest", "", "", "", "", "InputSpecs=Inline!SampleTestStringsGenerator!cssFinder", "Run this instance of of css finder test case."});

      stringifyTestItems(list);

   }

   public void cssFinder(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "paramss", "description"});
      list.add(new String[] {"CSSFind#", "verifyWebElement", "{how}", "{value}", "{function}", "yes", "Value={FindValue};CompareMode={CompareMode};QueryParam={Attribute}", "Run this web element verification using locType of '{how}', locSpec of '{value}', qryFunction of '{function}', Attribute of '{attribute}', verifying value of '{FindValue}' with comparison method of '{CompareMode}'."});
      list.add(new String[] {"CSSFind#", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3;CompareMode=gt;QueryParam={Attribute};class=integer", "Verify the size of the attribute is > 3"});
      list.add(new String[] {"CSSFind#", "verifyElementSize", "{how}", "{value}", "getText", "yes", "Value=3.And.2000;CompareMode=between;QueryParam={Attribute};class=integer", "Verify the size of the attribute is between 3 and 50."});

      stringifyTestItems(list);
   }

   public void frameSwitching(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"SwitchFrames#",  "createWebDriver", "", "", "", "", "URL=http://www.compendiumdev.co.uk/selenium/frames", "Launch the browser to the 'frames' page"});
      list.add(new String[] {"SwitchFrames#", "verifyWebDriver", "", "", "getTitle", "yes", "Value=Frameset Example Title (Example 6);CompareMode=Equals", "Verify the title of the 'frames' page."});
      list.add(new String[] {"SwitchFrames#", "switchToFrame", "", "", "", "yes", "Value=content", "Switch to the content frame."});
      list.add(new String[] {"SwitchFrames#", "click", "Css", "a[href='green.html']", "", "yes", "", "Click the link for the green page"});
      list.add(new String[] {"SwitchFrames#", "findElement", "Css", "h1[id='green']", "", "yes", "", "Find the green page"});
      list.add(new String[] {"SwitchFrames#", "click", "Css", "a[href='content.html']", "", "yes", "", "Click the link for the content page"});
      list.add(new String[] {"SwitchFrames#", "findElement", "xpath", "//h1[.='Content']", "", "yes", "", "Find the Content page again"});
      list.add(new String[] {"SwitchFrames#", "verifyWebElement", "xpath", "//h1[.='Content']", "GetText", "yes", "Value=Content;CompareMode=Is", "Find the Content page again"});

      stringifyTestItems(list);

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - all tests should pass
    * @return
    */

   public void nonBrowserPassingTests(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=>=", "Verify 23456 is greater than or equal to 12345"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=GT", "Verify Long 23,456,456,789 is greater than -12345 with 1000's separator"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=!=", "Verify Decimal 23,456,456,789.00007 is not equal to -12345.0001 with 1000's separator"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=GreaterThan", "Verify Amount $23,456 is greater than $12,345"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=;Class=Decimal;CompareMode=empty", "Verify empty value is empty"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=ge;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is greater than or equal to Date of 02/03/2000"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2012;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2012"});
      list.add(new String[] {"Passing#",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100;Class=Decimal;CompareMode=Between", "Verify Decimal 100 is between -12,345.0001 and 23,456"});

      stringifyTestItems(list);

   }

   /**
    * This is NOT one of Allan Richardson's tests, just demonstrating jDDT features of verifying some hard coded variables - these tests should fail
    * @return
    */

   public void nonBrowserFailingTests(ArrayList<String[]> list) {
      // Template:
      //list.add(new String[] {"id", "action", "locType", "locSpecs", "qryFunction", "active", "params", "description"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=12345;ActualValue=23456;Class=Int;CompareMode=<", "Verify 23456 is less than 12345"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=-12,345;ActualValue=23,456,456,789;Class=Long;CompareMode=Equals", "Verify Long 23,456,456,789 equals -12345 with 1000's separator"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=-12,345.0001;ActualValue=23,456,456,789.00007;Class=Decimal;CompareMode=LT", "Verify Decimal 23,456,456,789.00007 is less than -12345.0001 with 1000's separator"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=$12,345;ActualValue=$23,456;Class=Amount;CompareMode=LessThan", "Verify Amount $23,456 is less than $12,345"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=before;Option=MM/dd/yyyy", "Verify 03/02/2010 falls before 02/03/2000"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=02/03/2000;ActualValue=03/02/2010;Class=Date;CompareMode=equals;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 falls on Date of 02/03/2000"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=02/03/2000.and.05/05/2009;ActualValue=03/02/2010;Class=Date;CompareMode=between;Option=MM/dd/yyyy", "Verify Date of 03/02/3010 is between the Dates of 02/03/2000 and 05/05/2009"});
      list.add(new String[] {"Failing#",  "verify", "", "", "", "", "Value=-12,345.0001.and.23,456;ActualValue=100,000.05;Class=Decimal;CompareMode=Between", "Verify Decimal -12,345.0001 is between 100 and 23,456"});

      stringifyTestItems(list);

   }
}
