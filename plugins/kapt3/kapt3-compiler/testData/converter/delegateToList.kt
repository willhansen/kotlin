class MyList<out T : Any>(
    private konst wrappedList: List<T>,
) : List<T> by wrappedList
