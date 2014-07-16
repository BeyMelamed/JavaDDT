import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by BeyMelamed on 3/23/14.
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
 * <p/>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 03/23/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class TestDDTTest extends TestCase {
   @Test
   public void testDDT() {
      DDTestRunner.invokeDefaults();
      if (DDTestRunner.getReporter().shouldGenerateReport()) {
         try {
            DDTestRunner.getReporter().generateReport("Final Report", "");
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
      DDTestRunner.endSession();
   }
}
