/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto
import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ideaKpmSchemaInfoProto

internal object IdeaKpmProtoSchema {
    const konst versionMajor = 1
    const konst versionMinor = 0
    const konst versionPatch = 0

    internal konst infos = listOf(
        ideaKpmSchemaInfoProto {
            sinceSchemaVersionMajor = 1
            sinceSchemaVersionMinor = 0
            sinceSchemaVersionPatch = 0
            severity = IdeaKpmSchemaInfoProto.Severity.INFO
            message = "Initial version of IdeaKpmProto*"
        }
    )
}
