package com.finni.discordmodbot.event;

import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.finni.discordmodbot.event.enums.ModerationType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;


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

	 	MessageBuilder a =  new MessageBuilder().setAllowedMentions( allowedMentions );
		EmbedBuilder b = new EmbedBuilder();

		switch( type ) {
			case BAN:
				b.setTitle( "Ban" );
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#fd6147") );
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case TEMPBAN:
				b.setTitle("Temporary Ban");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#f98977") );
				b.addField("Duration", duration);
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case IPBAN:
				b.setTitle("IP-Ban");
				b.setColor( Color.decode("#d93d3d") );
				b.addField("IP-Address", affectedPlayer);
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case TEMPIPBAN:
				b.setTitle("Temporary IP-Ban");
				b.setColor( Color.decode("#db6c6c") );
				b.addField("IP-Address", affectedPlayer);
				b.addField("Duration", duration);
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case KICK:
				b.setTitle("Kick");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#fdae47") );
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case MUTE:
				b.setTitle("Mute");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#6e6e6e") );
				b.addField("Duration", "Permanently");
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case TEMPMUTE:
				b.setTitle("Temporary Mute");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#8b8a8a") );
				b.addField("Duration", duration);
				b.addField("Reason", reason);
				b.addField("Moderator", moderator);
				break;
			case UNMUTE:
				b.setTitle("Unmute");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#5498b7") );
				break;
			case UNBAN:
				b.setTitle("Unban");
				if(this.affectedPlayer != null) {
					Map<String,String> data = getPlayerData(discordLinkService);
					if (data.get("name") != null) {
						b.addField("MC-name", data.get("name"));
						a.append(data.get("name")+"\n");
					}
					if (data.get("uuid") != null) {
						b.addField("UUID", data.get("uuid"));
						a.append(data.get("uuid")+"\n");
					}
					if(data.get("discordID") != null) {
						b.addField("Discord ID", data.get("discordID"));
					}
				}
				b.setColor( Color.decode("#4798fd") );
				b.addField("Moderator", moderator);
				break;
			case UNBANIP:
				b.setTitle("IP-Unban");
				b.setColor( Color.decode("#47a9fd") );
				b.addField("IP-Address", affectedPlayer);
				b.addField("Moderator", moderator);
			default:
		}
		a.setEmbed( b );
		return a;
	}

	private Map<String, String> getPlayerData(DiscordLinkService discordLinkService) {
		Map<String, String> data = new HashMap<>();
		OfflinePlayer affectedPLayer = Bukkit.getOfflinePlayer(this.affectedPlayer);
		if (affectedPLayer.getName() != null) {
			data.put( "name", affectedPLayer.getName());
		}

		if (affectedPLayer.getUniqueId() != null) {
			data.put("uuid", affectedPLayer.getUniqueId().toString());
			//todo: fix
			/*String discordId = discordLinkService.getDiscordId(affectedPLayer.getUniqueId());
			if (discordId != null)
				data.put("discordID", discordId);*/
		}
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