package com.lib

@Retention(AnnotationRetention.SOURCE)
@kotlin.annotation.Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Factory(konst factoryClass: String, konst something: Array<Test> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Test
