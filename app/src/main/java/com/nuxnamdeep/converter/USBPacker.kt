package com.nuxnamdeep.converter

import java.io.ByteArrayOutputStream

/**
 * Empaquetador de datos para formato USB de NUX MG-300
 * 
 * Basado en captura USB real de NUX QuickTone
 * Formato: 0x04 0x43 0x58 (CX) + datos + checksum
 */
object USBPacker {
    private const val SEPARATOR = 0x04
    private const val CHUNK_SIZE = 256

    /**
     * Empaca datos en el formato USB de NUX QuickTone
     * 
     * @param data ByteArray con los datos a enviar
     * @param offset Dirección de memoria donde escribir (hex)
     * @return ByteArray con el paquete USB formateado
     */
    fun packForUSB(data: ByteArray, offset: Int): ByteArray {
        val packet = ByteArrayOutputStream()
        
        // Cabecera: 0x04 0x43 0x58 (CX)
        packet.write(0x04)
        packet.write(0x43)
        packet.write(0x58)
        
        // Tipo de operación (0x70 = datos)
        packet.write(0x04)
        packet.write(0x70)
        packet.write(0x58)
        
        // Offset (little-endian, 2 bytes)
        packet.write(offset and 0xFF)
        packet.write((offset shr 8) and 0xFF)
        
        // Padding (8 bytes)
        packet.write(0x04)
        packet.write(0x00)
        packet.write(0x00)
        packet.write(0x00)
        packet.write(0x00)
        packet.write(0x00)
        packet.write(0x00)
        packet.write(0x00)
        
        // Datos (cada byte precedido por 0x04)
        for (byte in data) {
            packet.write(SEPARATOR)
            packet.write(byte.toInt() and 0xFF)
        }
        
        // Checksum (XOR de todos los bytes de datos)
        var checksum = 0
        for (byte in data) {
            checksum = checksum xor (byte.toInt() and 0xFF)
        }
        packet.write(SEPARATOR)
        packet.write(checksum)
        
        return packet.toByteArray()
    }

    /**
     * Construye la cabecera del slot NUX
     * 
     * @param modelName Nombre del modelo (hasta 13 caracteres)
     * @param totalSize Tamaño total del slot (1928 bytes)
     * @return ByteArray con la cabecera formateada
     */
    fun buildSlotHeader(modelName: String, totalSize: Int): ByteArray {
        val header = ByteArrayOutputStream()
        
        // ID Sysex
        header.write(0xF0)
        header.write(0x43)
        header.write(0x58)
        header.write(0x70)
        header.write(0x58)
        
        // Dirección (slot 1)
        header.write(0x01)
        header.write(0x00)
        header.write(0x00)
        header.write(0x0C)
        
        // Configuración
        header.write(0x00)
        header.write(0x00)
        header.write(0x00)
        header.write(0x00)
        header.write(0x00)
        header.write(0x00)
        header.write(0x00)
        header.write(0x02)
        header.write(0x00)
        header.write(0x00)
        header.write(0x40)
        
        // Nombre del modelo (13 bytes)
        val nameBytes = modelName.take(13).toByteArray()
        header.write(nameBytes)
        repeat(13 - nameBytes.size) { header.write(0x00) }
        
        // Padding
        header.write(0x01)
        repeat(23) { header.write(0x00) }
        
        // Tamaños de submodelos (3 y 8)
        header.write(0x0C)
        header.write(0x08)
        
        // Padding y offset
        repeat(48) { header.write(0x00) }
        
        // Tamaño total
        header.write(totalSize and 0xFF)
        header.write((totalSize shr 8) and 0xFF)
        
        // Fecha (2026-07-15)
        header.write(0x32)
        header.write(0x64)
        header.write(0x48)
        header.write(0x11)
        header.write(0x23)
        header.write(0x06)
        
        // Padding final
        repeat(16) { header.write(0x00) }
        
        // Fin de Sysex
        header.write(0xF7)
        
        return header.toByteArray()
    }
}
