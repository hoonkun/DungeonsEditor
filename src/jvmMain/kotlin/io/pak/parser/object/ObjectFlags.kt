package io.pak.parser.`object`

enum class ObjectFlags(val value: UInt) {
    NoFlags(0x00000000u),
    Public(0x00000001u),
    Standalone(0x00000002u),
    MarkAsNative(0x00000004u),
    Transactional(0x00000008u),
    ClassDefaultObject(0x00000010u),
    ArchetypeObject(0x00000020u),
    Transient(0x00000040u),

    MarkAsRootSet(0x00000080u),
    TagGarbageTemp(0x00000100u),

    NeedInitialization(0x00000200u),
    NeedLoad(0x00000400u),
    KeepForCooker(0x00000800u),
    NeedPostLoad(0x00001000u),
    NeedPostLoadSubObjects(0x00002000u),
    NewerVersionExists(0x00004000u),
    BeginDestroyed(0x00008000u),
    FinishDestroyed(0x00010000u),

    BeingRegenerated(0x00020000u),
    DefaultSubObject(0x00040000u),
    WasLoaded(0x00080000u),
    TextExportTransient(0x00100000u),
    LoadCompleted(0x00200000u),
    InheritableComponentTemplate(0x00400000u),
    DuplicateTransient(0x00800000u),
    StrongRefOnFrame(0x01000000u),
    NonPIEDuplicateTransient(0x02000000u),
    Dynamic(0x04000000u),
    WillBeLoaded(0x08000000u),
}