package moe.xetanai.chattr.entities;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.Matchmaker;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Conversation {
	private final User user1;
	private final User user2;
	private final List<String> mutualInterests;
	private final long startTime;

	private boolean user1revealed;
	private boolean user2revealed;
	private int casePending;
	private List<String> log;

	public Conversation(Search s1, Search s2) {
		this.user1 = s1.getUser();
		this.user2 = s2.getUser();

		this.mutualInterests = new ArrayList<>(s1.getInterests());
		this.mutualInterests.retainAll(s2.getInterests());

		this.user1revealed = false;
		this.user2revealed = false;
		this.casePending = -1;

		this.log = new ArrayList<>();

		this.startTime = new Date().getTime();
	}

	/* SETTERS */

	public void addMessageToLog(Message m) {
		String toadd = "";
		if (m.getAuthor().isBot())
			toadd += "SYSTEM: ";
		else if (this.isRevealed())
			toadd += m.getAuthor() + ": ";
		else
			toadd += "User " + this.getUserNum(m.getAuthor()) + ": ";
		toadd += m.getContentRaw();

		this.log.add(toadd);
	}

	public void addMessageToLog(String s) {
		this.log.add(s);
	}

	public void toggleReveal(User u) {
		if (u.equals(this.user1))
			this.user1revealed = !this.user1revealed;
		else if (u.equals(this.user2))
			this.user2revealed = !this.user2revealed;
	}

	public void togglePendingCase(User u) {
		this.casePending = this.getUserNum(u);
	}

	/* GETTERS*/

	public boolean isRevealed() {
		return this.user1revealed && this.user2revealed; // If both have revealed, users' names+discrims are exposed to each other.
	}

	public boolean isUserRevealed(User u) {
		if (u.equals(this.user1))
			return this.user1revealed;
		else if (u.equals(this.user2))
			return this.user2revealed;

		return false;
	}

	public List<String> getLog() {
		return this.log;
	}

	public User getUser1() {
		return this.user1;
	}

	public User getUser2() {
		return this.user2;
	}

	public int getUserNum(User u) {
		if (u.equals(this.user1)) return 1;
		if (u.equals(this.user2)) return 2;
		return -1;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public List<String> getMutualInterests() {
		return this.mutualInterests;
	}

	public String getMutualInterestsAsString() {
		String compiled = "";
		for (int i = 0; i < this.mutualInterests.size() && i < 10; i++) {
			if (i != 0) {
				compiled += ", ";
			}

			compiled += this.mutualInterests.get(i);

			if (i == 9) {
				compiled += ", and " + (this.mutualInterests.size() - 10) + " other(s)";
			}
		}

		return compiled;
	}

	/* CONVENIENCE METHODS */

	public void sendSystemMessage(String msg, User specific) {
		if (specific != null) {
			// We can edit original as it's not going to both users.
			msg += "\n*(only you can see this)*";
			specific.openPrivateChannel().complete().sendMessage(msg).queue(this::addMessageToLog);
			return;
		}

		String temp = msg;
		this.user1.openPrivateChannel().complete().sendMessage(
				temp.replace("%USER1%", "You have")
						.replace("%USER2%", "Your partner has")
		).queue();

		temp = msg;
		this.user2.openPrivateChannel().complete().sendMessage(
				temp.replace("%USER1%", "Your partner has")
						.replace("%USER2%", "You have")
		).queue();

		this.addMessageToLog("SYSTEM: " + msg);
	}

	public void sendPartnerInfo(User requester) {
		if (!this.isRevealed()) {return;}

		if (requester == null) {
			this.sendPartnerInfo(this.user1);
			this.sendPartnerInfo(this.user2);
			return;
		}

		User target = this.getUserNum(requester) == 1 ? this.user2 : this.user1;

		MessageEmbed m = new EmbedBuilder()
				.setTitle(target.getName() + "#" + target.getDiscriminator())
				.setImage(target.getEffectiveAvatarUrl())
				.addField("Shared interests", this.getMutualInterestsAsString(), true).build();

		requester.openPrivateChannel().complete().sendMessage(m).queue();
	}

	public void stop(User u) {
		Matchmaker.stopConversation(this);

		this.sendSystemMessage("%USER" + this.getUserNum(u) + "% ended the conversation.", null);
		String haste;
		try {
			haste = this.postLogToHastebin();
			this.sendSystemMessage("Your logs are available here: https://www.hastebin.com/" + haste, null);

			if (this.casePending != -1) {
				Chattr.API.getTextChannelById(402558869877817345L).sendMessage("A Chattr convertsation was reported. Please review the following logs.\nhttps://www.hastebin.com/" + haste + "\n" +
						"Reporter: User 1 (" + (this.casePending == 1 ? this.user1.getId() : this.user2.getId()) + ")").queue();
			}
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	private String postLogToHastebin() throws IOException {
		String url = "https://hastebin.com/documents";

		URL u = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) u.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Chattr/1.0");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());

		for (String line : this.log) {
			line += "\n";
			wr.write(line.getBytes(Charset.forName("UTF-8")));
		}

		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream())
		);
		String il;
		StringBuffer response = new StringBuffer();

		while ((il = in.readLine()) != null) {
			response.append(il);
		}
		in.close();
		con.disconnect();

		Pattern p = Pattern.compile("\\{\"key\":\"(.*)\"}");
		Matcher m = p.matcher(response);

		if (!m.find()) {
			throw new UnsupportedOperationException("Hastebin did not provide a valid key!");
		}

		String key = m.group(1);

		return key;
	}
}
