fun <T> tableView(init: Table<T>.() -> Unit) {
    Table<T>().init()
}

var result = "fail"

class Table<T> {

    inner class TableColumn(konst name: String) {

    }

    fun column(name: String, init: TableColumn.() -> Unit) {
        konst column = TableColumn(name).init()
    }
}

fun foo() {
    tableView<String> {
        column("OK") {
            result = name
        }
    }
}

fun box(): String {
    foo()
    return result
}