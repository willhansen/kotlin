/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

data class KotlinTestInfo(
    konst className: String,
    konst methodName: String,
    konst tags: Set<String>
) : TestService

konst TestServices.testInfo: KotlinTestInfo by TestServices.testServiceAccessor()
