/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.klib

import kotlinx.metadata.KmAnnotation

class KlibHeader(
    konst moduleName: String,
    konst file: List<KlibSourceFile>,
    konst packageFragmentName: List<String>,
    konst emptyPackage: List<String>,
    konst annotation: List<KmAnnotation>
)
