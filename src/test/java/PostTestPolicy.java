import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by BeyMelamed on 2/17/14.
 * Selenium Based Automation Project.
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
 * An instance of this class contains the logic of what needs to be done when a test passes or fails
 * A policy instructs the test runner logic to act upon an actionEvent (FAIL, PASS or ANY) by either quitting a test unit (TEST_CASE or TEST_SESSION) or skipping one or more test steps.
 * </p>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 02/17/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class PostTestPolicy {
   private final String ValidTokens = "QSU,QTU,QTOF,QTOP,QSOF,QTOF,SOF,SOP,SUN";
   private String actionEvent; // The event upon which the policy acts (PASS, FAIL, ANY)
   private String quitUnit;   // Applicable to quitting policy (as opposed to skipping policy) - Either TES_TCASE or TEST_SESSION
   private int stepsToSkip; // Number of steps to skip (applies to a skipping policy)
   private String errors;  // Collects errors in construction logic of instance or action to take
   private String description;  // Facilitate reporting / explaining
   private String origin; // The original text from which the policy was derived.

   public PostTestPolicy () {

   }

   public PostTestPolicy (String input) {
      initialize(input);
   }

   private void initialize (String input) {

      setOrigin(input);

      if (isBlank(input)) {
         setErrors("Cannot initialize instance from blank string.");
         return;
      }

      if (input.length() < 3) {
         setErrors("Invalid input " + Util.sq(input) + " instance cannot be initialized.");
         return;
      }

      String prefix = input.substring(0,3).toUpperCase();
      if (ValidTokens.contains(prefix)) {
         switch (prefix) {
            case "QSU": {
               setActionEvent("ANY");
               setQuitUnit("TEST_SESSION");
               setDescription("Quit Session Unconditionally");
               break;
            }
            case "QTU": {
               setActionEvent("ANY");
               setQuitUnit("TEST_CASE");
               setDescription("Quit Test Case Unconditionally");
               break;
            }
            case "QTO": {
               if (input.equalsIgnoreCase("QTOP"))
                  setActionEvent("PASS");
               if (input.equalsIgnoreCase("QTOF"))
                  setActionEvent("FAIL");
               if (isBlank(getActionEvent())) {
                  setErrors("Incorrect Test Case Quitting Policy specified, use 'QTOP' or QTOF'");
               }
               else {
                  setQuitUnit("TEST_CASE");
                  setDescription("Quit Test Case On " + ((getActionEvent().equalsIgnoreCase("PASS")) ? "Pass" : "Fail"));
               }
               break;
            }
            case "QSO": {
               if (input.equalsIgnoreCase("QSOP"))
                  setActionEvent("PASS");
               if (input.equalsIgnoreCase("QSOF"))
                  setActionEvent("FAIL");
               if (isBlank(getActionEvent())) {
                  setErrors("Incorrect Test Session Quitting Policy specified, use 'QSOP' or QSOF'");
               }
               else {
                  setQuitUnit("TEST_SESSION");
                  setDescription("Quit Test Session On " + ((getActionEvent().equalsIgnoreCase("PASS")) ? "Pass" : "Fail"));
               }
               break;
            }

            case "SOF": {
               setActionEvent("FAIL");
               setStepToSkip();
               if (this.isValid())
                  setDescription("Skip " + String.valueOf(getStepsToSkip()) + " Steps On Fail");
               break;
            }

            case "SOP": {
               setActionEvent("PASS");
               setStepToSkip();
               if (this.isValid())
                  setDescription("Skip " + String.valueOf(getStepsToSkip()) + " Steps On Pass");
               break;
            }
            case "SUN": {
               setActionEvent("ANY");
               setStepToSkip();
               if (this.isValid())
                  setDescription("Skip " + String.valueOf(getStepsToSkip()) + " Steps Unconditionally");
               break;
            }
            default: setErrors("Invalid input " + Util.sq(input) + " instance cannot be initialized");
         }
      }
      else
         setErrors("Invalid input " + Util.sq(input) + " instance cannot be initialized");

   }

   private void setStepToSkip() {
      try {
         String tmp = getOrigin().substring(3);
         if (isBlank(tmp))
            setErrors("Number of steps to skip not specified.");
         else {
            // The following step will blow up if the remaining text in tmp is not numeric.
            setStepsToSkip(Integer.valueOf(tmp));
            if (getStepsToSkip() < 1) {
               setErrors("Number of steps to skip (" + tmp + ") must be greater than zero");
            }
         }
      }
      catch (Exception ex) {
         setErrors("Failed extracting number of steps to skip due to error: " + ex.getMessage().toString());
      }
   }

   private void setActionEvent(String value) {
      actionEvent = value;
   }

   public String getActionEvent() {
      if (isBlank(actionEvent)) {
         setActionEvent("");
      }
      return actionEvent;
   }

   private void setQuitUnit(String value) {
      quitUnit = value;
   }

   public String getQuitUnit() {
      if (isBlank(quitUnit)) {
         setQuitUnit("");
      }
      return quitUnit;
   }

   private void setErrors(String value) {
      errors = value;
   }

   public String getErrors() {
      if (isBlank(errors)) {
         setErrors("");
      }
      return errors;
   }

   private void setDescription(String value) {
      description = value;
   }

   public String getDescription() {
      if (isBlank(description)) {
         setDescription("");
      }
      return description;
   }

   private void setOrigin(String value) {
      origin = value;
   }

   public String getOrigin() {
      return origin;
   }

   private void setStepsToSkip(int value) {
      stepsToSkip = value;
   }

   public int getStepsToSkip() {
      return stepsToSkip;
   }

   public boolean isValid() {
      return (isBlank(errors) && (!isBlank(getActionEvent())));
   }

   public boolean isSkipper () {
      return getOrigin().toUpperCase().startsWith("S");
   }

   public boolean isQuitter () {
      return getOrigin().toUpperCase().startsWith("Q");
   }

   public boolean appliesToTestSession() {
      return getQuitUnit().equalsIgnoreCase("TEST_SESSION");
   }

   public boolean appliesToTestCase() {
      return getQuitUnit().equalsIgnoreCase("TEST_CASE");
   }

   public String toString() {
      String result = "";
      if (isValid()) {
         if (isQuitter())
            result = "Quit " + getQuitUnit() + " on " + getActionEvent();
         if (isSkipper())
            result += "Skip " + String.valueOf(getStepsToSkip()) + " steps on " + getActionEvent();
      }
      else
         result = "Invalid: " + getErrors();

      return result.replace("on ANY", "Un-condionally");
   }

   /**
    * For a given test result (PASS or FAIL) return a boolean indicating whether this unit of test should be terminated
    * If the unit to quit is TEST_SESSION then the actionEvent should equal status or ANY
    * @param status
    * @return
    */
   public boolean shouldQuitTestSession(String status) {
      // Skipper policy should not result in quitting session.
      if (!isQuitter() || !isValid())
         return false;

      if (!appliesToTestSession())
         return false;

      if (getActionEvent().toUpperCase() == status.toUpperCase())
         return true;

      return (getActionEvent().equalsIgnoreCase("ANY"));
   }

   /**
    * For a given test result (PASS or FAIL) return a boolean indicating whether this unit of test should be terminated
    * If the unit to quit is TEST_CASE then the actionEvent should equal status or ANY
    * @param status
    * @return
    */
   public boolean shouldQuitTestCase(String status) {
      // Skipper policy should not result in quitting session.
      if (!isQuitter() || !isValid())
         return false;

      if (!appliesToTestCase())
         return false;

      if (getActionEvent().toUpperCase() == status.toUpperCase())
         return true;

      return (getActionEvent().equalsIgnoreCase("ANY"));
   }

   public int stepsToSkip(String status) {
      int result = 0;

      if (!isSkipper())
         return result;

      if (getActionEvent().equalsIgnoreCase("ANY"))
         result = getStepsToSkip();
      else
      if (getActionEvent().equalsIgnoreCase(status))
         result = getStepsToSkip();

      return result;
   }

}

