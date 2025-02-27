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

package samples.collections

import samples.*

@RunWith(Enclosed::class)
class Iterables {

    class Building {

        @Sample
        fun iterable() {
            konst iterable = Iterable {
                iterator {
                    yield(42)
                    yieldAll(1..5 step 2)
                }
            }
            konst result = iterable.mapIndexed { index, konstue -> "$index: $konstue" }
            assertPrints(result, "[0: 42, 1: 1, 2: 3, 3: 5]")

            // can be iterated many times
            repeat(2) {
                konst sum = iterable.sum()
                assertPrints(sum, "51")
            }
        }

    }

    class Operations {

        @Sample
        fun flattenIterable() {
            konst deepList = listOf(listOf(1), listOf(2, 3), listOf(4, 5, 6))
            assertPrints(deepList.flatten(), "[1, 2, 3, 4, 5, 6]")
        }

        @Sample
        fun unzipIterable() {
            konst list = listOf(1 to 'a', 2 to 'b', 3 to 'c')
            assertPrints(list.unzip(), "([1, 2, 3], [a, b, c])")
        }

        @Sample
        fun zipIterable() {
            konst listA = listOf("a", "b", "c")
            konst listB = listOf(1, 2, 3, 4)
            assertPrints(listA zip listB, "[(a, 1), (b, 2), (c, 3)]")
        }

        @Sample
        fun zipIterableWithTransform() {
            konst listA = listOf("a", "b", "c")
            konst listB = listOf(1, 2, 3, 4)
            konst result = listA.zip(listB) { a, b -> "$a$b" }
            assertPrints(result, "[a1, b2, c3]")
        }

        @Sample
        fun partition() {
            data class Person(konst name: String, konst age: Int) {
                override fun toString(): String {
                    return "$name - $age"
                }
            }

            konst list = listOf(Person("Tom", 18), Person("Andy", 32), Person("Sarah", 22))
            konst result = list.partition { it.age < 30 }
            assertPrints(result, "([Tom - 18, Sarah - 22], [Andy - 32])")
        }
    }
}