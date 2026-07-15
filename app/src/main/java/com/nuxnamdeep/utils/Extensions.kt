package com.nuxnamdeep.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * Extension functions y utilities para el proyecto
 * 
 * Proporciona funciones auxiliares para simplificar tareas comunes
 */
// ============================================
// Toast extensions
// ============================================

/**
 * Muestra un Toast corto
 * 
 * @param message Mensaje a mostrar
 * @param duration Duración del Toast (Toast.LENGTH_SHORT o Toast.LENGTH_LONG)
 */
fun AppCompatActivity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Muestra un Toast corto desde cualquier contexto
 * 
 * @param message Mensaje a mostrar
 */
fun android.content.Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// ============================================
// String extensions
// ============================================

/**
 * Convierte un String a su representación hexadecimal
 * 
 * @return String con bytes en formato hexadecimal
 */
fun String.toHexString(): String {
    return this.map { "%02X".format(it.code and 0xFF) }.joinToString(" ")
}

/**
 * Convierte un String a su representación hexadecimal sin espacios
 * 
 * @return String con bytes en formato hexadecimal sin espacios
 */
fun String.toHexStringNoSpaces(): String {
    return this.map { "%02X".format(it.code and 0xFF) }.joinToString("")
}

// ============================================
// ByteArray extensions
// ============================================

/**
 * Convierte un ByteArray a su representación hexadecimal
 * 
 * @return String con bytes en formato hexadecimal
 */
fun ByteArray.toHexString(): String {
    return this.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
}

/**
 * Convierte un ByteArray a su representación hexadecimal sin espacios
 * 
 * @return String con bytes en formato hexadecimal sin espacios
 */
fun ByteArray.toHexStringNoSpaces(): String {
    return this.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
}

/**
 * Convierte un ByteArray a su representación hexadecimal con prefijo 0x
 * 
 * @return String con bytes en formato hexadecimal con prefijo
 */
fun ByteArray.toHexStringWithPrefix(): String {
    return this.joinToString(" ") { "0x%02X".format(it.toInt() and 0xFF) }
}

/**
 * Verifica si un ByteArray es igual a otro ignorando el caso de los bytes
 * 
 * @param other Otro ByteArray para comparar
 * @return true si los arrays son iguales
 */
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

/**
 * Obtiene la extensión de un archivo
 * 
 * @return Extensión del archivo (ej: "txt", "json")
 */
fun File.getExtension(): String {
    return this.extension.lowercase()
}

/**
 * Obtiene el nombre del archivo sin extensión
 * 
 * @return Nombre del archivo sin extensión
 */
fun File.getNameWithoutExtension(): String {
    val name = this.name
    val lastDot = name.lastIndexOf('.')
    return if (lastDot > 0) name.substring(0, lastDot) else name
}

/**
 * Verifica si el archivo es un archivo NAM (.nam o .json)
 * 
 * @return true si el archivo tiene extensión .nam o .json
 */
fun File.isNAMFile(): Boolean {
    val ext = this.getExtension()
    return ext == "nam" || ext == "json"
}

/**
 * Verifica si el archivo es un archivo NUX (.txt)
 * 
 * @return true si el archivo tiene extensión .txt
 */
fun File.isNUXFile(): Boolean {
    return this.getExtension() == "txt"
}

// ============================================
// Int extensions
// ============================================

/**
 * Convierte un Int a su representación hexadecimal
 * 
 * @return String con el Int en formato hexadecimal
 */
fun Int.toHexString(): String {
    return "%02X".format(this and 0xFF)
}

/**
 * Convierte un Int a su representación hexadecimal con prefijo 0x
 * 
 * @return String con el Int en formato hexadecimal con prefijo
 */
fun Int.toHexStringWithPrefix(): String {
    return "0x%02X".format(this and 0xFF)
}

// ============================================
// List extensions
// ============================================

/**
 * Convierte una lista de bytes a ByteArray
 * 
 * @return ByteArray con los bytes de la lista
 */
fun List<Byte>.toByteArray(): ByteArray {
    return this.toByteArray()
}

/**
 * Convierte una lista de Int a ByteArray
 * 
 * @return ByteArray con los Int convertidos a bytes
 */
fun List<Int>.toByteArray(): ByteArray {
    val result = ByteArray(this.size)
    for (i in this.indices) {
        result[i] = this[i].toByte()
    }
    return result
}
