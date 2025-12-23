pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "2.3.0-1.0.28"
    }
    repositories {
        google()            
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "kibris-tracker"
include(":app")
