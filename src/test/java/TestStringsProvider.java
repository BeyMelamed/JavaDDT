import java.util.ArrayList;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 07/01/14
 * Time: 2:06 PM
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
 * Description - An abstract class providing an array of strings from which test items are created for a DDTestRunner to iterate over and process
 *               Should be extended by particular test items providers (Excel, XML, Html, etc.)
 *
 * NOTE: This class is the root (abstract) of several test items strings provider classes
 *       Each of the item providers' item can use a newTest action specifying any other test items provider
 *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
 *
 * History
 * When         |Who      |What
 * =============|=========|====================================
 * 07/01/14     |Bey      |Initial Version
 * =============|=========|====================================
 */
public abstract class TestStringsProvider {
   private ArrayList<String[]> dataList = new ArrayList<String[]>();
   private String[][] dataStrings = new String[0][0];
   private String errors = "";
   private TestStringsProviderSpecs testStringsProviderSpecs;
   private Throwable exception;

   public static String[][] provideTestStrings(String[] args, String baseDataFolder) {
      TestStringsProviderSpecs specs = new TestStringsProviderSpecs(args);
      return buildTestStrings(specs, baseDataFolder);
   }

   public static String[][] provideTestStrings(String delimitedString, String baseDataFolder) {

      TestStringsProviderSpecs specs = new TestStringsProviderSpecs(delimitedString);
      return buildTestStrings(specs, baseDataFolder);

   }

   public static String[][] provideTestStrings(TestStringsProviderSpecs specs, String baseDataFolder) {
      return buildTestStrings(specs, baseDataFolder);
   }

   // The methods sq, dq below are purposefully duplicating method names in Util in order to enable usage of these methods in external subclasses without having to duplicate the Util class
   public static String sq(String str) {
      return "'" + str + "'";
   }

   public static String dq(String str) {
      return "\"" + str + "\"";
   }

   public static String[][] buildTestStrings(TestStringsProviderSpecs specs, String baseDataFolder) {

      specs.ensureFilePathIsValid(baseDataFolder);
      String inputType = specs.getSourceType();
      String[][] result = new String[0][];

      TestStringsProvider stringsProvider = null;

      if (inputType.equalsIgnoreCase("file")) {

         String fileName = specs.getFileName().toLowerCase();
         if (specs.getArgs().length > 1 && fileName.endsWith(".xml")) {
            stringsProvider = new FileTestStringsProvider.XMLTestStringsProvider(specs);
         }
         else if (specs.getArgs().length > 1 && fileName.endsWith(".html")) {
            stringsProvider = new FileTestStringsProvider.HtmlTestStringsProvider(specs);
         }
         else if (specs.getArgs().length == 3 && fileName.endsWith(".xls")) {
            stringsProvider = new FileTestStringsProvider.ExcelTestStringsProvider(specs);
         }
         else if (specs.getArgs().length == 3 && fileName.endsWith(".xlsx")) {
            stringsProvider = new FileTestStringsProvider.ExcelXSSFTestStringsProvider(specs);
         }
         else {
            System.out.println("Invalid / Unsupported Input File Type: " + sq(fileName));
            return result;
         }
      }
      else if(inputType.equalsIgnoreCase("inline")) {
         stringsProvider = new InlineTestStringsProvider(specs);
      }
      else {
         System.out.println("Invalid Test Items Provider Type: " + sq(inputType) + " - Aborted");
         return result;
      }

      try {
         if (stringsProvider instanceof TestStringsProvider) {
            stringsProvider.provideStrings();
            result = stringsProvider.getDataStrings();
         }
      }
      catch (Exception e) {
         throw new Exception(e.getMessage().toString());
      }
      finally {
         return result;
      }
   }

   public void setDataStrings(String[][] array) {
      dataStrings = array;
   }

   public String[][] getDataStrings() {
      if (dataStrings == null)
         setDataStrings(new String[0][]);
      return dataStrings;
   }

   public void setTestStringsProviderSpecs(TestStringsProviderSpecs specs) {
      testStringsProviderSpecs = specs;
   }

   public void setTestStringsProviderSpecs(String[] specs) {
      testStringsProviderSpecs = new TestStringsProviderSpecs(specs);
   }

   public void setTestStringsProviderSpecs(String specs) {
      testStringsProviderSpecs = new TestStringsProviderSpecs(specs);
   }

   public TestStringsProviderSpecs getTestStringsProviderSpecs() {
      return testStringsProviderSpecs;
   }

   public void setException(Throwable value) {
      exception = value;
   }

   public Throwable getException() {
      return exception;
   }

   public void stringifyTestItems(ArrayList<String[]> itemsList) {
      setDataStrings(new String[itemsList.size()][8]);
      int i = 0;
      Iterator itr = itemsList.iterator();
      while (itr.hasNext()) {
         getDataStrings()[i] = (String[]) itr.next();
         i++;
      }
   }

   public void setErrors(String value) {
      errors = value;
   }

   public String getErrors() {
      if (getException() instanceof Throwable)
         return getException().getMessage().toString();
      if (errors == null)
         setErrors("");
      return errors;
   }

   public String getClassName() {
      return getTestStringsProviderSpecs().getClassName();
   }

   public String getMethodName() {
      return getTestStringsProviderSpecs().getItemsContainerName();
   }

   public boolean isSetupValid() {
      if (!getTestStringsProviderSpecs().isSetupValid())
         return false;
      return true;
   }

   /**
    * This is the abstract method extending class have to implement
    * @param inputSpecs
    * @return
    */
   abstract void provideStrings() throws Throwable;

}
