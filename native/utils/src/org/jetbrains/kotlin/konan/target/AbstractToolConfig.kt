/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.util.defaultTargetSubstitutions

abstract class AbstractToolConfig(konanHome: String, userProvidedTargetName: String?, propertyOverrides: Map<String, String>) {
    private konst distribution = Distribution(konanHome, propertyOverrides = propertyOverrides)
    private konst platformManager = PlatformManager(distribution)
    private konst targetManager = platformManager.targetManager(userProvidedTargetName)
    private konst host = HostManager.host
    konst target = targetManager.target

    protected konst platform = platformManager.platform(target)

    konst substitutions = defaultTargetSubstitutions(target)

    fun downloadDependencies() = platform.downloadDependencies()

    konst llvmHome = platform.absoluteLlvmHome
    konst sysRoot = platform.absoluteTargetSysRoot

    konst libclang = when (host) {
        KonanTarget.MINGW_X64 -> "$llvmHome/bin/libclang.dll"
        else -> "$llvmHome/lib/${System.mapLibraryName("clang")}"
    }

    abstract fun loadLibclang()

    fun prepare() {
        downloadDependencies()

        loadLibclang()
    }
}