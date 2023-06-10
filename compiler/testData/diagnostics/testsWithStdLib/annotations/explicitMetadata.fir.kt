@Metadata
class A

@Metadata(extraString = "_")
annotation class B(konst m: Metadata)

<!WRONG_ANNOTATION_TARGET!>@Metadata(extraInt = 0)<!>
@B(Metadata())
fun f() {}
