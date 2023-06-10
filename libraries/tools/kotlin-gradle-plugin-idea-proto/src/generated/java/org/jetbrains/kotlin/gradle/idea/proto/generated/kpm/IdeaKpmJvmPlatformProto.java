// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

/**
 * Protobuf type {@code org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto}
 */
public final class IdeaKpmJvmPlatformProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)
    IdeaKpmJvmPlatformProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use IdeaKpmJvmPlatformProto.newBuilder() to construct.
  private IdeaKpmJvmPlatformProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private IdeaKpmJvmPlatformProto() {
    jvmTarget_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new IdeaKpmJvmPlatformProto();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ProtoKpm.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_kpm_IdeaKpmJvmPlatformProto_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ProtoKpm.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_kpm_IdeaKpmJvmPlatformProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.class, org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.Builder.class);
  }

  private int bitField0_;
  public static final int EXTRAS_FIELD_NUMBER = 1;
  private org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras_;
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   * @return Whether the extras field is set.
   */
  @java.lang.Override
  public boolean hasExtras() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   * @return The extras.
   */
  @java.lang.Override
  public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getExtras() {
    return extras_ == null ? org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance() : extras_;
  }
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   */
  @java.lang.Override
  public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder getExtrasOrBuilder() {
    return extras_ == null ? org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance() : extras_;
  }

  public static final int JVM_TARGET_FIELD_NUMBER = 2;
  private volatile java.lang.Object jvmTarget_;
  /**
   * <code>optional string jvm_target = 2;</code>
   * @return Whether the jvmTarget field is set.
   */
  @java.lang.Override
  public boolean hasJvmTarget() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string jvm_target = 2;</code>
   * @return The jvmTarget.
   */
  @java.lang.Override
  public java.lang.String getJvmTarget() {
    java.lang.Object ref = jvmTarget_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      jvmTarget_ = s;
      return s;
    }
  }
  /**
   * <code>optional string jvm_target = 2;</code>
   * @return The bytes for jvmTarget.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getJvmTargetBytes() {
    java.lang.Object ref = jvmTarget_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      jvmTarget_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(1, getExtras());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, jvmTarget_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getExtras());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, jvmTarget_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)) {
      return super.equals(obj);
    }
    org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto other = (org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto) obj;

    if (hasExtras() != other.hasExtras()) return false;
    if (hasExtras()) {
      if (!getExtras()
          .equals(other.getExtras())) return false;
    }
    if (hasJvmTarget() != other.hasJvmTarget()) return false;
    if (hasJvmTarget()) {
      if (!getJvmTarget()
          .equals(other.getJvmTarget())) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasExtras()) {
      hash = (37 * hash) + EXTRAS_FIELD_NUMBER;
      hash = (53 * hash) + getExtras().hashCode();
    }
    if (hasJvmTarget()) {
      hash = (37 * hash) + JVM_TARGET_FIELD_NUMBER;
      hash = (53 * hash) + getJvmTarget().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(byte[] data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)
      org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ProtoKpm.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_kpm_IdeaKpmJvmPlatformProto_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ProtoKpm.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_kpm_IdeaKpmJvmPlatformProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.class, org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.Builder.class);
    }

    // Construct using org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getExtrasFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (extrasBuilder_ == null) {
        extras_ = null;
      } else {
        extrasBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      jvmTarget_ = "";
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ProtoKpm.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_kpm_IdeaKpmJvmPlatformProto_descriptor;
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto getDefaultInstanceForType() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.getDefaultInstance();
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto build() {
      org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto buildPartial() {
      org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto result = new org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        if (extrasBuilder_ == null) {
          result.extras_ = extras_;
        } else {
          result.extras_ = extrasBuilder_.build();
        }
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        to_bitField0_ |= 0x00000002;
      }
      result.jvmTarget_ = jvmTarget_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object konstue) {
      return super.setField(field, konstue);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object konstue) {
      return super.setRepeatedField(field, index, konstue);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object konstue) {
      return super.addRepeatedField(field, konstue);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto) {
        return mergeFrom((org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto other) {
      if (other == org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.getDefaultInstance()) return this;
      if (other.hasExtras()) {
        mergeExtras(other.getExtras());
      }
      if (other.hasJvmTarget()) {
        bitField0_ |= 0x00000002;
        jvmTarget_ = other.jvmTarget_;
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              input.readMessage(
                  getExtrasFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              jvmTarget_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InkonstidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras_;
    private com.google.protobuf.SingleFieldBuilderV3<
        org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder> extrasBuilder_;
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     * @return Whether the extras field is set.
     */
    public boolean hasExtras() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     * @return The extras.
     */
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getExtras() {
      if (extrasBuilder_ == null) {
        return extras_ == null ? org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance() : extras_;
      } else {
        return extrasBuilder_.getMessage();
      }
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public Builder setExtras(org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto konstue) {
      if (extrasBuilder_ == null) {
        if (konstue == null) {
          throw new NullPointerException();
        }
        extras_ = konstue;
        onChanged();
      } else {
        extrasBuilder_.setMessage(konstue);
      }
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public Builder setExtras(
        org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder builderForValue) {
      if (extrasBuilder_ == null) {
        extras_ = builderForValue.build();
        onChanged();
      } else {
        extrasBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public Builder mergeExtras(org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto konstue) {
      if (extrasBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
            extras_ != null &&
            extras_ != org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance()) {
          extras_ =
            org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.newBuilder(extras_).mergeFrom(konstue).buildPartial();
        } else {
          extras_ = konstue;
        }
        onChanged();
      } else {
        extrasBuilder_.mergeFrom(konstue);
      }
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public Builder clearExtras() {
      if (extrasBuilder_ == null) {
        extras_ = null;
        onChanged();
      } else {
        extrasBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder getExtrasBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getExtrasFieldBuilder().getBuilder();
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder getExtrasOrBuilder() {
      if (extrasBuilder_ != null) {
        return extrasBuilder_.getMessageOrBuilder();
      } else {
        return extras_ == null ?
            org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance() : extras_;
      }
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder> 
        getExtrasFieldBuilder() {
      if (extrasBuilder_ == null) {
        extrasBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder>(
                getExtras(),
                getParentForChildren(),
                isClean());
        extras_ = null;
      }
      return extrasBuilder_;
    }

    private java.lang.Object jvmTarget_ = "";
    /**
     * <code>optional string jvm_target = 2;</code>
     * @return Whether the jvmTarget field is set.
     */
    public boolean hasJvmTarget() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @return The jvmTarget.
     */
    public java.lang.String getJvmTarget() {
      java.lang.Object ref = jvmTarget_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        jvmTarget_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @return The bytes for jvmTarget.
     */
    public com.google.protobuf.ByteString
        getJvmTargetBytes() {
      java.lang.Object ref = jvmTarget_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        jvmTarget_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @param konstue The jvmTarget to set.
     * @return This builder for chaining.
     */
    public Builder setJvmTarget(
        java.lang.String konstue) {
      if (konstue == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
      jvmTarget_ = konstue;
      onChanged();
      return this;
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearJvmTarget() {
      bitField0_ = (bitField0_ & ~0x00000002);
      jvmTarget_ = getDefaultInstance().getJvmTarget();
      onChanged();
      return this;
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @param konstue The bytes for jvmTarget to set.
     * @return This builder for chaining.
     */
    public Builder setJvmTargetBytes(
        com.google.protobuf.ByteString konstue) {
      if (konstue == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(konstue);
      bitField0_ |= 0x00000002;
      jvmTarget_ = konstue;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto)
  private static final org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto();
  }

  public static org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<IdeaKpmJvmPlatformProto>
      PARSER = new com.google.protobuf.AbstractParser<IdeaKpmJvmPlatformProto>() {
    @java.lang.Override
    public IdeaKpmJvmPlatformProto parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InkonstidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InkonstidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInkonstidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InkonstidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<IdeaKpmJvmPlatformProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<IdeaKpmJvmPlatformProto> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

