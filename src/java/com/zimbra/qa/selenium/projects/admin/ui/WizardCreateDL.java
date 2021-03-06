/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.admin.ui;

import java.awt.event.KeyEvent;
import com.zimbra.qa.selenium.framework.items.IItem;
import com.zimbra.qa.selenium.framework.ui.AbsTab;
import com.zimbra.qa.selenium.framework.ui.AbsWizard;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.projects.admin.items.DistributionListItem;

public class WizardCreateDL extends AbsWizard {
	public static class Locators {
		public static final String zdlg_DL_NAME = "zdlgv__NEW_DL_name";
		public static final String zdlg_DOMAIN_NAME="zdlgv__NEW_DL_name_2_display";
		public static final String zdlg_Check_Dynamic_group ="css=input[id='zdlgv__NEW_DL_dlType']";
		public static final String zdlg_Check_Right_Management ="css=input[id='zdlgv__NEW_DL_zimbraIsACLGroup']";
		public static final String zdlg_MemberURL ="css=input[id='zdlgv__NEW_DL_memberURL']";
	}

	public WizardCreateDL(AbsTab page) {
		super(page);
	}

	@Override
	public IItem zCompleteWizard(IItem item) throws HarnessException {

		if ( !(item instanceof DistributionListItem) )
			throw new HarnessException("item must be an DistributionListItem, was "+ item.getClass().getCanonicalName());

		DistributionListItem dl = (DistributionListItem)item;

		String CN = dl.getLocalName();
		String domain = dl.getDomainName();


		zType(Locators.zdlg_DL_NAME, CN);
		SleepUtil.sleepSmall();
		this.clearField(Locators.zdlg_DOMAIN_NAME);
		zType(Locators.zdlg_DOMAIN_NAME, "");
		zType(Locators.zdlg_DOMAIN_NAME, domain);

		this.zKeyboard.zTypeKeyEvent(KeyEvent.VK_TAB);
		zType(Locators.zdlg_DL_NAME, CN);

		
		if (dl.getDynamicDL()) {
		clickNext(AbsWizard.Locators.DL_DIALOG);
		sClick(Locators.zdlg_Check_Dynamic_group);
		SleepUtil.sleepMedium();
		sClick(Locators.zdlg_Check_Right_Management);
		zType(Locators.zdlg_MemberURL, dl.getMemberURL());
		}
		
		clickFinish(AbsWizard.Locators.DL_DIALOG);

		return (dl);
	}

	@Override
	public String myPageName() {
		return null;
	}

	@Override
	public boolean zIsActive() throws HarnessException {
		return false;
	}

	public void zComplete(DistributionListItem dl) {
	}

}
