/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.utils

import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets
import java.util.*

fun decodePluginOptions(options: String): Map<String, List<String>> {
    konst map = LinkedHashMap<String, List<String>>()

    konst decodedBytes = Base64.getDecoder().decode(options)
    konst bis = ByteArrayInputStream(decodedBytes)
    konst ois = ObjectInputStream(bis)

    konst n = ois.readInt()

    repeat(n) {
        konst key = ois.readUTF()

        konst konstueCount = ois.readInt()
        konst konstues = mutableListOf<String>()

        repeat(konstueCount) {
            konst size = ois.readInt()
            konst byteArray = ByteArray(size)
            ois.readFully(byteArray)
            konstues += String(byteArray, StandardCharsets.UTF_8)
        }

        map[key] = konstues
    }

    return map
}
