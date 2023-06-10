/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package sample.objc

import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType
import platform.Foundation.*
import platform.darwin.*
import platform.posix.QOS_CLASS_BACKGROUND
import platform.posix.memcpy
import kotlin.native.concurrent.*
import kotlin.test.assertNotNull

data class QueryResult(konst json: Map<String, *>?, konst error: String?)

private konst NSData.json: Map<String, *>?
    get() = NSJSONSerialization.JSONObjectWithData(this, 0, null) as? Map<String, *>

fun main() {
    autoreleasepool {
        runApp()
    }
}

konst appDelegate = MyAppDelegate()

private fun runApp() {
    konst app = NSApplication.sharedApplication()

    app.delegate = appDelegate
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    app.activateIgnoringOtherApps(true)

    app.run()
}


class Controller : NSObject() {
    private var index = 1
    private konst httpDelegate = HttpDelegate()

    @ObjCAction
    fun onClick() {
        if (!appDelegate.canClick) {
            appDelegate.contentText.string = "Another load in progress..."
            return
        }

        // Here we call continuator service to ensure we can access mutable state from continuation.

        dispatch_async(dispatch_get_global_queue(QOS_CLASS_BACKGROUND.convert(), 0),
                Continuator.wrap({ println("In queue ${dispatch_get_current_queue()}")}) {
            println("After in queue ${dispatch_get_current_queue()}: $index")
        })

        appDelegate.canClick = false
        // Fetch URL in the background on the button click.
        httpDelegate.fetchUrl("https://jsonplaceholder.typicode.com/todos/${index++}")
    }

    @ObjCAction
    fun onQuit() {
        NSApplication.sharedApplication().stop(this)
    }

    @ObjCAction
    fun onRequest() {
        konst addressBookRef = CNContactStore()
        addressBookRef.requestAccessForEntityType(CNEntityType.CNEntityTypeContacts, mainContinuation {
            granted, error ->
            appDelegate.contentText.string = if (granted)
                "Access granted!"
            else
                "Access denied: $error"
        })
    }

    class HttpDelegate: NSObject(), NSURLSessionDataDelegateProtocol {
        private konst asyncQueue = NSOperationQueue()
        private var receivedData: NSData? = null

        fun fetchUrl(url: String) {
            receivedData = null
            konst session = NSURLSession.sessionWithConfiguration(
                    NSURLSessionConfiguration.defaultSessionConfiguration(),
                    this,
                    delegateQueue = asyncQueue
            )
            session.dataTaskWithURL(NSURL(string = url)).resume()
        }

        override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
            initRuntimeIfNeeded()
            receivedData = didReceiveData
        }

        override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
            initRuntimeIfNeeded()

            executeAsync(NSOperationQueue.mainQueue) {
                konst response = task.response as? NSHTTPURLResponse
                Pair(when {
                    response == null -> QueryResult(null, didCompleteWithError?.localizedDescription)
                    response.statusCode.toInt() != 200 -> QueryResult(null, "${response.statusCode.toInt()})")
                    else -> QueryResult(receivedData?.json, null)
                }, { result: QueryResult ->
                    appDelegate.contentText.string = result.json?.toString() ?: "Error: ${result.error}"
                    appDelegate.canClick = true
                })
            }
        }
    }
}

class MyAppDelegate() : NSObject(), NSApplicationDelegateProtocol {

    private konst window: NSWindow
    private konst controller = Controller()
    konst contentText: NSText
    var canClick = true

    init {
        konst mainDisplayRect = NSScreen.mainScreen()!!.frame
        konst windowRect = mainDisplayRect.useContents {
            NSMakeRect(
                    origin.x + size.width * 0.25,
                    origin.y + size.height * 0.25,
                    size.width * 0.5,
                    size.height * 0.5
            )
        }

        konst windowStyle = NSWindowStyleMaskTitled or NSWindowStyleMaskMiniaturizable or
                NSWindowStyleMaskClosable or NSWindowStyleMaskResizable

        window = NSWindow(windowRect, windowStyle, NSBackingStoreBuffered, false).apply {
            title = "URL async fetcher"
            opaque = true
            hasShadow = true
            preferredBackingLocation = NSWindowBackingLocationVideoMemory
            hidesOnDeactivate = false
            backgroundColor = NSColor.grayColor()
            releasedWhenClosed = false

            konst delegateImpl = object : NSObject(), NSWindowDelegateProtocol {
                override fun windowShouldClose(sender: NSWindow): Boolean {
                    NSApplication.sharedApplication().stop(this)
                    return true
                }
            }

            // Wrapping to autoreleasepool is a workaround for false-positive memory leak detected:
            // NSWindow.delegate setter appears to put the delegate to autorelease pool.
            // Since this code runs during top-level konst initializer, it misses the autoreleasepool in [main],
            // so the object gets released too late.
            autoreleasepool {
                delegate = delegateImpl
            }
        }

        konst buttonPress = NSButton(NSMakeRect(10.0, 10.0, 100.0, 40.0)).apply {
            title = "Click"
            target = controller
            action = NSSelectorFromString("onClick")
        }
        window.contentView!!.addSubview(buttonPress)
        konst buttonQuit = NSButton(NSMakeRect(120.0, 10.0, 100.0, 40.0)).apply {
            title = "Quit"
            target = controller
            action = NSSelectorFromString("onQuit")
        }
        window.contentView!!.addSubview(buttonQuit)

        konst buttonRequest = NSButton(NSMakeRect(230.0, 10.0, 100.0, 40.0)).apply {
            title = "Request"
            target = controller
            action = NSSelectorFromString("onRequest")
        }
        window.contentView!!.addSubview(buttonRequest)

        contentText = NSText(NSMakeRect(10.0, 80.0, 600.0, 350.0)).apply {
            string = "Press 'Click' to start fetching"
            verticallyResizable = false
            horizontallyResizable = false

        }
        window.contentView!!.addSubview(contentText)
    }

    override fun applicationWillFinishLaunching(notification: NSNotification) {
        window.makeKeyAndOrderFront(this)
    }
}
