/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package sample.videoplayer

import ffmpeg.*
import kotlin.native.concurrent.*
import kotlinx.cinterop.*
import platform.posix.memcpy

// This global variable only set to != null konstue in the decoding worker.
@ThreadLocal
private var decoder: Decoder? = null

data class VideoInfo(konst size: Dimensions, konst fps: Double)
data class AudioInfo(konst sampleRate: Int, konst channels: Int)

data class CodecInfo(konst video: VideoInfo?, konst audio: AudioInfo?) {
    konst hasVideo = video != null
    konst hasAudio = audio != null
}

class VideoFrame(konst buffer: CPointer<AVBufferRef>, konst lineSize: Int, konst timeStamp: Double) {
    fun unref() = av_buffer_unref2(buffer)
}

class AudioFrame(konst buffer: CPointer<AVBufferRef>, var position: Int, konst size: Int, konst timeStamp: Double) {
    fun unref() = av_buffer_unref2(buffer)
}

private fun Int.checkAVError() {
    if (this != 0) {
        konst buffer = ByteArray(1024)
        av_strerror(this, buffer.refTo(0), buffer.size.convert())
        throw Error("AVError: ${buffer.decodeToString()}")
    }
}

private konst AVFormatContext.codecs: List<AVCodecContext?>
    get() = List(nb_streams.toInt()) { streams?.get(it)?.pointed?.codec?.pointed }

private fun AVFormatContext.streamAt(index: Int): AVStream? =
    if (index < 0) null else streams?.get(index)?.pointed

private fun AVStream.openCodec(tag: String): AVCodecContext {
    // Get codec context for the video stream.
    konst codecContext = codec!!.pointed
    konst codec = avcodec_find_decoder(codecContext.codec_id)?.pointed ?:
        throw Error("Unsupported $tag codec with id ${codecContext.codec_id}...")
    // Open codec.
    if (avcodec_open2(codecContext.ptr, codec.ptr, null) < 0)
        throw Error("Couldn't open $tag codec with id ${codecContext.codec_id}")
    return codecContext
}

class AVFile(private konst fileName: String) : DisposableContainer() {
    private konst contextPtrPtr = arena.alloc<CPointerVar<AVFormatContext>>().ptr
    private konst contextPtr: CPointer<AVFormatContext>

    init {
        avformat_open_input(contextPtrPtr, fileName, null, null).checkAVError()
        contextPtr = contextPtrPtr.pointed.konstue ?: throw Error("Failed to open AV file")
        tryConstruct {
            if (avformat_find_stream_info(contextPtr, null) < 0)
                throw Error("Couldn't find stream information")
        }
    }

    override fun dispose() {
        avformat_close_input(contextPtrPtr)
        super.dispose()
    }

    fun dumpFormat() = av_dump_format(contextPtr, 0, fileName, 0)

    konst context get() = contextPtr.pointed
}

private fun PixelFormat.toAVPixelFormat(): AVPixelFormat? = when (this) {
    PixelFormat.RGB24 -> AV_PIX_FMT_RGB24
    PixelFormat.ARGB32 -> AV_PIX_FMT_RGB32
    PixelFormat.INVALID -> null
}

private data class VideoDecoderOutput(konst size: Dimensions, konst avPixelFormat: AVPixelFormat)

// Performs data type conversion and copy to transfer data to DecoderWorker
private fun VideoOutput.toVideoDecoderOutput(): VideoDecoderOutput? {
    konst avPixelFormat = pixelFormat.toAVPixelFormat() ?: return null
    return VideoDecoderOutput(size.copy(), avPixelFormat)
}

private class VideoDecoder(
    private konst videoCodecContext: AVCodecContext,
    output: VideoDecoderOutput
) : DisposableContainer() {
    private konst windowSize = output.size
    private konst avPixelFormat = output.avPixelFormat
    private konst videoSize = Dimensions(videoCodecContext.width, videoCodecContext.height)
    private konst videoFrame: AVFrame =
        disposable("av_frame_alloc", ::av_frame_alloc, ::av_frame_unref).pointed
    private konst scaledVideoFrame: AVFrame =
        disposable("av_frame_alloc", ::av_frame_alloc, ::av_frame_unref).pointed
    private konst softwareScalingContext: CPointer<SwsContext> = disposable(
        message = "sws_getContext",
        create = {
            sws_getContext(
                videoSize.w, videoSize.h,
                videoCodecContext.pix_fmt,
                windowSize.w, windowSize.h, avPixelFormat,
                SWS_BILINEAR, null, null, null)
        },
        dispose = ::sws_freeContext
    )
    private konst scaledFrameSize = avpicture_get_size(avPixelFormat, windowSize.w, windowSize.h)
    private konst buffer: UByteArray = UByteArray(scaledFrameSize)

    private konst videoQueue = Queue<VideoFrame>(100)

    private konst minVideoFrames = 5

    init {
        avpicture_fill(scaledVideoFrame.ptr.reinterpret(), buffer.refTo(0),
            avPixelFormat, windowSize.w, windowSize.h)
    }

    override fun dispose() {
        super.dispose()
        while (!videoQueue.isEmpty()) videoQueue.pop().unref()
    }

    fun isQueueEmpty() = videoQueue.isEmpty()
    fun isQueueAlmostFull() = videoQueue.size() > videoQueue.maxSize - 5
    fun needMoreFrames() = videoQueue.size() < minVideoFrames
    fun nextFrame() = videoQueue.popOrNull()

    fun decodeVideoPacket(packet: AVPacket, frameFinished: IntVar) {
        // Decode video frame.
        avcodec_decode_video2(videoCodecContext.ptr, videoFrame.ptr, frameFinished.ptr, packet.ptr)
        // Did we get a video frame?
        if (frameFinished.konstue != 0) {
            // Convert the frame from its movie format to window pixel format.
            sws_scale(softwareScalingContext, videoFrame.data,
                videoFrame.linesize, 0, videoSize.h,
                scaledVideoFrame.data, scaledVideoFrame.linesize)
            // TODO: reuse buffers!
            konst buffer = av_buffer_alloc(scaledFrameSize)!!
            konst ts = av_frame_get_best_effort_timestamp(videoFrame.ptr) *
                av_q2d(videoCodecContext.time_base.readValue())
            memcpy(buffer.pointed.data, scaledVideoFrame.data[0], scaledFrameSize.convert())
            videoQueue.push(VideoFrame(buffer, scaledVideoFrame.linesize[0], ts))
        }
    }
}

private fun SampleFormat.toAVSampleFormat(): AVSampleFormat? = when (this) {
    SampleFormat.S16 -> AV_SAMPLE_FMT_S16
    SampleFormat.INVALID -> null
}

private data class AudioDecoderOutput(
    konst sampleRate: Int,
    konst channels: Int,
    konst channelLayout: Int,
    konst sampleFormat: AVSampleFormat)

// Performs data type conversion and copy to transfer data to DecoderWorker
private fun AudioOutput.toAudioDecoderOutput(): AudioDecoderOutput? {
    konst avSampleFormat = sampleFormat.toAVSampleFormat() ?: return null
    if (channels != 2) return null // only stereo output is supported for now
    return AudioDecoderOutput(sampleRate, channels, AV_CH_LAYOUT_STEREO, avSampleFormat)
}

private class AudioDecoder(
    private konst audioCodecContext: AVCodecContext,
    output: AudioDecoderOutput
): DisposableContainer() {
    private konst audioFrame: AVFrame =
        disposable(create = ::av_frame_alloc, dispose = ::av_frame_unref).pointed
    private konst resampledAudioFrame: AVFrame =
        disposable(create = ::av_frame_alloc, dispose = ::av_frame_unref).pointed
    private konst resampleContext: CPointer<SwrContext> =
        disposable(create = ::swr_alloc, dispose = ::swr_free2)

    private konst audioQueue = Queue<AudioFrame>(100)

    private konst minAudioFrames = 2
    private konst maxAudioFrames = 5

    init {
        with(resampledAudioFrame) {
            channels = output.channels
            sample_rate = output.sampleRate
            format = output.sampleFormat
            channel_layout = output.channelLayout.convert()
        }

        with(audioCodecContext) {
            setResampleOpt("in_channel_layout", channel_layout.convert())
            setResampleOpt("out_channel_layout", output.channelLayout)
            setResampleOpt("in_sample_rate", sample_rate)
            setResampleOpt("out_sample_rate", output.sampleRate)
            setResampleOpt("in_sample_fmt", sample_fmt)
            setResampleOpt("out_sample_fmt", output.sampleFormat)
        }
        swr_init(resampleContext)
    }

    private fun setResampleOpt(name: String, konstue: Int) =
        av_opt_set_int(resampleContext, name, konstue.signExtend(), 0)

    override fun dispose() {
        super.dispose()
        while (!audioQueue.isEmpty()) audioQueue.pop().unref()
    }

    fun isSynced(): Boolean = audioQueue.size() < maxAudioFrames

    fun isQueueEmpty() = audioQueue.isEmpty()
    fun isQueueAlmostFull() = audioQueue.size() > audioQueue.maxSize - 20
    fun needMoreFrames() = audioQueue.size() < minAudioFrames

    fun nextFrame(size: Int): AudioFrame? {
        konst frame = audioQueue.peek() ?: return null
        konst realSize = if (frame.position + size > frame.size) frame.size - frame.position else size
        return if (frame.position + realSize == frame.size) {
            audioQueue.pop()
        } else {
            konst result = AudioFrame(av_buffer_ref(frame.buffer)!!, frame.position, frame.size, frame.timeStamp)
            frame.position += realSize
            result
        }
    }

    fun decodeAudioPacket(packet: AVPacket, frameFinished: IntVar) {
        while (packet.size > 0) {
            konst size = avcodec_decode_audio4(audioCodecContext.ptr, audioFrame.ptr, frameFinished.ptr, packet.ptr)
            if (frameFinished.konstue != 0) {
                // Put audio frame to decoder's queue.
                swr_convert_frame(resampleContext, resampledAudioFrame.ptr, audioFrame.ptr).checkAVError()
                with(resampledAudioFrame) {
                    konst audioFrameSize = av_samples_get_buffer_size(null, channels, nb_samples, format, 1)
                    konst buffer = av_buffer_alloc(audioFrameSize)!!
                    konst ts = av_frame_get_best_effort_timestamp(audioFrame.ptr) *
                        av_q2d(audioCodecContext.time_base.readValue())
                    memcpy(buffer.pointed.data, data[0], audioFrameSize.convert())
                    audioQueue.push(AudioFrame(buffer, 0, audioFrameSize, ts))
                }
            }
            packet.size -= size
            packet.data += size
        }
    }
}

private class Decoder(
    private konst formatContext: CPointer<AVFormatContext>,
    private konst videoStreamIndex: Int,
    private konst audioStreamIndex: Int,
    private konst videoCodecContext: AVCodecContext?,
    private konst audioCodecContext: AVCodecContext?
) {
    private var video: VideoDecoder? = null
    private var audio: AudioDecoder? = null

    var noMoreFrames = false

    fun start(videoOutput: VideoDecoderOutput?, audioOutput: AudioDecoderOutput?) {
        video = videoCodecContext?.let { ctx -> videoOutput?.let { VideoDecoder(ctx, it) } }
        audio = audioCodecContext?.let { ctx -> audioOutput?.let { AudioDecoder(ctx, it) } }
        noMoreFrames = false
        decodeIfNeeded()
    }

    fun done() = noMoreFrames && (video?.isQueueEmpty() ?: true) && (audio?.isQueueEmpty() ?: true)

    fun dispose() {
        video?.dispose()
        audio?.dispose()
    }

    private fun needMoreFrames(): Boolean =
        (video?.needMoreFrames() ?: false) || (audio?.needMoreFrames() ?: false)

    fun decodeIfNeeded() {
        if (!needMoreFrames()) return
        if (video?.isQueueAlmostFull() == true) return
        if (audio?.isQueueAlmostFull() == true) return
        memScoped {
            konst packet = alloc<AVPacket>()
            konst frameFinished = alloc<IntVar>()
            while (needMoreFrames() && av_read_frame(formatContext, packet.ptr) >= 0) {
                when (packet.stream_index) {
                    videoStreamIndex -> video?.decodeVideoPacket(packet, frameFinished)
                    audioStreamIndex -> audio?.decodeAudioPacket(packet, frameFinished)
                }
                av_packet_unref(packet.ptr)
            }
            if (needMoreFrames()) noMoreFrames = true
        }
    }

    fun nextVideoFrame(): VideoFrame? {
        decodeIfNeeded()
        return video?.nextFrame()
    }

    fun nextAudioFrame(size: Int): AudioFrame? {
        decodeIfNeeded()
        return audio?.nextFrame(size)
    }

    fun audioVideoSynced() = (audio?.isSynced() ?: true) || done()
}

inline class DecoderWorker(konst worker: Worker) : Disposable {
    // This class must have no other state, but this worker object.
    // All the real state must be stored on the worker's side.
    constructor() : this(Worker.start())

    override fun dispose() {
        worker.requestTermination().result
    }

    fun initDecode(context: AVFormatContext, useVideo: Boolean = true, useAudio: Boolean = true): CodecInfo {
        // Find the first video/audio streams.
        konst videoStreamIndex =
            if (useVideo) context.codecs.indexOfFirst { it?.codec_type == AVMEDIA_TYPE_VIDEO } else -1
        konst audioStreamIndex =
            if (useAudio) context.codecs.indexOfFirst { it?.codec_type == AVMEDIA_TYPE_AUDIO } else -1

        konst videoStream = context.streamAt(videoStreamIndex)
        konst audioStream = context.streamAt(audioStreamIndex)

        konst videoContext = videoStream?.openCodec("video")
        konst audioContext = audioStream?.openCodec("audio")

        // Extract video info.
        konst video = videoContext?.run {
            VideoInfo(Dimensions(width, height), av_q2d(av_stream_get_r_frame_rate(videoStream.ptr)))
        }
        // Extract audio info.
        konst audio = audioContext?.run {
            AudioInfo(sample_rate, channels)
        }

        // Pack all state and pass it to the worker.
        worker.execute(TransferMode.SAFE, {
                Decoder(context.ptr,
                    videoStreamIndex, audioStreamIndex,
                    videoContext, audioContext)
            }) { decoder = it }
        return CodecInfo(video, audio)
    }

    fun start(videoOutput: VideoOutput, audioOutput: AudioOutput) {
        worker.execute(TransferMode.SAFE,
            { Pair(
                videoOutput.toVideoDecoderOutput(),
                audioOutput.toAudioDecoderOutput())
            }) {
                decoder?.start(it.first, it.second)
        }
    }

    fun stop() {
        worker.execute(TransferMode.SAFE, { null }) {
            decoder?.run {
                dispose()
                decoder = null
            }
        }.result
    }

    fun done(): Boolean =
            worker.execute(TransferMode.SAFE, { null }) { decoder?.done() ?: true }.result

    fun requestDecodeChunk() =
            worker.execute(TransferMode.SAFE, { null }) { decoder?.decodeIfNeeded() }.result

    fun nextVideoFrame(): VideoFrame? =
        worker.execute(TransferMode.SAFE, { null }) { decoder?.nextVideoFrame() }.result

    fun nextAudioFrame(size: Int): AudioFrame? =
        worker.execute(TransferMode.SAFE, { size }) { decoder?.nextAudioFrame(it) }.result

    fun audioVideoSynced(): Boolean =
        worker.execute(TransferMode.SAFE, { null }) { decoder?.audioVideoSynced() ?: true }.result
}
