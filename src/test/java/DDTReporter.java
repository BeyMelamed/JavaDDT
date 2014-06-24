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
 * ============|=========|====================================
 */
public class DDTReporter {

   private static Long firstSessionStep = 0L;
   private static Long lastSessionStep = 0L;
   private static DDTDate.DDTDuration sessionDuration;
   private static DDTDate.DDTDuration sectionDuration;

   //private List<TestEvent> testEvents;
   private List<TestItem> testItems;
   private boolean reportGenerated=false;
   private DDTSettings settings = DDTSettings.Settings();
   private Long firstReportStep = 0L;
   private Long lastReportStep = 0L;
   private List<String> failedTestsSummary = new ArrayList<String>(); // Constructed here - will be part of email message body

   // These numbers are initialized by the calling TestRunner instance - they represent the processing counters for the current reported section
   // The corresponding numbers on the session level are taken from the DDTestRunner static counters.
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
      this.failedTestsSummary = new ArrayList<String>();
   }

   public String durationString () {
      return sectionDuration.toString();
   }

   public boolean shouldGenerateReport() {
      return (!reportGenerated && (getDDTests().size() > 0));
   }

   public void addDDTest(TestItem testItem) {
      getDDTests().add(testItem);
   }

   private void setDDTests (List<TestItem> value) {
      testItems = value;
   }

   public List<TestItem> getDDTests () {
      if (testItems == null)
         setDDTests(new ArrayList<TestItem>());
      return testItems;
   }

   public void reset() {
      resetDuration();
      setDDTests(new ArrayList<TestItem>());
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
      return DDTestRunner.getEnvironmentItems();
   }

   private String sessionPassFail() {
      return (DDTestRunner.nSessionFail() > 0) ? "Session Failed" : "Session Passed";
   }

   private String sessionFailBlurb () {
      String result = sessionPassFail() + ": " +
            DDTestRunner.nSessionDone() + " steps processed, " +
            DDTestRunner.nSessionFail() + " failed, " +
            DDTestRunner.nSessionPass() + " passed";
      if (DDTestRunner.nSessionSkip() > 0)
         result += ", " + DDTestRunner.nSessionSkip() + " skipped";
      result += ".";
      return result;
   }

   private String sessionPassBlurb () {
      String result = sessionPassFail() + ": " +
            DDTestRunner.nSessionDone() + " steps processed, " +
            DDTestRunner.nSessionPass() + " passed";
      if (DDTestRunner.nSessionSkip() > 0)
         result += ", " + DDTestRunner.nSessionSkip() + " skipped, ";
      result += ", none failed.";
      return result;
   }

   private String sessionSummary() {
      return DDTestRunner.nSessionFail() > 0 ? sessionFailBlurb() : sessionPassBlurb();
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

      String extraEmailBody = (isBlank(emailBody) ? "" : "<br>" + emailBody) + "</br>";

      // Create the values for the various top sections of the report
      // Project, Module, Mode, Summary
      String[][] environmentItems = getEnvironmentItems();

      String projectName = settings.projectName();
      if (isBlank(projectName))
         projectName = "Selenium DDT Automation Project";
      String moduleName = description;
      if (isBlank(moduleName))
         moduleName = "Selenium based DDT Test Results";

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
      String rangeClause = "  Reportable steps included in this report: " + firstReportStep() + " thru " + (lastReportStep());
      if (lastReportStep() != firstReportStep() || isNotBlank(settings.dontReportActions()) ) {
         rangeClause += " - Actions excluded from reporting: " + settings.dontReportActions().replace(",", ", ");
      }

      String underscore = "<br>==================<br>"; // Assuming html contents of email message

      String emailSubject = "Test Results for Project: " + projectName + ", Section: " + moduleName + " - " + summary;
      summary += rangeClause;

      summary += " - Only test items with status of: " + settings.statusToReport().replace(",", ", ") + " are included (un-reported action steps not counted.)";

      String fileName = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date()) + ".xml";
      String folder = settings.reportsFolder() + Util.asSafePathString(description);

      // Ensure the folder exists - if no exception is thrown, it does!
      File tmp = Util.setupReportFolder(folder);
      String fileSpecs = folder + settings.fileSep() + fileName;

      String extraBlurb = "";
      int nReportableSteps = 0;
      XMLOutputFactory factory      = XMLOutputFactory.newInstance();

      try {
         XMLStreamWriter writer = factory.createXMLStreamWriter(
               new FileWriter(fileSpecs));

         writer.writeStartDocument();
         writer.writeCharacters("\n");

         // build the xml hierarchy - the innermost portion of it are the steps (see below)
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

         for (TestItem t : getDDTests()) {
            // Only report the statuses indicated for reporting in the settings.
            if (!(settings.statusToReport().contains(t.getStatus())))
               continue;
            String[] attributes =   new String[] {"Id", "Name", "Status", "ErrDesc"};
            String[] values = new String[] {t.paddedReportedStepNumber(), t.userReport(), t.getStatus(), t.getErrors()};
            writeStartElement(writer, "Step",attributes, values);

            // If step failed, add its description to the failedTestsSummary.
            if (t.hasErrors()) {
               nFailures++;
               String failureBlurb = underscore + "Failure " + nFailures + " - Step: " + t.paddedReportedStepNumber() + underscore;
               failedTestsSummary.add(failureBlurb + t.toString() + "<p>Errors:</p>" + t.errorsAsHtml() + "<br>");
            }

            // If step has any events to report - list those
            if (t.hasEventsToReport()) {
               String eventsToReport = settings.eventsToReport();
               writeStartElement(writer, "Events");
               for (TestEvent e : t.getEvents())
               {
                  if (eventsToReport.contains(e.getType().toString()))  {
                     writeStartElement(writer, "Event", new String[] {"name"}, new String[] {e.toString()});
                     writeEndElement(writer);
                  }
               }
               writeEndElement(writer); // step's events
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

         };

         reportGenerated = true;

      } catch (XMLStreamException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      if (isBlank(settings.emailRecipients())) {
         System.out.println("Empty Email Recipients List - Test Results not emailed.");
      }
      else {
         String messageBody = "Attached, please see a summary of test results run titled " + Util.dq(description) + "<br>" + (isBlank(extraBlurb) ? "" : "<br>" + extraBlurb) + extraEmailBody;
         try {
            Email.sendMail(emailSubject, messageBody, fileSpecs.replace(".xml", ".html"), failedTestsSummary);
         }
         catch (MessagingException e){
            e.printStackTrace();
         }
      }

      reset();
   }

   private void transformXmlFileToHtml(String fileSpecs, String resultsFolder) throws IOException, TransformerException {
      try {
         String htmlFileSpecs = fileSpecs.replace(".xml", ".html");
         String xslFileName = settings.xslFileName();
         String xslFileSpecs = settings.resourcesFolder() + xslFileName;
         String targetFolder = resultsFolder.endsWith(settings.fileSep()) ? resultsFolder : resultsFolder + settings.fileSep();

         StringWriter sw = new StringWriter();
         XSLTProcessor xsltProcessor = new XSLTProcessor();
         xsltProcessor.setFile(xslFileSpecs);

         URL xmlURL =  new File(fileSpecs).toURI().toURL();
         String xmlSystemId = xmlURL.toExternalForm();
         URL xsltURL =  new File(xslFileSpecs).toURI().toURL();
         String xsltSystemId = xsltURL.toExternalForm();
         URL htmlURL =  new File(htmlFileSpecs).toURI().toURL();
         String htmlSystemId = htmlURL.toExternalForm();

         File xmlFile = new File(fileSpecs);
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
