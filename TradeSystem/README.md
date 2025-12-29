# TradeSystem Plugin

A Paper 1.21 plugin for player-to-player trading with a double chest interface.

## Features

- **Double Chest GUI**: 54-slot inventory with separate areas for each player
- **Trade Requests**: Use `/trade [player]` to initiate a trade
- **Accept/Decline System**: Both players must accept before trade executes
- **5-Second Countdown**: After both players accept, trade executes after 5 seconds
- **Anti-Theft Protection**: Players can only interact with their own items
- **Anti-Duping**: Items are properly managed and rolled back if trade fails
- **Distance Check**: Players must be within 8 blocks to trade
- **Inventory Space Check**: Ensures players have enough space for traded items

## Installation

1. Build the plugin using Maven: `mvn clean package`
2. Copy the generated JAR file to your server's `plugins/` directory
3. Restart or reload your server

## Usage

### Commands

- `/trade [player]` - Initiate a trade with another player

### Permissions

- `tradesystem.trade` - Allows players to use the trade command (default: true)

## Configuration

No configuration file is needed. All settings are hardcoded:

- Maximum trade distance: 8 blocks
- Trade countdown: 5 seconds
- Permission: `tradesystem.trade`

## Development

### Requirements

- Java 17+
- Paper 1.21 API
- Maven

### Building

```bash
cd TradeSystem
mvn clean package
```

### Project Structure

```
TradeSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tradesystem/
│   │   │       ├── TradeSystemPlugin.java          # Main plugin class
│   │   │       ├── command/
│   │   │       │   └── TradeCommand.java           # Trade command handler
│   │   │       ├── trade/
│   │   │       │   ├── TradeSession.java           # Trade session management
│   │   │       │   └── TradeSessionManager.java    # Trade session manager
│   │   │       ├── inventory/
│   │   │       │   └── TradeInventoryManager.java  # Trade GUI management
│   │   │       ├── listener/
│   │   │       │   └── InventoryListener.java      # Inventory event handler
│   │   │       └── util/
│   │   │           └── TradeValidator.java          # Trade validation utilities
│   │   └── resources/
│   │       └── plugin.yml                         # Plugin configuration
│   └── README.md                                   # This file
└── pom.xml                                         # Maven build configuration
```

## License

This plugin is proprietary software. Unauthorized distribution or modification is prohibited.

## Support

For issues or feature requests, please contact the plugin developer.
