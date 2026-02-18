package com.hexvg.core.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder do tworzenia ItemStacków w wygodny sposób.
 *
 * Przykład użycia:
 * <pre>
 *     ItemStack item = new ItemBuilder(Material.DIAMOND_SWORD)
 *         .name("&bMieczyk Admina")
 *         .lore("&7Potężny miecz", "&7dla administratorów")
 *         .enchant(Enchantment.DAMAGE_ALL, 5)
 *         .unbreakable(true)
 *         .hideFlags()
 *         .build();
 * </pre>
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    /**
     * Ustawia nazwę itemu (obsługuje kolory legacy &).
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
        }
        return this;
    }

    /**
     * Ustawia lore (opis) itemu.
     */
    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<String> lore = Arrays.stream(lines)
                    .map(ChatUtils::colorize)
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        return lore(lines.toArray(new String[0]));
    }

    /**
     * Dodaje enchantment do itemu.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    /**
     * Ustawia ilość itemu.
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Ukrywa wszystkie flagi (enchant, attributes, unbreakable itp.).
     */
    public ItemBuilder hideFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    /**
     * Dodaje konkretne flagi.
     */
    public ItemBuilder addFlags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Ustawia niezniszczalność itemu.
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    /**
     * Ustawia Custom Model Data (dla resource packów).
     */
    public ItemBuilder customModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    /**
     * Buduje finalny ItemStack.
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Szybkie tworzenie prostego itemu z nazwą.
     */
    public static ItemStack of(Material material, String name) {
        return new ItemBuilder(material).name(name).build();
    }

    /**
     * Szybkie tworzenie itemu wypełniającego (np. szare szkło w GUI).
     */
    public static ItemStack filler(Material material) {
        return new ItemBuilder(material)
                .name(" ")
                .hideFlags()
                .build();
    }
}