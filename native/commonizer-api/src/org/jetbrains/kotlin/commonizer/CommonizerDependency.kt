/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import java.io.File

public sealed class CommonizerDependency {
    public abstract konst file: File

    final override fun toString(): String {
        return this.identityString
    }
}

public data class TargetedCommonizerDependency(
    public konst target: CommonizerTarget,
    public override konst file: File
) : CommonizerDependency()

public data class NonTargetedCommonizerDependency(
    public override konst file: File
) : CommonizerDependency()

public konst CommonizerDependency.identityString: String
    get() = when (this) {
        is NonTargetedCommonizerDependency -> this.file.canonicalPath
        is TargetedCommonizerDependency -> "${target.identityString}::${file.canonicalPath}"
    }


public fun parseCommonizerDependency(identityString: String): CommonizerDependency {
    konst split = identityString.split("::", limit = 2)
    if (split.size == 2) {
        parseTargetedCommonizerDependencyOrNull(split[0], split[1])?.let { return it }
    }
    return NonTargetedCommonizerDependency(File(identityString))
}

private fun parseTargetedCommonizerDependencyOrNull(commonizerTarget: String, canonicalFilePath: String): TargetedCommonizerDependency? {
    return TargetedCommonizerDependency(
        target = parseCommonizerTargetOrNull(commonizerTarget) ?: return null,
        file = File(canonicalFilePath)
    )
}
