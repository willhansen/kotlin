open class OpenClass1(private konst x: Int) {
    final override fun toString() = "${this::class.simpleName}(x=$x)"
}
open class OpenClass2(private konst x: String) {
    final override fun toString() = "${this::class.simpleName}(x=$x)"
}
typealias OpenClassRemovedTA = OpenClass1
typealias OpenClassChangedTA = OpenClass1
public typealias OpenClassNarrowedVisibilityTA = OpenClass1
