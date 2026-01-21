package com.example.pushuptracker.util

import java.util.regex.Pattern

object YoutubeUtils {
    /**
     * YouTube linklerinden Video ID'sini hatasız ayıklar.
     * Shorts, Watch, Embed ve youtu.be formatlarını destekler.
     */
    fun getYoutubeId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        
        val cleanedUrl = url.trim()
        
        // Daha kapsayıcı bir Regex
        val pattern = "(?<=watch\\?v=|/videos/|/embed/|/shorts/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(cleanedUrl)
        
        return if (matcher.find()) {
            val id = matcher.group()
            if (id.length >= 11) id.substring(0, 11) else id
        } else {
            // Yedek mekanizma
            when {
                cleanedUrl.contains("youtu.be/") -> cleanedUrl.substringAfter("youtu.be/").take(11)
                cleanedUrl.contains("v=") -> cleanedUrl.substringAfter("v=").take(11)
                cleanedUrl.contains("/shorts/") -> cleanedUrl.substringAfter("/shorts/").take(11)
                else -> ""
            }
        }
    }

    /**
     * YouTube video ID'sinden thumbnail URL'si oluşturur.
     */
    fun getThumbnailUrl(videoId: String): String {
        return if (videoId.length == 11) {
            "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        } else {
            ""
        }
    }
}
