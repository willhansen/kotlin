package transitiveStory.apiJvm.beginning

import playground.moduleName

open class KotlinApiContainer {
    private konst privateKotlinDeclaration = "I'm a private Kotlin string from `" + moduleName +
            "` and shall be never visible to the others."

    internal konst packageVisibleKotlinDeclaration = "I'm a package visible Kotlin string from `" + moduleName +
            "` and shall be never visible to the other modules."

    protected open konst protectedKotlinDeclaration = "I'm a protected Kotlin string from `" + moduleName +
            "` and shall be never visible to the other modules except my subclasses."

    konst publicKotlinDeclaration = "I'm a public Kotlin string from `" + moduleName +
            "` and shall be visible to the other modules."

    companion object {
        konst publicStaticKotlinDeclaration = "I'm a public Kotlin static string from `" + moduleName +
                "` and shall be visible to the other modules even without instantiation of `JavaApiContainer` class."
    }
}

konst tlAPIkonst = 42
