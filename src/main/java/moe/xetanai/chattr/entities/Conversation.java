package moe.xetanai.chattr.entities;

import moe.xetanai.chattr.Chattr;
import moe.xetanai.chattr.Matchmaker;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
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
		user1 = s1.getUser();
		user2 = s2.getUser();

		mutualInterests = new ArrayList<>(s1.getInterests());
		mutualInterests.retainAll(s2.getInterests());

		user1revealed = false;
		user2revealed = false;
		casePending = -1;

		log = new ArrayList<>();

		startTime = new Date().getTime();
	}

	/* SETTERS */

	public void addMessageToLog(Message m) {
		String toadd = "";
		if(m.getAuthor().isBot())
			toadd += "SYSTEM: ";
		else if(isRevealed())
			toadd += m.getAuthor() +": ";
		else
			toadd += "User "+ getUserNum(m.getAuthor()) +": ";
		toadd += m.getContentRaw();

		log.add(toadd);
	}

	public void addMessageToLog(String s) {
		log.add(s);
	}

	public void toggleReveal(User u) {
		if(u.equals(user1))
			user1revealed = !user1revealed;
		else if(u.equals(user2))
			user2revealed = !user2revealed;
	}

	public void togglePendingCase(User u) {
		casePending = getUserNum(u);
	}

	/* GETTERS*/

	public boolean isRevealed() {
		return user1revealed && user2revealed; // If both have revealed, users' names+discrims are exposed to each other.
	}

	public boolean isUserRevealed(User u) {
		if(u.equals(user1))
			return user1revealed;
		else if(u.equals(user2))
			return user2revealed;

		return false;
	}

	public List<String> getLog() {
		return log;
	}

	public User getUser1() {
		return user1;
	}

	public User getUser2() {
		return user2;
	}

	public int getUserNum (User u) {
		if(u.equals(user1)) return 1;
		if (u.equals(user2)) return 2;
		return -1;
	}

	public long getStartTime() {
		return startTime;
	}

	public List<String> getMutualInterests() {
		return mutualInterests;
	}

	public String getMutualInterestsAsString() {
		String compiled = "";
		for(int i = 0; i < mutualInterests.size() && i < 10; i++) {
			if(i != 0) {
				compiled += ", ";
			}

			compiled += mutualInterests.get(i);

			if(i == 9) {
				compiled += ", and "+ (mutualInterests.size()-10) +" other(s)";
			}
		}

		return compiled;
	}

	/* CONVENIENCE METHODS */

	public void sendSystemMessage(String msg, User specific) {
		if(specific != null) {
			// We can edit original as it's not going to both users.
			msg += "\n*(only you can see this)*";
			specific.openPrivateChannel().complete().sendMessage(msg).queue(this::addMessageToLog);
			return;
		}

		String temp = msg;
		user1.openPrivateChannel().complete().sendMessage(
				temp.replace("%USER1%","You have")
				.replace("%USER2%", "Your partner has")
		).queue();

		temp = msg;
		user2.openPrivateChannel().complete().sendMessage(
				temp.replace("%USER1%","Your partner has")
				.replace("%USER2%", "You have")
		).queue();

		addMessageToLog("SYSTEM: "+ msg);
	}

	public void sendPartnerInfo(User requester) {
		if(!this.isRevealed()) {return;}

		if(requester == null) {
			sendPartnerInfo(user1);
			sendPartnerInfo(user2);
			return;
		}

		User target = getUserNum(requester) == 1 ? user2 : user1;

		MessageEmbed m = new EmbedBuilder()
				.setTitle(target.getName() +"#"+ target.getDiscriminator())
				.setImage(target.getEffectiveAvatarUrl())
				.addField("Shared interests", getMutualInterestsAsString(), true).build();

		requester.openPrivateChannel().complete().sendMessage(m).queue();
	}

	public void stop(User u) {
		Matchmaker.stopConversation(this);

		this.sendSystemMessage("%USER"+ getUserNum(u) +"% ended the conversation.", null);
		String haste;
		try {
			haste = postLogToHastebin();
			this.sendSystemMessage("Your logs are available here: https://www.hastebin.com/"+ haste, null);

			if(casePending != -1) {
				Chattr.API.getTextChannelById(402558869877817345L).sendMessage("A Chattr convertsation was reported. Please review the following logs.\nhttps://www.hastebin.com/"+ haste +"\n" +
						"Reporter: User 1 ("+ (casePending == 1 ? user1.getId() : user2.getId()) +")").queue();
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

		for(String line : log) {
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

		while((il = in.readLine()) != null) {
			response.append(il);
		}
		in.close();
		con.disconnect();

		Pattern p = Pattern.compile("\\{\"key\":\"(.*)\"}");
		Matcher m = p.matcher(response);

		if(!m.find()) {
			throw new UnsupportedOperationException("Hastebin did not provide a valid key!");
		}

		String key = m.group(1);

		return key;
	}
}
