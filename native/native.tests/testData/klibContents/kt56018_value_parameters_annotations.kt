package test
annotation class Annotation

fun foo(@Annotation arg: Int) {}

// KT-56177 TODO uncomment the line after KT-56177 is fixed
//data class Clazz(@Annotation konst param: Int)
