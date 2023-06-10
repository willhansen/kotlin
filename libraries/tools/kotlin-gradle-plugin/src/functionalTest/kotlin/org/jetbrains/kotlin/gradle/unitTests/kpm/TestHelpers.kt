/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests.kpm

import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmFragment
import kotlin.reflect.KClass

internal konst <T : GradleKpmFragment> KClass<T>.decoratedClassCanonicalName: String get() = java.decoratedClassCanonicalName

internal konst <T : GradleKpmFragment> Class<T>.decoratedClassCanonicalName: String get() = "${canonicalName}_Decorated"
