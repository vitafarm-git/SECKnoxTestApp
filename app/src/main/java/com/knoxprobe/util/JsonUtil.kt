package com.knoxprobe.util

import org.json.JSONArray

fun List<String>.toJsonArray(): JSONArray = JSONArray().apply {
    forEach(::put)
}
