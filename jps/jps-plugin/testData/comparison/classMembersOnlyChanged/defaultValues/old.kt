package test

class A {
    fun argumentAdded() {}
    fun argumentRemoved(x: Int = 2) {}

    fun konstueAdded(x: Int) {}
    fun konstueRemoved(x: Int = 4) {}
    fun konstueChanged(x: Int = 5) {}
}

class ConstructorValueAdded(x: Int)
class ConstructorValueRemoved(x: Int = 8)
class ConstructorValueChanged(x: Int = 19)

class ConstructorArgumentAdded()
class ConstructorArgumentRemoved(x: Int = 10)