package com.example.explanationtable.model.easy

data class EasyLevelComponentsTable(
    val id: Int,
    val components: Map<Int, List<String>>
)