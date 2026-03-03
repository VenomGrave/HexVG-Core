package com.venomgrave.hexvg.api.audit;

public enum AuditAction {

    // ── Economy ───────────────────────────────────────────────────────────
    PAY,
    ECO_GIVE,
    ECO_TAKE,
    ECO_SET,
    ECO_RESET,

    // ── PlayerTools ───────────────────────────────────────────────────────
    SET_HOME,
    DEL_HOME,
    SET_WARP,
    DEL_WARP,
    SET_SPAWN,

    // ── Moderacja ─────────────────────────────────────────────────────────
    BAN,
    TEMP_BAN,
    UNBAN,
    BAN_IP,
    UNBAN_IP,
    KICK,
    KICK_ALL,
    MUTE,
    UNMUTE,
    WARN,
    WARN_CLEAR,
    FREEZE,

    // ── Staff ─────────────────────────────────────────────────────────────
    VANISH,
    SOCIAL_SPY,
    SPY,
    NOTE_ADD,
    NOTE_DELETE,

    // ── Serwer ────────────────────────────────────────────────────────────
    MAINTENANCE_ON,
    MAINTENANCE_OFF,
    RESTART_WARN_START,
    RESTART_WARN_CANCEL,

    // ── Vanity ────────────────────────────────────────────────────────────
    NICK_CHANGE,
    NICK_RESET,
    TAG_CHANGE,

    // ── Inne ──────────────────────────────────────────────────────────────
    DAILY_BONUS
}