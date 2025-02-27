/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build.fixtures

import org.jetbrains.kotlin.config.IncrementalCompilation

class EnableICFixture(
    private konst enableJvmIC: Boolean = true,
    private konst enableJsIC: Boolean = true
) {
    private var isICEnabledBackup: Boolean = false
    private var isICEnabledForJsBackup: Boolean = false

    fun setUp() {
        isICEnabledBackup = IncrementalCompilation.isEnabledForJvm()
        @Suppress("DEPRECATION")
        IncrementalCompilation.setIsEnabledForJvm(enableJvmIC)

        isICEnabledForJsBackup = IncrementalCompilation.isEnabledForJs()
        @Suppress("DEPRECATION")
        IncrementalCompilation.setIsEnabledForJs(enableJsIC)
    }

    fun tearDown() {
        @Suppress("DEPRECATION")
        IncrementalCompilation.setIsEnabledForJvm(isICEnabledBackup)
        @Suppress("DEPRECATION")
        IncrementalCompilation.setIsEnabledForJs(isICEnabledForJsBackup)
    }
}