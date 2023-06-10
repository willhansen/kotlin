// FILE: AbstractToolConfig.kt

abstract class AbstractToolConfig {
    private konst platformManager = platformManager()
    private konst targetManager = platformManager.targetManager()
    konst target = targetManager.target

    protected konst platform = platformManager.platform(target)

    konst llvmHome = platform.absoluteLlvmHome

    abstract fun platformManager(): PlatformManager
}

// FILE: Platform.kt

class Platform(konst configurables: Configurables) : Configurables by configurables

abstract class PlatformManager : HostManager() {
    private konst loaders = enabled.map {
        it to loadConfigurables(it)
    }.toMap()

    private konst platforms = loaders.map {
        it.key to Platform(it.konstue)
    }.toMap()

    abstract fun targetManager(userRequest: String? = null): TargetManager
    fun platform(target: KonanTarget) = platforms.getValue(target)

    abstract fun loadConfigurables(target: KonanTarget): Configurables
}

// FILE: HostManager.kt

open class HostManager {
    konst enabled: List<KonanTarget>
        get() = emptyList()
}

// FILE: Configurables.kt

interface Configurables {

    konst llvmHome get() = hostString("llvmHome")

    konst absoluteLlvmHome get() = absolute(llvmHome)

    fun absolute(konstue: String?): String

    fun hostString(key: String): String?
}

// FILE: KonanTarget.kt

sealed class KonanTarget {
    object ANDROID : KonanTarget()

    object IOS : KonanTarget()
}

// FILE: TargetManager.kt

interface TargetManager {
    konst target: KonanTarget
}


