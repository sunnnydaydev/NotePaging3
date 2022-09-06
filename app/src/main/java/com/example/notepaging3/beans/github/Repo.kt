package com.example.notepaging3.beans.github

import com.google.gson.annotations.SerializedName

/**
 * Create by SunnyDay /09/06 16:32:35
 */
data class Repo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("stargazers_count") val starCount: Int
)