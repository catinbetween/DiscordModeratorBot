package com.finni.discordmodbot.command;

import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomConsoleSender implements CommandSender {
    private final CommandSender sender;

    private String message;

    private InteractionEvent event;

    public String getMessage(){
        return message;
    }

    public CustomConsoleSender(CommandSender sender, InteractionEvent event) {
        this.sender = sender;
        this.message = "";
        this.event = event;
    }

    @Override
    public void sendMessage(String message) {
        if (!message.isEmpty())
            //event.reply(message);
            this.message += message + "\n";
    }

    @Override
    public void sendMessage(String[] messages) {
        List<String> queue = Arrays.stream(messages).collect(Collectors.toList());
        if (queue.size() > 0)
            sender.sendMessage(queue.toArray(new String[queue.size()]));

    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        this.sender.sendMessage(sender, message);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        this.sender.sendMessage(sender, messages);
    }

    @Override
    public Server getServer() {
        return sender.getServer();
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return sender.spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return sender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return sender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return sender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return sender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return sender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return sender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return sender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        sender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return sender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        sender.setOp(value);
    }
}