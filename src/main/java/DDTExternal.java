/**
 * Created by BeyMelamed on 6/14/2015.
 * Selenium Based Automation Project
 *
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
 *
 * Description
 * This class is a 'sand box' for implementation of methods not provided by the original project.
 * Methods that are called as a step (or part thereof) by the DDTTestRunner via the part of the are assumed to either return a boolean indicating pass or fail or throw in instance of throwable.
 * If the method is to accept a
 * <p/>
 * =============================================================================
 *
 * When      |Who            |What
 * ==========|===============|========================================================
 * 6/14/2015 |Bey            |Initial Version
 * ==========|===============|========================================================

 */

public class DDTExternal {
   public DDTExternal() {}
   public DDTExternal(DDTTestContext context) {}

   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding a comment, without an error means something was done successfull!
    * This is a static method but an instance method will do as well
    * @param context
    */
   public static void trivialPass(DDTTestContext context) {
      context.addComment("Hey, I passed!");
      return;
   }

   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding an error (possible, an exception) means something failed!
    * This is a static method but an instance method will do as well
    * @param context
    */
   public static void trivialFail(DDTTestContext context) {
      context.addError("Ooops, I failed!");
   }

   /**
    * Context is a Hashtable of <string, Object> associations with methods to get and set properties.
    * Adding an error (possible, an exception) means something failed!
    * This is an instance method but a static method will do as well
    * @param context
    */
   public void instanceFailure(DDTTestContext context) {
      context.addError("Ooops, Instance failure failed!");
   }
}
