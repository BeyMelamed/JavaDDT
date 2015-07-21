import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebElement;

import java.security.InvalidKeyException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 20/26/14
 * Time: 6:55 PM
 * Selenium Based Automation
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
 * Class containing a HashMap of <String, Object> used by the test engine
 * History
 * When        |Who      |What
 * ============|=========|============================================================================================================
 * 10/26/14    |Bey      |Initial Version
 * ============|=========|============================================================================================================
 */

@SuppressWarnings("unchecked")
public class DDTTestContext extends Hashtable {

   public DDTTestContext() {

   }

   /**
    * Construct an instance from a delimited string of the format key1=value1;key2=value2
    * @param delimitedString - the input (delimited) string
    * @param delim - the delimited string separating the key=value pairs - the default is ';'.
    *              If the actual delimiter is not the default then delimitedString has the first character that non-standard delim
    * @param validDelims - a coma-delimited string of valid delimiter - used only in the rare case of using non-standard delim
    */
   public DDTTestContext(String delimitedString, String delim, String validDelims) {

      // Parameters sanity check1 - all strings are not blank
      if (isBlank(delimitedString)) {
         return;
      }

      // Parameters sanity check2 - at least one instance of <key> "=" <value> exists
      if (!delimitedString.contains("=")) {
         return;
      }

      String actualDelim = delim;
      String actualStr = delimitedString;

      // Determine if the caller uses non standard delimiter.
      if (validDelims.contains(delimitedString.substring(0,1))) {
         actualDelim = delimitedString.substring(0,1);
         actualStr = delimitedString.substring(1);
      }

      String[] a1 = actualStr.split(actualDelim);
      for (int i = 0 ; i < a1.length; i++) {
         try {
            int idx = a1[i].indexOf("=");
            if (idx < 0) {
               throw new InvalidKeyException("'=' Delimiter not found in item " + (i + 1) + " of " + actualStr);
            }
            String key = a1[i].substring(0, idx);
            String value = a1[i].substring(idx + 1);

            if (isBlank(key))  {
               throw new InvalidKeyException ("Empty key value in item " + (i + 1) + " of " + actualStr);
            }
            if (null != this.get(key.toLowerCase())) {
               throw new InvalidKeyException ("Repeated key value of " + key + " in item " + (i + 1) + " of " + actualStr);
            }
            // store next & unique value (a2[1]) in hashtable using key of a2[0]
            this.put(key.toLowerCase(), value);
         } // try
         catch (InvalidKeyException e ) {
            System.out.println(e.getMessage());
            return;
         }
      } // for loop
   } // Parameterized Constructor

   /**
    * If so indicated, save the value in the test session variables amp
    */
   public void saveVariableIfNeeded(Object value) {
      String key = getString("SaveAs");
      if (!isBlank(key)) {
         DDTTestContext tmp = new DDTTestContext();
         tmp.setProperty(key, value);
         String blurb = Util.populateDictionaryFromHashtable(this, DDTTestRunner.getVarsMap());
      }
   }

   public void addComment(String value) {
      if (isNotBlank(value)) {
         String s = (String)this.getProperty("comments");
         s = Util.append(s, value, ", ");
         this.setProperty("Comments", s);
      }
   }

   public void addError(String value) {
      if (isNotBlank(value)) {
         String s = (String)this.getProperty("errors");
         s = Util.append(s, value, ", ");
         this.setProperty("errors", s);
      }
   }

   public void removeErrors() {
      try {
         remove("errors");
      }
      catch (Exception e) {
      }
   }

   public boolean containErrors() {
      return !StringUtils.isBlank(getString("errors"));
   }

   public Object getProperty(String key) {
      String s = key.toLowerCase();
      if (null != this.get(s))
         return this.get(s);
      return null;
   }

   public String getString(String key) {
      String result = (String) this.getProperty(key);
      if (StringUtils.isBlank(result))
         result = "";
      return result;
   }

   public int getInt(String key) {

      try {
         return Integer.valueOf(this.getProperty(key.toLowerCase()).toString());
      }
      catch (Exception e) {
         return 0;
      }
   }

   public Long getLong(String key) {
      return (Long) this.getProperty(key);
   }

   public Float getFloat(String key) {
      return (Float) this.getProperty(key);
   }

   public Double getDouble(String key) {
      return (Double) this.getProperty(key);
   }

   public Date getDate(String key) {
      return (Date) this.getProperty(key);
   }

   public boolean getBoolean(String key) {
      if (!this.containsKey(key.toLowerCase()))
         return false;
      return Util.asBoolean(this.getString(key.toLowerCase()));
   }

   public boolean getStringAsBoolean(String key) {
      String s = this.getString(key);
      if (null == s)
         return false;
      if (",yes,true,1,-1,".contains("," + s.toLowerCase() + ","))
         return true;
      return false;
   }

   public int getStringAsInteger(String key) {
      String s = this.getString(key);
      if (isBlank(s))
         return 0;
      try {
         return Integer.valueOf(s);
      }
      catch (Exception e)  {

      }
      finally {return 0;}
   }

   public Long getStringAsLong(String key) {
      Long result = 0L;
      String s = this.getString(key.toLowerCase());
      if (isBlank(s))
         return result;
      try {
         result = Long.parseLong(s);
      }
      catch (Exception e)  {

      }
      finally {return result;}
   }

   public Exception getException(String key) {
      return (Exception) this.getProperty(key.toLowerCase());
   }

   public WebElement getElement() {
      return (WebElement) getProperty("element");
   }

   /**
    * This is an item to be skipped if both conditions prevail:
    * 1. There is a string variable in the variables map with the key of "skipTokens" (a comma delimited string)
    * 2. There is a String property that (enclosed in commas) is contained in the string variable found above
    * @return
    */
   public boolean isSkipItem() {
      String skipTokens = DDTTestRunner.getVarsMap().getString("skipTokens");
      if (isBlank(skipTokens))
         return false;
      String skipToken = getString("skipToken");
      if (isBlank(skipToken))
         return false;
      return Util.surroundedBy(",", skipTokens.toLowerCase(), ",").contains(skipToken.toLowerCase());
   }

   /**
    *
    * @return a boolean indicating whether the property whose key is "active" is a string with boolean value of true
    */
   public boolean isActiveItem() {
      String activeFlag = getString("active");
      return Util.asBoolean(activeFlag);
   }

   /**
    * Indicates whether this is an active test context instance.
    * @return boolean indicating whether or not this is an active test context
    */
   public boolean isActive() {
      // If testItem exists - use testItem to determine whether or not this context is 'Active'
      TestItem item = (TestItem) getProperty("testItem");
      if (item instanceof TestItem) {
         return item.isActive();
      }
      // In lieu of testItem, if the "active" property is false, this is not an active item.
      if (!isActiveItem())
         return false;
      // If there is a property "skipItem" with an inactive indication, this is not an active item.
      if (isSkipItem())
         return false;

      // This is an active test context
      return true;
   }

   public void setProperty(String key, Object value) {
      if (value == null)
         return; // cannot (exception is thrown when trying to put null

      String s = key.toLowerCase();
      if (null != this.get(s))
         this.remove(s);
      this.put(s, value);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("");
      Set<Map.Entry<String, Object>> entries = entrySet();
      String key;
      Object value;
      String prefix = "";
      for (Map.Entry<String, Object> entry : entries) {
         key = entry.getKey();
         Object tmp = entry.getValue();
         if (tmp instanceof String) {
            sb.append(prefix + key + ": " + tmp);
            prefix = ", ";
         }
      }
      return sb.toString();
   }

}
