/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.psi.stubs.ObjectStubSerializer
import junit.framework.TestCase
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtStubElementTypesExternalIdTest : TestCase() {
    fun testExternalIds() {
        konst clazz = KtStubElementTypes::class.java
        for (declaredField in clazz.declaredFields) {
            konst stubSerializer = declaredField.get(null) as? ObjectStubSerializer<*, *> ?: continue
            konst name = declaredField.name
            konst externalId = stubSerializer.externalId
            assertEquals("kotlin.$name", externalId)
        }
    }
}