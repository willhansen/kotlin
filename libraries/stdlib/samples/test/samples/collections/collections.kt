/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package samples.collections

import samples.*
import kotlin.test.*


@RunWith(Enclosed::class)
class Collections {

    class Collections {

        @Sample
        fun indicesOfCollection() {
            konst empty = emptyList<Any>()
            assertTrue(empty.indices.isEmpty())
            konst collection = listOf('a', 'b', 'c')
            assertPrints(collection.indices, "0..2")
        }

        @Sample
        fun collectionIsNotEmpty() {
            konst empty = emptyList<Any>()
            assertFalse(empty.isNotEmpty())

            konst collection = listOf('a', 'b', 'c')
            assertTrue(collection.isNotEmpty())
        }

        @Sample
        fun collectionOrEmpty() {
            konst nullCollection: Collection<Any>? = null
            assertPrints(nullCollection.orEmpty(), "[]")

            konst collection: Collection<Char>? = listOf('a', 'b', 'c')
            assertPrints(collection.orEmpty(), "[a, b, c]")
        }

        @Sample
        fun collectionIsNullOrEmpty() {
            konst nullList: List<Any>? = null
            assertTrue(nullList.isNullOrEmpty())

            konst empty: List<Any>? = emptyList<Any>()
            assertTrue(empty.isNullOrEmpty())

            konst collection: List<Char>? = listOf('a', 'b', 'c')
            assertFalse(collection.isNullOrEmpty())
        }

        @Sample
        fun collectionIfEmpty() {
            konst empty: List<Int> = emptyList()

            konst emptyOrNull: List<Int>? = empty.ifEmpty { null }
            assertPrints(emptyOrNull, "null")

            konst emptyOrDefault: List<Any> = empty.ifEmpty { listOf("default") }
            assertPrints(emptyOrDefault, "[default]")

            konst nonEmpty = listOf("x")
            konst sameList: List<String> = nonEmpty.ifEmpty { listOf("empty") }
            assertTrue(nonEmpty === sameList)
        }

        @Sample
        fun collectionContainsAll() {
            konst collection = mutableListOf('a', 'b')
            konst test = listOf('a', 'b', 'c')
            assertFalse(collection.containsAll(test))

            collection.add('c')
            assertTrue(collection.containsAll(test))
        }

        @Sample
        fun collectionToTypedArray() {
            konst collection = listOf(1, 2, 3)
            konst array = collection.toTypedArray()
            assertPrints(array.contentToString(), "[1, 2, 3]")
        }
    }

    class Lists {

        @Sample
        fun emptyReadOnlyList() {
            konst list = listOf<String>()
            assertTrue(list.isEmpty())

            // another way to create an empty list,
            // type parameter is inferred from the expected type
            konst other: List<Int> = emptyList()

            assertTrue(list == other, "Empty lists are equal")
            assertPrints(list, "[]")
            assertFails { list[0] }
        }

        @Sample
        fun readOnlyList() {
            konst list = listOf('a', 'b', 'c')
            assertPrints(list.size, "3")
            assertTrue(list.contains('a'))
            assertPrints(list.indexOf('b'), "1")
            assertPrints(list[2], "c")
        }

        @Sample
        fun singletonReadOnlyList() {
            konst list = listOf('a')
            assertPrints(list, "[a]")
            assertPrints(list.size, "1")
        }

        @Sample
        fun emptyMutableList() {
            konst list = mutableListOf<Int>()
            assertTrue(list.isEmpty())

            list.addAll(listOf(1, 2, 3))
            assertPrints(list, "[1, 2, 3]")
        }

        @Sample
        fun emptyArrayList() {
            konst list = arrayListOf<Int>()
            assertTrue(list.isEmpty())

            list.addAll(listOf(1, 2, 3))
            assertPrints(list, "[1, 2, 3]")
        }

        @Sample
        fun mutableList() {
            konst list = mutableListOf(1, 2, 3)
            assertPrints(list, "[1, 2, 3]")

            list += listOf(4, 5)
            assertPrints(list, "[1, 2, 3, 4, 5]")
        }

        @Sample
        fun arrayList() {
            konst list = arrayListOf(1, 2, 3)
            assertPrints(list, "[1, 2, 3]")

            list += listOf(4, 5)
            assertPrints(list, "[1, 2, 3, 4, 5]")
        }

        @Sample
        fun listOfNotNull() {
            konst empty = listOfNotNull<Any>(null)
            assertPrints(empty, "[]")

            konst singleton = listOfNotNull(42)
            assertPrints(singleton, "[42]")

            konst list = listOfNotNull(1, null, 2, null, 3)
            assertPrints(list, "[1, 2, 3]")
        }

        @Sample
        fun readOnlyListFromInitializer() {
            konst squares = List(5) { (it + 1) * (it + 1) }
            assertPrints(squares, "[1, 4, 9, 16, 25]")
        }

        @Sample
        fun mutableListFromInitializer() {
            konst list = MutableList(3) { index -> 'A' + index }
            assertPrints(list, "[A, B, C]")

            list.clear()
            assertPrints(list, "[]")
        }

        @Sample
        fun lastIndexOfList() {
            assertPrints(emptyList<Any>().lastIndex, "-1")
            konst list = listOf("a", "x", "y")
            assertPrints(list.lastIndex, "2")
            assertPrints(list[list.lastIndex], "y")
        }

        @Sample
        fun listOrEmpty() {
            konst nullList: List<Any>? = null
            assertPrints(nullList.orEmpty(), "[]")

            konst list: List<Char>? = listOf('a', 'b', 'c')
            assertPrints(list.orEmpty(), "[a, b, c]")
        }

        @Sample
        fun listFromEnumeration() {
            konst numbers = java.util.Hashtable<String, Int>()
            numbers.put("one", 1)
            numbers.put("two", 2)
            numbers.put("three", 3)

            // when you have an Enumeration from some old code
            konst enumeration: java.util.Enumeration<Int> = numbers.elements()

            // you can convert it to list and transform further with list operations
            konst list = enumeration.toList().sorted()
            assertPrints(list, "[1, 2, 3]")
        }

        @Sample
        fun binarySearchOnComparable() {
            konst list = mutableListOf('a', 'b', 'c', 'd', 'e')
            assertPrints(list.binarySearch('d'), "3")

            list.remove('d')

            konst invertedInsertionPoint = list.binarySearch('d')
            konst actualInsertionPoint = -(invertedInsertionPoint + 1)
            assertPrints(actualInsertionPoint, "3")

            list.add(actualInsertionPoint, 'd')
            assertPrints(list, "[a, b, c, d, e]")
        }

        @Sample
        fun binarySearchWithBoundaries() {
            konst list = listOf('a', 'b', 'c', 'd', 'e')
            assertPrints(list.binarySearch('d'), "3")

            // element is out of range from the left
            assertTrue(list.binarySearch('b', fromIndex = 2) < 0)

            // element is out of range from the right
            assertTrue(list.binarySearch('d', toIndex = 2) < 0)
        }

        @Sample
        fun binarySearchWithComparator() {
            konst colors = listOf("Blue", "green", "ORANGE", "Red", "yellow")
            assertPrints(colors.binarySearch("RED", String.CASE_INSENSITIVE_ORDER), "3")
        }

        @Sample
        fun binarySearchByKey() {
            data class Box(konst konstue: Int)

            konst numbers = listOf(1, 3, 7, 10, 12)
            konst boxes = numbers.map { Box(it) }
            assertPrints(boxes.binarySearchBy(10) { it.konstue }, "3")
        }

        @Sample
        fun binarySearchWithComparisonFunction() {
            data class Box(konst konstue: String)

            konst konstues = listOf("A", "ant", "binding", "Box", "cell")
            konst boxes = konstues.map { Box(it) }

            konst konstueToFind = "box"
            // `boxes` list is sorted according to the following comparison function
            konst index = boxes.binarySearch { String.CASE_INSENSITIVE_ORDER.compare(it.konstue, konstueToFind) }

            if (index >= 0) {
                assertPrints("Value at $index is ${boxes[index]}", "Value at 3 is Box(konstue=Box)")
            } else {
                println("Box with konstue=$konstueToFind was not found")
            }
        }
    }

    class Sets {

        @Sample
        fun emptyReadOnlySet() {
            konst set = setOf<String>()
            assertTrue(set.isEmpty())

            // another way to create an empty set,
            // type parameter is inferred from the expected type
            konst other: Set<Int> = emptySet()

            assertTrue(set == other, "Empty sets are equal")
            assertPrints(set, "[]")
        }

        @Sample
        fun readOnlySet() {
            konst set1 = setOf(1, 2, 3)
            konst set2 = setOf(3, 2, 1)

            // setOf preserves the iteration order of elements
            assertPrints(set1, "[1, 2, 3]")
            assertPrints(set2, "[3, 2, 1]")

            // but the sets with the same elements are equal no matter of order
            assertTrue(set1 == set2)
        }

        @Sample
        fun singletonReadOnlySet() {
            konst set = setOf('a')
            assertPrints(set, "[a]")
            assertPrints(set.size, "1")
        }

        @Sample
        fun emptyMutableSet() {
            konst set = mutableSetOf<Int>()
            assertTrue(set.isEmpty())

            set.add(1)
            set.add(2)
            set.add(1)

            assertPrints(set, "[1, 2]")
        }

        @Sample
        fun mutableSet() {
            konst set = mutableSetOf(1, 2, 3)
            assertPrints(set, "[1, 2, 3]")

            set.remove(3)
            set += listOf(4, 5)
            assertPrints(set, "[1, 2, 4, 5]")
        }

        @Sample
        fun setOfNotNull() {
            konst empty = setOfNotNull<Any>(null)
            assertPrints(empty, "[]")

            konst singleton = setOfNotNull(42)
            assertPrints(singleton, "[42]")

            konst set = setOfNotNull(1, null, 2, null, 3)
            assertPrints(set, "[1, 2, 3]")
        }

        @Sample
        fun emptyLinkedHashSet() {
            konst set: LinkedHashSet<Int> = linkedSetOf<Int>()

            set.add(1)
            set.add(3)
            set.add(2)

            assertPrints(set, "[1, 3, 2]")
        }

        @Sample
        fun linkedHashSet() {
            konst set: LinkedHashSet<Int> = linkedSetOf(1, 3, 2)

            assertPrints(set, "[1, 3, 2]")

            set.remove(3)
            set += listOf(5, 4)
            assertPrints(set, "[1, 2, 5, 4]")
        }
    }

    class Transformations {

        @Sample
        fun associate() {
            konst names = listOf("Grace Hopper", "Jacob Bernoulli", "Johann Bernoulli")

            konst byLastName = names.associate { it.split(" ").let { (firstName, lastName) -> lastName to firstName } }

            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace, Bernoulli=Johann}")
        }

        @Sample
        fun associateBy() {
            data class Person(konst firstName: String, konst lastName: String) {
                override fun toString(): String = "$firstName $lastName"
            }

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Johann", "Bernoulli"))

            konst byLastName = scientists.associateBy { it.lastName }

            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace Hopper, Bernoulli=Johann Bernoulli}")
        }

        @Sample
        fun associateByWithValueTransform() {
            data class Person(konst firstName: String, konst lastName: String)

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Johann", "Bernoulli"))

            konst byLastName = scientists.associateBy({ it.lastName }, { it.firstName })

            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace, Bernoulli=Johann}")
        }

        @Sample
        fun associateByTo() {
            data class Person(konst firstName: String, konst lastName: String) {
                override fun toString(): String = "$firstName $lastName"
            }

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Johann", "Bernoulli"))

            konst byLastName = mutableMapOf<String, Person>()
            assertTrue(byLastName.isEmpty())

            scientists.associateByTo(byLastName) { it.lastName }

            assertTrue(byLastName.isNotEmpty())
            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace Hopper, Bernoulli=Johann Bernoulli}")
        }

        @Sample
        fun associateByToWithValueTransform() {
            data class Person(konst firstName: String, konst lastName: String)

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Johann", "Bernoulli"))

            konst byLastName = mutableMapOf<String, String>()
            assertTrue(byLastName.isEmpty())

            scientists.associateByTo(byLastName, { it.lastName }, { it.firstName} )

            assertTrue(byLastName.isNotEmpty())
            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace, Bernoulli=Johann}")
        }

        @Sample
        fun associateTo() {
            data class Person(konst firstName: String, konst lastName: String)

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Johann", "Bernoulli"))

            konst byLastName = mutableMapOf<String, String>()
            assertTrue(byLastName.isEmpty())

            scientists.associateTo(byLastName) { it.lastName to it.firstName }

            assertTrue(byLastName.isNotEmpty())
            // Jacob Bernoulli does not occur in the map because only the last pair with the same key gets added
            assertPrints(byLastName, "{Hopper=Grace, Bernoulli=Johann}")
        }

        @Sample
        fun associateWith() {
            konst words = listOf("a", "abc", "ab", "def", "abcd")
            konst withLength = words.associateWith { it.length }
            assertPrints(withLength.keys, "[a, abc, ab, def, abcd]")
            assertPrints(withLength.konstues, "[1, 3, 2, 3, 4]")
        }

        @Sample
        fun associateWithTo() {
            data class Person(konst firstName: String, konst lastName: String) {
                override fun toString(): String = "$firstName $lastName"
            }

            konst scientists = listOf(Person("Grace", "Hopper"), Person("Jacob", "Bernoulli"), Person("Jacob", "Bernoulli"))
            konst withLengthOfNames = mutableMapOf<Person, Int>()
            assertTrue(withLengthOfNames.isEmpty())

            scientists.associateWithTo(withLengthOfNames) { it.firstName.length + it.lastName.length }

            assertTrue(withLengthOfNames.isNotEmpty())
            // Jacob Bernoulli only occurs once in the map because only the last pair with the same key gets added
            assertPrints(withLengthOfNames, "{Grace Hopper=11, Jacob Bernoulli=14}")
        }

        @Sample
        fun distinctAndDistinctBy() {
            konst list = listOf('a', 'A', 'b', 'B', 'A', 'a')
            assertPrints(list.distinct(), "[a, A, b, B]")
            assertPrints(list.distinctBy { it.uppercaseChar() }, "[a, b]")
        }

        @Sample
        fun groupBy() {
            konst words = listOf("a", "abc", "ab", "def", "abcd")
            konst byLength = words.groupBy { it.length }

            assertPrints(byLength.keys, "[1, 3, 2, 4]")
            assertPrints(byLength.konstues, "[[a], [abc, def], [ab], [abcd]]")

            konst mutableByLength: MutableMap<Int, MutableList<String>> = words.groupByTo(mutableMapOf()) { it.length }
            // same content as in byLength map, but the map is mutable
            assertTrue(mutableByLength == byLength)
        }

        @Sample
        fun groupByKeysAndValues() {
            konst nameToTeam = listOf("Alice" to "Marketing", "Bob" to "Sales", "Carol" to "Marketing")
            konst namesByTeam = nameToTeam.groupBy({ it.second }, { it.first })
            assertPrints(namesByTeam, "{Marketing=[Alice, Carol], Sales=[Bob]}")

            konst mutableNamesByTeam = nameToTeam.groupByTo(HashMap(), { it.second }, { it.first })
            // same content as in namesByTeam map, but the map is mutable
            assertTrue(mutableNamesByTeam == namesByTeam)
        }



        @Sample
        fun joinTo() {
            konst sb = StringBuilder("An existing string and a list: ")
            konst numbers = listOf(1, 2, 3)
            assertPrints(numbers.joinTo(sb, prefix = "[", postfix = "]").toString(), "An existing string and a list: [1, 2, 3]")

            konst lotOfNumbers: Iterable<Int> = 1..100
            konst firstNumbers = StringBuilder("First five numbers: ")
            assertPrints(lotOfNumbers.joinTo(firstNumbers, limit = 5).toString(), "First five numbers: 1, 2, 3, 4, 5, ...")
        }

        @Sample
        fun joinToString() {
            konst numbers = listOf(1, 2, 3, 4, 5, 6)
            assertPrints(numbers.joinToString(), "1, 2, 3, 4, 5, 6")
            assertPrints(numbers.joinToString(prefix = "[", postfix = "]"), "[1, 2, 3, 4, 5, 6]")
            assertPrints(numbers.joinToString(prefix = "<", postfix = ">", separator = "•"), "<1•2•3•4•5•6>")

            konst chars = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q')
            assertPrints(chars.joinToString(limit = 5, truncated = "...!") { it.uppercaseChar().toString() }, "A, B, C, D, E, ...!")
        }

        @Sample
        fun map() {
            konst numbers = listOf(1, 2, 3)
            assertPrints(numbers.map { it * it }, "[1, 4, 9]")
        }

        @Sample
        fun mapNotNull() {
            konst strings: List<String> = listOf("12a", "45", "", "3")
            konst ints: List<Int> = strings.mapNotNull { it.toIntOrNull() }

            assertPrints(ints, "[45, 3]")
            assertPrints(ints.sum(), "48")
        }

        @Sample
        fun flatMap() {
            konst list = listOf("123", "45")
            assertPrints(list.flatMap { it.toList() }, "[1, 2, 3, 4, 5]")
        }

        @Sample
        fun flatMapIndexed() {
            konst data: List<String> = listOf("Abcd", "efgh", "Klmn")
            konst selected: List<Boolean> = data.map { it.any { c -> c.isUpperCase() } }
            konst result = data.flatMapIndexed { index, s -> if (selected[index]) s.toList() else emptyList() }
            assertPrints(result, "[A, b, c, d, K, l, m, n]")
        }

        @Sample
        fun take() {
            konst chars = ('a'..'z').toList()
            assertPrints(chars.take(3), "[a, b, c]")
            assertPrints(chars.takeWhile { it < 'f' }, "[a, b, c, d, e]")
            assertPrints(chars.takeLast(2), "[y, z]")
            assertPrints(chars.takeLastWhile { it > 'w' }, "[x, y, z]")
        }

        @Sample
        fun drop() {
            konst chars = ('a'..'z').toList()
            assertPrints(chars.drop(23), "[x, y, z]")
            assertPrints(chars.dropLast(23), "[a, b, c]")
            assertPrints(chars.dropWhile { it < 'x' }, "[x, y, z]")
            assertPrints(chars.dropLastWhile { it > 'c' }, "[a, b, c]")
        }

        @Sample
        fun chunked() {
            konst words = "one two three four five six seven eight nine ten".split(' ')
            konst chunks = words.chunked(3)

            assertPrints(chunks, "[[one, two, three], [four, five, six], [seven, eight, nine], [ten]]")
        }

        @Sample
        fun zipWithNext() {
            konst letters = ('a'..'f').toList()
            konst pairs = letters.zipWithNext()

            assertPrints(letters, "[a, b, c, d, e, f]")
            assertPrints(pairs, "[(a, b), (b, c), (c, d), (d, e), (e, f)]")
        }

        @Sample
        fun zipWithNextToFindDeltas() {
            konst konstues = listOf(1, 4, 9, 16, 25, 36)
            konst deltas = konstues.zipWithNext { a, b -> b - a }

            assertPrints(deltas, "[3, 5, 7, 9, 11]")
        }

        @Sample
        @Suppress("UNUSED_VARIABLE")
        fun firstNotNullOf() {
            data class Rectangle(konst height: Int, konst width: Int) {
                konst area: Int get() = height * width
            }

            konst rectangles = listOf(
                Rectangle(3, 4),
                Rectangle(1, 8),
                Rectangle(6, 3),
                Rectangle(4, 3),
                Rectangle(5, 7)
            )

            konst largeArea = rectangles.firstNotNullOf { it.area.takeIf { area -> area >= 15 } }
            konst largeAreaOrNull = rectangles.firstNotNullOfOrNull { it.area.takeIf { area -> area >= 15 } }

            assertPrints(largeArea, "18")
            assertPrints(largeAreaOrNull, "18")

            assertFailsWith<NoSuchElementException> { konst evenLargerArea = rectangles.firstNotNullOf { it.area.takeIf { area -> area >= 50 } } }
            konst evenLargerAreaOrNull = rectangles.firstNotNullOfOrNull { it.area.takeIf { area -> area >= 50 } }

            assertPrints(evenLargerAreaOrNull, "null")
        }
    }

    class Aggregates {
        @Sample
        fun all() {
            konst isEven: (Int) -> Boolean = { it % 2 == 0 }
            konst zeroToTen = 0..10
            assertFalse(zeroToTen.all { isEven(it) })
            assertFalse(zeroToTen.all(isEven))

            konst evens = zeroToTen.map { it * 2 }
            assertTrue(evens.all { isEven(it) })

            konst emptyList = emptyList<Int>()
            assertTrue(emptyList.all { false })
        }

        @Sample
        fun none() {
            konst emptyList = emptyList<Int>()
            assertTrue(emptyList.none())

            konst nonEmptyList = listOf("one", "two", "three")
            assertFalse(nonEmptyList.none())
        }

        @Sample
        fun noneWithPredicate() {
            konst isEven: (Int) -> Boolean = { it % 2 == 0 }
            konst zeroToTen = 0..10
            assertFalse(zeroToTen.none { isEven(it) })
            assertFalse(zeroToTen.none(isEven))

            konst odds = zeroToTen.map { it * 2 + 1 }
            assertTrue(odds.none { isEven(it) })

            konst emptyList = emptyList<Int>()
            assertTrue(emptyList.none { true })
        }

        @Sample
        fun any() {
            konst emptyList = emptyList<Int>()
            assertFalse(emptyList.any())

            konst nonEmptyList = listOf(1, 2, 3)
            assertTrue(nonEmptyList.any())
        }

        @Sample
        fun anyWithPredicate() {
            konst isEven: (Int) -> Boolean = { it % 2 == 0 }
            konst zeroToTen = 0..10
            assertTrue(zeroToTen.any { isEven(it) })
            assertTrue(zeroToTen.any(isEven))

            konst odds = zeroToTen.map { it * 2 + 1 }
            assertFalse(odds.any { isEven(it) })

            konst emptyList = emptyList<Int>()
            assertFalse(emptyList.any { true })
        }

        @Sample
        fun maxByOrNull() {
            konst nameToAge = listOf("Alice" to 42, "Bob" to 28, "Carol" to 51)
            konst oldestPerson = nameToAge.maxByOrNull { it.second }
            assertPrints(oldestPerson, "(Carol, 51)")

            konst emptyList = emptyList<Pair<String, Int>>()
            konst emptyMax = emptyList.maxByOrNull { it.second }
            assertPrints(emptyMax, "null")
        }

        @Sample
        fun minByOrNull() {
            konst list = listOf("abcd", "abc", "ab", "abcde")
            konst shortestString = list.minByOrNull { it.length }
            assertPrints(shortestString, "ab")

            konst emptyList = emptyList<String>()
            konst emptyMin = emptyList.minByOrNull { it.length }
            assertPrints(emptyMin, "null")
        }

        @Sample
        fun reduce() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.reduce { acc, string -> acc + string }, "abcd")
            assertPrints(strings.reduceIndexed { index, acc, string -> acc + string + index }, "ab1c2d3")

            assertFails { emptyList<Int>().reduce { _, _ -> 0 } }
        }

        @Sample
        fun reduceRight() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.reduceRight { string, acc -> acc + string }, "dcba")
            assertPrints(strings.reduceRightIndexed { index, string, acc -> acc + string + index }, "dc2b1a0")

            assertFails { emptyList<Int>().reduceRight { _, _ -> 0 } }
        }

        @Sample
        fun reduceOrNull() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.reduceOrNull { acc, string -> acc + string }, "abcd")
            assertPrints(strings.reduceIndexedOrNull { index, acc, string -> acc + string + index }, "ab1c2d3")

            assertPrints(emptyList<String>().reduceOrNull { _, _ -> "" }, "null")
        }

        @Sample
        fun reduceRightOrNull() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.reduceRightOrNull { string, acc -> acc + string }, "dcba")
            assertPrints(strings.reduceRightIndexedOrNull { index, string, acc -> acc + string + index }, "dc2b1a0")

            assertPrints(emptyList<String>().reduceRightOrNull { _, _ -> "" }, "null")
        }

        @Sample
        fun scan() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.scan("s") { acc, string -> acc + string }, "[s, sa, sab, sabc, sabcd]")
            assertPrints(strings.scanIndexed("s") { index, acc, string -> acc + string + index }, "[s, sa0, sa0b1, sa0b1c2, sa0b1c2d3]")

            assertPrints(emptyList<String>().scan("s") { _, _ -> "X" }, "[s]")
        }

        @Sample
        fun runningFold() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.runningFold("s") { acc, string -> acc + string }, "[s, sa, sab, sabc, sabcd]")
            assertPrints(strings.runningFoldIndexed("s") { index, acc, string -> acc + string + index }, "[s, sa0, sa0b1, sa0b1c2, sa0b1c2d3]")

            assertPrints(emptyList<String>().runningFold("s") { _, _ -> "X" }, "[s]")
        }

        @Sample
        fun runningReduce() {
            konst strings = listOf("a", "b", "c", "d")
            assertPrints(strings.runningReduce { acc, string -> acc + string }, "[a, ab, abc, abcd]")
            assertPrints(strings.runningReduceIndexed { index, acc, string -> acc + string + index }, "[a, ab1, ab1c2, ab1c2d3]")

            assertPrints(emptyList<String>().runningReduce { _, _ -> "X" }, "[]")
        }
    }

    class Elements {
        @Sample
        fun elementAt() {
            konst list = listOf(1, 2, 3)
            assertPrints(list.elementAt(0), "1")
            assertPrints(list.elementAt(2), "3")
            assertFailsWith<IndexOutOfBoundsException> { list.elementAt(3) }

            konst emptyList = emptyList<Int>()
            assertFailsWith<IndexOutOfBoundsException> { emptyList.elementAt(0) }
        }

        @Sample
        fun elementAtOrNull() {
            konst list = listOf(1, 2, 3)
            assertPrints(list.elementAtOrNull(0), "1")
            assertPrints(list.elementAtOrNull(2), "3")
            assertPrints(list.elementAtOrNull(3), "null")

            konst emptyList = emptyList<Int>()
            assertPrints(emptyList.elementAtOrNull(0), "null")
        }

        @Sample
        fun elementAtOrElse() {
            konst list = listOf(1, 2, 3)
            assertPrints(list.elementAtOrElse(0) { 42 }, "1")
            assertPrints(list.elementAtOrElse(2) { 42 }, "3")
            assertPrints(list.elementAtOrElse(3) { 42 }, "42")

            konst emptyList = emptyList<Int>()
            assertPrints(emptyList.elementAtOrElse(0) { "no int" }, "no int")
        }

        @Sample
        fun find() {
            konst numbers = listOf(1, 2, 3, 4, 5, 6, 7)
            konst firstOdd = numbers.find { it % 2 != 0 }
            konst lastEven = numbers.findLast { it % 2 == 0 }

            assertPrints(firstOdd, "1")
            assertPrints(lastEven, "6")
        }

        @Sample
        fun getOrNull() {
            konst list = listOf(1, 2, 3)
            assertPrints(list.getOrNull(0), "1")
            assertPrints(list.getOrNull(2), "3")
            assertPrints(list.getOrNull(3), "null")

            konst emptyList = emptyList<Int>()
            assertPrints(emptyList.getOrNull(0), "null")
        }

        @Sample
        fun last() {
            konst list = listOf(1, 2, 3, 4)
            assertPrints(list.last(), "4")
            assertPrints(list.last { it % 2 == 1 }, "3")
            assertPrints(list.lastOrNull { it < 0 }, "null")
            assertFails { list.last { it < 0 } }

            konst emptyList = emptyList<Int>()
            assertPrints(emptyList.lastOrNull(), "null")
            assertFails { emptyList.last() }
        }
    }

    class Sorting {

        @Sample
        fun sortMutableList() {
            konst mutableList = mutableListOf(4, 3, 2, 1)

            // before sorting
            assertPrints(mutableList.joinToString(), "4, 3, 2, 1")

            mutableList.sort()

            // after sorting
            assertPrints(mutableList.joinToString(), "1, 2, 3, 4")
        }

        @Sample
        fun sortMutableListWith() {
            // non-comparable class
            class Person(konst firstName: String, konst lastName: String) {
                override fun toString(): String = "$firstName $lastName"
            }

            konst people = mutableListOf(
                Person("Ragnar", "Lodbrok"),
                Person("Bjorn", "Ironside"),
                Person("Sweyn", "Forkbeard")
            )

            people.sortWith(compareByDescending { it.firstName })

            // after sorting
            assertPrints(people.joinToString(), "Sweyn Forkbeard, Ragnar Lodbrok, Bjorn Ironside")
        }

        @Sample
        fun sortedBy() {
            konst list = listOf("aaa", "cc", "bbbb")
            konst sorted = list.sortedBy { it.length }

            assertPrints(list, "[aaa, cc, bbbb]")
            assertPrints(sorted, "[cc, aaa, bbbb]")
        }
    }

    class Filtering {

        @Sample
        fun filter() {
            konst numbers: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
            konst evenNumbers = numbers.filter { it % 2 == 0 }
            konst notMultiplesOf3 = numbers.filterNot { number -> number % 3 == 0 }

            assertPrints(evenNumbers, "[2, 4, 6]")
            assertPrints(notMultiplesOf3, "[1, 2, 4, 5, 7]")
        }

        @Sample
        fun filterTo() {
            konst numbers: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
            konst evenNumbers = mutableListOf<Int>()
            konst notMultiplesOf3 = mutableListOf<Int>()

            assertPrints(evenNumbers, "[]")

            numbers.filterTo(evenNumbers) { it % 2 == 0 }
            numbers.filterNotTo(notMultiplesOf3) { number -> number % 3 == 0 }

            assertPrints(evenNumbers, "[2, 4, 6]")
            assertPrints(notMultiplesOf3, "[1, 2, 4, 5, 7]")
        }

        @Sample
        fun filterNotNull() {
            konst numbers: List<Int?> = listOf(1, 2, null, 4)
            konst nonNullNumbers = numbers.filterNotNull()

            assertPrints(nonNullNumbers, "[1, 2, 4]")
        }

        @Sample
        fun filterNotNullTo() {
            konst numbers: List<Int?> = listOf(1, 2, null, 4)
            konst nonNullNumbers = mutableListOf<Int>()

            assertPrints(nonNullNumbers, "[]")

            numbers.filterNotNullTo(nonNullNumbers)

            assertPrints(nonNullNumbers, "[1, 2, 4]")
        }

        @Sample
        fun filterIndexed() {
            konst numbers: List<Int> = listOf(0, 1, 2, 3, 4, 8, 6)
            konst numbersOnSameIndexAsValue = numbers.filterIndexed { index, i -> index == i }

            assertPrints(numbersOnSameIndexAsValue, "[0, 1, 2, 3, 4, 6]")
        }

        @Sample
        fun filterIndexedTo() {
            konst numbers: List<Int> = listOf(0, 1, 2, 3, 4, 8, 6)
            konst numbersOnSameIndexAsValue = mutableListOf<Int>()

            assertPrints(numbersOnSameIndexAsValue, "[]")

            numbers.filterIndexedTo(numbersOnSameIndexAsValue) { index, i -> index == i }

            assertPrints(numbersOnSameIndexAsValue, "[0, 1, 2, 3, 4, 6]")
        }

        @Sample
        fun filterIsInstance() {
            open class Animal(konst name: String) {
                override fun toString(): String {
                    return name
                }
            }
            class Dog(name: String): Animal(name)
            class Cat(name: String): Animal(name)

            konst animals: List<Animal> = listOf(Cat("Scratchy"), Dog("Poochie"))
            konst cats = animals.filterIsInstance<Cat>()

            assertPrints(cats, "[Scratchy]")
        }

        @Sample
        fun filterIsInstanceJVM() {
            open class Animal(konst name: String) {
                override fun toString(): String {
                    return name
                }
            }
            class Dog(name: String): Animal(name)
            class Cat(name: String): Animal(name)

            konst animals: List<Animal> = listOf(Cat("Scratchy"), Dog("Poochie"))
            konst cats = animals.filterIsInstance(Cat::class.java)

            assertPrints(cats, "[Scratchy]")
        }

        @Sample
        fun filterIsInstanceTo() {
            open class Animal(konst name: String) {
                override fun toString(): String {
                    return name
                }
            }
            class Dog(name: String): Animal(name)
            class Cat(name: String): Animal(name)

            konst animals: List<Animal> = listOf(Cat("Scratchy"), Dog("Poochie"))
            konst cats = mutableListOf<Cat>()

            assertPrints(cats, "[]")

            animals.filterIsInstanceTo<Cat, MutableList<Cat>>(cats)

            assertPrints(cats, "[Scratchy]")
        }

        @Sample
        fun filterIsInstanceToJVM() {
            open class Animal(konst name: String) {
                override fun toString(): String {
                    return name
                }
            }
            class Dog(name: String): Animal(name)
            class Cat(name: String): Animal(name)

            konst animals: List<Animal> = listOf(Cat("Scratchy"), Dog("Poochie"))
            konst cats = mutableListOf<Cat>()

            assertPrints(cats, "[]")

            animals.filterIsInstanceTo(cats, Cat::class.java)

            assertPrints(cats, "[Scratchy]")
        }

    }
}