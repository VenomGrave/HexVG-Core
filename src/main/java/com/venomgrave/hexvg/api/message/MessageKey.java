package com.venomgrave.hexvg.api.message;

public enum MessageKey {

    // ── General ───────────────────────────────────────────────────────────
    PREFIX                          ("general.prefix"),
    NO_PERMISSION                   ("general.no-permission"),
    PLAYER_ONLY                     ("general.player-only"),
    PLAYER_NOT_FOUND                ("general.player-not-found"),
    INVALID_NUMBER                  ("general.invalid-number"),
    INVALID_USAGE                   ("general.invalid-usage"),
    RELOAD_SUCCESS                  ("general.reload-success"),
    RELOAD_FAILED                   ("general.reload-failed"),
    CONSOLE_SENDER                  ("general.console-sender"),
    UNKNOWN_COMMAND                 ("general.unknown-command"),

    // ── Database ──────────────────────────────────────────────────────────
    DB_CONNECTING                   ("database.connecting"),
    DB_CONNECTED                    ("database.connected"),
    DB_CONNECTION_FAILED            ("database.connection-failed"),
    DB_RECONNECTING                 ("database.reconnecting"),
    DB_RECONNECT_FAILED             ("database.reconnect-failed"),

    // ── Session ───────────────────────────────────────────────────────────
    SESSION_FIRST_JOIN              ("session.first-join"),
    SESSION_SEEN_ONLINE             ("session.seen-online"),
    SESSION_SEEN_OFFLINE            ("session.seen-offline"),
    SESSION_NEVER_SEEN              ("session.seen-never"),
    SESSION_PLAYTIME                ("session.playtime"),
    SESSION_PLAYTIME_OTHER          ("session.playtime-other"),

    // ── AFK ───────────────────────────────────────────────────────────────
    AFK_NOW_AFK                     ("afk.now-afk"),
    AFK_NO_LONGER_AFK               ("afk.no-longer-afk"),
    AFK_SELF_AFK                    ("afk.self-afk"),
    AFK_SELF_BACK                   ("afk.self-back"),
    AFK_KICK_REASON                 ("afk.kick-reason"),
    AFK_TARGET_IS_AFK               ("afk.target-is-afk"),

    // ── Economy ───────────────────────────────────────────────────────────
    ECO_BALANCE_SELF                ("economy.balance-self"),
    ECO_BALANCE_OTHER               ("economy.balance-other"),
    ECO_PAY_SUCCESS                 ("economy.pay-success"),
    ECO_PAY_RECEIVED                ("economy.pay-received"),
    ECO_PAY_TAX                     ("economy.pay-tax"),
    ECO_PAY_SELF                    ("economy.pay-self"),
    ECO_PAY_INSUFFICIENT            ("economy.pay-insufficient"),
    ECO_PAY_INVALID_AMOUNT          ("economy.pay-invalid-amount"),
    ECO_PAY_MAX_EXCEEDED            ("economy.pay-max-exceeded"),
    ECO_GIVE                        ("economy.eco-give"),
    ECO_TAKE                        ("economy.eco-take"),
    ECO_SET                         ("economy.eco-set"),
    ECO_RESET                       ("economy.eco-reset"),
    ECO_BALTOP_HEADER               ("economy.baltop-header"),
    ECO_BALTOP_ENTRY                ("economy.baltop-entry"),
    ECO_BALTOP_EMPTY                ("economy.baltop-empty"),
    ECO_LOG_HEADER                  ("economy.log-header"),
    ECO_LOG_ENTRY                   ("economy.log-entry"),
    ECO_LOG_EMPTY                   ("economy.log-empty"),
    ECO_DAILY_CLAIMED               ("economy.daily-claimed"),
    ECO_DAILY_COOLDOWN              ("economy.daily-cooldown"),
    ECO_DAILY_STREAK                ("economy.daily-streak"),

    // ── Home ──────────────────────────────────────────────────────────────
    HOME_SET                        ("home.set"),
    HOME_SET_LIMIT                  ("home.set-limit"),
    HOME_DELETED                    ("home.deleted"),
    HOME_NOT_FOUND                  ("home.not-found"),
    HOME_TELEPORTING                ("home.teleporting"),
    HOME_TELEPORTED                 ("home.teleported"),
    HOME_LIST_HEADER                ("home.list-header"),
    HOME_LIST_ENTRY                 ("home.list-entry"),
    HOME_LIST_EMPTY                 ("home.list-empty"),

    // ── Warp ──────────────────────────────────────────────────────────────
    WARP_SET                        ("warp.set"),
    WARP_DELETED                    ("warp.deleted"),
    WARP_NOT_FOUND                  ("warp.not-found"),
    WARP_NO_ACCESS                  ("warp.no-access"),
    WARP_TELEPORTING                ("warp.teleporting"),
    WARP_TELEPORTED                 ("warp.teleported"),
    WARP_INFO                       ("warp.info"),
    WARP_LIST_HEADER                ("warp.list-header"),
    WARP_LIST_EMPTY                 ("warp.list-empty"),

    // ── Spawn ─────────────────────────────────────────────────────────────
    SPAWN_SET                       ("spawn.set"),
    SPAWN_NOT_SET                   ("spawn.not-set"),
    SPAWN_TELEPORTING               ("spawn.teleporting"),
    SPAWN_TELEPORTED                ("spawn.teleported"),

    // ── Teleport ──────────────────────────────────────────────────────────
    TP_TELEPORTED                   ("teleport.teleported"),
    TP_TELEPORTING                  ("teleport.teleporting"),
    TP_WARMUP                       ("teleport.warmup"),
    TP_WARMUP_CANCELLED             ("teleport.warmup-cancelled"),
    TP_COOLDOWN                     ("teleport.cooldown"),
    TP_COMBAT                       ("teleport.combat"),
    TP_UNSAFE                       ("teleport.unsafe"),
    TP_OFFLINE                      ("teleport.offline"),
    TPA_SENT                        ("teleport.tpa-sent"),
    TPA_RECEIVED                    ("teleport.tpa-received"),
    TPA_ACCEPTED                    ("teleport.tpa-accepted"),
    TPA_DENIED                      ("teleport.tpa-denied"),
    TPA_CANCELLED                   ("teleport.tpa-cancelled"),
    TPA_EXPIRED                     ("teleport.tpa-expired"),
    TPA_NO_REQUEST                  ("teleport.tpa-no-request"),
    TPA_ALREADY_SENT                ("teleport.tpa-already-sent"),
    BACK_NO_LOCATION                ("teleport.back-no-location"),
    BACK_TELEPORTED                 ("teleport.back-teleported"),
    RTP_SEARCHING                   ("teleport.rtp-searching"),
    RTP_TELEPORTED                  ("teleport.rtp-teleported"),
    RTP_FAILED                      ("teleport.rtp-failed"),
    RTP_COOLDOWN                    ("teleport.rtp-cooldown"),

    // ── Vanity ────────────────────────────────────────────────────────────
    NICK_SET                        ("vanity.nick-set"),
    NICK_RESET                      ("vanity.nick-reset"),
    NICK_TAKEN                      ("vanity.nick-taken"),
    NICK_TOO_LONG                   ("vanity.nick-too-long"),
    NICK_INVALID                    ("vanity.nick-invalid"),
    HAT_SET                         ("vanity.hat-set"),
    HAT_NO_ITEM                     ("vanity.hat-no-item"),

    // ── Moderation ────────────────────────────────────────────────────────
    MOD_BANNED                      ("moderation.banned"),
    MOD_BANNED_SCREEN               ("moderation.banned-screen"),
    MOD_TEMP_BANNED                 ("moderation.temp-banned"),
    MOD_TEMP_BANNED_SCREEN          ("moderation.temp-banned-screen"),
    MOD_UNBANNED                    ("moderation.unbanned"),
    MOD_NOT_BANNED                  ("moderation.not-banned"),
    MOD_ALREADY_BANNED              ("moderation.already-banned"),
    MOD_BANNED_IP                   ("moderation.banned-ip"),
    MOD_UNBANNED_IP                 ("moderation.unbanned-ip"),
    MOD_MUTED                       ("moderation.muted"),
    MOD_MUTED_NOTIFY                ("moderation.muted-notify"),
    MOD_UNMUTED                     ("moderation.unmuted"),
    MOD_NOT_MUTED                   ("moderation.not-muted"),
    MOD_KICKED                      ("moderation.kicked"),
    MOD_KICK_SCREEN                 ("moderation.kick-screen"),
    MOD_KICKALL_DONE                ("moderation.kickall-done"),
    MOD_FROZEN                      ("moderation.frozen"),
    MOD_UNFROZEN                    ("moderation.unfrozen"),
    MOD_FROZEN_NOTIFY               ("moderation.frozen-notify"),
    MOD_CANT_PUNISH                 ("moderation.cant-punish"),
    MOD_CANT_PUNISH_SELF            ("moderation.cant-punish-self"),

    // ── Warns ─────────────────────────────────────────────────────────────
    WARN_ISSUED                     ("warn.issued"),
    WARN_RECEIVED                   ("warn.received"),
    WARN_LIST_HEADER                ("warn.list-header"),
    WARN_LIST_ENTRY                 ("warn.list-entry"),
    WARN_LIST_EMPTY                 ("warn.list-empty"),
    WARN_CLEARED                    ("warn.cleared"),
    WARN_NOT_FOUND                  ("warn.not-found"),
    WARN_AUTO_KICK                  ("warn.auto-kick"),
    WARN_AUTO_MUTE                  ("warn.auto-mute"),
    WARN_AUTO_BAN                   ("warn.auto-ban"),

    // ── Staff ─────────────────────────────────────────────────────────────
    STAFF_VANISH_ON                 ("staff.vanish-on"),
    STAFF_VANISH_OFF                ("staff.vanish-off"),
    STAFF_VANISH_OTHER_ON           ("staff.vanish-other-on"),
    STAFF_VANISH_OTHER_OFF          ("staff.vanish-other-off"),
    STAFF_SOCIALSPY_ON              ("staff.socialspy-on"),
    STAFF_SOCIALSPY_OFF             ("staff.socialspy-off"),
    STAFF_SOCIALSPY_FORMAT          ("staff.socialspy-format"),
    STAFF_BROADCAST                 ("staff.broadcast"),
    STAFF_ANNOUNCE                  ("staff.announce"),
    STAFF_HELPOP_SENT               ("staff.helpop-sent"),
    STAFF_HELPOP_RECEIVED           ("staff.helpop-received"),
    STAFF_LIST_HEADER               ("staff.list-header"),
    STAFF_LIST_ENTRY                ("staff.list-entry"),
    STAFF_LIST_EMPTY                ("staff.list-empty"),
    STAFF_CHAT_FORMAT               ("staff.chat-format"),
    STAFF_NOTE_ADDED                ("staff.note-added"),
    STAFF_NOTE_LIST_HEADER          ("staff.note-list-header"),
    STAFF_NOTE_LIST_ENTRY           ("staff.note-list-entry"),
    STAFF_NOTE_LIST_EMPTY           ("staff.note-list-empty"),
    STAFF_NOTE_DELETED              ("staff.note-deleted"),
    STAFF_NOTE_NOT_FOUND            ("staff.note-not-found"),

    // ── Info ──────────────────────────────────────────────────────────────
    INFO_CHECK_HEADER               ("info.check-header"),
    INFO_CHECK_UUID                 ("info.check-uuid"),
    INFO_CHECK_STATUS               ("info.check-status"),
    INFO_CHECK_WARNS                ("info.check-warns"),
    INFO_CHECK_PLAYTIME             ("info.check-playtime"),
    INFO_CHECK_FIRST_JOIN           ("info.check-first-join"),
    INFO_CHECK_LAST_SEEN            ("info.check-last-seen"),
    INFO_WHOIS_HEADER               ("info.whois-header"),
    INFO_WHOIS_UUID                 ("info.whois-uuid"),
    INFO_WHOIS_IP                   ("info.whois-ip"),
    INFO_WHOIS_LOGINS               ("info.whois-logins"),
    INFO_WHOIS_FIRST                ("info.whois-first"),
    INFO_HISTORY_HEADER             ("info.history-header"),
    INFO_HISTORY_ENTRY_BAN          ("info.history-entry-ban"),
    INFO_HISTORY_ENTRY_MUTE         ("info.history-entry-mute"),
    INFO_HISTORY_ENTRY_KICK         ("info.history-entry-kick"),
    INFO_HISTORY_ENTRY_WARN         ("info.history-entry-warn"),
    INFO_HISTORY_EMPTY              ("info.history-empty"),
    INFO_UPTIME                     ("info.uptime"),
    INFO_LAG_HEADER                 ("info.lag-header"),
    INFO_LAG_TPS                    ("info.lag-tps"),
    INFO_LAG_MSPT                   ("info.lag-mspt"),
    INFO_LAG_RAM                    ("info.lag-ram"),
    INFO_LAG_ENTITIES               ("info.lag-entities"),

    // ── Maintenance ───────────────────────────────────────────────────────
    MAINTENANCE_ON                  ("maintenance.on"),
    MAINTENANCE_OFF                 ("maintenance.off"),
    MAINTENANCE_STATUS_ON           ("maintenance.status-on"),
    MAINTENANCE_STATUS_OFF          ("maintenance.status-off"),
    MAINTENANCE_KICK_MESSAGE        ("maintenance.kick-message"),
    MAINTENANCE_JOIN_BLOCKED        ("maintenance.join-blocked"),

    // ── RestartWarn ───────────────────────────────────────────────────────
    RESTART_WARN_STARTED            ("restart.warn-started"),
    RESTART_WARN_BROADCAST          ("restart.warn-broadcast"),
    RESTART_WARN_CANCELLED          ("restart.warn-cancelled"),
    RESTART_WARN_STATUS_ACTIVE      ("restart.status-active"),
    RESTART_WARN_STATUS_INACTIVE    ("restart.status-inactive"),
    RESTART_WARN_KICK               ("restart.kick-message"),
    RESTART_WARN_ALREADY_RUNNING    ("restart.already-running"),
    RESTART_WARN_NOT_RUNNING        ("restart.not-running"),

    // ── Chat ──────────────────────────────────────────────────────────────
    CHAT_CHANNEL_SWITCHED           ("chat.channel-switched"),
    CHAT_CHANNEL_NO_PERMISSION      ("chat.channel-no-permission"),
    CHAT_MSG_SENT                   ("chat.msg-sent"),
    CHAT_MSG_RECEIVED               ("chat.msg-received"),
    CHAT_MSG_OFFLINE                ("chat.msg-offline"),
    CHAT_REPLY_NO_TARGET            ("chat.reply-no-target"),
    CHAT_IGNORED                    ("chat.ignored"),
    CHAT_UNIGNORED                  ("chat.unignored"),
    CHAT_ALREADY_IGNORED            ("chat.already-ignored"),
    CHAT_NOT_IGNORED                ("chat.not-ignored"),
    CHAT_IGNORE_SELF                ("chat.ignore-self"),
    CHAT_IGNORE_LIST_HEADER         ("chat.ignore-list-header"),
    CHAT_IGNORE_LIST_ENTRY          ("chat.ignore-list-entry"),
    CHAT_IGNORE_LIST_EMPTY          ("chat.ignore-list-empty"),
    CHAT_MUTED                      ("chat.muted"),
    CHAT_SPAM_COOLDOWN              ("chat.spam-cooldown"),
    CHAT_PROFANITY_BLOCKED          ("chat.profanity-blocked"),
    CHAT_TARGET_AFK                 ("chat.target-afk"),

    // ── Errors ────────────────────────────────────────────────────────────
    ERROR_DATABASE                  ("errors.database"),
    ERROR_UNKNOWN                   ("errors.unknown"),
    ERROR_CONSOLE_ONLY              ("errors.console-only");

    // ── Infrastruktura ────────────────────────────────────────────────────

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}