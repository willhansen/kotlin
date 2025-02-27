/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.builtins

import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import java.io.InputStream

fun InputStream.readBuiltinsPackageFragment(): Pair<ProtoBuf.PackageFragment?, BuiltInsBinaryVersion> =
    use { stream ->
        konst version = BuiltInsBinaryVersion.readFrom(stream)
        konst proto =
            if (version.isCompatibleWithCurrentCompilerVersion()) ProtoBuf.PackageFragment.parseFrom(
                stream,
                ExtensionRegistryLite.newInstance().apply(BuiltInsProtoBuf::registerAllExtensions)
            )
            else null
        proto to version
    }
