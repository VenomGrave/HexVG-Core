package com.hexvg.core.integrations;

import com.hexvg.core.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wrapper na Vault API - ekonomia i uprawnienia.
 * Vault musi być zainstalowany na serwerze jako opcjonalna zależność.
 *
 * Użycie z innych pluginów:
 * <pre>
 *     VaultIntegration vault = HexVGCore.getInstance().getVaultIntegration();
 *
 *     if (vault.isEconomyAvailable()) {
 *         vault.deposit(player, 100.0);
 *         double balance = vault.getBalance(player);
 *     }
 *
 *     if (vault.hasPermission(player, "myplugin.use")) {
 *         // gracz ma uprawnienie
 *     }
 * </pre>
 */
public class VaultIntegration {

    private Economy economy;
    private Permission permission;
    private boolean economyAvailable = false;
    private boolean permissionAvailable = false;

    public VaultIntegration(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            Logger.warning("Vault nie znaleziony! Integracja wyłączona.");
            return;
        }

        setupEconomy(plugin);
        setupPermissions(plugin);
    }

    private void setupEconomy(JavaPlugin plugin) {
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            Logger.warning("Brak providera ekonomii dla Vault!");
            return;
        }

        economy = rsp.getProvider();
        economyAvailable = true;
        Logger.info("Vault Economy załadowany: &e" + economy.getName());
    }

    private void setupPermissions(JavaPlugin plugin) {
        RegisteredServiceProvider<Permission> rsp =
                plugin.getServer().getServicesManager().getRegistration(Permission.class);

        if (rsp == null) {
            Logger.warning("Brak providera uprawnień dla Vault!");
            return;
        }

        permission = rsp.getProvider();
        permissionAvailable = true;
        Logger.info("Vault Permissions załadowany: &e" + permission.getName());
    }

    // ---- Economy API ----

    /**
     * Sprawdza czy Vault Economy jest dostępna.
     */
    public boolean isEconomyAvailable() {
        return economyAvailable && economy != null;
    }

    /**
     * Zwraca saldo gracza.
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEconomyAvailable()) return 0;
        return economy.getBalance(player);
    }

    /**
     * Sprawdza czy gracz ma wystarczające środki.
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.has(player, amount);
    }

    /**
     * Dodaje środki graczowi.
     *
     * @return true jeśli operacja się powiodła
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Odejmuje środki od gracza.
     *
     * @return true jeśli operacja się powiodła
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        if (!has(player, amount)) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Formatuje kwotę do czytelnej formy (np. "100.00 $").
     */
    public String format(double amount) {
        if (!isEconomyAvailable()) return String.valueOf(amount);
        return economy.format(amount);
    }

    /**
     * Zwraca nazwę waluty.
     */
    public String getCurrencyName() {
        if (!isEconomyAvailable()) return "Unknown";
        return economy.currencyNamePlural();
    }

    // ---- Permission API ----

    /**
     * Sprawdza czy Vault Permission jest dostępny.
     */
    public boolean isPermissionAvailable() {
        return permissionAvailable && permission != null;
    }

    /**
     * Sprawdza czy gracz ma uprawnienie.
     */
    public boolean hasPermission(org.bukkit.entity.Player player, String perm) {
        if (!isPermissionAvailable()) return player.hasPermission(perm);
        return permission.has(player, perm);
    }

    /**
     * Dodaje uprawnienie graczowi.
     */
    public boolean addPermission(org.bukkit.entity.Player player, String perm) {
        if (!isPermissionAvailable()) return false;
        return permission.playerAdd(player, perm);
    }

    /**
     * Usuwa uprawnienie graczowi.
     */
    public boolean removePermission(org.bukkit.entity.Player player, String perm) {
        if (!isPermissionAvailable()) return false;
        return permission.playerRemove(player, perm);
    }

    /**
     * Zwraca główną grupę gracza.
     */
    public String getPrimaryGroup(org.bukkit.entity.Player player) {
        if (!isPermissionAvailable()) return "default";
        return permission.getPrimaryGroup(player);
    }

    /**
     * Zwraca surowy obiekt Economy (dla zaawansowanych operacji).
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Zwraca surowy obiekt Permission (dla zaawansowanych operacji).
     */
    public Permission getPermission() {
        return permission;
    }
}