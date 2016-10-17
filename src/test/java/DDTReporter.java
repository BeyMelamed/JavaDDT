import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import mx4j.tools.adaptor.http.XSLTProcessor;

import javax.mail.MessagingException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;


/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 12/30/13
 * Time: 3:28 PM
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
 * This is the DDTReporter providing reporting on 'session' and 'section' level.
 * Session level is generated at the end of the test session.
 * Section level is generated per user's request via a verb 'generateReport' - enabling reporting segmentation.
 * Note the various 'session' vs. 'section' methods below.
 * @TODO - resolve issues with invalid characters in elements or page title (e.g., char(92) messes up xml document)
 *         See http://stackoverflow.com/questions/4134438/saving-an-escape-character-0x1b-in-an-xml-file   for discussion.
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/30/13    |Bey      |Initial Version
 * 06/13/15    |Bey      |Add top of email message - same structure as the email header
 * 07/24/15    |Bey      |Various improvements & bugs (desciption handling)
 * 07/26/15    |Bey      |Implement Extent Reports
 * 08/21/15    |Bey      |Improve handling of Extent Reports (multiple files per test session)
 * 10/16/16    |Bey      |Adjust ddtSettings getters
 * ============|=========|====================================
 */
public class DDTReporter {
   /**
    * Used for creation of session summary strings
    */
   private final int _BLURB = 0;
   private final int _PROJECT = 1;
   private final int _MODULE = 2;
   private final int _SECTION = 3;
   private final int _OS = 4;
   private final int _ENV = 5;
   private final int _JAVA = 6;
   private final int _USER = 7;
   private final int _STATUS = 8;

   private static Long firstSessionStep = 0L;
   private static Long lastSessionStep = 0L;
   private static DDTDate.DDTDuration sessionDuration;
   private static DDTDate.DDTDuration sectionDuration;
   private static ExtentReports extentReport;
   private static String extentReportFileName;

   //private List<TestEvent> testEvents;
   private List<DDTReportItem> testItems;
   private boolean reportGenerated = false;
   private DDTSettings settings = DDTSettings.Settings();
   private Long firstReportStep = 0L;
   private Long lastReportStep = 0L;
   private ArrayList<String> failedTestsSummary = new ArrayList<>(); // Constructed here - will be part of email message body

   // These numbers are initialized by the calling TestRunner instance - they represent the processing counters for the current reported section
   // The corresponding numbers on the session level are taken from the DDTTestRunner static counters.
   private int nDone;
   private int nFail;
   private int nPass;
   private int nSkip;
   private String[] sessionFolders;

   public DDTReporter() {
      setSessionDuration();
      if (sectionDuration == null)
         resetDuration();
      String tmp[] = {"images", "tests"};
      sessionFolders = Util.setupReportingSessionFolders(tmp);
   }

   private static void setSessionDuration() {
      // This is a singleton, set up by the first instantiation of a member.
      if (sessionDuration == null)
         sessionDuration = new DDTDate.DDTDuration();
   }

   private static void setExtentReportFileName(String value) {
      extentReportFileName = value;
   }

   private static String getExtendReportFileName() {
      String tmp = extentReportFileName;
      if (isBlank(tmp)) {
         tmp = DDTSettings.Settings().getReportFileName();
         Date dt = new Date();
         String insert = "-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss.SSS").format(new Date()); //String.valueOf(dt.getTime());
         tmp = tmp.replace(".", insert + ".");
         setExtentReportFileName(tmp);
      }
      return extentReportFileName;
   }

   // Generate Extent Reporter Instance
   public static ExtentReports getExtentReportInstance() {
      String reportStyle = DDTSettings.Settings().getReportingStyle();
      if (!reportStyle.equalsIgnoreCase("extent"))
         return null;

      if (extentReport == null) {
         String defaultFileName = getExtendReportFileName();
         String defaultFolder = DDTSettings.Settings().getReportsFolder();
         if (!defaultFolder.endsWith("\\") && !defaultFolder.endsWith("/"))
            defaultFolder += "\\";
         defaultFileName = defaultFolder + defaultFileName;
         defaultFileName = DDTSettings.asValidOSPath(defaultFileName, true);
         extentReport = new ExtentReports(defaultFileName, true);

         // optional
         extentReport.config()
               .documentTitle(DDTSettings.Settings().getProjectName())
               .reportName("Regression")
               .reportHeadline(DDTSettings.Settings().getProjectName() + " (" + DDTSettings.Settings().getVersion() + ")");

         // optional
         extentReport
               .addSystemInfo("User Home", System.getProperty("user.home"))
               .addSystemInfo("Project Home", System.getProperty("user.dir"))
               .addSystemInfo("Java Home", System.getProperty("java.home"))
               .addSystemInfo("Country", System.getProperty("user.country"))
               .addSystemInfo("Time Zone", System.getProperty("user.timezone"));
      }
      return extentReport;
   }

   public static boolean isExtentReportInitialized() {
      return (extentReport instanceof ExtentReports);
   }

   public static ExtentTest getExtentTestInstance(TestItem testItem) {
      String reportStyle = DDTSettings.Settings().getReportingStyle();
      if (!reportStyle.equalsIgnoreCase("extent"))
         return null;

      String testDescription = testItem.getUserReport();
      if (testDescription.isEmpty())
         testDescription = testItem.getId();
      if (testDescription.isEmpty())
         testDescription = "Test Step " + testItem.paddedReportedStepNumber();

      String testName = testItem.pad() + testItem.getId() + " - " + testItem.getDescription();
      //ExtentTest test = new ExtentTest(testName, testDescription);
      ExtentTest test = getExtentReportInstance().startTest(testName, testDescription);
      return test;
   }

   public String sessionImagesFolderName() {
      return getSessionFolderName("images");
   }

   public String sessionTestsFolderName() {
      return getSessionFolderName("tests");
   }

   private String getSessionFolderName(String name) {
      String result = "";
      for (int i = 0; i < sessionFolders.length; i++) {
         if (sessionFolders[i].toLowerCase().endsWith(name.toLowerCase()))
            result = sessionFolders[i];
      }
      return result;
   }

   public static String sessionDurationString() {
      return sessionDuration.toString();
   }

   public void resetCounters(int done, int passed, int failed, int skipped) {
      nDone = done;
      nPass = passed;
      nFail = failed;
      nSkip = skipped;
   }

   public void resetDuration() {
      this.sectionDuration = new DDTDate.DDTDuration();
   }

   public void resetFailedSteps() {
      this.failedTestsSummary = new ArrayList<>();
   }

   public String durationString() {
      return sectionDuration.toString();
   }

   public boolean shouldGenerateReport() {
      return (!reportGenerated && (getDDTests().size() > 0));
   }

   /**
    * Add the report item to the list and update the result counters based on its status string (pass, fail, skip)
    *
    * @param reportItem
    */
   public void addDDTest(DDTReportItem reportItem) {
      getDDTests().add(reportItem);
      nDone++;
      if (reportItem.getStatus().equalsIgnoreCase("pass"))
         nPass++;
      if (reportItem.getStatus().equalsIgnoreCase("fail"))
         nFail++;
      if (reportItem.getStatus().equalsIgnoreCase("skip"))
         nSkip++;
   }

   private void setDDTests(List<DDTReportItem> value) {
      testItems = value;
   }

   public List<DDTReportItem> getDDTests() {
      if (testItems == null)
         setDDTests(new ArrayList<DDTReportItem>());
      return testItems;
   }

   public void reset() {
      resetDuration();
      setDDTests(new ArrayList<DDTReportItem>());
      resetFailedSteps();
      resetCounters(0, 0, 0, 0);
      setFirstReportStep(0L);
   }

   public void setFirstReportStep(Long value) {
      firstReportStep = value;
   }

   public Long firstReportStep() {
      return firstReportStep;
   }

   public void setLastReportStep(Long value) {
      lastReportStep = value;
   }

   public Long lastReportStep() {
      return lastReportStep;
   }

   public void setFirstSessionStep(Long value) {
      firstSessionStep = value;
   }

   public Long firstSessionStep() {
      return firstSessionStep;
   }

   public void setLastSessionStep(Long value) {
      lastSessionStep = value;
   }


   private static String[][] getEnvironmentItems() {

      return DDTTestRunner.getEnvironmentItems();
   }

   private String sessionPassFail() {
      return (DDTTestRunner.nSessionFail() > 0) ? "Session Failed" : "Session Passed";
   }

   private String sessionFailBlurb() {
      String result = sessionPassFail() + ": " +
            DDTTestRunner.nSessionDone() + " steps processed, " +
            DDTTestRunner.nSessionFail() + " failed, " +
            DDTTestRunner.nSessionPass() + " passed";
      if (DDTTestRunner.nSessionSkip() > 0)
         result += ", " + DDTTestRunner.nSessionSkip() + " skipped";
      result += ".";
      return result;
   }

   private String sessionPassBlurb() {
      String result = sessionPassFail() + ": " +
            DDTTestRunner.nSessionDone() + " steps processed, " +
            DDTTestRunner.nSessionPass() + " passed";
      if (DDTTestRunner.nSessionSkip() > 0)
         result += ", " + DDTTestRunner.nSessionSkip() + " skipped, ";
      result += ", none failed.";
      return result;
   }

   private String sessionSummary() {
      return DDTTestRunner.nSessionFail() > 0 ? sessionFailBlurb() : sessionPassBlurb();
   }

   private String sectionPassFail() {
      return (nFail > 0) ? "Section Failed" : "Section Passed";
   }

   private String sectionFailBlurb() {
      String result = sectionPassFail() + ": " +
            nDone + " steps processed (" + durationString() + "), " +
            nFail + " failed, " +
            nPass + " passed,";
      if (nSkip > 0)
         result += nSkip + " skipped";
      result += ".";
      return result;
   }

   private String sectionPassBlurb() {
      String result = sectionPassFail() + ": " +
            nDone + " steps processed (" + durationString() + "), " +
            nPass + " passed, ";
      if (nSkip > 0)
         result += nSkip + " skipped, ";
      result += "none failed.";
      return result;
   }

   private String sectionSummary() {
      return nFail > 0 ? sectionFailBlurb() : sectionPassBlurb();
   }

   /**
    * Place holder for generating more than one kind of report.
    *
    * @param description
    * @param emailBody
    */
   public void generateReport(String description, String emailBody) {
      String reportStyle = DDTSettings.Settings().getReportingStyle().toLowerCase();
      switch (reportStyle) {
         case "default":
            generateDefaultReport(description, emailBody);
            break;
         case "extent": {
            generateExtentReport(description, emailBody);
            break;
         }
         default:
            generateDefaultReport(description, emailBody);
      }
   }

   public void generateExtentReport(String description, String emailBody) {
      // Terminate the Exent report by flushing its contents and closing the instance.
      getExtentReportInstance().flush();
      //getExtentReportInstance().close();

      String reportFileName = getExtendReportFileName();

      String[] summaryItems = generateReportSummary(description);


      if (isBlank(settings.getEmailRecipients())) {
         System.out.println("Empty Email Recipients List - Test Results not emailed. Report Generated");
      } else {

         emailReportResults(description, emailBody, summaryItems, reportFileName);

      }

      setExtentReportFileName("");
      extentReport = null;
      reportGenerated = true;
      reset();

   }

   /**
    * Generate the following string array for creation of email and html (if reporting format calls for it):
    * [0] (blurb) {Settings.ReportTextMessage} "VM Turbo Section"
    * <p/>
    * [1] PROJECT: 'Selenium Based DDT Automation'
    * [2] MODULE: 'VM Turbo Section'
    * [3] SECTION: Test Results as of 16:34:18 - 2015, July 24 (Session duration: 00:00:42.899, Reported tests duration: 00:00:42.899)
    * [4] OS: OS Name: Windows 7, OS Version: 6.1, Browser: CHROME
    * [5] ENVIRONMENT: Country: US, Language: en, Time Zone: America/New_York
    * [6] JAVA: Version: 1.7.0_45, Home: C:\Program Files\Java\jdk1.7.0_45\jre
    * [7] USER: Name: BeyMelamed, Home: C:\Users\BeyMelamed, Project Home: C:\JavaDDT
    * [8] STATUS: Section Passed: 37 steps processed (00:00:42.900), 37 passed, 2 skipped, none failed. Session Passed: 37 steps processed, 37 passed, 2 skipped, none failed. Reportable steps included in this report: 1 thru 37 - Actions excluded from reporting: NewTest, GenerateReport, InitializeReport, SetVars - Item status included: PASS,FAIL,SKIP (un-reported action steps not counted.)
    */
   private String[] generateReportSummary(String description) {

      String[] result = new String[9];
      // Create the values for the various top sections of the report
      // Project, Module, Mode, Summary
      String[][] environmentItems = getEnvironmentItems();

      String durationBlurb = " (Session duration: " + sessionDurationString() + ", Reported tests duration: " + durationString() + ")";

      String projectName = settings.getProjectName();
      if (isBlank(projectName))
         projectName = "Selenium Based DDT Automation Project";
      String moduleName = description;
      if (isBlank(moduleName))
         moduleName = "Selenium based DDT Test Results";
      moduleName = Util.sq(moduleName) + " as of " + new SimpleDateFormat("HH:mm:ss - yyyy, MMMM dd").format(new Date()) + durationBlurb ;

      projectName = Util.sq(projectName);

      // @TODO - When documentation mode becomes available, weave that in... using "Documentation" instead of "Results"
      String mode = "Test Results";
      String osInfo = environmentItems[0][1];
      String envInfo = environmentItems[1][1];
      String javaInfo = environmentItems[2][1];
      String userInfo = environmentItems[3][1];

      //String summary = sectionSummary() + " " + sessionSummary();
      String sectionSummary = sectionSummary();
      String sessionSummary = sessionSummary();

      // String summarizing the scope of this report section
      String rangeClause = " Reportable steps included in this report: " + firstReportStep() + " thru " + DDTTestRunner.nSessionDone();
      if (lastReportStep() != firstReportStep() || isNotBlank(settings.getDontReportActions())) {
         rangeClause += " - Actions excluded from reporting: " + settings.getDontReportActions().replace(",", ", ");
      }

      String blurb = DDTSettings.Settings().getReportTextMessage();
      if (blurb.isEmpty())
         blurb = "Attached is a summary of test results titled: ";
      else
         blurb += "\n";
      blurb += description;

      String summary = rangeClause;

      summary += " - Item status included: " + settings.getStatusToReport() + " (un-reported action steps not counted.)";
      summary = summary.replaceAll(", , ", ", ");

      result[_BLURB] = blurb;
      result[_PROJECT] = projectName;
      result[_MODULE] = moduleName;
      result[_SECTION] = sectionSummary;
      result[_OS] = osInfo;
      result[_ENV] = envInfo;
      result[_JAVA] = javaInfo;
      result[_USER] = userInfo;
      result[_STATUS] = sessionSummary + rangeClause;

      return result;
   }

   /**
    * Generates the failedTestsSummary structure for placement in an email body.
    */
   private void generateFailureSections() {
      String underscore = "<br>==================<br>"; // Assuming html contents of email message
      int nFailures = 0;
      for (DDTReportItem t : getDDTests()) {

         // If step failed, add its description to the failedTestsSummary.
         if (t.hasErrors()) {
            nFailures++;
            String failureBlurb = underscore + "Failure " + nFailures + " - Step: " + t.paddedReportedStepNumber() + underscore;
            failedTestsSummary.add(failureBlurb + t.reportSummary() + "<p>Errors:</p>" + t.errorsAsHtml() + "<br>");
         }
      }
   }

   /**
    * Report Generator logic
    *
    * @param description
    * @param emailBody
    */
   public void generateDefaultReport(String description, String emailBody) {

      String fileName = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date()) + ".xml";

      if (getDDTests().size() < 1) {
         System.out.println("No Test Steps to report on.  Report Generation aborted.");
         return;
      }

      String[] summaryItems = generateReportSummary(description);

      try {

         generateAndTransformXMLOutput(fileName, summaryItems);

         reportGenerated = true;

      } catch (Exception e) {
         System.out.println("Exception Encountered while generating html report.\nReport not generated.");
         e.printStackTrace();
         return;
      }

      if (isBlank(settings.getEmailRecipients())) {
         System.out.println("Empty Email Recipients List - Test Results not emailed. Report Generated");
      } else {

         emailReportResults(description, emailBody, summaryItems, fileName);

      }

      reset();
   }

   private void emailReportResults(String description, String emailBody, String[] summaryItems, String fileName) {

      String folder = DDTSettings.Settings().getReportsFolder();
      String fileSpecs = DDTSettings.asValidOSPath(folder + File.separator + fileName, true);

      String emailSubject = "Test Results for Project: " + summaryItems[_PROJECT];

      String summary = summaryItems[_STATUS];

      String topBlurb =
            summaryItems[_BLURB] + "<br>" + "<br>" +
            "<b>PROJECT:</b>      " + summaryItems[_PROJECT] + "<br>" +
            "<b>MODULE</b>:       " + summaryItems[_MODULE] + "<br>" +
            "<b>SECTION</b>:      " + summaryItems[_SECTION] + "<br>" +
            "<b>OS</b>:           " + summaryItems[_OS] + "<br>" +
            "<b>ENVIRONMENT</b>:  " + summaryItems[_ENV] + "<br>" +
            "<b>JAVA</b>:         " + summaryItems[_JAVA] + "<br>" +
            "<b>USER</b>:         " + summaryItems[_USER] + "<br>" +
            "<b>STATUS</b>:       " + summary + "<br>";

      String extraEmailBody = (isBlank(emailBody) ? "<br>" + topBlurb : "<br>" + topBlurb + "<br>" + emailBody) + "</br>";

      String messageBody = "";
      if (isNotEmpty(description))
         messageBody = "<br>Report Title: " + Util.dq(description) + "<br>";

      messageBody += extraEmailBody;  // extraBlurb = no reportable steps

      // Generate Failure to be embedded in the body of the report (for the current section)
      generateFailureSections();

      try {
         Email.sendMail(emailSubject, messageBody, fileSpecs.replace(".xml", ".html"), failedTestsSummary);
         System.out.println("Report Generated.  Report Results Emailed to: " + settings.getEmailRecipients());
      } catch (MessagingException e) {
         System.out.println("Messaging Exception Encountered while emailing test results.\nResults not sent, Report generated.");
         e.printStackTrace();
      }

   }

   private void generateAndTransformXMLOutput(String fileName, String[] reportItems) {
      String folder = settings.getReportsFolder();

      // Ensure the folder exists - if no exception is thrown, it does!
      File tmp = Util.setupReportFolder(DDTSettings.asValidOSPath(folder, true));
      String fileSpecs = DDTSettings.asValidOSPath(folder + File.separator + fileName, true);

      String extraBlurb = "";

      int nReportableSteps = 0;
      XMLOutputFactory factory = XMLOutputFactory.newInstance();

      try {
         XMLStreamWriter writer = factory.createXMLStreamWriter(
               new FileWriter(fileSpecs));
         writer.writeStartDocument();
         writer.writeCharacters("\n");

         // build the xml hierarchy - the innermost portion of it are the steps (see below)
         // In parallel, build the top portion of the email body.
         writeStartElement(writer, "Project", new String[]{"name"}, new String[]{reportItems[_PROJECT]});
         writeStartElement(writer, "Module", new String[]{"name"}, new String[]{reportItems[_MODULE]});
         writeStartElement(writer, "Mode", new String[]{"name"}, new String[]{"Test Results"});  // Revisit... This is Resutls or Documentation
         writeStartElement(writer, "OperatingSystem", new String[]{"name"}, new String[]{reportItems[_OS]});
         writeStartElement(writer, "Environment", new String[]{"name"}, new String[]{reportItems[_ENV]});
         writeStartElement(writer, "Java", new String[]{"name"}, new String[]{reportItems[_JAVA]});
         writeStartElement(writer, "User", new String[]{"name"}, new String[]{reportItems[_USER]});
         writeStartElement(writer, "Summary", new String[]{"name"}, new String[]{reportItems[_STATUS]});
         writeStartElement(writer, "Steps");

         // Failures will be added to the mailed message body - we construct it here.
         int nFailures = 0;

         for (DDTReportItem t : getDDTests()) {
            // Only report the statuses indicated for reporting in the settings.
            if (!(settings.getStatusToReport().contains(t.getStatus())))
               continue;
            String[] attributes = new String[]{"Id", "Name", "Status", "ErrDesc"};
            String xmlItem = Util.xmlize(t.getUserReport());
            String[] values = new String[]{t.paddedReportedStepNumber(), xmlItem, t.getStatus(), t.getErrors()};
            writeStartElement(writer, "Step", attributes, values);

            writeEndElement(writer); // step
            nReportableSteps++;
         }

         // If no reportable steps recorded, write a step element to indicate so...
         if (nReportableSteps < 1) {
            extraBlurb = "*** No Reportable Steps encountered ***";
            String[] attributes = new String[]{"Id", "Name", "Status", "ErrDesc"};
            String[] values = new String[]{"------", extraBlurb, "", ""};

            writeStartElement(writer, "Step", attributes, values);
            writeEndElement(writer); // step
         }

         // close each of the xml hierarchy elements in reverse order
         writeEndElement(writer); // steps
         writeEndElement(writer); // summarywriteEndElement(writer); // user
         writeEndElement(writer); // java
         writeEndElement(writer); // environment
         writeEndElement(writer); // operating system
         writeEndElement(writer); // mode
         writeEndElement(writer); // module
         writeEndElement(writer); // project

         writer.writeEndDocument();

         writer.flush();
         writer.close();
      } catch (XMLStreamException e) {
         System.out.println("Exception encountered in xml construction: " + e.toString());
         e.printStackTrace();
         return;
      }
      catch (IOException e) {
         System.out.println("Exception encountered in xml construction: " + e.toString());
         e.printStackTrace();
         return;
      }

      /**
       * Try to transpose the xml just produced to an html file
       */
      try {
         transformXmlFileToHtml(fileSpecs, folder);
      } catch (Exception e) {
         System.out.println("Error encountered while transofrming xml file to html.\nReport not generated.");
         e.printStackTrace();
      }

   }

   private void transformXmlFileToHtml(String fileSpecs, String resultsFolder) throws IOException, TransformerException {
      try {
         String baseSpecs =  DDTSettings.asValidOSPath(fileSpecs, true);
         String htmlFileSpecs = baseSpecs.replace(".xml", ".html");
         String xslFileName = DDTSettings.asValidOSPath(settings.getXslFileName(), false);
         String xslFileSpecs =  DDTSettings.asValidOSPath(settings.getResourcesFolder() + xslFileName, false);
         //String targetFolder = resultsFolder.endsWith(File.separator) ? resultsFolder : resultsFolder + File.separator;

         StringWriter sw = new StringWriter();
         XSLTProcessor xsltProcessor = new XSLTProcessor();
         xsltProcessor.setFile(xslFileSpecs);

         URL xmlURL =  new File(baseSpecs).toURI().toURL();
         //String xmlSystemId = xmlURL.toExternalForm();
         URL xsltURL =  new File(xslFileSpecs).toURI().toURL();
         //String xsltSystemId = xsltURL.toExternalForm();
         URL htmlURL =  new File(htmlFileSpecs).toURI().toURL();
         //String htmlSystemId = htmlURL.toExternalForm();

         File xmlFile = new File(baseSpecs);
         File xsltFile = new File(xslFileSpecs);
         File htmlResult = new File(htmlFileSpecs);
         htmlResult.createNewFile();
         OutputStream htmlOutputStream = new FileOutputStream(new File(htmlFileSpecs));

         javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(xmlFile);
         javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
         javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(htmlOutputStream);

         javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance(  );
         javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
         trans.transform(xmlSource, result);

      }
      catch (IOException e ) {
         System.out.println(e.getCause().toString());
      }
      catch (TransformerException e ) {
         System.out.println(e.getCause().toString());
      }
   }

   private void writeStartElement(XMLStreamWriter writer, String name) throws XMLStreamException {
      writer.writeStartElement(name);
      writer.writeCharacters("\n");
   }

   private void writeStartElement(XMLStreamWriter writer, String name, String[] attributes, String[] values) throws XMLStreamException {
      writer.writeStartElement(name);
      for (int i = 0 ; i < attributes.length; i++) {
         writer.writeAttribute(attributes[i], values[i]);
      }
      writer.writeCharacters("\n");
   }

   private void writeEndElement(XMLStreamWriter writer) throws XMLStreamException {
      writer.writeEndElement();
      writer.writeCharacters("\n");
   }
}
