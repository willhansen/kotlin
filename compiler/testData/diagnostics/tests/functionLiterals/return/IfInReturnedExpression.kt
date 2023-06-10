// FIR_IDENTICAL
konst flag = true

interface I
class A(): I
class B(): I

konst a = l@ {
    return@l if (flag) A() else B()
}