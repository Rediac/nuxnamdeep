package com.nuxnamdeep

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nuxnamdeep.converter.NAM2NUXConverter
import com.nuxnamdeep.services.FileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    // ============================================
    // MIDI
    // ============================================
    private lateinit var midiManager: MidiManager
    private var midiDevice: MidiDevice? = null
    private var midiOutputPort: MidiOutputPort? = null

    // ============================================
    // UI
    // ============================================
    private lateinit var btnSelectFile: Button
    private lateinit var btnConvert: Button
    private lateinit var btnSend: Button
    private lateinit var btnShare: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvFileName: TextView

    // ============================================
    // Datos
    // ============================================
    private var selectedFile: File? = null
    private var convertedData: ByteArray? = null
    private var convertedFile: File? = null

    // ============================================
    // Permisos
    // ============================================
    private val PERMISSION_REQUEST_CODE = 100
    private val FILE_PICK_REQUEST_CODE = 1001

    // ============================================
    // Ciclo de vida
    // ============================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupMIDI()
        checkPermissions()
        handleIncomingFile()
    }

    // ============================================
    // Configuración de UI
    // ============================================
    private fun setupViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnConvert = findViewById(R.id.btnConvert)
        btnSend = findViewById(R.id.btnSend)
        btnShare = findViewById(R.id.btnShare)
        tvStatus = findViewById(R.id.tvStatus)
        tvFileName = findViewById(R.id.tvFileName)

        btnSelectFile.setOnClickListener { selectFile() }
        btnConvert.setOnClickListener { convertFile() }
        btnSend.setOnClickListener { sendToPedal() }
        btnShare.setOnClickListener { shareFile() }

        btnConvert.isEnabled = false
        btnSend.isEnabled = false
        btnShare.isEnabled = false
    }

    // ============================================
    // Manejo de archivo recibido desde otra app
    // ============================================
    private fun handleIncomingFile() {
        intent?.let { intent ->
            when (intent.action) {
                Intent.ACTION_VIEW -> {
                    intent.data?.let { uri ->
                        try {
                            val inputStream = contentResolver.openInputStream(uri)
                            val file = File(cacheDir, "incoming_${System.currentTimeMillis()}.nam")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            selectedFile = file
                            tvFileName.text = "📄 ${uri.lastPathSegment ?: "archivo"}"
                            btnConvert.isEnabled = true
                            tvStatus.text = "Archivo recibido ✅"
                        } catch (e: Exception) {
                            tvStatus.text = "❌ Error al leer archivo: ${e.message}"
                        }
                    }
                }
                Intent.ACTION_SEND -> {
                    intent.getParcelableExtra<android.os.Parcelable>(Intent.EXTRA_STREAM)?.let { parcelable ->
                        val uri = parcelable as android.net.Uri
                        try {
                            val inputStream = contentResolver.openInputStream(uri)
                            val file = File(cacheDir, "incoming_${System.currentTimeMillis()}.nam")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            selectedFile = file
                            tvFileName.text = "📄 ${uri.lastPathSegment ?: "archivo"}"
                            btnConvert.isEnabled = true
                            tvStatus.text = "Archivo recibido ✅"
                        } catch (e: Exception) {
                            tvStatus.text = "❌ Error al leer archivo: ${e.message}"
                        }
                    }
                }
                else -> {
                    // No hay acción específica
                }
            }
        }
    }

    // ============================================
    // Permisos
    // ============================================
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                tvStatus.text = "⚠️ Algunos permisos no fueron concedidos"
            }
        }
    }

    // ============================================
    // Seleccionar archivo
    // ============================================
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
        }
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, "selected_${System.currentTimeMillis()}.nam")
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    selectedFile = file
                    tvFileName.text = "📄 ${uri.lastPathSegment ?: "archivo"}"
                    btnConvert.isEnabled = true
                    tvStatus.text = "Archivo seleccionado ✅"
                } catch (e: Exception) {
                    tvStatus.text = "❌ Error al leer archivo: ${e.message}"
                }
            }
        }
    }

    // ============================================
    // Convertir archivo
    // ============================================
    private fun convertFile() {
        selectedFile?.let { file ->
            btnConvert.isEnabled = false
            tvStatus.text = "🔄 Convirtiendo..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val name = file.nameWithoutExtension
                    convertedData = NAM2NUXConverter.convert(file, name)

                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val outputFile = File(
                        downloadsDir,
                        "nam_slot1_${name.lowercase().replace(" ", "_")}_FULL.txt"
                    )
                    outputFile.writeBytes(convertedData!!)
                    convertedFile = outputFile

                    withContext(Dispatchers.Main) {
                        tvStatus.text = "✅ Conversión exitosa! (${convertedData?.size ?: 0} bytes)"
                        btnSend.isEnabled = true
                        btnShare.isEnabled = true
                        btnConvert.isEnabled = true
                        tvFileName.text = "📄 ${outputFile.name}"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "❌ Error: ${e.message}"
                        btnConvert.isEnabled = true
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Selecciona un archivo NAM primero", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // Enviar a pedalera
    // ============================================
    private fun sendToPedal() {
        convertedData?.let { data ->
            btnSend.isEnabled = false
            tvStatus.text = "📤 Enviando a pedalera..."

            if (midiDevice == null || midiOutputPort == null) {
                tvStatus.text = "❌ Pedalera no conectada"
                btnSend.isEnabled = true
                return
            }

            try {
                val packets = data.toList().chunked(512)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        for ((index, chunk) in packets.withIndex()) {
                            val chunkArray = chunk.toByteArray()
                            midiOutputPort?.send(chunkArray, 0, chunkArray.size)

                            Thread.sleep(50)

                            withContext(Dispatchers.Main) {
                                tvStatus.text = "📤 Enviando... ${index + 1}/${packets.size}"
                            }
                        }

                        withContext(Dispatchers.Main) {
                            tvStatus.text = "✅ Envío completado! (${data.size} bytes)"
                            btnSend.isEnabled = true
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "❌ Error al enviar: ${e.message}"
                            btnSend.isEnabled = true
                        }
                    }
                }
            } catch (e: Exception) {
                tvStatus.text = "❌ Error al enviar: ${e.message}"
                btnSend.isEnabled = true
            }
        } ?: run {
            Toast.makeText(this, "Convierte un archivo primero", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // Compartir archivo
    // ============================================
    private fun shareFile() {
        convertedFile?.let { file ->
            FileService.shareFile(this, file)
        } ?: run {
            Toast.makeText(this, "No hay archivo para compartir", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // MIDI
    // ============================================
    private fun setupMIDI() {
        midiManager = getSystemService(MIDI_SERVICE) as MidiManager

        midiManager.registerDeviceCallback(object : MidiManager.DeviceCallback() {
            override fun onDeviceAdded(deviceInfo: MidiDeviceInfo?) {
                deviceInfo?.let { connectDevice(it) }
            }

            override fun onDeviceRemoved(deviceInfo: MidiDeviceInfo?) {
                closeMIDI()
                runOnUiThread {
                    tvStatus.text = "⚠️ Pedalera desconectada"
                }
            }
        }, null)

        val devices = midiManager.devices
        for (device in devices) {
            connectDevice(device)
        }
    }

    private fun connectDevice(deviceInfo: MidiDeviceInfo) {
        try {
            midiManager.openDevice(deviceInfo, { device ->
                midiDevice = device
                val outputPorts = deviceInfo.outputPortCount
                if (outputPorts > 0) {
                    midiOutputPort = device.openOutputPort(0)
                    runOnUiThread {
                        val productName = deviceInfo.properties.get(MidiDeviceInfo.PROPERTY_PRODUCT)
                        tvStatus.text = "✅ Pedalera conectada: ${productName ?: "NUX"}"
                    }
                }
            }, null)
        } catch (e: Exception) {
            runOnUiThread {
                tvStatus.text = "❌ Error al conectar: ${e.message}"
            }
        }
    }

    private fun closeMIDI() {
        midiOutputPort?.close()
        midiOutputPort = null
        midiDevice?.close()
        midiDevice = null
    }

    override fun onDestroy() {
        super.onDestroy()
        closeMIDI()
    }
}
