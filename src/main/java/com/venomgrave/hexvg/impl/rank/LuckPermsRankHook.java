package com.venomgrave.hexvg.impl.rank;

import com.venomgrave.hexvg.api.rank.RankHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class LuckPermsRankHook implements RankHook {

    private final LuckPerms lp;
    private final Logger    logger;

    public LuckPermsRankHook(Logger logger) {
        this.logger = logger;
        LuckPerms api = null;
        try {
            api = LuckPermsProvider.get();
            logger.info("[RankHook] LuckPerms API załadowane.");
        } catch (IllegalStateException e) {
            logger.severe("[RankHook] Nie można załadować LuckPerms: "
                    + e.getMessage());
        }
        this.lp = api;
    }

    @Override
    public int getWeight(Player player) {
        if (lp == null) return 0;

        try {
            User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) return 0;

            // 1. Szukamy meta.weight bezpośrednio na graczu
            int userWeight = user.getNodes().stream()
                    .filter(n -> n instanceof MetaNode)
                    .map(n -> (MetaNode) n)
                    .filter(m -> m.getMetaKey().equals("weight"))
                    .mapToInt(m -> parseIntSafe(m.getMetaValue()))
                    .max()
                    .orElse(-1);

            if (userWeight >= 0) return userWeight;

            // 2. Fallback — weight z głównej grupy gracza
            return getGroupWeight(user.getPrimaryGroup());

        } catch (Exception e) {
            logger.warning("[RankHook] getWeight error dla "
                    + player.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean hasBypass(Player player, String operation) {
        if (lp == null) return player.isOp();
        return player.hasPermission("hexvg.bypass." + operation)
                || player.hasPermission("hexvg.bypass.*");
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    private int getGroupWeight(String groupName) {
        if (lp == null || groupName == null) return 0;
        try {
            Group group = lp.getGroupManager().getGroup(groupName);
            return group != null ? group.getWeight().orElse(0) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}