import jxl.Sheet;
import jxl.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 1/20/14
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
 * Description - An abstract class providing an array of test items for a DDTestRunner to iterate over and process
 *               Should be extended by particular test items providers (Excel, XML, Html, etc.)
 *
 * NOTE: This class is the root (abstract) of several test items provider classes
 *       Each of the item providers' item can use a newTest action specifying any other test items provider
 *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
 *
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 1/20/14     |Bey      |Initial Version
 * ============|=========|====================================
 */
public abstract class TestItemsProvider {

   public static TestItem[] testItems;
   private ArrayList<String[]> dataList = new ArrayList<String[]>();
   private String[][] dataStrings = new String[0][0];

   private Exception exception;

   public static TestItemsProvider providerWithItems(String[] args) throws Exception {

      String inputType = args[0];

      TestItemsProvider provider = null;
      if (inputType.equalsIgnoreCase("file")) {
         if (args.length > 1 && args[1].toLowerCase().endsWith(".xml")) {
            provider = new XMLTestItemsProvider();
            args = new String[] {args[0], args[1], ""};
         }
         else if (args.length > 1 && (args[1].toLowerCase().endsWith(".html"))) {
            provider = new HtmlTestItemsProvider();
            args = new String[] {args[0], args[1], ""};
         }
         else if (args.length == 3 && (args[1].toLowerCase().endsWith(".xls"))) {
            provider = new ExcelTestItemsProvider();
         }
         else if (args.length == 3 && (args[1].toLowerCase().endsWith(".xlsx"))) {
            provider = new ExcelXSSFTestItemsProvider();
         }
         else {
            System.out.println("Invalid / Unsupported Input File Type: " + Util.dq(args[1]));
            return null;
         }
      }
      else if(inputType.equalsIgnoreCase("inline")) {
         provider = new InlineTestItemsProvider();
      }
      else {
         System.out.println("Invalid Test Items Provider Type: " + Util.dq(inputType) + " - Aborted");
         return null;
      }

      try {
         if (provider instanceof TestItemsProvider) {
            provider.buildItems(new String[]{args[1], args[2]});
            provider.assembleTestItems();
            return provider;
         }
      }
      catch (Exception e) {
          throw new Exception(e.getMessage().toString());
       }
      return null;
   }

   public void setDataList(ArrayList<String[]> value) {
      dataList = value;
   }

   public void setTestItems(TestItem[] value) {
      testItems = value;
   }

   public TestItem[] getTestItems() {
     if (testItems == null)
        setTestItems(new TestItem[0]);
      return testItems;
   }

   public ArrayList<String[]> getDataList() {
      if (dataList == null)
         setDataList(new ArrayList<String[]>());
      return dataList;
   }

   public void setDataStrings(String[][] value) {
      dataStrings = value;
   }

   public String[][] getDataStrings() {
      if (dataStrings == null)
         setDataStrings(new String[0][0]);
      return dataStrings;
   }

   abstract void buildItems(String[] args) throws Exception;

   /**
    *
    * @return An array of TestItem[] instances from the provider's list of String[] array
    */
   private TestItem[] assembleTestItems() {
      stringifyTestItems();
      int n = dataStrings.length;
      testItems = new TestItem[n];
      for (int i = 0; i < n; i++) {
         String[] thisData = dataStrings[i];
         testItems[i] = new TestItem(thisData[0], thisData[1], thisData[2], thisData[3], thisData[4], thisData[5], thisData[6], thisData[7]);
      }
      return testItems;
   }

   private void stringifyTestItems() {
      setDataStrings(new String[dataList.size()][8]);
      int i = 0;
      Iterator itr = dataList.iterator();
      while (itr.hasNext()) {
         getDataStrings()[i] = (String[]) itr.next();
         i++;
      }
   }

   public void addItem(String id, String action, String locType, String locSpecs, String qryFunction, String active, String data, String description) {
      String[] thisItem = {id, action, locType, locSpecs, qryFunction, active, data, description};
      dataList.add(thisItem);
   }

   public void setException (Exception value) {
      exception = value;
   }

   public Exception getException() {
      return exception;
   }

   public DDTSettings settings() {
      return DDTSettings.Settings();
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 5/31/14
    * Time: 16:30 PM
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
    * Description - A class providing an array of test items for a DDTestRunner to iterate over and process where the items reside in some file.
    *
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 5/31/14     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static abstract class FileTestItemsProvider extends TestItemsProvider {

      private String fileName;

      public FileTestItemsProvider() {

      }

      public void setFileName(String value) {
         fileName = value;
      }

      public String getFileName() {
         if (isBlank(fileName)) {
            setFileName(settings().inputFileName());
         }

         if (!fileName.contains("/") && !fileName.contains("\\") && (!isBlank(fileName))) {
            fileName = DDTSettings.prefixFileNameWithPath(fileName, "Data");
            setFileName(fileName);
         }

         return fileName;
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 1/20/14
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
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 1/20/14     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class ExcelTestItemsProvider extends FileTestItemsProvider {
      private String worksheetName;

      public ExcelTestItemsProvider() {

      }

      public void setWorksheetName(String value) {
         worksheetName = value;
      }

      public String getWorksheetName() {
         if (isBlank(worksheetName)) {
            setWorksheetName(settings().inputWorksheetName());
         }
         return worksheetName;
      }

      @Override
      void buildItems(String[] args)
            throws jxl.read.biff.BiffException, ArrayIndexOutOfBoundsException, IOException {

         Workbook wb = null;
         if (args.length >1) {
            setFileName(args[0]);
            setWorksheetName(args[1]);
         }

         String inputFile = getFileName();

         int nRows = 0;
         if (isBlank(inputFile) || isBlank(getWorksheetName()))  {
            System.out.println("Either File or Worksheet name(s) is / are empty - please explore");
         }
         else try {
            File inputWorkbook = new File(inputFile);
            String[] thisItem = new String[8];
            wb = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = wb.getSheet(getWorksheetName());
            nRows = sheet.getRows();
            // Consider rows 2 and on - the first one is column titles
            for (int row = 1; row < nRows; row++) {

               for (int j = 0; j < 8; j++) {
                  thisItem[j] = sheet.getCell(j, row).getContents();
               }
               addItem(thisItem[0], thisItem[1], thisItem[2], thisItem[3], thisItem[4], thisItem[5], thisItem[6], thisItem[7]);
            }
         } catch (IOException e) {
            setException(e);
            throw new java.io.IOException("Failed to get test items from worksheet named: " + Util.sq(getWorksheetName()) + " in file: " + getFileName());
         }
         finally {
            if (wb instanceof Workbook) {
               wb.close();
               wb = null;
            }
         }
         System.out.println(String.valueOf(nRows-1) + " test items found on worksheet " + getWorksheetName() + " in file: " + getFileName());

      }

   }

   /**
    * Created by BeyMelamed on 1/29/14.
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
    * TestItems provider for MS .xlsx files
    *
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
    *
    * When      |Who            |What
    * ==========|===============|========================================================
    * 01/29/14  |Bey            |Initial Version
    * ==========|===============|========================================================
    */
   public static class ExcelXSSFTestItemsProvider extends ExcelTestItemsProvider {
      @Override
      void buildItems(String[] args) throws IOException {

         XSSFWorkbook workbook = null;
         int size = 0;
         if (args.length >1) {
            setFileName(args[0]);
            setWorksheetName(args[1]);
         }

         String inputFile = getFileName();

         if (isBlank(inputFile) || isBlank(getWorksheetName()))  {
            System.out.println("Either File or Worksheet name(s) is / are empty - please explore");
         }
         else try {
            File inputWorkbook = new File(inputFile);

            FileInputStream fis = new FileInputStream(inputWorkbook);
            workbook = new XSSFWorkbook(fis);
            XSSFSheet worksheet = workbook.getSheet(getWorksheetName());

            size = worksheet.getLastRowNum();
            String[] cellData = new String[8];
            for (int row = 1; row <= size; row++) {
               XSSFRow xssfRow = worksheet.getRow(row);

               for (int i = 0; i < 8; i++) {
                  Cell cell = xssfRow.getCell(i);
                  if (cell == null) {
                     cellData[i] = "";
                     continue;
                  }
                  cell.setCellType(Cell.CELL_TYPE_STRING);
                  cellData[i] = ((cell == null) ?  "" : cell.toString());
               }
               addItem(cellData[0], cellData[1], cellData[2], cellData[3], cellData[4], cellData[5], cellData[6], cellData[7]);
            }
         }
         catch (IOException e) {
            setException(e);
            System.out.println(e.getMessage().toString());
         }
         finally {
            workbook = null;
         }
         System.out.println(String.valueOf(size) + " test items found on worksheet " + getWorksheetName() + " in file: " + getFileName());

      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 5/31/14
    * Time: 16:30 PM
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
    * Description - A class providing an array of test items for a DDTestRunner to iterate over and process where the items are generated from 'inline' methods
    * that return an instance of TestItems.
    * When the Inline TestItems provider is used, the inputFileName is "Inline" (ignoreCase) and the worksheet name is a method name in the InlineItemsProvider class
    *
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure names of classes is spelled correctly
    *
    * History
    * When         |Who      |What
    * =============|=========|====================================
    * 06/15/14     |Bey      |Initial Version
    * =============|=========|====================================
    */
   public static class InlineTestItemsProvider extends TestItemsProvider {

      public InlineTestItemsProvider() {

      }

      @Override
      void buildItems(String[] args) throws Exception {

         String testItemsGeneratorClassName = "";
         String testItemsGeneratorMethod = "";

         if (args.length > 1) {
            testItemsGeneratorClassName = args[0];
            testItemsGeneratorMethod = args[1];
         }

         if (isBlank(testItemsGeneratorClassName))
            testItemsGeneratorClassName = DDTSettings.Settings().testItemsGeneratingClassName();

         if (isBlank(testItemsGeneratorClassName))
            throw (new Exception("TestItems Generator Class not specified! - Please explore"));

         if (isBlank(testItemsGeneratorMethod))
            testItemsGeneratorMethod = DDTSettings.Settings().testItemsGeneratingMethodName();

         if (isBlank(testItemsGeneratorMethod))
            throw (new Exception("TestItems Generator Method not specified! - Please explore"));

         // Invoke a method in an 'inline' items provide class
         // This code is here by design in order to keep the inline test item provider with no dependency on any of the main classes (such as TestItem)
         try {
            Class c = Class.forName(testItemsGeneratorClassName);
            try {
               Object o = c.newInstance();
               Method m = c.getDeclaredMethod(testItemsGeneratorMethod);
               m.setAccessible(true);
               ArrayList<String[]> testData = (ArrayList<String[]>)  m.invoke(o);
               setDataList(testData);
            }
            catch (InvocationTargetException e) {
               throw (new Exception("InvocationTargetException generated by invoking Inline Items Generator Class: " + Util.sq(testItemsGeneratorClassName) + " / Method: " + Util.sq(testItemsGeneratorMethod)));
            }
            catch (Exception e) {
               throw (new Exception("Exception generated by invoking Inline Items Generator Class: " + Util.sq(testItemsGeneratorClassName) + " / Method: " + Util.sq(testItemsGeneratorMethod)));
            }
         }
         catch (Exception e) {
            System.out.println("failed to get class for class named: " + Util.sq(testItemsGeneratorClassName));
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 1/20/14
    * Time: 5:16 PM
    * Selenium Based Java Data Driven Test Automation
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
    * Description  - Gets test items to process from an XML file
    *
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 01/20/14    |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class XMLTestItemsProvider extends FileTestItemsProvider {

      public XMLTestItemsProvider() {

      }

      @Override
      void buildItems(String[] args)
            throws XMLStreamException, IOException {

         if (args.length > 0) {
            setFileName(args[0]);
         }

         String inputFile = getFileName();

         if (isBlank(inputFile))  {
            System.out.println("Failed to get test items from XML File (blank parameter) - please explore");
         }
         else try {

            DDTestParser reader = new DDTestParser();
            reader.buildTestItems(getFileName());
            setException(reader.getException());

            if (getException() instanceof Exception)
               throw new Exception("Exception encountered while reading test items from XML file: " + getFileName());

         } catch (Exception e) {
            setException(e);
            throw new java.io.IOException("Failed to get test items from XML file: " + getFileName());
         }
      }

      /**
       * Created with IntelliJ IDEA.
       * User: Avraham (Bey) Melamed
       * Date: 1/20/14
       * Time: 5:16 PM
       * Selenium Based Java Data Driven Test Automation
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
       * Description  - Parses XML file to extract test items from it
       *
       * NOTE: This class is one of several test items provider classes
       *       Each of the item providers' item can use a newTest action specifying any other test items provider
       *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
       *
       * History
       * When        |Who      |What
       * ============|=========|====================================
       * 01/20/14    |Bey      |Initial Version
       * ============|=========|====================================
       */
      public class DDTestParser {
         static final String TESTITEM = "TestItem";
         static final String ID = "Id";
         static final String ACTION = "Action";
         static final String LOCTYPE = "LocType";
         static final String LOCSPECS = "LocSpecs";
         static final String QRYFUNCTION = "QryFunction";
         static final String ACTIVE = "Active";
         static final String DATA = "Data";
         static final String DESCRIPTION = "Description";

         private Exception exception;

         public void setException (Exception value) {
            exception = value;
         }

         public Exception getException() {
            return exception;
         }

         public int count = 0;

         /**
          * Build the instances ArrayList<String[]>
          * @param inputFile
          */
         public void buildTestItems(String inputFile) {
            try {
               // Create a new XMLInputFactory
               XMLInputFactory inputFactory = XMLInputFactory.newInstance();
               // Setup a new eventReader
               InputStream in = new FileInputStream(inputFile);
               XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

               String id = "";
               String action = "";
               String locType = "";
               String locSpecs = "";
               String qryFunction = "";
               String active = "";
               String data = "";
               String description = "";
               while (eventReader.hasNext()) {
                  XMLEvent event = eventReader.nextEvent();

                  if (event.isStartElement()) {
                     StartElement startElement = event.asStartElement();
                     // If we have an item element, we create a new item
                     if (startElement.getName().getLocalPart() == (TESTITEM)) {
                        // We read the attributes from this tag and add the date
                        // attribute to our object
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                           Attribute attribute = attributes.next();
                           switch (attribute.getName().toString()) {
                              case ID : id = attribute.getValue() ; break;
                              case ACTION : action = attribute.getValue() ; break;
                              case LOCTYPE : locType = attribute.getValue() ; break;
                              case LOCSPECS : locSpecs = attribute.getValue() ; break;
                              case QRYFUNCTION : qryFunction = attribute.getValue() ; break;
                              case ACTIVE: active = attribute.getValue() ; break;
                              case DATA : data = attribute.getValue() ; break;
                              case DESCRIPTION : description = attribute.getValue() ; break;
                           }
                        }
                        addItem(id, action, locType, locSpecs, qryFunction, active, data, description);
                        count++;
                     }
                     else
                        continue;
                  }
                  // Upon reaching the end of an item - skip to the next
                  if (event.isEndElement()) {
                     EndElement endElement = event.asEndElement();
                  }
               }
            } catch (FileNotFoundException e) {
               setException(e);
            } catch (XMLStreamException e) {
               setException(e);
            }
         }

      }

   }

   /**
    * Created by BeyMelamed on 1/29/14.
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
    * TestItems provider for html files
    *
    * NOTE: This class is one of several test items provider classes
    *       Each of the item providers' item can use a newTest action specifying any other test items provider
    *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
    *
    * When      |Who            |What
    * ==========|===============|========================================================
    * 05/28/14  |Bey            |Initial Version
    * ==========|===============|========================================================
    */

   public static class HtmlTestItemsProvider extends FileTestItemsProvider {

      public HtmlTestItemsProvider() {

      }

      @Override
      /**
       * Gets test Items from an html file that is structured in a proprietary way...
       * Here is an example of a file with two test steps
       *
       <!DOCTYPE html>
       <html>
          <head lang="en">
             <meta charset="UTF-16">
             <title>DDT Demo Root</title>
          </head>
          <body>
             <table>
                <tbody>
                   <tr>
                      <td class="id">DDTDemo01</td>
                      <td class="action">newTest</td>
                      <td class="locType"></td>
                      <td class="locSpecs"></td>
                      <td class="qryFunction"></td>
                      <td class="active"></td>
                      <td class="data">InputSpecs=File:DDTRoot.xls:Calculate</td>
                      <td class="description">Run the Calculator tests</td>
                   </tr>
                   <tr>
                      <td class="id">DDTDemo02</td>
                      <td class="action">newTest</td>
                      <td class="locType"></td>
                      <td class="locSpecs"></td>
                      <td class="qryFunction"></td>
                      <td class="active"></td>
                      <td class="data">InputSpecs=File:DDTRoot.xls:ChainingFinders</td>
                      <td class="description">Run the Chaining Finders tests</td>
                   </tr>
                </tbody>
             </table>
          </body>
       </html>
       * Note that class is used to identify properties as 'class' attribute does not have to be unique

       */
      void buildItems(String[] args)
            throws ArrayIndexOutOfBoundsException, IOException {

         String id = "";
         String action = "";
         String locType = "";
         String locSpecs = "";
         String qryFunction = "";
         String active = "";
         String data = "";
         String description = "";

         int size = 0;
         if (args.length > 0) {
            setFileName(args[0]);
         }

         String inputFile = getFileName();

         File theFile;
         if (isBlank(inputFile))  {
            System.out.println("HTML File name is empty - please explore");
         }
         else try {
            theFile = new File(inputFile);
            Document doc = Jsoup.parse(theFile, "UTF-16");
            org.jsoup.select.Elements rows = doc.getElementsByTag("tr");
            size = rows.size();
            testItems = new TestItem[size];
            int index = 0;
            for (org.jsoup.nodes.Element row : rows) {

               org.jsoup.select.Elements cells = row.getElementsByTag("td");
               id = cells.select(".id").text();
               action = cells.select(".action").text();
               locType = cells.select(".locType").text();
               locSpecs = cells.select(".locSpecs").text();
               qryFunction = cells.select(".qryFunction").text();
               active = cells.select(".active").text();
               data = cells.select(".data").text();
               description = cells.select(".description").text();
               addItem(id, action, locType, locSpecs, qryFunction, active, data, description);
            }
         } catch (IOException e) {
            setException(e);
            throw new java.io.IOException("Failed to get test items from html file: " + getFileName());
         }
         catch (Exception e) {
            setException(e);
            throw new java.io.IOException("Failed to get test items from html file: " + e.toString());
         }
         System.out.println(String.valueOf(testItems.length) + " test items found on file: " + getFileName());

         return;
      }
   }
}

