package com.nuxnamdeep

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
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
    // USB (copiado de tu proyecto)
    // ============================================
    private lateinit var usbManager: UsbManager
    private var connection: android.hardware.usb.UsbDeviceConnection? = null
    private var endpointOut: android.hardware.usb.UsbEndpoint? = null

    private val ACTION_USB_PERMISSION = "com.nuxnamdeep.USB_PERMISSION"

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let { connectToDevice(it) }
                }
            }
        }
    }

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
        setupUSB()
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
    // USB (copiado de tu proyecto)
    // ============================================
    private fun setupUSB() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        registerReceiver(permissionReceiver, IntentFilter(ACTION_USB_PERMISSION))
        findDevice()
    }

    private fun findDevice() {
        val devices = usbManager.deviceList
        if (devices.isEmpty()) {
            tvStatus.text = "Conecta la NUX por USB"
            return
        }
        val device = devices.values.first()
        tvStatus.text = "Encontrado: ${device.productName ?: "NUX"}"
        if (usbManager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            requestPermission(device)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val pi = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pi)
    }

    private fun connectToDevice(device: UsbDevice) {
        connection = usbManager.openDevice(device) ?: return
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            connection?.claimInterface(intf, true)
            for (j in 0 until intf.endpointCount) {
                val ep = intf.getEndpoint(j)
                if (ep.direction == UsbConstants.USB_DIR_OUT &&
                    ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    endpointOut = ep
                    tvStatus.text = "✅ Conectado: ${device.productName ?: "NUX"}"
                    return
                }
            }
        }
    }

    // ============================================
    // Enviar datos por USB (copiado de tu proyecto)
    // ============================================
    private fun sendDataToNUX(data: ByteArray): Boolean {
        val conn = connection
        val ep = endpointOut
        if (conn != null && ep != null) {
            val result = conn.bulkTransfer(ep, data, data.size, 2000)
            return result == data.size
        }
        return false
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
                else -> {}
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
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                permissions.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
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
    // Enviar a pedalera (USANDO TU MÉTODO USB)
    // ============================================
    private fun sendToPedal() {
        convertedData?.let { data ->
            btnSend.isEnabled = false
            tvStatus.text = "📤 Enviando a pedalera..."

            if (connection == null || endpointOut == null) {
                tvStatus.text = "❌ Pedalera no conectada"
                btnSend.isEnabled = true
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Dividir en paquetes de 512 bytes (límite USB)
                    val packets = data.toList().chunked(512)

                    for ((index, chunk) in packets.withIndex()) {
                        val chunkArray = chunk.toByteArray()
                        val sent = sendDataToNUX(chunkArray)

                        if (!sent) {
                            withContext(Dispatchers.Main) {
                                tvStatus.text = "❌ Error al enviar paquete ${index + 1}"
                                btnSend.isEnabled = true
                            }
                            return@launch
                        }

                        // Pequeña pausa entre paquetes
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
    // Ciclo de vida
    // ============================================
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(permissionReceiver)
        } catch (e: Exception) {
            // Ya estaba desregistrado
        }
        connection?.close()
    }
}
