/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

data class ObjCExportedStubs(
    konst classForwardDeclarations: Set<ObjCClassForwardDeclaration>,
    konst protocolForwardDeclarations: Set<String>,
    konst stubs: List<Stub<*>>
)