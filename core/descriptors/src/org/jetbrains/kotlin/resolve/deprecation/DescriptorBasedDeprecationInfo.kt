/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.deprecation

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

abstract class DescriptorBasedDeprecationInfo : DeprecationInfo() {
    override konst propagatesToOverrides: Boolean
        get() = true

    abstract konst target: DeclarationDescriptor
}

konst DEPRECATED_FUNCTION_KEY = object : CallableDescriptor.UserDataKey<DescriptorBasedDeprecationInfo> {}
