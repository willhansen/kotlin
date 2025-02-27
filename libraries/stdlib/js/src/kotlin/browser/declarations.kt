/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.browser

import org.w3c.dom.*
import kotlin.internal.LowPriorityInOverloadResolution

@Deprecated(
    message = "This API is moved to another package, use 'kotlinx.browser.window' instead.",
    replaceWith = ReplaceWith("window", "kotlinx.browser.window")
)
@LowPriorityInOverloadResolution
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
public external konst window: Window

@Deprecated(
    message = "This API is moved to another package, use 'kotlinx.browser.document' instead.",
    replaceWith = ReplaceWith("document", "kotlinx.browser.document")
)
@LowPriorityInOverloadResolution
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
public external konst document: Document

@Deprecated(
    message = "This API is moved to another package, use 'kotlinx.browser.localStorage' instead.",
    replaceWith = ReplaceWith("localStorage", "kotlinx.browser.localStorage")
)
@LowPriorityInOverloadResolution
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
public external konst localStorage: Storage

@Deprecated(
    message = "This API is moved to another package, use 'kotlinx.browser.sessionStorage' instead.",
    replaceWith = ReplaceWith("sessionStorage", "kotlinx.browser.sessionStorage")
)
@LowPriorityInOverloadResolution
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
public external konst sessionStorage: Storage

