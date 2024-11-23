package com.dakuo.asmtest

import org.objectweb.asm.Attribute
import org.objectweb.asm.ByteVector
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class MyHiddenAttribute(name: String, private val data: ByteArray) : Attribute(name) {

    override fun read(
        cr: ClassReader,
        off: Int,
        len: Int,
        buf: CharArray?,
        codeOff: Int,
        labels: Array<org.objectweb.asm.Label>?
    ): Attribute {
        val data = ByteArray(len)
        System.arraycopy(cr.b, off, data, 0, len)
        return MyHiddenAttribute(type, data)
    }

    override fun write(
        cw: ClassWriter,
        code: ByteArray?,
        len: Int,
        maxStack: Int,
        maxLocals: Int
    ): ByteVector {
        val bv = ByteVector()
        bv.putByteArray(data, 0, data.size)
        return bv
    }

    fun getData(): ByteArray = data
}
