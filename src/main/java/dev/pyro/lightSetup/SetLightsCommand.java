package dev.pyro.lightSetup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Command executor for the '/setlights' command.
 * This command allows players to receive a wand with a specified number of ticks.
 */
public class SetLightsCommand implements CommandExecutor {
    private final Plugin plugin;
    private final LampManager lampManager;

    /**
     * Constructs a new SetLightsCommand.
     *
     * @param plugin the plugin instance
     * @param lampManager the lamp manager instance
     */
    public SetLightsCommand(Plugin plugin, LampManager lampManager) {
        this.plugin = plugin;
        this.lampManager = lampManager;
    }

    /**
     * Executes the '/setlights' command.
     *
     * @param sender the source of the command
     * @param command the command which was executed
     * @param label the alias of the command which was used
     * @param args the arguments passed to the command
     * @return true if the command was successful, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cQuesto comando può essere usato solo da un giocatore!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso corretto: /setlights <numero di tick>");
            return true;
        }

        try {
            int ticks = Integer.parseInt(args[0]);
            if (ticks <= 0) {
                sender.sendMessage("§cIl numero di tick deve essere positivo!");
                return true;
            }

            Player player = (Player) sender;

            ItemStack wand = WandCreator.createWand(ticks);
            player.getInventory().addItem(wand);
            player.sendMessage("§aHai ricevuto il Bastone delle Luci con " + ticks + " tick!");

            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cDevi inserire un numero valido!");
            return true;
        }
    }
}