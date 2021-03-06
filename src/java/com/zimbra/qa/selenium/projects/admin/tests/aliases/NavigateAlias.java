/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.admin.tests.aliases;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.projects.admin.core.AdminCommonTest;
import com.zimbra.qa.selenium.projects.admin.ui.PageManageAliases;

public class NavigateAlias extends AdminCommonTest {

	public NavigateAlias() {
		logger.info("New "+ NavigateAlias.class.getCanonicalName());

		// All tests start at the "Aliases" page
		super.startingPage = app.zPageManageAliases;
	}
	
	/**
	 * Testcase : Navigate to Aliases page
	 * Steps :
	 * 1. Go to Accounts
	 * 2. Verify navigation path -- "Home --> Manage --> Aliases"
	 * @throws HarnessException
	 */
	@Test( description = "Navigate to Aliases",
			groups = { "sanity", "L0" })
			public void NavigateAlias_01() throws HarnessException {
		
		/*
		 * Verify navigation path -- "Home --> Manage Accounts --> Aliases"
		 */
		ZAssert.assertTrue(app.zPageManageAliases.zVerifyHeader(PageManageAliases.Locators.HOME), "Verfiy the \"Home\" text exists in navigation path");
		ZAssert.assertTrue(app.zPageManageAliases.zVerifyHeader(PageManageAliases.Locators.MANAGE), "Verfiy the \"Manage Accounts\" text exists in navigation path");
		ZAssert.assertTrue(app.zPageManageAliases.zVerifyHeader(PageManageAliases.Locators.ALIAS), "Verfiy the \"Aliases\" text exists in navigation path");
		
	}

}