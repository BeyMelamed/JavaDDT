import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 07/02/14
 * Time: 2:22 PM
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
 * Description - Provides test items from an Excel (.xls) spreadsheet.
 *
 * NOTE: This class is one of several test item strings provider classes
 *       Each of the item providers' item can use a newTest action specifying any other test items provider
 *
 * History
 * When         |Who      |What
 * =============|=========|====================================
 * 07/02/14     |Bey      |Initial Version
 * =============|=========|====================================
 */
public class InlineTestStringsProvider extends TestStringsProvider {

   // Three constructors are available to get things going

   // No input specs - input specs will be provided later
   public InlineTestStringsProvider() {
   }

   // Test String Specifier object
   public InlineTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
      setTestStringsProviderSpecs(inputSpecs);
      addError(getTestStringsProviderSpecs().getErrors());
   }

   // String array input specs
   public InlineTestStringsProvider(String[] inputSpecs) {
      setTestStringsProviderSpecs(inputSpecs);
      addError(getTestStringsProviderSpecs().getErrors());
   }

   // (delimited) String input specs from which the specs (String[]) array will be constructed
   public InlineTestStringsProvider(String inputSpecs) {
      setTestStringsProviderSpecs(inputSpecs);
      addError(getTestStringsProviderSpecs().getErrors());
   }

   public boolean isSetupValid() {
      return getTestStringsProviderSpecs().isValid();
   }

   public static boolean isFileOrFolderName(String s) {
      if (s.contains(File.separator))
         return true;
      if (s.contains(":"))
         return true;
      return false;
   }

   public static Class<?> loadInlineClassIfNeeded(String className) {
      try {
         // If the class can be loaded, just return it.
         Class c = Class.forName(className);
         return c;
      }
      catch (Exception e) {
         // Initialize the class loader with a parent that is the InlineTestStringProvider class
         DDTClassLoader ddtClassLoader = new DDTClassLoader(InlineTestStringsProvider.class.getClassLoader());

         try {
            Class aClass = ddtClassLoader.loadClass(className);
            if (!isBlank(ddtClassLoader.getErrors()))
               System.out.println("Class " + sq(className) + " Not loaded due to errors: " + ddtClassLoader.getErrors());
            else
               System.out.println("Class " + sq(className) + " loaded.");
            return aClass;
         } catch (ClassNotFoundException notFoundException) {
            System.out.println("Class " + sq(className) + " Not Loaded due to errors:");
            notFoundException.printStackTrace();
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   @Override
   void provideStrings() throws Exception {

      // Setup errors are set by the constructor - if any.
      if (!isBlank(getErrors()) || !isSetupValid())
         return;

      String testItemsGeneratorClassName = getClassName();
      String testItemsGeneratorMethod = getMethodName();

      // Invoke a method in an 'inline' items provide class
      // This code is here by design in order to keep the inline test item provider with no dependency on any of the main classes (such as TestItem)
      try {
         Class c = loadInlineClassIfNeeded(testItemsGeneratorClassName);
         try {
            Object o = c.newInstance();
            ArrayList<String[]> testData = new ArrayList <>();
            Method m = c.getDeclaredMethod(testItemsGeneratorMethod, testData.getClass());
            m.setAccessible(true);
            // Invoke the method on the instance with testData as a parameter to be created and returned
            m.invoke(o, testData);
            stringifyTestItems(testData);
         }
         catch (InvocationTargetException e) {
            throw (new Exception("InvocationTargetException generated by invoking Inline Items Generator Class: " + sq(testItemsGeneratorClassName) + " / Method: " + sq(testItemsGeneratorMethod)));
         }
         catch (Exception e) {
            throw (new Exception("Exception generated by invoking Inline Items Generator Class: " + sq(testItemsGeneratorClassName) + " / Method: " + sq(testItemsGeneratorMethod)));
         }
      }
      catch (Exception e) {
         System.out.println("failed to get class for class named: " + sq(testItemsGeneratorClassName));
      }
   }
}
