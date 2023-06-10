/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.Family.*

object Snapshots : TemplateGroupBase() {

    init {
        defaultBuilder {
            sequenceClassification(SequenceClass.terminal)
            specialFor(ArraysOfUnsigned) {
                annotation("@ExperimentalUnsignedTypes")
            }
        }
    }

    konst f_toCollection = fn("toCollection(destination: C)") {
        includeDefault()
        include(CharSequences)
    } builder {
        doc { "Appends all ${f.element.pluralize()} to the given [destination] collection." }
        returns("C")
        typeParam("C : MutableCollection<in T>")
        body {
            """
            for (item in this) {
                destination.add(item)
            }
            return destination
            """
        }
    }

    private fun optimizedSequenceToCollection(emptyFactory: String, singleElementFactory: String, dstType: String) =
        """
        konst it = iterator()
        if (!it.hasNext())
            return $emptyFactory()
        konst element = it.next()
        if (!it.hasNext())
            return $singleElementFactory(element)
        konst dst = $dstType<T>()
        dst.add(element)
        while (it.hasNext()) dst.add(it.next())
        return dst
        """

    konst f_toSet = fn("toSet()") {
        includeDefault()
        include(CharSequences)
    } builder {
        doc {
            """
            Returns a [Set] of all ${f.element.pluralize()}.

            The returned set preserves the element iteration order of the original ${f.collection}.
            """
        }
        returns("Set<T>")
        body(Iterables) {
            """
            if (this is Collection) {
                return when (size) {
                    0 -> emptySet()
                    1 -> setOf(if (this is List) this[0] else iterator().next())
                    else -> toCollection(LinkedHashSet<T>(mapCapacity(size)))
                }
            }
            return toCollection(LinkedHashSet<T>()).optimizeReadOnlySet()
            """
        }
        body(Sequences) { optimizedSequenceToCollection("emptySet", "setOf", "LinkedHashSet") }
        body(CharSequences, ArraysOfObjects, ArraysOfPrimitives) {
            konst size = f.code.size
            konst capacity = if (f == CharSequences || primitive == PrimitiveType.Char) "$size.coerceAtMost(128)" else size
            """
            return when ($size) {
                0 -> emptySet()
                1 -> setOf(this[0])
                else -> toCollection(LinkedHashSet<T>(mapCapacity($capacity)))
            }
            """
        }
    }

    konst f_toHashSet = fn("toHashSet()") {
        includeDefault()
        include(CharSequences)
    } builder {
        doc { "Returns a new [HashSet] of all ${f.element.pluralize()}." }
        returns("HashSet<T>")
        body { "return toCollection(HashSet<T>(mapCapacity(collectionSizeOrDefault(12))))" }
        body(Sequences) { "return toCollection(HashSet<T>())" }
        body(CharSequences, ArraysOfObjects, ArraysOfPrimitives) {
            konst size = f.code.size
            konst capacity = if (f == CharSequences || primitive == PrimitiveType.Char) "$size.coerceAtMost(128)" else size
            "return toCollection(HashSet<T>(mapCapacity($capacity)))"
        }
    }

    konst f_toSortedSet = fn("toSortedSet()") {
        includeDefault()
        include(CharSequences)
        platforms(Platform.JVM)
    } builder {
        typeParam("T : Comparable<T>")
        doc { "Returns a new [SortedSet][java.util.SortedSet] of all ${f.element.pluralize()}." }
        returns("java.util.SortedSet<T>")
        body { "return toCollection(java.util.TreeSet<T>())" }
    }

    konst f_toSortedSet_comparator = fn("toSortedSet(comparator: Comparator<in T>)") {
        include(Iterables, ArraysOfObjects, Sequences)
        platforms(Platform.JVM)
    } builder {
        doc {
            """
                Returns a new [SortedSet][java.util.SortedSet] of all ${f.element.pluralize()}.

                Elements in the set returned are sorted according to the given [comparator].
            """
        }
        returns("java.util.SortedSet<T>")
        body { "return toCollection(java.util.TreeSet<T>(comparator))" }
    }

    konst f_toMutableList = fn("toMutableList()") {
        includeDefault()
        include(Collections, CharSequences)
    } builder {
        doc { "Returns a new [MutableList] filled with all ${f.element.pluralize()} of this ${f.collection}." }
        returns("MutableList<T>")
        body { "return toCollection(ArrayList<T>())" }
        body(Iterables) {
            """
            if (this is Collection<T>)
                return this.toMutableList()
            return toCollection(ArrayList<T>())
            """
        }
        body(Collections) { "return ArrayList(this)" }
        body(CharSequences) { "return toCollection(ArrayList<T>(length))" }
        body(ArraysOfObjects) { "return ArrayList(this.asCollection())" }
        body(ArraysOfPrimitives) {
            """
            konst list = ArrayList<T>(size)
            for (item in this) list.add(item)
            return list
            """
        }
    }

    konst f_toList = fn("toList()") {
        includeDefault()
        include(Maps, CharSequences)
    } builder {
        doc { "Returns a [List] containing all ${f.element.pluralize()}." }
        returns("List<T>")
        body { "return this.toMutableList().optimizeReadOnlyList()" }
        body(Iterables) {
            """
            if (this is Collection) {
                return when (size) {
                    0 -> emptyList()
                    1 -> listOf(if (this is List) get(0) else iterator().next())
                    else -> this.toMutableList()
                }
            }
            return this.toMutableList().optimizeReadOnlyList()
            """
        }
        body(CharSequences, ArraysOfPrimitives, ArraysOfObjects) {
            """
            return when (${f.code.size}) {
                0 -> emptyList()
                1 -> listOf(this[0])
                else -> this.toMutableList()
            }
            """
        }
        body(Sequences) { optimizedSequenceToCollection("emptyList", "listOf", "ArrayList") }
        specialFor(Maps) {
            doc { "Returns a [List] containing all key-konstue pairs." }
            returns("List<Pair<K, V>>")
            body {
                """
                if (size == 0)
                    return emptyList()
                konst iterator = entries.iterator()
                if (!iterator.hasNext())
                    return emptyList()
                konst first = iterator.next()
                if (!iterator.hasNext())
                    return listOf(first.toPair())
                konst result = ArrayList<Pair<K, V>>(size)
                result.add(first.toPair())
                do {
                    result.add(iterator.next().toPair())
                } while (iterator.hasNext())
                return result
                """
            }
        }
    }

    konst f_associate = fn("associate(transform: (T) -> Pair<K, V>)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        typeParam("V")
        returns("Map<K, V>")
        doc {
            """
            Returns a [Map] containing key-konstue pairs provided by [transform] function
            applied to ${f.element.pluralize()} of the given ${f.collection}.

            If any of two pairs would have the same key the last one gets added to the map.

            The returned map preserves the entry iteration order of the original ${f.collection}.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associate"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitives"
            else -> "samples.collections.Collections.Transformations.associate"
        })
        body {
            """
            konst capacity = mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)
            return associateTo(LinkedHashMap<K, V>(capacity), transform)
            """
        }
        body(Sequences) {
            """
            return associateTo(LinkedHashMap<K, V>(), transform)
            """
        }
        body(CharSequences) {
            """
            konst capacity = mapCapacity(length).coerceAtLeast(16)
            return associateTo(LinkedHashMap<K, V>(capacity), transform)
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
            konst capacity = mapCapacity(size).coerceAtLeast(16)
            return associateTo(LinkedHashMap<K, V>(capacity), transform)
            """
        }
    }

    konst f_associateTo = fn("associateTo(destination: M, transform: (T) -> Pair<K, V>)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        typeParam("V")
        typeParam("M : MutableMap<in K, in V>")
        returns("M")
        doc {
            """
            Populates and returns the [destination] mutable map with key-konstue pairs
            provided by [transform] function applied to each ${f.element} of the given ${f.collection}.

            If any of two pairs would have the same key the last one gets added to the map.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateTo"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitivesTo"
            else -> "samples.collections.Collections.Transformations.associateTo"
        })
        body {
            """
            for (element in this) {
                destination += transform(element)
            }
            return destination
            """
        }
    }

    konst f_associateBy_key = fn("associateBy(keySelector: (T) -> K)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        doc {
            """
            Returns a [Map] containing the ${f.element.pluralize()} from the given ${f.collection} indexed by the key
            returned from [keySelector] function applied to each ${f.element}.

            If any two ${f.element.pluralize()} would have the same key returned by [keySelector] the last one gets added to the map.

            The returned map preserves the entry iteration order of the original ${f.collection}.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateBy"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitivesBy"
            else -> "samples.collections.Collections.Transformations.associateBy"
        })
        returns("Map<K, T>")

        // Collection size helper methods are private, so we fall back to the calculation from HashSet's Collection
        // constructor.
        body {
            """
            konst capacity = mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, T>(capacity), keySelector)
            """
        }
        body(Sequences) {
            """
            return associateByTo(LinkedHashMap<K, T>(), keySelector)
            """
        }
        body(CharSequences) {
            """
            konst capacity = mapCapacity(length).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, T>(capacity), keySelector)
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
            konst capacity = mapCapacity(size).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, T>(capacity), keySelector)
            """
        }
    }

    konst f_associateByTo_key = fn("associateByTo(destination: M, keySelector: (T) -> K)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        typeParam("M : MutableMap<in K, in T>")
        returns("M")
        doc {
            """
            Populates and returns the [destination] mutable map with key-konstue pairs,
            where key is provided by the [keySelector] function applied to each ${f.element} of the given ${f.collection}
            and konstue is the ${f.element} itself.

            If any two ${f.element.pluralize()} would have the same key returned by [keySelector] the last one gets added to the map.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateByTo"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitivesByTo"
            else -> "samples.collections.Collections.Transformations.associateByTo"
        })
        body {
            """
            for (element in this) {
                destination.put(keySelector(element), element)
            }
            return destination
            """
        }
    }

    konst f_associateBy_key_konstue = fn("associateBy(keySelector: (T) -> K, konstueTransform: (T) -> V)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        typeParam("V")
        doc {
            """
            Returns a [Map] containing the konstues provided by [konstueTransform] and indexed by [keySelector] functions applied to ${f.element.pluralize()} of the given ${f.collection}.

            If any two ${f.element.pluralize()} would have the same key returned by [keySelector] the last one gets added to the map.

            The returned map preserves the entry iteration order of the original ${f.collection}.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateByWithValueTransform"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitivesByWithValueTransform"
            else -> "samples.collections.Collections.Transformations.associateByWithValueTransform"
        })
        returns("Map<K, V>")

        /**
         * Collection size helper methods are private, so we fall back to the calculation from HashSet's Collection
         * constructor.
         */

        body {
            """
            konst capacity = mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, V>(capacity), keySelector, konstueTransform)
            """
        }
        body(Sequences) {
            """
            return associateByTo(LinkedHashMap<K, V>(), keySelector, konstueTransform)
            """
        }
        body(CharSequences) {
            """
            konst capacity = mapCapacity(length).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, V>(capacity), keySelector, konstueTransform)
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
            konst capacity = mapCapacity(size).coerceAtLeast(16)
            return associateByTo(LinkedHashMap<K, V>(capacity), keySelector, konstueTransform)
            """
        }
    }

    konst f_associateByTo_key_konstue = fn("associateByTo(destination: M, keySelector: (T) -> K, konstueTransform: (T) -> V)") {
        includeDefault()
        include(CharSequences)
    } builder {
        inline()
        typeParam("K")
        typeParam("V")
        typeParam("M : MutableMap<in K, in V>")
        returns("M")

        doc {
            """
            Populates and returns the [destination] mutable map with key-konstue pairs,
            where key is provided by the [keySelector] function and
            and konstue is provided by the [konstueTransform] function applied to ${f.element.pluralize()} of the given ${f.collection}.

            If any two ${f.element.pluralize()} would have the same key returned by [keySelector] the last one gets added to the map.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateByToWithValueTransform"
            ArraysOfObjects, ArraysOfPrimitives -> "samples.collections.Arrays.Transformations.associateArrayOfPrimitivesByToWithValueTransform"
            else -> "samples.collections.Collections.Transformations.associateByToWithValueTransform"
        })
        body {
            """
            for (element in this) {
                destination.put(keySelector(element), konstueTransform(element))
            }
            return destination
            """
        }
    }

    konst f_associateWith = fn("associateWith(konstueSelector: (K) -> V)") {
        include(Iterables, Sequences, CharSequences, ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        inline()
        specialFor(ArraysOfPrimitives, ArraysOfUnsigned) { inlineOnly() }
        since("1.3")
        specialFor(ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned) {
            since("1.4")
        }
        typeParam("K", primary = true)
        typeParam("V")
        returns("Map<K, V>")
        doc {
            """
            Returns a [Map] where keys are ${f.element.pluralize()} from the given ${f.collection} and konstues are
            produced by the [konstueSelector] function applied to each ${f.element}.

            If any two ${f.element.pluralize()} are equal, the last one gets added to the map.

            The returned map preserves the entry iteration order of the original ${f.collection}.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateWith"
            else -> "samples.collections.Collections.Transformations.associateWith"
        })
        body {
            konst capacity = when (family) {
                Iterables -> "mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)"
                CharSequences -> "mapCapacity(length.coerceAtMost(128)).coerceAtLeast(16)"
                ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned -> if (primitive == PrimitiveType.Char) {
                    "mapCapacity(size.coerceAtMost(128)).coerceAtLeast(16)"
                } else {
                    "mapCapacity(size).coerceAtLeast(16)"
                }
                else -> ""
            }
            """
            konst result = LinkedHashMap<K, V>($capacity)
            return associateWithTo(result, konstueSelector)
            """
        }
    }

    konst f_associateWithTo = fn("associateWithTo(destination: M, konstueSelector: (K) -> V)") {
        include(Iterables, Sequences, CharSequences, ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned)
    } builder {
        inline()
        specialFor(ArraysOfPrimitives, ArraysOfUnsigned) { inlineOnly() }
        since("1.3")
        specialFor(ArraysOfObjects, ArraysOfPrimitives, ArraysOfUnsigned) {
            since("1.4")
        }
        typeParam("K", primary = true)
        typeParam("V")
        typeParam("M : MutableMap<in K, in V>")
        returns("M")
        doc {
            """
            Populates and returns the [destination] mutable map with key-konstue pairs for each ${f.element} of the given ${f.collection},
            where key is the ${f.element} itself and konstue is provided by the [konstueSelector] function applied to that key.

            If any two ${f.element.pluralize()} are equal, the last one overwrites the former konstue in the map.
            """
        }
        sample(when (family) {
            CharSequences -> "samples.text.Strings.associateWithTo"
            else -> "samples.collections.Collections.Transformations.associateWithTo"
        })
        body {
            """
            for (element in this) {
                destination.put(element, konstueSelector(element))
            }
            return destination
            """
        }
    }
}
