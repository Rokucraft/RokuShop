package com.rokucraft.rokushop.commands;

import com.rokucraft.rokushop.RokuShop;
import com.rokucraft.rokushop.entities.Shop;
import com.rokucraft.rokushop.entities.ShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;

import static com.rokucraft.rokushop.commands.parsers.ShopParser.shopParser;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.description.Description.description;

public class OpenCommand {
    private static final CloudKey<Shop> SHOP_KEY = CloudKey.of("shop", Shop.class);
    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private final Command<CommandSender> command;
    private final RokuShop plugin;

    public OpenCommand(Command.Builder<CommandSender> builder, RokuShop plugin) {
        this.plugin = plugin;
        command = builder.literal("open", description("Open a shop"))
                .permission("rokushop.open")
                .required(SHOP_KEY, shopParser(plugin), description("The shop to open"))
                .optional(PLAYER_KEY, playerParser(), description("The player you want to show the shop to"))
                .handler(this::execute)
                .build();
    }

    private void execute(@NonNull CommandContext<CommandSender> context) {
        Shop shop = context.get(SHOP_KEY);
        Player target = context.optional(PLAYER_KEY)
                .orElseGet(() -> context.sender() instanceof Player player ? player : null);
        if (target == null) {
            context.sender()
                    .sendMessage(Component.text("You did not provide a target", NamedTextColor.RED));
            return;
        }
        new ShopGUI(shop, plugin.economy()).open(target);
    }

    public Command<CommandSender> command() {
        return command;
    }
}
