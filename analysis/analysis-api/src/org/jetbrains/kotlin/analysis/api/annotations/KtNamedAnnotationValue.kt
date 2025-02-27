/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.annotations

import org.jetbrains.kotlin.name.Name

/**
 * Name-Value pair which is used as annotation argument.
 */
public data class KtNamedAnnotationValue(konst name: Name, konst expression: KtAnnotationValue)