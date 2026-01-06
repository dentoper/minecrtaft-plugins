# BetterFurnace

Enhanced furnace and campfire display plugin for Paper 1.21.x servers. Shows real-time cooking progress, item names, and fuel information in the player's action bar.

## Features

- **Real-time Progress Display**: See cooking progress with animated progress bars
- **Multiple Block Support**:
  - Furnace
  - Blast Furnace
  - Smoker
  - Campfire
  - Soul Campfire
- **Customizable Display**: Configure formats, colors, and animation speed
- **Player Preferences**: Individual settings per player
- **Performance Optimized**: Efficient tracking and display system

## Installation

1. Build the plugin: `mvn clean package`
2. Copy the generated JAR from `target/` to your server's `plugins/` folder
3. Restart the server or load the plugin
4. Configure `config.yml` as needed

## Configuration

### Enable/Disable Blocks

```yaml
blocks:
  furnace:
    enabled: true
  blast_furnace:
    enabled: true
  smoker:
    enabled: true
  campfire:
    enabled: true
  soul_campfire:
    enabled: true
```

### Display Settings

```yaml
display:
  enabled: true
  update_title: false  # true to use action bar, false to use title
  animation:
    enabled: true
    speed_ticks: 4
  show_progress_bar: true
  show_item_count: true
  show_fuel: true
```

### Custom Formats

Available placeholders:
- `<item>` - Name of the item being cooked
- `<progress>` - Progress bar or percentage
- `<fuel>` - Fuel item name and count

```yaml
display:
  formats:
    furnace: "&e⚙ &f<item> &e| &f<progress> &e| &f⛽<fuel>"
    campfire: "&c🔥 &f<item> &c| &f<progress>"
```

### Progress Bar Customization

```yaml
display:
  progress_bar:
    length: 10
    fill: "█"
    empty: "░"
    color_complete: "&a"
    color_incomplete: "&7"
```

## Permissions

- `betterfurnace.use` - Use BetterFurnace features (default: true)
- `betterfurnace.admin` - Administrative commands (default: op)

## Requirements

- Paper 1.21.x or higher
- Java 21

## Technical Details

### Architecture

The plugin is organized into several components:

- **BetterFurnacePlugin**: Main plugin class with lifecycle management
- **ConfigManager**: Handles configuration loading and access
- **CookingTracker**: Tracks active cooking operations
- **DisplayManager**: Manages display updates and animations
- **PreferenceManager**: Stores player preferences
- **Listeners**:
  - FurnaceListener: Handles furnace interactions
  - CampfireListener: Handles campfire interactions
  - ChunkListener: Cleans up on chunk unload
- **Utils**:
  - DisplayUtils: Color and text formatting
  - AnimationUtils: Progress bar and cooking animations
  - ItemNameUtils: Item name formatting
- **Models**:
  - CookingState: Represents cooking operation state
  - PlayerPreferences: Player-specific settings

### API Version

- Paper API 1.21.1-R0.1-SNAPSHOT
- Uses Adventure API for modern text components
- PersistentDataContainer for item metadata

## License

This project is open source and available for use and modification.

## Support

For issues, questions, or suggestions, please open an issue in the repository.
