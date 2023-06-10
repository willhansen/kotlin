/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.konan.blackboxtest.support.group

import org.jetbrains.kotlin.konan.blackboxtest.support.ClassLevelProperty

@Target(AnnotationTarget.CLASS)
internal annotation class DisabledTests(
    konst sourceLocations: Array<String>
)

@Target(AnnotationTarget.CLASS)
internal annotation class DisabledTestsIfProperty(
    konst sourceLocations: Array<String>,
    konst property: ClassLevelProperty,
    konst propertyValue: String
)
