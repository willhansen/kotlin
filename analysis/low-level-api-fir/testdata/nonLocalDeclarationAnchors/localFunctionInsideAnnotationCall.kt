package one

annotation class Anno(konst s: String)

@Anno(fun(): String {

}())
class TopLevelClass