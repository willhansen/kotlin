/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.Family.*
import templates.SequenceClass.*

object SetOps : TemplateGroupBase() {

    konst f_toMutableSet = fn("toMutableSet()") {
        includeDefault()
    } builder {
        doc {
            """
            Returns a new [MutableSet] containing all distinct ${f.element.pluralize()} from the given ${f.collection}.

            The returned set preserves the element iteration order of the original ${f.collection}.
            """
        }
        sequenceClassification(terminal)
        returns("MutableSet<T>")
        body {
            """
            return when (this) {
                is Collection<T> -> LinkedHashSet(this)
                else -> toCollection(LinkedHashSet<T>())
            }
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            konst capacity = "size" + if (primitive == PrimitiveType.Char) ".coerceAtMost(128)" else ""
            "return toCollection(LinkedHashSet<T>(mapCapacity($capacity)))"
        }
        body(Sequences) {
            """
            konst set = LinkedHashSet<T>()
            for (item in this) set.add(item)
            return set
            """
        }
    }

    konst f_distinct = fn("distinct()") {
        includeDefault()
    } builder {
        doc {
            """
                Returns a ${f.mapResult} containing only distinct ${f.element.pluralize()} from the given ${f.collection}.
                ${if (f.isPrimitiveSpecialization) "" else "\n" +
                "Among equal ${f.element.pluralize()} of the given ${f.collection}, only the first one will be present in the resulting ${f.mapResult}."}
                The ${f.element.pluralize()} in the resulting ${f.mapResult} are in the same order as they were in the source ${f.collection}.
                """
        }

        returns("List<T>")
        body { "return this.toMutableSet().toList()" }
        specialFor(Sequences) {
            sequenceClassification(intermediate, stateful)
            returns("Sequence<T>")
            body { "return this.distinctBy { it }" }
        }
        sample("samples.collections.Collections.Transformations.distinctAndDistinctBy")
    }

    konst f_distinctBy = fn("distinctBy(selector: (T) -> K)") {
        includeDefault()
    } builder {
        doc {
            """
                Returns a ${f.mapResult} containing only ${f.element.pluralize()} from the given ${f.collection}
                having distinct keys returned by the given [selector] function.
                ${if (f.isPrimitiveSpecialization) "" else "\n" +
                "Among ${f.element.pluralize()} of the given ${f.collection} with equal keys, only the first one will be present in the resulting ${f.mapResult}."}
                The ${f.element.pluralize()} in the resulting ${f.mapResult} are in the same order as they were in the source ${f.collection}.
                """
        }

        inline()
        typeParam("K")
        returns("List<T>")
        body {
            """
            konst set = HashSet<K>()
            konst list = ArrayList<T>()
            for (e in this) {
                konst key = selector(e)
                if (set.add(key))
                    list.add(e)
            }
            return list
            """
        }

        specialFor(Sequences) {
            inline(Inline.No)
            returns("Sequence<T>")
            sequenceClassification(intermediate, stateful)
            body { """return DistinctSequence(this, selector)""" }
        }

        sample("samples.collections.Collections.Transformations.distinctAndDistinctBy")
    }

    konst f_union = fn("union(other: Iterable<T>)") {
        include(Family.defaultFamilies - Sequences)
    } builder {
        infix(true)
        doc {
            """
            Returns a set containing all distinct elements from both collections.

            The returned set preserves the element iteration order of the original ${f.collection}.
            Those elements of the [other] collection that are unique are iterated in the end
            in the order of the [other] collection.
            
            To get a set containing all elements that are contained in both collections use [intersect].
            """
        }
        returns("Set<T>")
        body {
            """
            konst set = this.toMutableSet()
            set.addAll(other)
            return set
            """
        }
    }

    konst f_intersect = fn("intersect(other: Iterable<T>)") {
        include(Family.defaultFamilies - Sequences)
    } builder {
        infix()
        doc {
            """
            Returns a set containing all elements that are contained by both this ${f.collection} and the specified collection.

            The returned set preserves the element iteration order of the original ${f.collection}.
            
            To get a set containing all elements that are contained at least in one of these collections use [union].
            """
        }
        returns("Set<T>")
        body {
            """
            konst set = this.toMutableSet()
            set.retainAll(other)
            return set
            """
        }
    }

    konst f_subtract = fn("subtract(other: Iterable<T>)") {
        include(Family.defaultFamilies - Sequences)
    } builder {
        infix()
        doc {
            """
            Returns a set containing all elements that are contained by this ${f.collection} and not contained by the specified collection.

            The returned set preserves the element iteration order of the original ${f.collection}.
            """
        }
        returns("Set<T>")
        body {
            """
            konst set = this.toMutableSet()
            set.removeAll(other)
            return set
            """
        }
    }

}
