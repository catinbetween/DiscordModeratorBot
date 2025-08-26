package com.catinbetween.minecraft.discordmodbot.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;

@Log
@Data
@AllArgsConstructor
public class DiscordModerationSettings {
    public boolean kick = false;
    public boolean ban = false;
    public boolean tempBan = false;
    public boolean tempIPBan = false;
    public boolean ipBan = false;
    public boolean mute = false;
    public boolean tempMute = false;
    public boolean unban = false;
    public boolean unmute = false;
    public boolean ipUnban = false;
    public boolean warn = false;
    public boolean unwarn = false;
}
