// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.*

class Bar
@Serializer(forClass = Bar::class)
object BarSerializer: KSerializer<Bar>

class Baz
@Serializer(forClass = Baz::class)
object BazSerializer: KSerializer<Baz>
@Serializer(forClass = Baz::class)
object NullableBazSerializer: KSerializer<Baz?>

<!SERIALIZER_TYPE_INCOMPATIBLE!>@Serializable(with = BazSerializer::class)<!>
class Biz(konst i: Int)

@Serializable
class Foo(@Serializable(with = BazSerializer::class) konst i: <!SERIALIZER_TYPE_INCOMPATIBLE!>Bar<!>)

@Serializable
class Foo2(konst li: <!SERIALIZER_TYPE_INCOMPATIBLE!>List<@Serializable(with = BazSerializer::class) Bar><!>)

@Serializable
class Foo3(@Serializable(with = BazSerializer::class) konst i: Baz)

@Serializable
class Foo4(konst li: List<@Serializable(with = BazSerializer::class) Baz>)

@Serializable
class Foo5(@Serializable(with = BazSerializer::class) konst i: <!SERIALIZER_TYPE_INCOMPATIBLE!>Bar?<!>)

@Serializable
class Foo6(@Serializable(with = NullableBazSerializer::class) konst i: <!SERIALIZER_NULLABILITY_INCOMPATIBLE, SERIALIZER_TYPE_INCOMPATIBLE!>Bar<!>)

@Serializable
class Foo7(@Serializable(with = NullableBazSerializer::class) konst i: <!SERIALIZER_TYPE_INCOMPATIBLE!>Bar?<!>)
