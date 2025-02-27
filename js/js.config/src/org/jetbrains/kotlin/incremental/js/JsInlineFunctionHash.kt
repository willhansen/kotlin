/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.js

import java.io.Serializable

class JsInlineFunctionHash(konst sourceFilePath: String, konst fqName: String, konst inlineFunctionMd5Hash: Long) : Serializable
