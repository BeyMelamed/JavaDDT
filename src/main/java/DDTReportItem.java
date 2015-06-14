import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Created by BeyMelamed on 2/13/14.
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
 * <p>
 *    Instances of this class are items the DDTReport uses in its 'default' reporting mode.
 *    These items can derive from either TestItem instance or Verb with a DDTestContext
 * <p/>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 11/16/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class DDTReportItem extends DDTBase {
   private String userReport;
   private String status;
   private Long sessionStepNumber = 0L;
   private DDTTestContext testContext = null;
   private String description;
   private String id;

   public DDTReportItem() {

   }

   /**
    * Set an instance of DDTReportItem from a TestItem instance - this replicates the original reporting methodology
    * @param testItem
    */
   public DDTReportItem(TestItem testItem) {
      setUserReport(testItem.getUserReport());
      setStatus(testItem.getStatus());
      addError(testItem.getErrors());
      addComment(testItem.getComments());
      setSessionStepNumber(testItem.getSessionStepNumber());
      setId(testItem.getId());
      setDescription(testItem.getDescription());
   }

   public void setSessionStepNumber(Long value) {
      sessionStepNumber = value;
   }

   public Long getSessionStepNumber () {
      if (sessionStepNumber == null)
         setSessionStepNumber("testing"); // The 'testing' screen should 'consider' this item as a reportable step
      return sessionStepNumber;
   }

   public void setDescription(String value) {
      description = value;
   }

   public String getDescription() {
      return description;
   }

   public void setId(String value) {
      id = value;
   }

   public String getId() {
      return id;
   }

   /**
    * Sets this session's and instances next session and next session reported step number(s)
    * This should be called from a test context not based on TestItem instance
    */
   public void setSessionStepNumber(String action) {
      DDTTestRunner.setNextReportingStep(action);
      sessionStepNumber = DDTTestRunner.currentSessionStep();
   }

   public String paddedReportedStepNumber() {
      return String.format("%06d", getSessionStepNumber());
   }

   public void setStatus(String value) {
      status = value;
   }

   public String getStatus() {
      return status;
   }

   public void setUserReport(String value) {
      userReport = value;
   }

   public String getUserReport() {
      return userReport;
   }

   public void setTestContext(DDTTestContext tc) {
      testContext = tc;
   }

   public String errorsAsHtml() {

      if (this.hasErrors()) {
         return getErrors().replace("\n", "<br>");
      }
      else
         return "";
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("");
      if (testContext instanceof DDTTestContext) {
         sb.append(testContext.toString());
      }

      if (!isBlank(getComments())) {
         if (sb.length() > 0)
            sb.append(", ");
         sb.append("Comments: " + getComments());
      }

      if (!isBlank(getErrors())) {
         if (sb.length() > 0)
            sb.append(", ");
         sb.append("Errors: " + getErrors());
      }

      return sb.toString();
   }

   public String reportSummary() {
      StringBuilder sb = new StringBuilder("");

      if (!isBlank(getId())) {
         sb.append("ID: " + getId());
      }

      if (!isBlank(getDescription())) {
         if (sb.length() > 0)
            sb.append(", ");
         sb.append("Description: " + getDescription());
      }

      if (!isBlank(getComments())) {
         if (sb.length() > 0)
            sb.append(", ");
         sb.append("Comments: " + getComments());
      }

      return sb.toString();
   }


}
