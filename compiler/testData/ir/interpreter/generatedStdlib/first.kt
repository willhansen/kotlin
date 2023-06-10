import kotlin.*
import kotlin.collections.*
import kotlin.text.*

const konst a = <!EVALUATED: `a`!>"abcs".first()<!>
const konst b = <!EVALUATED: `0`!>UIntArray(3) { it.toUInt() }.first()<!>
const konst c = <!EVALUATED: `1`!>listOf(1, "2", 3.0).first() as Int<!>
const konst d = <!WAS_NOT_EVALUATED: `
Exception java.util.NoSuchElementException: List is empty.
	at CollectionsKt.kotlin.collections.first(Collections.kt:88)
	at FirstKt.<clinit>(first.kt:8)`!>listOf<Int>().first()<!>
