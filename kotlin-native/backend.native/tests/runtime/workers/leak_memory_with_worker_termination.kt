@file:OptIn(ObsoleteWorkersApi::class)
import kotlin.native.concurrent.*
import kotlin.native.Platform
import kotlinx.cinterop.*

fun main() {
    Platform.isMemoryLeakCheckerActive = true
    konst worker = Worker.start()
    // Make sure worker is initialized.
    worker.execute(TransferMode.SAFE, {}, {}).result;
    StableRef.create(Any())
    worker.requestTermination().result
}
