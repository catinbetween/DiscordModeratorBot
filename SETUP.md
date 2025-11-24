# DiscordModeratorBot Setup Guide

This guide will walk you through setting up the DiscordModeratorBot for your Minecraft server.

## Prerequisites Checklist

- [ ] Minecraft 1.20.1 Fabric server
- [ ] Fabric Loader installed
- [ ] Fabric API installed  
- [ ] Simple Discord Link mod (≥3.3.2) installed and configured
- [ ] Discord server with admin permissions
- [ ] Basic understanding of JSON configuration files

## Step-by-Step Setup

### 1. Download Required Mods

1. **DiscordModeratorBot**: Download from GitHub releases
2. **Simple Discord Link**: Download from [GitHub](https://github.com/hypherionmc/Simple-Discord-Link)
3. **Fabric API**: Download from [Modrinth](https://modrinth.com/mod/fabric-api)

### 2. Install Mods

1. Place all downloaded `.jar` files in your server's `/mods/` folder
2. Start your server once to generate initial configuration files
3. Stop the server

### 3. Create Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application" and give it a name
3. Go to the "Bot" section
4. Click "Add Bot"
5. Copy the bot token (keep this secure!)
6. Enable "Server Members Intent" and "Message Content Intent"

### 4. Invite Bot to Server

1. Go to the "OAuth2" → "URL Generator" section
2. Select scopes:
   - [ ] `bot`
   - [ ] `applications.commands`
3. Select permissions:
   - [ ] Read Messages/View Channels
   - [ ] Send Messages
   - [ ] Use Slash Commands
   - [ ] Read Message History
   - [ ] Embed Links
4. Copy the generated URL and open it to invite the bot

### 5. Configure Simple Discord Link

Edit `/config/simple-discord-link/simple-discord-link.toml`:

```toml
[general]
enabled = true

[botConfig]
botToken = "YOUR_BOT_TOKEN_HERE"

[guildConfig]
ownerID = "YOUR_DISCORD_USER_ID"
guildID = "YOUR_DISCORD_SERVER_ID"
```

### 6. Get Required IDs

**Discord Server ID (guildID):**
1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click your server name → "Copy Server ID"

**Your User ID (ownerID):**
1. Right-click your username → "Copy User ID"

**Role IDs (allowedRoles):**
1. Go to Server Settings → Roles
2. Right-click a role → "Copy Role ID"

**Channel IDs (optional):**
1. Right-click a channel → "Copy Channel ID"

### 7. Start and Test

1. Start your Minecraft server
2. Check logs for successful initialization:
   ```
   [INFO] Initialized Database and SDLink integration successfully
   ```
3. Test the `/whois` command in Discord

## Configuration Options Explained

### Core Settings
- **`botToken`**: Your Discord bot's token (required)
- **`guildID`**: Your Discord server ID (required)
- **`ownerID`**: Your Discord user ID (required for owner commands)

### Channel Restrictions
- **`commandChannelId`**: Restrict commands to specific channel (empty = all channels)
- **`allowedChannels`**: List of channels where commands work (empty = all channels)

### Role Permissions
- **`allowedRoles`**: List of role IDs that can use commands (empty = everyone)

### Logging
- **`logChannelID`**: Channel for bot logs (optional)
- **`logLevel`**: Log detail level (`INFO`, `DEBUG`, `WARN`, `ERROR`)

## Troubleshooting

### Bot Not Starting
- Check bot token is correct
- Verify Simple Discord Link is working
- Check server logs for errors

### Commands Not Working
- Ensure bot has proper permissions in Discord
- Check `allowedRoles` configuration
- Verify user has required roles

### No Identity Data
- Ensure Simple Discord Link is properly configured
- Players must link their accounts first
- Check database file permissions

### Permission Errors
```
/dmb reload  # Requires 'discordmodbot.admin' permission or op level 4
```

## Next Steps

Once configured:
1. Have players link their accounts using Simple Discord Link
2. Test `/whois` commands with different parameters
3. Set up logging channels for monitoring
4. Configure role restrictions as needed
5. Monitor the database file growth over time