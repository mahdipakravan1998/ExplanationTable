package com.example.explanationtable.ui

object Routes {
    const val MAIN = "main"

    // Parameterized route for StagesListPage: accepts a difficulty argument
    const val STAGES_LIST = "stages_list"
    const val STAGES_LIST_WITH_ARG = "stages_list/{difficulty}"

    // Updated route for the GameplayPage to include both stageNumber and difficulty
    const val GAMEPLAY = "gameplay"
    const val GAMEPLAY_WITH_ARGS = "gameplay/{stageNumber}/{difficulty}"
}
