import com.opera.core.systems.OperaDriver;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Created by BeyMelamed on 2/13/14 - Modeled after Alan Richardson's original code and adopted for JavaDDT.
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
 * A singleton class holding the WebDriver object that is used to perform all web driver activity
 * Plagiarized straight out of Allan Richardson online Web Driver II course - highly recommended!
 * <p/>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 02/13/14  |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class Driver extends Thread{
   private static WebDriver aDriver=null;
   private static long browserStartTime = 0L;
   private static long savedTimecount = 0L;
   public static final long DEFAULT_TIMEOUT_SECONDS = 10;
   private static boolean avoidRecursiveCall=false;
   public static final String BROWSER_PROPERTY_NAME = "Driver";
   public enum BrowserName{FIREFOX, GOOGLECHROME, SAUCELABS, OPERA, IE, HTMLUNIT, HEADLESS}

   public static BrowserName currentDriver;

   private static BrowserName useThisDriver = null;

   // default for browsermob localhost:8080
   // default for fiddler: localhost:8888
   public static String PROXY="localhost:8080";

   public static WebDriver getDriver() {
      return aDriver;
   }

   public static boolean isInitialized() {
      return (aDriver instanceof WebDriver);
   }

   public static void set(BrowserName aBrowser){
      useThisDriver = aBrowser;

      // close any existing driver
      if(aDriver != null){
         aDriver.quit();
         aDriver = null;
      }
   }

   public static BrowserName asBrowserName(String browserName) {
      BrowserName result = null;
      switch (browserName.toUpperCase()){
         case "FIREFOX":
            result = BrowserName.FIREFOX;
            break;
         case "CHROME":
            result = BrowserName.GOOGLECHROME;
            break;
         case "IE":
            result = BrowserName.IE;
            break;
         case "OPERA":
            result = BrowserName.OPERA;
            break;
         case "SAUCELABS":
            result = BrowserName.SAUCELABS;
            break;
         case "HTMLUNIT":
            result = BrowserName.HTMLUNIT;
            break;
         case "HEADLESS":
            result = BrowserName.HEADLESS;
            break;
         default:
            throw new RuntimeException("Unknown Browser in " + BROWSER_PROPERTY_NAME + ": " + browserName);

      }
      return result;
   }

   public static WebDriver get() {

      if(useThisDriver == null){

         String defaultBrowser = System.getProperty(BROWSER_PROPERTY_NAME, "FIREFOX");
         useThisDriver = asBrowserName(defaultBrowser);
      }


      if(aDriver==null){

         long startBrowserTime = System.currentTimeMillis();
         DesiredCapabilities capabilities;
         Hashtable<String, Object> desiredCapabilities;
         Set<Map.Entry<String, Object>> entries;

         switch (useThisDriver) {
            case FIREFOX:
               FirefoxProfile profile = new FirefoxProfile();
               profile.setEnableNativeEvents(true);

               aDriver = new FirefoxDriver();//profile);
               currentDriver = BrowserName.FIREFOX;
               break;

            case OPERA:

               aDriver = new OperaDriver();
               currentDriver = BrowserName.OPERA;
               break;

            case HTMLUNIT:

               capabilities = new DesiredCapabilities();
               desiredCapabilities = DDTSettings.Settings().getDesiredCapabilities();
               entries = desiredCapabilities.entrySet();
               for (Map.Entry<String, Object> entry : entries) {
                  capabilities.setCapability(entry.getKey(), entry.getValue());
               }
               aDriver = new HtmlUnitDriver(capabilities);
               currentDriver = BrowserName.HTMLUNIT;
               break;

            case HEADLESS:

               capabilities = new DesiredCapabilities();
               desiredCapabilities = DDTSettings.Settings().getDesiredCapabilities();
               entries = desiredCapabilities.entrySet();
               for (Map.Entry<String, Object> entry : entries) {
                 capabilities.setCapability(entry.getKey(), entry.getValue());
               }
               capabilities.setCapability(
                     PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                     DDTSettings.Settings().resourcesFolder() + "phantomjs.exe");

               // Launch driver (will take care and ownership of the phantomjs process)
               aDriver = new PhantomJSDriver(capabilities);
               currentDriver = BrowserName.HEADLESS;
               break;

            case IE:

               setDriverPropertyIfNecessary(BrowserName.IE);

               aDriver = new InternetExplorerDriver();
               currentDriver = BrowserName.IE;
               break;

            case GOOGLECHROME:

               setDriverPropertyIfNecessary(BrowserName.GOOGLECHROME);

               ChromeOptions options = new ChromeOptions();
               options.addArguments("disable-plugins");
               options.addArguments("disable-extensions");

               aDriver = new ChromeDriver(options);
               currentDriver = BrowserName.GOOGLECHROME;
               break;

            case SAUCELABS:

               capabilities = DesiredCapabilities.firefox();
               capabilities.setCapability("version", "5");
               capabilities.setCapability("platform", Platform.XP);
               try {
                  // add url to environment variables to avoid releasing with source
                  String sauceURL = System.getenv("SAUCELABS_URL");
                  aDriver = new RemoteWebDriver(
                        new URL(sauceURL),
                        capabilities);
               } catch (MalformedURLException e) {
                  e.printStackTrace();
               }
               currentDriver = BrowserName.SAUCELABS;
               break;
         }


         long browserStartedTime = System.currentTimeMillis();
         browserStartTime = browserStartedTime - startBrowserTime;

         // we want to shutdown the shared brower when the tests finish
         Runtime.getRuntime().addShutdownHook(
               new Thread(){
                  public void run(){
                     Driver.quit();
                  }
               }
         );

      }else{

         try{
            // is browser still alive
            if(aDriver.getWindowHandle()!=null){
               // assume it is still alive
            }
         }catch(Exception e){
            if(avoidRecursiveCall){
               // something has gone wrong as we have been here already
               throw new RuntimeException();
            }

            quit();
            aDriver=null;
            avoidRecursiveCall = true;
            return get();
         }

         savedTimecount += browserStartTime;
         System.out.println("Saved another " + browserStartTime + "ms : total saved " + savedTimecount + "ms");
      }

      avoidRecursiveCall = false;
      return aDriver;
   }

   private static void setDriverPropertyIfNecessary(BrowserName browserName) {
      // http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
      /**
       * @TODO: Handle the issue below on the fly... (Can it be done?)
       * Problems with IE may include the error 'Protected Mode settings are not the same for all zones'
       * From Google Search (http://stackoverflow.com/questions/14952348/not-able-to-launch-ie-browser-using-selenium2-webdriver-with-java)
       * 1. Open IE
       * 2. Go to Tools -> Internet Options -> Security
       * 3. Set all zones to the same protected mode, enabled or disabled should not matter.
       * 4. Finally, set Zoom level to 100% by right clicking on the gear located at the top right corner and enabling the status-bar.
       *    Default zoom level is now displayed at the lower right.
       */

      String propertyKey;
      switch (browserName) {
         case  IE : {propertyKey = DDTSettings.Settings().iePropertyKey(); break;}
         case GOOGLECHROME: {propertyKey = DDTSettings.Settings().chromePropertyKey(); break;}
         default: {propertyKey = DDTSettings.Settings().chromePropertyKey();}
      }

      if(!System.getProperties().containsKey(propertyKey)){
         String fileLocation;

         switch (browserName) {
            case  IE : {fileLocation = DDTSettings.Settings().ieDriverFileName(); break;}
            case GOOGLECHROME: {fileLocation = DDTSettings.Settings().chromeDriverFileName(); break;}
            default: {fileLocation = DDTSettings.Settings().chromeDriverFileName();}
         }

         File driverExe = new File(DDTSettings.asValidOSPath(fileLocation, true));
         if(driverExe.exists()){
            System.setProperty(propertyKey, fileLocation);
         }else{
            fileLocation = "C:/Program Files/Google/Chrome/Application/Chrome.exe";
            driverExe = new File(DDTSettings.asValidOSPath(fileLocation, true));
            if(driverExe.exists()){
               System.setProperty(propertyKey, fileLocation);
            }else{
               // expect an error on the follow through when we try to use the driver
            }
         }
      }
   }

   public static WebDriver get(String aURL, boolean maximize){
      get();
      aDriver.get(aURL);

      if(maximize){
         try{
            aDriver.manage().window().maximize();
         }catch(UnsupportedCommandException e){
            System.out.println("Remote Driver does not support maximise");
         }catch(UnsupportedOperationException e){
            System.out.println("Opera driver does not support maximize yet");
         }
      }
      return aDriver;
   }

   public static WebDriver get(String aURL){
      return get(aURL,false);
   }

   public static void quit(){
      if(aDriver!=null){
         System.out.println("total time saved by reusing browsers " + savedTimecount + "ms");
         try{
            aDriver.quit();
            aDriver=null;
         }catch(Exception e){
            // I don't care about errors at this point
         }

      }
   }
}
