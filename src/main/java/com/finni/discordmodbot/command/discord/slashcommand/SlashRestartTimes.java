package com.finni.discordmodbot.command.discord.slashcommand;

import 	com.finni.discordmodbot.DiscordModBot;
import com.finni.discordmodbot.command.CustomConsoleSender;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SlashRestartTimes implements InteractionCommand {

	private final DiscordModBot plugin;

	public SlashRestartTimes() {
		this.plugin = DiscordModBot.getInstance();
	}

	@Override
	public void onCommand(InteractionEvent event) {
		CustomConsoleSender wrapped = new CustomConsoleSender(Bukkit.getServer().getConsoleSender());
		Bukkit.dispatchCommand(wrapped, "uar time");
		Bukkit.getScheduler().runTaskLater(plugin, () -> event.reply(parseTimes(wrapped.getMessage())), 20L);
	}

	private String parseTimes(String uarTime) {
		Pattern pattern= Pattern.compile("(\\d{1,2})([dhms]?)");

		uarTime = uarTime.replaceAll("(§.)+|» ", "")
				.replaceAll("Server Restarting in ", "")
				.replace("!", "")
				.replace("and ", "");

		String formattedTime = uarTime.split("\n")[0];
		Matcher m=pattern.matcher(formattedTime);
		Map<String, String> params = new HashMap<>();
		while (m.find()){
			params.put(m.group(2), m.group(1));
		}

		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime restartIn;
		LocalDateTime restartAgo;

		int days = (params.get("d") != null ? Integer.parseInt(params.get("d")) : 0);
		int hours = (params.get("h") != null ? Integer.parseInt(params.get("h")) : 0);
		int minutes = (params.get("m") != null ? Integer.parseInt(params.get("m")) : 0);
		int seconds = (params.get("s") != null ? Integer.parseInt(params.get("s")) : 0);

		restartIn = now.plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
		String returnv = "The server will restart <t:" + restartIn.toEpochSecond(ZoneOffset.UTC) + ":R> (<t:" + restartIn.toEpochSecond(ZoneOffset.UTC) + ":T>)";

		//prepare uarTime for ago parsing
		uarTime = uarTime.split("\n")[1].replace("Last restart happened ", "").replace(" ago)", "");

		Matcher mAgo=pattern.matcher(uarTime);
		Map<String, String> paramsAgo = new HashMap<>();
		while (mAgo.find()){
			paramsAgo.put(mAgo.group(2), mAgo.group(1));
		}

		int daysAgo = (paramsAgo.get("d") != null ? Integer.parseInt(paramsAgo.get("d")) : 0);
		int hoursAgo = (paramsAgo.get("h") != null ? Integer.parseInt(paramsAgo.get("h")) : 0);
		int minutesAgo = (paramsAgo.get("m") != null ? Integer.parseInt(paramsAgo.get("m")) : 0);
		int secondsAgo = (paramsAgo.get("s") != null ? Integer.parseInt(paramsAgo.get("s")) : 0);

		restartAgo = now.minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo).minusSeconds(secondsAgo); // subtract the time ago

		returnv += "\n(Last restart happened <t:" + restartAgo.toEpochSecond(ZoneOffset.UTC) + ":R>)";
		return returnv;
	}

	@Override
	public String getName() {
		return "restarttime";
	}

	@Override
	public String getDescription() {
		return "See when the server will restart next.";
	}

	@Override
	public List<InteractionCommandArgument> getArguments() {
		return null;
	}

	@Override
	public boolean isEphemeral() {
		return true;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}
}