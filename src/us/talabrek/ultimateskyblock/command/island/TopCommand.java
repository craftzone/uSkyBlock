package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class TopCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public TopCommand(uSkyBlock plugin) {
        super("top", "usb.island.topten", "voir le top 10 des �les.");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        plugin.getIslandLogic().showTopTen(sender);
        return true;
    }
}
