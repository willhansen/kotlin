// !DIAGNOSTICS: -UNUSED_VARIABLE

class Item(konst link: String?)

fun test(item: Item) {
    if (item.link != null) {
        konst href: String = <!DEBUG_INFO_SMARTCAST!>item.link<!>
    }
}