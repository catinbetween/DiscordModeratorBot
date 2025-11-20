package com.catinbetween.minecraft.discordmodbot.command;

import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.command.ServerCommandSource;

@Log4j2
public class MeowCommand  {
    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        // Check if SDLink is ready, enabled and can send messages
        if (!BotController.INSTANCE.isBotReady() || source.getPlayer() == null)
            return 0;

        // Construct the author
        DiscordAuthor author = DiscordAuthor.of(source.getDisplayName().getString(), source.getPlayer().getUuidAsString(), String.valueOf(source.getPlayer().getName()));
        log.info("meow {}, {}, {}", source.getDisplayName().getString(), source.getPlayer().getUuidAsString(), source.getPlayer().getName());

        // Construct the message
        DiscordMessage message1 = new DiscordMessageBuilder(MessageType.CUSTOM)
                .message("meow " )
                .author(author)
                .build();

        // Send the message
        message1.sendMessage();
        return 1;
    }
}
