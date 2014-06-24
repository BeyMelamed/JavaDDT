import java.util.ArrayList;

/* Created by BeyMelamed on 06/19/2014.
      * Selenium Based Automation Project
      * Created by BeyMelamed on 06/17/2014.
      * Selenium Based Automation Project
      * Description
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
      * This class is used to provide ('inline') a String[][] array from which TestItem instances are created by the testItemsProvider class
      * This and other classes extending InlineTestItemsProvider can be developed independently of the main line of code and loaded dynamically
      *
      * When        |Who            |What
      * ============|===============|========================================================
      * 06/19/2014  |Bey            |Initial Version
      * ============|===============|========================================================
      */
public class ExternalTestItemsGenerator extends TestItemsProvider.InlineTestItemsProvider {
   public ExternalTestItemsGenerator() {

   }

   // ================================================================================================================
   //                               Test Items Generating Methods
   // ================================================================================================================

   public ArrayList<String[]> root() {

      //addItem("id", "action", "LocType", "locSpecs", "qryFunction", "active", "data", "description");
      addItem("CrownPOC01", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:calculator", "Run the Calculator tests as Crown");
      addItem("CrownPOC02", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:chainingFinders", "Run the ChainFinders tests as Crown");
      addItem("CrownPOC03", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:cssFinders", "Run the CssFinders tests as Crown");
      addItem("CrownPOC04", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:frameSwitching", "Run the Frame Switching tests as Crown");
      addItem("CrownPOC05", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:nonBrowserPassingTests", "Run constant verification tests without web driver that should pass as Crown");
      addItem("CrownPOC06", "newTest", "", "", "", "", "InputSpecs=Inline:SampleTestItemsGenerator:nonBrowserFailingTests", "Run constant verification tests without web driver that should fail as Crown");
      addItem("CrownPOC07", "generateReport", "", "", "", "", "Description=Report For Inline Test Provider", "Generate the report for the demo inline items provider");

      return getDataList();
   }
}

