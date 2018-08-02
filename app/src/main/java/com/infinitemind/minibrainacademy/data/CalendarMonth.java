package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class CalendarMonth {
	private int month; //0 - 11
	private int year; //ex.: 2018
	private ArrayList<SelectedDay> selectedDays; //ex.: 0, 7, 30

	public CalendarMonth(int month, int year, ArrayList<SelectedDay> selectedDays) {
		this.month = month;
		this.year = year;
		this.selectedDays = selectedDays;
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

	public ArrayList<SelectedDay> getSelectedDays() {
		return selectedDays;
	}

	public void setSelectedDays(ArrayList<SelectedDay> selectedDays) {
		this.selectedDays = selectedDays;
	}
}
