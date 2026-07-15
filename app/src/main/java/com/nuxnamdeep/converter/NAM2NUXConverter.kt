package com.nuxnamdeep.converter

import com.google.gson.Gson
import com.nuxnamdeep.models.NAMModel
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Conversor principal de archivos NAM a formato NUX MG-300
 * 
 * Lee un archivo NAM (.json), extrae los pesos de los submodelos,
 * los cuantifica y los empaqueta en el formato USB de NUX
 */
object NAM2NUXConverter {
    private const val CHUNK_SIZE = 256
    private const val SLOT_SIZE = 1928

    /**
     * Convierte un archivo NAM a formato NUX
     * 
     * @param namFile Archivo .nam o .json del modelo
     * @param modelName Nombre del modelo (por defecto "Model")
     * @return ByteArray con el slot NUX completo
     */
    fun convert(namFile: File, modelName: String = "Model"): ByteArray {
        val json = namFile.readText()
        val model = Gson().fromJson(json, NAMModel::class.java)
        
        // Usar el nombre del modelo del metadata si está disponible
        val finalName = if (model.metadata.name.isNotEmpty()) {
            model.metadata.name
        } else {
            modelName
        }

        // Extraer submodelos
        val sub1 = model.config.submodels.getOrElse(0) { 
            throw IllegalArgumentException("No se encontró el submodelo 1")
        }
        val sub2 = model.config.submodels.getOrElse(1) { 
            throw IllegalArgumentException("No se encontró el submodelo 2")
        }

        // Cuantificar pesos
        val weights1 = Quantizer.quantize(sub1.model.weights, sub1.maxValue)
        val weights2 = Quantizer.quantize(sub2.model.weights, sub2.maxValue)

        // Construir el slot completo
        val output = ByteArrayOutputStream()
        
        // 1. Cabecera del slot
        output.write(USBPacker.buildSlotHeader(finalName, SLOT_SIZE))
        
        // 2. Submodelo 1 (offset 0x0000)
        output.write(USBPacker.packForUSB(weights1, 0x0000))
        
        // 3. Submodelo 2 (offset 0x0011, luego incrementando)
        var offset = 0x0011
        for (i in weights2.indices step CHUNK_SIZE) {
            val end = minOf(i + CHUNK_SIZE, weights2.size)
            val chunk = weights2.copyOfRange(i, end)
            output.write(USBPacker.packForUSB(chunk, offset))
            offset += CHUNK_SIZE
        }
        
        return output.toByteArray()
    }

    /**
     * Convierte un archivo NAM y lo guarda en el sistema de archivos
     * 
     * @param namFile Archivo .nam o .json del modelo
     * @param outputDir Directorio donde guardar el archivo convertido
     * @param modelName Nombre del modelo (opcional)
     * @return File con el archivo convertido
     */
    fun convertAndSave(
        namFile: File, 
        outputDir: File, 
        modelName: String = "Model"
    ): File {
        val data = convert(namFile, modelName)
        val fileName = "nam_slot1_${modelName.lowercase().replace(" ", "_")}_FULL.txt"
        val outputFile = File(outputDir, fileName)
        outputFile.writeBytes(data)
        return outputFile
    }
}
