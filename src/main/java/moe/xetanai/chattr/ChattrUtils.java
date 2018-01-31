package moe.xetanai.chattr;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class ChattrUtils {
	private ChattrUtils() {}

	public static Member getFirstMemberFor(User u) {
		List<Guild> guilds = u.getMutualGuilds();
		if (guilds.isEmpty()) {return null;}

		return guilds.get(0).getMember(u);
	}

	public static String joinList(String delim, List list, int limit) {
		String joined = "";
		for (int i = 0; i < list.size() && (limit == -1 || i < limit); i++) {
			if (i != 0) {
				joined += delim;
			}

			joined += list.get(i).toString();

			if (i == limit - 1) {
				joined += ", and " + (list.size() - 10) + " other(s)";
			}
		}

		return joined;
	}
}
