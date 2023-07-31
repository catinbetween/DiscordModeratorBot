package com.finni.discordmodbot.event;

import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.finni.discordmodbot.event.enums.ModerationType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;


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

	public MessageBuilder toDiscordEmbed(DiscordApi api, DiscordLinkService discordLinkService){

		AllowedMentions allowedMentions = new AllowedMentionsBuilder().addUser( api.getClientId()).build();

	 	MessageBuilder message =  new MessageBuilder().setAllowedMentions( allowedMentions );
		EmbedBuilder embed = new EmbedBuilder();

		switch( type ) {
			case BAN:
				embed.setTitle( "Ban" );
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#fd6147") );
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case TEMPBAN:
				embed.setTitle("Temporary Ban");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#f98977") );
				embed.addField("Duration", duration);
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case IPBAN:
				embed.setTitle("IP-Ban");
				embed.setColor( Color.decode("#d93d3d") );
				embed.addField("IP-Address", affectedPlayer);
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case TEMPIPBAN:
				embed.setTitle("Temporary IP-Ban");
				embed.setColor( Color.decode("#db6c6c") );
				embed.addField("IP-Address", affectedPlayer);
				embed.addField("Duration", duration);
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case KICK:
				embed.setTitle("Kick");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#fdae47") );
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case MUTE:
				embed.setTitle("Mute");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#6e6e6e") );
				embed.addField("Duration", "Permanently");
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case TEMPMUTE:
				embed.setTitle("Temporary Mute");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#8b8a8a") );
				embed.addField("Duration", duration);
				embed.addField("Reason", reason);
				embed.addField("Moderator", moderator);
				break;
			case UNMUTE:
				embed.setTitle("Unmute");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#5498b7") );
				break;
			case UNBAN:
				embed.setTitle("Unban");
				addPlayerIDsAndNames( discordLinkService, message, embed );
				embed.setColor( Color.decode("#4798fd") );
				embed.addField("Moderator", moderator);
				break;
			case UNBANIP:
				embed.setTitle("IP-Unban");
				embed.setColor( Color.decode("#47a9fd") );
				embed.addField("IP-Address", affectedPlayer);
				embed.addField("Moderator", moderator);
			default:
		}
		message.setEmbed( embed );
		return message;
	}


	private void addPlayerIDsAndNames( DiscordLinkService discordLinkService, MessageBuilder message, EmbedBuilder embed )
	{
		if(this.affectedPlayer != null) {
			Map<String,String> data = getPlayerData(discordLinkService);
			if (data.get("name") != null) {
				embed.addField("MC-name", data.get("name"));
				message.append(data.get("name")+"\n");
			}
			if (data.get("uuid") != null) {
				embed.addField("UUID", data.get("uuid"));
				message.append(data.get("uuid")+"\n");
			}
			if(data.get("discordID") != null) {
				embed.addField("Discord ID", data.get("discordID"));
			}
		}
	}


	private Map<String, String> getPlayerData(DiscordLinkService discordLinkService) {
		Map<String, String> data = new HashMap<>();
		OfflinePlayer affectedPLayer = Bukkit.getOfflinePlayer(this.affectedPlayer);
		if (affectedPLayer.getName() != null) {
			data.put( "name", affectedPLayer.getName());
		}

		data.put("uuid", affectedPLayer.getUniqueId().toString());
		//todo: fix
		/*String discordId = discordLinkService.getDiscordId(affectedPLayer.getUniqueId());
		if (discordId != null)
			data.put("discordID", discordId);*/

		return data;
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
	public @NotNull HandlerList getHandlers() {
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