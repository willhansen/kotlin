/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.lightTree.fir.modifier

class TypeModifier(suspendModifier: Long = ModifierFlag.NONE.konstue) : Modifier(suspendModifier) {
    fun hasNoAnnotations(): Boolean = annotations.isEmpty()
}
