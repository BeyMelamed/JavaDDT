import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 12/11/13
 * Time: 11:42 PM
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
 * This is where various settings of the project reside
 * Defaults can be overridden by properties file
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/11/13    |Bey      |Initial Version
 * 05/08/14    |Bey      |Introduce Time Zone Adjustment
 * 06/22/14    |Bey      |Change format of test items provider specs
 * 07/23/15    |Bey      |Add Locale Code (for now, only date localization)
 * 08/22/15    |Bey      |Add Version and property file handling for both, DDTProperties and BuildProperties
 * 08/24/15    |Bey      |Change TakeImage (from boolean to policy string) to enable image story of UI steps.
 * 12/19/15    |Bey      |Change ProjectFolder derivation to the current project folder
 * 06/28/16    |Bey      |Add email and report "blurb" variable ReportTextMessage
 * 06/28/16    |Bey      |Add an optional list of attachments (in addition to extent report)
 * 09/23/16    |Bey      |Resolve issue with line breaks not appearing in email message body
 * 10/14/16    |Bey      |Enable encryption of email sender password
 * 10/16/16    |Bey      |Adjust ddtSettings getters.
 * ============|=========|====================================
 */
public class DDTSettings {
   private static Properties properties;
   private static Properties buildProperties;
   private final String DDTVersion = "Please Set Version in ddt.properties file!";
   private final String FileSeparator = File.separator;
   private final String ProjectFolder = getProjectFolder();
   private final String ResourcesFolder = ProjectFolder +  "src" + FileSeparator + "test" + FileSeparator + "resources" + FileSeparator;
   private final String DataFolder = ProjectFolder +  "data" + FileSeparator;
   private final String ScriptsFolder = ProjectFolder +  "scripts" + FileSeparator;
   private final String ImagesFolder = ResourcesFolder +  "images" + FileSeparator;
   private final String ReportsFolder = ResourcesFolder + "reports" + FileSeparator;
   private final String ClassLoadFolder = ProjectFolder + "target" + FileSeparator + "classes" + FileSeparator;
   private final String TargetFolder = ProjectFolder + "target" + FileSeparator;
   private final String XslFileName = "Automation.Xsl";
   private final String ItemDelim = ";";
   private final String AndDelim = ".and.";
   private final String OrDelim = ".or.";
   private final String ValidDelims = ";~|!@#$%^&*()_+";
   private final String InputSpecs = "File!DDTRoot.xlsx!Root";
   private final String IEDriverFileName = ResourcesFolder+"IEDriverServer.exe";
   private final String ChromeDriverFileName = ResourcesFolder+"ChromeDriver.exe";
   private final String ChromePropertyKey = "webdriver.chrome.driver";
   private final String IEPropertyKey = "webdriver.ie.driver";
   private final String BrowserName = "IE"; // FIREFOX, IE, CHROME
   private final long WaitTime = 1; // in seconds
   private final int WaitInterval = 100; // in millis
   private final int DefaultPause = 0; // in millis
   private final String ProjectName = "Selenium Based DDT Automation";
   private final String EmailSender = "retsettdd@gmail.com";
   private final String EmailPassword = "Kishkes01";
   private final boolean EmailPasswordEncrypted = true;
   private final String EmailRecipients = "beymelamed01@optimum.net";
   private final String EmailHost = "smtp.gmail.com";
   private final String EmailPort = "587";
   private final boolean EmailAuthenticationRequired = true;
   private final boolean IsLocal = true;
   private final String TakeImagePolicy = "OnFail";
   private final boolean ReportEachTableCell = false;
   private final boolean TabOut = true;
   private final String StatusToReport = "PASS,FAIL,SKIP";
   private final String DontReportActions = "NewTest,GenerateReport,InitializeReport";
   private final String ReportElements = "status,action,locType,qryFunction,description,active,data,comments,errors,exceptionStack,exceptionCause,duration,screenShotFileName";
   private final String DefaultComparison = "Equals";
   private final String TestItemReportTemplate = "{id}{status}{active}{action}{loctype}{locspecs}{qryfunction}{data}{comments}{description}{screenShotFileName} \n {errors}";
   private final String TestItemJSONTemplate = "{id}{status}{active}{action}{loctype}{locspecs}{qryfunction}{data}{comments}{description}{errors}{screenShotFileName}";
   private final String DateFormat = "MM/dd/yyyy";
   private final String TimeStampFormat = "MM/dd/yyyy HH:mm:ss";
   private final String TimeZoneAdjustment = "";
   private final String DesiredCapabilityNames = "javascriptEnabled,databaseEnabled,locationContextEnabled,applicationCacheEnabled,applicationCacheEnabled,webStorageEnabled,acceptSslCerts,rotatable,nativeEvents,proxy,unexpectedAlertBehaviour,elementScrollBehavior";
   private final String DesiredCapabilityValues = "true,false,true,true,true,false,false,false,true,false,dismiss,true";
   private final boolean StripWhiteSpace = true;
   private final String ReportingStyle = "Default";
   private final String ReportFileName = "DDTTestResults.html";
   private final String ReportTextMessage = "Below, please find a summary of DDT Test Session (or Section): ";
   private final String LocaleCode = "en";
   private final boolean IsNestedReporting = false;
   private final String Attachments = "";

   private String ddtVersion;
   private String resourcesFolder;
   private String imagesFolder;
   private String reportsFolder;
   private String dataFolder;
   private String scriptsFolder;
   private String classLoadFolder;
   private String xslFileName;
   private String ieDriverFileName;
   private String chromeDriverFileName;
   private String inputSpecs;
   private String validDelims;
   private String itemDelim;
   private String andDelim;
   private String orDelim;
   private String iePropertyKey;
   private String chromePropertyKey;
   private String browserName;
   private boolean isLocal; // if true (local) - drives the screen shots file output type (FILE or BYTE)
   private String takeImagePolicy; // Never = {as expected}, OnFail = only on Failed UI steps, Always = on all UI steps (pass or fail)
   private boolean reportEachTableCell;  // if True then reporting on table searches may be huge - use true for debugging purposes mostly
   private boolean tabOut;               // Should driver append tab to data entry string in order to tab out of it
   private Long waitTime = -1L;
   private int waitInterval = -1;
   private int defaultPause =-1;
   private String projectName;
   private String emailSender;
   private String emailPassword;
   private String emailRecipients;
   private String emailHost;
   private String emailPort;
   private String attachments;
   private boolean emailAuthenticationRequired;
   private boolean emailPasswordEncrypted;
   private String statusToReport;
   private String dontReportActions;
   private String reportElements;
   private String defaultComparison;
   private String testItemReportTemplate;
   private String testItemJSONTemplate;
   private String dateFormat;
   private String timeStampFormat;
   private String timeZoneAdjustment;
   private String desiredCapabilityNames;
   private String desiredCapabilityValues;
   private boolean stripWhiteSpace;
   private String reportingStyle;
   private String reportFileName;
   private String reportTextMessage;
   private String localeCode;
   private static DDTSettings ddtSettings;
   private boolean isNestedReporting;

   public DDTSettings () {
      loadProperties();
   }

   // Determine the current project folder and return it with a file separator appended.
   private static String getProjectFolder() {
      String s = System.getProperty("user.dir");
      System.out.println("JavaDDT's Project Folder Is: " + s + File.separator);
      return s + File.separator;
   }
   // Load Properties from a file (path)
   private static Properties readProperties(String path) {
      if (isNotEmpty(path)) {
         Properties p = new Properties();
         InputStream is = null;
         File f = new File(asValidOSPath(path, true));
         try {
            is = new FileInputStream(f);
            p.load(is);
            return p;
         }
         catch (FileNotFoundException e) {
            System.out.println("*** Properties file (" + path + ") Not Found *** " + e.getMessage());
            return null;
         }
         catch (IOException e)  {
            System.out.println("*** Properties file (" + path + ") Cannot Be Opened *** " + e.getMessage());
            return null;
         }
         catch (Throwable e)  {
            System.out.println("*** Properties file (" + path + ") Cannot Be Opened *** " + e.getMessage());
            return null;
         }
      }
      else {
         System.out.println("*** Properties file path is Empty ***");
         return null;
      }
   }

   // Load the properties from which Settings() are to be derived.
   private  void loadProperties() {
      String blurb = "Properties file missing - Using Defaults!";
      properties = readProperties(ResourcesFolder + "ddt.properties");
      if (properties != null){
        blurb = "Properties loaded from ddt.Properties file.";
      }
      System.out.println(blurb);
   }

   // Load the Build Properties (this is where we find the maven Version property for the project
   private  void loadBuildProperties() {
      if (buildProperties instanceof Properties)
         return;

      String propsFileName = ResourcesFolder + "ddt.properties";
      propsFileName = propsFileName.replace("\\\\","\\");
      propsFileName = asValidOSPath(propsFileName, true);
      buildProperties = readProperties(propsFileName);
      if (buildProperties instanceof Properties)
         System.out.println("Build Properties loaded from: " + propsFileName);
      else {
         System.out.println("Failed to load Build Properties from: " + propsFileName + " - Using Version '1.0.0'");
         buildProperties = defaultBuildProperties();
      }
   }

   /**
    * Default Build Properties is used whenever the version of the project is needed.
    * This is the default construct in liew of pom.properties file in the maven-archiver folder of the Target folder of the project.
    * @return Properties that has the version entry - used when the build itself does not have those
    */
   private Properties defaultBuildProperties() {
      String currentVersion = DDTSettings.ddtSettings.getVersion();
      Properties p = new Properties();
      p.setProperty("version", "1.0.0");
      p.setProperty("groupId","com.DynaBytes.Automation");
      p.setProperty("artifactId", "JavaDDT");
      return p;
   }

   public String ensureEndsWithFileSeparator (String path) {
      return (path.endsWith(File.separator)) ? path : (path + File.separator);
   }

   public static boolean isReportableAction (String action) {
      return !(Settings().getDontReportActions().toLowerCase().contains(action.toLowerCase()));
   }

   public static boolean isWindowsOS() {
      if (System.getProperties().getProperty("os.name").toLowerCase().startsWith("win"))
         return true;
      return false;
   }

   public static String prefixFileNameWithPath(String fileName, String pathType) {
      String result = "";
      if (isBlank(pathType))
         return result;  // let all hell break loose - one should know better
      switch (pathType.toLowerCase()) {
         case "resource" : {result = Settings().getResourcesFolder() + fileName; break;}
         case "report" : {result = Settings().getReportsFolder() + fileName; break;}
         case "image" : {result = Settings().getImagesFolder() + fileName; break;}
         case "data" : {result = Settings().getDataFolder() + fileName; break;}
         case "script" : {result = Settings().getScriptsFolder() + fileName; break;}
         case "load" : {result = Settings().getClassLoadFolder() + fileName; break;}
         default : {result = Settings().getResourcesFolder() + fileName;}
      }
      return result;
   }

   /**
    * @return a singleton instance of the DDTSettings object - each attribute of which is lazily initialized from a properties file or hard coded value
    */
   public static DDTSettings Settings() {
      if (ddtSettings == null) {
         ddtSettings = new DDTSettings();
         initialize();
      }
      return ddtSettings;
   }

   /**
    * Initializes various aspects of the project's settings
    */
   private static void initialize() {
      Verb.initialize();
      ddtSettings.setIsLocal(true); // Turn off when testing remotely
      // Initialize various folders
      initializeFolders(ddtSettings);
      // Add global variables
      DDTTestRunner.addVariable("$d", ddtSettings.getItemDelim()); // Preserved variable representing a primary delimiter (e.g. between tokens in the Data property of TestItem instance)
      DDTTestRunner.addVariable("$and",ddtSettings.getAndDelim()); // Preserved variable representing "and" delimiter for various validations like comparison mode "between"
      DDTTestRunner.addVariable("$or",ddtSettings.getOrDelim()); // Preserved variable representing "or" delimiter for various validations like  comparison mode "or", "in"

      // Set the default class load folder - the folder optionall contains inline test string provider classes
      // We do this to keep the class 'clean' off of referencing DDT classes.
      DDTClassLoader.setDefaultLoadFolder(Settings().getClassLoadFolder());

      // Set the Default Wait Time and Default Poll Time for element location
      // Done here to ensure they are set to default upfront - this will also set the values on the UILocator class level.
      Long tmp1 = ddtSettings.getWaitTime();
      int tmp2 = ddtSettings.getWaitInterval();
   }

   /**
    * Resets the settings forcing re-reading of the properties file of the project
    */
   public static void reset() {
      ddtSettings = null;
   }

   public String[] inputSpecsArray() {
      return getInputSpecs().split(TestStringsProviderSpecs.SPLITTER);
   }

   public String[] inputSpecsArrayWithDataFolder() {
      String[] result = inputSpecsArray();
      TestStringsProviderSpecs specs = new TestStringsProviderSpecs(inputSpecsArray());
      if (!specs.isSetupValid())
         return result;

      specs.ensureFilePathIsValid(getDataFolder());

      String file = specs.getFileName();
      if (!isBlank(file))
         result[1] = file;

      return result;
   }

   /**
    *
    * @return the input type
    */
   public String inputType() {
      String result = "";
      try {
            result = inputSpecsArray()[0];
      }
      catch (Exception e) {}
      finally {
         return result;
      }
   }

   /**
    *
    * @return the input file name when the input type is "File"
    */
   public String getInputFileName() {
      String result = "";
      try {
         if (inputType().equalsIgnoreCase("file"))
            result = DDTSettings.asValidOSPath(inputSpecsArray()[1], true);
      }
      catch (Exception e) {}
      finally {
         return result;
      }
   }

   /**
    *
    * @return the 'root' worksheet name when the input type is "File"
    */
   public String inputWorksheetName() {
      String result = "";
      try {
         if (inputType().equalsIgnoreCase("file") && inputSpecsArray().length > 2) {
            result = inputSpecsArray()[2];
         }
      }
      catch (Exception e) {}
      finally {
         return result;
      }
   }

   public String testItemsGeneratingClassName() {
      String result = "";
      try {
         if (inputType().equalsIgnoreCase("inline")) {
            result = inputSpecsArray()[1];
         }
      }
      catch (Exception e) {}
      finally {
         return result;
      }
   }

   public String testItemsGeneratingMethodName() {
      String result = "";
      try {
         if (inputType().equalsIgnoreCase("inline") && inputSpecsArray().length > 2) {
            result = inputSpecsArray()[2];
         }
      }
      catch (Exception e) {}
      finally {
         return result;
      }
   }

   /**
    * Get the number of hours to add to the reference date of the application in order to align the test workstation with the web application
    * @return integer - the number of hours to add to the current datetime stamp (can be negative which means, subtract)
    */
   public int getTimeZoneAdjustmentInHours() {
      String tza = getTimeZoneAdjustment().trim();
      int result = 0;
      int factor = 1; //  Based on the leading sign of timeZoneAdjustment, set factor to +1 or -1 -  defaulting to +1

      if (isBlank(tza))
         return result;  // No adjustment

      try {

         if (tza.startsWith("+") || tza.startsWith("-")) {
            // Only adjust for negative sign to change the default
            if (tza.startsWith("-")) {
               factor = -1;
            }
            // Regardless of the sign - extract the rest of the string that should be numeric
            tza = substring(tza, 1);
         }
         // Play defense if user is playing tricks...
         if (!isNumeric(tza))
            return result;

         // Convert number of hours to millis
         result = Integer.valueOf(tza);
      }

      catch (Exception e) {
         result = 0;
      }

      return result * factor;
   }

   public Hashtable<String, Object> getDesiredCapabilities() {
      String names = getDesiredCapabilityNames();
      String values = getDesiredCapabilityValues();
      String[] desiredCapabilities = names.split(",");
      String[] desiredValues = values.split(",");
      int i = desiredCapabilities.length;
      Hashtable<String, Object> result = new Hashtable<String, Object>();
      String capability = "";
      String value = "";
      for (int j = 0; j < i; j++) {
         capability = desiredCapabilities[j];
         value = desiredValues[j];
         if (",true,false,".contains(value.toLowerCase())) {
            result.put(capability, Util.asBoolean(value));
         }
         else {
            if (isNumeric(value)){
               result.put(capability, Integer.valueOf(value));
            }
            else
               result.put(capability, value);
         }
      }
      return result;
   }
   /**
    * Returns the system-wide separator string that is used application-wide to split strings to substrings, etc.
    * This is a sequence that could not be included in the input stream - for example: Tab
    */
   public static String separator() {
      char c = ((char) 9);
      return String.valueOf(c);
   }

   /**
    * Reference the various folders so as to initialize them and, consequently, create system variables for them.
    */
   public static void initializeFolders(DDTSettings settings) {

      String s = settings.getImagesFolder();
      s = settings.getResourcesFolder();
      s = settings.getReportsFolder();
      s = settings.getDataFolder();
      s = settings.getScriptsFolder();
      s = settings.getClassLoadFolder();
   }

   /**
    * Convenience method returns a value from properties file by the propertyKey or (if the value is null) the default value (value)
    * @param value         The default value
    * @param propertyKey   The key to the properties collection
    * @param acceptBlanks  If true, blank property is acceptable as return value, else, replace blank with the default
    * @return              String - the value for this property
    */
   private String getPropertyOrDefaultValue(String value, String propertyKey, boolean acceptBlank) {
      if (isBlank(propertyKey))
         return value;
      if (!properties.containsKey(propertyKey))
         return value;
      String propertyValue = properties.getProperty(propertyKey);
      return (isBlank(propertyValue) && !acceptBlank) ? value : propertyValue;
   }

   /**
    * Replace all separators of the Windows system (backslash) to forward slash because forward slash works in all operating systems
    * @param path
    * @return
    */
   public static String asValidOSPath(String path, boolean toLower) {
      if (isBlank(path))
         return "";
      if (toLower)
         path = path.toLowerCase();
      if (File.separator == "\\")
         return path.replace("/", "\\");
      else
         return path.replace("\\", "/");
   }

   /**
    * ===================== Getters / Setters =====================
    */

   private void setDDTVersion(String value) {
      ddtVersion = value;
   }

   private String getDDTVersion() {
      if (isBlank(ddtVersion)) {
         String s = getPropertyOrDefaultValue(DDTVersion, "DDTVersion", false);
         setDDTVersion(s);
      }
      return ddtVersion;
   }

   public synchronized String getVersion() {
      String version = null;

      loadBuildProperties();
      // try to load from maven pom.properties first
      if (buildProperties.containsKey("version"))
         version = buildProperties.getProperty("version","");

      // fallback to using Java API
      if (version == null) {
         Package aPackage = getClass().getPackage();
         if (aPackage != null) {
            version = aPackage.getImplementationVersion();
            if (version == null) {
               version = aPackage.getSpecificationVersion();
            }
         }
      }

      if (version == null)
         version = getDDTVersion();

      if (version == null) {
         // we could not compute the version so use a blank
         version = "";
      }

      return version;
   }


   private void setValidDelims (String value) {
      validDelims = value;
   }

   public String getValidDelims() {
      if (isBlank(validDelims)) {
         String s = getPropertyOrDefaultValue(ValidDelims, "ValidDelims", false);
         setValidDelims(s);
      }
      return validDelims;
   }

   private void setProjectName(String value) {
      projectName = value;
   }

   public String getProjectName() {
      if (isBlank(projectName)) {
         String s = getPropertyOrDefaultValue(ProjectName, "ProjectName", false);
         setProjectName(s);
      }
      return projectName;
   }

   private void setLocaleCode(String value) {
      localeCode = value;
   }

   public String getLocaleCode() {
      if (isBlank(localeCode)) {
         String s = getPropertyOrDefaultValue(LocaleCode, "LocaleCode", false);
         setLocaleCode(s);
      }
      return localeCode;
   }

   private void setReportFileName(String value) {
      reportFileName = value;
   }

   public String getReportFileName() {
      if (isBlank(reportFileName)) {
         String s = getPropertyOrDefaultValue(ReportFileName, "ReportFileName", false);
         setReportFileName(s);
      }
      return reportFileName;
   }

   private void setResourcesFolder(String value) {
      resourcesFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$resourcesDir",resourcesFolder);
   }

   public String getResourcesFolder() {
      if (isBlank(resourcesFolder)) {
         String s = getPropertyOrDefaultValue(ResourcesFolder, "ResourcesFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setResourcesFolder(s);
      }
      return resourcesFolder;
   }

   private void setReportsFolder(String value) {
      reportsFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$reportsDir",reportsFolder);
   }

   public String getReportsFolder() {
      if (isBlank(reportsFolder)) {
         String s = getPropertyOrDefaultValue(ReportsFolder, "ReportsFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setReportsFolder(s);
      }
      return reportsFolder;
   }

   private void setImagesFolder(String value) {
      imagesFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$imagesDir",imagesFolder);
   }

   public String getImagesFolder() {
      if (isBlank(imagesFolder)) {
         String s = getPropertyOrDefaultValue(ImagesFolder, "ImagesFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setImagesFolder(s);
      }
      return imagesFolder;
   }

   private void setDataFolder(String value) {
      dataFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$dataDir",dataFolder);
   }

   public String getDataFolder() {
      if (isBlank(dataFolder)) {
         String s = getPropertyOrDefaultValue(DataFolder, "DataFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setDataFolder(s);
      }
      return dataFolder;
   }

   private void setScriptsFolder(String value) {
      scriptsFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$scriptsDir",scriptsFolder);
   }

   public String getScriptsFolder() {
      if (isBlank(scriptsFolder)) {
         String s = getPropertyOrDefaultValue(ScriptsFolder, "ScriptsFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setScriptsFolder(s);
      }
      return  scriptsFolder;
   }

   private void setClassLoadFolder(String value) {
      classLoadFolder = asValidOSPath(value, true);
      DDTTestRunner.addVariable("$classLoadDir",classLoadFolder);
   }

   private void setAttachments(String value) {
      attachments = value;
   }

   public String getAttachments() {
      if (isBlank(attachments)) {
         String s = getPropertyOrDefaultValue(Attachments, "Attachments", true);
         setAttachments(s);
      }
      return attachments;
   }

   public String getClassLoadFolder() {
      if (isBlank(classLoadFolder)) {
         String s = getPropertyOrDefaultValue(ClassLoadFolder, "ClassLoadFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setClassLoadFolder(s);
      }
      return  classLoadFolder;
   }

   private void setXslFileName(String value) {
      xslFileName = DDTSettings.asValidOSPath(value, true);
   }

   public String getXslFileName() {
      if (isBlank(xslFileName)) {
         String s = getPropertyOrDefaultValue(XslFileName, "XslFileName", false);
         setXslFileName(s);
      }
      return xslFileName;
   }

   private void setIEDriverFileName(String value) {
       String tmp = value;

       if (!isWindowsOS()) {
           tmp = tmp.toLowerCase().replace(".exe", "");
       }

       ieDriverFileName = DDTSettings.asValidOSPath(tmp, true);
   }

   public String getIEDriverFileName() {
      if (isBlank(ieDriverFileName)) {
         String s = getPropertyOrDefaultValue(IEDriverFileName, "IEDriverFileName", false);
         s = s.replace("%proj%", ProjectFolder);
         setIEDriverFileName(s);
      }
      return  asValidOSPath(ieDriverFileName, true);
   }

   private void setChromeDriverFileName(String value) {
      String tmp = value;
      if (!isWindowsOS()) {
         tmp = tmp.toLowerCase().replace(".exe", "");
      }
      chromeDriverFileName = asValidOSPath(tmp, true);
   }

   public String getChromeDriverFileName() {
      if (isBlank(chromeDriverFileName)) {
         String s = getPropertyOrDefaultValue(ChromeDriverFileName, "ChromeDriverFileName", false);
         s = s.replace("%proj%", ProjectFolder);
         setChromeDriverFileName(s);
      }
      return asValidOSPath(chromeDriverFileName, true);
   }

   private void setInputSpecs(String value) {
      inputSpecs = value;
   }

   public String getInputSpecs() {
      if (isBlank(inputSpecs)) {
         String s = getPropertyOrDefaultValue(InputSpecs, "InputSpecs", false);
         setInputSpecs(s);
      }
      return inputSpecs;
   }

   private void setItemDelim(String value) {
      itemDelim = value;
   }

   public String getItemDelim() {
      if (isBlank(itemDelim)) {
         String s = getPropertyOrDefaultValue(ItemDelim, "ItemDelim", false);
         if (s.toLowerCase().startsWith("char(")) {
            s = s.toLowerCase().replace("char(", "");
            s = s.replace(")", "");
         }
         if (isNumeric(s)) {
            int delim = Integer.valueOf(s);
            char c = ((char) delim);
            setItemDelim(String.valueOf(c));
         }
         else
            setItemDelim(s);
      }
      return itemDelim;

   }

   private void setAndDelim(String value) {
      andDelim = value;
   }

   public String getAndDelim() {
      if (isBlank(andDelim))  {
         String s = getPropertyOrDefaultValue(AndDelim, "AndDelim", false);
         setAndDelim(s);
      }
      return andDelim;
   }

   public String getAndToken () {
      String result = getAndDelim();
      result = capitalize(result.replace(".", ""));
      return " " + result + " ";
   }

   private void setOrDelim(String value) {
      orDelim = value;
   }

   public String getOrDelim() {
      if (isBlank(orDelim))  {
         String s = getPropertyOrDefaultValue(OrDelim, "OrDelim", false);
         setOrDelim(s);
      }
      return orDelim;
   }

   public String getOrToken () {
      String result = getOrDelim();
      result = capitalize(result.replace(".", ""));
      return " " + result + " ";
   }

   private void setChromePropertyKey(String value) {
      chromePropertyKey = value;
   }

   public String getChromePropertyKey() {
      if (isBlank(chromePropertyKey)) {
         String s = getPropertyOrDefaultValue(ChromePropertyKey, "ChromePropertyKey", false);
         setChromePropertyKey(s);
      }
      return chromePropertyKey;
   }

   private void setIEPropertyKey(String value) {
      iePropertyKey = value;
   }

   public String getIEPropertyKey() {
      if (isBlank(iePropertyKey)) {
         String s = getPropertyOrDefaultValue(IEPropertyKey, "IEPropertyKey", false);
         setIEPropertyKey(s);
      }
      return iePropertyKey;
   }

   private void setBrowserName(String value) {
      browserName = value;
   }

   public String getBrowserName() {
      if (isBlank(browserName)) {
         String s = getPropertyOrDefaultValue(BrowserName, "BrowserName", false);
         setBrowserName(s);
      }
      return browserName;
   }

   private void setIsLocal(boolean value) {
      isLocal = value;
   }

   public boolean getIsLocal() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(IsLocal), "IsLocal", false);
      setIsLocal(Util.asBoolean(s));
      return isLocal;
   }

   private void setEmailAuthenticationRequired(boolean value) {
      emailAuthenticationRequired = value;
   }

   public boolean getEmailAuthenticationRequired() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(EmailAuthenticationRequired), "EmailAuthenticationRequired", false);
      setEmailAuthenticationRequired(Util.asBoolean(s));
      return emailAuthenticationRequired;
   }

   private void setEmailPasswordEncrypted(boolean value) {
      emailPasswordEncrypted = value;
   }

   public boolean getEmailPasswordEncrypted() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(EmailPasswordEncrypted), "EmailPasswordEncrypted", false);
      setEmailPasswordEncrypted(Util.asBoolean(s));
      return emailPasswordEncrypted;
   }

   private void setTakeImagePolicy(String value) {
      takeImagePolicy = value;
   }

   public String getTakeImagePolicy() {
      String s = getPropertyOrDefaultValue(TakeImagePolicy, "TakeImagePolicy", true);

      setTakeImagePolicy(s);
      return takeImagePolicy;
   }

   private void setReportEachTableCell(boolean value) {
      reportEachTableCell = value;
   }

   public boolean getReportEachTableCell() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(ReportEachTableCell), "ReportEachTableCell", false);
      setReportEachTableCell(Util.asBoolean(s));
      return reportEachTableCell;
   }

   private void setTabOut(boolean value) {
      tabOut = value;
   }

   public boolean getTabOut() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(TabOut), "TabOut", false);
      setTabOut(Util.asBoolean(s));
      return tabOut;
   }

   private void setWaitTime(Long value) {
      waitTime = value;
      UILocator.setWaitTimeSec(waitTime);
   }

   public Long getWaitTime() {
      if (waitTime < 0L) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Long.toString(WaitTime), "WaitTime", false);
         setWaitTime(Long.valueOf(s));
      }
      return waitTime;
   }

   private void setWaitInterval(int value) {
      waitInterval = value;
      UILocator.setWaitPollTime(waitInterval);
   }

   public int getWaitInterval() {
      if (waitInterval < 0) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Integer.toString(WaitInterval), "WaitInterval", false);
         setWaitInterval(Integer.valueOf(s));
      }
      return waitInterval;
   }

   private void setDefaultPause(int value) {
      defaultPause = value;
   }

   public int getDefaultPause() {
      if (defaultPause < 0) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Integer.toString(DefaultPause), "DefaultPause", false);
         setDefaultPause(Integer.valueOf(s));
      }
      return defaultPause;
   }

   private void setEmailRecipients(String value) {
      emailRecipients = value;
   }

   public String getEmailRecipients() {
      if (isBlank(emailRecipients)) {
         String s = getPropertyOrDefaultValue(EmailRecipients, "EmailRecipients", true);
         setEmailRecipients(s);
      }
      return emailRecipients;
   }

   private void setEmailSender(String value) {
      emailSender = value;
   }

   public String getEmailSender() {
      if (isBlank(emailSender)) {
         String s = getPropertyOrDefaultValue(EmailSender, "EmailSender", false);
         setEmailSender(s);
      }
      return emailSender;
   }

   private void setEmailPassword(String value) {
      emailPassword = value;
   }

   public String getEmailPassword() {
      if (isBlank(emailPassword)) {
         String s = getPropertyOrDefaultValue(EmailPassword, "EmailPassword", false);
         setEmailPassword(s);
      }
      return emailPassword;
   }

   private void setEmailHost(String value) {
      emailHost = value;
   }

   public String getEmailHost() {
      if (isBlank(emailHost)) {
         String s = getPropertyOrDefaultValue(EmailHost, "EmailHost", false);
         setEmailHost(s);
      }
      return emailHost;
   }

   private void setEmailPort(String value) {
      emailPort = value;
   }

   public String getEmailPort() {
      if (isBlank(emailPort)) {
         String s = getPropertyOrDefaultValue(EmailPort, "EmailPort", false);
         setEmailPort(s);
      }
      return emailPort;
   }

   private void setStatusToReport(String value) {
      statusToReport = value;
   }

   public String getStatusToReport() {
      if (isBlank(statusToReport)) {
         String s = getPropertyOrDefaultValue(StatusToReport, "StatusToReport", false);
         setStatusToReport(s);
      }
      return statusToReport;
   }

   private void setDontReportActions(String value) {
      dontReportActions = value;
   }

   public String getDontReportActions() {
      if (isBlank(dontReportActions)) {
         String s = getPropertyOrDefaultValue(DontReportActions, "DontReportActions", false);
         setDontReportActions(s);
      }
      return dontReportActions;
   }

   private void setReportElements(String value) {
      reportElements = value;
   }

   public String getReportElements() {
      if (isBlank(reportElements)) {
         String s = getPropertyOrDefaultValue(ReportElements, "ReportElements", false);
         setReportElements(s);
      }
      return reportElements;
   }

   private void setDefaultComparison(String value) {
      defaultComparison = value;
   }

   public String getDefaultComparison() {
      if (isBlank(defaultComparison)) {
         String s = getPropertyOrDefaultValue(DefaultComparison, "DefaultComparison", true);
         setDefaultComparison(s);
      }
      return defaultComparison;
   }

   private void setTestItemReportTemplate(String value) {
      testItemReportTemplate = value;
   }

   public String getTestItemReportTemplate() {
      if (isBlank(testItemReportTemplate)) {
         String s = getPropertyOrDefaultValue(TestItemReportTemplate, "TestItemReportTemplate", true);
         setTestItemReportTemplate(s);
      }
      return testItemReportTemplate;
   }

   private void setTestItemJSONTemplate(String value) {
      testItemJSONTemplate = value;
   }

   public String getTestItemJSONTemplate() {
      if (isBlank(testItemJSONTemplate)) {
         String s = getPropertyOrDefaultValue(TestItemJSONTemplate, "TestItemJSONTemplate", true);
         setTestItemJSONTemplate(s);
      }
      return testItemJSONTemplate;
   }

   private void setDateFormat(String value) {
      dateFormat = value;
   }

   public String getDateFormat() {
      if (isBlank(dateFormat)) {
         String s = getPropertyOrDefaultValue(DateFormat, "DateFormat", true);
         setDateFormat(s);
      }
      return dateFormat;
   }

   private void setReportTextMessage(String value) {
      reportTextMessage = value;
   }

   public String getReportTextMessage() {
      if (isBlank(reportTextMessage)) {
         String s = getPropertyOrDefaultValue(ReportTextMessage, "ReportTextMessage", true);
         setReportTextMessage(s.replace("*crlf*", System.lineSeparator()));
      }
      String value = reportTextMessage.replace("*crlf*", System.lineSeparator());
      return value;

   }

   private void setTimeStampFormat(String value) {
      timeStampFormat = value;
   }

   public String getTimeStampFormat() {
      if (isBlank(timeStampFormat)) {
         String s = getPropertyOrDefaultValue(TimeStampFormat, "TimeStampFormat", true);
         setTimeStampFormat(s);
      }
      return timeStampFormat;
   }

   private void setTimeZoneAdjustment(String value) {
      timeZoneAdjustment = value;
   }

   public String getTimeZoneAdjustment() {
      if (isBlank(timeZoneAdjustment)) {
         String s = getPropertyOrDefaultValue(TimeZoneAdjustment, "TimeZoneAdjustment", true);
         setTimeZoneAdjustment(s);
      }
      return timeZoneAdjustment;
   }

   private void setDesiredCapabilityNames(String value) {
      desiredCapabilityNames = value;
   }

   public String getDesiredCapabilityNames() {
      if (isBlank(desiredCapabilityNames)) {
         String s = getPropertyOrDefaultValue(DesiredCapabilityNames, "DesiredCapabilityNames", true);
         setDesiredCapabilityNames(s);
      }
      return desiredCapabilityNames;
   }

   private void setDesiredCapabilityValues(String value) {
      desiredCapabilityValues = value;
   }

   public String getDesiredCapabilityValues() {
      if (isBlank(desiredCapabilityValues)) {
         String s = getPropertyOrDefaultValue(DesiredCapabilityValues, "DesiredCapabilityValues", true);
         setDesiredCapabilityValues(s);
      }
      return desiredCapabilityValues;
   }

   private void setStripWhiteSpace(boolean value) {
      stripWhiteSpace = value;
   }

   /**
    * Used for (potential) improvement of (mostly string) verifications relieving users of counting spaces, and other white space characters...
    * @return
    */
   public boolean getStripWhiteSpace() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(StripWhiteSpace), "StripWhiteSpace", false);
      setStripWhiteSpace(Util.asBoolean(s));
      return stripWhiteSpace;
   }

   private void setReportingStyle(String value) {
      reportingStyle = value;
   }

   public String getReportingStyle() {
      if (isBlank(reportingStyle)) {
         String s = getPropertyOrDefaultValue(ReportingStyle, "ReportingStyle", false);
         setReportingStyle(s);
      }
      return reportingStyle;
   }

   private void setIsNestedReporting(boolean value) {
      isNestedReporting = value;
   }

   /**
    * Used for nested (Extent) reporting style
    * @return boolean
    */
   public boolean getNestedReporting() {
      String s = getPropertyOrDefaultValue(Util.booleanString(IsNestedReporting), "IsNestedReporting", false);
      setIsNestedReporting(Util.asBoolean(s));
      return isNestedReporting;
   }

}
