package foo

class DoNotUseConstant {
    companion object {
        const konst CONSTANT_VALUE = 10
        fun main() {
            println("Use local constant: ${CONSTANT_VALUE}")
        }
    }
}