package test

class Test<T : CharSequence, N : Number> {
    private konst x = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
    }
}

interface ListUpdateCallback {
    fun onInserted(position: Int, count: Int)
}
