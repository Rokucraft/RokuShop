package com.rokucraft.rokushop.entities;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
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

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final ChestGui gui;
    private final Economy economy;
    private static final ItemStack previous =
            createSkull(Component.text("Previous", NamedTextColor.YELLOW), "MHF_ArrowLeft");
    private static final ItemStack next =
            createSkull(Component.text("Next", NamedTextColor.YELLOW), "MHF_ArrowRight");
    public ShopGUI(Shop shop, Economy economy) {
        this.economy = economy;

        gui = new ChestGui(6, ComponentHolder.of(shop.displayName()));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnGlobalDrag(event -> event.setCancelled(true));

        PaginatedPane pages = new PaginatedPane(7, 4);
        pages.populateWithGuiItems(
                shop.items()
                        .stream()
                        .map(this::shopItem)
                        .toList()
        );
        gui.addPane(Slot.fromXY(1, 1), pages);

        StaticPane navigation = new StaticPane(9, 1);
        GuiItem backward = new GuiItem(previous, event -> {
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                updateNavigation(pages, navigation);
            }
        });
        GuiItem forward = new GuiItem(next, event -> {
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                updateNavigation(pages, navigation);
            }
        });
        navigation.addItem(backward, 0, 0);
        navigation.addItem(forward, 8, 0);
        gui.addPane(Slot.fromXY(0, 5), navigation);
        updateNavigation(pages, navigation);
    }

    public void open(Player player) {
        gui.show(player);
    }

    private void updateNavigation(PaginatedPane pages, StaticPane navigation) {
        navigation.getItem(Slot.fromXY(0, 0)).setVisible(pages.getPage() > 0);
        navigation.getItem(Slot.fromXY(8, 0)).setVisible(pages.getPage() < pages.getPages() - 1);
        gui.update();
    }

    private static ItemStack createSkull(Component name, String ownerName) {
        var head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, skullMeta -> {
            skullMeta.displayName(name);
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName));
        });
        return head;
    }

    private GuiItem shopItem(ShopItem shopItem) {
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
        return new GuiItem(
                displayedItem,
                event -> {
                    if (!(event.getWhoClicked() instanceof Player player)) return;
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
