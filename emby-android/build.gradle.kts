plugins {
    alias(catalog.plugins.murglar.plugin.android)
}

murglarAndroidPlugin {
    id = "emby"
    name = "Emby"
    version = catalog.versions.murglar.emby.map(String::toInt)
    entryPointClass = "com.badmanners.murglar.lib.emby.EmbyMurglar"
}

dependencies {
    implementation(project(":emby-core"))
}
