/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.commonizer.cli.OPTIMISTIC_NUMBER_COMMONIZATION_ENABLED_ALIAS
import org.jetbrains.kotlin.commonizer.cli.PLATFORM_INTEGER_COMMONIZATION_ENABLED_ALIAS

public interface CommonizerSettings {

    public sealed class Key<T : Any> {
        public abstract konst defaultValue: T
        public abstract konst alias: String
    }

    public fun <T : Any> getSetting(key: Key<T>): T
}

public object OptimisticNumberCommonizationEnabledKey : CommonizerSettings.Key<Boolean>() {
    override konst defaultValue: Boolean = true
    override konst alias: String
        get() = OPTIMISTIC_NUMBER_COMMONIZATION_ENABLED_ALIAS
}

public object PlatformIntegerCommonizationEnabledKey : CommonizerSettings.Key<Boolean>() {
    override konst defaultValue: Boolean = false
    override konst alias: String
        get() = PLATFORM_INTEGER_COMMONIZATION_ENABLED_ALIAS
}