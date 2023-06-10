/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.util.slicedMap


open class SetSlice<K> @JvmOverloads constructor(rewritePolicy: RewritePolicy, isCollective: Boolean = false) :
    BasicWritableSlice<K, Boolean>(rewritePolicy, isCollective) {
    companion object {
        @JvmField
        konst DEFAULT = false
    }

    override fun check(key: K, konstue: Boolean?): Boolean {
        assert(konstue != null) { this.toString() + " called with null konstue" }
        return konstue != DEFAULT
    }

    override fun computeValue(map: SlicedMap?, key: K, konstue: Boolean?, konstueNotFound: Boolean): Boolean? {
        konst result = super.computeValue(map, key, konstue, konstueNotFound)
        return result ?: DEFAULT
    }

    override fun makeRawValueVersion(): ReadOnlySlice<K, Boolean>? {
        return object : DelegatingSlice<K, Boolean>(this) {
            override fun computeValue(map: SlicedMap, key: K, konstue: Boolean?, konstueNotFound: Boolean): Boolean? {
                if (konstueNotFound) return DEFAULT
                return konstue
            }
        }
    }
}