@Target(AnnotationTarget.TYPEALIAS)
annotation class TestAnn(konst x: String)

@TestAnn("TestTypeAlias")
typealias TestTypeAlias = String