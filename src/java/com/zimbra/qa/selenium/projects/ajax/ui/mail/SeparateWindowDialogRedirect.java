/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
/**
 *
 */
package com.zimbra.qa.selenium.projects.ajax.ui.mail;

import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.framework.util.staf.Stafpostqueue;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.DialogRedirect.*;

/**
 * Represents a "Specify Message Send Time" dialog box (Send Later)
 *
 * See https://bugzilla.zimbra.com/show_bug.cgi?id=7524 See
 * https://bugzilla.zimbra.com/show_bug.cgi?id=61935
 *
 * <p>
 *
 * @author Matt Rhoades
 *
 */
public class SeparateWindowDialogRedirect extends AbsDialogSeparateWindow {

	public SeparateWindowDialogRedirect(AbsApplication application, AbsSeparateWindow window) {
		super(application, window);

		logger.info("new " + SeparateWindowDialogRedirect.class.getCanonicalName());

	}

	public static class Field {

		public static final Field To = new Field("To");

		private String field;

		private Field(String name) {
			field = name;
		}

		@Override
		public String toString() {
			return (field);
		}

	}

	public void zFillField(Field field, String value) throws HarnessException {
		tracer.trace("Set " + field + " to " + value);

		String locator = null;

		if (field == Field.To) {

			locator = Locators.FieldEmailLocator;

		} else {
			throw new HarnessException("Unsupported field: " + field);
		}

		if (locator == null) {
			throw new HarnessException("locator was null for field " + field);
		}

		// Make sure the button exists
		if (!MyWindow.sIsElementPresent(locator))
			throw new HarnessException("Field is not present field=" + field + " locator=" + locator);
		
		// Enter text
		MyWindow.sType(locator, value);
		
	}

	public AbsPage zClickButton(Button button) throws HarnessException {
		logger.info(myPageName() + " zClickButton(" + button + ")");

		tracer.trace("Click dialog button " + button);

		AbsPage page = null;
		String locator = null;

		if (button == Button.B_OK) {

			locator = Locators.ButtonOkButtonLocator;

			MyWindow.zClickAt(locator, "");

			MyWindow.zWaitForBusyOverlay();

			// Check the message queue
			Stafpostqueue sp = new Stafpostqueue();
			sp.waitForPostqueue();

			return (page);

		} else if (button == Button.B_CANCEL) {

			locator = Locators.ButtonCancelButtonLocator;

		} else if (button == Button.B_TO) {

			locator = Locators.ButtonToButtonLocator;

		} else {
			throw new HarnessException("Button " + button + " not implemented");
		}

		// Make sure the locator was set
		if (locator == null) {
			throw new HarnessException("Button " + button + " not implemented");
		}

		// Make sure the locator exists
		if (!MyWindow.sIsElementPresent(locator)) {
			throw new HarnessException("Button " + button + " locator " + locator + " not present!");
		}

		MyWindow.zClickAt(locator, "");

		MyWindow.zWaitForBusyOverlay();

		return (page);
	}

	public boolean zIsActive() throws HarnessException {
		logger.info(myPageName() + " zIsActive()");

		// Check if the separate window is active
		if (!super.zIsActive()) {
			return (false);
		}

		String locator = DialogRedirect.Locators.RedirectDialogLocator;

		if (!MyWindow.sIsElementPresent(locator)) {
			return (false);
		}

		if (!MyWindow.zIsVisiblePerPosition(locator, 0, 0)) {
			return (false);
		}

		return (true);
	}

	@Override
	public String myPageName() {
		return (this.getClass().getCanonicalName());
	}

}
