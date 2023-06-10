// WITH_STDLIB
abstract class Table<T>(
        konst content: Array<Array<T>>
)

fun box(): String {
    konst x = object : Table<String>(
            Array(1, {
                x-> Array(1, {y -> "OK"})
            })
    ) {}

    return x.content[0][0]
}
