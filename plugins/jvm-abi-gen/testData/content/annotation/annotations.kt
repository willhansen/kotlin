package test

@Retention(AnnotationRetention.SOURCE)
private annotation class A1(konst x: Int)

@Retention(AnnotationRetention.BINARY)
private annotation class A2(konst x: Int)

@Retention(AnnotationRetention.RUNTIME)
private annotation class A3(konst x: Int)

@Retention(AnnotationRetention.SOURCE)
annotation class B1(konst x: Int)

@Retention(AnnotationRetention.BINARY)
annotation class B2(konst x: Int)

@Retention(AnnotationRetention.RUNTIME)
annotation class B3(konst x: Int)

@A1(0) @A2(1) @A3(2) class T1

@B1(0) @B2(1) @B3(2) class T2
