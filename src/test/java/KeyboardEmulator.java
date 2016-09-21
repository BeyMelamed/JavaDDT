import javafx.scene.input.KeyCode;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
 * 12/21/15  |Bey            |Added functionKey function
 * ==========|===============|========================================================
 */
public class KeyboardEmulator extends Thread {
   private final static int Shift = KeyEvent.VK_SHIFT;
   private final static int Enter = KeyEvent.VK_ENTER;
   private final static int Tab = KeyEvent.VK_TAB;
   private final static int F5 = KeyEvent.VK_F5;
   private final static int CTRL = KeyEvent.VK_CONTROL;
   private final static int HOME = KeyEvent.VK_HOME;
   private final static int END = KeyEvent.VK_END;
   private final static int KeyDown = KeyEvent.VK_DOWN;
   private final static int KeyUp = KeyEvent.VK_UP;
   private static Robot kb;
   private static String text;
   private static String shiftChars = "!@#$%^&*()_+{}:\"<>?";
   private static String unShiftChars = "1234567890-=[];',./";
   private final static KeyCode[] functionKeys = {KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, KeyCode.F6, KeyCode.F7, KeyCode.F8, KeyCode.F9,
   KeyCode.F10, KeyCode.F11, KeyCode.F12, KeyCode.F13, KeyCode.F14, KeyCode.F15, KeyCode.F16, KeyCode.F17, KeyCode.F18, KeyCode.F19, KeyCode.F20, KeyCode.F21,
         KeyCode.F22, KeyCode.F23, KeyCode.F24};

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

   public static void f5() {
      try {
         Robot keyboard = getKeyboard();
         keyboard.keyPress(F5);
      }
      catch (AWTException e) {

      }
   }

   // Send a function key, optionally with a combination of contro, shift or alt key depressed.
   // key            - A string like "F5" - must start with f or F followed by number that is between 1 and 24
   // ctrlSjoftAlt   - a string containing "ctrl" and / or "shift" and / or "alt"
   // Both params are case insensitive
   public static void functionKey(String key, String ctrlShiftAlt) {
      String sTmp = key.toLowerCase();
      int iTmp = 0;
      KeyCode keyCode;
      boolean usedCtrl = false, usedShift = false, usedAlt = false;
      if (sTmp.startsWith("f")) {
         // Get the numeric value of the function key, will be used as index into the KeyCode array
         iTmp = Integer.valueOf(sTmp.substring(1, sTmp.length()));
         if (iTmp < 25 && iTmp > 0) {
            keyCode = functionKeys[iTmp-1];

            // Determine whether or not to mask with ctrl, shift, alt ...
            try {
               Robot keyboard = getKeyboard();
               keyboard.waitForIdle();
               if (ctrlShiftAlt.contains("ctrl")) {
                  keyboard.keyPress(KeyEvent.CTRL_DOWN_MASK);
                  usedCtrl = true;
               }

               if (ctrlShiftAlt.contains("shift")) {
                  keyboard.keyPress(KeyEvent.SHIFT_DOWN_MASK);
                  usedShift = true;
               }

               if (ctrlShiftAlt.contains("alt")) {
                  keyboard.keyPress(KeyEvent.ALT_DOWN_MASK);
                  usedAlt = true;
               }

               //keyboard.keyPress(KeyEvent.keyCode);

               // Determine what key to release

               if (usedCtrl)
                  keyboard.keyRelease(CTRL);
               if (usedShift)
                  keyboard.keyRelease(Shift);
               if (usedAlt)
                  keyboard.keyRelease(KeyEvent.VK_ALT);

            }
            catch (Exception e) {
               //ignore
            }
         }

      }
   }

   public static void copyToClipboard() {
      try {
         Robot keyboard = getKeyboard();
         // Highlight text
         keyboard.keyPress(HOME);
         keyboard.keyPress(Shift);
         keyboard.keyPress(END);
         keyboard.keyRelease(Shift);

         keyboard.keyPress(CTRL);
         keyboard.keyPress(KeyEvent.getExtendedKeyCodeForChar('c'));
         keyboard.keyRelease(CTRL);
      }
      catch (AWTException e) {}
      finally {}
   }

   public static void pasteFromClipboard() {
      try {
         Robot keyboard = getKeyboard();
         keyboard.keyPress(CTRL);
         keyboard.keyPress(KeyEvent.getExtendedKeyCodeForChar('v'));
         keyboard.keyRelease(CTRL);
      }
      catch (AWTException e) {}
      finally {}
   }

   public static String type(String s, boolean appendEnter) throws AWTException {
      if (isBlank(s))
         return s;

      String typed = "";
      try {
         Robot keyboard = getKeyboard();
         keyboard.waitForIdle();
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
         }
      }
      catch (AWTException e) {

      }
      finally {
         return typed;
      }
   }

   public static void printKeyCodes() {
      Field[] fields = java.awt.event.KeyEvent.class.getDeclaredFields();
      for (Field f : fields) {
         if (Modifier.isStatic(f.getModifiers())) {
            System.out.println(f.getName());
         }
      }
   }

}

