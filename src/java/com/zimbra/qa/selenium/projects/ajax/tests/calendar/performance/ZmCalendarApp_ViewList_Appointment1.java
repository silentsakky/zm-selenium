package com.zimbra.qa.selenium.projects.ajax.tests.calendar.performance;

import java.util.*;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.AppointmentItem;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.framework.util.performance.*;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCommonTest;


public class ZmCalendarApp_ViewList_Appointment1 extends AjaxCommonTest {

	
	public ZmCalendarApp_ViewList_Appointment1() throws HarnessException {
		logger.info("New "+ ZmCalendarApp_ViewList_Appointment1.class.getCanonicalName());
		
		super.startingPage = app.zPageMail;
		
		super.startingAccountPreferences = new HashMap<String, String>() {
			private static final long serialVersionUID = 3038458962443347843L;
			{
				put("zimbraPrefCalendarInitialView", "list");
			}};


	}
	
	
	@Test(	description = "Measure the time to load the Calendar, list view, initial view",
			groups = { "performance" })
	public void ZmCalendarApp_01() throws HarnessException {
		
		// Create an appointment
		String subject = "subject"+ ZimbraSeleniumProperties.getUniqueString();
		AppointmentItem.createAppointmentSingleDay(
				app.zGetActiveAccount(),
				Calendar.getInstance(),
				60,
				null,
				subject,
				"content" + ZimbraSeleniumProperties.getUniqueString(),
				"location" + ZimbraSeleniumProperties.getUniqueString(),
				null);


		// Start the perf token
		PerfToken token = PerfMetrics.startTimestamp(PerfKey.ZmCalendarApp, "Load the calendar app, list view, initial load");

		// Go to calendar
		app.zPageCalendar.zNavigateTo();

		PerfMetrics.waitTimestamp(token);
				
		// Wait for the app to load
		app.zPageCalendar.zWaitForActive();
		

	}

	@Test(	description = "Measure the time to load the Calendar, list view, 1 appointment",
			groups = { "performance" })
	public void ZmCalendarApp_02() throws HarnessException {
		
		// Create an appointment
		String subject = "subject"+ ZimbraSeleniumProperties.getUniqueString();
		AppointmentItem.createAppointmentSingleDay(
				app.zGetActiveAccount(),
				Calendar.getInstance(),
				60,
				null,
				subject,
				"content" + ZimbraSeleniumProperties.getUniqueString(),
				"location" + ZimbraSeleniumProperties.getUniqueString(),
				null);


		// Start the perf token
		PerfToken token = PerfMetrics.startTimestamp(PerfKey.ZmCalendarApp, "Load the calendar app, list view, 1 appointment");

		// Go to calendar
		app.zPageCalendar.zNavigateTo();

		PerfMetrics.waitTimestamp(token);
				
		// Wait for the app to load
		app.zPageCalendar.zWaitForActive();
		

	}

}