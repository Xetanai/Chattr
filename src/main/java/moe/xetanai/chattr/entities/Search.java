package moe.xetanai.chattr.entities;

import moe.xetanai.chattr.Matchmaker;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Search {
	private final User user;
	private final List<String> interests;
	private long startTime = -1;
	private static final long MAXTIME = 30000;

	public Search(User user, List<String> interests) {
		List<String> lower = new ArrayList<>();
		for (String i : interests) {
			lower.add(i.toLowerCase()); // Convert all interests to lowercase
		}

		this.user = user;
		this.interests = lower;
	}

	public Search(User user, String... interests) {
		this(user, Arrays.asList(interests));
	}

	public User getUser() {
		return this.user;
	}

	public List<String> getInterests() {
		return this.interests;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void setStartTime() {
		this.startTime = new Date().getTime();
	}

	/* CONVENIENCE METHODS */

	public long getSearchTime() {
		return new Date().getTime() - this.getStartTime();
	}

	public double getMinimumCompatibility() {
		if (this.interests.isEmpty())
			return 0d;
		return 1 - (this.getSearchTime() / MAXTIME);
	}

	public double getCompatibility(Search partner) {
		List<String> mixed = new ArrayList<>(this.interests); // Make a copy to modify and compare
		mixed.removeAll(partner.getInterests()); // Remove possible duplicate/shared interests
		mixed.addAll(partner.getInterests()); // Merge the interests

		if (mixed.size() == 0)
			return 1d; // Two blank interest users will always match.

		int matched = 0; // Total common interests
		for (String i : mixed) { // Loop over every interest
			if (this.interests.contains(i) && partner.getInterests().contains(i)) {
				matched++; // Add 1 if both like this interest
			}
		}

		return ((double) matched) / mixed.size(); // Return percent of mutual interests
	}

	public void start() {
		this.setStartTime();
		Matchmaker.addSearch(this);
	}

	public void stop() {
		Matchmaker.stopSearch(this);
	}

	;
}
