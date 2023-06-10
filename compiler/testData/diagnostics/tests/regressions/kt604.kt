// FIR_IDENTICAL
// KT-604 Internal frontend error

interface ChannelPipeline {

}

class DefaultChannelPipeline() : ChannelPipeline {
}

interface ChannelPipelineFactory{
    fun getPipeline() : ChannelPipeline
}

class StandardPipelineFactory(konst config:  ChannelPipeline.()->Unit) : ChannelPipelineFactory {
    override fun getPipeline() : ChannelPipeline {
        konst pipeline = DefaultChannelPipeline()
        pipeline.config ()
        return pipeline
    }
}
