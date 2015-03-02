
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 12/31/13
 * Time: 12:20 AM
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
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 12/31/13    |Bey      |Initial Version
 * ============|=========|====================================
 */
public class TestEvent {
   private String message;
   private DDTTestRunner.TestEventType eType;
   private Date timeStamp = new Date();
   private String id="";

   public static String eventName(DDTTestRunner.TestEventType event) {
      String result = "";
      switch (event)
      {
         case INIT : result = "INIT"; break;
         case INFO : result = "INFO"; break;
         case PASS : result = "PASS"; break;
         case FAIL : result = "FAIL"; break;
         case SKIP : result = "SKIP"; break;
         default: result = "????";
      }

      return result;
   }
   public TestEvent() {

   }
   public TestEvent(DDTTestRunner.TestEventType type, String message) {
      setType(type);
      setMessage(message);
      setTimeStamp();
   }

   public boolean isNull() {
      return (eType == null || isBlank(message) || isBlank(id));
   }

   public void setId(String strId) {
      // Use time stamp and a random number to fake uniqueness of id
      this.id = strId;
   }

   public void setType(DDTTestRunner.TestEventType value) {
      this.eType = value;
   }

   public DDTTestRunner.TestEventType getType() {
      if (eType == null)
         setType(DDTTestRunner.TestEventType.INIT);
      return eType;
   }

   public void setMessage(String value) {
      this.message = value;
   }

   public String getMessage() {
      return message;
   }

   public void setTimeStamp() {
      timeStamp = new Date();
   }

   public Date getTimeStamp() {
      return timeStamp;
   }

   public String formattedTimeStamp() {
      return new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(getTimeStamp());
   }

   @Override
   public String toString() {
      String result = isNull() ? "Null Event" : eventName(eType) + ": " + id + " - " + formattedTimeStamp();
      if (!isBlank(message))
         result += " - " + message;
      return result;
   }
}
