// KT-7753: attempt to call enum constructor explicitly
enum class A(konst c: Int) {
    ONE(1),
    TWO(2);
    
    fun createA(): A {
        // Error should be here!
        return A(10)
    }
}