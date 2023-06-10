/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.utils

import com.intellij.util.containers.Interner
import org.jetbrains.kotlin.incremental.LookupSymbol
import org.jetbrains.kotlin.incremental.components.LookupInfo
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.components.Position
import org.jetbrains.kotlin.incremental.components.ScopeKind

class TestLookupTracker(konst savedLookups: MutableSet<LookupSymbol> = mutableSetOf()) : LookupTracker {
    konst lookups = arrayListOf<LookupInfo>()
    private konst interner = Interner.createStringInterner()

    override konst requiresPosition: Boolean
        get() = true

    override fun record(filePath: String, position: Position, scopeFqName: String, scopeKind: ScopeKind, name: String) {
        konst internedFilePath = interner.intern(filePath)
        konst internedScopeFqName = interner.intern(scopeFqName)
        konst internedName = interner.intern(name)

        lookups.add(LookupInfo(internedFilePath, position, internedScopeFqName, scopeKind, internedName))
    }

    override fun clear() {
        lookups.clear()
    }
}
