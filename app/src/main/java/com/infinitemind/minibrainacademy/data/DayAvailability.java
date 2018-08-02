package com.infinitemind.minibrainacademy.data;

import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class DayAvailability {
	private State state;
	private int day;
	private int month;
	private int year;

	public enum State {
		Available, Unavailable, Empty
	}

	public DayAvailability() {

	}

	public DayAvailability(DayAvailability d) {
		this.state = d.getState();
		this.day = d.getDay();
		this.month = d.getMonth();
		this.year = d.getYear();
	}

	public DayAvailability(State state, int day, int month, int year) {
		this.state = state;
		this.day = day;
		this.month = month;
		this.year = year;
	}


	public static ArrayList<DayAvailability> getDaysFromMonth(ArrayList<DayAvailability> availableDays, int month, int year) {
		ArrayList<DayAvailability> availableDaysFromMonth = new ArrayList<>();
		for(DayAvailability d : availableDays)
			if(d.getMonth() == month && d.getYear() == year)
				availableDaysFromMonth.add(d);
		return availableDaysFromMonth;
	}

	public static DayAvailability getDay(ArrayList<DayAvailability> days, int day, int month, int year) {
		for(DayAvailability d : days)
			if(d.getDay() == day && d.getMonth() == month && d.getYear() == year) return d;
		days.add(new DayAvailability(State.Empty, day, month, year));
		return days.get(days.size() - 1);
	}

	public static ArrayList<DayAvailability> getBackup(ArrayList<DayAvailability> days) {
		ArrayList<DayAvailability> backup = new ArrayList<>();
		for(DayAvailability d : days) backup.add(new DayAvailability(d));
		return backup;
	}

	public static void restoreBackup(ArrayList<DayAvailability> backup, ArrayList<DayAvailability> days) {
		for(DayAvailability d : days) {
			DayAvailability day = DayAvailability.getDay(backup, d.getDay(), d.getMonth(), d.getYear());
			d.set(day);
		}
	}

	public static DayAvailability returnIfContains(ArrayList<DayAvailability> days, DayAvailability day) {
		for(DayAvailability d : days)
			if((day.getState() == null || day.getState() == d.getState()) &&
					day.getYear() == d.getYear() && day.getMonth() == d.getMonth() && day.getDay() == d.getDay())
				return d;
		return null;
	}

	public static HashMap<String, Object> toHashMap(ArrayList<DayAvailability> days) {
		HashMap<String, Object> map = new HashMap<>();
		try {
			for(DayAvailability d : days) {
				Date date = Constants.dateFormat5.parse(d.getDate());
				Calendar now = GregorianCalendar.getInstance();
				long twoMonths = 5184000000L;
				if(now.getTimeInMillis() - date.getTime() < twoMonths)
					map.put(d.getDate(), d);
			}
		} catch(ParseException ignored) {}
		return map;
	}


	public String getDate() {
		return String.format(Locale.getDefault(), "%d-%d-%d", getDay(), getMonth(), getYear());
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void set(DayAvailability d) {
		this.state = d.getState();
		this.day = d.getDay();
		this.month = d.getMonth();
		this.year = d.getYear();
	}
}
