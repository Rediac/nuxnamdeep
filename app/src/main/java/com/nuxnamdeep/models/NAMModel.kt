package com.nuxnamdeep.models

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos para archivos NAM (Neural Amp Modeler)
 * 
 * Estructura basada en el formato JSON de NAM
 * Compatible con archivos .nam y .json generados por Tone3000
 */
data class NAMModel(
    val version: String,
    val metadata: NAMMetadata,
    val config: NAMConfig
)

/**
 * Metadatos del modelo NAM
 */
data class NAMMetadata(
    val name: String,
    @SerializedName("modeled_by") val modeledBy: String,
    @SerializedName("gear_make") val gearMake: String,
    @SerializedName("gear_model") val gearModel: String,
    val gain: Double,
    val loudness: Double? = null,
    @SerializedName("input_level_dbu") val inputLevelDbu: Double? = null,
    @SerializedName("output_level_dbu") val outputLevelDbu: Double? = null
)

/**
 * Configuración del modelo NAM
 */
data class NAMConfig(
    val architecture: String,
    val submodels: List<Submodel>
)

/**
 * Submodelo de la red neuronal
 */
data class Submodel(
    @SerializedName("max_value") val maxValue: Double,
    val model: SubmodelData
)

/**
 * Datos del submodelo (contiene los pesos)
 */
data class SubmodelData(
    val weights: List<Double>,
    @SerializedName("sample_rate") val sampleRate: Int? = null
)

/**
 * Slot NUX para almacenar el modelo convertido
 */
data class NUXSlot(
    val slotNumber: Int,
    val modelName: String,
    val data: ByteArray,
    val totalSize: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NUXSlot
        if (slotNumber != other.slotNumber) return false
        if (modelName != other.modelName) return false
        if (!data.contentEquals(other.data)) return false
        if (totalSize != other.totalSize) return false
        return true
    }

    override fun hashCode(): Int {
        var result = slotNumber
        result = 31 * result + modelName.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + totalSize
        return result
    }
}
