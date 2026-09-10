import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val localProperties = Properties()
val localPropertiesFile = rootDir.resolve("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val mapboxDownloadsToken = localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN")
    ?: providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse("sk.placeholder")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = mapboxDownloadsToken
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "AppMiRuta"
include(":app")
