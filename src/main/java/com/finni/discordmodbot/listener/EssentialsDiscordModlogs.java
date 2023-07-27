package com.finni.discordmodbot.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.DiscordApi;
import com.finni.discordmodbot.appender.ModerationLogListener;
import com.finni.discordmodbot.event.ModerationLogEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;


public class EssentialsDiscordModlogs implements Listener
{
	private static final Logger rootLogger = (Logger) LogManager.getRootLogger();

	static EssentialsDiscordModlogs instance;
	private final YamlConfiguration config;

	private final DiscordApi discordapi;

	private  final DiscordService discordService;

	private String logChannelID;
	private boolean kickEventListeningActivated;
	private boolean banEventListeningActivated;
	private boolean tempBanEventListeningActivated;
	private boolean tempIPBanEventListeningActivated;
	private boolean IPBanEventListeningActivated;
	private boolean muteEventListeningActivated;
	private boolean tempMuteEventListeningActivated;
	private boolean unbanEventListeningActivated;
	private boolean unmuteEventListeningActivated;
	private boolean unbanIPEventListeningActivated;

	public EssentialsDiscordModlogs(DiscordApi discordapi, YamlConfiguration config) {
		this.discordapi = discordapi;
		this.config = config;
		this.discordService = Bukkit.getServicesManager().load( DiscordService.class );
		ModerationLogListener logAppender = new ModerationLogListener();
		rootLogger.addAppender( logAppender );

		if(this.discordService != null)
			this.discordService.getInviteUrl();

		this.kickEventListeningActivated = this.config.getBoolean( "kickEventListeningActivated", false );
		this.banEventListeningActivated = this.config.getBoolean( "banEventListeningActivated", false );
		this.tempBanEventListeningActivated = this.config.getBoolean( "tempBanEventListeningActivated", false );
		this.tempIPBanEventListeningActivated = this.config.getBoolean( "tempIPBanEventListeningActivated", false );
		this.IPBanEventListeningActivated = this.config.getBoolean( "IPBanEventListeningActivated", false );
		this.muteEventListeningActivated = this.config.getBoolean( "muteEventListeningActivated", false );
		this.tempMuteEventListeningActivated = this.config.getBoolean( "tempMuteEventListeningActivated", false );
		this.unbanEventListeningActivated = this.config.getBoolean( "unbanEventListeningActivated", false );
		this.unmuteEventListeningActivated = this.config.getBoolean( "unmuteEventListeningActivated", false );
		this.unbanIPEventListeningActivated = this.config.getBoolean( "unbanIPEventListeningActivated", false );
		this.logChannelID = this.config.getString( "logChannelID" );

	}

	@EventHandler()
	public void onChat( ModerationLogEvent event) {
		switch( event.getType() ) {
			case BAN:
				if(!kickEventListeningActivated) event.setCancelled( true ); break;
			case TEMPBAN:
				if(!banEventListeningActivated) event.setCancelled( true ); break;
			case TEMPIPBAN:
				if(!tempBanEventListeningActivated) event.setCancelled( true ); break;
			case IPBAN:
				if(!tempIPBanEventListeningActivated) event.setCancelled( true ); break;
			case KICK:
				if(!IPBanEventListeningActivated) event.setCancelled( true ); break;
			case MUTE:
				if(!muteEventListeningActivated) event.setCancelled( true ); break;
			case TEMPMUTE:
				if(!tempMuteEventListeningActivated) event.setCancelled( true ); break;
			case UNBAN:
				if(!unbanEventListeningActivated) event.setCancelled( true ); break;
			case UNMUTE:
				if(!unmuteEventListeningActivated) event.setCancelled( true ); break;
			case UNBANIP:
				if(!unbanIPEventListeningActivated) event.setCancelled( true ); break;
		}

		if( !event.isCancelled() )
			discordapi.getChannelById( logChannelID).get().asTextChannel().get().sendMessage( event.toMessage() );
	}

	public static void createNewInstance(DiscordApi discordapi,YamlConfiguration yamlConfiguration ) {
		instance = new EssentialsDiscordModlogs(discordapi, yamlConfiguration);
	}

	public static EssentialsDiscordModlogs getInstance() {
		return instance;
	}

}