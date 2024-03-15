
kotlin.targets
    .filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    .flatMap { it.binaries }
    .forEach {
        it.linkerOpts("-lsqlite3")
    }
