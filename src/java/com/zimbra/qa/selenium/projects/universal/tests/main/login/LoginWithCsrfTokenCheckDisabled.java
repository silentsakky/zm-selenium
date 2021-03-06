/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.universal.tests.main.login;

import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAccount;
import com.zimbra.qa.selenium.framework.util.ZimbraAdminAccount;
import com.zimbra.qa.selenium.projects.universal.core.UniversalCommonTest;

public class LoginWithCsrfTokenCheckDisabled extends UniversalCommonTest {

	public LoginWithCsrfTokenCheckDisabled() {
		logger.info("New "+ LoginWithCsrfTokenCheckDisabled.class.getCanonicalName());
		super.startingPage = app.zPageLogin;
	}

	
	@Test( description = "Login to the webclient after disabling csrf check", priority=5, 
			groups = { "smoke", "L0"})

	public void LoginWithCsrfTokenCheckDisabled_01() throws HarnessException {
		try {

			String zimbraCsrfTokenCheckEnabledValue = "FALSE";

			// Change zimbraCsrfTokenCheckEnabled value to false
			ZimbraAdminAccount.GlobalAdmin().soapSend(
					"<ModifyConfigRequest xmlns='urn:zimbraAdmin'>"
							+		"<a n='zimbraCsrfTokenCheckEnabled'>"+ zimbraCsrfTokenCheckEnabledValue + "</a>"
							+	"</ModifyConfigRequest>");

			// Restart zimbra services
			staf.execute("zmmailboxdctl restart");
			SleepUtil.sleepVeryLong();
			
			for (int i=0; i<=5; i++) {
				app.zPageMain.sRefreshPage();
				if (app.zPageLogin.sIsElementPresent("css=input[class^='ZLoginButton']") == true || 
						app.zPageLogin.sIsElementPresent("css=div[id$='parent-ZIMLET'] td[id$='ZIMLET_textCell']") == true) {
					break;
				} else {
					SleepUtil.sleepLong();
					if (i == 3) {
						staf.execute("zmmailboxdctl restart");
						SleepUtil.sleepLongMedium();
					}
					continue;
				}
			}

			staf.execute("zmcontrol status");
			SleepUtil.sleepMedium();

			// Login
			app.zPageLogin.zLogin(ZimbraAccount.AccountZWC());

			// Verify main page becomes active
			ZAssert.assertTrue(app.zPageMain.zIsActive(), "Verify that the account is logged in");
		}

		finally {

			String zimbraCsrfTokenCheckEnabledValue = "TRUE";

			// Change zimbraCsrfTokenCheckEnabled value to false
			ZimbraAdminAccount.GlobalAdmin().soapSend(
					"<ModifyConfigRequest xmlns='urn:zimbraAdmin'>"
							+		"<a n='zimbraCsrfTokenCheckEnabled'>"+ zimbraCsrfTokenCheckEnabledValue + "</a>"
							+	"</ModifyConfigRequest>");

			staf.execute("zmmailboxdctl restart");
			SleepUtil.sleepVeryLong();

			for (int i=0; i<=5; i++) {
				app.zPageMain.sRefreshPage();
				if (app.zPageLogin.sIsElementPresent("css=input[class^='ZLoginButton']") == true || 
						app.zPageLogin.sIsElementPresent("css=div[id$='parent-ZIMLET'] td[id$='ZIMLET_textCell']") == true) {
					break;
				} else {
					SleepUtil.sleepLong();
					if (i == 3) {
						staf.execute("zmmailboxdctl restart");
						SleepUtil.sleepLongMedium();
					}
					continue;
				}
			}
		}

	}
}
