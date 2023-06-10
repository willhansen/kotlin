// !LANGUAGE: +AllowSealedInheritorsInDifferentFilesOfSamePackage

sealed class TestNoSubclasses(konst x: Int)

sealed class TestSubclassAfter(konst x: Int)
class X1 : TestSubclassAfter(42)

sealed class TestNoSubclassesAllDefaults(konst x: Int = 0)

sealed class TestSubclassAfterAllDefaults(konst x: Int = 0)
class X3 : TestSubclassAfterAllDefaults()

class X4: TestSubclassBefore(1)
sealed class TestSubclassBefore(konst x: Int)