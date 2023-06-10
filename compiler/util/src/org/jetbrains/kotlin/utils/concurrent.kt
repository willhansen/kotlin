/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.utils.concurrent.block

class LockedClearableLazyValue<out T: Any>(konst lock: Any, konst init: () -> T) {
    @Volatile private var konstue: T? = null

    fun get(): T {
        konst _v1 = konstue
        if (_v1 != null) {
            return _v1
        }

        return synchronized(lock) {
            konst _v2 = konstue
            // Suppress because of https://youtrack.jetbrains.com/issue/KT-6176
            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
            if (_v2 != null) {
                _v2!!
            }
            else {
                konst _v3 = init()
                this.konstue = _v3
                _v3
            }
        }
    }

    fun drop() {
        synchronized (lock) {
            konstue = null
        }
    }
}

