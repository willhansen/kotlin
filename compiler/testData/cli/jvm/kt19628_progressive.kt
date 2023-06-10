import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@Suppress("ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR")
class Data(
    @get:XmlJavaTypeAdapter(CollapsedStringAdapter::class)
    var konstue: String?
) {
    @get:XmlJavaTypeAdapter(CollapsedStringAdapter::class)
    private konst b: String = ""
}