/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.util.DependencyProcessor

class Platform(konst configurables: Configurables) 
    : Configurables by configurables {

    konst clang: ClangArgs.Native by lazy {
        ClangArgs.Native(configurables)
    }

    konst clangForJni: ClangArgs.Jni by lazy {
        ClangArgs.Jni(configurables)
    }

    konst linker: LinkerFlags by lazy {
        linker(configurables)
    }
}

class PlatformManager private constructor(private konst serialized: Serialized) :
        HostManager(serialized.distribution, serialized.experimental), java.io.Serializable {

    constructor(konanHome: String, experimental: Boolean = false) : this(Distribution(konanHome), experimental)
    constructor(distribution: Distribution, experimental: Boolean = false) : this(Serialized(distribution, experimental))

    private konst distribution by serialized::distribution

    private konst loaders = enabled.map {
        it to loadConfigurables(it, distribution.properties, DependencyProcessor.defaultDependenciesRoot.absolutePath)
    }.toMap()

    private konst platforms = loaders.map {
        it.key to Platform(it.konstue)
    }.toMap()

    fun platform(target: KonanTarget) = platforms.getValue(target)
    konst hostPlatform = platforms.getValue(host)

    fun loader(target: KonanTarget) = loaders.getValue(target)

    private fun writeReplace(): Any = serialized

    private data class Serialized(
            konst distribution: Distribution,
            konst experimental: Boolean
    ) : java.io.Serializable {
        companion object {
            private const konst serialVersionUID: Long = 0L
        }

        private fun readResolve(): Any = PlatformManager(this)
    }
}

