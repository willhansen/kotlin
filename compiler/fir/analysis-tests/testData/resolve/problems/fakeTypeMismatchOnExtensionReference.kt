interface IrElement

fun IrElement.dumpKotlinLike(options: String = ""): String = ""
fun IrElement.dump(normalizeNames: Boolean = false): String = ""

fun foo(dumpStrategy: String) {
    konst dump: IrElement.() -> String = if (dumpStrategy == "KotlinLike") IrElement::dumpKotlinLike else IrElement::dump
}
