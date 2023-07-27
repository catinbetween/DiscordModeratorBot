package com.finni.discordmodbot.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.finni.discordmodbot.event.enums.ModerationType;


public class ModerationLogEvent extends Event implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private String moderator = "??";

	private String affectedPlayer = "";

	private String duration  = "";

	private String ip = "";

	private String reason = "";

	private ModerationType type;

	private boolean isCancelled;


	public ModerationLogEvent() {

	}

	public String toMessage(){
		switch( type ) {
			case BAN:
				return "Player " + moderator + " banned Player " + affectedPlayer + " with reason: " + reason;
			case TEMPBAN:
				return "Player " + moderator + " temporarily banned Player " + affectedPlayer + " for " + duration + " with reason: " + reason;
			case IPBAN:
				return "Player " + moderator + " permanently banned the IP " + affectedPlayer + " with reason: " + reason;
			case TEMPIPBAN:
				return "Player " + moderator + " temporarily banned the IP " + affectedPlayer + " for " + duration + " with reason: " + reason;
			case KICK:
				return "Player " + moderator + " kicked Player " + affectedPlayer + " with reason: " + reason;
			case MUTE:
				return "Player " + moderator + " permanently muted Player " + affectedPlayer + " with reason: " + reason;
			case TEMPMUTE:
				return "Player " + moderator + " temporarily muted Player " + affectedPlayer + " for " + duration + " with reason: " + reason;
			case UNMUTE:
				return "Player " + affectedPlayer + "has been unmuted.";
			case UNBAN:
				return "Player " + moderator + " has unbanned Player " + affectedPlayer + ".";
			case UNBANIP:
				return "Player " + moderator + " has unbanned IP " + affectedPlayer + ".";
			default:
				return "";
		}
	}

	public void setModerator (String moderator) {
		this.moderator = moderator;
	}
	public String getModerator(){
		return moderator;
	}

	public void setAffectedPlayer (String affectedPlayer) {
		this.affectedPlayer = affectedPlayer;
	}
	public String getAffectedPlayer(){
		return affectedPlayer;
	}

	public void setDuration (String duration) {
		this.duration = duration;
	}
	public String getDuration(){
		return duration;
	}

	public void setIp (String ip) {
		this.ip = ip;
	}
	public String getIp(){
		return ip;
	}

	public void setReason (String reason) {
		this.reason = reason;
	}
	public String getReason(){
		return reason;
	}

	public void setType (ModerationType type) {
		this.type = type;
	}
	public ModerationType getType(){
		return type;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}


	@Override
	public boolean isCancelled()
	{
		return this.isCancelled;
	}


	@Override
	public void setCancelled( boolean cancel )
	{
		this.isCancelled = cancel;
	}
}