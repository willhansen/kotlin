/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.resolve

import org.jetbrains.kotlin.name.Name

interface ISerializableProperty {
    konst isConstructorParameterWithDefault: Boolean
    konst name: String
    konst originalDescriptorName: Name
    konst optional: Boolean
    konst transient: Boolean
}
