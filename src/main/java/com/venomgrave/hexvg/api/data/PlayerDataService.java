package com.venomgrave.hexvg.api.data;

import java.util.UUID;

public interface PlayerDataService {

    /**
     * Tworzy lub aktualizuje profil gracza.
     * Wywoływane przy każdym JoinEvent.
     */
    void upsertProfile(UUID uuid, String name, String ipHash);

    /**
     * Pseudonim gracza lub null.
     */
    String getNick(UUID uuid);

    /**
     * Ustawia pseudonim gracza.
     */
    void setNick(UUID uuid, String nick);

    /**
     * Resetuje pseudonim gracza.
     */
    void clearNick(UUID uuid);

    /**
     * Sprawdza czy pseudonim jest zajęty przez innego gracza.
     */
    boolean isNickTaken(String nick, UUID excludeUuid);

    /**
     * Hash pierwszego IP gracza.
     */
    String getFirstIpHash(UUID uuid);

    /**
     * Sprawdza czy gracz ma aktywny ban w hexvg_punishments.
     * Wynik cachowany — invalidowany przy ban/unban.
     */
    boolean isBanned(UUID uuid);

    /**
     * Sprawdza czy gracz ma aktywny mute w hexvg_punishments.
     */
    boolean isMuted(UUID uuid);

    /**
     * Liczba aktywnych warnów gracza.
     */
    int getActiveWarnCount(UUID uuid);

    /**
     * Invaliduje cache bana/muta dla gracza.
     * Wywoływane przez ServerTools po ban/unban/mute/unmute.
     */
    void invalidateCache(UUID uuid);
}