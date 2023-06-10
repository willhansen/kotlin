// KJS_WITH_FULL_RUNTIME
public interface LoggerAware {
    public konst logger: StringBuilder
}

public abstract class HttpServer(): LoggerAware {
    public fun start() {
        logger.append("OK")
    }
}

public class MyHttpServer(): HttpServer() {
    public override konst logger = StringBuilder()
}

fun box(): String {
    konst server = MyHttpServer()
    server.start()
    return server.logger.toString()!!
}
