import java.awt.*;
import java.awt.event.KeyEvent;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by BeyMelamed on 2/3/14.
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
 * Emulates a keyboard in cases where the keyboard should be used outside of web page
 * Example: File Dialog
 * When      |Who            |What
 * ==========|===============|========================================================
 * 02/03/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class KeyboardEmulator extends Thread {
   private final static int Shift = KeyEvent.VK_SHIFT;
   private final static int Enter = KeyEvent.VK_ENTER;
   private final static int Tab = KeyEvent.VK_TAB;
   private static Robot kb;
   private static String text;
   private static String shiftChars = "!@#$%^&*()_+{}:\"<>?";
   private static String unShiftChars = "1234567890-=[];',./";

   public static void setKeyboard (Robot value) {
      kb = value;
   }

   public static Robot getKeyboard() throws AWTException {
      if (kb == null) {
         try {
            setKeyboard(new Robot());
         }
         catch (AWTException e) {}
      }
      return kb;
   }

   public static void setText(String value) {
      text = value;
   }

   public static String getText() {
      if (isBlank(text)) {
         setText("");
      }
      return text;
   }

   public static String type(String s, boolean appendEnter) throws AWTException {
      if (isBlank(s))
         return s;

      String typed = "";
      try {
         Robot keyboard = getKeyboard();
         int size = s.length();
         char c;
         for (int i = 0; i < size; i++) {
            c = s.charAt(i);
            int idx = shiftChars.indexOf(c);
            if (idx > -1) {
               char cc = unShiftChars.charAt(idx);
               keyboard.keyPress(Shift);
               keyboard.keyPress(KeyEvent.getExtendedKeyCodeForChar(cc));
               keyboard.keyRelease(KeyEvent.getExtendedKeyCodeForChar(cc));
               keyboard.keyRelease(Shift);
            }
            else {
               keyboard.keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
               keyboard.keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
            }
            typed += String.valueOf(c);
         }
         if (appendEnter) {
            keyboard.keyPress(Enter);
            keyboard.keyRelease(Enter);
         }
      }
      catch (AWTException e) {

      }
      finally {
         return typed;
      }
   }

}

