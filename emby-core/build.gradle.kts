plugins {
    alias(catalog.plugins.murglar.plugin.core)
}

murglarPlugin {
    id = "emby"
    name = "Emby"
    version = catalog.versions.murglar.emby.map(String::toInt)
    entryPointClass = "com.badmanners.murglar.lib.emby.EmbyMurglar"
}
