package me.hsgamer.limitcraft;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class LimitCraft extends JavaPlugin implements Listener {
    private final List<String> blockMaterials = new ArrayList<>();
    private final Permission bypass = new Permission("limitcraft.bypass", PermissionDefault.OP);
    private boolean useEvent;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        blockMaterials.addAll(getConfig().getStringList("block-materials"));

        if (getConfig().getBoolean("remove-recipes")) {
            useEvent = false;
            removeRecipes();
        } else {
            useEvent = true;
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getPluginManager().addPermission(bypass);
        }
    }

    @Override
    public void onDisable() {
        if (useEvent) {
            HandlerList.unregisterAll((Plugin) this);
            getServer().getPluginManager().removePermission(bypass);
        }
    }

    private void removeRecipes() {
        List<NamespacedKey> list = new ArrayList<>();
        Iterator<Recipe> recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if ((recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe) && isBlockMaterial(recipe.getResult().getType())) {
                list.add(((Keyed) recipe).getKey());
            }
        }
        list.forEach(key -> getServer().removeRecipe(key));
    }

    private boolean isBlockMaterial(Material material) {
        return blockMaterials.stream().anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(material.name().toLowerCase(Locale.ROOT)));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer().hasPermission(bypass)) return;
        Optional<Material> optional = Optional.ofNullable(event.getRecipe()).map(Recipe::getResult).map(ItemStack::getType);
        if (optional.isPresent() && isBlockMaterial(optional.get())) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked().hasPermission(bypass)) return;
        Optional<Material> optional = Optional.ofNullable(event.getCurrentItem()).map(ItemStack::getType);
        if (optional.isPresent() && isBlockMaterial(optional.get())) {
            event.setCancelled(true);
        }
    }
}
