package com.finni.discordmodbot.listener;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.DiscordApi;

import net.essentialsx.api.v2.events.UserKickEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;


public class EssentialsDiscordHook implements Listener
{

	static EssentialsDiscordHook instance;
	private final YamlConfiguration config;

	private final DiscordApi discordapi;

	private  final DiscordService discordService;

	private String logChannelID;
	private boolean kickEventListeningActivated;

	public EssentialsDiscordHook(DiscordApi discordapi, YamlConfiguration config) {
		this.discordapi = discordapi;
		this.config = config;
		this.discordService = Bukkit.getServicesManager().load( DiscordService.class );

		if(this.discordService != null)
			this.discordService.getInviteUrl();

		this.kickEventListeningActivated = this.config.getBoolean( "kickEventListeningActivated", false );
		this.logChannelID = this.config.getString( "logChannelID" );

	}

	@EventHandler()
	public void onChat( UserKickEvent event) {
		String kicker = event.getKicked() != null ? event.getKicked().getName() : "ProbablyTheConsole";
		String kicked = event.getKicker() != null ? event.getKicker().getName() : "???";
		String reason = event.getReason() != null ? event.getReason() : "(no reason given)";

		if( kickEventListeningActivated )
			discordapi.getChannelById( logChannelID).get().asTextChannel().get().sendMessage( "Player " + kicker + " kicked " + kicked + " with reason: " + reason );
	}

	public static void createNewInstance(DiscordApi discordapi,YamlConfiguration yamlConfiguration ) {
		instance = new EssentialsDiscordHook(discordapi, yamlConfiguration);
	}

	public static EssentialsDiscordHook getInstance() {
		return instance;
	}

}