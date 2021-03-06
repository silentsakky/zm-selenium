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
package com.zimbra.qa.selenium.projects.admin.tests.certificates;

import org.testng.annotations.Test;
import com.zimbra.common.soap.Element;
import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.ConfigProperties;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAdminAccount;
import com.zimbra.qa.selenium.projects.admin.core.AdminCommonTest;
import com.zimbra.qa.selenium.projects.admin.items.*;
import com.zimbra.qa.selenium.projects.admin.ui.WizardInstallCertificate;
import com.zimbra.qa.selenium.projects.admin.ui.WizardInstallCertificate.Locators;

public class InstallSelfSignedCertificate extends AdminCommonTest {

	public InstallSelfSignedCertificate() {
		logger.info("New " + InstallSelfSignedCertificate.class.getCanonicalName());

		// All tests start at the "Alias" page
		super.startingPage = app.zPageManageCertificates;
	}

	/**
	 * Testcase : Install self-signed certificate 1. Install self-signed
	 * certificate from GUI 2. Verify certificate using SOAP.
	 * 
	 * @throws HarnessException
	 */
	@Test(description = "Install self-signed certificate", groups = { "smoke", "L1" })
	public void InstallSelfSignedCertificate_01() throws HarnessException {
		try {
			String CountryName = "US";
			String state = "Texas";
			String city = "Frisco";
			String OrganizationName = "Zimbra";
			String OrganizationUnit = "Zimbra Collaboration Server";
			CertificateItem cert = new CertificateItem();
			cert.setCountryName(CountryName);
			cert.setStateName(state);
			cert.setCityName(city);
			cert.setOrganizationName(OrganizationName);
			cert.setOrganizationUnit(OrganizationUnit);
			String issuer = "Zimbra Collaboration Server";
			String hostname = ConfigProperties.getStringProperty("server.host");

			// Select server
			app.zPageManageCertificates.zListItem(Action.A_LEFTCLICK, hostname);

			// Click on install certificate
			WizardInstallCertificate wizard = (WizardInstallCertificate) app.zPageManageCertificates
					.zToolbarPressPulldown(Button.B_GEAR_BOX, Button.B_INSTALL_CERTIFICATE);

			// Select self-signed certificate
			wizard.setCertificateAction(Locators.INSTALL_SELF_SIGNED_CERTIFICATE);

			// Fill out the wizard
			wizard.zCompleteWizard(cert);

			// Wait for certificate installation
			SleepUtil.sleepVeryLong();
			SleepUtil.sleepVeryLong();

			// Restart zimbra services
			staf.execute("zmmailboxdctl restart");
			app.zPageMain.sRefresh();
			
			ZimbraAdminAccount.AdminConsoleAdmin().provision();
			ZimbraAdminAccount.AdminConsoleAdmin().authenticate();

			// Get server ID
			ZimbraAdminAccount.AdminConsoleAdmin().soapSend("<GetServerRequest xmlns='urn:zimbraAdmin'>"
					+ "<server by='name'>" + hostname + "</server>" + "</GetServerRequest>");
			String id = ZimbraAdminAccount.AdminConsoleAdmin().soapSelectValue("//admin:server", "id");

			// Verify certificate is installed correctly
			ZimbraAdminAccount.AdminConsoleAdmin().soapSend("<GetCertRequest server='" + id
					+ "' xmlns='urn:zimbraAdmin' type='mailboxd' option='self'>" + "</GetCertRequest>");

			// Verify Issuer
			Element cert_issuer = ZimbraAdminAccount.AdminConsoleAdmin()
					.soapSelectNode("//admin:GetCertResponse/admin:cert/admin:issuer", 1);
			ZAssert.assertStringContains(cert_issuer.toString(), issuer, "Verify issuer is correct!");

			// Verify Server
			String cert_server = ZimbraAdminAccount.AdminConsoleAdmin().soapSelectValue("//admin:cert", "server");
			ZAssert.assertStringContains(cert_server, hostname, "Verify server is correct!");
		}

		finally {
			staf.execute("zmcertmgr deploycrt comm /opt/zimbra/ssl/zimbra/commercial/commercial.crt /opt/zimbra/ssl/zimbra/commercial/commercial_ca.crt");
			staf.execute("zmmailboxdctl restart");
			SleepUtil.sleepVeryVeryLong();
		}
	}

}
