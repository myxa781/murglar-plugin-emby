pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven { url = java.net.URI.create("https://jitpack.io") }
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            val prefix = "murglar-gradle-plugin-"
            if (requested.id.id.startsWith(prefix)) {
                val artifactId = "${requested.id.id.substringAfter(prefix)}-plugin-gradle-plugin"
                useModule("com.github.badmannersteam.murglar-plugins:$artifactId:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("catalog") {
            version("murglar-plugins", "7.0")
            version("murglar-emby", "1")

            plugin("murglar-plugin-core", "murglar-gradle-plugin-core").versionRef("murglar-plugins")
            plugin("murglar-plugin-android", "murglar-gradle-plugin-android").versionRef("murglar-plugins")
        }
    }
}

rootProject.name = "murglar-plugin-emby"
include(":emby-core")
include(":emby-android")
