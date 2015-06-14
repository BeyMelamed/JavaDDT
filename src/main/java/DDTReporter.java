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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/30/13    |Bey      |Initial Version
 * 06/13/15    |Bey      |Add top of email message - same structure as the email header
 * ============|=========|====================================
 */
public class DDTReporter {

   private static Long firstSessionStep = 0L;
   private static Long lastSessionStep = 0L;
   private static DDTDate.DDTDuration sessionDuration;
   private static DDTDate.DDTDuration sectionDuration;

   //private List<TestEvent> testEvents;
   private List<DDTReportItem> testItems;
   private boolean reportGenerated=false;
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
   public static String sessionDurationString () {
      return sessionDuration.toString();
   }

   public void resetCounters(int done, int passed, int failed, int skipped) {
      nDone = done;
      nPass = passed;
      nFail = failed;
      nSkip = skipped;
   }

   public void resetDuration () {
      this.sectionDuration = new DDTDate.DDTDuration();
   }

   public void resetFailedSteps () {
      this.failedTestsSummary = new ArrayList<>();
   }

   public String durationString () {
      return sectionDuration.toString();
   }

   public boolean shouldGenerateReport() {
      return (!reportGenerated && (getDDTests().size() > 0));
   }

   /**
    * Add the report item to the list and update the result counters based on its status string (pass, fail, skip)
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

   private void setDDTests (List<DDTReportItem> value) {
      testItems = value;
   }

   public List<DDTReportItem> getDDTests () {
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

   private String sessionFailBlurb () {
      String result = sessionPassFail() + ": " +
            DDTTestRunner.nSessionDone() + " steps processed, " +
            DDTTestRunner.nSessionFail() + " failed, " +
            DDTTestRunner.nSessionPass() + " passed";
      if (DDTTestRunner.nSessionSkip() > 0)
         result += ", " + DDTTestRunner.nSessionSkip() + " skipped";
      result += ".";
      return result;
   }

   private String sessionPassBlurb () {
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

   private String sectionFailBlurb () {
      String result = sectionPassFail() + ": " +
            nDone + " steps processed (" + durationString() +"), "  +
            nFail + " failed, " +
            nPass + " passed,";
      if (nSkip > 0)
         result += nSkip + " skipped";
      result += ".";
      return result;
   }

   private String sectionPassBlurb () {
      String result = sectionPassFail() + ": " +
            nDone + " steps processed (" + durationString() +"), "  +
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
    * @param description
    * @param emailBody
    */
   public void generateReport(String description, String emailBody) {
      String reportStyle = DDTSettings.Settings().reportingStyle().toLowerCase();
      switch (reportStyle) {
         case "default" : generateDefaultReport(description, emailBody); break;
         default: generateDefaultReport(description, emailBody);
      }
   }

   /**
    * Report Generator logic
    * @param description
    * @param emailBody
    */
   public void generateDefaultReport(String description, String emailBody) {

      if (getDDTests().size() < 1) {
         System.out.println("No Test Steps to report on.  Report Generation aborted.");
         return;
      }

      // Create the values for the various top sections of the report
      // Project, Module, Mode, Summary
      String[][] environmentItems = getEnvironmentItems();

      String projectName = settings.projectName();
      if (isBlank(projectName))
         projectName = "Selenium Based Java DDT Automation Project";
      String moduleName = description;
      if (isBlank(moduleName))
         moduleName = "Selenium based Java DDT Test Results";

      projectName = Util.sq(projectName);
      moduleName = Util.sq(moduleName);

      String durationBlurb = " (Session duration: " + sessionDurationString() + ", Reported tests duration: " + durationString() + ")";
      // @TODO - When documentation mode becomes available, weave that in... using "Documentation" instead of "Results"
      String mode = "Test Results as of " + new SimpleDateFormat("HH:mm:ss - yyyy, MMMM dd").format(new Date()) + durationBlurb;
      String osInfo = environmentItems[0][1];
      String envInfo = environmentItems[1][1];
      String javaInfo = environmentItems[2][1];
      String userInfo = environmentItems[3][1];

      String summary = sectionSummary() + " " + sessionSummary();

      // String summarizing the scope of this report section
      String rangeClause = " Reportable steps included in this report: " + firstReportStep() + " thru " + (lastReportStep());
      if (lastReportStep() != firstReportStep() || isNotBlank(settings.dontReportActions()) ) {
         rangeClause += " - Actions excluded from reporting: " + settings.dontReportActions().replace(",", ", ");
      }

      String underscore = "<br>==================<br>"; // Assuming html contents of email message

      String emailSubject = "Test Results for Project: " + projectName + ", Section: " + moduleName;
      summary += rangeClause;

      summary += " - Item status included: " + settings.statusToReport() + " (un-reported action steps not counted.)";
      summary = summary.replaceAll(", , ", ", ");

      String fileName = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date()) + ".xml";
      String folder = settings.reportsFolder() + Util.asSafePathString(description);

      // Ensure the folder exists - if no exception is thrown, it does!
      File tmp = Util.setupReportFolder( DDTSettings.asValidOSPath(folder, true));
      String fileSpecs = folder + File.separator + DDTSettings.asValidOSPath(fileName, true);

      String extraBlurb = "";

      int nReportableSteps = 0;
      XMLOutputFactory factory      = XMLOutputFactory.newInstance();

      try {
         XMLStreamWriter writer = factory.createXMLStreamWriter(
               new FileWriter(fileSpecs));

         writer.writeStartDocument();
         writer.writeCharacters("\n");

         // build the xml hierarchy - the innermost portion of it are the steps (see below)
         // In parallel, build the top portion of the email body.
         writeStartElement(writer, "Project", new String[] {"name"}, new String[] {projectName});
         writeStartElement(writer, "Module", new String[] {"name"}, new String[] {moduleName});
         writeStartElement(writer, "Mode", new String[] {"name"}, new String[] {mode});
         writeStartElement(writer, "OperatingSystem", new String[] {"name"}, new String[] {osInfo});
         writeStartElement(writer, "Environment", new String[] {"name"}, new String[] {envInfo});
         writeStartElement(writer, "Java", new String[] {"name"}, new String[] {javaInfo});
         writeStartElement(writer, "User", new String[] {"name"}, new String[] {userInfo});
         writeStartElement(writer, "Summary", new String[] {"name"}, new String[] {summary});
         writeStartElement(writer, "Steps");

         // Failures will be added to the mailed message body - we construct it here.
         int nFailures = 0;

         for (DDTReportItem t : getDDTests()) {
            // Only report the statuses indicated for reporting in the settings.
            if (!(settings.statusToReport().contains(t.getStatus())))
               continue;
            String[] attributes =   new String[] {"Id", "Name", "Status", "ErrDesc"};
            String xmlItem = Util.xmlize(t.getUserReport());
            if (xmlItem != t.getUserReport())
               System.out.println("Original: " + t.getUserReport() + "\nAfter: " + xmlItem);
            String[] values = new String[] {t.paddedReportedStepNumber(), xmlItem, t.getStatus(), t.getErrors()};
            writeStartElement(writer, "Step",attributes, values);

            // If step failed, add its description to the failedTestsSummary.
            if (t.hasErrors()) {
               nFailures++;
               String failureBlurb = underscore + "Failure " + nFailures + " - Step: " + t.paddedReportedStepNumber() + underscore;
               failedTestsSummary.add(failureBlurb + t.reportSummary() + "<p>Errors:</p>" + t.errorsAsHtml() + "<br>");
            }

            writeEndElement(writer); // step
            nReportableSteps++;
         }

         // If no reportable steps recorded, write a step element to indicate so...
         if (nReportableSteps < 1) {
            extraBlurb = "*** No Reportable Steps encountered ***";
            String[] attributes =   new String[] {"Id", "Name", "Status", "ErrDesc"};
            String[] values = new String[] {"------", extraBlurb, "", ""};

            writeStartElement(writer, "Step",attributes, values);
            writeEndElement(writer); // step
         }

         // close each of the xml hierarchy elements in reverse order
         writeEndElement(writer); // steps
         writeEndElement(writer); // summary
         writeEndElement(writer); // user
         writeEndElement(writer); // java
         writeEndElement(writer); // environment
         writeEndElement(writer); // operating system
         writeEndElement(writer); // mode
         writeEndElement(writer); // module
         writeEndElement(writer); // project

         writer.writeEndDocument();

         writer.flush();
         writer.close();

         try {
            transformXmlFileToHtml(fileSpecs, folder);
         }
         catch (Exception e ) {
            System.out.println("Error encountered while transofrming xml file to html.\nReport not generated.");
            e.printStackTrace();
            return;
         }

         reportGenerated = true;

      } catch (XMLStreamException e) {
         System.out.println("XML Stream Exception Encountered while transforming xml file to html.\nReport not generated.");
         e.printStackTrace();
         return;
      } catch (IOException e) {
         System.out.println("IO Exception Encountered while transforming xml file to html.\nReport not generated.");
         e.printStackTrace();
         return;
      }

      String topBlurb =
            "<b>PROJECT:</b>      " + projectName + "<br>" +
            "<b>MODULE</b>:       " + moduleName + "<br>" +
            "<b>SECTION</b>:      " + mode + "<br>" +
            "<b>OS</b>:           " + osInfo + "<br>" +
            "<b>ENVIRONMENT</b>:  " + envInfo +  "<br>" +
            "<b>JAVA</b>:         " + javaInfo + "<br>" +
            "<b>USER</b>:         " + userInfo + "<br>" +
            "<b>STATUS</b>:       " + summary + "<br>";

      String extraEmailBody = (isBlank(emailBody) ? "<br>" + topBlurb : "<br>" + topBlurb + "<br>" + emailBody) + "</br>";


      if (isBlank(settings.emailRecipients())) {
         System.out.println("Empty Email Recipients List - Test Results not emailed. Report Generated");
      }
      else {
         String messageBody = "Attached is a summary of test results run titled " + Util.dq(description) + "<br>" + (isBlank(extraBlurb) ? "" : "<br>" + extraBlurb) + extraEmailBody;
         try {
            Email.sendMail(emailSubject, messageBody, fileSpecs.replace(".xml", ".html"), failedTestsSummary);
            System.out.println("Report Generated.  Report Results Emailed to: " + settings.emailRecipients());
         }
         catch (MessagingException e){
            System.out.println("Messaging Exception Encountered while emailing test results.\nResults not sent, Report generated.");
            e.printStackTrace();
         }
      }

      reset();
   }

   private void transformXmlFileToHtml(String fileSpecs, String resultsFolder) throws IOException, TransformerException {
      try {
         String baseSpecs =  DDTSettings.asValidOSPath(fileSpecs, true);
         String htmlFileSpecs = baseSpecs.replace(".xml", ".html");
         String xslFileName = DDTSettings.asValidOSPath(settings.xslFileName(), false);
         String xslFileSpecs =  DDTSettings.asValidOSPath(settings.resourcesFolder() + xslFileName, false);
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
