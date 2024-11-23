package com.dakuo.asmtest

import org.objectweb.asm.*
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.io.path.createTempDirectory

fun addCustomAttributeToJar(inputJar: String, outputJar: String, attributeName: String, attributeData: String) {
    val jarFile = JarFile(inputJar)
    val tempDir = createTempDirectory().toFile()
    tempDir.deleteOnExit()

    // 解压 JAR 文件
    for (entry in jarFile.entries()) {
        val entryFile = File(tempDir, entry.name)
        if (entry.isDirectory) {
            entryFile.mkdirs()
        } else {
            entryFile.parentFile.mkdirs()
            jarFile.getInputStream(entry).use { input ->
                FileOutputStream(entryFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    // 修改每个 .class 文件
    tempDir.walkTopDown().filter { it.extension == "class" }.forEach { classFile ->
        val modifiedBytes = addCustomAttributeToClass(classFile.readBytes(), attributeName, attributeData)
        classFile.writeBytes(modifiedBytes)
    }

    // 打包成新的 JAR
    val newJarFile = JarOutputStream(FileOutputStream(outputJar))
    tempDir.walkTopDown().forEach { file ->
        val entryName = file.relativeTo(tempDir).path.replace("\\", "/")
        if (file.isFile) {
            val jarEntry = JarEntry(entryName)
            jarEntry.time = file.lastModified()
            newJarFile.putNextEntry(jarEntry)
            file.inputStream().use { it.copyTo(newJarFile) }
            newJarFile.closeEntry()
        }
    }
    newJarFile.close()
}

fun addCustomAttributeToClass(classBytes: ByteArray, attributeName: String, attributeData: String): ByteArray {
    val cr = ClassReader(classBytes)
    val cw = ClassWriter(0)

    val customAttribute = MyHiddenAttribute(attributeName, attributeData.toByteArray())

    cr.accept(object : ClassVisitor(Opcodes.ASM9, cw) {
        override fun visitAttribute(attr: Attribute?) {
            super.visitAttribute(attr)
        }

        override fun visitEnd() {
            super.visitAttribute(customAttribute)
            super.visitEnd()
        }
    }, 0)

    return cw.toByteArray()
}

fun readCustomAttributesFromJar(jarPath: String) {
    val jarFile = JarFile(jarPath)
    jarFile.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { entry ->
        jarFile.getInputStream(entry).use { input ->
            val cr = ClassReader(input)
            val cw = ClassWriter(0)

            cr.accept(object : ClassVisitor(Opcodes.ASM9, cw) {
                override fun visitAttribute(attr: Attribute?) {
                    if (attr?.type == "HiddenAttribute") {
                        // 尝试直接获取自定义属性的数据
                        try {
                            // 通过反射获取私有字段 cachedContent
                            val cachedContentField = attr::class.java.getDeclaredField("cachedContent")
                            cachedContentField.isAccessible = true
                            val cachedContent = cachedContentField.get(attr)

                            // 通过反射获取 cachedContent 的 data 字段
                            val dataField = cachedContent::class.java.getDeclaredField("data")
                            dataField.isAccessible = true
                            val byteArray = dataField.get(cachedContent) as ByteArray

                            // 转换为字符串并输出
                            val hiddenMessage = String(byteArray)
                            println(hiddenMessage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }, ClassReader.SKIP_CODE)
        }
    }
}


fun main() {
    val inputJarPath = "jar/AsmAttribute-1.0.0.jar" // 输入的 JAR 文件路径
    val outputJarPath = "jar/AsmAttribute-1.0.0.out.jar" // 输出的 JAR 文件路径
    val attributeName = "HiddenAttribute"
    val attributeData = "这是一条隐藏信息"

    addCustomAttributeToJar(inputJarPath, outputJarPath, attributeName, attributeData)

    readCustomAttributesFromJar("jar/AsmAttribute-1.0.0.out.jar")


}
