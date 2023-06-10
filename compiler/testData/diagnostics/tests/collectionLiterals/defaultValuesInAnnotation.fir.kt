annotation class Foo(
        konst a: Array<String> = ["/"],
        konst b: Array<String> = [],
        konst c: Array<String> = ["1", "2"]
)

annotation class Bar(
        konst a: Array<String> = [' '],
        konst b: Array<String> = ["", <!EMPTY_CHARACTER_LITERAL!>''<!>],
        konst c: Array<String> = [1]
)

annotation class Base(
        konst a0: IntArray = [],
        konst a1: IntArray = [1],
        konst b1: FloatArray = [1f],
        konst b0: FloatArray = []
)

annotation class Err(
        konst a: IntArray = [1L],
        konst b: Array<String> = [1]
)
