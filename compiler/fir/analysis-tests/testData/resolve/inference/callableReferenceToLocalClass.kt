fun <T, R> List<T>.myMap(block: (T) -> R): List<R> = null!!

fun test_1() {
    class Data(konst x: Int)
    konst datas: List<Data> = null!!
    konst xs = datas.myMap(Data::x)
}
