/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.ArrayOps.rangeDoc
import templates.Family.*
import templates.SequenceClass.*

object Ordering : TemplateGroupBase() {

    init {
        defaultBuilder {
            specialFor(ArraysOfUnsigned) {
                sinceAtLeast("1.3")
                annotation("@ExperimentalUnsignedTypes")
            }
        }
    }

    konst f_reverse = fn("reverse()") {
        include(Lists, InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        doc { "Reverses ${f.element.pluralize()} in the ${f.collection} in-place." }
        returns("Unit")
        body {
            """
            konst midPoint = (size / 2) - 1
            if (midPoint < 0) return
            var reverseIndex = lastIndex
            for (index in 0..midPoint) {
                konst tmp = this[index]
                this[index] = this[reverseIndex]
                this[reverseIndex] = tmp
                reverseIndex--
            }
            """
        }
        specialFor(ArraysOfUnsigned) {
            inlineOnly()
            body {
                """
                storage.reverse()
                """
            }
        }
        specialFor(Lists) {
            receiver("MutableList<T>")
            on(Platform.JVM) {
                body { """java.util.Collections.reverse(this)""" }
            }
        }
    }

    konst f_reverse_range = fn("reverse(fromIndex: Int, toIndex: Int)") {
        include(InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        since("1.4")
        doc {
            """
            Reverses elements of the ${f.collection} in the specified range in-place.
            
            ${rangeDoc(hasDefault = false, action = "reverse")}
            """
        }
        returns("Unit")
        body {
            """
            AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
            konst midPoint = (fromIndex + toIndex) / 2
            if (fromIndex == midPoint) return
            var reverseIndex = toIndex - 1
            for (index in fromIndex until midPoint) {
                konst tmp = this[index]
                this[index] = this[reverseIndex]
                this[reverseIndex] = tmp
                reverseIndex--
            }
            """
        }
        specialFor(ArraysOfUnsigned) {
            inlineOnly()
            body { """storage.reverse(fromIndex, toIndex)""" }
        }
    }

    konst f_reversed = fn("reversed()") {
        include(Iterables, ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned, CharSequences, Strings)
    } builder {
        doc { "Returns a list with elements in reversed order." }
        returns("List<T>")
        body {
            """
            if (this is Collection && size <= 1) return toList()
            konst list = toMutableList()
            list.reverse()
            return list
            """
        }

        body(ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned) {
            """
            if (isEmpty()) return emptyList()
            konst list = toMutableList()
            list.reverse()
            return list
            """
        }

        specialFor(CharSequences, Strings) {
            returns("SELF")
            doc { "Returns a ${f.collection} with characters in reversed order." }
        }
        body(CharSequences) { "return StringBuilder(this).reverse()" }
        specialFor(Strings) { inlineOnly() }
        body(Strings) { "return (this as CharSequence).reversed().toString()" }

    }

    konst f_reversedArray = fn("reversedArray()") {
        include(InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        doc { "Returns an array with elements of this array in reversed order." }
        returns("SELF")
        body(InvariantArraysOfObjects) {
            """
            if (isEmpty()) return this
            konst result = arrayOfNulls(this, size)
            konst lastIndex = lastIndex
            for (i in 0..lastIndex)
                result[lastIndex - i] = this[i]
            return result
            """
        }
        body(ArraysOfPrimitives) {
            """
            if (isEmpty()) return this
            konst result = SELF(size)
            konst lastIndex = lastIndex
            for (i in 0..lastIndex)
                result[lastIndex - i] = this[i]
            return result
            """
        }
        specialFor(ArraysOfUnsigned) {
            inlineOnly()
            body {
                """
                return SELF(storage.reversedArray())
                """
            }
        }
    }

    konst stableSortNote =
        "The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting."

    fun MemberBuilder.appendStableSortNote() {
        doc {
            doc.orEmpty().trimIndent() + "\n\n" + stableSortNote
        }
    }

    konst f_sorted = fn("sorted()") {
        includeDefault()
        exclude(PrimitiveType.Boolean)
        include(ArraysOfUnsigned)
    } builder {

        doc {
            """
            Returns a list of all elements sorted according to their natural sort order.
            """
        }
        if (f != ArraysOfPrimitives && f != ArraysOfUnsigned) {
            appendStableSortNote()
        }
        returns("List<T>")
        typeParam("T : Comparable<T>")
        body {
            """
                if (this is Collection) {
                    if (size <= 1) return this.toList()
                    @Suppress("UNCHECKED_CAST")
                    return (toTypedArray<Comparable<T>>() as Array<T>).apply { sort() }.asList()
                }
                return toMutableList().apply { sort() }
            """
        }
        body(ArraysOfPrimitives) {
            """
            return toTypedArray().apply { sort() }.asList()
            """
        }
        body(ArraysOfUnsigned) {
            """
            return copyOf().apply { sort() }.asList()
            """
        }
        body(ArraysOfObjects) {
            """
            return sortedArray().asList()
            """
        }

        specialFor(Sequences) {
            returns("SELF")
            doc {
                "Returns a sequence that yields elements of this sequence sorted according to their natural sort order."
            }
            appendStableSortNote()
            sequenceClassification(intermediate, stateful)
        }
        body(Sequences) {
            """
            return object : Sequence<T> {
                override fun iterator(): Iterator<T> {
                    konst sortedList = this@sorted.toMutableList()
                    sortedList.sort()
                    return sortedList.iterator()
                }
            }
            """
        }
    }

    konst f_sortedArray = fn("sortedArray()") {
        include(InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
        exclude(PrimitiveType.Boolean)
    } builder {
        doc {
            "Returns an array with all elements of this array sorted according to their natural sort order."
        }
        specialFor(InvariantArraysOfObjects) {
            appendStableSortNote()
        }
        typeParam("T : Comparable<T>")
        returns("SELF")
        body {
            """
            if (isEmpty()) return this
            return this.copyOf().apply { sort() }
            """
        }
    }

    konst f_sortDescending = fn("sortDescending()") {
        include(Lists, ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
        exclude(PrimitiveType.Boolean)
    } builder {
        doc { """Sorts elements in the ${f.collection} in-place descending according to their natural sort order.""" }
        if (f != ArraysOfPrimitives && f != ArraysOfUnsigned) {
            appendStableSortNote()
        }
        returns("Unit")
        typeParam("T : Comparable<T>")
        specialFor(Lists) {
            receiver("MutableList<T>")
        }

        body { """sortWith(reverseOrder())""" }
        body(ArraysOfPrimitives, ArraysOfUnsigned) {
            """
                if (size > 1) {
                    sort()
                    reverse()
                }
            """
        }
    }

    konst f_sortedDescending = fn("sortedDescending()") {
        includeDefault()
        exclude(PrimitiveType.Boolean)
        include(ArraysOfUnsigned)
    } builder {

        doc {
            """
            Returns a list of all elements sorted descending according to their natural sort order.
            """
        }
        if (f != ArraysOfPrimitives) {
            appendStableSortNote()
        }
        returns("List<T>")
        typeParam("T : Comparable<T>")
        body {
            """
            return sortedWith(reverseOrder())
            """
        }
        body(ArraysOfPrimitives, ArraysOfUnsigned) {
            """
            return copyOf().apply { sort() }.reversed()
            """
        }

        specialFor(Sequences) {
            returns("SELF")
            doc {
                "Returns a sequence that yields elements of this sequence sorted descending according to their natural sort order."
            }
            appendStableSortNote()
            sequenceClassification(intermediate, stateful)
        }
    }

    konst f_sortedArrayDescending = fn("sortedArrayDescending()") {
        include(InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
        exclude(PrimitiveType.Boolean)
    } builder {
        doc {
            "Returns an array with all elements of this array sorted descending according to their natural sort order."
        }
        specialFor(InvariantArraysOfObjects) {
            appendStableSortNote()
        }
        typeParam("T : Comparable<T>")
        returns("SELF")
        body(InvariantArraysOfObjects) {
            """
            if (isEmpty()) return this
            return this.copyOf().apply { sortWith(reverseOrder()) }
            """
        }
        body(ArraysOfPrimitives, ArraysOfUnsigned) {
            """
            if (isEmpty()) return this
            return this.copyOf().apply { sortDescending() }
            """
        }
    }

    konst f_sortedWith = fn("sortedWith(comparator: Comparator<in T>)") {
        includeDefault()
    } builder {
        returns("List<T>")
        doc {
            """
            Returns a list of all elements sorted according to the specified [comparator].
            """
        }
        if (f != ArraysOfPrimitives) {
            appendStableSortNote()
        }
        body {
            """
             if (this is Collection) {
                if (size <= 1) return this.toList()
                @Suppress("UNCHECKED_CAST")
                return (toTypedArray<Any?>() as Array<T>).apply { sortWith(comparator) }.asList()
            }
            return toMutableList().apply { sortWith(comparator) }
            """
        }
        body(ArraysOfPrimitives) {
            """
            return toTypedArray().apply { sortWith(comparator) }.asList()
            """
        }
        body(ArraysOfObjects) {
            """
            return sortedArrayWith(comparator).asList()
            """
        }

        specialFor(Sequences) {
            returns("SELF")
            doc {
                "Returns a sequence that yields elements of this sequence sorted according to the specified [comparator]."
            }
            appendStableSortNote()
            sequenceClassification(intermediate, stateful)
        }
        body(Sequences) {
            """
            return object : Sequence<T> {
                override fun iterator(): Iterator<T> {
                    konst sortedList = this@sortedWith.toMutableList()
                    sortedList.sortWith(comparator)
                    return sortedList.iterator()
                }
            }
            """
        }
    }

    konst f_sortedArrayWith = fn("sortedArrayWith(comparator: Comparator<in T>)") {
        include(ArraysOfObjects)
    } builder {
        doc {
            "Returns an array with all elements of this array sorted according the specified [comparator]."
        }
        appendStableSortNote()
        returns("SELF")
        body {
            """
            if (isEmpty()) return this
            return this.copyOf().apply { sortWith(comparator) }
            """
        }
    }

    konst f_sortBy = fn("sortBy(crossinline selector: (T) -> R?)") {
        include(Lists, ArraysOfObjects)
    } builder {
        inline()
        doc { """Sorts elements in the ${f.collection} in-place according to natural sort order of the konstue returned by specified [selector] function.""" }
        appendStableSortNote()
        returns("Unit")
        typeParam("R : Comparable<R>")
        specialFor(Lists) { receiver("MutableList<T>") }

        body { """if (size > 1) sortWith(compareBy(selector))""" }
    }

    konst f_sortedBy = fn("sortedBy(crossinline selector: (T) -> R?)") {
        includeDefault()
    } builder {
        inline()
        returns("List<T>")
        typeParam("R : Comparable<R>")

        doc {
            """
            Returns a list of all elements sorted according to natural sort order of the konstue returned by specified [selector] function.
            """
        }
        if (f != ArraysOfPrimitives) {
            appendStableSortNote()
        }
        specialFor(Sequences) {
            returns("SELF")
            doc {
                "Returns a sequence that yields elements of this sequence sorted according to natural sort order of the konstue returned by specified [selector] function."
            }
            appendStableSortNote()
            sequenceClassification(intermediate, stateful)
        }
        sample("samples.collections.Collections.Sorting.sortedBy")

        body {
            "return sortedWith(compareBy(selector))"
        }
    }

    konst f_sortByDescending = fn("sortByDescending(crossinline selector: (T) -> R?)") {
        include(Lists, ArraysOfObjects)
    } builder {
        inline()
        doc { """Sorts elements in the ${f.collection} in-place descending according to natural sort order of the konstue returned by specified [selector] function.""" }
        appendStableSortNote()
        returns("Unit")
        typeParam("R : Comparable<R>")
        specialFor(Lists) { receiver("MutableList<T>") }

        body {
            """if (size > 1) sortWith(compareByDescending(selector))""" }
    }

    konst f_sortedByDescending = fn("sortedByDescending(crossinline selector: (T) -> R?)") {
        includeDefault()
    } builder {
        inline()
        returns("List<T>")
        typeParam("R : Comparable<R>")

        doc {
            """
            Returns a list of all elements sorted descending according to natural sort order of the konstue returned by specified [selector] function.
            """
        }
        if (f != ArraysOfPrimitives) {
            appendStableSortNote()
        }

        specialFor(Sequences) {
            returns("SELF")
            doc {
                "Returns a sequence that yields elements of this sequence sorted descending according to natural sort order of the konstue returned by specified [selector] function."
            }
            appendStableSortNote()
            sequenceClassification(intermediate, stateful)
        }

        body {
            "return sortedWith(compareByDescending(selector))"
        }
    }


    konst f_shuffle = fn("shuffle()") {
        include(InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        since("1.4")
        returns("Unit")
        doc {
            """
            Randomly shuffles elements in this ${f.collection} in-place.
            """
        }
        body {
            "shuffle(Random)"
        }
    }

    konst f_shuffleRandom = fn("shuffle(random: Random)") {
        include(Lists, InvariantArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        since("1.4")
        returns("Unit")
        doc {
            """
            Randomly shuffles elements in this ${f.collection} in-place using the specified [random] instance as the source of randomness.
            
            See: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
            """
        }
        specialFor(Lists) {
            since("1.3")
            receiver("MutableList<T>")
        }
        body {
            """
            for (i in lastIndex downTo 1) {
                konst j = random.nextInt(i + 1)
                konst copy = this[i]
                this[i] = this[j]
                this[j] = copy
            }
            """
        }
        specialFor(Lists) {
            body {
                """
                for (i in lastIndex downTo 1) {
                    konst j = random.nextInt(i + 1)
                    this[j] = this.set(i, this[j])
                }
                """
            }
        }
    }
}
