package moe.xetanai.chattr;

import moe.xetanai.chattr.entities.Conversation;
import moe.xetanai.chattr.entities.Search;
import net.dv8tion.jda.core.entities.User;
import java.util.ArrayList;
import java.util.List;

public class Matchmaker {
	private static final long MAXTIME = 30000; // 30 seconds
	private static final List<Search> searches = new ArrayList<>();
	private static final List<Conversation> conversations = new ArrayList<>();

	private Matchmaker() {}

	public static boolean SearchFor(Search priority) {
		double minCompat = priority.getMinimumCompatibility();

		for(Search s : searches) {
			if(s.equals(priority))
				continue; // Skip self in list.
			double partnerMinCompat = s.getMinimumCompatibility();
			double compat = priority.getCompatibility(s);

			if(compat >= minCompat && compat >= partnerMinCompat) {
				// These users are sufficiently compatible.
				startConversation(priority,s);
				return true;
			}
		}
		return false;
	}

	public static void addSearch(Search s) {
		searches.add(s);
	}

	public static void stopSearch(Search s) {
		searches.remove(s);
	}

	public static Conversation startConversation(Search u1, Search u2) {
		Conversation c = new Conversation(u1,u2);
		conversations.add(c);
		c.sendSystemMessage("Found a match! You share "+ c.getMutualInterests().size() +" interests:\n" +
				c.getMutualInterestsAsString(), null);

		if(c.getMutualInterests().isEmpty()) {
			String msg = "We couldn't find anyone who shares any interests with you :(\nTry again later. For now, here's someone random!";
			if(!u1.getInterests().isEmpty())
				c.sendSystemMessage(msg, u1.getUser());
			if(!u2.getInterests().isEmpty())
				c.sendSystemMessage(msg, u2.getUser());
		}
		searches.remove(u1);
		searches.remove(u2);
		return c;
	}

	public static void stopConversation(Conversation c) {
		conversations.remove(c);
	}

	public static Conversation getConversationForUser(User u) {
		for(Conversation c : conversations) {
			if(c.getUser1().equals(u) || c.getUser2().equals(u))
				return c;
		}

		return null;
	}

	public static Search getSearchForUser(User u) {
		for (Search s : searches) {
			if(s.getUser().equals(u))
				return s;
		}

		return null;
	}
}
