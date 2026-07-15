package com.nuxnamdeep.services

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * Servicio para manejar operaciones con archivos
 * 
 * Proporciona funciones para leer, guardar y compartir archivos
 * en el sistema Android
 */
object FileService {

    /**
     * Guarda datos en un archivo en la carpeta Downloads
     * 
     * @param context Contexto de la aplicación
     * @param data ByteArray con los datos a guardar
     * @param fileName Nombre del archivo
     * @return File con el archivo guardado, o null si hay error
     */
    fun saveToDownloads(context: Context, data: ByteArray, fileName: String): File? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            file.writeBytes(data)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Guarda datos en un archivo en el directorio de caché
     * 
     * @param context Contexto de la aplicación
     * @param data ByteArray con los datos a guardar
     * @param fileName Nombre del archivo
     * @return File con el archivo guardado, o null si hay error
     */
    fun saveToCache(context: Context, data: ByteArray, fileName: String): File? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, fileName)
            file.writeBytes(data)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Comparte un archivo usando Intent
     * 
     * @param context Contexto de la aplicación
     * @param file Archivo a compartir
     * @return true si se inició el intent, false si hay error
     */
    fun shareFile(context: Context, file: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Lee el contenido de un archivo como String
     * 
     * @param file Archivo a leer
     * @return String con el contenido del archivo
     */
    fun readFile(file: File): String {
        return file.readText()
    }

    /**
     * Lee un archivo desde una URI
     * 
     * @param context Contexto de la aplicación
     * @param uri URI del archivo
     * @return File con el contenido, o null si hay error
     */
    fun readFileFromUri(context: Context, uri: android.net.Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.nam")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verifica si un archivo es un archivo NAM válido
     * 
     * @param file Archivo a verificar
     * @return true si es un archivo NAM válido
     */
    fun isValidNAMFile(file: File): Boolean {
        return try {
            val content = file.readText()
            // Verificar que sea JSON válido
            com.google.gson.Gson().fromJson(content, Map::class.java)
            // Verificar que tenga las claves esperadas
            content.contains("\"version\"") && 
            content.contains("\"metadata\"") && 
            content.contains("\"config\"")
        } catch (e: Exception) {
            false
        }
    }
}
