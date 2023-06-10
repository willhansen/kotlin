@file:JvmName("Utils")
@file:JvmMultifileClass

konst bVal: Int get() = 0

class OuterClass{
    inner class InnerClass {
        konst getZero: Int get() = 0
    }
}