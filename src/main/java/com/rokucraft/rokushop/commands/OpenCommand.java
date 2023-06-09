package com.rokucraft.rokushop.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.paper.PaperCommandManager;
import com.rokucraft.rokushop.RokuShop;
import com.rokucraft.rokushop.entities.Shop;
import com.rokucraft.rokushop.entities.ShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OpenCommand {
    private final Command<CommandSender> command;
    private final CommandArgument<CommandSender, Shop> shopArgument;
    private final CommandArgument<CommandSender, Player> playerArgument;
    private final RokuShop plugin;

    public OpenCommand(Command.Builder<CommandSender> builder, PaperCommandManager<CommandSender> manager, RokuShop plugin) {
        this.plugin = plugin;
        shopArgument = manager.argumentBuilder(Shop.class, "shop").build();
        playerArgument = PlayerArgument.optional("player");
        command = builder.literal("open", ArgumentDescription.of("Open a shop"))
                .permission("rokushop.open")
                .argument(shopArgument, ArgumentDescription.of("The shop to open"))
                .argument(playerArgument, ArgumentDescription.of("The player you want to show the shop to"))
                .handler(this::execute)
                .build();
    }

    private void execute(@NonNull CommandContext<CommandSender> context) {
        Shop shop = context.get(shopArgument);
        Player target = context.getOptional(playerArgument)
                .orElseGet(() -> context.getSender() instanceof Player player ? player : null);
        if (target == null) {
            context.getSender()
                    .sendMessage(Component.text("You did not provide a target", NamedTextColor.RED));
            return;
        }
        new ShopGUI(shop, plugin.economy()).open(target);
    }

    public Command<CommandSender> command() {
        return command;
    }
}
