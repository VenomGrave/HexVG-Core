package com.hexvg.core.database.migration;

import lombok.Getter;

/**
 * Abstrakcyjna klasa reprezentująca jedną migrację bazy danych.
 *
 * Przykład implementacji:
 * <pre>
 *     new Migration(1, "HexShop", "create_shops_table") {
 *         {@literal @}Override
 *         public String[] getSQL() {
 *             return new String[]{
 *                 "CREATE TABLE IF NOT EXISTS shops (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(64))",
 *                 "CREATE INDEX idx_shop_name ON shops (name)"
 *             };
 *         }
 *     }
 * </pre>
 */
@Getter
public abstract class Migration {

    /** Numer wersji migracji - musi być unikalny per plugin i rosnący */
    private final int version;

    /** Nazwa pluginu właściciela migracji */
    private final String pluginName;

    /** Czytelna nazwa migracji (do logów) */
    private final String name;

    protected Migration(int version, String pluginName, String name) {
        this.version = version;
        this.pluginName = pluginName;
        this.name = name;
    }

    /**
     * Zwraca tablicę zapytań SQL do wykonania w ramach tej migracji.
     * Wszystkie zapytania są wykonywane w jednej transakcji.
     */
    public abstract String[] getSQL();
}