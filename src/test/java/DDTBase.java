import org.openqa.selenium.WebElement;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created with IntelliJ IDEA.
 * User: Avraham (Bey) Melamed
 * Date: 10/28/14
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
 * The base DDT class with basic comments, errors and exception handling
 * History
 * When        |Who      |What
 * ============|=========|====================================
 * 10/28/14    |Bey      |Initial Version
 * ============|=========|====================================
 */
public class DDTBase {
   private String errors;
   private String comments;
   private Throwable exception;
   private WebElement element = null;

   public DDTBase() {

   }
   public void addError(String value) {
      if (!isBlank(value))
         errors = isBlank(getErrors()) ?  value : getErrors() + ", " + value;
   }

   public String getErrors() {
      if (null==errors)
         clearErrors();
      return errors;
   }

   public void addComment(String value) {
      if (!isBlank(value))
         comments = isBlank(getComments()) ?  value : getComments() + ", " + value;
   }

   public String getComments() {
      if (null == comments)
         clearComments();
      return comments;
   }

   /**
    * Clear for re-use
    */
   public void clear() {
      clearErrors();
      clearComments();
      clearElement();
      clearException();
   }

   public void setException(Throwable value) {
      exception = value;
   }

   public void setException(String value) {
      setException(new Exception(value));
   }

   public Throwable getException() {
      return exception;
   }

   public boolean hasException() {
      return (exception instanceof Throwable);
   }

   public boolean hasErrors( ){
      return !isBlank(errors);
   }

   public boolean hasComments( ){
      return !isBlank(comments);
   }

   public boolean isValid() {
      return !hasErrors() && (!hasException());
   }

   public void clearErrors() {
      errors = "";
   }

   public void clearElement() {
      element = null;
   }

   public void clearComments() {
      comments = "";
   }

   public void clearException() {
      exception = null;
   }

   public WebElement getElement(){
      return element;
   }

   public boolean hasElement() {
      if (getElement() instanceof WebElement)
         return true;
      return false;
   }

   public void setElement(WebElement value) {
      element = value;
   }

   public String basicReport() {
      String result = "";
      StringBuilder sb = new StringBuilder(result);

      if (!isBlank(comments))
         sb.append("Comments: " + comments);

      if (!isBlank(errors))
         sb.append("Errors: " + errors);

      if (exception instanceof Exception)
         sb.append("Exception Generated: " + exception.getMessage());

      if (element instanceof WebElement)
         sb.append("WebElement: " + element.toString());

      result = sb.toString();

      return result;
   }

}
