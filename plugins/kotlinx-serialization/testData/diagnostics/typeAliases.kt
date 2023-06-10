// FIR_IDENTICAL
// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.*

typealias MyString = String
typealias MyLong = Long

@Serializable
class Box<T>(konst t: T)

typealias MyBox<T> = Box<T>

@Serializable
class Foo(
    konst s: MyString,
    konst l: MyLong,
    konst b: Box<MyLong>,
    konst bb: MyBox<Long>,
    konst bbb: MyBox<MyLong>
)