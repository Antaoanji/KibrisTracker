package com.example.pushuptracker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.example.pushuptracker.data.remote.GithubApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val githubApiService: GithubApiService
) {
    private val GITHUB_OWNER = "Antaoanji"
    private val GITHUB_REPO = "KibrisTracker"

    suspend fun checkForUpdates(): UpdateInfo? {
        return try {
            val latestRelease = githubApiService.getLatestRelease(GITHUB_OWNER, GITHUB_REPO)
            val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            
            val latestVersion = latestRelease.tagName.replace("v", "")
            
            if (isVersionNewer(currentVersion, latestVersion)) {
                val apkUrl = latestRelease.assets.firstOrNull { it.name.endsWith(".apk") }?.downloadUrl
                UpdateInfo(
                    latestVersion = latestRelease.tagName,
                    downloadUrl = apkUrl ?: latestRelease.htmlUrl,
                    releaseNotes = "Yeni bir güncelleme mevcut! Sürüm: ${latestRelease.tagName}"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Update check failed", e)
            null
        }
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        return try {
            val currentParts = current.split(".").map { it.toInt() }
            val latestParts = latest.split(".").map { it.toInt() }
            
            for (i in 0 until minOf(currentParts.size, latestParts.size)) {
                if (latestParts[i] > currentParts[i]) return true
                if (latestParts[i] < currentParts[i]) return false
            }
            latestParts.size > currentParts.size
        } catch (e: Exception) {
            latest > current
        }
    }

    fun openDownloadPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String
)
