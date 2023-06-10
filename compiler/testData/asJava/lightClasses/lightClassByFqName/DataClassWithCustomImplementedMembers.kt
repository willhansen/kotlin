// p.Wrapper
package p

class Wrapper {
    data class Equals(konst code: G) {
        override fun equals(other: Any?): Boolean = true
    }

    data class HashCode(konst code: G) {
        override fun hashCode() = 3
    }

    data class ToString(konst code: G) {
        override fun toString() = "b"
    }
}

class G
