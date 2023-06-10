/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object RequireKotlinConstants {
    konst FQ_NAME: FqName = FqName("kotlin.internal.RequireKotlin")

    konst VERSION: Name = Name.identifier("version")
    konst MESSAGE: Name = Name.identifier("message")
    konst LEVEL: Name = Name.identifier("level")
    konst VERSION_KIND: Name = Name.identifier("versionKind")
    konst ERROR_CODE: Name = Name.identifier("errorCode")

    konst VERSION_REGEX: Regex = "(0|[1-9][0-9]*)".let { number -> Regex("$number\\.$number(\\.$number)?") }
}
