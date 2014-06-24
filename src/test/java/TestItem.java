import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang.StringUtils.*;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 3/14/14
 * Time: 12:55 PM
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
 * Description
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 03/14/14    |Bey      |Initial Version
 * ============|=========|====================================
 */
public class TestItem {

   // Instance variables *** denotes variable supplied by various data providers - the rest are created 'along the way' - during the test session
   private String id;                         // *** Step Id - Reporting property to help track test steps - documentation only
   private String action;                     // *** Vocabulary Action name - The action to be taken at that step
   private String locType;                    // *** Locator type (Css, Xpath, Id, Name, LinkText, PartialLinkText ... - for WebDriver steps
   private String locSpecs;                   // *** The string from which to create a By (chained Bys)                - for WebDriver steps
   private String qryFunction;                // *** Query (interrogation) function to use in verification (GetText, GetTitle, ...)
   private String active;                     // *** Indicates whether the step is active - a string such as 'no' or 'false' means: skip me.
   private String data;                       // *** A delimited string used for supplementing action, locator and interrogation functions - step specific
   private String description;                // *** Reporting property to help track purpose of test steps - documentation only

   private String comments;                   // Information about what took place
   private String errors;                     // Information about what went wrong
   private Long sessionStepNumber;            // The ordinal number of this step within a testing session - reporting purposes
   private Long sessionReportedStepNumber;    // The reported ordinal number of this step within a testing session - reporting purposes - counts the number of reported steps when some action are marked as 'do not report me'
   private int level;                         // The level at which this instance is invoked - inherited from the web driver
   private Exception exception;               // An exception that was triggered by this step
   private WebElement element;                // The web element used in this step
   private WebElement mrElement;              // The Most Recent web element used in previous step - used as 'anchor' element in actions requiring 'anchor' element
   private WebDriver driver;                  // The web driver used in this step
   private Date startTime;                    // The time this step was launched
   private Date currTime;                     // The time this step was done
   private List<TestEvent> testEvents;        // List of events that took place during execution of this step.
   private Hashtable<String, String> dataProperties;
   private Hashtable<String, String> savedProperties;
   private PostTestPolicy ptp;               // Post Test Policy indicating what, if anything, should be done after test item instance was executed
   private String parentInputFileName;       // The input File where 'this' testItem is found
   private String parentInputWorksheetName;  // The input worksheet name where 'this' testItem is found - applicable to spreadsheet
   private String screenShotFileName;        // The name of the Screen Shot file name - if any - taken by the webdriver upon failure
   private Long parentStepNumber;            // The step number of the instance's 'parent'
   private TestItem parentTestItem;          // The instance's parent testItem
   DDTDate.DDTDuration duration;

   public static final String LevelToken = "level";

   /**
    * Class function playing part in customizing the way a test item appears in reports -
    * User has control over this by changing strings in the ddt.prperties file
    * @param item
    * @return
    */
   public static String reportItemFromTemplate(TestItem item) {
      String[] reportElements = DDTSettings.Settings().reportElements().split(",");
      String result = DDTSettings.Settings().testItemReportTemplate();
      String open = "{";
      String close = "}";

      for (int i = 0; i < reportElements.length; i++) {
         String value = "";
         String key = reportElements[i];
         switch (key.toLowerCase()) {
            case "id" :
            {
               if (isNotBlank(item.id)) value = "Id: " + item.id ; break;
            }
            case "action" :
            {
               if (isNotBlank(item.action)) value = "Action: " + item.action ; break;
            }
            case "status" :
            {
               value = "Status: " + item.getStatus() ; break;
            }
            case "loctype" :
            {
               if (isNotBlank(item.locType)) value = "locType: " + item.locType ; break;
            }
            case "locspecs" :
            {
               if (isNotBlank(item.locSpecs)) value = "locSpecs: " + item.locSpecs ; break;
            }
            case "qryfunction" :
            {
               if (isNotBlank(item.qryFunction)) value = "qryFunction: " + item.qryFunction ; break;
            }
            case "active" :
            {
               value = item.isActive() ? "Active" : "Inactive" ; break;
            }
            case "data" :
            {
               if (isNotBlank(item.data)) value = "Data: " + item.data ; break;
            }
            case "description" :
            {
               if (isNotBlank(item.description)) value = "Description: " + item.description ; break;
            }
            case "comments" :
            {
               if (isNotBlank(item.getComments())) value = "Comments: " + item.getComments() ; break;
            }
            case "errors" :
            {
               if (isNotBlank(item.getErrors())) value = "Errors: " + item.getErrors() ; break;
            }
            case "exceptiontrace" :
            {
               if (isNotBlank(item.exceptionTrace())) value = "Trace: " + item.exceptionTrace() ; break;
            }
            case "exceptioncause" :
            {
               if (isNotBlank(item.exceptionCause())) value = "Cause: " + item.exceptionCause() ; break;
            }

            case "screenshotfilename" :
            {
               if (isNotBlank(item.getScreenShotFileName())) value = "ScreenShot: " + item.getScreenShotFileName() ; break;
            }

            case "duration" :
            {
               value = "Duration: " + item.durationString() ; break;
            }
            default: {}
         }

         if (isNotBlank(value))  {
            result = result.replace(open + key + close, value + ", ");
         }

      }

      result = Util.removeEmptyTokens(result, open, close);
      result = replace(result, ", \n", "\n");
      if (result.endsWith(", "))
         result = result.substring(0, result.length()-2);

      return result;
   }

   /**
    * The Default Constructor for data source initializing all external properties to empty strings (XML)
    */
   public TestItem() {
      id = "";
      description = "";
      action = "";
      locType = "";
      locSpecs = "";
      qryFunction = "";
      active = "";
      data = "";
   }

   /**
    * The Default Constructor for Spreadsheet data source - with all properties
    * @param _id              - TestItem's ID - reporting purposes
    * @param _action          - Encodes and action to be performed during this step
    * @param _locType         - Encodes the type of locator (By)
    * @param _locSpecs        - Contains a specific string for construction of a By object corresponding to LocType
    * @param _qryFunction     - Specifies how web element (or driver) is to be queried (GetText, GetTitle, etc.)
    * @param _active          - Indicates whether or not the step is active
    * @param _data            - Data for manipulation (UI interaction) or as needed for other commands
    * @param _description     - TestItem's Description - reporting purposes
    */
   public TestItem(
         String _id,
         String _action,
         String _locType,
         String _locSpecs,
         String _qryFunction,
         String _active,
         String _data,
         String _description)
   {

      id = _id.isEmpty()? "" : _id;
      action = _action.isEmpty()? "" : uncapitalize(_action);
      locType = _locType.isEmpty()? "" : _locType;
      locSpecs = _locSpecs.isEmpty()? "" : _locSpecs;
      qryFunction = _qryFunction.isEmpty()? "" : _qryFunction;
      active = _active.isEmpty()? "Yes" : _active;
      data = _data.isEmpty()? "" : _data;
      description = _description.isEmpty()? "" : _description;
   }

   /**
    * The initialize method adds test environment context to the collection of 8 basic strings provided by the data providers.
    * - Variable substitution.
    * - Creation of dataProperties - the hashtable containing the various indicators embedded in the 'data' property.
    * - Post Test Policy creation.
    * - Duration aspects
    * - Setting of the step number within the session
    */
   public void initialize() {

      // Setup the duration for this test item
      initDuration();

      // Substitute variables in the basic strings on the data source - each property may have substitution value in runner's dictionary
      id = Util.substituteVariables(id, DDTestRunner.getVarsMap());
      action = Util.substituteVariables(action, DDTestRunner.getVarsMap());
      locType = Util.substituteVariables(locType, DDTestRunner.getVarsMap());
      locSpecs = Util.substituteVariables(locSpecs, DDTestRunner.getVarsMap());
      qryFunction = Util.substituteVariables(qryFunction, DDTestRunner.getVarsMap());
      active = Util.substituteVariables(active, DDTestRunner.getVarsMap());
      data = Util.substituteVariables(data, DDTestRunner.getVarsMap());
      description = Util.substituteVariables(description, DDTestRunner.getVarsMap());

      // Create HashTables off of the appropriate attributes (data, locType...)
      // Values that give context to the various Vocabulary entries use the entries in the dataProperties
      if (isBlank(data)) {dataProperties = new Hashtable<String, String>();}
      else {
         dataProperties = Util.parseDelimitedString(data);
         String tmp = dataProperties.get("ptp");
         if (!isBlank(tmp)) {
            PostTestPolicy policy = new PostTestPolicy(tmp);
            if (policy.isValid()) {
               setPostTestPolicy(policy);
            }
            else
               addComment("Failed to construct Post Test Policy (" + policy.getErrors() + ")");
         }
      }

      if ((DDTestRunner.getDriver()) != null)
         driver = DDTestRunner.getDriver();

      startTime = new Date();
      currTime = startTime;

      setStepNumber();

      savedProperties = new Hashtable<String, String>();

      TestEvent event =  new TestEvent(DDTestRunner.TestEventType.INIT, "Initialized");
      this.addEvent(event);

   }

// ====================================== Start Setter / Getter Section ================================

   public void setId(String value) {
      id=value;
   }

   public String getId() {
      return id;
   }

   public void setAction(String value){
      action = value;
   }

   public String getAction() {
      return action;
   }

   public void setLocType(String value) {
      locType = value;
   }
   public String getLocType() {
      return locType;
   }

   public void setLocSpecs(String value) {
      locSpecs = value;
   }
   public String getLocSpecs() {
      return locSpecs;
   }

   public void setQryFunction(String value) {
      qryFunction = value;
   }
   public String getQryFunction() {
      return qryFunction;
   }

   public void setActive(String value) {
      active = value;
   }
   public String getSaveAs() {
      String result = dataProperties.get("saveas");
      if (isBlank(result))
         result = "";
      return result;
   }

   public void setData(String value) {
      data = value;
   }
   public String getData() {
      return data;
   }

   public void setDescription(String value) {
      description = value;
   }
   public String getDescription() {
      return description;
   }

// ====================================== End Setter / Getter Section ================================

   public void setParentStepNumber(Long value) {
      parentStepNumber = value;
   }

   public void setParentTestItem(TestItem value) {
      parentTestItem = value;
   }

   public TestItem getParentTestItem() {
      return parentTestItem;
   }

   /**
    * Used to maintain a Hashtable of level => currentTestItem
    * @return "level" + getLevel() or empty
    */
   public String getLevelKey() {
      return LevelToken + String.valueOf(level + 0);
   }

   public long getWaitTime() {
      String waitForElement;
      long result = -1;

      waitForElement =  dataProperties.get("waittime");

      result = (isBlank(waitForElement) || !isNumeric(waitForElement)) ? 0 : Long.parseLong(waitForElement);

      if (result < 1L)
         result = DDTSettings.Settings().waitTime();

      return result;
   }

   public void setDataProperties(Hashtable<String, String> value) {
      dataProperties = value;
   }

   public Hashtable<String, String> getDataProperties() {
      if (dataProperties == null)
         setDataProperties(new Hashtable<String, String>());
      return dataProperties;
   }

   public void setDriver(WebDriver value) {
      driver = value;
   }

   public WebDriver getDriver() {
      return driver;
   }

   public void setElement(WebElement value) {
      element = value;
   }
   public WebElement getElement() {
      return element;
   }

   public void setMrElement(WebElement value) {
      mrElement = value;
   }
   public WebElement getMrElement() {
      return mrElement;
   }

   public int getWaitInterval() {
      String waitInterval;
      int result = -1;

      waitInterval =  dataProperties.get("waitinterval");

      result = (isBlank(waitInterval) || !isNumeric(waitInterval)) ? 0 : Integer.parseInt(waitInterval);

      if (result < 1)
         result = DDTSettings.Settings().waitInterval();

      return result;
   }

   /**
    * Extracts the optional parameter 'queryparam' from the instance's dataProperties structure.
    * @return the optional query parameter to be used as either getAttribute(queryParam) or getCssValue(queryparam) when querying a web element
    */
   public String getQueryParam() {
      String result = dataProperties.get("queryparam");
      if (isBlank(result))
         result = "";
      return result;
   }

   public List<TestEvent> getEvents() {
      if (testEvents == null)
         testEvents = new ArrayList<TestEvent>();
      return testEvents;
   }

   public void addEvent(TestEvent event) {
      // This is where the event id is set up to be formatted like the padded step number "." {next event number}
      String id = paddedReportedStepNumber() + "." + (getEvents().size() + 1);
      event.setId(id);
      getEvents().add(event);
      setCurrTime();
   }

   public void setLevel(int aNumber) {
      level = aNumber;
   }

   public int getLevel() {
      return level;
   }
   public void addSavedProperty(String varName, String propertyValue) {
      if (isNotBlank(varName)) {
         String key = varName.toLowerCase();
         if (savedProperties.containsKey(key))
            savedProperties.remove(key);
         savedProperties.put(key, propertyValue);
      }
   }

   public void addInfoEvent(String txt) {
      TestEvent event = new TestEvent(DDTestRunner.TestEventType.INFO, txt);
      addEvent(event);
   }

   public void addPassEvent(String txt) {
      TestEvent event = new TestEvent(DDTestRunner.TestEventType.PASS, txt);
      addEvent(event);
   }

   public void addFailEvent(String txt) {
      TestEvent event = new TestEvent(DDTestRunner.TestEventType.FAIL, txt);
      addEvent(event);
   }

   public void addSkipEvent(String txt) {
      TestEvent event = new TestEvent(DDTestRunner.TestEventType.SKIP, txt);
      addEvent(event);
   }

   public int nEvents() {
      Iterator itr = getEvents().iterator();
      int i = 0;
      while (itr.hasNext())  {
         itr.next();
         i++;
      }
      return i;
   }

   private void initDuration() {
      duration = new DDTDate.DDTDuration();
   }

   public DDTDate.DDTDuration getDuration() {
      if (duration == null) {
         initDuration();
      }
      return duration;
   }

   public String durationString() {
      getDuration().setEndTime();
      return duration.toString();
   }

   /**
    *
    * @return  String indicating the status of the instance
    */
   public String getStatus() {
      String result = "INIT";
      if (nEvents() > 0 ) {
         result = (isFailure()) ? "FAIL" : (isActive() ? "PASS" : "SKIP");
      }
      return result;
   }
   /**
    * Sets this session's and instances next session and next session reported step number(s)
    */
   public void setStepNumber() {
      DDTestRunner.setNextReportingStep(action);
      sessionStepNumber = DDTestRunner.currentSessionStep();
      sessionReportedStepNumber = DDTestRunner.currentReportedSessionStep();
   }

   public String paddedReportedStepNumber() {
      return String.format("%06d", sessionReportedStepNumber);
   }

   public Long getSessionStepNumber() {
      return sessionStepNumber;
   }

   public boolean isUITest() {
      if (element instanceof WebElement)
         return true;
      if (!(isBlank(locType + locSpecs + qryFunction)))
         return true;
      return Vocabulary.getUIMethods().contains(getAction());
   }

   private void setPostTestPolicy(PostTestPolicy policy) {
      ptp = policy;
   }

   public PostTestPolicy getPostTestPolicy() {
      return ptp;
   }

   public boolean shouldQuitTestCase() {
      if ((getPostTestPolicy() instanceof PostTestPolicy)) {
         return getPostTestPolicy().shouldQuitTestCase(getStatus());
      }
      else
         return false;
   }

   public boolean shouldQuitTestSession() {
      if ((getPostTestPolicy() instanceof PostTestPolicy)) {
         return getPostTestPolicy().shouldQuitTestSession(getStatus());
      }
      else
         return false;
   }


   /**
    * Creates a string representing item as a JSON string
    * @return A string representing detailed item as a JSON string
    */
   public String asJSONString() {
      String open = "{";
      String close = "}";

      StringBuilder sb = new StringBuilder("");
      sb.append(Util.dq("id") + ":" + Util.dq(getId()) + ",");
      sb.append(Util.dq("action") + ":" + Util.dq(getAction()) + ",");
      sb.append(Util.dq("status") + ":" + Util.dq(getStatus()) + ",");
      sb.append(Util.dq("locType") + ":" + Util.dq(getLocType()) + ",");
      sb.append(Util.dq("locSpecs") + ":" + Util.dq(Util.jsonify(getLocSpecs())) + ",");
      sb.append(Util.dq("qryFunction") + ":" + Util.dq(getQryFunction()) + ",");
      sb.append(Util.dq("data") + ":" + Util.dq(Util.jsonify((getData()))) + ",");
      sb.append(Util.dq("comments") + ":" + Util.dq(Util.jsonify((getComments()))) + ",");
      sb.append(Util.dq("errors") + ":" + Util.dq(getErrors()) + ",");
      sb.append(Util.dq("description") + ":" + Util.dq(Util.jsonify((getDescription()))) + ",");
      sb.append(Util.dq("trace") + ":" + Util.dq(Util.jsonify(exceptionTrace())) + ",");
      sb.append(Util.dq("duration") + ":" + Util.dq(Util.jsonify(getDuration().toString())) + ",");
      sb.append(Util.dq("isActive") + ":" + Util.dq(isActive() ? "True" : "False") + ",");
      sb.append(Util.dq("hasErrors") + ":" + Util.dq(hasErrors() ? "True" : "False") + ",");
      sb.append(Util.dq("level") + ":" + Util.dq(String.valueOf(getLevel())) + ",");
      sb.append(Util.dq("sessionStepNumber") + ":" + Util.dq(String.valueOf(getSessionStepNumber())) + ",");
      sb.append(Util.dq("inputType") + ":" + Util.dq(getInputType()) + ",");
      sb.append(Util.dq("inputProvider") + ":" + Util.dq(Util.jsonify(getInputProvider())) + ",");
      sb.append(Util.dq("inputSegment") + ":" + Util.dq(getInputSegment()) + ",");
      sb.append(Util.dq("parentInputProvider") + ":" + Util.dq(Util.jsonify(getParentInputProvider())) + ",");
      sb.append(Util.dq("parentInputSegment") + ":" + Util.dq(Util.jsonify(getParentInputSegment())) + ",");
      sb.append(Util.dq("screenShotFileName") + ":" + Util.dq(Util.jsonify(getScreenShotFileName())));
      return open + sb.toString().replace("\n", "\\n") + close;
   }

   public int getStepsToSkip() {
      if ((getPostTestPolicy() instanceof PostTestPolicy)) {
         return getPostTestPolicy().stepsToSkip(getStatus());
      }
      else
         return 0;
   }

   public String durationText() {
      Period p = new Period(startTime.getTime(), currTime.getTime());
      String result = "";
      String prefix = "";
      if (p.getHours()> 0 ) {
         result += p.getHours() + "Hours";
         prefix = ", ";
      }

      if (p.getMinutes()> 0 ) {
         result += prefix + p.getMinutes() + "Minutes";
         prefix = ", ";
      }
      else {
         if (!isBlank(result)) {
            // non zero hours but zero minutes
            result += prefix + "0 minutes";
            prefix = ", ";
         }
      }

      if (p.getSeconds()> 0 ) {
         result += prefix + p.getSeconds() + "Seconds";
         prefix = ", ";
      }
      else {
         if (!isBlank(result)) {
            // non zero hours or minutes but zero seconds
            result += prefix + "0 seconds";
            prefix = ", ";
         }
      }

      result += prefix + p.getMillis() + "msec";
      return result;
      //return p.getHours() +  " " + p.getMinutes() + " " + p.getSeconds() + " " + p.getMillis();
      //return duration().toString();
      //return new SimpleDateFormat("HHmmss.SSS").format(duration());
   }
   /**
    * Try to get a driver even if one is not in the instance (it may be in DDTTestRunner from a previous instance)
    * If none found, load an error in testItem instance
    */
   public void getDriverIfNeeded() {
      driver = ((driver instanceof WebDriver) ? driver : DDTestRunner.getDriver());
      if (!(driver instanceof WebDriver))
         addError("*** Web Driver Not Present ***");
   }

   public boolean isActive(){return (isBlank(this.active)) ? true : Util.asBoolean(this.active);}

   @Override
   public String toString(){
      if (isEmpty())
         return "Empty Item";
      String sep = ", ";
      String s = "Id: " + this.id;
      if (!this.isActive())
         s += " (InActive)";

      if (!this.description.isEmpty()) {s += sep + "Description: " + this.description;}
      if (!this.action.isEmpty()) {s += sep + "Action: " + this.action;}
      if (!this.locType.isEmpty()) {s += sep + "locType: " + this.locType;}
      if (!this.locSpecs.isEmpty()) {s += sep + "locSpecs: " + this.locSpecs;}
      if (!this.qryFunction.isEmpty()) {s += sep + "qryFunction: " + this.qryFunction;}
      if (!this.data.isEmpty()) {s += sep + "Data: " + this.data;}

      return s;
   }

   public void addComment(String s) {
      if (isNotBlank(s)) comments = Util.append(comments, s, ", ");
   }


   public void addError(String s) {
      if (isNotBlank(s)) errors = Util.append(errors, s, ", ");
   }

   public String getErrors() {
      if (isBlank(errors))
         setErrors("");
      return errors;
   }

   public String getComments() {
      if (isBlank(comments))
            setComments("");
      return comments;
   }
   public Exception getException() {
      return exception;
   }
   public void setErrors(String errors) {
      this.errors = errors;
   }
   public void setComments(String value) {
      comments = value;
   }

   public void setException(Exception e) {
      exception = e;
   }

   public String getParentInputProvider() {
      String result = "";
      if (getParentTestItem() instanceof TestItem)
         result = getParentTestItem().getInputProvider();
      return result;
   }

   public void setParentInputWorksheetName(String value) {
      parentInputWorksheetName = value;
   }

   public String getParentInputSegment() {
      String result = "";
      if (getParentTestItem() instanceof TestItem)
         result = getParentTestItem().getInputSegment();
      return result;
   }

   public void setScreenShotFileName(String value) {
      screenShotFileName = value;
   }

   public String getScreenShotFileName() {
      if (screenShotFileName == null)
         setScreenShotFileName("");
      return screenShotFileName;
   }

   public String getScreenShotFileNameAsImgHtml() {
      if (isBlank(getScreenShotFileName()))
         return "";
      return "<img width=" +
            Util.dq("1000") +
            " src=" +
            Util.dq(getScreenShotFileName() +
            " ?raw=true") +
            " alt=" +
            Util.dq(getScreenShotFileName() +
            " ?raw=true") +
            ">";
            //<img alt="filename.png?raw=true" src="filename.png?raw=true" width="100">

   }

   public boolean shouldDebug() {
      return getDescription().toLowerCase().contains(":debug:");
   }
   public String errorsAsHtml() {

      if (this.hasErrors()) {
         return errors.replace("\n", "<br>");
      }
      else
         return "";
   }

   public Boolean hasErrors() {
      if (errors == null || errors.isEmpty()) return false; else return true;
   }

   public boolean hasComments() {
      if (comments == null || comments.isEmpty()) return false; else return true;
   }

   public boolean hasException() {
      return (exception instanceof Exception);
   }

   public boolean isFailure() {
      return (hasException() || hasErrors());
   }

   //Empty line in the spreadsheet results in an empty Test - will be skipped
   public boolean isEmpty() {
      return isBlank(id) && isBlank(action) && isBlank(locType) && isBlank(data) && isBlank(qryFunction);
   }

   public boolean shouldSaveProperty() {
      return !hasErrors() &&
            isNotBlank(locType) &&
            isNotBlank(getSaveAs()) &&
            (driver instanceof WebDriver);
   }
   public String pad() {
      return (level < 1) ? "" : StringUtils.left("-----------------", level) + " ";
   }

   public String report(){
      String result = toString();
      String prefix = "PASS: "; // We are optimists!
      if (hasException() && isBlank(errors)) {
         addError(("Exception: " + getException().getCause().toString() + " Stack: " + getException().getStackTrace()));}
      if (hasErrors())
         result = Util.append(result, errors, "\n" + pad() + "Errors: ") ;
      if (hasException()) {
         String tr = exceptionTrace();
         if (isNotBlank(tr))
            result = Util.append(result, tr, "\nTrace: ") + "\n";
      }
      if (hasComments())
         result = Util.append(result, comments, "\n" + pad() + "Comments: ");
      if (!isActive()) prefix = "SKIP: ";
      if (hasErrors()) prefix = "FAIL: ";

      return  pad() + prefix + result + " (" + durationString() + ")";
   }

   public String userReport() {
      return reportItemFromTemplate(this);
   }

   public String exceptionTrace() {
      String result = "";
      if (hasException())  {
         try {
            result = Util.stackTraceToString(getException());
         }
         catch (Exception e) {}
      }
      return result;
   }

   public String exceptionCause() {
      String result = "";
      if (hasException())  {
         try {
            result = exception.getCause().toString();
         }
         catch (Exception e) {}
      }
      return result;
   }

   // Based on the settings and the instance's events, indicate whether or not there are any events to report
   public boolean hasEventsToReport() {
      boolean result = false;
      String eventsToReport = DDTSettings.Settings().eventsToReport().toUpperCase();
      if (!isBlank(eventsToReport)) {
         for (int i = 0; i<testEvents.size();i++) {
            if (eventsToReport.contains(testEvents.get(i).getType().toString())) {
               result = true;
               break;
            }
         }
      }

      return result;
   }
   public void setCurrTime() {
      this.currTime = new Date();
   }

   /**
    * Save the properties present in the instance's savedProperties hashtable
    */
   public void saveProperties() {
      Set keys = savedProperties.entrySet();
      if (keys.size() < 1)
         return;

      String blurb = Util.populateDictionaryFromHashtable(savedProperties, DDTestRunner.getVarsMap());
      savedProperties.clear();
      addComment(blurb);
   }

   /**
    * Answer the type of the input container "file" or "inline" or ""
    * @return String
    */
   public String getInputType() {
      String specs = getDataProperties().get("inputspecs");
      if (isBlank(specs))
         return "";
      String tmp[] = specs.split(":");
      String result = tmp[0];
      return result;
   }

   /**
    * Answer the name of the input container (file name or inline testitems provider)
    * @return String
    */
   public String getInputProvider() {
      String specs = getDataProperties().get("inputspecs");
      if (isBlank(specs))
         return "";
      String tmp[] = specs.split(":");
      String result = tmp[1];
      if (isBlank(result))
         return "";
      if (tmp[0].equalsIgnoreCase("inline"))
         return result;
      result = result.replace("%data%", DDTSettings.Settings().dataFolder());
      if ((indexOf(result,'/') < 0) && (indexOf(result,"\\")) < 0 )
         result = DDTSettings.Settings().dataFolder() + result;
      return result;
   }

   /**
    * Answer the name of the instance's provider segment (WorksheetName  - if any) where 'child' testItems are found
    * @return String - Worksheet name
    */
   public String getInputSegment() {
      String specs = getDataProperties().get("inputspecs");
      if (isBlank(specs))
         return "";
      String[] tmp =specs.split(":");
      String result = "";
      if (tmp.length > 2)
         result = tmp[2];
      if (isBlank(result))
         result = "";
      return result;
   }

   /**
    * Created with IntelliJ IDEA.
    * User: Avraham (Bey) Melamed
    * Date: 6/07/14
    * Time: 12:55 PM
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
    * Description
    * Aggregate of TestItem instances
    * History
    * When        |Who      |What
    * ============|=========|====================================
    * 06/07/14    |Bey      |Initial Version
    * ============|=========|====================================
    */
   public static class TestItems {

      private TestItem[] testItems = new TestItem[0];
      private TestItem parentItem = null;

      public TestItems() {

      }

      public void setItems(TestItem[] items) {
         testItems = items;
      }

      public TestItem[] getItems() {
         return testItems;
      }

      public int getSize() {
         if (getItems() == null)
            return 0;
         return getItems().length;
      }

      public String getInputProvider() {
         String result = "";
         if (getParentItem() instanceof TestItem) {
            result = getParentItem().getInputProvider();
         }
         else
            result = DDTSettings.Settings().inputFileName();
         return result;
      }

      public String getInputSegment() {
         String result = "";
         if (getParentItem() instanceof TestItem) {
            result = getParentItem().getInputSegment();
         }
         return result;
      }

      public void setParentItem(TestItem value) {
         parentItem = value;
      }

      public TestItem getParentItem() {
         return parentItem;
      }

      /**
       * Return a json File Name that is composed of the parent's test session step number, file name, the worksheet if any...
       * @return
       */
      public String getJSONFileName() {
         String fileName = "";
         String worksheetName = "";
         if (getParentItem() instanceof TestItem)
            fileName=getInputProvider();
         else
            fileName="root";

         if (isBlank(fileName))
            return fileName;  // should never happen

         if (fileName != "root") {
            worksheetName = getInputSegment();

            if (isBlank(worksheetName))
               worksheetName = ""; // Not every input is from a spreadsheet - this is valid
            if (!isBlank(worksheetName))
               worksheetName = "_" + worksheetName;

            fileName = fileName.replace("\\", "~");
            fileName = fileName.replace("/", "~");
            String[] nameArray = fileName.split("~");
            fileName = nameArray[nameArray.length - 1];
            if (fileName.toLowerCase().endsWith(".xml") || fileName.toLowerCase().endsWith(".xls"))
               fileName = fileName.substring(0, fileName.length() - 4);
            if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".html"))
               fileName = fileName.substring(0, fileName.length() - 5);
            if (fileName.equalsIgnoreCase("root")) {
               fileName += "_" + new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date());
            }
            fileName = getParentItem().paddedReportedStepNumber() + "_" + fileName;
         }

         // Return the file name without the folder
         return fileName + worksheetName + ".json";
      }

      public int countItemsStatus(String status) {
         if (getItems().length < 1)
            return 0;
         int result = 0;
         for (int i = 0; i < getSize(); i++ ) {
            if (getItems()[i].getStatus().equalsIgnoreCase(status))
               result++;
         }
         return result;
      }

      public int getNPass() {
         return countItemsStatus("pass");
      }

      public int getNFail() {
         return countItemsStatus("fail");
      }

      public int getNSkip() {
         return countItemsStatus("skip");
      }

      /**
       * Creates a JSON string representing this section of test steps
       * @return
       */
      public String asJSONArray() {
         StringBuilder sb = new StringBuilder("");
         // Section Summary
         sb.append(Util.dq("inputProvider") + ":" + Util.dq(Util.jsonify(getInputProvider())) + ",");
         sb.append(Util.dq("inputSegment") + ":" + Util.dq(getInputSegment()) + ",");
         sb.append(Util.dq("jsonFileName") + ":" + Util.dq(Util.jsonify((getJSONFileName()))) + ",");
         sb.append(Util.dq("size") + ":" + Util.dq(String.valueOf(getSize())) + ",");
         sb.append(Util.dq("nPass") + ":" + Util.dq(String.valueOf(getNPass())) + ",");
         sb.append(Util.dq("nFail") + ":" + Util.dq(String.valueOf(getNFail())) + ",");
         sb.append(Util.dq("nSkip") + ":" + Util.dq(String.valueOf(getNSkip())));

         if (getSize() < 1)
            return "{" + sb.toString() + "}";

         sb.append("," + Util.dq("tests") + ":[");
         for (int i = 0 ; i < getSize(); i++) {
            sb.append((getItems()[i]).asJSONString() + (i < getSize() -1 ? "," : ""));
         }
         sb.append("]");
         return "{" + sb.toString() + "}";
      }

      public void reportAsJSON() {
         String jsonRepresentation = asJSONArray();
         String outputFileName = getJSONFileName();
         if (isBlank(outputFileName)) {
            System.out.println("Test Case Not Reported - Empty File Name Encountered!");
           return;
         }
         outputFileName = DDTestRunner.getReporter().sessionTestsFolderName() + DDTSettings.Settings().fileSep() + outputFileName;
         Util.fileWrite(outputFileName, jsonRepresentation);
      }

   }

}


