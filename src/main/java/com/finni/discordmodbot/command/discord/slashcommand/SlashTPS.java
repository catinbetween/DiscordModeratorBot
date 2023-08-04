package com.finni.discordmodbot.command.discord.slashcommand;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import com.finni.discordmodbot.DiscordModBot;
import com.finni.discordmodbot.command.CustomConsoleSender;
import io.papermc.lib.PaperLib;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.Map;


public class SlashTPS implements InteractionCommand {

	private final DiscordModBot plugin;

	public SlashTPS(){
		this.plugin = DiscordModBot.getInstance();
	}

	@Override
	public void onCommand(InteractionEvent event) {
		// This could work, but i'd think its better to show the output of the /tps command.
		/* double[] TPS = Bukkit.getTPS();

		event.reply("TPS (1m, 5m, 15m): " + TPS[0] + ", " + TPS[1] + ", " + TPS[2]);

		 */
		CustomConsoleSender wrapped = new CustomConsoleSender(Bukkit.getServer().getConsoleSender());
		Bukkit.dispatchCommand(wrapped, "tps");
		Bukkit.getScheduler().runTaskLater(plugin, () -> event.reply(parseStringFromTPS(wrapped.getMessage())), 20L);

	}
	private String parseStringFromTPS(String message){
		// going to .replace() all the garbage out of the message

		String finalNewmsg = "";
		message = message.replace("§8[§e§l⚡§8]§7  ","§8[§e§l⚡§8]§7 ").replace("§8[§e§l⚡§8]§7 ","")
				//.replace("\n\n","\n")
				.replace("95%ile","`95th %ile`")
				.replace("*","")
				.replace("5s","`5s`")
				.replace("10s","`10s`")
				.replace("1m","`1m`")
				.replace("5m","`5m`")
				.replace("15m","`15m`")
				.replace("min","`min`")
				.replace("med","`med`")
				//.replace("95th %ile","`95th %ile`")
				.replace("max","`max`")
				.replace("CPU","__**CPU**__")
				.replace("Tick","__**Tick**__")
				.replace("TPS","__**TPS**__");


		for (String line : message.split("\n")) {
			// Bukkit.getLogger().info("DCMB: Line is '" + line + "'");
			if(line.startsWith("§a")) {
				line = line.replace("§a","`")
						.replace("§7","`")
						.replace("§c","`")
						.replace("§8","`")
						.replace("§e","`");
				if (!line.strip().endsWith(")") || !line.strip().endsWith(":")) {
					line = line + "`";
				}
			}
			finalNewmsg += line + "\n";
		}
		return finalNewmsg.replace(")`",")");
	}
	/*
[⚡] TPS from last 5s, 10s, 1m, 5m, 15m:
[⚡]  20.0, 20.0, 20.0,20.0, *20.0
[⚡]
[⚡] Tick durations (min/med/95%ile/max ms) from last 10s, 1m:
[⚡]  1.3/2.1/3.2/8.4;  1.3/3.8/15.4/205.5
[⚡]
[⚡] CPU usage from last 10s, 1m, 15m:
[⚡]  28%, 40%, 40%  (system)
[⚡]  1%, 8%, 8%  (process)*/

	/*
TPS from last 5s, 10s, 1m, 5m, 15m:
20.0, 20.0, 20.0,20.0, *20.0

Tick durations (min/med/95%ile/max ms) from last 10s, 1m:
1.3/2.1/3.2/8.4;  1.3/3.8/15.4/205.5

CPU usage from last 10s, 1m, 15m:
28%, 40%, 40%  (system)
1%, 8%, 8%  (process)*/
	@Override
	public String getName() {
		return "tps";
	}

	@Override
	public String getDescription() {
		return "Get the server's current TPS.";
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

/*
if (line.contains("system") || line.contains("process")){
				String regex = "\\b\\d+(?:\\.\\d+)?\\b";

				line = line.replace("  "," ").strip();
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(line);
				// Use a StringBuilder to construct the modified string
				StringBuilder sb = new StringBuilder();
				int lastMatchEnd = 0;
				while (matcher.find()) {
					int matchStart = matcher.start();
					int matchEnd = matcher.end();

					// Append the non-number part before this match
					sb.append(line, lastMatchEnd, matchStart);

					// Append the matched number with backticks added
					sb.append('`').append(line, matchStart, matchEnd).append('`');

					lastMatchEnd = matchEnd;
				}
				int matchStart = matcher.start();
				int matchEnd = matcher.end();

				// Append the non-number part before this match
				sb.append(line, lastMatchEnd, matchStart);

				// Append the matched number with backticks added
				sb.append('`').append(line, matchStart, matchEnd).append('`');

			}
			else if(line.length() - line.replace("/", "").length() == 6) { // check if there are 6 slashes in the line
				String regex = "\\d+(?:\\.\\d+)?(?:/\\d+(?:\\.\\d+)?)*";

				// Compile the regex pattern
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(line);

				// Use a StringBuilder to construct the modified string
				StringBuilder sb = new StringBuilder();
				int lastMatchEnd = 0;

				// Iterate through the matches and add backticks around each number
				while (matcher.find()) {
					int matchStart = matcher.start();
					int matchEnd = matcher.end();

					// Append the non-number part before this match
					sb.append(line, lastMatchEnd, matchStart);

					// Append the matched number with backticks added
					sb.append('`').append(line, matchStart, matchEnd).append('`');

					lastMatchEnd = matchEnd;
				}

				// Append the remaining part after the last match
				sb.append(line.substring(lastMatchEnd));
				line = sb.toString();
			} else if(line.length() - line.replace(",","").length() == 4) { // if there are 4 commas in the line
				String regex = "\\d+(?:\\.\\d+)?";

				// Compile the regex pattern
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(line);

				// Use a StringBuilder to construct the modified string
				StringBuilder sb = new StringBuilder();
				int lastMatchEnd = 0;

				// Iterate through the matches and add backticks around each number
				while (matcher.find()) {
					int matchStart = matcher.start();
					int matchEnd = matcher.end();

					// Append the non-number part before this match
					sb.append(line, lastMatchEnd, matchStart);

					// Append the matched number with backticks added
					sb.append('`').append(line, matchStart, matchEnd).append('`');

					lastMatchEnd = matchEnd;
				}

				// Append the remaining part after the last match
				sb.append(line.substring(lastMatchEnd));

				line = sb.toString();
			}
 */