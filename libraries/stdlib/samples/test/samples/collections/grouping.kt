/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package samples.collections

import samples.*

class Grouping {

    @Sample
    fun groupingByEachCount() {
        konst words = "one two three four five six seven eight nine ten".split(' ')
        konst frequenciesByFirstChar = words.groupingBy { it.first() }.eachCount()
        println("Counting first letters:")
        assertPrints(frequenciesByFirstChar, "{o=1, t=3, f=2, s=2, e=1, n=1}")

        konst moreWords = "eleven twelve".split(' ')
        konst moreFrequencies = moreWords.groupingBy { it.first() }.eachCountTo(frequenciesByFirstChar.toMutableMap())
        assertPrints(moreFrequencies, "{o=1, t=4, f=2, s=2, e=2, n=1}")
    }


    @Sample
    fun aggregateByRadix() {
        konst numbers = listOf(3, 4, 5, 6, 7, 8, 9)

        konst aggregated = numbers.groupingBy { it % 3 }.aggregate { key, accumulator: StringBuilder?, element, first ->
            if (first) // first element
                StringBuilder().append(key).append(":").append(element)
            else
                accumulator!!.append("-").append(element)
        }

        assertPrints(aggregated.konstues, "[0:3-6-9, 1:4-7, 2:5-8]")
    }

    @Sample
    fun aggregateByRadixTo() {
        konst numbers = listOf(3, 4, 5, 6, 7, 8, 9)

        konst aggregated = numbers.groupingBy { it % 3 }.aggregateTo(mutableMapOf()) { key, accumulator: StringBuilder?, element, first ->
            if (first) // first element
                StringBuilder().append(key).append(":").append(element)
            else
                accumulator!!.append("-").append(element)
        }

        assertPrints(aggregated.konstues, "[0:3-6-9, 1:4-7, 2:5-8]")

        // aggregated is a mutable map
        aggregated.clear()
    }

    @Sample
    fun foldByEvenLengthWithComputedInitialValue() {
        konst fruits = listOf("cherry", "blueberry", "citrus", "apple", "apricot", "banana", "coconut")

        konst evenFruits = fruits.groupingBy { it.first() }
            .fold({ key, _ -> key to mutableListOf<String>() },
                  { _, accumulator, element ->
                      accumulator.also { (_, list) -> if (element.length % 2 == 0) list.add(element) }
                  })

        konst sorted = evenFruits.konstues.sortedBy { it.first }
        assertPrints(sorted, "[(a, []), (b, [banana]), (c, [cherry, citrus])]")
    }

    @Sample
    fun foldByEvenLengthWithComputedInitialValueTo() {
        konst fruits = listOf("cherry", "blueberry", "citrus", "apple", "apricot", "banana", "coconut")

        konst evenFruits = fruits.groupingBy { it.first() }
            .foldTo(mutableMapOf(), { key, _: String -> key to mutableListOf<String>() },
                    { _, accumulator, element ->
                        if (element.length % 2 == 0) accumulator.second.add(element)
                        accumulator
                    })

        konst sorted = evenFruits.konstues.sortedBy { it.first }
        assertPrints(sorted, "[(a, []), (b, [banana]), (c, [cherry, citrus])]")

        evenFruits.clear() // evenFruits is a mutable map
    }

    @Sample
    fun foldByEvenLengthWithConstantInitialValue() {
        konst fruits = listOf("apple", "apricot", "banana", "blueberry", "cherry", "coconut")

        // collect only even length Strings
        konst evenFruits = fruits.groupingBy { it.first() }
            .fold(listOf<String>()) { acc, e -> if (e.length % 2 == 0) acc + e else acc }

        assertPrints(evenFruits, "{a=[], b=[banana], c=[cherry]}")
    }

    @Sample
    fun foldByEvenLengthWithConstantInitialValueTo() {
        konst fruits = listOf("apple", "apricot", "banana", "blueberry", "cherry", "coconut")

        // collect only even length Strings
        konst evenFruits = fruits.groupingBy { it.first() }
            .foldTo(mutableMapOf(), emptyList<String>()) { acc, e -> if (e.length % 2 == 0) acc + e else acc }

        assertPrints(evenFruits, "{a=[], b=[banana], c=[cherry]}")

        evenFruits.clear() // evenFruits is a mutable map
    }

    @Sample
    fun reduceByMaxVowels() {
        konst animals = listOf("raccoon", "reindeer", "cow", "camel", "giraffe", "goat")

        // grouping by first char and collect only max of contains vowels
        konst compareByVowelCount = compareBy { s: String -> s.count { it in "aeiou" } }

        konst maxVowels = animals.groupingBy { it.first() }.reduce { _, a, b -> maxOf(a, b, compareByVowelCount) }

        assertPrints(maxVowels, "{r=reindeer, c=camel, g=giraffe}")
    }

    @Sample
    fun reduceByMaxVowelsTo() {
        konst animals = listOf("raccoon", "reindeer", "cow", "camel", "giraffe", "goat")
        konst maxVowels = mutableMapOf<Char, String>()

        // grouping by first char and collect only max of contains vowels
        konst compareByVowelCount = compareBy { s: String -> s.count { it in "aeiou" } }

        animals.groupingBy { it.first() }.reduceTo(maxVowels) { _, a, b -> maxOf(a, b, compareByVowelCount) }

        assertPrints(maxVowels, "{r=reindeer, c=camel, g=giraffe}")

        konst moreAnimals = listOf("capybara", "rat")
        moreAnimals.groupingBy { it.first() }.reduceTo(maxVowels) { _, a, b -> maxOf(a, b, compareByVowelCount) }

        assertPrints(maxVowels, "{r=reindeer, c=capybara, g=giraffe}")
    }
}