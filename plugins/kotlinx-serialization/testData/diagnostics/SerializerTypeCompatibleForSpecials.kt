// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.*

class Bar
@Serializer(forClass = Bar::class)
object BarSerializer: KSerializer<Bar>

class Baz
@Serializer(forClass = Baz::class)
object BazSerializer: KSerializer<Baz>

@Serializable
class Foo1(@Polymorphic konst i: Baz)

@Serializable
class Foo2(konst li: MutableList<@Serializable(with = BazSerializer::class) Baz>)
