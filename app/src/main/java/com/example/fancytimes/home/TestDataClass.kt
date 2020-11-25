package com.example.fancytimes.home

data class TestDataClass(
    val requestCode: Int,

    val title: String,

    val text: String,

    val timeInMillis: Long,

    val repetition: Long?
)