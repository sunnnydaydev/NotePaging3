package com.example.notepaging3.beans.github

import com.google.gson.annotations.SerializedName

/**
 * Create by SunnyDay /09/06 16:31:43
 */
data class RepoResponse(
    @SerializedName("items")
    val items: List<Repo> = emptyList()
)