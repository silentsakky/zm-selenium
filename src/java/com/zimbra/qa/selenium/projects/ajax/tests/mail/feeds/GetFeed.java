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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.feeds;

import java.net.*;
import java.util.*;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.items.FolderItem.SystemFolder;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;

public class GetFeed extends PrefGroupMailByMessageTest {

	public GetFeed() {
		logger.info("New " + GetFeed.class.getCanonicalName());
	}

	@Test( description = "Verify a feed appears in the folder tree", groups = { "functional", "L2" })

	public void GetFeed_01() throws HarnessException, MalformedURLException {

		FolderItem root = FolderItem.importFromSOAP(app.zGetActiveAccount(),
				SystemFolder.UserRoot);
		FolderItem inbox = FolderItem.importFromSOAP(app.zGetActiveAccount(),
				SystemFolder.Inbox);

		String foldername = "folder"
				+ ConfigProperties.getUniqueString();

		// feed.rss=http://server/files/Service/RSS/Basic/basic.xml
		String feed = ConfigProperties.getStringProperty("rss.sample");

		app.zGetActiveAccount().soapSend(
				"<CreateFolderRequest xmlns='urn:zimbraMail'>"
						+ "<folder name='" + foldername + "' l='"
						+ root.getId() + "' url='" + feed + "'/>"
						+ "</CreateFolderRequest>");

		// Click on the "Inbox" to refresh
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, inbox);

		List<FolderItem> folders = app.zTreeMail.zListGetFolders();
		ZAssert.assertNotNull(folders, "Verify the folder list exists");

		// Make sure the message appears in the list
		FolderItem found = null;
		for (FolderItem f : folders) {
			logger.info("Subject: looking for "+ foldername +" found: "+ f.getName());
			if ( f.getName().contains(foldername) ) {
				found = f;
				break;
			}
		}
		ZAssert.assertNotNull(found, "Verify the feed is in the folder tree");

		//ZAssert.assertTrue(app.zPageMail.zIsVisiblePerPosition(
				//"css=div[id^='zti__main_Mail'] td[class='DwtTreeItem-Text'] span:contains('" + foldername + "')", 0, 0),
				//"Verify the feed is in the folder tree");

	}

	@Test( description = "Reload a feed", groups = { "functional", "L2" })

	public void GetFeed_02() throws HarnessException, MalformedURLException {

		FolderItem root = FolderItem.importFromSOAP(app.zGetActiveAccount(),
				SystemFolder.UserRoot);
		FolderItem inbox = FolderItem.importFromSOAP(app.zGetActiveAccount(),
				SystemFolder.Inbox);

		String foldername = "folder"
				+ ConfigProperties.getUniqueString();

		// feed.rss=http://server/files/Service/RSS/Basic/basic.xml
		String url = ConfigProperties.getStringProperty("rss.sample");

		app.zGetActiveAccount().soapSend(
				"<CreateFolderRequest xmlns='urn:zimbraMail'>"
						+ "<folder name='" + foldername + "' l='"
						+ root.getId() + "' url='" + url + "'/>"
						+ "</CreateFolderRequest>");

		FolderItem feed = FolderItem.importFromSOAP(app.zGetActiveAccount(),
				foldername);

		// Click on the "Inbox" to refresh
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, inbox);

		// Click on the "Feed"
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, feed);

		// Click on the "Reload Feed" button
		app.zPageMail.zToolbarPressButton(Button.B_LOADFEED);

		// Get the list of items
		List<MailItem> messages = app.zPageMail.zListGetMessages();
		ZAssert.assertGreaterThan(messages.size(), 0,
				"Verify that RSS items exist in the list");

	}

}
