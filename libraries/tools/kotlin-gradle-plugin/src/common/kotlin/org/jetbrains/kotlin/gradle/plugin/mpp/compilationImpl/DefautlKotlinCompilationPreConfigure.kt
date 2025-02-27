/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl

import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinCompilationImplFactory

internal konst DefaultKotlinCompilationPreConfigure = KotlinCompilationImplFactory.PreConfigure.composite(
    KotlinCompilationK2MultiplatformConfigurator
)