package com.infinitemind.minibrainacademy.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Event extends Readable {
	private String title;
	private String description;
	private String date;
	private String duration;
	private String address;
	private String phoneNumber;
	private ArrayList<String> requirements;
	private ArrayList<UUID> animators;
	private State state;
	private int cost;
	private int requiredAmount;
	private int color;
	private int notificationType;

	public Event() {
		this("", "", "", 0);
	}

	public Event(String title, String date, String address, int color) {
		this(title, "", date, "", address, "", new ArrayList<>(), new ArrayList<>(), State.Ongoing, 0, 0, color);
	}

	public Event(String title, String description, String date, String duration, String address, String phoneNumber,
	             ArrayList<String> requirements, ArrayList<UUID> animators, State state, int cost, int requiredAmount, int color) {
		this.title = title;
		this.description = description;
		this.date = date;
		this.duration = duration;
		this.address = address;
		this.requirements = requirements;
		this.animators = animators;
		this.phoneNumber = phoneNumber;
		this.state = state;
		this.cost = cost;
		this.requiredAmount = requiredAmount;
		this.color = color;
		this.id = UUID.randomUUID();
		this.creatorId = UUID.randomUUID();
		this.readBy = new ArrayList<>();
		setSelected(false);
	}

	public static ArrayList<Event> getEventsFromMonth(ArrayList<Event> events, int month, int year) {
		ArrayList<Event> eventsFromMonth = new ArrayList<>();
		for(Event e : events)
			if(Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().indexOf(".") + 1, e.getDateFormat1().lastIndexOf("."))) - 1 == month &&
					Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().lastIndexOf(".") + 1, e.getDateFormat1().indexOf(" "))) == year)
				eventsFromMonth.add(e);
		return eventsFromMonth;
	}

	public static ArrayList<Event> getEventsFromWeek(ArrayList<Event> events, Week week, int month, int year, boolean lastWeek) {
		ArrayList<Event> eventsFromWeek = new ArrayList<>();

		Calendar startDate = Calendar.getInstance();
		startDate.set(Calendar.YEAR, year);
		startDate.set(Calendar.MONTH, month);
		if(lastWeek) startDate.add(Calendar.MONTH, 1);
		if(week.getStartOfWeek() > week.getEndOfWeek()) {
			startDate.set(Calendar.DAY_OF_MONTH, week.getEndOfWeek());
			startDate.add(Calendar.DAY_OF_MONTH, -6);
		} else startDate.set(Calendar.DAY_OF_MONTH, week.getStartOfWeek());
		Calendar endDate = Calendar.getInstance();
		endDate.set(Calendar.YEAR, year);
		endDate.set(Calendar.MONTH, month);
		if(lastWeek) endDate.add(Calendar.MONTH, 1);
		if(week.getStartOfWeek() < week.getEndOfWeek()) {
			endDate.set(Calendar.DAY_OF_MONTH, week.getStartOfWeek());
			endDate.add(Calendar.DAY_OF_MONTH, 6);
		} else endDate.set(Calendar.DAY_OF_MONTH, week.getEndOfWeek());

		for(Event e : events) {
			Calendar d = Calendar.getInstance();
			d.set(Calendar.DAY_OF_MONTH, Integer.parseInt(e.getDateFormat1().substring(0, e.getDateFormat1().indexOf("."))));
			d.set(Calendar.MONTH, Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().indexOf(".") + 1, e.getDateFormat1().lastIndexOf("."))) - 1);
			d.set(Calendar.YEAR, Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().lastIndexOf(".") + 1, e.getDateFormat1().indexOf(" "))));
			if(d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0)
				eventsFromWeek.add(e);
		}
		return eventsFromWeek;
	}

	public static ArrayList<Event> getEventsFromDay(ArrayList<Event> events, int day, int month, int year) {
		ArrayList<Event> eventsFromDay = new ArrayList<>();
		for(Event e : events)
			if(Integer.parseInt(e.getDateFormat1().substring(0, e.getDateFormat1().indexOf("."))) == day &&
					Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().indexOf(".") + 1, e.getDateFormat1().lastIndexOf("."))) - 1 == month &&
					Integer.parseInt(e.getDateFormat1().substring(e.getDateFormat1().lastIndexOf(".") + 1, e.getDateFormat1().indexOf(" "))) == year)
				eventsFromDay.add(e);
		return eventsFromDay;
	}

	public static ArrayList<SelectedDay> getSelectedDaysFromMonth(ArrayList<Event> events, int month, int year) {
		ArrayList<SelectedDay> selectedDays = new ArrayList<>();
		for(Event e : getEventsFromMonth(events, month, year))
			selectedDays.add(new SelectedDay(Integer.parseInt(e.getDateFormat1().substring(0, e.getDateFormat1().indexOf("."))), e.getColor()));
		return selectedDays;
	}

	public static String getRequirementsList(Context context, ArrayList<String> requirements) {
		if(requirements.isEmpty())
			return "";
		else {
			StringBuilder stringBuilder = new StringBuilder(context.getResources().getString(R.string.requirements)).append(": \n  -");
			for(String requirement : requirements)
				stringBuilder.append(requirement.toLowerCase().trim()).append(", \n  -");
			stringBuilder.delete(stringBuilder.length() - 6, stringBuilder.length()).append(".");
			return stringBuilder.toString();
		}
	}

	public static ArrayList<Event> searchForEvents(Context c, ArrayList<Event> events, String query) {
		ArrayList<Event> outputEvents = new ArrayList<>();
		String[] phrases = query.toLowerCase().split(" ");
		for(Event e : events) {
			boolean contains = true;
			for(String phrase : phrases) {
				if(!(e.getTitle().toLowerCase().contains(phrase) ||
						e.getDescription().toLowerCase().contains(phrase) ||
						e.getAddress().toLowerCase().contains(phrase) ||
						e.getPhoneNumber().toLowerCase().contains(phrase) ||
						e.getDuration().toLowerCase().contains(phrase) ||
						e.getDateFormat1().toLowerCase().contains(phrase) ||
						e.getMonth(c).toLowerCase().contains(phrase) ||
						Profile.contains(Identifiable.getDataById(Profile.class, e.getAnimators()), phrase) ||
						Requirement.contains(e.getRequirements(), phrase)))
					contains = false;
			}
			if(contains) outputEvents.add(e);
		}
		return outputEvents;
	}

	public static ArrayList<Event> getEventsByState(ArrayList<Event> events, State state) {
		ArrayList<Event> output = new ArrayList<>();
		for(Event e : events) if(e.getState() == state) output.add(e);
		return output;
	}

	public static ArrayList<Event> getEventsByNotState(ArrayList<Event> events, State state) {
		ArrayList<Event> output = new ArrayList<>();
		for(Event e : events) if(e.getState() != state) output.add(e);
		return output;
	}

	public static Comparator<Event> getComparator(boolean sortUp) {
		int mult = sortUp ? 1 : -1;
		return (event1, event2) -> {
			Date date1, date2;
			try {
				date1 = event1.getFullDate();
				date2 = event2.getFullDate();
				if(event1.isSelected() && event2.isSelected()) return mult * date1.compareTo(date2);
				else if(event1.isSelected()) return -mult;
				else if(event2.isSelected()) return mult;
				else return mult * date1.compareTo(date2);
			} catch(ParseException e) {
				e.printStackTrace();
			}
			return 0;
		};
	}

	public static ArrayList<Event> load(DataSnapshot dataSnapshot) {
		ArrayList<Event> events = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren()) {
			Event e = loadEvent(data);
			events.add(e);
		}
		return events;
	}

	@NonNull
	public static Event loadEvent(DataSnapshot data) {
		Event e = new Event();
		if(data.hasChild("id"))
			e.id = UUID.fromString(data.child("id").getValue(String.class));
		if(data.hasChild("title"))
			e.title = data.child("title").getValue(String.class);
		if(data.hasChild("description"))
			e.description = data.child("description").getValue(String.class);
		if(data.hasChild("date"))
			e.date = data.child("date").getValue(String.class);
		if(data.hasChild("address"))
			e.address = data.child("address").getValue(String.class);
		if(data.hasChild("duration"))
			e.duration = data.child("duration").getValue(String.class);
		if(data.hasChild("phoneNumber"))
			e.phoneNumber = data.child("phoneNumber").getValue(String.class);
		if(data.hasChild("color"))
			e.color = data.child("color").getValue(Integer.class);
		if(data.hasChild("cost"))
			e.cost = data.child("cost").getValue(Integer.class);
		if(data.hasChild("requiredAmount"))
			e.requiredAmount = data.child("requiredAmount").getValue(Integer.class);
		if(data.hasChild("state"))
			e.state = State.valueOf(data.child("state").getValue(String.class));
		if(data.hasChild("notification"))
			e.notificationType = data.child("notification").getValue(Integer.class);
		if(data.hasChild("creatorId"))
			e.creatorId = UUID.fromString(data.child("creatorId").getValue(String.class));
		if(data.hasChild("requirements"))
			e.requirements = data.child("requirements").getValue(new GenericTypeIndicator<ArrayList<String>>() {});
		if(e.requirements == null) e.requirements = new ArrayList<>();
		if(data.hasChild("animators")) {
			ArrayList<String> animators = data.child("animators").getValue(new GenericTypeIndicator<ArrayList<String>>() {});
			if(animators != null) e.animators = Identifiable.getIdsFromStringIds(animators);
		}
		if(e.animators == null) e.animators = new ArrayList<>();
		if(data.hasChild("readBy")) {
			ArrayList<String> readBy = data.child("readBy").getValue(new GenericTypeIndicator<ArrayList<String>>() {});
			if(readBy != null) e.readBy = Identifiable.getIdsFromStringIds(readBy);
		}
		if(e.readBy == null) e.readBy = new ArrayList<>();
		return e;
	}

	public int getDay() {
		return Integer.parseInt(getDateFormat1().substring(0, getDateFormat1().indexOf(".")));
	}

	public String getMonth(Context context) {
		int id = context.getResources().getIdentifier("month" + (getMonthIndex() - 1), "string", context.getPackageName());
		return context.getResources().getString(id);
	}

	public int getMonthIndex() {
		return Integer.parseInt(getDateFormat1().substring(getDateFormat1().indexOf(".") + 1, getDateFormat1().lastIndexOf(".")));
	}

	public int getYear() {
		return Integer.parseInt(getDateFormat1().substring(getDateFormat1().lastIndexOf(".") + 1, getDateFormat1().indexOf(" ")));
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getFullDate() throws ParseException {
		return Constants.dateFormat1.parse(date);
	}

	/**
	 * @return full date "dd.MM.yyyy HH:mm"
	 */
	public String getDateFormat1() {
		return date;
	}

	/**
	 * @return date "MMM dd, yyyy"
	 */
	public String getDateFormat2(Context c) {
		return String.format(Locale.getDefault(), "%s %d, %d", getMonth(c), getDay(), getYear());
	}

	/**
	 * @return date "HH:mm"
	 */
	public String getDateFormat3() throws ParseException {
		return Constants.dateFormat3.format(getFullDate());
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public ArrayList<String> getRequirements() {
		return requirements;
	}

	public void setRequirements(ArrayList<String> requirements) {
		this.requirements = requirements;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getRequiredAmount() {
		return requiredAmount;
	}

	public void setRequiredAmount(int requiredAmount) {
		this.requiredAmount = requiredAmount;
	}

	public ArrayList<UUID> getAnimators() {
		return animators;
	}

	public void setAnimators(ArrayList<UUID> animators) {
		this.animators = animators;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void set(Event event) {
		this.title = event.getTitle();
		this.description = event.getDescription();
		this.date = event.getDateFormat1();
		this.duration = event.getDuration();
		this.address = event.getAddress();
		this.requirements = event.getRequirements();
		this.animators = event.getAnimators();
		this.phoneNumber = event.getPhoneNumber();
		this.state = event.getState();
		this.cost = event.getCost();
		this.requiredAmount = event.getRequiredAmount();
		this.color = event.getColor();
		this.id = event.getId();
		this.setSelected(event.isSelected());
	}

	public int getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}

	public enum State {
		Ongoing, Canceled, Done
	}
}
