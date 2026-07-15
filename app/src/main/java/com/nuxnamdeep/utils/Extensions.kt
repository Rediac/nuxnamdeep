package com.nuxnamdeep.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * Extension functions y utilities para el proyecto
 */
// ============================================
// Toast extensions
// ============================================

fun AppCompatActivity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun android.content.Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// ============================================
// String extensions
// ============================================

fun String.toHexString(): String {
    return this.map { "%02X".format(it.code and 0xFF) }.joinToString(" ")
}

fun String.toHexStringNoSpaces(): String {
    return this.map { "%02X".format(it.code and 0xFF) }.joinToString("")
}

// ============================================
// ByteArray extensions
// ============================================

fun ByteArray.toHexString(): String {
    return this.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
}

fun ByteArray.toHexStringNoSpaces(): String {
    return this.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
}

fun ByteArray.toHexStringWithPrefix(): String {
    return this.joinToString(" ") { "0x%02X".format(it.toInt() and 0xFF) }
}

fun ByteArray.contentEqualsIgnoreCase(other: ByteArray): Boolean {
    if (this.size != other.size) return false
    for (i in this.indices) {
        if (this[i].toInt() and 0xFF != other[i].toInt() and 0xFF) return false
    }
    return true
}

// ============================================
// File extensions
// ============================================

fun File.getExtension(): String {
    return this.extension.lowercase()
}

fun File.getNameWithoutExtension(): String {
    val name = this.name
    val lastDot = name.lastIndexOf('.')
    return if (lastDot > 0) name.substring(0, lastDot) else name
}

fun File.isNAMFile(): Boolean {
    val ext = this.getExtension()
    return ext == "nam" || ext == "json"
}

fun File.isNUXFile(): Boolean {
    return this.getExtension() == "txt"
}

// ============================================
// Int extensions
// ============================================

fun Int.toHexString(): String {
    return "%02X".format(this and 0xFF)
}

fun Int.toHexStringWithPrefix(): String {
    return "0x%02X".format(this and 0xFF)
}

// ============================================
// List extensions (CORREGIDO - nombres únicos)
// ============================================

fun List<Byte>.toByteArrayFromBytes(): ByteArray {
    return this.toByteArray()
}

fun List<Int>.toByteArrayFromInts(): ByteArray {
    val result = ByteArray(this.size)
    for (i in this.indices) {
        result[i] = this[i].toByte()
    }
    return result
}
