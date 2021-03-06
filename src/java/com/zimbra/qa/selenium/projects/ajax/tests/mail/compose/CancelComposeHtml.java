/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.compose;

import java.util.List;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.AbsDialog;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.ui.Shortcut;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.DialogWarning;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew.Field;


public class CancelComposeHtml extends PrefGroupMailByMessageTest {

	public CancelComposeHtml() {
		logger.info("New "+ CancelComposeHtml.class.getCanonicalName());
		
		
		
		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "html");

		
	}
	
	@Test( description = "Verify message dialog, when cancel a html draft (body filled)",
			groups = { "functional", "L2" })
	public void CancelComposeHtml_B_01() throws HarnessException {
		
		
		// Create the message data to be sent
		String body = "body" + ConfigProperties.getUniqueString();
		

		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertTrue(mailform.zIsActive(), "Verify the new form opened");
		
		
		
		// Fill out the form with the data
		mailform.zFillField(Field.Body, body);
		

		
		// Cancel the message
		// A warning dialog should appear regarding losing changes
		AbsDialog warning = (AbsDialog)mailform.zToolbarPressButton(Button.B_CANCEL);
		ZAssert.assertNotNull(warning, "Verify the dialog is returned");
		
		// Dismiss the dialog
		warning.zClickButton(Button.B_NO);
		warning.zWaitForClose(); // Make sure the dialog is dismissed
		

	}
	/**
	 * Test Case: Cancel HTML composed mail using keyboard shortcut 'Escape'
	 * 1.Compose Html mail
	 * 2.Press 'Esc' key of keyboard
	 * 3.Verify 'SaveCurrentMessageAsDraft'Warning Dialog
	 * 4.Press No
	 * 5.Verify Message no longer present in inbox
	 * @throws HarnessException
	 */
	
	@Test( description = "Cancel html composed mail using keyboard shortcut 'Escape'",
			groups = { "functional", "L3" })
	public void CancelComposeHtml_A_02() throws HarnessException {
		
		Shortcut shortcut = Shortcut.S_ESCAPE;

		// Create the message data to be sent
		String body = "body" + ConfigProperties.getUniqueString();
		String Subject = "subject" + ConfigProperties.getUniqueString();
		

		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertTrue(mailform.zIsActive(), "Verify the new form opened");	
		
		
		// Fill out the form with the data
		mailform.zFillField(Field.Subject, Subject);
		mailform.zFillField(Field.Body, body);		
		
		DialogWarning warning =(DialogWarning)app.zPageMail.zKeyboardShortcut(shortcut);
		ZAssert.assertNotNull(warning, "Verify the dialog is opened");
		
		warning.zClickButton(Button.B_NO);
		warning.zWaitForClose(); // Make sure the dialog is dismissed
		
		List<MailItem> messages = app.zPageMail.zListGetMessages();
		
		MailItem found = null;
		for (MailItem m : messages) {
			logger.info("Subject: looking for "+ Subject +" found: "+ m.gSubject);
			if ( Subject.equals(m.gSubject) ) {
				found = m;
				break;
			}
		}
		ZAssert.assertNull(found, "Verify the message is no longer in the inbox");
		
	}
}
