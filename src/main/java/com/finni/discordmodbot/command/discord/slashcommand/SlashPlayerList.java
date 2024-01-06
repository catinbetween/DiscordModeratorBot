package com.finni.discordmodbot.command.discord.slashcommand;

import java.util.*;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import net.essentialsx.api.v2.services.discord.*;
import org.bukkit.Bukkit;


public class SlashPlayerList implements InteractionCommand {


	public SlashPlayerList(){
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

		// remaking the map in the right order
		String returnv = "";
		if((Bukkit.getOnlinePlayers().size()) < (Bukkit.getMaxPlayers()) && (Bukkit.getOnlinePlayers().size() == Bukkit.getMaxPlayers()-2 || Bukkit.getOnlinePlayers().size() == Bukkit.getMaxPlayers()-1) ){
			returnv = "\uD83D\uDFE1 "; // yellow circle unicode
		}
		else if ((Bukkit.getOnlinePlayers().size()) < (Bukkit.getMaxPlayers())){
			returnv = "\uD83D\uDFE2 "; // green circle unicode
		}
		else if ((Bukkit.getOnlinePlayers().size()) == (Bukkit.getMaxPlayers())){
			returnv = "\uD83D\uDD34 "; // red circle unicode
		}
		else if ((Bukkit.getOnlinePlayers().size()) > (Bukkit.getMaxPlayers())){
			returnv = "\uD83D\uDD35 "; // blue circle unicode
		}

		returnv += summary.replace("are ","are `")
				.replace(" out","` out")
				.replace("maximum ","maximum `")
				.replace(" players","` players") + "\n\n";

		List<Map.Entry<String, List<User>>> users2 = new ArrayList<Map.Entry<String, List<User>>>(5);
		for(int i=0; i<6; i++){ users2.add(null); }
		for(Map.Entry<String, List<User>> e : users.entrySet()) {
			String key = e.getKey();
			List<User> values = e.getValue();
			int priority = 6;
			if (key.equals("owner")){ key = "Owners"; priority = 0;}
			else if (key.equals("admin")){ key = "Admins"; priority = 1;}
			else if (key.equals("head-mod")){ key = "Head Mods"; priority = 2;}
			else if (key.equals("mod")){ key = "Mods"; priority = 3;}
			else if (key.equals("jr-mod")){ key = "Jr Mods"; priority = 4;}
			else if (key.equals("default")){ key = "Players"; priority = 5;}

			users2.add(priority, new AbstractMap.SimpleEntry<>(key, values));
		}
		for(Map.Entry<String, List<User>> e : users2) {
			if(e == null || e.getValue().size() == 0){ continue;}
			returnv += "__" + e.getKey() + "__: ";
			e.getValue().sort(new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {

					if (o1.isAfk() == o2.isAfk() && !o1.isAfk()) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					} else if (o1.isAfk() == o2.isAfk() && o1.isAfk()) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					} else if (o1.isAfk() && !o2.isAfk()) {
						return 1;
					} else if (!o1.isAfk() && o2.isAfk()) {
						return -1;
					}
					return 0;
				}
			});
			for(User player : e.getValue()){
				returnv += "`" + player.getName().replace("[AFK]", "[AFK] ") + ", ";
			}
			returnv = returnv.substring(0, returnv.length() - 2) + "`\n";
		}
		event.reply(returnv);
	}

	@Override
	public String getName() {
		return "playerlist";
	}

	@Override
	public String getDescription() {
		return "Get the current list of the current online players.";
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