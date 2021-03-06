/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.selenium.projects.ajax.core;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.*;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;
import org.xml.sax.SAXException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import com.zimbra.qa.selenium.framework.core.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.framework.util.ConfigProperties.AppType;
import com.zimbra.qa.selenium.framework.util.staf.StafServicePROCESS;
import com.zimbra.qa.selenium.projects.ajax.ui.AppAjaxClient;
import com.zimbra.qa.selenium.projects.ajax.ui.contacts.FormContactNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.SeparateWindowDisplayMail;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.SeparateWindowFormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.tasks.FormTaskNew;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.FormApptNew;
import com.zimbra.qa.selenium.projects.ajax.ui.contacts.FormContactDistributionListNew;
import com.zimbra.qa.selenium.projects.ajax.ui.contacts.FormContactGroupNew;

public class AjaxCommonTest {

	protected static Logger logger = LogManager.getLogger(AjaxCommonTest.class);
	protected AppAjaxClient app = null;

	protected AbsTab startingPage = null;
	protected Map<String, String> startingAccountPreferences = null;
	protected Map<String, String> startingUserPreferences = null;
	protected Map<String, String> startingUserZimletPreferences = null;

	private WebDriver webDriver = ClientSessionFactory.session().webDriver();
	WebElement we = null;
	
	protected StafServicePROCESS staf = new StafServicePROCESS();
	String sJavaScriptErrorsHtmlFileName = "Javascript-errors-report.html";

	protected AjaxCommonTest() {
		logger.info("New " + AjaxCommonTest.class.getCanonicalName());

		app = new AppAjaxClient();

		startingPage = app.zPageMain;
		startingAccountPreferences = new HashMap<String, String>();
		startingUserZimletPreferences = new HashMap<String, String>();
	}

	public void commonTestZimbraConfiguration() throws HarnessException {
		
		logger.info("-------------- Pre-configuration and required setup --------------");

		if (ConfigProperties.getStringProperty("staf").equals("true")) {
			
			try {
				// Grant createDistList right to domain
				logger.info("Grant createDistList right to domain");
				staf.execute("zmprov grr domain " + ConfigProperties.getStringProperty("testdomain") + " dom "
						+ ConfigProperties.getStringProperty("testdomain") + " createDistList");
				
				// Disable zimbraSmimeOCSPEnabled attribute for S/MIME
				logger.info("Disable zimbraSmimeOCSPEnabled attribute for S/MIME");
				staf.execute("zmprov mcf zimbraSmimeOCSPEnabled FALSE");
				
			} catch(Exception e) {
				logger.error("Unable to grant createDistList right and can't disable zimbraSmimeOCSPEnabled for S/MIME", e);
			}
		}
	}

	@BeforeSuite(groups = { "always" })
	public void commonTestBeforeSuite() throws HarnessException, IOException, InterruptedException, SAXException {

		logger.info("BeforeSuite: start");
		ZimbraAccount.ResetAccountZWC();

		try {

			ConfigProperties.setAppType(ConfigProperties.AppType.AJAX);

			// Dynamic wait for App to be ready
			int maxRetry = 5;
			int retry = 0;
			boolean appIsReady = false;
			while (retry < maxRetry && !appIsReady) {
				try {
					logger.info("Retry #" + retry);
					retry++;
					webDriver.navigate().to(ConfigProperties.getBaseURL());
					appIsReady = true;

				} catch (WebDriverException e) {
					if (retry == maxRetry) {
						logger.error("Unable to open ajax app. Is a valid certificate installed?", e);
						throw e;
					} else {
						logger.info("App is still not ready...", e);
						SleepUtil.sleep(6000);
						continue;
					}
				}
			}
			logger.info("App is ready!");
			commonTestZimbraConfiguration();

		} catch (WebDriverException e) {
			logger.error("Unable to open ajax app. Is a valid certificate installed?", e);

		} catch (Exception e) {
			logger.warn(e);
		}

		// Delete javascript error file
		if (fGetJavaScriptErrorsHtmlFile().exists()) {
			fGetJavaScriptErrorsHtmlFile().delete();
		}

		logger.info("BeforeSuite: finish");
	}

	@BeforeClass(groups = { "always" })
	public void commonTestBeforeClass() throws HarnessException {
		logger.info("BeforeClass: start");
		logger.info("BeforeClass: finish");
	}

	@BeforeMethod(groups = { "always" })
	public void commonTestBeforeMethod(Method method, ITestContext testContext) throws HarnessException {

		logger.info("BeforeMethod: start");

		// Get the test name & description
		for (ITestNGMethod testngMethod : testContext.getAllTestMethods()) {
			String methodClass = testngMethod.getRealClass().getSimpleName();
			if (methodClass.equals(method.getDeclaringClass().getSimpleName())
					&& testngMethod.getMethodName().equals(method.getName())) {
				synchronized (AjaxCommonTest.class) {
					logger.info("---------BeforeMethod-----------------------");
					logger.info("Test       : " + methodClass + "." + testngMethod.getMethodName());
					logger.info("Description: " + testngMethod.getDescription());
					logger.info("----------------------------------------");
				}
				break;
			}
		}

		// If test account preferences are defined, then make sure the test
		// account uses those preferences
		if ((startingAccountPreferences != null) && (!startingAccountPreferences.isEmpty())) {
			logger.info("BeforeMethod: startingAccountPreferences are defined");

			// If the current test accounts preferences match, then the account
			// can be used
			if (!ZimbraAccount.AccountZWC().compareAccountPreferences(startingAccountPreferences)) {

				logger.info("BeforeMethod: startingAccountPreferences do not match active account");

				// Reset the account
				ZimbraAccount.ResetAccountZWC();

				// Create a new account
				// Set the preferences accordingly
				ZimbraAccount.AccountZWC().modifyAccountPreferences(startingAccountPreferences);
				ZimbraAccount.AccountZWC().modifyUserZimletPreferences(startingUserZimletPreferences);
			}

		}

		// If test account zimlet preferences are defined, then make sure the
		// test account uses those zimlet preferences
		if ((startingUserZimletPreferences != null) && (!startingUserZimletPreferences.isEmpty())) {
			logger.info("BeforeMethod: startingAccountZimletPreferences are defined");

			// If the current test accounts preferences match, then the account
			// can be used
			if (!ZimbraAccount.AccountZWC().compareUserZimletPreferences(startingUserZimletPreferences)) {

				logger.info("BeforeMethod: startingAccountZimletPreferences do not match active account");
				ZimbraAccount.ResetAccountZWC();
				ZimbraAccount.AccountZWC().modifyAccountPreferences(startingAccountPreferences);
				ZimbraAccount.AccountZWC().modifyUserZimletPreferences(startingUserZimletPreferences);
			}

			ZimbraAccount.AccountZWC().modifyUserZimletPreferences(startingUserZimletPreferences);
		}

		// If AccountZWC is not currently logged in, then login now
		if (!ZimbraAccount.AccountZWC().equals(app.zGetActiveAccount())) {
			logger.info("BeforeMethod: AccountZWC is not currently logged in");

			if (app.zPageMain.zIsActive())
				try {
					app.zPageLogin.sOpen(ConfigProperties.getLogoutURL());
					app.zPageLogin.sOpen(ConfigProperties.getBaseURL());

				} catch (Exception ex) {
					if (!app.zPageLogin.zIsActive()) {
						logger.error("Login page is not active ", ex);

						app.zPageLogin.sOpen(ConfigProperties.getLogoutURL());
						app.zPageLogin.sOpen(ConfigProperties.getBaseURL());
					}
				}
		}

		// If a startingPage is defined, then make sure we are on that page
		if (startingPage != null) {
			logger.info("BeforeMethod: startingPage is defined");

			// If the starting page is not active, navigate to it
			if (!startingPage.zIsActive()) {
				startingPage.zNavigateTo();
			}

			// Confirm that the page is active
			if (!startingPage.zIsActive()) {
				throw new HarnessException("Unable to navigate to " + startingPage.myPageName());
			}

			logger.info("BeforeMethod: startingPage navigation done");
		}

		// Handle open dialogs and tabs
		logger.info("BeforeMethod: Handle open dialogs and tabs");
		app.zPageMain.zHandleDialogs(startingPage);
		app.zPageMain.zHandleComposeTabs();

		logger.info("BeforeMethod: finish");
	}

	public File fGetJavaScriptErrorsHtmlFile() throws HarnessException {
		String sJavaScriptErrorsFolderPath = ExecuteHarnessMain.testoutputfoldername
				+ "\\debug\\projects\\javascript-errors";
		String sJavaScriptErrorsHtmlFilePath = sJavaScriptErrorsFolderPath + "\\" + sJavaScriptErrorsHtmlFileName;
		File fJavaScriptErrorsHtmlFile = new File(sJavaScriptErrorsHtmlFilePath);
		return fJavaScriptErrorsHtmlFile;
	}

	public Path pGetJavaScriptErrorsHtmlFilePath() throws HarnessException {
		String sJavaScriptErrorsFolderPath = ExecuteHarnessMain.testoutputfoldername
				+ "\\debug\\projects\\javascript-errors";
		Path pJavaScriptErrorsHtmlFile = Paths.get(sJavaScriptErrorsFolderPath, sJavaScriptErrorsHtmlFileName);
		return pJavaScriptErrorsHtmlFile;
	}

	@AfterSuite(groups = { "always" })
	public void commonTestAfterSuite() throws HarnessException, IOException {
		logger.info("AfterSuite: start");

		if (ConfigProperties.getStringProperty("javascript.errors.report").equals("true")) {
			if (fGetJavaScriptErrorsHtmlFile().exists()) {
				List<String> lines;
				lines = Arrays.asList("</table>", "</body>",
						"<br/><h2 style='font-family:calibri; font-size:15px;'>** Selenium testcase error screenshot path may or may not be exists, it actually depends on the nature of the javascript error.</h2>",
						"</html>");
				Files.write(pGetJavaScriptErrorsHtmlFilePath(), lines, Charset.forName("UTF-8"),
						StandardOpenOption.APPEND);
			}
		}

		webDriver.quit();
		logger.info("AfterSuite: finished by closing selenium session");
	}

	@AfterClass(groups = { "always" })
	public void commonTestAfterClass() throws HarnessException {
		logger.info("AfterClass: start");

		ZimbraAccount currentAccount = app.zGetActiveAccount();
		if (currentAccount != null && currentAccount.accountIsDirty && currentAccount == ZimbraAccount.AccountZWC()) {
			ZimbraAccount.ResetAccountZWC();
		}

		logger.info("AfterClass: finish");
	}

	@AfterMethod(groups = { "always" })
	public void commonTestAfterMethod(Method method, ITestResult testResult) throws HarnessException, IOException {
		logger.info("AfterMethod: start");

		if (ZimbraURI.needsReload()) {
			logger.error("The URL does not match the base URL. Reload app.");
			app.zPageLogin.sOpen(ConfigProperties.getLogoutURL());
			app.zPageLogin.sOpen(ConfigProperties.getBaseURL());
		}

		if ((!app.zPageMain.zIsActive()) && (!app.zPageLogin.zIsActive())) {
			logger.error("Neither login page nor main page were active. Reload app.", new Exception());
			app.zPageLogin.sOpen(ConfigProperties.getLogoutURL());
			app.zPageLogin.sOpen(ConfigProperties.getBaseURL());
		}

		if (ConfigProperties.getStringProperty("javascript.errors.report").equals("true")) {

			// **************** Capture JavaScript Errors ****************
			logger.info("AfterMethod: Capture javascript errors");

			// Logs, Javascript error folder
			List<String> lines;
			Logs webDriverLog = webDriver.manage().logs();
			LogEntries[] logEntries = { webDriverLog.get(LogType.BROWSER) };

			for (int i = 0; i <= logEntries.length - 1; i++) {

				// Get hostname
				String hostname = null;
				try {
					InetAddress addr;
					addr = InetAddress.getLocalHost();
					hostname = addr.getHostName();
				} catch (UnknownHostException ex) {
					logger.info("Hostname can not be resolved");
				}

				// Configuration parameters
				String application = WordUtils.capitalize(method.getDeclaringClass().toString().split("\\.")[7]);
				String seleniumTestcase = method.getName().toString();
				String testOutputFolderName = ExecuteHarnessMain.testoutputfoldername;

				// Javascript error html file configuration
				String sJavaScriptErrorsFolderPath = testOutputFolderName + "\\debug\\projects\\javascript-errors";
				String sJavaScriptErrorsHtmlFile = sJavaScriptErrorsFolderPath + "\\" + sJavaScriptErrorsHtmlFileName;
				Path pJavaScriptErrorsHtmlFilePath = Paths.get(sJavaScriptErrorsFolderPath,
						sJavaScriptErrorsHtmlFileName);
				File fJavaScriptErrorsHtmlFile = new File(sJavaScriptErrorsHtmlFile);

				// Create javascript-errors folder
				File fJavaScriptErrorsFolder = new File(sJavaScriptErrorsFolderPath);
				if (!fJavaScriptErrorsFolder.exists())
					fJavaScriptErrorsFolder.mkdirs();

				// Screenshot
				String screenShotFilePath;
				if (testOutputFolderName.contains(ConfigProperties.getStringProperty("testOutputDirectory"))) {
					screenShotFilePath = "file:///" + testOutputFolderName
							+ "/debug" + method.getDeclaringClass().toString()
									.replace("class com.zimbra.qa.selenium", "").replace(".", "/")
							+ "/" + seleniumTestcase + "ss1.png";
				} else {
					int appPosition = testOutputFolderName.indexOf(ConfigProperties.getAppType().toString());
					screenShotFilePath = ConfigProperties.getStringProperty("webPortal") + "/portal/machines/"
							+ hostname + "/selenium/" + ConfigProperties.getAppType().toString().toLowerCase()
							+ "/results/" + testOutputFolderName.substring(appPosition)
							+ "/debug" + method.getDeclaringClass().toString()
									.replace("class com.zimbra.qa.selenium", "").replace(".", "/")
							+ "/" + seleniumTestcase + "ss1.png";
				}
				screenShotFilePath = screenShotFilePath.replace("\\", "/");

				// Bug summary
				logger.info("AfterMethod: Get bug summary from bug tracking tool");
				String bugNos = null, commaSeparatedBugSummary = "", commaSeparatedBugStatus = "";
				try {
					bugNos = method.getAnnotation(Bugs.class).ids();
				} catch (NullPointerException e) {
					logger.info("Bugs are not associated for " + method.getName() + " test");
				} finally {
					if (bugNos != null) {
						logger.info("Associated bugs for " + method.getName() + " test: " + bugNos);
					}
				}
				if (bugNos != null && bugNos.contains(",")) {
					bugNos = bugNos.replaceAll(" ", "");
					String[] bugNumbers = bugNos.split(",");
					for (String bugNo : bugNumbers) {
						URL url = new URL(
								ConfigProperties.getStringProperty("bugTrackingTool") + "/show_bug.cgi?id=" + bugNo);
						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(url.openStream(), "UTF-8"))) {
							for (String line; (line = reader.readLine()) != null;) {
								if (line.contains("bz_status_")) {
									commaSeparatedBugStatus = line.substring(line.indexOf("bz_bug bz_status_") + 17,
											line.indexOf(" bz_product_"));
									commaSeparatedBugSummary += "<a target='_blank' href='"
											+ ConfigProperties.getStringProperty("bugTrackingTool")
											+ "/show_bug.cgi?id=" + bugNo + "'>" + "Bug " + bugNo + "</a> ("
											+ commaSeparatedBugStatus + "), ";
									break;
								}
							}
						}
					}
					commaSeparatedBugSummary = commaSeparatedBugSummary.substring(0,
							commaSeparatedBugSummary.length() - 2);

				} else if (bugNos != null && !bugNos.isEmpty()) {
					URL url = new URL(
							ConfigProperties.getStringProperty("bugTrackingTool") + "/show_bug.cgi?id=" + bugNos);
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
						for (String line; (line = reader.readLine()) != null;) {
							if (line.contains("bz_status_")) {
								commaSeparatedBugStatus = line.substring(line.indexOf("bz_bug bz_status_") + 17,
										line.indexOf(" bz_product_"));
								commaSeparatedBugSummary = "<a target='_blank' href='"
										+ ConfigProperties.getStringProperty("bugTrackingTool") + "/show_bug.cgi?id="
										+ bugNos + "'>" + "Bug " + bugNos + "</a> (" + commaSeparatedBugStatus + ")";
								break;
							}
						}
					}

				} else {
					commaSeparatedBugSummary = "<a target='_blank' style='color:brown;' href='"
							+ ConfigProperties.getStringProperty("bugTrackingTool") + "/enter_bug.cgi?product=ZCS'>"
							+ "File a bug</a>";
				}

				if (fJavaScriptErrorsHtmlFile.createNewFile()) {
					logger.info("Javascript errors file is created");

					// Javascript errors html file
					lines = Arrays.asList(
							"<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>",
							"<html>", "<head>", "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>",
							"<link rel='icon' href='" + ConfigProperties.getStringProperty("webPortal")
									+ "/portal/web/wp-content/themes/iconic-one/images/favicon.ico' type='image/x-icon'/>",
							"<title>JavaScript Error Report</title>", "</head>", "<body>",
							"<h2 style='font-family:calibri; font-size:26px;'>Ajax JavaScript Errors Report Generated by Selenium</h2>",
							"<table style='font-family:calibri; font-size:15px;' border='1'>",
							"<tr><th>Application</th><th>Selenium testcase</th><th>Javascript error</th><th>**Screenshot path</th><th>Bug Summary</th></tr>");
					Files.write(pJavaScriptErrorsHtmlFilePath, lines, Charset.forName("UTF-8"),
							StandardOpenOption.APPEND);
				} else {
					logger.info("Javascript errors file already exists");
				}

				for (LogEntry entry : logEntries[i]) {

					// Parse javascript error
					String javaScriptError = new Date(entry.getTimestamp()) + " " + entry.getLevel() + " "
							+ entry.getMessage();
					String seleniumTestcasePath = method.getDeclaringClass().toString().replaceFirst("class ", "") + "."
							+ method.getName();
					logger.info("JavaScript error: " + javaScriptError);

					// Javascript error
					lines = Arrays.asList("<tr><td style='text-align:center'>" + application + "</td><td>"
							+ seleniumTestcasePath + "</td><td style='color:brown;'>" + javaScriptError
							+ "</td><td style='text-align:center'><a target='_blank' href='" + screenShotFilePath + "'>"
							+ "Screenshot" + "</a></td><td style='text-align:center'>" + commaSeparatedBugSummary
							+ "</td></tr>");
					Files.write(pJavaScriptErrorsHtmlFilePath, lines, Charset.forName("UTF-8"),
							StandardOpenOption.APPEND);
				}
			}

		}

		logger.info("AfterMethod: finish");
	}

	@AfterMethod(groups = { "performance" })
	public void performanceTestAfterMethod() {
		ZimbraAccount.ResetAccountZWC();
	}

	@DataProvider(name = "DataProviderSupportedCharsets")
	public Object[][] DataProviderSupportedCharsets() throws HarnessException {
		return (ZimbraCharsets.getInstance().getSampleTable());
	}

	public void ModifyAccountPreferences(String string) throws HarnessException {
		StringBuilder settings = new StringBuilder();
		for (Map.Entry<String, String> entry : startingAccountPreferences.entrySet()) {
			settings.append(String.format("<a n='%s'>%s</a>", entry.getKey(), entry.getValue()));
		}
		ZimbraAdminAccount.GlobalAdmin().soapSend("<ModifyAccountRequest xmlns='urn:zimbraAdmin'>" + "<id>" + string
				+ "</id>" + settings.toString() + "</ModifyAccountRequest>");
	}

	public void zUpload(String filePath) throws HarnessException {

		// File name
		String fileName = filePath.substring(filePath.lastIndexOf('\\') + 1);

		// Upload file
		SleepUtil.sleepLong();
		zUploadFile(filePath);
		// app.zPageMail.zKeyboardTypeStringUpload(filePath);
		SleepUtil.sleepLongMedium();

		// File locator
		String fileLocator = null;
		Boolean isMailApp = app.zPageMail.zIsVisiblePerPosition("div[id^='ztb__COMPOSE']", 0, 0);
		Boolean isCalendarApp = app.zPageMail.zIsVisiblePerPosition("div[id^='ztb__APPT']", 0, 0);
		Boolean isTasksApp = app.zPageMail.zIsVisiblePerPosition("div[id^='ztb__TKE']", 0, 0);
		Boolean isBriefcaseApp = app.zPageMail.zIsVisiblePerPosition("div[class='ZmUploadDialog']", 0, 0);
		Boolean isPreferencesApp = app.zPageMail.zIsVisiblePerPosition("div[id='ztb__PREF']", 0, 0);

		if (isMailApp == true) {
			fileLocator = "css=a[id^='COMPOSE']:contains(" + fileName + ")";
		} else if (isCalendarApp == true || isTasksApp == true) {
			we = webDriver.findElement(By.name("__calAttUpload__"));
		} else if (isBriefcaseApp == true) {
			we = webDriver.findElement(By.name("uploadFile"));
		} else if (isPreferencesApp == true) {
			we = webDriver.findElement(By.name("file"));
		}

		Boolean isFileSelected = false;
		for (int i = 1; i <= 3; i++) {
			if (isMailApp == true) {
				isFileSelected = app.zPageMail.zIsVisiblePerPosition(fileLocator, 0, 0);
			} else {
				isFileSelected = we.getAttribute("value").contains(fileName);
			}
			if (isFileSelected == true) {
				break;
			} else {
				SleepUtil.sleepMedium();
				zUploadFile(filePath);
				SleepUtil.sleepLongMedium();
			}
		}
	}

	public void zUpload(String filePath, SeparateWindowFormMailNew window) throws HarnessException {

		// File name
		String fileName = filePath.substring(filePath.lastIndexOf('\\') + 1);

		// Upload file
		SleepUtil.sleepLong();
		zUploadFile(filePath);
		SleepUtil.sleepLongMedium();

		Boolean isFileSelected = false;
		for (int i = 1; i <= 3; i++) {
			isFileSelected = window.zIsVisiblePerPosition("css=a[id^='COMPOSE']:contains(" + fileName + ")", 0, 0);
			if (isFileSelected == true) {
				break;
			} else {
				SleepUtil.sleepMedium();
				zUploadFile(filePath);
				SleepUtil.sleepLongMedium();
			}
		}
	}

	public void zUpload(String filePath, SeparateWindowDisplayMail window) throws HarnessException {

		// File name
		String fileName = filePath.substring(filePath.lastIndexOf('\\') + 1);

		// Upload file
		SleepUtil.sleepLong();
		zUploadFile(filePath);
		SleepUtil.sleepLongMedium();

		Boolean isFileSelected = false;
		for (int i = 1; i <= 3; i++) {
			isFileSelected = window.zIsVisiblePerPosition("css=a[id^='COMPOSE']:contains(" + fileName + ")", 0, 0);
			if (isFileSelected == true) {
				break;
			} else {
				SleepUtil.sleepMedium();
				zUploadFile(filePath);
				SleepUtil.sleepLongMedium();
			}
		}
	}

	public void zUploadFile(String filePath) throws HarnessException {

		// Put path to your image in a clipboard
		StringSelection ss = new StringSelection(filePath);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		SleepUtil.sleepMedium();

		// Imitate mouse events like ENTER, CTRL+C, CTRL+V
		Robot robot;
		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);

			robot.delay(1000);
			
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void zUploadInlineImageAttachment(String filePath) throws HarnessException {
		SleepUtil.sleepLong();
		zUploadFile(filePath);
		SleepUtil.sleepLongMedium();
	}

	public AbsPage zToolbarPressPulldown(Button button, Button option) throws HarnessException {

		logger.info("Click to zNewDropdownOptions(" + option + ")");

		if (option == null)
			throw new HarnessException("Button cannot be null!");

		String locator = null;
		AbsPage page = null;

		if (option == Button.O_NEW_MESSAGE) {
			locator = "css=td[id='zb__NEW_MENU_NEW_MESSAGE_title']";
			page = new FormMailNew(this.app);

		} else if (option == Button.O_NEW_CONTACT) {
			locator = "css=td[id='zb__NEW_MENU_NEW_CONTACT_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_CONTACTGROUP) {
			locator = "css=td[id='zb__NEW_MENU_NEW_GROUP_title']";
			page = new FormContactGroupNew(this.app);

		} else if (option == Button.O_NEW_DISTRIBUTION_LIST) {
			locator = "css=td[id='zb__NEW_MENU_NEW_DISTRIBUTION_LIST_title']";
			page = new FormContactDistributionListNew(this.app);

		} else if (option == Button.O_NEW_APPOINTMENT) {
			locator = "css=td[id='zb__NEW_MENU_NEW_APPT_title']";
			page = new FormApptNew(this.app);

		} else if (option == Button.O_NEW_TASK) {
			locator = "css=td[id='zb__NEW_MENU_NEW_TASK_title']";
			page = new FormTaskNew(this.app);

		} else if (option == Button.O_NEW_DOCUMENT) {
			locator = "css=td[id='zb__NEW_MENU_NEW_DOC_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_FOLDER) {
			locator = "css=td[id='zb__NEW_MENU_NEW_FOLDER_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_TAG) {
			locator = "css=td[id='zb__NEW_MENU_NEW_TAG_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_CONTACTS_FOLDER) {
			locator = "css=td[id='zb__NEW_MENU_NEW_ADDRBOOK_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_CALENDAR) {
			locator = "css=td[id='zb__NEW_MENU_NEW_CALENDAR_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_TASK_FOLDER) {
			locator = "css=td[id='zb__NEW_MENU_NEW_TASK_FOLDER_title']";
			page = new FormContactNew(this.app);

		} else if (option == Button.O_NEW_BRIEFCASE) {
			locator = "css=td[id='zb__NEW_MENU_NEW_BRIEFCASE_title']";
			page = new FormContactNew(this.app);
		}

		if (locator == null)
			throw new HarnessException("locator was null for option " + option);

		if (!app.zPageMail.sIsElementPresent(locator))
			throw new HarnessException("Button is not present locator=" + locator + " button=" + option);

		// Click to New dropdown
		SleepUtil.sleepMedium();
		app.zPageMail.sClickAt("css=td[id='zb__NEW_MENU_dropdown'] div[class='ImgSelectPullDownArrow']", "");
		SleepUtil.sleepSmall();
		app.zPageMain.zHandleComposeTabs();
		SleepUtil.sleepSmall();

		// Select option
		app.zPageMail.sClickAt(locator, "");
		app.zPageMail.zWaitForBusyOverlay();

		SleepUtil.sleepSmall();

		if (page != null) {
			page.zWaitForActive();
		}

		return (page);
	}

	public void zFreshLogin() {

		ZimbraAccount.ResetAccountZWC();

		try {
			if (ConfigProperties.getAppType() == AppType.AJAX) {
				ConfigProperties.setAppType(ConfigProperties.AppType.AJAX);
			} else if (ConfigProperties.getAppType() == AppType.ADMIN) {
				ConfigProperties.setAppType(ConfigProperties.AppType.ADMIN);
			} else if (ConfigProperties.getAppType() == AppType.TOUCH) {
				ConfigProperties.setAppType(ConfigProperties.AppType.TOUCH);
			} else if (ConfigProperties.getAppType() == AppType.HTML) {
				ConfigProperties.setAppType(ConfigProperties.AppType.HTML);
			} else if (ConfigProperties.getAppType() == AppType.MOBILE) {
				ConfigProperties.setAppType(ConfigProperties.AppType.MOBILE);
			}

			webDriver.navigate().to(ConfigProperties.getLogoutURL());

		} catch (WebDriverException e) {
			logger.error("Unable to open app.", e);
			throw e;
		}

		try {

			((AppAjaxClient) app).zPageLogin.sOpen(ConfigProperties.getLogoutURL());
			if (ConfigProperties.getAppType() == AppType.AJAX) {
				if (ZimbraAccount.AccountZWC() != null) {
					((AppAjaxClient) app).zPageLogin.zLogin(ZimbraAccount.AccountZWC());
				} else {
					((AppAjaxClient) app).zPageLogin.zLogin(ZimbraAccount.Account10());
				}
			}

		} catch (HarnessException e) {
			logger.error("Unable to navigate to app.", e);
		}

	}
}