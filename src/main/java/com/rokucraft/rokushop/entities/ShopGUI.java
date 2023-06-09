package com.rokucraft.rokushop.entities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.core.transform.types.PaginatedTransform;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.type.ChestInterface;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final ChestInterface shopInterface;
    private final Economy economy;
    private static final ItemStack previous =
            createSkull(Component.text("Previous", NamedTextColor.YELLOW), "MHF_ArrowLeft");
    private static final ItemStack next =
            createSkull(Component.text("Next", NamedTextColor.YELLOW), "MHF_ArrowRight");
    public ShopGUI(Shop shop, Economy economy) {
        this.economy = economy;

        var transform = new PaginatedTransform<ItemStackElement<ChestPane>, ChestPane, PlayerViewer>(
                Vector2.at(1, 1),
                Vector2.at(7, 4),
                shop.items()
                        .stream()
                        .map(this::shopElement)
                        .toList()
        );
        transform.backwardElement(
                Vector2.at(0, 5),
                t -> ItemStackElement.of(previous, c -> t.previousPage())
        );
        transform.forwardElement(
                Vector2.at(8, 5),
                t -> ItemStackElement.of(next, c -> t.nextPage())
        );
        shopInterface = ChestInterface.builder()
                .clickHandler(ClickHandler.cancel())
                .rows(6)
                .title(shop.displayName())
                .addReactiveTransform(transform)
                .build();
    }

    public void open(Player player) {
        shopInterface.open(PlayerViewer.of(player));
    }

    private static ItemStack createSkull(Component name, String ownerName) {
        var head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, skullMeta -> {
            skullMeta.displayName(name);
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName));
        });
        return head;
    }

    private ItemStackElement<ChestPane> shopElement(ShopItem shopItem) {
        ItemStack item = shopItem.item();
        ItemStack displayedItem = new ItemStack(item);
        displayedItem.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            if (!lore.isEmpty()) {
                lore.add(Component.empty());
            }
            lore.add(
                    Component.text()
                            .append(Component.text("Cost: ", NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text(economy.currencyNameSingular(), NamedTextColor.GOLD))
                            .append(Component.text(shopItem.price(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false)
                            .build()
            );
            meta.lore(lore);
        });
        return new ItemStackElement<>(
                displayedItem,
                context -> {
                    Player player = context.viewer().player();
                    EconomyResponse res = economy.withdrawPlayer(player, shopItem.price());
                    if (!res.transactionSuccess()) {
                        player.sendMessage(Component.text("You do not have enough money to purchase this!", NamedTextColor.RED));
                        return;
                    }
                    player.sendMessage(
                            Component.text()
                                    .append(Component.text("Purchased", NamedTextColor.GREEN))
                                    .append(Component.space())
                                    .append(Component.text(item.getAmount()))
                                    .append(Component.text("x"))
                                    .append(Component.space())
                                    .append(item.displayName().hoverEvent(item))
                    );
                    player.getInventory().addItem(item);
                }
                );
    }
}
