// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto_extras.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated;

/**
 * Protobuf type {@code org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto}
 */
public final class IdeaExtrasProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)
    IdeaExtrasProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use IdeaExtrasProto.newBuilder() to construct.
  private IdeaExtrasProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private IdeaExtrasProto() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new IdeaExtrasProto();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 1:
        return internalGetValues();
      default:
        throw new RuntimeException(
            "Inkonstid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.class, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder.class);
  }

  public static final int VALUES_FIELD_NUMBER = 1;
  private static final class ValuesDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, com.google.protobuf.ByteString> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, com.google.protobuf.ByteString>newDefaultInstance(
                org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.BYTES,
                com.google.protobuf.ByteString.EMPTY);
  }
  private com.google.protobuf.MapField<
      java.lang.String, com.google.protobuf.ByteString> konstues_;
  private com.google.protobuf.MapField<java.lang.String, com.google.protobuf.ByteString>
  internalGetValues() {
    if (konstues_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          ValuesDefaultEntryHolder.defaultEntry);
    }
    return konstues_;
  }

  public int getValuesCount() {
    return internalGetValues().getMap().size();
  }
  /**
   * <code>map&lt;string, bytes&gt; konstues = 1;</code>
   */

  @java.lang.Override
  public boolean containsValues(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetValues().getMap().containsKey(key);
  }
  /**
   * Use {@link #getValuesMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, com.google.protobuf.ByteString> getValues() {
    return getValuesMap();
  }
  /**
   * <code>map&lt;string, bytes&gt; konstues = 1;</code>
   */
  @java.lang.Override

  public java.util.Map<java.lang.String, com.google.protobuf.ByteString> getValuesMap() {
    return internalGetValues().getMap();
  }
  /**
   * <code>map&lt;string, bytes&gt; konstues = 1;</code>
   */
  @java.lang.Override

  public com.google.protobuf.ByteString getValuesOrDefault(
      java.lang.String key,
      com.google.protobuf.ByteString defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, com.google.protobuf.ByteString> map =
        internalGetValues().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;string, bytes&gt; konstues = 1;</code>
   */
  @java.lang.Override

  public com.google.protobuf.ByteString getValuesOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, com.google.protobuf.ByteString> map =
        internalGetValues().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
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
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetValues(),
        ValuesDefaultEntryHolder.defaultEntry,
        1);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<java.lang.String, com.google.protobuf.ByteString> entry
         : internalGetValues().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, com.google.protobuf.ByteString>
      konstues__ = ValuesDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, konstues__);
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
    if (!(obj instanceof org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)) {
      return super.equals(obj);
    }
    org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto other = (org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto) obj;

    if (!internalGetValues().equals(
        other.internalGetValues())) return false;
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
    if (!internalGetValues().getMap().isEmpty()) {
      hash = (37 * hash) + VALUES_FIELD_NUMBER;
      hash = (53 * hash) + internalGetValues().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(byte[] data)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto parseFrom(
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
  public static Builder newBuilder(org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto prototype) {
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
   * Protobuf type {@code org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)
      org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetValues();
        default:
          throw new RuntimeException(
              "Inkonstid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetMutableValues();
        default:
          throw new RuntimeException(
              "Inkonstid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.class, org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.Builder.class);
    }

    // Construct using org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      internalGetMutableValues().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.ProtoExtras.internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor;
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getDefaultInstanceForType() {
      return org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance();
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto build() {
      org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto buildPartial() {
      org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto result = new org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto(this);
      int from_bitField0_ = bitField0_;
      result.konstues_ = internalGetValues();
      result.konstues_.makeImmutable();
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
      if (other instanceof org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto) {
        return mergeFrom((org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto other) {
      if (other == org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto.getDefaultInstance()) return this;
      internalGetMutableValues().mergeFrom(
          other.internalGetValues());
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
              com.google.protobuf.MapEntry<java.lang.String, com.google.protobuf.ByteString>
              konstues__ = input.readMessage(
                  ValuesDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableValues().getMutableMap().put(
                  konstues__.getKey(), konstues__.getValue());
              break;
            } // case 10
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

    private com.google.protobuf.MapField<
        java.lang.String, com.google.protobuf.ByteString> konstues_;
    private com.google.protobuf.MapField<java.lang.String, com.google.protobuf.ByteString>
    internalGetValues() {
      if (konstues_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ValuesDefaultEntryHolder.defaultEntry);
      }
      return konstues_;
    }
    private com.google.protobuf.MapField<java.lang.String, com.google.protobuf.ByteString>
    internalGetMutableValues() {
      onChanged();;
      if (konstues_ == null) {
        konstues_ = com.google.protobuf.MapField.newMapField(
            ValuesDefaultEntryHolder.defaultEntry);
      }
      if (!konstues_.isMutable()) {
        konstues_ = konstues_.copy();
      }
      return konstues_;
    }

    public int getValuesCount() {
      return internalGetValues().getMap().size();
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */

    @java.lang.Override
    public boolean containsValues(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetValues().getMap().containsKey(key);
    }
    /**
     * Use {@link #getValuesMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, com.google.protobuf.ByteString> getValues() {
      return getValuesMap();
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */
    @java.lang.Override

    public java.util.Map<java.lang.String, com.google.protobuf.ByteString> getValuesMap() {
      return internalGetValues().getMap();
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */
    @java.lang.Override

    public com.google.protobuf.ByteString getValuesOrDefault(
        java.lang.String key,
        com.google.protobuf.ByteString defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, com.google.protobuf.ByteString> map =
          internalGetValues().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */
    @java.lang.Override

    public com.google.protobuf.ByteString getValuesOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, com.google.protobuf.ByteString> map =
          internalGetValues().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearValues() {
      internalGetMutableValues().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */

    public Builder removeValues(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableValues().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, com.google.protobuf.ByteString>
    getMutableValues() {
      return internalGetMutableValues().getMutableMap();
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */
    public Builder putValues(
        java.lang.String key,
        com.google.protobuf.ByteString konstue) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (konstue == null) {
  throw new NullPointerException("map konstue");
}

      internalGetMutableValues().getMutableMap()
          .put(key, konstue);
      return this;
    }
    /**
     * <code>map&lt;string, bytes&gt; konstues = 1;</code>
     */

    public Builder putAllValues(
        java.util.Map<java.lang.String, com.google.protobuf.ByteString> konstues) {
      internalGetMutableValues().getMutableMap()
          .putAll(konstues);
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


    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto)
  private static final org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto();
  }

  public static org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<IdeaExtrasProto>
      PARSER = new com.google.protobuf.AbstractParser<IdeaExtrasProto>() {
    @java.lang.Override
    public IdeaExtrasProto parsePartialFrom(
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

  public static com.google.protobuf.Parser<IdeaExtrasProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<IdeaExtrasProto> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

