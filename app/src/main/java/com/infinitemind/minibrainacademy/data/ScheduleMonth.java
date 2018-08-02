package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class ScheduleMonth extends Identifiable {
	private ArrayList<Event> events;
	private ArrayList<Week> weeks;
	private int selectedWeek;
	private int selectedMonth;
	private int selectedYear;

	public ScheduleMonth(ArrayList<Event> events, ArrayList<Week> weeks, int selectedMonth, int selectedYear) {
		this.events = events;
		this.weeks = weeks;
		this.selectedMonth = selectedMonth;
		this.selectedYear = selectedYear;
		this.selectedWeek = 0;
		this.id = UUID.randomUUID();
	}


	public static int getMonthIndexByEventId(ArrayList<ScheduleMonth> months, UUID id) {
		for(int i = 0; i < months.size(); i++)
			if(Identifiable.contains(months.get(i).getEvents(), id)) return i;
		return -1;
	}


	public ArrayList<Event> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}

	public ArrayList<Week> getWeeks() {
		return weeks;
	}

	public void setWeeks(ArrayList<Week> weeks) {
		this.weeks = weeks;
	}

	public int getSelectedWeek() {
		return selectedWeek;
	}

	public void setSelectedWeek(int selectedWeek) {
		this.selectedWeek = selectedWeek;
	}

	public int getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(int selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	public int getSelectedYear() {
		return selectedYear;
	}

	public void setSelectedYear(int selectedYear) {
		this.selectedYear = selectedYear;
	}
}