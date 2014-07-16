import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.*;


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
 *    Class assisting in validation of date and time for the prevailing locale
 *    The class can be used for two purposes, namely:
 *    1. Maintaining of a hashtable with properties representing components of date/time stamps values of which can be used for verification of strings containing one or more such components
 *    2. Verification of a single date/time stamp component.
 *    Where various styles (SHORT,MEDIUM,LONG,FULL apply to formatting, these styles are supported (example month can be '1', '01', 'Jan' or 'January'
 * <p/>
 * When      |Who            |What
 * ==========|===============|========================================================
 * 02/13/14  |Bey            |Initial Version
 * 05/08/14  |Bey            |Introduce Time Zone Adjustment
 * 06/23/14  |Bey            |Git Recommit
 * ==========|===============|========================================================
 */
public class DDTDate {
   private static final String errPrefix = "Date Parser - ";
   private static final String DefaultFormatError = errPrefix + "Invalid input or format specified";
   private static final String sep = ",";
   private static final String Units ="years,months,days,hours,minutes,seconds";
   private static final Locale DefaultLocale = Locale.getDefault();
   public static DDTDuration DurationInstance = new DDTDuration();

   // A DateFormat for the default Locale - Methods available for various date formatting components
   private static final DateFormat DefaultFormat = DateFormat.getDateInstance();
   // Units that can be added to / subtracted from a date - partial implementation
   private static final String[] DateUnits = {"years","months","days","hours","minutes", "seconds"};
   // The corresponding duration field types to use when adding / subtracting unit(s)
   private static final DurationFieldType[] DurationTypes = {DurationFieldType.years(), DurationFieldType.months(), DurationFieldType.days(), DurationFieldType.hours(), DurationFieldType.minutes(), DurationFieldType.seconds()};
   // Output type options
   private static final String OutputTypes = "date,time,year,month,day,doy,dow,hour,hour24,minute,second,ampm,zone,era";
   // Output Style - Optional - if exists, must be one of short, medium, long, full - default is short
   private static final String OutputStyles = "short,medium,long,full";

   private MutableDateTime referenceDate;
   private String output;
   private int units;
   private DurationFieldType durationUnit;
   private String unitType;
   private String outputType;
   private String outputStyle;
   private Exception ex;
   private Locale locale;
   private String dateFormat;
   private String timeStampFormat;
   private String comments;

   public DDTDate()
   {
   }

   public DDTDate(String input) {
      initialize(input);
   }

   // ======================== Getters / Setters ========================

   public void setReferenceDate(MutableDateTime value) {
      referenceDate = value;
   }

   public MutableDateTime getReferenceDate() {
      if (!(referenceDate instanceof MutableDateTime)) {
         setReferenceDate(new MutableDateTime());
      }
      return referenceDate;
   }

   public MutableDateTime getReferenceDateAdjustedForTimeZone() {
      DateTime result = getReferenceDate().toDateTime();
      int timeZoneAdjustmentInHours = DDTSettings.Settings().getTimeZoneAdjustmentInHours();
      return result.plusHours(timeZoneAdjustmentInHours).toMutableDateTime();
   }

   private void setOutput() {
      output = createOutput();
   }

   public String getOutput() {
      return output;
   }

   private void setOutputType (String value) {
      outputType = value;
   }

   public String getOutputType() {
      if (isBlank(outputType))
         setOutputType("date");
      return outputType;
   }

   private void setOutputStyle (String value) {
      outputStyle = value;
   }

   public String getOutputStyle() {
      if (isBlank(outputStyle))
         setOutputStyle("short");
      return outputStyle;
   }

   private void setUnitType (String value) {
      unitType = value;
   }

   private void setException (Exception e) {
      ex = e;
   }

   public Exception getException () {
      return ex;
   }

   private void setDefaultException () {
      setException(DefaultFormatError);
   }

   private void setException (String s) {
      ex = new Exception(errPrefix + s);
   }

   private void setLocale (Locale value) {
      locale = value;
   }

   public Locale getLocal() {
      if (locale == null) {
         setLocale(DefaultLocale);
      }
      return locale;
   }

   private void addComment (String comment) {
      String prefix = ", ";
      if (isBlank(comments)) {
         comments = "";
         prefix = "";
      }

      comments += prefix + comment;
   }

   public String getComments() {
      return comments;
   }

   private void setUnits (int value) {
      units = value;
   }

   public int getUnits() {
      return units;
   }

   private void setDurationType(DurationFieldType value) {
      durationUnit = value;
   }

   public DurationFieldType getDurationType() {
      return durationUnit;
   }

   private void setDateFormat (String value) {
      dateFormat = value;
   }

   public String getDateFormat() {
      if (isBlank(dateFormat)) {
         setDateFormat(defaultDateFormat());
      }
      return dateFormat;
   }

   private void setTimeStampFormat (String value) {
      timeStampFormat = value;
   }

   public String getTimeStampFormat() {
      if (isBlank(timeStampFormat)) {
         setTimeStampFormat(defaultTimeStampFormat());
      }
      return timeStampFormat;
   }

   private String defaultDateFormat() {
      return DDTSettings.Settings().dateFormat();
   }

   private String defaultTimeStampFormat() {
      return DDTSettings.Settings().timeStampFormat();
   }

   // ======================== End Getters / Setters ========================

   /**
    * Sets the date from a properly formatted string with the following logical structure
    * %DATE{+/-}{digits}{unitType},{outputType},{outputStyle}@
    * Examples:
    * %date% - (case insensitive) sets the date to the current date and does not set any input - equivalent to the default constructor
    * %date+3month,year,long%
    * %date-3hours,day,full%
    * @param input
    */
   public void initialize(String input) {
      try {
         parseInput(input);
         if (!(ex instanceof Exception))
            setOutput();
      }
      catch (Exception e) {
         setException("Problem encountered in parsing input");
      }
   }

   public boolean hasException() {
      return (ex instanceof Exception);
   }

   public void resetDateProperties(String input, Hashtable<String, Object> varsMap) throws Exception {
      // initial validations - must have some input and varsMap must be a Hashtable.
      if (isBlank(input)) {
         setDefaultException();
         return;
      }

      if (!(varsMap instanceof Hashtable)) {
         setException("Invalid variables map encountered - contact technician!");
         return;
      }

      try {
         if (input.startsWith("%") && input.endsWith("%")) {
            String tmp = input.replaceAll("%","");
            if (tmp.toLowerCase().startsWith("date")) {
               tmp = substring(tmp,4);
               if (isBlank(tmp)) {
                  // Input is %date% - other instance variables will be lazily initialized using defaults
                  // Just reset the (class side) referenceDate variable
                  setReferenceDate(null);
                  // Maintain the date properties in the varsMap using current date
                  maintainDateProperties(varsMap);
                  return;
               }

               // break the remaining input to its components
               // Add/Subtract Units
               // Unit to add / subtract - Optional - if it does not exist, use current date.
               //                                     if it does exist, there should be a + or minus followed by
               //                                     unit which should be one of years, months, days, hours, minutes (pluralization is optional)
               // Output Type - Optional - must be one of year, month, day, doy (day of year), dow (day of week), hour, minute, ampm,
               // Output Style - Optional - if exists, must be one of short, medium, long, full - default is short
               String[] components = tmp.split(sep);
               int size = components.length;
               if (size > 3 || (size < 1)) {
                  setException("Number of formatting components must be between 1 and 3 (" + String.valueOf(size) + " provided - " + Util.sq(input)+ ")" );
                  return;
               }

               // Order of components should not matter.  Parse one at a time and set what's available

               String component;
               for (int i = 0; i < size; i++) {
                  component = trim(components[i]);
                  if (isBlank(component))
                     continue;

                  if (component.startsWith("+") || component.startsWith("-")) {
                     // 'math component' is the part of the input indicating whether or not to increment / decrement and data / time stamp units and by how much
                     parseMathComponent(component);
                     if (hasException())
                        return;

                     if (getUnits() != 0) {
                        setReferenceDate(getReferenceDateAdjustedForTimeZone());
                        getReferenceDate().add(getDurationType(), getUnits() );
                     }

                     // Maintain the date properties in the varsMap
                     maintainDateProperties(varsMap);
                  } // Math component
               } // Iterate over components
            } // Input has proper date token (%date%)
            else
               setDefaultException();
         } // input starts and ends with valid delimiters (@)
         else
            setDefaultException();
      } // try
      catch (Exception e) {
         setException(e);
      }
   }

   /**
    * This function will populate the varsMap with new copies of values related to display format of date & time stamps.
    * Those values are based on the class (static) date variable that is currently in use.
    * Those values can later be used in verification of UI elements that are date-originated but may change in time (say, today's date, month, year - etc.)
    * The base date that is used is Locale dependent (currently, the local is assumed to be that of the workstation running the software.)
    * When relevant, values are provided in various styles (SHORT, MEDIUM, LONG, FULL) - not all date elements have all those versions available.
    * The purpose of this 'exercise' is to set up the facility for the user to verify any component of date / time stamps independently of one another or 'traditional' formatting.
    *
    * The variables maintained here are formatted with a prefix to distinguish them from other, user defined variables.
    *
    * @TODO: enable Locale modifications (at present the test machine's locale is considered and within a test session only one locale can be tested)
    * @TODO: complete the entire set of data and time components
    * @param varsMap
    */
   private void maintainDateProperties(Hashtable<String, Object> varsMap) throws Exception {

      String prefix = "$";

      try {
         // Build formatting objects for each of the four styles
         DateFormat shortStyleFormatter  = DateFormat.getDateInstance(DateFormat.SHORT);
         DateFormat mediumStyleFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
         DateFormat longStyleFormatter   = DateFormat.getDateInstance(DateFormat.LONG);
         DateFormat fullStyleFormatter   = DateFormat.getDateInstance(DateFormat.FULL);

         // Use a dedicated variable to hold values of formatting results to facilitate debugging
         // @TODO (maybe) when done debugging - convert to inline calls to maintainDateProperty ...
         String formatValue;

         // Examples reflect time around midnight of February 6 2014 - actual values DO NOT include quotes (added here for readability)

         MutableDateTime theReferenceDate = getReferenceDateAdjustedForTimeZone();
         // Default Date using DDTSettings pattern.
         formatValue = new SimpleDateFormat(defaultDateFormat()).format(theReferenceDate.toDate());
         maintainDateProperty(prefix + "defaultDate", formatValue, varsMap);

         // Short Date - '2/6/14'
         formatValue = shortStyleFormatter.format(theReferenceDate.toDate());
         maintainDateProperty(prefix + "shortDate", formatValue, varsMap);

         // Medium Date - 'Feb 6, 2014'
         formatValue = mediumStyleFormatter.format(theReferenceDate.toDate());
         maintainDateProperty(prefix + "mediumDate", formatValue, varsMap);

         // Long Date - 'February 6, 2014'
         formatValue = longStyleFormatter.format(theReferenceDate.toDate());
         maintainDateProperty(prefix + "longDate", formatValue, varsMap);

         // Full Date 'Thursday, February 6, 2014'
         formatValue = fullStyleFormatter.format(theReferenceDate.toDate());
         maintainDateProperty(prefix + "fullDate", formatValue, varsMap);

         // hours : minutes : seconds : milliseconds (broken to separate components)  -
         formatValue = theReferenceDate.toString("hh:mm:ss:SSS");
         if (formatValue.toString().contains(":")) {
            String[] hms = split(formatValue.toString(), ":");
            if (hms.length > 3) {
               // Hours - '12'
               formatValue = hms[0];
               maintainDateProperty(prefix + "hours", formatValue, varsMap);
               // Minutes - '02'
               formatValue = hms[1];
               maintainDateProperty(prefix + "minutes", formatValue, varsMap);
               // Seconds - '08'
               formatValue = hms[2];
               maintainDateProperty(prefix + "seconds", formatValue, varsMap);
               // Milliseconds - '324'
               formatValue = hms[3];
               maintainDateProperty(prefix + "milliseconds", formatValue, varsMap);
               // Hours in 24 hours format - '23'
               formatValue = theReferenceDate.toString("HH");
               maintainDateProperty(prefix + "hours24", formatValue, varsMap);
            }
            else
               setException("Failed to format reference date to four time units!");
         }
         else {
            setException("Failed to format reference date to its time units!");
         }

         // hours : minutes : seconds (default timestamp)  - '12:34:56'
         formatValue = theReferenceDate.toString("hh:mm:ss");
         maintainDateProperty(prefix + "timeStamp", formatValue, varsMap);

         // Short Year - '14'
         formatValue = theReferenceDate.toString("yy");
         maintainDateProperty(prefix + "shortYear", formatValue, varsMap);

         // Long Year - '2014'
         formatValue = theReferenceDate.toString("yyyy");
         maintainDateProperty(prefix + "longYear", formatValue, varsMap);

         // Short Month - '2'
         formatValue = theReferenceDate.toString("M");
         maintainDateProperty(prefix + "shortMonth", formatValue, varsMap);

         // Padded Month - '02'
         formatValue = theReferenceDate.toString("MM");
         maintainDateProperty(prefix + "paddedMonth", formatValue, varsMap);

         // Short Month Name - 'Feb'
         formatValue = theReferenceDate.toString("MMM");
         maintainDateProperty(prefix + "shortMonthName", formatValue, varsMap);

         // Long Month Name - 'February'
         formatValue = theReferenceDate.toString("MMMM");
         maintainDateProperty(prefix + "longMonthName", formatValue, varsMap);

         // Week in Year - '2014' (the year in which this week falls)
         formatValue = String.valueOf(theReferenceDate.getWeekyear());
         maintainDateProperty(prefix + "weekYear", formatValue, varsMap);

         // Short Day in date stamp - '6'
         formatValue = theReferenceDate.toString("d");
         maintainDateProperty(prefix + "shortDay", formatValue, varsMap);

         // Padded Day in date stamp - possibly with leading 0 - '06'
         formatValue = theReferenceDate.toString("dd");
         maintainDateProperty(prefix + "paddedDay", formatValue, varsMap);

         // Day of Year - '37'
         formatValue = theReferenceDate.toString("D");
         maintainDateProperty(prefix + "yearDay", formatValue, varsMap);

         // Short Day Name - 'Thu'
         formatValue = theReferenceDate.toString("E");
         maintainDateProperty(prefix + "shortDayName", formatValue, varsMap);

         // Long Day Name - 'Thursday'
         DateTime dt = new DateTime(theReferenceDate.toDate());
         DateTime.Property dowDTP = dt.dayOfWeek();
         formatValue = dowDTP.getAsText();
         maintainDateProperty(prefix + "longDayName", formatValue, varsMap);

         // AM/PM - 'AM'
         formatValue = theReferenceDate.toString("a");
         maintainDateProperty(prefix + "ampm", formatValue, varsMap);

         // Era - (BC/AD)
         formatValue = theReferenceDate.toString("G");
         maintainDateProperty(prefix + "era", formatValue, varsMap);

         // Time Zone - 'EST'
         formatValue = theReferenceDate.toString("zzz");
         maintainDateProperty(prefix + "zone", formatValue, varsMap);

         addComment("Date variables replenished for date: " + mediumStyleFormatter.format(theReferenceDate.toDate()) );
      }
      catch (Exception e) {
         setException(e);
      }
   }

   /**
    * Replenish a single property in the variables map varsMap
    * @param key
    * @param value
    * @param varsMap
    */
   private void maintainDateProperty (String key, Object value, Hashtable<String, Object> varsMap) {
      String tmp = key.toLowerCase(); // varsMap is maintained in lower case by convention
      Object oldValue = varsMap.get(tmp);
      if (oldValue != null) {
         // Change value in varsMap only if needed.
         if (oldValue.equals(value))
            return;
         varsMap.remove(tmp);
      }

      varsMap.put(tmp, String.valueOf(value));

      // Reporting ... @TODO comment out when done testing (thanks) (you are welcome)
/*
      if (oldValue == null)
         System.out.println(" key " + Util.sq(key) + " set to " + Util.sq(value.toString()) + " in the variables map.");
      else
         System.out.println("Old version of key " + Util.sq(key) + " => " + Util.sq(oldValue.toString()) + " reset to " + Util.sq(value.toString()) + " in the variables map.");
*/

   }

   /**
    * Verifies the input is valid
    * Do not return anything, just set the instance's exception appropriately
    * @param input
    */
   private void parseInput(String input) {
      if (isBlank(input)) {
         setDefaultException();
         return;
      }

      if (input.startsWith("%") && input.endsWith("%")) {
         String tmp = input.replaceAll("%","");
         if (tmp.toLowerCase().startsWith("date")) {
            tmp = substring(tmp,4).replaceAll(" ", "");
            if (isBlank(tmp)) {
               // Input is %date% - other instance variables will be lazily initialized using defaults
               // Just reset the (class side) referenceDate variable
               setReferenceDate(null);
               return;
            }

            // break the remaining input to its components
            // Add/Subtract Units
            // Unit to add / subtract - Optional - if it does not exist, use current date.
            //                                     if it does exist, there should be a + or minus followed by
            //                                     unit which should be one of years, months, days, hours, minutes (pluralization is optional)
            // Output Type - Optional - must be one of year, month, day, doy (day of year), dow (day of week), hour, minute, ampm,
            // Output Style - Optional - if exists, must be one of short, medium, long, full - default is short
            String[] components = tmp.split(sep);
            int size = components.length;
            if (size > 3 || (size < 1)) {
               setException("Number of formatting components must be between 1 and 3 (" + String.valueOf(size) + " provided - " + Util.sq(input)+ ")" );
               return;
            }

            // Order of components should not matter.  Parse one at a time and set what's available

            String component;
            for (int i = 0; i < size; i++) {
               component = trim(components[i]);
               if (isBlank(component))
                  continue;

               if (component.startsWith("+") || component.startsWith("-")) {
                  parseMathComponent(component);
                  if (hasException())
                     return;

                  // Skip to the next component
                  continue;
               } // Math component
               else if (OutputTypes.contains(component.toLowerCase())) {
                  setOutputType(component.toLowerCase());
                  continue;
               }
               else if (OutputStyles.contains(component.toLowerCase())) {
                  setOutputStyle(component.toLowerCase());
                  continue;
               }
               else {
                  setException("Invalid component (number " + String.valueOf(i) + ") encountered in input (" + Util.sq(component) + ")");
               }
            }
         }
         else
            setDefaultException();
      } // input starts and ends with valid delimiters (@)
      else
         setDefaultException();
   }

   private void parseMathComponent(String component) {
      boolean foundUnit = false;
      boolean shouldAdd = component.startsWith("+");
      // strip the sign to add or subtract
      String tmp = component.substring(1).toLowerCase();
      for (int j = 0; j < DateUnits.length; j++) {
         if (tmp.contains(DateUnits[j].toLowerCase())) {
            foundUnit = true;
            setDurationType(DurationTypes[j]);
            // eliminate the duration unit from the component
            tmp = tmp.replaceAll(DateUnits[j].toLowerCase(),"").trim();
            // The component now should be digits only
            if (tmp.isEmpty()) {
               setException("Invalid Input - Number of units not specified");
               return;
            }

            try {
               setUnits (shouldAdd ? Integer.valueOf(tmp) : (0 - Integer.valueOf(tmp)));
            }
            catch (Exception e) {
               setException(e);
               return;
            }
         } // if (found unit type)
      } // iterate over DateUnits in component

      if (!foundUnit) {
         setException("Invalid component Duration Unit Type encountered in input (use one of: " + Util.asString(DateUnits, ",") + ")");
         return;
      }
   }

   /**
    * Creates the output the user indicated in the input (outputType component) subject to the requested style (outputStyle) component
    * @return
    */
   private String createOutput() {
      String result = "";
      try {
         // If needed, adjust the reference date by the number and type of units specified  - as per the time zone
         if (getUnits() != 0) {
            setReferenceDate(getReferenceDateAdjustedForTimeZone());
            getReferenceDate().add(getDurationType(), getUnits() );
         }

         // Create date formatters to be used for all varieties - the corresponding date variables are always set for convenience purposes
         DateFormat shortFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
         DateFormat mediumFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
         DateFormat longFormatter = DateFormat.getDateInstance(DateFormat.LONG);
         DateFormat fullFormatter = DateFormat.getDateInstance(DateFormat.FULL);

         // Build the specific formatter specified
         DateFormat formatter = null;
         switch (getOutputStyle().toLowerCase()) {
            case "medium" : {
               formatter = mediumFormatter;
               break;
            }
            case "long" : {
               formatter = longFormatter;
               break;
            }
            case "full" : {
               formatter = fullFormatter;
               break;
            }
            default:
               formatter = shortFormatter;
         } // output style switch

         // construct the specified result - one at a time
         MutableDateTime theReferenceDate = getReferenceDateAdjustedForTimeZone();
         switch (getOutputType().toLowerCase())  {
            case "date" : {
               result = formatter.format(theReferenceDate.toDate());
               break;
            }

            case "time" :{
               switch (getOutputStyle().toLowerCase())    {
                  case "short": {
                     result = theReferenceDate.toString("hh:mm:ss");
                     break;
                  }
                  default:
                     result = theReferenceDate.toString("hh:mm:ss.SSS");
               }
               break;
            }
            // separate time components
            case "hour" : case "minute" : case "second" :case "hour24" : {
               String tmp = theReferenceDate.toString("hh:mm:ss");
               if (tmp.toString().contains(":")) {
                  String[] hms = split(tmp.toString(), ":");
                  if (hms.length > 2) {
                     switch (getOutputType().toLowerCase()) {
                        case "hour" : {
                           // Hour - '12'
                           result = hms[0];
                           break;
                        }
                        case "minute" : {
                           // Minutes - '34'
                           result = hms[1];
                           break;
                        }
                        case "second" : {
                           // Second - '56'
                           result = hms[2];
                           break;
                        }
                        case "hour24" : {
                           // Hour - '23'
                           result = theReferenceDate.toString("HH");
                           break;
                        }
                        default: result = hms[0];
                     } // switch for individual time component
                  } // three parts of time components
               } // timestamp contains separator ":"
               break;
            } // Hours, Minutes, Seconds

            case "year" : {
               switch (getOutputStyle().toLowerCase())    {
                  case "short": {
                     result = theReferenceDate.toString("yy");
                     break;
                  }
                  default:
                     result = theReferenceDate.toString("yyyy");
               }
               break;
            }

            case "month" : {
               switch (getOutputStyle().toLowerCase())    {
                  case "short": {
                     result = theReferenceDate.toString("M");
                     break;
                  }
                  case "medium": {
                     // padded with 0
                     result = theReferenceDate.toString("MM");
                     break;
                  }
                  case "long": {
                     // short name 'Feb'
                     result = theReferenceDate.toString("MMM");
                     break;
                  }
                  default:
                     // Full name 'September'
                     result = theReferenceDate.toString("MMMM");
               }
               break;
            }

            case "day" : {
               switch (getOutputStyle().toLowerCase())    {
                  case "short" : {
                     result = theReferenceDate.toString("d");
                     break;
                  }
                  case "medium" : {
                     result = theReferenceDate.toString("dd");
                     break;
                  }
                  default: result =theReferenceDate.toString("dd");
               }
            }

            case "doy" : {
               result = theReferenceDate.toString("D");
               break;
            }

            case "dow" : {
               switch (getOutputStyle().toLowerCase())    {
                  case "short": {
                     result = theReferenceDate.toString("E");
                     break;
                  }
                  case "medium": {
                     DateTime dt = new DateTime(theReferenceDate.toDate());
                     DateTime.Property dowDTP = dt.dayOfWeek();
                     result = dowDTP.getAsText();
                     break;
                  }
                  default:
                     result = theReferenceDate.toString("E");
               }
               break;
            } // day of week

            case "zone" : {
               result = theReferenceDate.toString("zzz");
               break;
            }

            case "era" : {
               result = theReferenceDate.toString("G");
               break;
            }

            case "ampm" : {
               result = theReferenceDate.toString("a");
               break;
            }

            default: {
               setException("Invalid Output Unit - cannot set output");
            }

            // Create full date variables for the short, medium, long, full styles

         } // output type switch
      } // try constructing result
      catch (Exception e) {
         setException(e);
      }
      finally {
         return result;
      }
   }

   /**
    * Created by BeyMelamed on 02/13/14.
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
    *    The DDT project's duration functionality derived from start time and end time
    *    Instances of this class are embedded in the test item and reporting objects.
    * <p/>
    * When      |Who            |What
    * ==========|===============|========================================================
    * 02/13/14  |Bey            |Initial Version
    * ==========|===============|========================================================
    */
   public static class DDTDuration {

      private Date startTime;
      private Date endTime;

      public DDTDuration() {
         setStartTime();
         setEndTime();
      }

      public void setStartTime(Date value) {
         startTime = value;
      }

      public void setStartTime() {
         startTime = new Date();
      }

      public Date getStartTime() {
         if (startTime == null)
            setStartTime();
         return startTime;
      }

      public void setEndTime(Date value) {
         endTime = value;
      }

      public void setEndTime() {
         endTime = new Date();
      }

      public Date getEndTime() {
         if (endTime == null)
            setEndTime();
         return endTime;
      }

      private Period asPeriod() {
         return new Period(getEndTime().getTime() - getStartTime().getTime());
      }

      @Override
      public String toString() {

         this.setEndTime();
         Period p = this.asPeriod();
         String result = String.format("%02d:%02d:%02d.%03d", p.getHours(), p.getMinutes(), p.getSeconds(), p.getMillis());
         if (p.getDays() > 0)
            result = String.format("%03d days, %02d:%02d:%02d.%03d", p.getDays(),p.getHours(), p.getMinutes(), p.getSeconds(), p.getMillis());
         return result;
      }
   }
}

