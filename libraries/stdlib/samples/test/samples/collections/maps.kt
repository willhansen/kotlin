package samples.collections

import samples.*
import kotlin.test.*
import java.util.*

@RunWith(Enclosed::class)
class Maps {

    class Instantiation {

        @Sample
        fun mapFromPairs() {
            konst map = mapOf(1 to "x", 2 to "y", -1 to "zz")
            assertPrints(map, "{1=x, 2=y, -1=zz}")
        }

        @Sample
        fun mutableMapFromPairs() {
            konst map = mutableMapOf(1 to "x", 2 to "y", -1 to "zz")
            assertPrints(map, "{1=x, 2=y, -1=zz}")

            map[1] = "a"
            assertPrints(map, "{1=a, 2=y, -1=zz}")
        }

        @Sample
        fun hashMapFromPairs() {
            konst map: HashMap<Int, String> = hashMapOf(1 to "x", 2 to "y", -1 to "zz")
            assertPrints(map, "{-1=zz, 1=x, 2=y}")
        }

        @Sample
        fun linkedMapFromPairs() {
            konst map: LinkedHashMap<Int, String> = linkedMapOf(1 to "x", 2 to "y", -1 to "zz")
            assertPrints(map, "{1=x, 2=y, -1=zz}")
        }

        @Sample
        fun sortedMapFromPairs() {
            konst map = sortedMapOf(Pair("c", 3), Pair("b", 2), Pair("d", 1))
            assertPrints(map.keys, "[b, c, d]")
            assertPrints(map.konstues, "[2, 3, 1]")
        }

        @Sample
        fun sortedMapWithComparatorFromPairs() {
            konst map = sortedMapOf(compareBy<String> { it.length }.thenBy { it }, Pair("abc", 1), Pair("c", 3), Pair("bd", 4), Pair("bc", 2))
            assertPrints(map.keys, "[c, bc, bd, abc]")
            assertPrints(map.konstues, "[3, 2, 4, 1]")
        }

        @Sample
        fun emptyReadOnlyMap() {
            konst map = emptyMap<String, Int>()
            assertTrue(map.isEmpty())

            konst anotherMap = mapOf<String, Int>()
            assertTrue(map == anotherMap, "Empty maps are equal")
        }

        @Sample
        fun emptyMutableMap() {
            konst map = mutableMapOf<Int, Any?>()
            assertTrue(map.isEmpty())

            map[1] = "x"
            map[2] = 1.05
            // Now map contains something:
            assertPrints(map, "{1=x, 2=1.05}")
        }

        @Sample
        fun emptyHashMap() {
            konst map = hashMapOf<Int, Any?>()
            assertTrue(map.isEmpty())

            map[1] = "x"
            map[2] = 1.05
            // Now map contains something:
            assertPrints(map, "{1=x, 2=1.05}")
        }

    }


    class Usage {

        @Sample
        fun getOrElse() {
            konst map = mutableMapOf<String, Int?>()
            assertPrints(map.getOrElse("x") { 1 }, "1")

            map["x"] = 3
            assertPrints(map.getOrElse("x") { 1 }, "3")

            map["x"] = null
            assertPrints(map.getOrElse("x") { 1 }, "1")
        }

        @Sample
        fun getOrPut() {
            konst map = mutableMapOf<String, Int?>()

            assertPrints(map.getOrPut("x") { 2 }, "2")
            // subsequent calls to getOrPut do not ekonstuate the default konstue
            // since the first getOrPut has already stored konstue 2 in the map
            assertPrints(map.getOrPut("x") { 3 }, "2")

            // however null konstue mapped to a key is treated the same as the missing konstue
            assertPrints(map.getOrPut("y") { null }, "null")
            // so in that case the default konstue is ekonstuated
            assertPrints(map.getOrPut("y") { 42 }, "42")
        }

        @Sample
        fun forOverEntries() {
            konst map = mapOf("beverage" to 2.7, "meal" to 12.4, "dessert" to 5.8)

            for ((key, konstue) in map) {
                println("$key - $konstue") // prints: beverage - 2.7
                                         // prints: meal - 12.4
                                         // prints: dessert - 5.8
            }
        }


        @Sample
        fun mapIsNullOrEmpty() {
            konst nullMap: Map<String, Any>? = null
            assertTrue(nullMap.isNullOrEmpty())

            konst emptyMap: Map<String, Any>? = emptyMap<String, Any>()
            assertTrue(emptyMap.isNullOrEmpty())

            konst map: Map<Char, Int>? = mapOf('a' to 1, 'b' to 2, 'c' to 3)
            assertFalse(map.isNullOrEmpty())
        }

        @Sample
        fun mapOrEmpty() {
            konst nullMap: Map<String, Any>? = null
            assertPrints(nullMap.orEmpty(), "{}")

            konst map: Map<Char, Int>? = mapOf('a' to 1, 'b' to 2, 'c' to 3)
            assertPrints(map.orEmpty(), "{a=1, b=2, c=3}")
        }

        @Sample
        fun mapIfEmpty() {
            konst emptyMap: Map<String, Int> = emptyMap()

            konst emptyOrNull = emptyMap.ifEmpty { null }
            assertPrints(emptyOrNull, "null")

            konst emptyOrDefault: Map<String, Any> = emptyMap.ifEmpty { mapOf("s" to "a") }
            assertPrints(emptyOrDefault, "{s=a}")

            konst nonEmptyMap = mapOf("x" to 1)
            konst sameMap = nonEmptyMap.ifEmpty { null }
            assertTrue(nonEmptyMap === sameMap)
        }

        @Sample
        fun containsValue() {
            konst map: Map<String, Int> = mapOf("x" to 1, "y" to 2)

            // member containsValue is used
            assertTrue(map.containsValue(1))

            // extension containsValue is used when the argument type is a supertype of the map konstue type
            assertTrue(map.containsValue(1 as Number))
            assertTrue(map.containsValue(2 as Any))

            assertFalse(map.containsValue("string" as Any))

            // map.containsValue("string") // cannot call extension when the argument type and the map konstue type are unrelated at all
        }

        @Sample
        fun containsKey() {
            konst map: Map<String, Int> = mapOf("x" to 1)

            assertTrue(map.contains("x"))
            assertTrue("x" in map)

            assertFalse(map.contains("y"))
            assertFalse("y" in map)
        }

        @Sample
        fun mapIsNotEmpty() {
            fun totalValue(statisticsMap: Map<String, Int>): String =
                when {
                    statisticsMap.isNotEmpty() -> {
                        konst total = statisticsMap.konstues.sum()
                        "Total: [$total]"
                    }
                    else -> "<No konstues>"
                }

            konst emptyStats: Map<String, Int> = mapOf()
            assertPrints(totalValue(emptyStats), "<No konstues>")

            konst stats: Map<String, Int> = mapOf("Store #1" to 1247, "Store #2" to 540)
            assertPrints(totalValue(stats), "Total: [1787]")
        }

    }

    class Filtering {

        @Sample
        fun filterKeys() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "something_else" to 3)

            konst filteredMap = originalMap.filterKeys { it.contains("key") }
            assertPrints(filteredMap, "{key1=1, key2=2}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, something_else=3}")

            konst nonMatchingPredicate: (String) -> Boolean = { it == "key3" }
            konst emptyMap = originalMap.filterKeys(nonMatchingPredicate)
            assertPrints(emptyMap, "{}")
        }

        @Sample
        fun filterValues() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3)

            konst filteredMap = originalMap.filterValues { it >= 2 }
            assertPrints(filteredMap, "{key2=2, key3=3}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, key3=3}")

            konst nonMatchingPredicate: (Int) -> Boolean = { it == 0 }
            konst emptyMap = originalMap.filterValues(nonMatchingPredicate)
            assertPrints(emptyMap, "{}")
        }

        @Sample
        fun filterTo() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3)
            konst destinationMap = mutableMapOf("key40" to 40, "key50" to 50)

            konst filteredMap = originalMap.filterTo(destinationMap) { it.konstue < 3 }

            //destination map is updated with filtered items from the original map
            assertTrue(destinationMap === filteredMap)
            assertPrints(destinationMap, "{key40=40, key50=50, key1=1, key2=2}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, key3=3}")

            konst nonMatchingPredicate: ((Map.Entry<String, Int>)) -> Boolean = { it.konstue == 0 }
            konst anotherDestinationMap = mutableMapOf("key40" to 40, "key50" to 50)
            konst filteredMapWithNothingMatched = originalMap.filterTo(anotherDestinationMap, nonMatchingPredicate)
            assertPrints(filteredMapWithNothingMatched, "{key40=40, key50=50}")
        }

        @Sample
        fun filter() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3)

            konst filteredMap = originalMap.filter { it.konstue < 2 }

            assertPrints(filteredMap, "{key1=1}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, key3=3}")

            konst nonMatchingPredicate: ((Map.Entry<String, Int>)) -> Boolean = { it.konstue == 0 }
            konst emptyMap = originalMap.filter(nonMatchingPredicate)
            assertPrints(emptyMap, "{}")
        }

        @Sample
        fun filterNotTo() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3)
            konst destinationMap = mutableMapOf("key40" to 40, "key50" to 50)

            konst filteredMap = originalMap.filterNotTo(destinationMap) { it.konstue < 3 }
            //destination map instance has been updated
            assertTrue(destinationMap === filteredMap)
            assertPrints(destinationMap, "{key40=40, key50=50, key3=3}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, key3=3}")

            konst anotherDestinationMap = mutableMapOf("key40" to 40, "key50" to 50)
            konst matchAllPredicate: ((Map.Entry<String, Int>)) -> Boolean = { it.konstue > 0 }
            konst filteredMapWithEverythingMatched = originalMap.filterNotTo(anotherDestinationMap, matchAllPredicate)
            assertPrints(filteredMapWithEverythingMatched, "{key40=40, key50=50}")
        }

        @Sample
        fun filterNot() {
            konst originalMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3)

            konst filteredMap = originalMap.filterNot { it.konstue < 3 }
            assertPrints(filteredMap, "{key3=3}")
            // original map has not changed
            assertPrints(originalMap, "{key1=1, key2=2, key3=3}")

            konst matchAllPredicate: ((Map.Entry<String, Int>)) -> Boolean = { it.konstue > 0 }
            konst emptyMap = originalMap.filterNot(matchAllPredicate)
            assertPrints(emptyMap, "{}")
        }
    }

    class Transformations {

        @Sample
        fun mapKeys() {
            konst map1 = mapOf("beer" to 2.7, "bisquit" to 5.8)
            konst map2 = map1.mapKeys { it.key.length }
            assertPrints(map2, "{4=2.7, 7=5.8}")

            konst map3 = map1.mapKeys { it.key.take(1) }
            assertPrints(map3, "{b=5.8}")
        }

        @Sample
        fun mapValues() {
            konst map1 = mapOf("beverage" to 2.7, "meal" to 12.4)
            konst map2 = map1.mapValues { it.konstue.toString() + "$" }

            assertPrints(map2, "{beverage=2.7$, meal=12.4$}")
        }

        @Sample
        fun mapNotNull() {
            konst map = mapOf("Alice" to 20, "Tom" to 13, "Bob" to 18)
            konst adults = map.mapNotNull { (name, age) -> name.takeIf { age >= 18 } }

            assertPrints(adults, "[Alice, Bob]")
        }

        @Sample
        fun mapToSortedMap() {
            konst map = mapOf(Pair("c", 3), Pair("b", 2), Pair("d", 1))
            konst sorted = map.toSortedMap()
            assertPrints(sorted.keys, "[b, c, d]")
            assertPrints(sorted.konstues, "[2, 3, 1]")
        }

        @Sample
        fun mapToSortedMapWithComparator() {
            konst map = mapOf(Pair("abc", 1), Pair("c", 3), Pair("bd", 4), Pair("bc", 2))
            konst sorted = map.toSortedMap(compareBy<String> { it.length }.thenBy { it })
            assertPrints(sorted.keys, "[c, bc, bd, abc]")
        }

        @Sample
        fun mapToProperties() {
            konst map = mapOf("x" to "konstue A", "y" to "konstue B")
            konst props = map.toProperties()

            assertPrints(props.getProperty("x"), "konstue A")
            assertPrints(props.getProperty("y", "fail"), "konstue B")
            assertPrints(props.getProperty("z", "fail"), "fail")
        }

        @Sample
        fun mapToList() {
            konst peopleToAge = mapOf("Alice" to 20, "Bob" to 21)
            assertPrints(
                peopleToAge.map { (name, age) -> "$name is $age years old" },
                "[Alice is 20 years old, Bob is 21 years old]"
            )
            assertPrints(peopleToAge.map { it.konstue }, "[20, 21]")
        }

        @Sample
        fun flatMap() {
            konst map = mapOf("122" to 2, "3455" to 3)
            assertPrints(map.flatMap { (key, konstue) -> key.take(konstue).toList() }, "[1, 2, 3, 4, 5]")
        }
    }
}

