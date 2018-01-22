package moe.xetanai.chattr;

import java.util.List;

public class ChattrUtils {
	private ChattrUtils() {}

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
