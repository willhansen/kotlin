//                      Nothing
//                      │ fun TODO(): Nothing
//                      │ │
fun <T> genericFoo(): T = TODO()

//        T                  fun <T> genericFoo<T>(): T
//        │                  │
konst <T> T.generic: T get() = genericFoo()
