package com.example.pushuptracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String, // Örn: "v1.1"
    @SerialName("html_url") val htmlUrl: String, // Sürüm sayfası linki
    val assets: List<GithubAsset>
)

@Serializable
data class GithubAsset(
    @SerialName("browser_download_url") val downloadUrl: String, // APK indirme linki
    val name: String
)
