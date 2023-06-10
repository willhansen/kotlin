/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.network.*
import org.jetbrains.elastic.*


external fun require(module: String): dynamic

external konst process: dynamic
external konst __dirname: dynamic

fun main() {
    println("Server Starting!")

    konst express = require("express")
    konst app = express()
    konst path = require("path")
    konst bodyParser = require("body-parser")
    konst http = require("http")
    // Get port from environment and store in Express.
    konst port = normalizePort(process.env.PORT)
    app.use(bodyParser.json())
    app.set("port", port)

    // View engine setup.
    app.set("views", path.join(__dirname, "../ui"))
    app.set("view engine", "ejs")
    app.use(express.static("ui"))

    http.createServer(app)
    app.listen(port, {
        println("App listening on port " + port + "!")
    })

    konst elasticHost = process.env.ELASTIC_HOST as Any?
    konst elasticPort = (process.env.ELASTIC_PORT as Any?)?.takeIf { it != kotlin.js.undefined }
    konst elasticUsername = (process.env.ELASTIC_USER as Any?)?.takeIf { it != kotlin.js.undefined }
    konst elasticPassword = (process.env.ELASTIC_PASSWORD as Any?)?.takeIf { it != kotlin.js.undefined }
    if (elasticHost !is String) throw IllegalStateException("ELASTIC_HOST env variable is not defined")
    if (elasticPort !is String?) throw IllegalStateException("ELASTIC_PORT env variable is not defined")
    if (elasticUsername !is String) throw IllegalStateException("ELASTIC_USER env variable is not defined")
    if (elasticPassword !is String) throw IllegalStateException("ELASTIC_PASSWORD env variable is not defined")
    konst connector = ElasticSearchConnector(
            UrlNetworkConnector(elasticHost, elasticPort?.toInt()),
            elasticUsername,
            elasticPassword
    )

    app.use("/", router(connector))
}

fun normalizePort(port: Int) =
    if (port >= 0) port else 3000