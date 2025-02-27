# LightSetup

A Minecraft plugin that allows you to control Redstone Lamps, creating custom lighting effects. Originally developed for Neon Network.

## Features

- Create a wand to control Redstone Lamps
- Set custom time intervals for lamp flashing
- Lamps remain active even after server restart
- Automatic configuration saving

## Installation

1. Download the latest version from the [Releases](https://github.com/Pyro18/LightSetup/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. The plugin will automatically generate a default configuration file

## Usage

### Commands

- `/setlights <ticks>` - Gives you a Wand with the specified tick interval
    - Example: `/setlights 20` (creates a wand that makes lamps flash every 1 second, as 20 ticks = 1 second in Minecraft)

### Permissions

- `lightsetup.setlights` - Allows players to use the `/setlights` command (default: op)

### How to Use

1. Get the Wand using the `/setlights` command
2. Place Redstone Lamps wherever you want in your world
3. Right-click on a Redstone Lamp with the Wand to activate it
4. The lamp will flash at the specified interval
5. Right-click again to deactivate the lamp

## Building from Source Code

### Requirements

- Java Development Kit (JDK) 21 or higher
- Maven

### Build Instructions

1. Clone this repository:
   ```
   git clone https://github.com/Pyro18/LightSetup.git
   ```
2. Navigate to the project directory:
   ```
   cd LightSetup
   ```
3. Build with Maven:
   ```
   mvn clean package
   ```
4. The compiled JAR will be in the `build/libs` directory

## How It Works

The plugin is structured around several key components:

### LampManager

The central class responsible for tracking, activating, and deactivating lamps:

```java
public class LampManager {
    // ...
    public void toggleLampWithTicks(Block lamp, Player player, int ticks) {
        if (isLampActive(lamp)) {
            deactivateLamp(lamp);
            player.sendMessage("§cLampada disattivata!");
        } else {
            activateLampWithTicks(lamp, ticks);
            player.sendMessage("§aLampada attivata con intervallo di " + ticks + " tick!");
        }
    }
    // ...
}
```

### WandCreator

Handles the creation and management of wand items:

```java
public class WandCreator {
    // ...
    public static ItemStack createWand(int ticks) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        
        meta.setDisplayName(WAND_NAME + " §7(" + ticks + " tick)");
        // Adds lore, enchantments, and persistent data
        // ...
        
        return wand;
    }
    // ...
}
```

### LampTask

A scheduled task that manages the flashing behavior of activated lamps:

```java
public class LampTask extends BukkitRunnable {
    // ...
    @Override
    public void run() {
        // Toggle lamp state
        isLit = !isLit;
        
        // Update lamp block data
        BlockData blockData = lamp.getBlockData();
        if (blockData instanceof Lightable) {
            Lightable lightable = (Lightable) blockData;
            lightable.setLit(isLit);
            lamp.setBlockData(lightable);
        }
    }
    // ...
}
```

### Data Persistence

The plugin saves all active lamps to a configuration file, allowing lamps to persist through server restarts:

```java
// Save a lamp's state to the configuration file
private void saveLamp(Location location, int ticks) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    String locationKey = serializeLocation(location);
    
    config.set("lamps." + locationKey, ticks);
    
    config.save(configFile);
}
```

## Contributing

Contributions are welcome! Feel free to report issues or send pull requests.

## Author

- [Pyro18](https://github.com/Pyro18)