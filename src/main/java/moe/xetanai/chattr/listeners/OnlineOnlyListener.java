package moe.xetanai.chattr.listeners;

import moe.xetanai.chattr.Matchmaker;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import static net.dv8tion.jda.core.OnlineStatus.ONLINE;

public class OnlineOnlyListener extends ListenerAdapter {
	@Override
	public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent e) {
		if (e.getCurrentOnlineStatus() != ONLINE) {
			Matchmaker.getSearchForUser(e.getUser()).stop();
			e.getUser().openPrivateChannel().complete().sendMessage("Your search was ended because your online status changed.").queue();
		}
	}
}
