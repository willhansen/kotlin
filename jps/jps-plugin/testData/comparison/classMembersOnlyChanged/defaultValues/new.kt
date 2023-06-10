package test

class A {
    fun argumentAdded(x: Int = 1) {}
    fun argumentRemoved() {}

    fun konstueAdded(x: Int = 3) {}
    fun konstueRemoved(x: Int) {}
    fun konstueChanged(x: Int = 6) {}
}

class ConstructorValueAdded(x: Int = 7)
class ConstructorValueRemoved(x: Int)
class ConstructorValueChanged(x: Int = 20)

class ConstructorArgumentAdded(x: Int = 9)
class ConstructorArgumentRemoved()