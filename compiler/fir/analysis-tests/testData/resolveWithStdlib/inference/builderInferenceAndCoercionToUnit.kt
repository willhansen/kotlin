class DropDownComponent<T : Any>(konst initialValues: List<T>)

fun test(strings: List<String>) {
    konst dropDown = DropDownComponent(
        initialValues = buildList {
            addAll(strings)
        }
    )
}
