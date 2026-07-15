package com.nuxnamdeep.converter

/**
 * Cuantificador de pesos para modelos NAM
 * 
 * Convierte pesos de punto flotante (Double) a bytes (0-255)
 * para su uso en dispositivos NUX MG-300
 */
object Quantizer {
    private const val MAX_BYTE = 255

    /**
     * Cuantifica una lista de pesos flotantes a bytes
     * 
     * @param weights Lista de pesos (Double) del modelo NAM
     * @param maxValue Valor máximo de los pesos (por defecto 2.0)
     * @return ByteArray con los pesos cuantificados (0-255)
     */
    fun quantize(weights: List<Double>, maxValue: Double = 2.0): ByteArray {
        val result = ByteArray(weights.size)
        
        for (i in weights.indices) {
            // Normalizar de [-maxValue, maxValue] a [0, 1]
            var normalized = (weights[i] / maxValue + 1.0) / 2.0
            normalized = normalized.coerceIn(0.0, 1.0)
            
            // Convertir a byte (0-255)
            result[i] = (normalized * MAX_BYTE).toInt().toByte()
        }
        
        return result
    }

    /**
     * Descuantifica un ByteArray a pesos flotantes
     * (operación inversa para depuración)
     * 
     * @param bytes ByteArray con pesos cuantificados
     * @param maxValue Valor máximo de los pesos originales
     * @return Lista de Double con los pesos reconstruidos
     */
    fun dequantize(bytes: ByteArray, maxValue: Double = 2.0): List<Double> {
        val result = mutableListOf<Double>()
        
        for (b in bytes) {
            val normalized = (b.toInt() and 0xFF) / MAX_BYTE.toDouble()
            val value = (normalized * 2.0 - 1.0) * maxValue
            result.add(value)
        }
        
        return result
    }
}
