package com.finni.discordmodbot.command.discord.slashcommand;

import 	com.finni.discordmodbot.DiscordModBot;
import com.finni.discordmodbot.command.CustomConsoleSender;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


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
		// Bukkit.getLogger().info("UAR time is " + uarTime);
		uarTime = uarTime.replace("§x§9§4§8§3§f§f§lU§x§9§9§8§4§f§f§ll§x§9§e§8§5§f§f§lt§x§a§3§8§6§f§f§li§x§a§8§8§7§f§f§lm§x§a§d§8§8§f§f§la§x§b§2§8§9§f§f§lt§x§b§7§8§a§f§f§le§x§b§c§8§b§f§f§lA§x§c§1§8§c§f§f§lu§x§c§6§8§d§f§f§lt§x§c§b§8§e§f§f§lo§x§d§0§8§f§f§f§lR§x§d§5§9§0§f§f§le§x§d§a§9§1§f§f§ls§x§d§f§9§2§f§f§lt§x§e§4§9§3§f§f§la§x§e§9§9§4§f§f§lr§x§e§e§9§5§f§f§lt§r §f» §fRestarting in §d", "")
				.replace("§f!", "");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		///9h 11m and 15s
		//(Last restart happened 3h 55m and 54s ago)
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		if (uarTime.split("\n")[0].contains("h")) {
			hours = Integer.parseInt(uarTime.split("h")[0]);
		}
		if (uarTime.split("\n")[0].contains("m")) {
			if (uarTime.split("\n")[0].contains("h")) {
				minutes = Integer.parseInt(uarTime.split("h ")[1].split("m")[0]);
			} else {
				minutes = Integer.parseInt(uarTime.split("m")[0]);
			}
		}
		if (uarTime.split("\n")[0].contains("s")) {
			if (uarTime.split("\n")[0].contains("m")) {
				seconds = Integer.parseInt(uarTime.split("m and ")[1].split("s")[0]);
			} else if (uarTime.split("\n")[0].contains("h")) {
				seconds = Integer.parseInt(uarTime.split("h and ")[1].replace("s", ""));
			} else {
				seconds = Integer.parseInt(uarTime.split("s")[0]);
			}
		}
		now = now.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
		String returnv = "The server will restart <t:" + now.toEpochSecond(ZoneOffset.UTC) + ":R> (<t:" + now.toEpochSecond(ZoneOffset.UTC) + ":T>)";

		// generate timestamp for ago
		uarTime = uarTime.split("\n")[1].replace("§7§o(Last restart happened ", "").replace(" ago)", "");
		int hoursago = 0;
		int minutesago = 0;
		int secondsago = 0;
		if (uarTime.contains("h")) {
			hoursago = Integer.parseInt(uarTime.split("h")[0]);
		}
		if (uarTime.contains("m")) {
			if (uarTime.contains("h")) {
				minutesago = Integer.parseInt(uarTime.split("h ")[1].split("m")[0]);
			} else {
				minutesago = Integer.parseInt(uarTime.split("m")[0]);
			}
		}
		if (uarTime.contains("s")) {
			if (uarTime.contains("m")) {
				secondsago = Integer.parseInt(uarTime.split("m and ")[1].split("s")[0]);
			} else if (uarTime.contains("h")) {
				secondsago = Integer.parseInt(uarTime.split("h and ")[1].split("s")[0]);
			} else {
				secondsago = Integer.parseInt(uarTime.split("s")[0]);
			}
		}
		now = now.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds); // subtract the time we added
		now = now.minusHours(hoursago).minusMinutes(minutesago).minusSeconds(secondsago); // subtract the time ago

		returnv += "\n(Last restart happened <t:" + now.toEpochSecond(ZoneOffset.UTC) + ":R>)";
		return returnv;
	}

	@Override
	public String getName() {
		return "uar"; // you can change this back to restarttime, but I think
		// this is better because if you type this in game that's how you get the time to restart
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