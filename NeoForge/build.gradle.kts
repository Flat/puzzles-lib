import fuzs.multiloader.extension.packageName

plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-neoforge")
}

multiloader {
    mixins {
        plugin.set("${project.group}.${project.packageName}.mixin.MixinConfigPluginNeoForgeImpl")
        mixin("MenuProviderWithDataNeoForgeMixin", "ResourceManagerRegistryLoadTaskNeoForgeMixin")
        accessor("EntityNeoForgeAccessor", "PackNeoForgeAccessor")
        clientAccessor("RegisterKeyMappingsEventNeoForgeAccessor")
    }
}
