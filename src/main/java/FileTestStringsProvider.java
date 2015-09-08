import jxl.Sheet;
import jxl.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import java.util.ArrayList;
import java.util.Iterator;

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
 * 10/28/14     |Bey      |Subclass from BaseDDT
 * 09/06/15     |Bey      |Move boolean isSetupValid here to avoid repetition in subclasses
 * =============|=========|====================================
 */
public abstract class FileTestStringsProvider extends TestStringsProvider {

   public String getSourceName() {
      return getTestStringsProviderSpecs().getSourceName();
   }

   public String getItemsContainerName() {
      return getTestStringsProviderSpecs().getItemsContainerName();
   }

   public boolean isSetupValid() {
      return getTestStringsProviderSpecs().isValid();
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 7/2/14
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
    * NOTE: This class is one of several test item strings provider classes - this is used for .xls files
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 1/20/14     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class ExcelTestStringsProvider extends FileTestStringsProvider {

      public ExcelTestStringsProvider() {
      }

      public ExcelTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public ExcelTestStringsProvider(String[] inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public ExcelTestStringsProvider(String inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      @Override
      void provideStrings()
            throws jxl.read.biff.BiffException, ArrayIndexOutOfBoundsException, IOException {

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         Workbook wb = null;

         String inputFile = getSourceName();
         String worksheetName = getItemsContainerName();

         int nRows = 0;
         try {
            File inputWorkbook = new File(DDTSettings.asValidOSPath(inputFile, true));
            wb = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = wb.getSheet(worksheetName);
            nRows = sheet.getRows();
            setDataStrings(new String[nRows-1][8]);
            // Consider rows 2 and on - the first one is column titles
            for (int row = 1; row < nRows; row++) {

               for (int j = 0; j < 8; j++) {
                  String content = sheet.getCell(j, row).getContents();
                  if (isBlank(content))
                     content = "";
                  getDataStrings()[row-1][j] = content;
               }
            }
         }
         catch (IOException e) {
            setException(e);
            System.out.println("Failed to get test items from worksheet named: " + Util.sq(worksheetName) + " in file: " + inputFile);
         }
         catch (ArrayIndexOutOfBoundsException e) {
            setException(e);
            System.out.println("Failed to get test items from worksheet named: " + Util.sq(worksheetName) + " in file: " + inputFile);
         }
         catch (jxl.read.biff.BiffException e) {
            setException(e);
            System.out.println("Failed to get test items from worksheet named: " + Util.sq(worksheetName) + " in file: " + inputFile);
         }
         finally {
            if (wb instanceof Workbook) {
               wb.close();
               wb = null;
            }
            System.out.println(String.valueOf(nRows-1) + " test items found on worksheet " + worksheetName + " in file: " + inputFile);
         }
      }

   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 7/2/14
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
    * Description - Provides test items from an Excel (.xlsx) spreadsheet.
    *
    * NOTE: This class is one of several test item strings provider classes - this is used for .xlsx files
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 7/02/14     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class ExcelXSSFTestStringsProvider extends FileTestStringsProvider {

      public ExcelXSSFTestStringsProvider() {
      }

      public ExcelXSSFTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public ExcelXSSFTestStringsProvider(String[] inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public ExcelXSSFTestStringsProvider(String inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      @Override
      void provideStrings() throws IOException {

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         String inputFile = getSourceName();
         String worksheetName = getItemsContainerName();

         XSSFWorkbook workbook = null;
         int nRows = 0;

         if (isBlank(inputFile) || isBlank(worksheetName))  {
            System.out.println("Either File or Worksheet name(s) is / are empty - please explore");
         }
         else {
            try {
               File inputWorkbook = new File(DDTSettings.asValidOSPath(inputFile, true));

               FileInputStream fis = new FileInputStream(inputWorkbook);
               workbook = new XSSFWorkbook(fis);
               XSSFSheet worksheet = workbook.getSheet(worksheetName);

               nRows = worksheet.getLastRowNum();
               setDataStrings(new String[nRows][8]);

               for (int row = 1; row <= nRows; row++) {
                  XSSFRow xssfRow = worksheet.getRow(row);

                  for (int i = 0; i < 8; i++) {
                     String content = "";
                     Cell cell = xssfRow.getCell(i);
                     if (cell != null) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        content = cell.toString();
                     }
                     getDataStrings()[row-1][i] = content;
                  }
               }
            } catch (IOException e) {
               setException(e);
               System.out.println(e.getMessage().toString());
            } finally {
               workbook = null;
               System.out.println(String.valueOf(nRows) + " test items found on worksheet " + worksheetName + " in file: " + inputFile);
            }
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 9/6/15
    * Time: 2:22 PM
    * Selenium Based Automation
    *
    * =============================================================================
    * Copyright 2015 Avraham (Bey) Melamed.
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
    * Description - Provides test items from a json file.
    * {
         "$schema": "http://json-schema.org/draft-04/schema#",
         "title": "TestStep",
         "description": "An instance of DDT Test Step",
         "type": "TestItem",
         "properties": {
            "id": {
            "description": "ID for for a test step",
            "type": "string"
         },
    "action": {
    "description": "Step Action - The action invoked (verb used) for this test step",
    "type": "string"
    "locType": {
    "description": "Locator type for this test step - used only for UI steps",
    "type": "string"
    },
    "locSpecs": {
    "description": "Locator details for this test step - used only for UI steps",
    "type": "string"
    },
    "qryFunction": {
    "description": "Web Element Query Function - used only for a UI test step",
    "type": "string"
    },
    "active": {
    "description": "Indicates whether this test step is active (blank = yes).",
    "type": "string"
    },
    "data": {
    "description": "Step specific action modifiers for this test step.",
    "type": "string"
    },
    "description": {
    "description": "Description of this test step.",
    "type": "string"
    },
    },
      "required": ["action"]
    }
    *
    * NOTE: This class is one of several test item strings provider classes - this is used for delimited .txt files
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 9/06/15     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class JSONTestStringsProvider extends FileTestStringsProvider {

      public JSONTestStringsProvider() {
      }

      public JSONTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      @Override
      /**
       * Gets test Items from a JSON file that is structured in a proprietary way... (same order as the spreadsheet provider)
       * Extension is .json
       * Must have at least the action property - but this is not enforced here.
       *
       * Here is an example of a file with two test steps
       *
       {
       "TestItems": [
       {
          "id":"JSONTest#",
          "action": "NewTest",
          "data": "InputSpecs=File!DDTRoot.xlsx!Root",
          "description": "Run the first calculation test scenario"
       },
       {
          "id":"JSONTest#",
          "action": "Click",
          "locType": "id",
          "locSpecs": "submitButton",
          "description": "Click the Submit Button"
       }]
       }

       */

      void provideStrings() throws ArrayIndexOutOfBoundsException, IOException {

         int nRows = 0;

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         String inputFile = getSourceName();

         ArrayList<String[]> itemList = new ArrayList<String[]>();
         int nItems = 0;

         try {

            File theFile;
            JSONParser jp = new JSONParser();
            theFile = new File(DDTSettings.asValidOSPath(inputFile, true));
            FileReader fr = new FileReader(theFile);
            JSONArray tests = new JSONArray();
            Object jsonItems = jp.parse(fr);
            JSONObject jsonObject =  (JSONObject) jsonItems;

            // Note: the name of the objects in the JSON file is critical AND IS CASE SENSITIVE!!!
            tests = (JSONArray) jsonObject.get("TestItems");

            // This methodology uses a string iterator for getting each of the Json objects.

            Iterator<String> iterator = tests.iterator();
            Object id;
            Object action;
            Object locType;
            Object locSpecs;
            Object qryFunction;
            Object active;
            Object data;
            Object description;
            while (iterator.hasNext()){
               //TODO - Figure out a better way to get the properties out of the JSON object
               Object tmp = iterator.next();
               JSONObject jo = (JSONObject) tmp;

               // Get TestItem (temporary) properties - these are null or strings - names are for readability purposes the same as TestItem properties.
               id = jo.get("id");
               action = jo.get("action");
               locType = jo.get("locType");
               locSpecs = jo.get("locSpecs");
               qryFunction = jo.get("qryFunction");
               active = jo.get("active");
               data = jo.get("data");
               description = jo.get("description");

               // Needs to be replenished each iteration - ?? why ??
               String[] anItem = new String[8];

               // Creata an array of string representing a single test item
               anItem[0] = (id == null ? "" : id.toString());
               anItem[1] = (action == null ? "" : action.toString());
               anItem[2] = (locType == null ? "" : locType.toString());
               anItem[3] = (locSpecs == null ? "" : locSpecs.toString());
               anItem[4] = (qryFunction == null ? "" : qryFunction.toString());
               anItem[5] = (active == null ? "" : active.toString());
               anItem[6] = (data == null ? "" : data.toString());
               anItem[7] = (description == null ? "" : description.toString());

               itemList.add(anItem);
               nItems++;
            }

            if (nItems > 0) {
               stringifyTestItems(itemList);
               System.out.println(nItems + " Items found on file " + inputFile);
               addComment(nItems + " Items found on file " + inputFile);
            }
            else {
               System.out.println("No Items found on file " + inputFile);
               addError("No Items found on file " + inputFile);
            }
         }
         catch (Exception e) {
            setException(e);
            throw new java.io.IOException("Failed to get test item strings from text file: " + e.toString());
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 7/2/14
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
    * Description - Provides test items from an html (.html) spreadsheet.
    *
    * NOTE: This class is one of several test item strings provider classes - this is used for .html files
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 7/02/14     |Bey      |Initial Version
    * 8/30/15     |Bey      |Eliminate unused methods
    * ============|=========|====================================
    */
   public static class HtmlTestStringsProvider extends FileTestStringsProvider {

      public HtmlTestStringsProvider() {
      }

      public HtmlTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
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

      void provideStrings() throws ArrayIndexOutOfBoundsException, IOException {

         String id = "";
         String action = "";
         String locType = "";
         String locSpecs = "";
         String qryFunction = "";
         String active = "";
         String data = "";
         String description = "";

         int nRows = 0;

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         String inputFile = getSourceName();

         try {
            File theFile;
            theFile = new File(DDTSettings.asValidOSPath(inputFile, true));
            Document doc = Jsoup.parse(theFile, "UTF-16");
            org.jsoup.select.Elements rows = doc.getElementsByTag("tr");
            nRows = rows.size();

            setDataStrings(new String[nRows][8]);

            int rowNo = 0;
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
               String[] item = {id, action, locType, locSpecs, qryFunction, active, data, description};
               getDataStrings()[rowNo++] = item;

            }
         }
         catch (IOException e) {
            setException(e);
            throw new java.io.IOException("Failed to get test item strings from html file: " + inputFile);
         }
         catch (Exception e) {
            setException(e);
            throw new java.io.IOException("Failed to get test item strings from html file: " + e.toString());
         }
         finally {
            System.out.println(nRows + " test item strings found on file: " + inputFile);
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 8/30/15
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
    * Description - Provides test items from a delimited text file.
    *
    * NOTE: This class is one of several test item strings provider classes - this is used for delimited .txt files
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 8/30/15     |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class DelimitedTestStringsProvider extends FileTestStringsProvider {

      public DelimitedTestStringsProvider() {
      }

      public DelimitedTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

//      public boolean isSetupValid() {
//         return getTestStringsProviderSpecs().isValid();
//      }

      @Override
      /**
       * Gets test Items from a delimited text file that is structured in a proprietary way... (same order as the spreadsheet provider)
       * Extension is .txt by convention
       * Empty lines are ignored           .
       * Lines starting with # are ignored.
       * The first line is headers by convention and is ignored
       * Must have at least two columns (one tab) per line
       *
       * Here is an example of a file with two test steps (^t denotes a tab character)
       *
       Id^tAction^tLocType^t^tLocSpecs^tQryFunction^tActive^tData^tDescription
       Demo#^tNewTest^t^t^t^t^tInputSpecs=File!DDTRootxls!Calculate1^tRun the first calculation test scenraio
       Demo#^tNewTest^t^t^t^t^tInputSpecs=File!DDTRootxls!Calculate2^tRun the second calculation test scenraio

       * Note that class is used to identify properties as 'class' attribute does not have to be unique

       */

      void provideStrings() throws ArrayIndexOutOfBoundsException, IOException {

         int nRows = 0;

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         String inputFile = getSourceName();

         ArrayList<String[]> itemList = new ArrayList<String[]>();
         String theLine = "";
         String[] anItem;
         boolean done = false;
         int nItems = 0;

         try {
            File theFile;
            theFile = new File(DDTSettings.asValidOSPath(inputFile, true));
            FileReader fr = new FileReader(theFile);
            BufferedReader br = new BufferedReader(fr);

            while (!done) {
               theLine = br.readLine();
               done = (theLine == null);
               if (!done) {
                  nRows++;
                  if (nRows == 1)
                     continue;
                  if (isBlank(theLine))
                     continue;
                  if (theLine.startsWith("#"))
                     continue;
                  anItem = theLine.split("\t");
                  int len = anItem.length;
                  if (len < 2 || len > 8) {
                     System.out.println("Invalid line encountered in " + inputFile.toString() + "(" + theLine + "), line has " + len + " delimiters");
                     continue;
                  }
                  if (len < 8) {
                     // Make up empty items' fields
                     String[] tmpItem = {"","","","","","","",""};
                     for (int i = 0; i < len; i++)
                        tmpItem[i] = anItem[i];
                     anItem = tmpItem;
                  }
                  itemList.add(anItem);
                  nItems++;
               }
            }

            if (nItems > 0) {
               stringifyTestItems(itemList);
               System.out.println(nItems + " Items found on file " + inputFile);
               addComment(nItems + " Items found on file " + inputFile);
            }
            else {
               System.out.println("No Items found on file " + inputFile);
               addError("No Items found on file " + inputFile);
            }
         }
         catch (Exception e) {
            setException(e);
            throw new java.io.IOException("Failed to get test item strings from text file: " + e.toString());
         }
      }
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 7/2/14
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
    * NOTE: This class is one of several test items provider classes - this is the XML version
    *
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 07/02/14    |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class XMLTestStringsProvider extends FileTestStringsProvider {

      public XMLTestStringsProvider() {
      }

      public XMLTestStringsProvider(TestStringsProviderSpecs inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public XMLTestStringsProvider(String[] inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      public XMLTestStringsProvider(String inputSpecs) {
         setTestStringsProviderSpecs(inputSpecs);
         addError(getTestStringsProviderSpecs().getErrors());
      }

      @Override
      void provideStrings() throws XMLStreamException, IOException {

         // Setup errors are set by the constructor - if any.
         if (!isBlank(getErrors()) || !isSetupValid())
            return;

         String inputFile = getSourceName();

         try {

            DDTestParser reader = new DDTestParser();
            reader.buildTestStrings(inputFile);
            setException(reader.getException());

            if (getException() instanceof Exception)
               throw new Exception("Exception encountered while reading test items from XML file: " + inputFile);

         }
         catch (XMLStreamException e) {
            setException(e);
            System.out.println("Failed to get test items from XML file: " + inputFile);
         }
         catch (Exception e) {
            setException(e);
            System.out.println("Failed to get test items from XML file: " + inputFile);
         }
      }

      /**
       * Created with IntelliJ IDEA.
       * User: Avraham (Bey) Melamed
       * Date: 7/2/14
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
       * Description  - Parses XML file to extract test items strings from it
       *
       * History
       * When        |Who      |What
       * ============|=========|====================================
       * 07/02/14    |Bey      |Initial Version
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

         int count = 0;
         ArrayList<String[]> itemsList = new ArrayList<>();

         /**
          * Build the instances ArrayList<String[]>
          * @param inputFile
          */
         @SuppressWarnings("unchecked")
         public void buildTestStrings(String inputFile) {
            ArrayList<String[]> itemsList = new ArrayList<String[]>();

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
                        String[] item = {id, action, locType, locSpecs, qryFunction, active, data, description};
                        itemsList.add(item);
                        count++;
                     }
                     else
                        continue;
                  } // Trip on the next start element
                  // Upon reaching the end of an item - skip to the next
                  if (event.isEndElement()) {
                     EndElement endElement = event.asEndElement();
                  }
               } // While event reader hasNext()
            } catch (FileNotFoundException e) {
               setException(e);
            } catch (XMLStreamException e) {
               setException(e);
            }
            stringifyTestItems(itemsList);
         } // BuildTestStrings
      } // DDTestParser
   } // XMLTestStringsProvider
}
