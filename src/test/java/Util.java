import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.CapabilityType;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Map.Entry;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 12/31/13
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
 * Utilities methods live here
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/31/13    |Bey      |Initial Version
 * 10/01/14    |Bey      |Cleanup - remove unused methods
 * ============|=========|====================================
 */
public class Util {

   public static String sq(String str) { return surroundedBy("'", str, "'"); }

   public static String dq(String str) { return surroundedBy("\"", str, "\""); }

   public static String surroundedBy(String left, String str, String right) { return left + str + right;}

   public static Boolean asBoolean(String str) {
      if (str == null) {return false;}
      switch (str.toLowerCase())
      {
         case "yes"     : {return true;}
         case "true"    : {return true;}
         case "no"      : {return false;}
         case "false"   : {return false;}
         default        : {return false;}
      }
   }

   /**
    * Return the string true or false based on the boolean value of predicate.
    * @param predicate
    * @return
    */
   public static String booleanString(boolean predicate) {
      return predicate ? "true" : "false";
   }

   /**
    * Creates an html element from the tag, properties and contents strings
    * Example: tag = div, properties = "", contents = "Title" will return <div>Title</div>
    * Example: tag = a, properties = href='file:///C:\myfile.pdf' title='Click to view', contents = myfile.pdf will return:
    *                <a href='file:///C:\myfile.pdf' title='Click to view'>Title</a>
    * @param tag
    * @param properties
    * @param contents
    * @return
    */
   public static String makeHtmlElement(String tag, String properties, String contents) {
      if (isBlank(tag))
          return "";
      return "<" + tag + (isBlank(properties) ? "" : " " + properties)  + ">" + contents + "</" + tag + ">";
   }

   /**
    * Replaces a string with xml special characters converted to their corresponding code
    * @param text
    * @return a string with xml special characters converted to their corresponding code
    */
   public static String EncodeHtmlString(String text) {
      String result="";
      if (isBlank(text))
         return result;

      StringBuilder sb = new StringBuilder("");
      int len = text.length();
      Character c = null;
      for (int i = 0; i < len; i++) {
         c = text.charAt(i);
         switch (String.valueOf(c)) {
            case "&": sb.append("&amp;"); break;
            case "<": sb.append("&lt;"); break;
            case ">": sb.append("&gt;"); break;
            case "'": sb.append("&apos;"); break;
            case "\"" : sb.append("&quot;"); break;
            default: sb.append(c);
         }
      }
      result = sb.toString();
      return result;
   }

   public static String append(String strOld, String strNew, String strSep){
      String result = "";
      String prefix = "";

      if (strOld == null || strOld.isEmpty())  return strNew;

      return strOld + strSep + strNew;
   }

   /**
    *
    * @param s - the String to 'jsonify'
    * @return A string to be used as a value of some JSON element with properly 'escaped' characters
    */
   public static String jsonify(String s) {
      String result = "";
      if (isNotBlank(s))  {
         result = JSONObject.escape(s);
      }
      return result;
   }

   /**
    * Maintains a hashtable (dict) that enables string substitution in a reusable manner from a test item's set of variables (ht)
    * The dict hashtable is typically the DDTestRunner's varsMap.
    * Test Item's data property is in the form of {variableName}={someValue} instances separated by ";"
    * This delimited string is converted to a hashtable<String, String> where the keys are {variableName} and values are {someValue}
    * @param ht<String, String> structure
    * @param dict<String, Object> structure
    * @return String - comments that will populate test item's comments attribute.
    * @see Special cases:
    *    1. When {someValue} starts with "%date" and is surrounded by "%" - a DDTDate object is used to create a host of date / time stamp variable
    */
   public static String populateDictionaryFromHashtable(Hashtable<String, String> ht, Hashtable<String, Object> dict) {
      //Enumeration<String> keys = testItem.getDataProperties().keys();
      String result = "";
      String prefix = "";
      String key;
      Object value;
      Set<Entry<String, String>> entries = ht.entrySet();
      for (Entry<String, String> entry : entries) {
         // = itr.next();
         key = entry.getKey().toLowerCase();
         value = entry.getValue();

         // Special Case - date variables maintained by  DDDate class instance
         if (value.toString().toLowerCase().startsWith("%date") &&(value.toString().toLowerCase().endsWith("%"))) {
            try {
               DDTDate referenceDate = new DDTDate();
               referenceDate.resetDateProperties(value.toString(), dict);
               if (referenceDate.hasException())
                  result += prefix + referenceDate.getException().getMessage().toString();
               else
                  result += referenceDate.getComments();
            }
            catch (Exception e) {
               result += "Error encountered in setting date variables: " + e.getCause();
            }
            finally {
               result += " (variable named " + Util.sq(key) + " should be ignored)";
               prefix = ", ";
            }

         }
         else   {

            Object val = dict.get(key);
            if (val != null) {
               if (val.equals(value)) {
                  result += "Old version of (key) " + Util.sq(key) + " found in but is the same as existing value in the dictionary. ";
                  prefix = ", ";
                  continue;
               }
               else {
                  dict.remove(key);
                  result += "Old version of (key) " + Util.sq(key) + " found in, and removed from, dictionary. ";
                  prefix = ", ";
               }
            }

            dict.put(key, value);
            result += prefix + "Variable " + Util.sq(key) + " with value " + Util.sq(value.toString()) + " added to dictionary.";
         }
      }
      return result;
   }

   public static String asSafePathString(String path) {
      if (isBlank(path))
         path = "";
      return path.replace(":","").replace("\\","").replace("/","");
   }

   /**
    * Flattens a String array to one string delimited by delim string
    * @param aStr  An array of strings
    * @param delim  A dlimiter between each element of the array
    * @return  A string concatenation each element of the array
    */
   public static String asString(String[] aStr, String delim) {
      String result = "", prefix = "";
      // Handle situations that merit immediate return...
      if (aStr == null) return result;
      if (aStr.length == 0) return result;
      if (aStr.length == 2) return aStr[0];

      if (isBlank(delim))
         prefix = ", ";
      else
         prefix = delim;

      StringBuilder sb = new StringBuilder().append(aStr[0]);
      for (int i = 1; i < aStr.length; i++) {
         sb.append(prefix + aStr[i]);
      }

      result = sb.toString();

      return result;

   }
   /**
    *
    * @param e Exception object
    * @return String representing each of the stack elements
    */
   public static String stackTraceToString(Throwable e) {
      StringBuilder sb = new StringBuilder();
      String prefix = "";
      for (StackTraceElement element : e.getStackTrace()) {
         sb.append(prefix + element.toString());
         prefix = "\n";
      }
      return sb.toString();
   }

   /**
    * Parses a string with multiple instances of <key> '=' <value> - delimited by one of the valid delimiters to a hash table
    * The default delim is ';' but if delimStr's first character is one of the alternative delimiters then this is the used delimiter
    * Example:
    * delimStr of: "Value1=User;Value2=Password" will return a hash table [value1 -> User} {value2 -> Password}
    * delimStr of: "|Value1=User|Value2=Password" will also return a hash table [value1 -> User} {value2 -> Password}
    */
   public static Hashtable<String, String>  parseDelimitedString(String delimStr) {
      String DELIMS = DDTSettings.Settings().validDelims();
      String actualDelim = DDTSettings.Settings().itemDelim();
      String actualStr = delimStr;

      Hashtable<String, String> ht = new Hashtable<String, String> ();

      // Parameters sanity check1 - all strings are not blank
      if (isBlank(delimStr)) {
         return ht;
      }

      // Parameters sanity check2 - at least one instance of <key> "=" <value> exists
      if (!delimStr.contains("=")) {
         return ht;
      }

      // Determine if the caller uses their own delimiter.
      if (DELIMS.contains(delimStr.substring(0,1))) {
         actualDelim = delimStr.substring(0,1);
         actualStr = delimStr.substring(1);
      }

      String[] a1 = actualStr.split(actualDelim);
      for (int i = 0 ; i < a1.length; i++) {
         try {
            int idx = a1[i].indexOf("=");
            if (idx < 0) {
               throw new InvalidKeyException ("'=' Delimiter not found in item " + i + " of " + actualStr);
            }
            String key = a1[i].substring(0, idx);
            String value = a1[i].substring(idx + 1);

            if (isBlank(key))  {
               throw new InvalidKeyException ("Empty key value in item " + i + " of " + actualStr);
            }
            if (null != ht.get(key.toLowerCase())) {
               throw new InvalidKeyException ("Repeated key value of " + key + " in item " + i + " of " + actualStr);
            }
            // store next & unique value (a2[1]) in hashtable using key of a2[0]
            ht.put(key.toLowerCase(), value);
         }
         catch (InvalidKeyException e ) {
            return new Hashtable<String, String>();
         }
      }
      return ht;
   }
   /**
    *
    * @param input
    * @param dict
    * @return string with occurrences of {someText} substituted for the contents of a variable in the varsMap
    */
   public static String substituteVariables(String input, Hashtable<String, Object> dict) {
      String result = "";
      char open = '{';
      char close = '}';
      String inputCopy = input;
      String prefix, suffix = "";
      int idxOpen = inputCopy.indexOf(open);
      int idxClose = inputCopy.indexOf(close);

      // The case there are no variables to substitute
      if (idxOpen < 0 || idxClose < 0 || isBlank(input))
         return input;

      while (isNotBlank(inputCopy) && (idxOpen >= 0) && (idxClose > 2)) {
         // if (suffix.length() > 0) result += suffix;
         // Get new values for prefix, suffix and variable name
         prefix = inputCopy.substring(0, idxOpen);
         suffix = inputCopy.substring(idxClose + ((idxClose < (inputCopy.length())) ? 1 : 0));
         String varName = inputCopy.substring(idxOpen+1, idxClose);
         Object value = dict.get(varName.toLowerCase());
         //@TODO Handle better the case no value was returned (item not in the dictionary)
         if (value == null) {
            value = "";
         }
         result += prefix + value;

         // Get new values for inputCopy, idxCopy, idxClose
         inputCopy = inputCopy.substring(idxClose + 1, inputCopy.length());
         idxOpen = inputCopy.indexOf(open);
         idxClose = inputCopy.indexOf(close);
      }
      // Append the end of string (suffix) if any
      result += suffix;
      return result;

   }

   /**
    *
    * @param input a string - possibly with 'tokens' to remove -
    *        where a token is a substring of the form {content} (open, content, close)
    * @param left
    * @param right
    * @return string with placeholders surrounded by 'open' and 'close' removed
    */
   public static String removeEmptyTokens(String input, String open, String close) {
      String result = "";
      String inputCopy = input;
      String prefix, suffix = "";
      int idxOpen = inputCopy.indexOf(open);
      int idxClose = inputCopy.indexOf(close);

      // The case there are no tokens to substitute
      if (idxOpen < 0 || idxClose < 0 || isBlank(input))
         return input;

      while (isNotBlank(inputCopy) && (idxOpen >= 0) && (idxClose > 2)) {
         // Get new values for prefix, suffix and variable name
         prefix = inputCopy.substring(0, idxOpen);
         int start = idxClose + ((idxClose < (inputCopy.length())) ? 0 : 1);
         int end = inputCopy.length() - 1;
         suffix = inputCopy.substring(idxClose + ((idxClose < (inputCopy.length())) ? 1 : 0));
         result += prefix;

         // Get new values for inputCopy, idxCopy, idxClose
         inputCopy = inputCopy.substring(idxClose + 1, inputCopy.length());
         idxOpen = inputCopy.indexOf(open);
         idxClose = inputCopy.indexOf(close);
      }
      // Append the end of string (suffix) if any
      result += suffix;
      return result;

   }

   /**
    * // Takes a screen shot if the driver's capabilities include Screen Shots
    * @param driver        The Web Driver instance
    * @param outputType    Either FILE or Base64
    * @param folder        The folder for images
    * @param filename      The base file name (sans time stamp)
    * @return              Error string starting with ERROR: if failed to create file or the file name that was created
    */
   public static String takeScreenImage (WebDriver driver, String folder, String fileName) {
      String result = "";
      WebDriver theDriver = driver;

      if (!(theDriver instanceof WebDriver)) theDriver = DDTestRunner.getDriver();
      if (!(theDriver instanceof WebDriver)) {
         return "ERROR: No Web Driver found at this step!";
      }

      if(!((HasCapabilities)theDriver).getCapabilities().is(CapabilityType.TAKES_SCREENSHOT)) {
         return "ERROR: Driver " + Util.sq(theDriver.toString()) + " has no TAKES_SCREENSHOT capabilities.  Screen shot not taken";
      }

      String imagesFolder = (isBlank(folder)) ? DDTestRunner.getReporter().sessionImagesFolderName() : folder;

      try {
         File testTempDir = new File(imagesFolder);
         if(testTempDir.exists()){
            if(!testTempDir.isDirectory()){
               return "ERROR: Image path exists but is not a directory";
            }
         }else{
            testTempDir.mkdirs();
         }
      }
      catch (Exception e ) {
         return "ERROR: File operation (mkdir) failed. " + e.getCause();
      }

      String actualFileName = fileName + " - " + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".png";

      // create screenshot using a casted driver
      TakesScreenshot snapper = (TakesScreenshot)theDriver;

      if (DDTSettings.Settings().isLocal())  {
         File tempImageFile = snapper.getScreenshotAs(OutputType.FILE);
         if (tempImageFile.length() < (1L)) {
            return "ERROR: Failed to take screen shot on remote driver";
         }

         File tmpFile = new File(imagesFolder, actualFileName);      // testTempImage

         // move screenshot to our local store
         try {
            FileUtils.moveFile(tempImageFile, tmpFile);
         } catch (Exception e) {
            return "ERROR: Failed to move tmp file to image file. " + Util.sq(tmpFile.getAbsolutePath()) + " " + e.getCause().toString();
         }

         if (tmpFile.length() < 1L) {
            return "ERROR: Failed to move tmp file to image file. " + Util.sq(tmpFile.getAbsolutePath());
         }

         result = tmpFile.getAbsolutePath();
      }
      else {
         // Create Base64 screen shot file on the remote driver and store it locally
         String tempImageFile = snapper.getScreenshotAs(OutputType.BASE64);
         if (tempImageFile.length() < (1L)) {
            return "ERROR: Failed to take screen shot on remote driver";
         }

         Base64 decoder = new Base64();
         byte[] imgBytes = (byte[]) decoder.decode(tempImageFile);

         File tmpFile = new File(imagesFolder, actualFileName);

         FileOutputStream osf = null;
         try {
            osf = new FileOutputStream(tmpFile);
            osf.write(imgBytes);
            osf.flush();
         } catch (Exception e) {
            return "ERROR: Failed to create File Output Stream " + e.getCause();
         }

         if (tmpFile.length() < 1L) {
            return "ERROR: File created from  File Output Stream is empty!";
         }

         result = tmpFile.getAbsolutePath();
      }

      return result;
   }

   public static void fileWrite(String fileName, String text) {
      File output = new File (fileName);
      try {
         FileWriter fw = new FileWriter(output);
         fw.append(text);
         fw.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Trim white spaces from the originalString
    * @param originalString
    * @return
    */
   public static String stripWhiteSpace(String originalString) {
      String result = "";
      if (isNotBlank(originalString)) {
         int len = originalString.length();
         char c;
         StringBuilder sb = new StringBuilder("");
         for (int i = 0; i < len; i++) {
            c = originalString.charAt(i);
            if (!Character.isWhitespace(c))
               sb.append(c);
         }
         result = sb.toString();
      }
      return result;
   }

   public static File setupReportFolder(String path) {
      //String s = File.separator;
      // String ourTestTempPathName = System.getProperty("user.dir") + String.format("%ssrc%stest%sresources%stemp%sscreenshots",s,s,s,s,s);

      File testTempDir = new File(path);
      if(testTempDir.exists()){
         if(!testTempDir.isDirectory()){
            System.out.println("Path " + Util.sq(path) + " exists but is not a directory");
            System.exit(2);
            // fail("Path " + Util.sq(path) + " exists but is not a directory");
         }
      }
      else {
         testTempDir.mkdirs();
      }

      return testTempDir;
   }

   /**
    * Sets up reporting session folders for a new reporting session.
    * The folders are relative to (and below) the project's Reports folder and have the following structure:
    * {Reports}
    *    {YYYYMMDD}_NNN - where NNN is an incremental sequence number
    *       {folderNames[0]}
    *       {folderNames[1]}
    *       ...
    * @param folderNames - an array of folders to create below the daily session folder
    * @return an array of File objects
    */
   public static String[] setupReportingSessionFolders(String[] folderNames) {
      String reportsFolder = DDTSettings.Settings().reportsFolder();
      String dailyFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
      String baseFolder = reportsFolder + dailyFolder; //
      int sessionNumber = 1;

      boolean folderExists = true;
      String sessionFolderName = "";
      while (folderExists) {
         // The base folder name with appended 3 digits 0 padded sequence number (assuming no more than a 1,000 test sessions / day will occur)
         sessionFolderName = baseFolder + "_" + String.format("%03d", sessionNumber);
         File testFolder = new File(sessionFolderName);
         // If session folder already exists, advance the session counter, else, create the base folder then the Images and Steps folders.
         if (testFolder.exists() && testFolder.isDirectory())
            sessionNumber++;
         else {
            testFolder.mkdirs();
            folderExists = false;
         }
      }

      // Iterate over the names of the folders to create and create those...
      // Rely on caller not to 1) Replicate names 2) Provide empty strings, 3) Provide illegal strings (containing ":" "\" etc.)
      int n = folderNames.length;
      String[] result = new String[n];
      for (int i = 0; i < n; i++) {
         String subFolderName = sessionFolderName + File.separator + folderNames[i];
         File subFolder = new File(subFolderName);
         subFolder.mkdirs();
         result[i] = subFolderName;
      }
      return result;
   }

}
