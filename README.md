# DiscordModeratorBot

**Minecraft Version:** 1.21.10  
**License:** MPL-2.0

A Fabric mod extension for Minecraft servers that integrates with [Simple Discord Link](https://github.com/hypherionmc/Simple-Discord-Link) to enhance its user linking features with a user history, as the original Simple Discord Link only keeps track of the current linked accounts.

Originally developed for [The Clicks](https://www.youtube.com/channel/UCPr3T20IPZ03gU9gZ2BwM5g) community discord server [The Clique](https://discord.com/servers/the-clique-460806595803217932), this mod provides a powerful slash command for server moderators to have an overview over past minecraft accounts, discord accounts and changed usernames. That way, moderators can still identify users even if they changed their Minecraft username or Discord account or if they are no longer linked or part of the Discord server.


## Features

### Advanced Player Lookup (`/whois` command)
- **Multi-parameter search**: Look up players by Minecraft username, UUID, Discord user, or Discord user ID
- **Comprehensive identity tracking**: History of all Minecraft and Discord accounts used by a player
- **Account linking status**: See current, historical, and pending verification states
- **Server membership detection**: Detects when Discord users have left the server
- **SDLink integration**: Fallback to SDLink database for recently linked but not-yet-synced accounts

### Identity Profiles
- **Account History**: Maintains a history of account changes and linking events
- **Profile merging**: Tries to identify if a person already exist and handles username changes or account changes accordingly
- **Real-time synchronization**: Syncs with SDLink events for up-to-date information
- **Database persistence**: Uses SQLite for data storage

### User Experience
- **Visual status indicators**: Color-coded embeds and emoji markers for quick status recognition
- **Account timeline**: Shows when accounts were first seen, last seen, and linking duration
- **Server membership status**: Indicates if a Discord user has left the server
- **Account overview**: Overview over past Minecraft or Discord accounts

## Installation

### Prerequisites
1. **Fabric Loader** for Minecraft 1.20.1
2. **Simple Discord Link** mod (version ≥3.3.2)
3. **Discord Bot** with appropriate permissions

### Dependencies
- Fabric Loader
- Fabric API
- Simple Discord Link (≥3.3.2)
- Required Java libraries (handled automatically)

### Setup Steps

1. **Download and Install**
   ```
   1. Download the latest release from GitHub releases
   2. Place the .jar file in your server's `/mods/` folder
   3. Ensure Simple Discord Link is also installed
   4. Start the server once to generate configuration files
   ```

2. **Configure the Discord Bot**
   - Create a Discord application and bot at [Discord Developer Portal](https://discord.com/developers/applications)
   - Copy the bot token (keep this secret!)
   - Invite the bot to your server with appropriate permissions

3. **Configure the Mod**
   
   Edit `/config/discordmodbot/DiscordModBot_config.json`:
   ```json
   {
     "botToken": "YOUR_BOT_TOKEN_HERE",
     "guildID": "YOUR_DISCORD_SERVER_ID",
     "ownerID": "YOUR_DISCORD_USER_ID",
     "commandChannelId": "OPTIONAL_CHANNEL_ID",
     "logChannelID": "OPTIONAL_LOG_CHANNEL_ID",
     "allowedRoles": ["MODERATOR_ROLE_ID", "ADMIN_ROLE_ID"],
     "allowedChannels": ["ALLOWED_CHANNEL_ID"],
     "logLevel": "INFO"
   }
   ```

4. **Required Bot Permissions**
   - Read Messages/View Channels
   - Send Messages
   - Use Slash Commands
   - Read Message History
   - Embed Links
   - Attach Files (for larger response data)

## Usage

### `/whois` Slash Command

The primary feature of this mod is the comprehensive `/whois` command that provides detailed player identity information.

#### Command Syntax
```
/whois mcusername:<username>     - Search by Minecraft username
/whois mcuuid:<uuid>             - Search by Minecraft UUID  
/whois discorduser:<@user>       - Search by Discord user mention
/whois discorduserid:<id>        - Search by Discord user ID
```

## In-Game Commands
```
/dmb reload  - Reload configuration (requires discordmodbot.admin permission)
```

### Configuration
- Configuration files are automatically created on first run
- Use `/dmb reload` to apply configuration changes without restarting
- Monitor logs for initialization and sync status

## Technical Details

### Database Schema
- Uses SQLite for persistent storage
- Maintains identity profiles with snapshot history
- Tracks linking events and account changes

### Integration Points
- **Simple Discord Link**: Primary account linking system
- **Discord Bot Framework**: JDA-based Discord integration  
- **Fabric Events**: Server lifecycle and player join events
- **Permission Systems**: Configurable role-based access control

## Troubleshooting

**Bot not responding to commands:**
- Verify bot token is correct and bot is online
- Check bot permissions in Discord server
- Ensure guildID matches your Discord server
- Check logs for initialization errors

**Identity data not syncing:**
- Verify Simple Discord Link is working correctly
- Check database file permissions in `/config/discordmodbot/`
- Monitor logs for sync event processing

**Command permission issues:**
- Verify allowedRoles configuration
- Check user roles in Discord
- Ensure bot can see user's roles

### Configuration Examples

**Basic Setup:**
```json
{
  "botToken": "YOUR_BOT_TOKEN",
  "guildID": "YOUR_GUILD_ID", 
  "ownerID": "YOUR_USER_ID",
  "allowedRoles": ["123456789"]
}
```

**Advanced Setup:**
```json
{
  "botToken": "YOUR_BOT_TOKEN",
  "guildID": "YOUR_GUILD_ID",
  "ownerID": "YOUR_USER_ID", 
  "commandChannelId": "987654321",
  "logChannelID": "876543210",
  "allowedRoles": ["123456789", "987654321"],
  "allowedChannels": ["555666777"]
}
```

## Contributing

This mod is open source under the MPL-2.0 license. Contributions, bug reports, and feature requests are welcome through GitHub issues and pull requests.

## Credits

- **Simple Discord Link**: Core account linking functionality
- **JDA**: Discord API integration
- **Fabric**: Minecraft modding framework
- **The Clique Community**: Testing and feedback
- **FinleyOfTheWoods**: Borrowed some manager classes that they had originally written for their own discord mods :] [Check out their github!](https://github.com/FinleyOfTheWoods)
