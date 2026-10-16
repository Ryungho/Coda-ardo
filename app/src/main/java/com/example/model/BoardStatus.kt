package com.example.model

sealed class OtaUploadState {
    object Idle : OtaUploadState()
    object Connecting : OtaUploadState()
    data class Uploading(
        val progress: Float, // 0.0 to 1.0
        val bytesSent: Long,
        val totalBytes: Long,
        val speedKbps: Float,
        val stage: String
    ) : OtaUploadState()
    data class Success(val message: String, val durationMs: Long) : OtaUploadState()
    data class Error(val message: String) : OtaUploadState()
}

data class RemoteBoardStatus(
    val ipAddress: String = "",
    val isOnline: Boolean = false,
    val pingMs: Long = -1,
    val chipModel: String = "",
    val freeHeapBytes: Long = 0,
    val uptimeSeconds: Long = 0,
    val wifiRssiDbm: Int = 0,
    val firmwareVersion: String = "",
    val lastCheckTime: Long = 0
)

data class UsbDeviceInfo(
    val deviceName: String,
    val vendorId: Int,
    val productId: Int,
    val manufacturerName: String?,
    val productName: String?,
    val isSupported: Boolean
)
