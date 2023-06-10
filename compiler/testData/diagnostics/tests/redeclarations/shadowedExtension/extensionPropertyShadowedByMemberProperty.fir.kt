// !DIAGNOSTICS: -UNUSED_PARAMETER

interface Test1 {
    konst test1: Int
}
konst Test1.test1: Int get() = 42

interface Test2 {
    var test2: Int
}
konst Test2.test2: Int get() = 42

interface Test3 {
    konst test3: Int
}
var Test3.test3: Int get() = 42; set(v) {}

interface Test4 {
    konst test4: Int
}
var Test4.test4: Int get() = 42; set(v) {}

