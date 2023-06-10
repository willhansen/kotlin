// WITH_STDLIB

class Environment(
    konst fieldAccessedInsideChild: Int,
    konst how: Environment.() -> Unit
)
fun box(): String {
    Environment(
        3,
        {
            class Child {
                konst a = fieldAccessedInsideChild
            }
            class Parent {
                konst children: List<Child> =
                    (0..4).map { Child() }
            }
        }
    )

    return "OK"
}