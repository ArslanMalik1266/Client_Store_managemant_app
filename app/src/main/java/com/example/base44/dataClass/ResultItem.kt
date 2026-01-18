package com.example.base44.dataClass

data class ResultItem(
    val id: String,
    val title: String,
    val date: String,
    val firstPrize: String,
    val secondPrize: String,
    val thirdPrize: String,
    val specialNumbers: List<String> = emptyList(),
    val consolationNumbers: List<String> = emptyList()
)
