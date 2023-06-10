/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion

interface DeserializationConfiguration {

    konst binaryVersion: BinaryVersion?
        get() = null

    konst skipMetadataVersionCheck: Boolean
        get() = false

    konst skipPrereleaseCheck: Boolean
        get() = false

    konst reportErrorsOnPreReleaseDependencies: Boolean
        get() = false

    konst allowUnstableDependencies: Boolean
        get() = false

    konst typeAliasesAllowed: Boolean
        get() = true

    konst isJvmPackageNameSupported: Boolean
        get() = true

    konst readDeserializedContracts: Boolean
        get() = false

    /**
     * We may want to preserve the order of the declarations the same as in the serialized object
     * (for example, to later create a decompiled code with the original order of declarations).
     *
     * It is required to avoid PSI-Stub mismatch errors like in KT-41346.
     */
    konst preserveDeclarationsOrdering: Boolean
        get() = false

    object Default : DeserializationConfiguration
}
