package com.dakuo.asmtest

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

fun addCustomAttributeToJarInMemory(
    inputJarStream: InputStream,
    attributeName: String,
    attributeData: String
): InputStream {
    // 创建一个内存中的字节数组输出流，用于保存修改后的 JAR
    val outputStream = ByteArrayOutputStream()

    // 创建一个 Map 用于存储解压的文件
    val fileEntries = mutableMapOf<String, ByteArray>()

    // 解压 JAR 文件到内存
    JarInputStream(inputJarStream).use { jarInput ->
        var entry: JarEntry? = jarInput.nextJarEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                jarInput.copyTo(byteArrayOutputStream)
                fileEntries[entry.name] = byteArrayOutputStream.toByteArray()
            }
            entry = jarInput.nextJarEntry
        }
    }

    // 修改 .class 文件
    fileEntries.forEach { (name, content) ->
        if (name.endsWith(".class")) {
            fileEntries[name] = addCustomAttributeToClass(content, attributeName, attributeData)
        }
    }

    // 将修改后的文件重新打包成新的 JAR
    JarOutputStream(outputStream).use { jarOutput ->
        fileEntries.forEach { (name, content) ->
            val jarEntry = JarEntry(name)
            jarOutput.putNextEntry(jarEntry)
            jarOutput.write(content)
            jarOutput.closeEntry()
        }
    }

    // 返回新的 JAR 文件的 InputStream
    return ByteArrayInputStream(outputStream.toByteArray())
}


fun main() {
    val inputJarPath = "jar/AsmAttribute-1.0.0.jar" // 输入的 JAR 文件路径
    val outputJarPath = "jar/AsmAttribute-1.0.0.out.jar" // 输出的 JAR 文件路径
    val attributeName = "HiddenAttribute"
    val attributeData = "这是一条隐藏信息1"

    val input = addCustomAttributeToJarInMemory(FileInputStream(inputJarPath), attributeName, attributeData)

    FileOutputStream(outputJarPath).use {
        input.copyTo(it)
    }

    readCustomAttributesFromJar(outputJarPath)

}