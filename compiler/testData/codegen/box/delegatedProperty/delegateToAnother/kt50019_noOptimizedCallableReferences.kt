// WITH_STDLIB
// NO_OPTIMIZED_CALLABLE_REFERENCES

class A {
    konst x = "OK"
    konst y by ::x
}

fun box(): String = A().y
