package com.rokucraft.rokushop.entities;

import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record ShopItem(int price, ItemStack item) { }
