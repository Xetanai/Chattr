package moe.xetanai.chattr;

import moe.xetanai.chattr.entities.Conversation;
import moe.xetanai.chattr.entities.Search;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Matchmaker {
	private static final long MAXTIME = 30000; // 30 seconds
	private static final List<Search> searches = new CopyOnWriteArrayList<>();
	private static final List<Conversation> conversations = new ArrayList<>();
	private static final Logger log = LoggerFactory.getLogger("Matchmaker");

	private static final Thread passiveMatchmaker = new Thread(() -> {
		boolean interrupted = false;
		log.info("Passive matchmaker thread started.");
		while (!interrupted) {
			run();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException err) {
				log.error("Matchmaker thread was interrupted! Passive matchmaking may fail.", err);
				interrupted = true;
			}
		}

		Thread.currentThread().interrupt();
	});

	private Matchmaker() {}

	public static void start() {
		passiveMatchmaker.start();
	}

	public static void addSearch(Search s) {
		searches.add(s);
	}

	public static void stopSearch(Search s) {
		searches.remove(s);
	}

	public static Conversation startConversation(Search u1, Search u2) {
		Conversation c = new Conversation(u1, u2);
		conversations.add(c);
		c.sendSystemMessage("Found a match! You share " + c.getMutualInterests().size() + " interests:\n" +
				ChattrUtils.joinList(", ", c.getMutualInterests(), 10), null);

		if (c.getMutualInterests().isEmpty()) {
			String msg = "We couldn't find anyone who shares any interests with you :(\nTry again later. For now, here's someone random!";
			if (!u1.getInterests().isEmpty()) // Send condolences to anyone who was looking with interests.
				c.sendSystemMessage(msg, u1.getUser());
			if (!u2.getInterests().isEmpty())
				c.sendSystemMessage(msg, u2.getUser());
		}
		return c;
	}

	public static void stopConversation(Conversation c) {
		conversations.remove(c);
	}

	public static Conversation getConversationForUser(User u) {
		for (Conversation c : conversations) {
			if (c.getUser1().equals(u) || c.getUser2().equals(u))
				return c;
		}

		return null;
	}

	public static Search getSearchForUser(User u) {
		for (Search s : searches) {
			if (s.getUser().equals(u))
				return s;
		}

		return null;
	}

	private static void run() {
		List<Search> completedMatches = new ArrayList<>(); // List of completed matches.
		// Because searches is a CopyOnWriteArrayList, this entire run would not reflect changes and could cause some funny business.

		for (Search s : searches) {
			if (completedMatches.contains(s)) {
				continue;
			} // We've already matched this user in this run. Awaiting removal.

			double minCompat = s.getMinimumCompatibility();

			for (Search p : searches) {
				if (completedMatches.contains(p)) {
					continue;
				} // We've already matched this user in this run. Awaiting removal.

				if (s.equals(p)) {continue;} // Skip self
				double partnerMinCompat = p.getMinimumCompatibility();
				double compat = s.getCompatibility(p);

				if (compat >= minCompat && compat >= partnerMinCompat) {
					// These users are sufficiently compatible.
					startConversation(s, p);
					completedMatches.add(s);
					completedMatches.add(p);
				}
			}
		}

		searches.removeAll(completedMatches); // Remove completed matches from the search queue now that we're finished iterating.
	}
}
