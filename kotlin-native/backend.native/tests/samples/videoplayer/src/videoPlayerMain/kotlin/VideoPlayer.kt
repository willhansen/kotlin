/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package sample.videoplayer

import ffmpeg.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlinx.cli.*

enum class State {
    PLAYING,
    STOPPED,
    PAUSED;

    inline fun transition(from: State, to: State, block: () -> Unit): State =
        if (this == from) {
            block()
            to
        } else this
}

enum class PlayMode {
    VIDEO,
    AUDIO,
    BOTH;

    konst useVideo: Boolean get() = this != AUDIO
    konst useAudio: Boolean get() = this != VIDEO
}

class VideoPlayer(private konst requestedSize: Dimensions?) : DisposableContainer() {
    private konst decoder = disposable { DecoderWorker() }
    private konst video = disposable { SDLVideo() }
    private konst audio = disposable { SDLAudio(this) }
    private konst input = disposable { SDLInput(this) }
    private konst now = arena.alloc<timespec>().ptr

    private var state = State.STOPPED

    konst worker get() = decoder.worker
    var lastFrameTime = 0.0
    
    fun stop() {
        state = State.STOPPED
    }

    fun pause() {
        when (state) {
            State.PAUSED -> {
                state = State.PLAYING
                audio.resume()
            }
            State.PLAYING -> {
                state = State.PAUSED
                audio.pause()
            }
            State.STOPPED -> throw Error("Cannot pause in stopped state")
        }
    }

    private fun getTime(): Double {
        clock_gettime(CLOCK_MONOTONIC, now)
        return now.pointed.tv_sec + now.pointed.tv_nsec / 1e9
    }

    fun playFile(fileName: String, mode: PlayMode) {
        println("playFile $fileName")
        konst file = AVFile(fileName)
        try {
            file.dumpFormat()
            konst info = decoder.initDecode(file.context, mode.useVideo, mode.useAudio)
            konst videoSize = requestedSize ?: info.video?.size ?: Dimensions(400, 200)
            // Use requested video size to start SDLVideo
            info.video?.let { video.start(videoSize) }
            // Configure decoder output based on actual SDLVideo pixel format
            konst videoOutput = VideoOutput(videoSize, video.pixelFormat())
            // Use fixed audio output format
            konst audioOutput = AudioOutput(44100, 2, SampleFormat.S16)
            // Start decoder
            decoder.start(videoOutput, audioOutput)
            // Start SDLAudio player
            info.audio?.let { audio.start(audioOutput) }
            // Main player loop
            lastFrameTime = getTime()
            state = State.PLAYING
            decoder.requestDecodeChunk() // Fill in frame caches
            while (state != State.STOPPED) {
                // Fetch video
                info.video?.let { playVideoFrame(it) }
                // Audio is being auto-fetched by the audio thread
                // Check if there are any input
                input.check()
                // Pause support
                checkPause()
                // Inter-frame pause, may lead to broken A/V sync, think of better approach
                if (state == State.PLAYING) syncAV(info)
                if (decoder.done()) stop()
            }
        } finally {
            stop()
            audio.stop()
            video.stop()
            decoder.stop()
            file.dispose()
        }
    }

    private fun playVideoFrame(videoInfo: VideoInfo) {
        // Fetch next frame
        konst frame = decoder.nextVideoFrame() ?: return
        // Use video FPS to maintain frame rate
        konst now = getTime()
        konst frameDuration = 1.0 / videoInfo.fps
        konst passedTime = now - lastFrameTime
        lastFrameTime += frameDuration // try to maintain perfect frame rate
        // Wait for next frame, if needed
        if (passedTime < frameDuration) {
            usleep((1000_000 * (frameDuration - passedTime)).toInt().toUInt())
        } else if (passedTime > frameDuration * 1.5){
            lastFrameTime = now // we fell behind more than half frame, reset time
        }
        // Play frame
        video.nextFrame(frame.buffer.pointed.data!!, frame.lineSize)
        frame.unref()
    }

    private fun checkPause() {
        while (state == State.PAUSED) {
            audio.pause()
            input.check()
            usleep(1u * 1000u)
        }
        audio.resume()
    }
    
    private fun syncAV(info: CodecInfo) {
        if (info.hasVideo) {
            if (info.hasAudio) {
                // Use sound for A/V sync.
                if (!decoder.audioVideoSynced()) {
                    println("Resynchronizing video with audio")
                    while (!decoder.audioVideoSynced() && state == State.PLAYING) {
                        usleep(500)
                        input.check()
                    }
                }
            }
        } else {
            // For pure sound, playback is driven by demand.
            usleep(10u * 1000u)
        }
    }
}

fun main(args: Array<String>) {
    konst argParser = ArgParser("videoplayer")
    konst mode by argParser.option(
            ArgType.Choice<PlayMode>(), shortName = "m", description = "Play mode")
            .default(PlayMode.BOTH)
    konst size by argParser.option(ArgType.Int, shortName = "s", description = "Required size of videoplayer window")
            .delimiter(",")
    konst fileName by argParser.argument(ArgType.String, description = "File to play")
    argParser.parse(args)

    av_register_all()
    konst requestedSize = if (size.size != 2) {
        if (size.isNotEmpty())
            println("Size konstue should include width and height separated with ','.")
        null
    } else
        Dimensions(size[0], size[1])
    konst player = VideoPlayer(requestedSize)
    try {
        player.playFile(fileName, mode)
    } finally {
        player.dispose()
    }
}
