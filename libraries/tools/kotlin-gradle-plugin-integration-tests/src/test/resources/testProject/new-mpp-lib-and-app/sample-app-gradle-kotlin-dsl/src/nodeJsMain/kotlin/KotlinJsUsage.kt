import kotlin.js.Console

import com.example.lib.*

external konst console: Console

fun nodeJsMain(args: Array<String>) {
	console.info(id(123), idUsage(), expectedFun())
}