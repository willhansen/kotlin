//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

@kotlin.jvm.JvmName("-initializeideaKpmJsPlatformProto")
inline fun ideaKpmJsPlatformProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto.newBuilder()).apply { block() }._build()
object IdeaKpmJsPlatformProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto = _builder.build()

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    var extras: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto
      @JvmName("getExtras")
      get() = _builder.getExtras()
      @JvmName("setExtras")
      set(konstue) {
        _builder.setExtras(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    fun clearExtras() {
      _builder.clearExtras()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     * @return Whether the extras field is set.
     */
    fun hasExtras(): kotlin.Boolean {
      return _builder.hasExtras()
    }
    konst IdeaKpmJsPlatformProtoKt.Dsl.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
      get() = _builder.extrasOrNull

    /**
     * <code>optional bool isIr = 2;</code>
     */
    var isIr: kotlin.Boolean
      @JvmName("getIsIr")
      get() = _builder.getIsIr()
      @JvmName("setIsIr")
      set(konstue) {
        _builder.setIsIr(konstue)
      }
    /**
     * <code>optional bool isIr = 2;</code>
     */
    fun clearIsIr() {
      _builder.clearIsIr()
    }
    /**
     * <code>optional bool isIr = 2;</code>
     * @return Whether the isIr field is set.
     */
    fun hasIsIr(): kotlin.Boolean {
      return _builder.hasIsIr()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJsPlatformProtoOrBuilder.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
  get() = if (hasExtras()) getExtras() else null

