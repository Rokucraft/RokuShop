package com.rokucraft.rokushop.entities;

import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record Shop(Component displayName, List<ShopItem> items) {}
