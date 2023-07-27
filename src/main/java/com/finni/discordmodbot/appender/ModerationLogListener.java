package com.finni.discordmodbot.appender;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.Bukkit;

import com.finni.discordmodbot.event.ModerationLogEvent;
import com.finni.discordmodbot.event.enums.ModerationType;

public class ModerationLogListener extends AbstractAppender {

	public ModerationLogListener() {
		// do your calculations here before starting to capture
		super("ModerationLogListener", null, null);
		start();
	}

	@Override
	public void append(LogEvent event) {
		LogEvent log = event.toImmutable();

		if (event.getLoggerName().equals( "Essentials" ) || event.getMessage().getFormat().replaceAll( "\u001B\\[\\d\\d?m", "" ).matches( "^Player (\\S*) unmuted.*$" )
				|| event.getMessage().getFormat().matches( "(^Banned IP \\S*:(.*)$)|(\\S*: Banned IP \\S*:.*)" )) {
			String message = event.getMessage().getFormat().replaceAll( "\u001B\\[\\d\\d?m", "" );

			if (matcherList( message ).entrySet().stream().anyMatch( entry -> {
				Matcher matcher = entry.getValue();
				return matcher.find();
			} )) {
				ModerationLogEvent moderationLogEvent = getModerationLogEvent( matcherList( message ) );
				Bukkit.getPluginManager().callEvent(moderationLogEvent);
			}
		}

	}

	private HashMap<ModerationType, Matcher> matcherList(String string) {
		HashMap<ModerationType, Matcher> matchers = new HashMap<>();
		matchers.put( ModerationType.TEMPBAN, Pattern.compile( "Player (.*) temporarily banned (\\S*) for (.*):(.*)$" )
				.matcher( string ));
		matchers.put( ModerationType.TEMPIPBAN, Pattern.compile( "Player (.*) temporarily banned IP address (\\S*) for (.*):(.*)$" )
				.matcher( string ));
		matchers.put( ModerationType.BAN, Pattern.compile( "Player (\\S*) banned (\\S*) for: You have been banned:(\\n.*)" )
				.matcher( string ));
		matchers.put( ModerationType.IPBAN1, Pattern.compile( "^Banned IP (\\S*):(.*)$" )
				.matcher( string ));
		matchers.put( ModerationType.IPBAN2, Pattern.compile( "\\[(\\S*): Banned IP (\\S*): (.*)\\]" )
				.matcher( string ));
		matchers.put( ModerationType.IPBAN3, Pattern.compile( "Player (\\S*) banned IP address (\\S*) for:(.*)$" )
				.matcher( string ));
		matchers.put( ModerationType.KICK, Pattern.compile( "Player (\\S*) kicked (\\S*) for (.*)" )
				.matcher( string ));
		matchers.put( ModerationType.MUTE, Pattern.compile( "(\\S*) has muted player (\\S*)\\.( ?Reason: (.*))?$" )
				.matcher( string ));
		matchers.put( ModerationType.TEMPMUTE, Pattern.compile( "(\\S*) has muted player (\\S*) for (\\d+ \\S*).(Reason: (.*))?$" )
				.matcher( string ));
		matchers.put( ModerationType.UNMUTE, Pattern.compile( "^Player (\\S*) unmuted.*$" )
				.matcher( string ));
		matchers.put( ModerationType.UNBAN, Pattern.compile( "^Player (\\S*) unbanned (\\S*)$" )
				.matcher( string ));
		matchers.put( ModerationType.UNBANIP, Pattern.compile( "^Player (\\S*) unbanned IP: (\\S*$)" )
				.matcher( string ));

		return matchers;
	}

	private ModerationLogEvent getModerationLogEvent(HashMap<ModerationType, Matcher> matchers)
	{

			Optional<ModerationLogEvent> opt = matchers.entrySet().stream().map( entry -> {
			ModerationType type = entry.getKey();
			Matcher matcher = entry.getValue();

			if( matcher.find() )
				return createEvent( type, matcher );
			return null;
		} ).filter( Objects::nonNull ).findFirst();

		return opt.orElse( null );
	}

	private ModerationLogEvent createEvent(ModerationType type, Matcher matcher) {

		ModerationLogEvent event = null;
		switch( type )
		{
			case BAN:
				event = new ModerationLogEvent();
				event.setType( ModerationType.BAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ).replaceAll( "\n", "" ) );
				event.setDuration( "permanently" );
				break;

			case TEMPBAN:
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				event.setReason( matcher.group( 4 ) );
				break;

			case TEMPIPBAN:
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPIPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				if( matcher.groupCount() > 3 && matcher.group(4) != null)
				{
					event.setReason( matcher.group( 4 ) );
				}
				else
				{
					event.setReason( "(none given)" );
				}
				break;

			case IPBAN1:
				event = new ModerationLogEvent();
				event.setType( ModerationType.IPBAN );
				event.setModerator( "(Unknown, Probably The Console though)" );
				event.setAffectedPlayer( matcher.group( 1 ) );
				event.setReason( matcher.group( 2 ) );
				event.setDuration( "permanently" );
				break;

			case IPBAN2:
				String string= "";
			case IPBAN3:
				event = new ModerationLogEvent();
				event.setType( ModerationType.IPBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ) );
				event.setDuration( "permanently" );
				break;

			case KICK:
				event = new ModerationLogEvent();
				event.setType( ModerationType.KICK );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setReason( matcher.group( 3 ) );
				break;

			case MUTE:
				event = new ModerationLogEvent();
				event.setType( ModerationType.MUTE );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( "permanently" );
				if( matcher.groupCount() > 3 && matcher.group(4) != null)
				{
					event.setReason( matcher.group( 4 ) );
				}
				else
				{
					event.setReason( "(none given)" );
				}
				break;

			case TEMPMUTE:
				event = new ModerationLogEvent();
				event.setType( ModerationType.TEMPMUTE );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				event.setDuration( matcher.group( 3 ) );
				if( matcher.groupCount() > 4 && (matcher.group(5) != null) )
				{
					event.setReason( matcher.group( 5 ) );
				}
				else
				{
					event.setReason( "(none)" );
				}
				break;

			case UNMUTE:
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNMUTE );
				event.setModerator( "(Unknown)" );
				event.setAffectedPlayer( matcher.group( 1 ) );
				break;

			case UNBAN:
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNBAN );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				break;

			case UNBANIP:
				event = new ModerationLogEvent();
				event.setType( ModerationType.UNBANIP );
				event.setModerator( matcher.group( 1 ) );
				event.setAffectedPlayer( matcher.group( 2 ) );
				break;
		}

		return event;
	}
}
