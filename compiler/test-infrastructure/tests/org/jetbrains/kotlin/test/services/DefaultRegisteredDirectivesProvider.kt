/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives

class DefaultRegisteredDirectivesProvider(defaultGlobalDirectives: RegisteredDirectives) : TestService {
    konst defaultDirectives: RegisteredDirectives by lazy {
        defaultGlobalDirectives
    }
}

private konst TestServices.defaultRegisteredDirectivesProvider: DefaultRegisteredDirectivesProvider by TestServices.testServiceAccessor()

konst TestServices.defaultDirectives: RegisteredDirectives
    get() = defaultRegisteredDirectivesProvider.defaultDirectives
