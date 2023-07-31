package com.finni.discordmodbot.listener;

import com.finni.discordmodbot.DiscordModBot;

import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.DiscordApi;
import com.finni.discordmodbot.appender.ModerationLogListener;
import com.finni.discordmodbot.event.ModerationLogEvent;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class EssentialsDiscordModlogs implements Listener
{
	private static final Logger rootLogger = (Logger) LogManager.getRootLogger();

	static EssentialsDiscordModlogs instance;

	private final DiscordApi discordapi;
	private  final DiscordLinkService discordLinkService;

	private final String logChannelID;

	private final Map<String, Boolean> moderationLogSettings;

	public EssentialsDiscordModlogs() {
		this.discordapi = DiscordModBot.getInstance().getDiscordAPI();
		this.discordLinkService = Bukkit.getServicesManager().load( DiscordLinkService.class );
		ModerationLogListener logAppender = new ModerationLogListener();
		rootLogger.addAppender( logAppender );

		moderationLogSettings = DiscordModBot.getInstance().getMcModBotConfig()
				.getMapList("moderationLogSettings").stream()
				.flatMap(e->e.entrySet().stream())
				.map(entry -> ((Map.Entry<String, Boolean>)entry))
				.collect(Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ));

		this.logChannelID = DiscordModBot.getInstance().getMcModBotConfig().getString( "logChannelID" );

	}

	@EventHandler()
	public void onChat( ModerationLogEvent event) {
        switch (event.getType()) {
            case KICK -> { if (!moderationLogSettings.get("kick")) event.setCancelled(true); }
            case BAN -> { if (!moderationLogSettings.get("ban")) event.setCancelled(true); }
            case TEMPBAN -> { if (!moderationLogSettings.get("tempBan")) event.setCancelled(true); }
            case TEMPIPBAN -> { if (!moderationLogSettings.get("tempIPBan")) event.setCancelled(true); }
            case IPBAN -> { if (!moderationLogSettings.get("ipBan")) event.setCancelled(true); }
            case MUTE -> { if (!moderationLogSettings.get("mute")) event.setCancelled(true); }
            case TEMPMUTE -> { if (!moderationLogSettings.get("tempMute")) event.setCancelled(true); }
            case UNBAN -> { if (!moderationLogSettings.get("unban")) event.setCancelled(true); }
            case UNMUTE -> { if (!moderationLogSettings.get("unmute")) event.setCancelled(true); }
            case UNBANIP -> { if (!moderationLogSettings.get("ipUnban")) event.setCancelled(true); }
        }

		if( !event.isCancelled() ) {
			MessageBuilder message = event.toDiscordEmbed(discordapi, discordLinkService);
			Optional<TextChannel> channel = discordapi.getChannelById( logChannelID ).flatMap( Channel::asTextChannel );
			channel.ifPresent( message::send );
		}
	}

	public static void createNewInstance() {
		instance = new EssentialsDiscordModlogs();
	}

	public static EssentialsDiscordModlogs getInstance() {
		return instance;
	}

}