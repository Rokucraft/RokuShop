package com.rokucraft.rokushop.serializers;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {
    public static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

    private static final String MATERIAL = "material";
    private static final String AMOUNT = "amount";
    private static final String META = "meta";
    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String CUSTOM_MODEL_DATA = "custom-model-data";
    private static final String UNBREAKABLE = "unbreakable";
    private static final String ITEM_FLAGS = "item-flags";


    @Override
    public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Material material = node.node(MATERIAL).get(Material.class);
        int amount = node.node(AMOUNT).getInt(1);
        if (material == null)
            throw new SerializationException(Material.class, "You did not provide a material.");
        ItemStack item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        var metaNode = node.node(META);
        meta.displayName(metaNode.node(DISPLAY_NAME).get(Component.class));
        meta.lore(metaNode.node(LORE).getList(Component.class));
        meta.setCustomModelData(metaNode.node(CUSTOM_MODEL_DATA).getInt());
        meta.setUnbreakable(metaNode.node(UNBREAKABLE).getBoolean());
        meta.addItemFlags(metaNode.node(ITEM_FLAGS).get(ItemFlag[].class));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        node.node(MATERIAL).set(obj.getType());
        node.node(AMOUNT).set(obj.getAmount());
        var meta = obj.getItemMeta();
        var metaNode = node.node(META);
        metaNode.node(DISPLAY_NAME).set(meta.displayName());
        metaNode.node(LORE).set(meta.lore());
        metaNode.node(CUSTOM_MODEL_DATA).set(meta.getCustomModelData());
        metaNode.node(UNBREAKABLE).set(meta.isUnbreakable());
        metaNode.node(ITEM_FLAGS).set(meta.getItemFlags());
    }
}
