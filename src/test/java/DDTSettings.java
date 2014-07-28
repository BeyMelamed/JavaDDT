import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import static java.lang.Long.*;
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
 * ============|=========|====================================
 * @TODO - set up all properties from property file (handle non string types), introduce all email properties to Settings
 */
public class DDTSettings {
   private static Properties properties;

   private final String FileSeparator = File.separator;
   private final String ProjectFolder = System.getProperty("user.dir") + FileSeparator;
   private final String ResourcesFolder = ProjectFolder +  "src" + FileSeparator + "test" + FileSeparator + "Resources" + FileSeparator;
   private final String DataFolder = ProjectFolder +  "Data" + FileSeparator;
   private final String ScriptsFolder = ProjectFolder +  "Scripts" + FileSeparator;
   private final String ImagesFolder = ResourcesFolder +  "Images" + FileSeparator;
   private final String ReportsFolder = ResourcesFolder + "Reports" + FileSeparator;
   private final String ClassLoadFolder = ProjectFolder + "target" + FileSeparator + "classes" + FileSeparator;
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
   private final String BrowserName = "CHROME"; // FIREFOX, IE, CHROME
   private final long WaitTime = 1; // in seconds
   private final int WaitInterval = 100; // in millis
   private final int DefaultPause = 0; // in millis
   private final String ProjectName = "Selenium Based DDT Automation";
   private final String EmailSender = "retsettdd@gmail.com";
   private final String EmailPassword = "Kishkes01";
   private final String EmailRecipients = "bmelamed@microedge.com,beymelamed@gmail.com";
   private final String EmailHost = "smtp.gmail.com";
   private final String EmailPort = "587";
   private final boolean EmailAuthenticationRequired = true;
   private final boolean IsLocal = true;
   private final boolean TakeImageOnFailedStep = true;
   private final boolean ReportEachTableCell = false;
   private final boolean TabOut = true;
   private final String EventsToReport = "SKIP,INFO,FAIL";
   private final String StatusToReport = "PASS,FAIL,SKIP";
   private final String DontReportActions = "NewTest,GenerateReport,InitializeReport";
   private final String ReportElements = "status,action,locType,qryFunction,description,active,data,comments,errors,exceptionStack,exceptionCause,duration,events,screenShotFileName";
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
   private boolean takeImageOnFailedStep; // if true then an image is taken automatically on each failure (turn off while testing)
   private boolean reportEachTableCell;  // if True then reporting on table searches may be huge - use true for debugging purposes mostly
   private boolean tabOut;               // Should driver append tab to data entry string in order to tab out of it
   private long waitTime = -1;
   private int waitInterval = -1;
   private int defaultPause =-1;
   private String projectName;
   private String emailSender;
   private String emailPassword;
   private String emailRecipients;
   private String emailHost;
   private String emailPort;
   private boolean emailAuthenticationRequired;
   private String eventsToReport;
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

   private static DDTSettings ddtSettings;

   public DDTSettings () {
      loadProperties();
   }

   private  void loadProperties() {
      properties = new Properties();
      InputStream is = null;
      String blurb = "";

      String propsFileName = ResourcesFolder + "ddt.properties";
      try {
         File f = new File(propsFileName);
         is = new FileInputStream( f );
      }
      catch ( Exception e ) {
         is = null;
      }

      try {
         if ( is == null ) {
            // Try loading from classpath
            is = getClass().getResourceAsStream(ProjectFolder +"resources/ddt.properties");
         }
         // Try loading properties from the file (if found)
         properties.load( is );
         blurb = "Properties loaded from ddt.Properties file";
      }
      catch ( Exception e ) {
         blurb = "Properties file missing - Using Defaults!";
      }
      finally {
         System.out.println(blurb);
      }
   }

   public String ensureEndsWithFileSeparator (String path) {
      return (path.endsWith(File.separator)) ? path : (path + File.separator);
   }

   public static boolean isReportableAction (String action) {
      return !(Settings().dontReportActions().toLowerCase().contains(action.toLowerCase()));
   }

   public static String prefixFileNameWithPath(String fileName, String pathType) {
      String result = "";
      if (isBlank(pathType))
         return result;  // let all hell break loose - one should know better
      switch (pathType.toLowerCase()) {
         case "resource" : {result = Settings().resourcesFolder() + fileName; break;}
         case "report" : {result = Settings().reportsFolder() + fileName; break;}
         case "image" : {result = Settings().imagesFolder() + fileName; break;}
         case "data" : {result = Settings().dataFolder() + fileName; break;}
         case "script" : {result = Settings().scriptsFolder() + fileName; break;}
         case "load" : {result = Settings().classLoadFolder() + fileName; break;}
         default : {result = Settings().resourcesFolder() + fileName;}
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
      ddtSettings.setTakeImageOnFailedStep(false); // Turn off when testing
      ddtSettings.setIsLocal(true); // Turn off when testing remotely
      // Initialize various folders
      initializeFolders(ddtSettings);
      // Add global variables
      DDTestRunner.addVariable("$d", ddtSettings.itemDelim()); // Preserved variable representing a primary delimiter (e.g. between tokens in the Data property of TestItem instance)
      DDTestRunner.addVariable("$and",ddtSettings.andDelim()); // Preserved variable representing "and" delimiter for various validations like comparison mode "between"
      DDTestRunner.addVariable("$or",ddtSettings.orDelim()); // Preserved variable representing "or" delimiter for various validations like  comparison mode "or", "in"

      // Set the default class load folder - the folder optionall contains inline test string provider classes
      // We do this to keep the class 'clean' off of referencing DDT classes.
      DDTClassLoader.setDefaultLoadFolder(Settings().classLoadFolder());
   }

   /**
    * Resets the settings forcing re-reading of the properties file of the project
    */
   public static void reset() {
      ddtSettings = null;
   }

   public String[] inputSpecsArray() {
      return inputSpecs().split(TestStringsProviderSpecs.SPLITTER);
   }

   public String[] inputSpecsArrayWithDataFolder() {
      String[] result = inputSpecsArray();
      TestStringsProviderSpecs specs = new TestStringsProviderSpecs(inputSpecsArray());
      if (!specs.isSetupValid())
         return result;

      specs.ensureFilePathIsValid(dataFolder());

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
   public String inputFileName() {
      String result = "";
      try {
         if (inputType().equalsIgnoreCase("file"))
            result = inputSpecsArray()[1];
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
      String tza = timeZoneAdjustment().trim();
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
      String names = desiredCapabilityNames();
      String values = desiredCapabilityValues();
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

      String s = settings.imagesFolder();
      s = settings.resourcesFolder();
      s = settings.reportsFolder();
      s = settings.dataFolder();
      s = settings.scriptsFolder();
      s = settings.classLoadFolder();
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
    * ===================== Getters / Setters =====================
    */

   private void setValidDelims (String value) {
      validDelims = value;
   }

   public String validDelims() {
      if (isBlank(validDelims)) {
         String s = getPropertyOrDefaultValue(ValidDelims, "ValidDelims", false);
         setValidDelims(s);
      }
      return validDelims;
   }

   private void setResourcesFolder(String value) {
      resourcesFolder = value;
      DDTestRunner.addVariable("$resourcesDir",value);
   }

   public String resourcesFolder() {
      if (isBlank(resourcesFolder)) {
         String s = getPropertyOrDefaultValue(ResourcesFolder, "ResourcesFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setResourcesFolder(s);
      }
      return resourcesFolder;
   }

   private void setReportsFolder(String value) {
      reportsFolder = value;
      DDTestRunner.addVariable("$reportsDir",value);
   }

   public String reportsFolder() {
      if (isBlank(reportsFolder)) {
         String s = getPropertyOrDefaultValue(ReportsFolder, "ReportsFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setReportsFolder(s);
      }
      return reportsFolder;
   }

   private void setImagesFolder(String value) {
      imagesFolder = value;
      DDTestRunner.addVariable("$imagesDir",value);
   }

   public String imagesFolder() {
      if (isBlank(imagesFolder)) {
         String s = getPropertyOrDefaultValue(ImagesFolder, "ImagesFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setImagesFolder(s);
      }
      return imagesFolder;
   }

   private void setDataFolder(String value) {
      dataFolder = value;
      DDTestRunner.addVariable("$dataDir",value);
   }

   public String dataFolder() {
      if (isBlank(dataFolder)) {
         String s = getPropertyOrDefaultValue(DataFolder, "DataFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setDataFolder(s);
      }
      return dataFolder;
   }

   private void setScriptsFolder(String value) {
      scriptsFolder = value;
      DDTestRunner.addVariable("$scriptsDir",value);
   }

   public String scriptsFolder() {
      if (isBlank(scriptsFolder)) {
         String s = getPropertyOrDefaultValue(ScriptsFolder, "ScriptsFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setScriptsFolder(s);
      }
      return scriptsFolder;
   }

   private void setClassLoadFolder(String value) {
      classLoadFolder = value;
      DDTestRunner.addVariable("$classLoadDir",value);
   }

   public String classLoadFolder() {
      if (isBlank(classLoadFolder)) {
         String s = getPropertyOrDefaultValue(ClassLoadFolder, "ClassLoadFolder", false);
         s = ensureEndsWithFileSeparator(s.replace("%proj%", ProjectFolder));
         setClassLoadFolder(s);
      }
      return classLoadFolder;
   }

   private void setProjectName(String value) {
      projectName = value;
   }

   public String projectName() {
      if (isBlank(projectName)) {
         String s = getPropertyOrDefaultValue(ProjectName, "ProjectName", false);
         setProjectName(s);
      }
      return projectName;
   }

   private void setXslFileName(String value) {
      xslFileName = value;
   }

   public String xslFileName() {
      if (isBlank(xslFileName)) {
         String s = getPropertyOrDefaultValue(XslFileName, "XslFileName", false);
         setXslFileName(s);
      }
      return xslFileName;
   }

   private void setIEDriverFileName(String value) {
      ieDriverFileName = value;
   }

   public String ieDriverFileName() {
      if (isBlank(ieDriverFileName)) {
         String s = getPropertyOrDefaultValue(IEDriverFileName, "IEDriverFileName", false);
         s = s.replace("%proj%", ProjectFolder);
         setIEDriverFileName(s);
      }
      return ieDriverFileName;
   }

   private void setChromeDriverFileName(String value) {
      chromeDriverFileName = value;
   }

   public String chromeDriverFileName() {
      if (isBlank(chromeDriverFileName)) {
         String s = getPropertyOrDefaultValue(ChromeDriverFileName, "ChromeDriverFileName", false);
         s = s.replace("%proj%", ProjectFolder);
         setChromeDriverFileName(s);
      }
      return chromeDriverFileName;
   }

   private void setInputSpecs(String value) {
      inputSpecs = value;
   }

   public String inputSpecs() {
      if (isBlank(inputSpecs)) {
         String s = getPropertyOrDefaultValue(InputSpecs, "InputSpecs", false);
         setInputSpecs(s);
      }
      return inputSpecs;
   }

   private void setItemDelim(String value) {
      itemDelim = value;
   }

   public String itemDelim() {
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

   public String andDelim() {
      if (isBlank(andDelim))  {
         String s = getPropertyOrDefaultValue(AndDelim, "AndDelim", false);
         setAndDelim(s);
      }
      return andDelim;
   }

   public String andToken () {
      String result = andDelim();
      result = capitalize(result.replace(".", ""));
      return " " + result + " ";
   }

   private void setOrDelim(String value) {
      orDelim = value;
   }

   public String orDelim() {
      if (isBlank(orDelim))  {
         String s = getPropertyOrDefaultValue(OrDelim, "OrDelim", false);
         setOrDelim(s);
      }
      return orDelim;
   }

   public String orToken () {
      String result = orDelim();
      result = capitalize(result.replace(".", ""));
      return " " + result + " ";
   }

   private void setChromePropertyKey(String value) {
      chromePropertyKey = value;
   }

   public String chromePropertyKey() {
      if (isBlank(chromePropertyKey)) {
         String s = getPropertyOrDefaultValue(ChromePropertyKey, "ChromePropertyKey", false);
         setChromePropertyKey(s);
      }
      return chromePropertyKey;
   }

   private void setIEPropertyKey(String value) {
      iePropertyKey = value;
   }

   public String iePropertyKey() {
      if (isBlank(iePropertyKey)) {
         String s = getPropertyOrDefaultValue(IEPropertyKey, "IEPropertyKey", false);
         setIEPropertyKey(s);
      }
      return iePropertyKey;
   }

   private void setBrowserName(String value) {
      browserName = value;
   }

   public String browserName() {
      if (isBlank(browserName)) {
         String s = getPropertyOrDefaultValue(BrowserName, "BrowserName", false);
         setBrowserName(s);
      }
      return browserName;
   }

   private void setIsLocal(boolean value) {
      isLocal = value;
   }

   public boolean isLocal() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(IsLocal), "IsLocal", false);
      setIsLocal(Util.asBoolean(s));
      return isLocal;
   }

   private void setEmailAuthenticationRequired(boolean value) {
      emailAuthenticationRequired = value;
   }

   public boolean emailAuthenticationRequired() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(EmailAuthenticationRequired), "EmailAuthenticationRequired", false);
      setEmailAuthenticationRequired(Util.asBoolean(s));
      return emailAuthenticationRequired;
   }

   private void setTakeImageOnFailedStep(boolean value) {
      takeImageOnFailedStep = value;
   }

   public boolean takeImageOnFailedStep() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(TakeImageOnFailedStep), "TakeImageOnFailedStep", false);
      setTakeImageOnFailedStep(Util.asBoolean(s));
      return takeImageOnFailedStep;
   }

   private void setReportEachTableCell(boolean value) {
      reportEachTableCell = value;
   }

   public boolean reportEachTableCell() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(ReportEachTableCell), "ReportEachTableCell", false);
      setReportEachTableCell(Util.asBoolean(s));
      return reportEachTableCell;
   }

   private void setTabOut(boolean value) {
      tabOut = value;
   }

   public boolean tabOut() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(TabOut), "TabOut", false);
      setTabOut(Util.asBoolean(s));
      return tabOut;
   }
   private void setWaitTime(long value) {
      waitTime = value;
   }

   public long waitTime() {
      if (waitTime < 0L) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Long.toString(WaitTime), "WaitTime", false);
         setWaitTime(Long.valueOf(s));
      }
      return waitTime;
   }

   private void setWaitInterval(int value) {
      waitInterval = value;
   }

   public int waitInterval() {
      if (waitInterval < 0) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Integer.toString(WaitInterval), "WaitInterval", false);
         setWaitInterval(Integer.valueOf(s));
      }
      return waitInterval;
   }

   private void setDefaultPause(int value) {
      defaultPause = value;
   }

   public int defaultPause() {
      if (defaultPause < 0) /* not initialized yet */ {
         String s = getPropertyOrDefaultValue(Integer.toString(DefaultPause), "DefaultPause", false);
         setDefaultPause(Integer.valueOf(s));
      }
      return defaultPause;
   }

   private void setEmailRecipients(String value) {
      emailRecipients = value;
   }

   public String emailRecipients() {
      if (isBlank(emailRecipients)) {
         String s = getPropertyOrDefaultValue(EmailRecipients, "EmailRecipients", true);
         setEmailRecipients(s);
      }
      return emailRecipients;
   }

   private void setEmailSender(String value) {
      emailSender = value;
   }

   public String emailSender() {
      if (isBlank(emailSender)) {
         String s = getPropertyOrDefaultValue(EmailSender, "EmailSender", false);
         setEmailSender(s);
      }
      return emailSender;
   }

   private void setEmailPassword(String value) {
      emailPassword = value;
   }

   public String emailPassword() {
      if (isBlank(emailPassword)) {
         String s = getPropertyOrDefaultValue(EmailPassword, "EmailPassword", false);
         setEmailPassword(s);
      }
      return emailPassword;
   }

   private void setEmailHost(String value) {
      emailHost = value;
   }

   public String emailHost() {
      if (isBlank(emailHost)) {
         String s = getPropertyOrDefaultValue(EmailHost, "EmailHost", false);
         setEmailHost(s);
      }
      return emailHost;
   }

   private void setEmailPort(String value) {
      emailPort = value;
   }

   public String emailPort() {
      if (isBlank(emailPort)) {
         String s = getPropertyOrDefaultValue(EmailPort, "EmailPort", false);
         setEmailPort(s);
      }
      return emailPort;
   }

   private void setEventsToReport(String value) {
      eventsToReport = value;
   }

   public String eventsToReport() {
      if (isBlank(eventsToReport)) {
         String s = getPropertyOrDefaultValue(EventsToReport, "EventsToReport", false);
         setEventsToReport(s);
      }
      return eventsToReport;
   }

   private void setStatusToReport(String value) {
      statusToReport = value;
   }

   public String statusToReport() {
      if (isBlank(statusToReport)) {
         String s = getPropertyOrDefaultValue(StatusToReport, "StatusToReport", false);
         setStatusToReport(s);
      }
      return statusToReport;
   }

   private void setDontReportActions(String value) {
      dontReportActions = value;
   }

   public String dontReportActions() {
      if (isBlank(dontReportActions)) {
         String s = getPropertyOrDefaultValue(DontReportActions, "DontReportActions", false);
         setDontReportActions(s);
      }
      return dontReportActions;
   }

   private void setReportElements(String value) {
      reportElements = value;
   }

   public String reportElements() {
      if (isBlank(reportElements)) {
         String s = getPropertyOrDefaultValue(ReportElements, "ReportElements", false);
         setReportElements(s);
      }
      return reportElements;
   }

   private void setDefaultComparison(String value) {
      defaultComparison = value;
   }

   public String defaultComparison() {
      if (isBlank(defaultComparison)) {
         String s = getPropertyOrDefaultValue(DefaultComparison, "DefaultComparison", true);
         setDefaultComparison(s);
      }
      return defaultComparison;
   }

   private void setTestItemReportTemplate(String value) {
      testItemReportTemplate = value;
   }

   public String testItemReportTemplate() {
      if (isBlank(testItemReportTemplate)) {
         String s = getPropertyOrDefaultValue(TestItemReportTemplate, "TestItemReportTemplate", true);
         setTestItemReportTemplate(s);
      }
      return testItemReportTemplate;
   }

   private void setTestItemJSONTemplate(String value) {
      testItemJSONTemplate = value;
   }

   public String testItemJSONTemplate() {
      if (isBlank(testItemJSONTemplate)) {
         String s = getPropertyOrDefaultValue(TestItemJSONTemplate, "TestItemJSONTemplate", true);
         setTestItemJSONTemplate(s);
      }
      return testItemJSONTemplate;
   }

   private void setDateFormat(String value) {
      dateFormat = value;
   }

   public String dateFormat() {
      if (isBlank(dateFormat)) {
         String s = getPropertyOrDefaultValue(DateFormat, "DateFormat", true);
         setDateFormat(s);
      }
      return dateFormat;
   }

   private void setTimeStampFormat(String value) {
      timeStampFormat = value;
   }

   public String timeStampFormat() {
      if (isBlank(timeStampFormat)) {
         String s = getPropertyOrDefaultValue(TimeStampFormat, "TimeStampFormat", true);
         setTimeStampFormat(s);
      }
      return timeStampFormat;
   }

   private void setTimeZoneAdjustment(String value) {
      timeZoneAdjustment = value;
   }

   public String timeZoneAdjustment() {
      if (isBlank(timeZoneAdjustment)) {
         String s = getPropertyOrDefaultValue(TimeZoneAdjustment, "TimeZoneAdjustment", true);
         setTimeZoneAdjustment(s);
      }
      return timeZoneAdjustment;
   }

   private void setDesiredCapabilityNames(String value) {
      desiredCapabilityNames = value;
   }

   public String desiredCapabilityNames() {
      if (isBlank(desiredCapabilityNames)) {
         String s = getPropertyOrDefaultValue(DesiredCapabilityNames, "DesiredCapabilityNames", true);
         setDesiredCapabilityNames(s);
      }
      return desiredCapabilityNames;
   }

   private void setDesiredCapabilityValues(String value) {
      desiredCapabilityValues = value;
   }

   public String desiredCapabilityValues() {
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
   public boolean stripWhiteSpace() {
      // no lazy initialization as a boolean can never be null - it is a primitive
      String s = getPropertyOrDefaultValue(Util.booleanString(StripWhiteSpace), "StripWhiteSpace", false);
      setStripWhiteSpace(Util.asBoolean(s));
      return stripWhiteSpace;
   }

   private void setReportingStyle(String value) {
      reportingStyle = value;
   }

   public String reportingStyle() {
      if (isBlank(reportingStyle)) {
         String s = getPropertyOrDefaultValue(ReportingStyle, "ReportingStyle", false);
         setReportingStyle(s);
      }
      return reportingStyle;
   }

}
