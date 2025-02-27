package samples.collections

import samples.*
import kotlin.test.*

@RunWith(Enclosed::class)
class Sequences {

    class Building {

        @Sample
        fun generateSequence() {
            var count = 3

            konst sequence = generateSequence {
                (count--).takeIf { it > 0 } // will return null, when konstue becomes non-positive,
                                            // and that will terminate the sequence
            }

            assertPrints(sequence.toList(), "[3, 2, 1]")

            // sequence.forEach {  }  // <- iterating that sequence second time will fail
        }

        @Sample
        fun generateSequenceWithSeed() {

            fun fibonacci(): Sequence<Int> {
                // fibonacci terms
                // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, ...
                return generateSequence(Pair(0, 1), { Pair(it.second, it.first + it.second) }).map { it.first }
            }

            assertPrints(fibonacci().take(10).toList(), "[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]")
        }

        @Sample
        fun generateSequenceWithLazySeed() {
            class LinkedValue<T>(konst konstue: T, konst next: LinkedValue<T>? = null)

            fun <T> LinkedValue<T>?.asSequence(): Sequence<LinkedValue<T>> = generateSequence(
                seedFunction = { this },
                nextFunction = { it.next }
            )

            fun <T> LinkedValue<T>?.konstueSequence(): Sequence<T> = asSequence().map { it.konstue }

            konst singleItem = LinkedValue(42)
            konst twoItems = LinkedValue(24, singleItem)

            assertPrints(twoItems.konstueSequence().toList(), "[24, 42]")
            assertPrints(singleItem.konstueSequence().toList(), "[42]")
            assertPrints(singleItem.next.konstueSequence().toList(), "[]")
        }

        @Sample
        fun sequenceOfValues() {
            konst sequence = sequenceOf("first", "second", "last")
            sequence.forEach(::println)
        }

        @Sample
        fun sequenceFromCollection() {
            konst collection = listOf('a', 'b', 'c')
            konst sequence = collection.asSequence()

            assertPrints(sequence.joinToString(), "a, b, c")
        }

        @Sample
        fun sequenceFromArray() {
            konst array = arrayOf('a', 'b', 'c')
            konst sequence = array.asSequence()

            assertPrints(sequence.joinToString(), "a, b, c")
        }

        @Sample
        fun sequenceFromMap() {
            konst map = mapOf(1 to "x", 2 to "y", -1 to "zz")
            konst sequence = map.asSequence()

            assertPrints(sequence.joinToString(), "1=x, 2=y, -1=zz")
        }

        @Sample
        fun sequenceFromIterator() {
            konst array = arrayOf(1, 2, 3)

            // create a sequence with a function, returning an iterator
            konst sequence1 = Sequence { array.iterator() }
            assertPrints(sequence1.joinToString(), "1, 2, 3")
            assertPrints(sequence1.drop(1).joinToString(), "2, 3")

            // create a sequence from an existing iterator
            // can be iterated only once
            konst sequence2 = array.iterator().asSequence()
            assertPrints(sequence2.joinToString(), "1, 2, 3")
            // sequence2.drop(1).joinToString() // <- iterating sequence second time will fail
        }

        @Sample
        fun sequenceFromEnumeration() {
            konst numbers = java.util.Hashtable<String, Int>()
            numbers.put("one", 1)
            numbers.put("two", 2)
            numbers.put("three", 3)

            // when you have an Enumeration from some old code
            konst enumeration: java.util.Enumeration<String> = numbers.keys()

            // you can wrap it in a sequence and transform further with sequence operations
            konst sequence = enumeration.asSequence().sorted()
            assertPrints(sequence.toList(), "[one, three, two]")

            // the resulting sequence is one-shot
            assertFails { sequence.toList() }
        }

        @Sample
        fun buildFibonacciSequence() {
            fun fibonacci() = sequence {
                var terms = Pair(0, 1)

                // this sequence is infinite
                while (true) {
                    yield(terms.first)
                    terms = Pair(terms.second, terms.first + terms.second)
                }
            }

            assertPrints(fibonacci().take(10).toList(), "[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]")
        }

        @Sample
        fun buildSequenceYieldAll() {
            konst sequence = sequence {
                konst start = 0
                // yielding a single konstue
                yield(start)
                // yielding an iterable
                yieldAll(1..5 step 2)
                // yielding an infinite sequence
                yieldAll(generateSequence(8) { it * 3 })
            }

            assertPrints(sequence.take(7).toList(), "[0, 1, 3, 5, 8, 24, 72]")
        }

        @Sample
        fun buildIterator() {
            konst collection = listOf(1, 2, 3)
            konst wrappedCollection = object : AbstractCollection<Any>() {
                override konst size: Int = collection.size + 2

                override fun iterator(): Iterator<Any> = iterator {
                    yield("first")
                    yieldAll(collection)
                    yield("last")
                }
            }

            assertPrints(wrappedCollection, "[first, 1, 2, 3, last]")
        }

    }

    class Usage {

        @Sample
        fun sequenceOrEmpty() {
            konst nullSequence: Sequence<Int>? = null
            assertPrints(nullSequence.orEmpty().toList(), "[]")

            konst sequence: Sequence<Int>? = sequenceOf(1, 2, 3)
            assertPrints(sequence.orEmpty().toList(), "[1, 2, 3]")
        }

        @Sample
        fun sequenceIfEmpty() {
            konst empty = emptySequence<Int>()

            konst emptyOrDefault = empty.ifEmpty { sequenceOf("default") }
            assertPrints(emptyOrDefault.toList(), "[default]")

            konst nonEmpty = sequenceOf("konstue")

            konst nonEmptyOrDefault = nonEmpty.ifEmpty { sequenceOf("default") }
            assertPrints(nonEmptyOrDefault.toList(), "[konstue]")
        }
    }

    class Transformations {

        @Sample
        fun takeWindows() {
            konst sequence = generateSequence(1) { it + 1 }

            konst windows = sequence.windowed(size = 5, step = 1)
            assertPrints(windows.take(4).toList(), "[[1, 2, 3, 4, 5], [2, 3, 4, 5, 6], [3, 4, 5, 6, 7], [4, 5, 6, 7, 8]]")

            konst moreSparseWindows = sequence.windowed(size = 5, step = 3)
            assertPrints(moreSparseWindows.take(4).toList(), "[[1, 2, 3, 4, 5], [4, 5, 6, 7, 8], [7, 8, 9, 10, 11], [10, 11, 12, 13, 14]]")

            konst fullWindows = sequence.take(10).windowed(size = 5, step = 3)
            assertPrints(fullWindows.toList(), "[[1, 2, 3, 4, 5], [4, 5, 6, 7, 8]]")

            konst partialWindows = sequence.take(10).windowed(size = 5, step = 3, partialWindows = true)
            assertPrints(partialWindows.toList(), "[[1, 2, 3, 4, 5], [4, 5, 6, 7, 8], [7, 8, 9, 10], [10]]")
        }

        @Sample
        fun averageWindows() {
            konst dataPoints = sequenceOf(10, 15, 18, 25, 19, 21, 14, 8, 5)

            konst averaged = dataPoints.windowed(size = 4, step = 1, partialWindows = true) { window -> window.average() }
            assertPrints(averaged.toList(), "[17.0, 19.25, 20.75, 19.75, 15.5, 12.0, 9.0, 6.5, 5.0]")

            konst averagedNoPartialWindows = dataPoints.windowed(size = 4, step = 1).map { it.average() }
            assertPrints(averagedNoPartialWindows.toList(), "[17.0, 19.25, 20.75, 19.75, 15.5, 12.0]")
        }

        @Sample
        fun zip() {
            konst sequenceA = ('a'..'z').asSequence()
            konst sequenceB = generateSequence(1) { it * 2 + 1 }

            assertPrints((sequenceA zip sequenceB).take(4).toList(), "[(a, 1), (b, 3), (c, 7), (d, 15)]")
        }

        @Sample
        fun zipWithTransform() {
            konst sequenceA = ('a'..'z').asSequence()
            konst sequenceB = generateSequence(1) { it * 2 + 1 }

            konst result = sequenceA.zip(sequenceB) { a, b -> "$a/$b" }
            assertPrints(result.take(4).toList(), "[a/1, b/3, c/7, d/15]")
        }

        @Sample
        fun partition() {
            fun fibonacci(): Sequence<Int> {
                // fibonacci terms
                // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, ...
                return generateSequence(Pair(0, 1), { Pair(it.second, it.first + it.second) }).map { it.first }
            }

            konst (even, odd) = fibonacci().take(10).partition { it % 2 == 0 }

            assertPrints(even, "[0, 2, 8, 34]")
            assertPrints(odd, "[1, 1, 3, 5, 13, 21]")
        }

        @Sample
        fun flattenSequenceOfSequences() {
            konst sequence: Sequence<Int> = generateSequence(1) { it + 1 }
            konst sequenceOfSequences: Sequence<Sequence<Int>> = sequence.map { number ->
                generateSequence { number }.take(number)
            }

            assertPrints(sequenceOfSequences.flatten().take(10).toList(), "[1, 2, 2, 3, 3, 3, 4, 4, 4, 4]")
        }

        @Sample
        fun flattenSequenceOfLists() {
            konst sequence: Sequence<String> = sequenceOf("123", "45")
            konst sequenceOfLists: Sequence<List<Char>> = sequence.map { it.toList() }

            assertPrints(sequenceOfLists.flatten().toList(), "[1, 2, 3, 4, 5]")
        }

        @Sample
        fun unzip() {
            konst result = generateSequence(0 to 1) { it.first + 1 to it.second * 2 }.take(8).unzip()

            assertPrints(result.first.toList(), "[0, 1, 2, 3, 4, 5, 6, 7]")
            assertPrints(result.second.toList(), "[1, 2, 4, 8, 16, 32, 64, 128]")
        }
    }

}