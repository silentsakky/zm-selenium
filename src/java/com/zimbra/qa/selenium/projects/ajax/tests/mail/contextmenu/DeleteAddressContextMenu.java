/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2015, 2016 Synacor, Inc.
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
 *
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.selenium.projects.ajax.tests.mail.contextmenu;

import org.testng.annotations.*;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew.Field;

public class DeleteAddressContextMenu extends PrefGroupMailByMessageTest {

	public DeleteAddressContextMenu() {
		logger.info("New " + DeleteAddressContextMenu.class.getCanonicalName());
		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "text");
	}

	
	@Test( description = "Right click To bubble address >> Delete", 
			groups = { "smoke", "L1" })
	
	public void DeleteToAddressContextMenu_01() throws HarnessException {

		// Create the message data to be sent
		MailItem mail = new MailItem();
		mail.dToRecipients.add(new RecipientItem(ZimbraAccount.AccountA(), RecipientItem.RecipientType.To));

		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertNotNull(mailform, "Verify the new form opened");

		// Fill out the form with the data
		mailform.zFill(mail);
		app.zPageMail.zRightClickAddressBubble(Field.To);
		app.zPageMail.zDeleteAddressContextMenu();
		ZAssert.assertTrue(app.zPageMail.zHasTOCcBccEmpty(), "To/Cc/Bcc should be empty");

	}

	
	@Test( description = "Right click Cc bubble address >> Delete", 
			groups = { "smoke", "L1" })
	
	public void DeleteCcAddressContextMenu_02() throws HarnessException {

		// Create the message data to be sent
		MailItem mail = new MailItem();
		mail.dCcRecipients.add(new RecipientItem(ZimbraAccount.AccountB(), RecipientItem.RecipientType.Cc));

		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertNotNull(mailform, "Verify the new form opened");

		// Fill out the form with the data
		mailform.zFill(mail);
		app.zPageMail.zRightClickAddressBubble(Field.Cc);
		app.zPageMail.zDeleteAddressContextMenu();
		ZAssert.assertTrue(app.zPageMail.zHasTOCcBccEmpty(), "To/Cc/Bcc should be empty");
	}

	
	@Test( description = "Right click BCc bubble address >> Delete", 
			groups = { "smoke", "L1" })
	
	public void DeleteBccAddressContextMenu_03() throws HarnessException {

		// Create the message data to be sent
		MailItem mail = new MailItem();
		mail.dBccRecipients.add(new RecipientItem(ZimbraAccount.AccountB(), RecipientItem.RecipientType.Bcc));

		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertNotNull(mailform, "Verify the new form opened");

		// Fill out the form with the data
		mailform.zFill(mail);
		app.zPageMail.zRightClickAddressBubble(Field.Bcc);
		app.zPageMail.zDeleteAddressContextMenu();
		ZAssert.assertTrue(app.zPageMail.zHasTOCcBccEmpty(), "To/Cc/Bcc should be empty");
	}
	
}