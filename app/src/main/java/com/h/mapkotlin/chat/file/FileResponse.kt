package com.h.mapkotlin.chat.file

data class FileResponse(
    val path: List<String>,
    val status: Boolean,
    val time: String,
    val type: Int,
    val u: Int
)