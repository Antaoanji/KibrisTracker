package com.example.pushuptracker.data.remote

import com.example.pushuptracker.model.GithubRelease
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiService {
    // KULLANIM: "repos/KULLANICI_ADI/DEPO_ADI/releases/latest"
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubRelease
}
