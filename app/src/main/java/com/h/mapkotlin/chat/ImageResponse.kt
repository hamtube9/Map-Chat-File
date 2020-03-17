package com.h.mapkotlin.chat

data class ImageResponse(
    val path: List<Path>,
    val status: Boolean,
    val time: String,
    val type: Int,
    val u: Int
)