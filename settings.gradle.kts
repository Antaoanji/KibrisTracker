pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "2.3.3"
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
