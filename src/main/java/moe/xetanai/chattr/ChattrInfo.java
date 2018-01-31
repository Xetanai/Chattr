package moe.xetanai.chattr;

import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChattrInfo {
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 2;
	public static final int VERSION_PATCH = 0;
	public static final int BUILD = 39;
	public static final String VERSION = String.format("%d.%d.%d_%d", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, BUILD);

	public static final List<Long> devs = new ArrayList<>();

	private ChattrInfo() {}

	public static boolean isDevVersion() {
		return new File(".git").isDirectory();
	}

	public static List<Long> getDeveloperIds() {
		return devs;
	}

	public static boolean isDeveloper(User u) {
		return devs.contains(u.getIdLong());
	}

	public static void init() {
		if (!devs.isEmpty()) {return;} // This should be called only once ever.

		JSONArray devarr = Chattr.RAWCFG.getJSONArray("developers");
		for (int i = 0; i < devarr.length(); i++) {
			devs.add(devarr.getLong(i));
		}
	}
}
