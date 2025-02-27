/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns

internal class DescriptorsLookup(konst builtIns: KonanBuiltIns) {

    konst interopBuiltIns by lazy {
        InteropBuiltIns(this.builtIns)
    }
}