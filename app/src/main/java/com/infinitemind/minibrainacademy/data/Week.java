package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.Calendar;

public class Week {
	private int startOfWeek;
	private int endOfWeek;
	private int numberOfEvents;

	public Week(int startOfWeek, int endOfWeek) {
		this.startOfWeek = startOfWeek;
		this.endOfWeek = endOfWeek;
	}


	public static Calendar getStartOfWeek(Week week, int month, int year, boolean lastWeek) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MONTH, month);
		now.set(Calendar.YEAR, year);
		if(lastWeek && week.getStartOfWeek() > week.getEndOfWeek()) now.add(Calendar.MONTH, 1);
		now.set(Calendar.DAY_OF_MONTH, week.getEndOfWeek());
		now.add(Calendar.DAY_OF_MONTH, -6);
		return now;
	}

	public static int getWeekIndexByEvent(ArrayList<Week> weeks, int month, int year, Event e) {
		for(int i = 0; i < weeks.size(); i++) {
			int index = Event.getEventsFromWeek(GlobalData.allEvents, weeks.get(i), month, year, i == weeks.size()).indexOf(e);
			if(index >= 0) return i;
		}
		return -1;
	}


	public int getStartOfWeek() {
		return startOfWeek;
	}

	public void setStartOfWeek(int startOfWeek) {
		this.startOfWeek = startOfWeek;
	}

	public int getEndOfWeek() {
		return endOfWeek;
	}

	public void setEndOfWeek(int endOfWeek) {
		this.endOfWeek = endOfWeek;
	}

	public int getNumberOfEvents() {
		return numberOfEvents;
	}

	public void setNumberOfEvents(int numberOfEvents) {
		this.numberOfEvents = numberOfEvents;
	}
}
