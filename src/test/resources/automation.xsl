<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
          xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xsl:template match="/">
    <html>
      <head/>

      <!-- <body title="Automation Test Result" onload="alternatecolor('alternate');"> -->
      <body title="Automation Test Result">

          <table align="center" width="80%" border="0" cellspacing="1" bgcolor="#000000" id="alternate">
            <tr>
              <td>
                <table align="center" width="100%" height="10%" border="0" cellspacing="1" bgcolor="#000000" id="alternate">
                  <tr>
                    <td width="50" bgcolor="#011414">
                      <img width="100" src="https://github.com/BeyMelamed/JavaDDT/blob/master/src/test/resources/Images/Crown.png?raw=true" alt="https://github.com/BeyMelamed/JavaDDT/blob/master/src/test/resources/Images/Crown.png?raw=true"></img>
                    </td>
                    <td width="100%" height="100%">
                      <table align="center" width="100%" height="100%" border="0" cellspacing="0" bgcolor="#000000" id="alternate">
                        <!-- Project Element -->
                        <tr>
                          <td width="20%" style="background-color:#011414; color:#ffffff;">
                            <b>Project Name</b>
                          </td>
                          <td bgcolor="5BF7F7">
                            <b>
                              <xsl:value-of select="Project/@name" />
                            </b>
                          </td>
                        </tr>
                        <tr>
                        </tr>
                        <!-- Module Element -->
                        <tr>
                          <td style="background-color:#011414; color:#ffffff;">
                            <b>Module Name</b>
                          </td>
                          <td bgcolor="5BF7F7">
                            <b>
                              <xsl:value-of select="Project/Module/@name" />
                            </b>
                          </td>
                        </tr>
                        <tr>
                        </tr>
                        <!-- Mode Element -->
                        <tr>
                          <td style="background-color:#011414; color:#ffffff;">
                            <b>Reporting Mode</b>
                          </td>
                          <td bgcolor="5BF7F7">
                            <b>
                              <xsl:value-of select="Project/Module/Mode/@name" />
                            </b>
                          </td>
                        </tr>
                        <tr>
                        </tr>
                          <!-- OS Element -->
                          <tr>
                              <td style="background-color:#011414; color:#ffffff;">
                                  <b>Operating System</b>
                              </td>
                              <td bgcolor="5BF7F7">
                                  <b>
                                      <xsl:value-of select="Project/Module/Mode/OperatingSystem/@name" />
                                  </b>
                              </td>
                          </tr>
                          <tr>
                          </tr>
                          <!-- Environment Element -->
                          <tr>
                              <td style="background-color:#011414; color:#ffffff;">
                                  <b>Environment</b>
                              </td>
                              <td bgcolor="5BF7F7">
                                  <b>
                                      <xsl:value-of select="Project/Module/Mode/OperatingSystem/Environment/@name" />
                                  </b>
                              </td>
                          </tr>
                          <tr>
                          </tr>
                          <!-- Java Element -->
                          <tr>
                              <td style="background-color:#011414; color:#ffffff;">
                                  <b>Java</b>
                              </td>
                              <td bgcolor="5BF7F7">
                                  <b>
                                      <xsl:value-of select="Project/Module/Mode/OperatingSystem/Environment/Java/@name" />
                                  </b>
                              </td>
                          </tr>
                          <tr>
                          </tr>
                          <!-- User Element -->
                          <tr>
                              <td style="background-color:#011414; color:#ffffff;">
                                  <b>User</b>
                              </td>
                              <td bgcolor="5BF7F7">
                                  <b>
                                      <xsl:value-of select="Project/Module/Mode/OperatingSystem/Environment/Java/User/@name" />
                                  </b>
                              </td>
                          </tr>
                          <tr>
                          </tr>
                        <!-- Summary Element -->
                        <tr>
                          <td style="background-color:#011414; color:#ffffff;">
                            <b>Summary</b>
                          </td>
                          <td bgcolor="5BF7F7">
                            <b>
                                <xsl:value-of select="Project/Module/Mode/OperatingSystem/Environment/Java/User/Summary/@name" />
                            </b>
                          </td>
                        </tr>                      
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>
                <table align="center" width="100%" border="0" cellspacing="1" bgcolor="#000000" id="alternate">
                  <thead>
                    <tr style="background-color:#000055; color:#ffffff;">
                      <th width="5%">
                        <font face="verdana" size="2">Step #</font>
                      </th>
                      <th width="65%">
                        <font face="verdana" size="2">Action and Step Detail</font>
                      </th>
                      <th width="30%">
                        <font face="verdana" size="2">Execution Errors and Comments</font>
                      </th>
                      <th>
                        <font face="verdana" size="2">Status</font>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <xsl:for-each select="Project/Module/Mode/OperatingSystem/Environment/Java/User/Summary/Steps/Step">
                      <xsl:sort select="Id"/>
                      <xsl:variable name="altColor">
                        <xsl:choose>
                          <xsl:when test="position() mod 2 = 0">099D9D</xsl:when>
                          <xsl:otherwise>5BF7F7</xsl:otherwise>
                        </xsl:choose>
                      </xsl:variable>
                      <tr bgcolor="{$altColor}">
                        <b>
                          <td>
                            <b>
                              <font face="verdana" size="2">
                                <xsl:value-of select="@Id" />
                              </font>
                            </b>
                          </td>
                          <td>
                            <b>
                              <font face="verdana" size="2">
                                <xsl:value-of select="@Name"  disable-output-escaping="yes"/>
                              </font>
                            </b>
                          </td>
                          <td>
                              <xsl:value-of select="@ErrDesc" disable-output-escaping="yes" />
                          </td>
        <td>
          <xsl:choose>
            <xsl:when test="@Status='PASS'">
              <font color="darkgreen" face="verdana" size="2">
                <b>Pass</b>
              </font>
            </xsl:when>
            <xsl:when test="@Status='FAIL'">
              <font color="red" face="verdana" size="4">
                <b>Fail</b>
              </font>
            </xsl:when>
            <xsl:when test="@Status='SKIP'">
              <font color="blue" face="verdana" size="2" font-style="italic">
                <b>Skip</b>
              </font>
            </xsl:when>
          </xsl:choose>
        </td>
        </b>
        </tr>
        </xsl:for-each>
        </tbody>
        </table>
        </td>
        </tr>
        </table>
      
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
