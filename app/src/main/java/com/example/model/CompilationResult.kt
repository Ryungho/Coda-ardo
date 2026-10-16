package com.example.model

data class CompilationIssue(
    val line: Int,
    val column: Int,
    val message: String,
    val isWarning: Boolean = false
)

data class CompilationResult(
    val success: Boolean,
    val targetBoard: Board,
    val issues: List<CompilationIssue>,
    val flashUsedBytes: Long,
    val flashTotalBytes: Long,
    val ramUsedBytes: Long,
    val ramTotalBytes: Long,
    val hexOutput: String,
    val binaryBytes: ByteArray,
    val buildLogs: List<String>
) {
    val flashPercentage: Float
        get() = if (flashTotalBytes > 0) (flashUsedBytes.toFloat() / flashTotalBytes.toFloat()) * 100f else 0f

    val ramPercentage: Float
        get() = if (ramTotalBytes > 0) (ramUsedBytes.toFloat() / ramTotalBytes.toFloat()) * 100f else 0f

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompilationResult
        return success == other.success &&
                targetBoard == other.targetBoard &&
                issues == other.issues &&
                flashUsedBytes == other.flashUsedBytes &&
                hexOutput == other.hexOutput
    }

    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + targetBoard.hashCode()
        result = 31 * result + issues.hashCode()
        result = 31 * result + flashUsedBytes.hashCode()
        return result
    }
}
