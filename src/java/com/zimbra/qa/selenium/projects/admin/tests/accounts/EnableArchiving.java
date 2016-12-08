/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.admin.tests.accounts;

import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAdminAccount;
import com.zimbra.qa.selenium.framework.util.ConfigProperties;
import com.zimbra.qa.selenium.projects.admin.core.AdminCommonTest;
import com.zimbra.qa.selenium.projects.admin.items.AccountItem;
import com.zimbra.qa.selenium.projects.admin.ui.FormEditAccount;
import com.zimbra.qa.selenium.projects.admin.ui.PageMain;

public class EnableArchiving extends AdminCommonTest {

	public EnableArchiving() {
		logger.info("New "+ EnableArchiving.class.getCanonicalName());
		super.startingPage = app.zPageManageAccounts;
	}

	/**
	 * Testcase : Edit account - Verify administrator should be able to enable archiving at the account level
	 * Steps :
	 * 1. Login to admin console
	 * 2. Edit created account 
	 * 3. Go to Archiving tab > enable archiving
	 * 4. Verify archiving is enabled
	 * @throws HarnessException
	 */
	@Test( description = "Verify administrator should be able to enable archiving at the account level",
			groups = { "smoke", "L1", "network" })

	public void EnableArchiving_01() throws HarnessException {

		// Create a new account in the Admin Console using SOAP
		AccountItem account = new AccountItem("email" + ConfigProperties.getUniqueString(),ConfigProperties.getStringProperty("testdomain"));
		ZimbraAdminAccount.AdminConsoleAdmin().soapSend(
				"<CreateAccountRequest xmlns='urn:zimbraAdmin'>"
		+			"<name>" + account.getEmailAddress() + "</name>"
		+			"<password>test123</password>"
		+		"</CreateAccountRequest>");

		// Refresh the account list
		app.zPageManageAccounts.sClickAt(PageMain.Locators.REFRESH_BUTTON, "");

		// Click on account to be Edited.
		app.zPageManageAccounts.zListItem(Action.A_LEFTCLICK, account.getEmailAddress());

		// Click on Edit button
		FormEditAccount form = (FormEditAccount) app.zPageManageAccounts.zToolbarPressPulldown(Button.B_GEAR_BOX, Button.O_EDIT);

		// Click on archiving
		app.zPageEditAccount.zToolbarPressButton(Button.B_ARCHIVING);
		
		if (app.zPageEditAccount.sIsElementPresent(FormEditAccount.Locators.NO_LABEL) == true)
		{
		form.zEnableArchieving();
		}
	
		// Verify archiving is enabled
		ZAssert.assertTrue(app.zPageEditAccount.sIsElementPresent(FormEditAccount.Locators.YES_LABEL), "Verify archiving is enabled");

	}

}
