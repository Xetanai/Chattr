package moe.xetanai.chattr;

import java.io.File;

public class ChattrInfo {
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 2;
	public static final int VERSION_PATCH = 0;
	public static final int BUILD = 15;
	public static final String VERSION = String.format("%d.%d.%d_%d", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, BUILD);

	private ChattrInfo() {}

	public static boolean isDevVersion() {
		return new File(".git").isDirectory();
	}
}
