package com.example.explanationtable.model

data class EasyLevelTable(
    val id: Int,
    val rows: Map<Int, Map<Int, List<String>>>
)