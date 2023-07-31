package com.finni.discordmodbot.appender;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.finni.discordmodbot.DiscordModBot;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.Bukkit;

import com.finni.discordmodbot.event.ModerationLogEvent;
import com.finni.discordmodbot.event.enums.ModerationType;

public class ModerationLogListener extends AbstractAppender {

	Map<String, Boolean> moderationLogSettings;
	Map<ModerationType, Pattern> patterns;

	public ModerationLogListener() {
		// do your calculations here before starting to capture
		super("ModerationLogListener", null, null, false, null);
		start();

		this.moderationLogSettings = DiscordModBot.getInstance().getMcModBotConfig()
				.getMapList("moderationLogSettings").stream()
				.flatMap(e->e.entrySet().stream())
				.map(entry -> ((Map.Entry<String, Boolean>)entry))
				.collect(Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ));

		patterns = createPatternList();
	}

	@Override
	public void append(LogEvent event) {

		if (event.getLoggerName().equals( "Essentials" ) || event.getMessage().getFormat().replaceAll( "\u001B\\[\\d\\d?m", "" ).matches( "^Player (\\S*) unmuted.*$" )
				|| event.getMessage().getFormat().matches( "(^Banned IP \\S*:(.*)$)|(\\S*: Banned IP \\S*:.*)" )) {
			String message = event.getMessage().getFormat().replaceAll( "\u001B\\[\\d\\d?m", "" );

			createModerationLogEvent( message ).ifPresent(modEvent -> Bukkit.getPluginManager().callEvent(modEvent));
		}
	}

	private boolean getModerationLogSetting(String key) {
		return moderationLogSettings.get(key);
	}

	private HashMap<ModerationType, Matcher> createMatcherList(String string) {
		return patterns.entrySet().stream().collect(Collectors.toMap( Map.Entry::getKey, entry -> entry.getValue().matcher(string), (prev, next) -> next, HashMap::new));
	}
	private HashMap<ModerationType, Pattern> createPatternList() {
		HashMap<ModerationType, Pattern> matchers = new HashMap<>();

		if (getModerationLogSetting("kick")) {
			matchers.put(ModerationType.KICK, Pattern.compile("Player (\\S*) kicked (\\S*) for (.*)"));
		}
		if (getModerationLogSetting("ban")) {
			matchers.put(ModerationType.BAN, Pattern.compile("Player (\\S*) banned (\\S*) for: You have been banned:(\\n.*)"));
		}
		if (getModerationLogSetting("tempBan")) {
			matchers.put(ModerationType.TEMPBAN, Pattern.compile("Player (.*) temporarily banned (\\S*) for (.*):(.*)$"));
		}
		if (getModerationLogSetting("tempIPBan")) {
			matchers.put(ModerationType.TEMPIPBAN, Pattern.compile("Player (.*) temporarily banned IP address (\\S*) for (.*):(.*)$"));
		}
		if (getModerationLogSetting("ipBan")) {
			matchers.put(ModerationType.IPBAN1, Pattern.compile("^Banned IP (\\S*):(.*)$"));
		}
		if (getModerationLogSetting("ipBan")) {
			matchers.put(ModerationType.IPBAN2, Pattern.compile("\\[(\\S*): Banned IP (\\S*): (.*)\\]"));
		}
		if (getModerationLogSetting("ipBan")) {
			matchers.put(ModerationType.IPBAN3, Pattern.compile("Player (\\S*) banned IP address (\\S*) for:(.*)$"));
		}
		if (getModerationLogSetting("mute")) {
			matchers.put(ModerationType.MUTE, Pattern.compile("(\\S*) has muted player (\\S*)\\.( ?Reason: (.*))?$"));
		}
		if (getModerationLogSetting("tempMute")) {
			matchers.put(ModerationType.TEMPMUTE, Pattern.compile("(\\S*) has muted player (\\S*) for (\\d+ \\S*).(Reason: (.*))?$"));
		}
		if (getModerationLogSetting("unban")) {
			matchers.put(ModerationType.UNBAN, Pattern.compile("^Player (\\S*) unbanned (\\S*)$"));
		}
		if (getModerationLogSetting("unmute")) {
			matchers.put(ModerationType.UNMUTE, Pattern.compile("^Player (\\S*) unmuted.*$"));
		}
		if (getModerationLogSetting("ipUnban")) {
			matchers.put(ModerationType.UNBANIP, Pattern.compile("^Player (\\S*) unbanned IP: (\\S*$)"));
		}

		return matchers;
	}

	private Optional<ModerationLogEvent> createModerationLogEvent(String message) {

		HashMap<ModerationType, Matcher> matchers = this.createMatcherList(message);

		return matchers.entrySet().stream().map( entry -> {
			ModerationType type = entry.getKey();
			Matcher matcher = entry.getValue();

			if( matcher.find() )
				return createEvent( type, matcher );
			return null;
		} ).filter( Objects::nonNull ).findFirst();
	}

	private ModerationLogEvent createEvent(ModerationType type, Matcher matcher) {

		ModerationLogEvent event = null;
		switch( type ) {
			case BAN -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.BAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ).replaceAll( "\n", "" ) );
				event.setDuration( "permanently" );
			}case TEMPBAN -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				event.setReason( matcher.group( 4 ) );
			} case TEMPIPBAN -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPIPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				if( matcher.groupCount() > 3 && matcher.group( 4 ) != null ) {
					event.setReason( matcher.group( 4 ) );
				} else {
					event.setReason( "(none given)" );
				}
			} case IPBAN1 -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.IPBAN );
				event.setModerator( "(Unknown, Probably The Console though)" );
				event.setAffectedPlayer( matcher.group( 1 ) );
				event.setReason( matcher.group( 2 ) );
				event.setDuration( "permanently" );
			} case IPBAN2, IPBAN3 -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.IPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ) );
				event.setDuration( "permanently" );
			} case KICK -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.KICK );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ) );
			} case MUTE -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.MUTE );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( "permanently" );
				if( matcher.groupCount() > 3 && matcher.group( 4 ) != null ) {
					event.setReason( matcher.group( 4 ) );
				} else {
					event.setReason( "(none given)" );
				}
			} case TEMPMUTE -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPMUTE );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				if( matcher.groupCount() > 4 && ( matcher.group( 5 ) != null ) ) {
					event.setReason( matcher.group( 5 ) );
				} else {
					event.setReason( "(none)" );
				}
			} case UNMUTE -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNMUTE );
				event.setModerator( "(Unknown)" );
				event.setAffectedPlayer( matcher.group( 1 ) );
			} case UNBAN -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
			} case UNBANIP -> {
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNBANIP );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
			}
		}

		return event;
	}
}
