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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.compose.inlineimage;

import java.awt.event.KeyEvent;
import java.io.File;
import org.testng.SkipException;
import org.testng.annotations.Test;
import com.zimbra.common.soap.Element;
import com.zimbra.qa.selenium.framework.items.FolderItem;
import com.zimbra.qa.selenium.framework.items.MailItem;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew.Field;

public class ForwardMailWithInlineImageAttachment extends PrefGroupMailByMessageTest {

	public ForwardMailWithInlineImageAttachment() {
		logger.info("New "+ ForwardMailWithInlineImageAttachment.class.getCanonicalName());
		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "html");
		super.startingAccountPreferences.put("zimbraPrefForwardReplyInOriginalFormat", "FALSE");
	}

	@Test( description = "Forward to a mail with attachment - Verify inline image sent",
			groups = { "smoke", "L1" })

	public void ForwardMailWithInlineImageAttachment_01() throws HarnessException {

		if (OperatingSystem.isWindows() == true) {

			try {

				//-- DATA
				final String mimeSubject = "subjectAttachment";
				final String mimeFile = ConfigProperties.getBaseDirectory() + "/data/public/mime/email17/mime.txt";
				FolderItem sent = FolderItem.importFromSOAP(app.zGetActiveAccount(), FolderItem.SystemFolder.Sent);

				LmtpInject.injectFile(app.zGetActiveAccount().EmailAddress, new File(mimeFile));

				MailItem original = MailItem.importFromSOAP(app.zGetActiveAccount(), "subject:("+ mimeSubject +")");
				ZAssert.assertNotNull(original, "Verify the message is received correctly");

				//-- GUI

				// Refresh current view
				ZAssert.assertTrue(app.zPageMail.zVerifyMailExists(mimeSubject), "Verify message displayed in current view");

				// Select the item
				app.zPageMail.zListItem(Action.A_LEFTCLICK, mimeSubject);

				// Reply to the item
				FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_FORWARD);

				mailform.zFillField(Field.To, ZimbraAccount.AccountA().EmailAddress);

				final String fileName = "samplejpg.jpg";
				final String filePath = ConfigProperties.getBaseDirectory() + "\\data\\public\\other\\" + fileName;

				app.zPageMail.zPressButton(Button.O_ATTACH_DROPDOWN);
				app.zPageMail.zPressButton(Button.B_ATTACH_INLINE);
				zUploadInlineImageAttachment(filePath);

				app.zPageMail.zVerifyInlineImageAttachmentExistsInComposeWindow();

				// Send the message
				mailform.zSubmit();

				//-- Verification

				// From the receiving end, verify the message details
				MailItem received = MailItem.importFromSOAP(ZimbraAccount.AccountA(), "from:("+ app.zGetActiveAccount().EmailAddress +") subject:("+ mimeSubject +")");
				ZAssert.assertNotNull(received, "Verify the message is received correctly");

				ZimbraAccount.AccountA().soapSend(
						"<GetMsgRequest xmlns='urn:zimbraMail'>"
						+		"<m id='"+ received.getId() +"'/>"
						+	"</GetMsgRequest>");

				String getFilename = ZimbraAccount.AccountA().soapSelectValue("//mail:mp[@cd='inline']", "filename");
				ZAssert.assertEquals(getFilename, fileName, "Verify existing attachment exists in the forwarded mail");

				getFilename = ZimbraAccount.AccountA().soapSelectValue("//mail:mp[@cd='attachment']", "filename");
				ZAssert.assertEquals(getFilename, fileName, "Verify attachment exists in the forwarded mail");

				Element[] nodes = ZimbraAccount.AccountA().soapSelectNodes("//mail:mp[@filename='" + fileName + "']");
				ZAssert.assertEquals(nodes.length, 2, "Verify attachment exist in the forwarded mail");


				// Verify UI for attachment
				app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, sent);
				app.zPageMail.zListItem(Action.A_LEFTCLICK, mimeSubject);
				ZAssert.assertTrue(app.zPageMail.zVerifyAttachmentExistsInMail(fileName), "Verify attachment exists in the email");
				ZAssert.assertTrue(app.zPageMail.zVerifyInlineImageAttachmentExistsInMail(), "Verify inline attachment exists in the email");

			} finally {

				app.zPageMain.zKeyboardKeyEvent(KeyEvent.VK_ESCAPE);

			}

		} else {
			throw new SkipException("File upload operation is allowed only for Windows OS, skipping this test...");
		}
	}
}