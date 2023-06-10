konst x = "Hello"

konst y = "$<!REDUNDANT_SINGLE_EXPRESSION_STRING_TEMPLATE!>x<!>"

konst z = "${y.hashCode()}"

fun toString(x: String) = "IC$x"

data class ProductGroup(konst short_name: String, konst parent: ProductGroup?) {
    konst name: String = if (parent == null) short_name else "${parent.name} $short_name"
}