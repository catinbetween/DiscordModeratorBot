package com.finni.discordmodbot.command.discord.slashcommand;

import java.util.List;
import java.util.Map;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import net.essentialsx.api.v2.services.discord.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;


public class SlashPlayerList implements InteractionCommand {


	public SlashPlayerList(){
	}

	private boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished")) {
			if (meta.asBoolean()) return true;
		}
		return false;
	}

	@Override
	public void onCommand( InteractionEvent event) {
		IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

		if(essentials == null) {
			Bukkit.getLogger().severe("Essentials Plugin is null!");
			return;
		}

		String summary = PlayerList.listSummary(essentials,null, false);
		Map<String, List<User>> users = PlayerList.getPlayerLists(essentials, null, false);
		List<String> a = users.entrySet().stream().map(e -> {
			String key = e.getKey();
			List<User> values = e.getValue();
			return (key.equals("default") ? "Players" : key) + ":\n" + PlayerList.listUsers(essentials, values, ",") + "\n\n";
		}).toList();
		String summaryList = a.stream().reduce("\n", String::concat);

		event.reply(summary + "\n\n" + summaryList);
	}

	@Override
	public String getName() {
		return "playerlist";
	}

	@Override
	public String getDescription() {
		return "Get current player list";
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