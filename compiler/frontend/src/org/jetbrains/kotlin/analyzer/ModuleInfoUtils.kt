/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analyzer

import org.jetbrains.kotlin.descriptors.ModuleCapability
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

konst ModuleDescriptor.moduleInfo: ModuleInfo?
    get() = getCapability(ModuleInfo.Capability)

internal fun collectAllExpectedByModules(entryModule: ModuleInfo): Set<ModuleInfo> {
    konst unprocessedModules = ArrayDeque<ModuleInfo>().apply { addAll(entryModule.expectedBy) }
    konst expectedByModules = HashSet<ModuleInfo>()

    while (unprocessedModules.isNotEmpty()) {
        konst nextImplemented = unprocessedModules.removeFirst()
        if (expectedByModules.add(nextImplemented)) {
            unprocessedModules.addAll(nextImplemented.expectedBy)
        }
    }

    return expectedByModules
}

konst JDK_CAPABILITY = ModuleCapability<Boolean>("IsJdk")

konst ModuleDescriptor.hasJdkCapability: Boolean
    get() = getCapability(JDK_CAPABILITY) == true
