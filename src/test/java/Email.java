
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

/**
 * Created by BeyMelamed on 1/3/14.
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
 * This class is used to email the results of a test session with an attachment file that is the html rendition of the test results
 * The code is plagiarized from various Google queries regarding java email using the javax classes.
 * Please note the dependency of the code on the various email settings for situations where the email host requires authentication vs when it is self authenticating
 * When      |Who            |What
 * ==========|===============|========================================================
 * 1/3/14    |Bey            |Initial Version
 * ==========|===============|========================================================
 */
public class Email {

   public static void sendMail(String subject, String messageBody, String fileName, List<String> failedTests) throws MessagingException {

      DDTSettings settings = DDTSettings.Settings();
      final String sender = settings.emailSender();
      final String password = settings.emailPassword();
      String[] recipients = settings.emailRecipients().split(",");
      String host = settings.emailHost();
      String port = settings.emailPort();
      boolean emailAuthenticationRequired = settings.emailAuthenticationRequired();
      String trueOrFalse = emailAuthenticationRequired ? "true" : "false";
      Session session;

      // Use system properties and add some related to email protocol
      Properties props = System.getProperties();

      // Setup mail server
      props.setProperty("mail.smtp.host", host);
      props.setProperty("mail.smtp.port", port);
      props.setProperty("mail.smtp.ssl.enable", "false");
      props.setProperty("java.net.preferIPv4Stack","true");
      props.setProperty("mail.smtp.starttls.enable", trueOrFalse);
      props.setProperty("mail.smtp.auth", trueOrFalse);

      // Create a session with or without authentication object as indicated in the settings.
      if (emailAuthenticationRequired) {
         Authenticator auth = new Authenticator() {
            protected PasswordAuthentication  getPasswordAuthentication() {
               return new PasswordAuthentication(sender, password);
            }
         };
         session = Session.getDefaultInstance(props, auth);
      }
      else {
         props.setProperty("mail.smtp.user", sender); // Needed if not authenticating
         props.setProperty("mail.smtp.password", password); // Needed if not authenticatin
         session = Session.getDefaultInstance(props);
      }

      // Setup the message in two parts, body and attachment then send it
      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(sender));
         //message.setReplyTo({new InternetAddress(sender)});

         // Set To: header field of the header.
         for (int i = 0; i<recipients.length; i++) {
            message.addRecipient(Message.RecipientType.TO,
                  new InternetAddress(recipients[i]));
         }

         // Set Subject: header field
         message.setSubject(subject);

         // Create the message part
         BodyPart messageBodyPart = new MimeBodyPart();

         // construct a section of test failures if any
         String failedTestSection = "";
         if (!failedTests.isEmpty())   {
            int i = 0;
            StringBuilder sb = new StringBuilder("");
            sb.append("<br><br>" + failedTests.size() + " test steps failed - see failures summary below:<br>");
            for (String failure:failedTests) {
               sb.append("<p>"+failedTests.get(i++) + "<br></p>");
            }
            failedTestSection = sb.toString();
            sb = null;
         }
         // Fill the body of the message
         messageBodyPart.setContent(messageBody + failedTestSection,"text/html");

         // Create a multipart message
         Multipart multipart = new MimeMultipart();

         // Set text message part
         multipart.addBodyPart(messageBodyPart);

         // Part two is attachment
         messageBodyPart = new MimeBodyPart();
         DataSource source = new FileDataSource(fileName);
         messageBodyPart.setDataHandler(new DataHandler(source));
         messageBodyPart.setFileName(source.getName());  // When using setFileName(fileName) - the name is the entire path with non-alpha characters represented as under scores
         multipart.addBodyPart(messageBodyPart);

         // If any other attachments are available (they should be specified with full path in the ddt.properties file in a folder accessible from here...)
         // This is a comma delimited list of file names...  where (optionally) the string "%res%" stands for the project's Resources folder  and %data% for the data folder

         String attachments = settings.attachments();
         if (!attachments.isEmpty())  {
            String[] fileNames = attachments.split(",");
            for (int i=0; i < fileNames.length; i++) {
               messageBodyPart = new MimeBodyPart();
               source = new FileDataSource(fileNames[i].replace("%res%", settings.resourcesFolder()).replace("%data%", settings.dataFolder()));
               try {
                  source.getInputStream().read(new byte[1]);
                  source.getInputStream().close();
                  messageBodyPart.setDataHandler(new DataHandler(source));
                  messageBodyPart.setFileName(source.getName());  // When using setFileName(fileName) - the name is the entire path with non-alpha characters represented as under scores
                  multipart.addBodyPart(messageBodyPart);
               }
               catch (Exception e) {
                  // Ignore this file...
                  System.out.println("Attachement " + source.getName() + " Not Found - Skipped!");
               }
            }
         }
         // Place the entire kit & caboodle in the message
         message.setContent(multipart );

         // Send message
         Transport.send(message);
      }
      catch (MessagingException e) {
         e.printStackTrace();
      }

   }
}

