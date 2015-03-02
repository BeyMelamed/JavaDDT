import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by BeyMelamed on 7/2/2014.
 * Selenium Based Automation Project
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
 * Description - This class is the specifier of TestStringsProvider specification.
 *               The specification is derived from a colon ':' delimited string where
 *               Element 1 is the type (File or Inline)
 *               Element 2 is the source name (file name or class name)
 *               Element 3 is the row
 *
 * NOTE: This class is the root (abstract) of several test items strings provider classes
 *       Each of the item providers' item can use a newTest action specifying any other test items provider
 *       Just follow the proper naming conventions and ensure files (if any) exist where expected.
 *
 * History
 *
 * When      |Who            |What
 * ==========|===============|========================================================
 * 7/2/2014  |Bey            |Initial Version
 * 10/28/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class TestStringsProviderSpecs extends DDTBase{

   public static final String SPLITTER = "!";

   private final String supportedFileExtensions = ",.xml,.xls,.xlsx,.html,";
   private String inputSpecs;
   private String[] args;

   /**
    * Constructor 1 - must provide a source specs as array string
    * @param sourceSpecs
    */
   public TestStringsProviderSpecs(String[] args) {
      setArgs(args);
      determineErrors();
   }

   /**
    * Constructor 2 - must provide a source specs string - better be a ":" delimited one
    * @param sourceSpecs
    */
   public TestStringsProviderSpecs(String specs) {
      setInputSpecs(specs);
      // Convert the (hopefully delimited) specs to array
      setArgs(getInputSpecsArray());
      determineErrors();
   }

   public boolean isValid() {
      return isBlank(getErrors());
   }

   public void setInputSpecs(String value) {
      inputSpecs = value;
   }

   public String getInputSpecs() {
      if (isBlank(inputSpecs))
         setInputSpecs("");
      return inputSpecs;
   }

   private String[] getInputSpecsArray() {
      String[] result = null;
      if (isBlank(getInputSpecs()))
         return result;

      result = getInputSpecs().split(SPLITTER);

      return result;
   }
   public void setArgs(String[] value) {
      args = value;
   }

   public String[] getArgs() {
      if (args == null)
         setArgs(new String[0]);
      return args;
   }

   public String getSupportedFileExtensions() {
      return supportedFileExtensions;
   }

   public boolean isInlineProvider() {
      boolean result = false;
      if (getArgs() == null || getArgs().length < 2)
         return result;
      result = getSourceType().equalsIgnoreCase("inline");
      return result;
   }

   public boolean isFileProvider() {
      boolean result = false;
      if (getArgs() == null || getArgs().length < 2)
         return result;
      result = getSourceType().equalsIgnoreCase("file");
      return result;
   }

   public String getSourceType() {
      if (getArgs() == null || getArgs().length < 2)
         return "";
      return getArgs()[0];
   }

   public String getSourceName() {

      String result = "";
      if (getArgs() == null || getArgs().length < 2)
         return result;

      result = getArgs()[1];

      return result;
   }

   public String getClassName() {

      String result = "";
      if (!isInlineProvider())
         return result;
      result = getSourceName();

      return result;
   }

   public String getFileName() {

      String result = "";
      if (!isFileProvider())
         return result;
      result = getSourceName();

      return result;
   }

   public String getItemsContainerName() {

      String result = "";
      if (getArgs() == null || getArgs().length < 3)
         return result;
      result = getArgs()[2];
      return result;
   }

   public boolean isFileNameValid() {
      boolean result = false;

      if (!isFileProvider())
         return result;

      String sourceName = getSourceName().toLowerCase();
      String[] supportedFileExtensions = getSupportedFileExtensions().split(",");
      int size = supportedFileExtensions.length;
      for (int i = 0; i < size ; i++) {
        if (isBlank(supportedFileExtensions[i]))
           continue;
        if (sourceName.endsWith(supportedFileExtensions[i])) {
           result = true;
           break;
        }
      }

      return result;
   }

   public void setFileName(String value) {
      if (!isSetupValid() || !isFileProvider())
         return;
      args[1] = DDTSettings.asValidOSPath(value, true);
   }
   public boolean isSetupValid() {
      if (getArgs().length < 2)
         return false;
      if (!(isFileProvider() || isInlineProvider()))
         return false;
      if (isBlank(getArgs()[1]))
         return false;

      return true;
   }

   public boolean isSpreadSheet() {
      if (!isFileProvider())
         return false;
      if (getSourceName().toLowerCase().endsWith(".xls"))
         return true;
      if (getSourceName().toLowerCase().endsWith(".xlsx"))
         return true;
      return false;
   }

   public void ensureFilePathIsValid(String baseDataFolder) {
      if (!isFileProvider() || !isSetupValid() || isBlank(baseDataFolder))
         return;
      String tmp = getFileName();
      // Ensure this gets done only  once
      if (tmp.toLowerCase().startsWith(baseDataFolder.toLowerCase()))
         return;
      if (tmp.toLowerCase().contains("%data%"))
         tmp = tmp.toLowerCase().replace("%data%", baseDataFolder);
      if ((indexOf(tmp, '/') < 0) && (indexOf(tmp, "\\")) < 0)
         tmp = baseDataFolder + tmp;
      setFileName(tmp);
   }

   /**
    * Establish any setup errors and, if any, set the instances errors  property
    */
   private void determineErrors() {
      if (getArgs() == null) {
         addError("Blank Input Specs.");
         return;
      }

      if (getArgs().length < 2) {
         addError("Input Specs string is not properly constructed - should have at least Type and Input Source delimited by '" + SPLITTER + "'");
         return;
      }

      if (isFileProvider() && isBlank(getFileName())) {
         addError("Source File Name is required but is blank.");
         return;
      }

      if (isInlineProvider() && isBlank(getClassName())) {
         addError("Source Class Name is required but is blank.");
         return;
      }

      if (isInlineProvider() && isBlank(getItemsContainerName())) {
         addError("Source Method Name is required but is blank.");
         return;
      }

      if (isFileProvider() && !isFileNameValid())  {
         addError("Unsupported File Extension in Source Name.");
         return;
      }

      if (isFileProvider() && !isFileNameValid())  {
         addError("Unsupported File Extension in Source Name.");
         return;
      }

      if (isSpreadSheet() && isBlank(getItemsContainerName())) {
         addError("Spread Sheet Name not provided for spreadsheet source.");
         return;
      }
   }

}
