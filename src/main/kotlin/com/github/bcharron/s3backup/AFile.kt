package com.github.bcharron.s3backup

import org.bouncycastle.openpgp.api.OpenPGPCertificate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import java.io.File

data class AFile(
    val path: String,
    val relativePath: String,
    val size: Long,
) {
    companion object {

        fun fromPath(
            fullPath: String,
            commonPath: String,
            size: Long,
        ): AFile {
            val relative = fullPath.substring(commonPath.length).trimStart('/')

            return AFile(fullPath, relative, size)
        }
    }

    fun sizeKB() = String.format("%.2f", (size / 1024f))

    fun delete() = File(path).delete()
}
