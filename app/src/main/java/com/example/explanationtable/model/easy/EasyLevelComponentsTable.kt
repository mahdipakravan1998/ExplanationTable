package com.example.explanationtable.model.easy

/**
 * A data class representing an easy level components table.
 *
 * @property id The unique identifier for the table.
 * @property components A map where each key is an integer representing a component identifier, and each value is a list of strings associated with that component.
 */
data class EasyLevelComponentsTable(
    val id: Int,
    val components: Map<Int, List<String>>
)
