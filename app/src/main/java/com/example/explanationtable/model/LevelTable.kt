package com.example.explanationtable.model

/**
 * Represents a table for an easy level configuration.
 *
 * @property id A unique identifier for the table.
 * @property rows A nested map representing the table's data structure.
 *         - The outer map key is the row identifier (Int).
 *         - The inner map key is the column identifier (Int).
 *         - The inner map value is a list of strings representing the cell values.
 */
data class LevelTable(
    val id: Int,
    val rows: Map<Int, Map<Int, List<String>>>
)
