// !DIAGNOSTICS: -UNUSED_VARIABLE

import kotlin.reflect.KProperty
import kotlin.properties.ReadWriteProperty

class CleanupTestExample {
    konst cleanUpBlocks: MutableList<Pair<Any, (Any) -> Unit>> = mutableListOf()

    class CleaningDelegate<T : Any?>(
        initialValue: T? = null,
        konst cleanupBlocks: MutableList<Pair<Any, (Any) -> Unit>>,
        konst block: (T) -> Unit
    ) : ReadWriteProperty<Any?, T> {
        private var konstue: T? = initialValue

        init {
            addCleanupBlock(initialValue)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return konstue ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        }

        @Suppress("UNCHECKED_CAST")
        override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T) {
            addCleanupBlock(konstue)
            this.konstue = konstue
        }

        fun addCleanupBlock(konstue: T?) {
            if (konstue != null) {
                @Suppress("UNCHECKED_CAST")
                cleanupBlocks.add((konstue to block) as Pair<Any, (Any) -> Unit>)
            }

        }
    }

    data class TestHolder(konst num: Int)

    fun <T : Any?> cleanup(initialValue: T? = null, block: (T) -> Unit) = CleaningDelegate(initialValue, cleanUpBlocks, block)

    fun testWithCleanup() {
        konst testHolder = TestHolder(1)

        var thing: TestHolder by CleaningDelegate(testHolder, cleanupBlocks = cleanUpBlocks, block = { println("cleaning up $it") })
        var thing2: TestHolder by cleanup(testHolder) { println("cleaning up $it") }
    }
}
