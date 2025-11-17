package com.github.bcharron.s3backup

data class AFile(val path: String, val relativePath: String, val size: Long) {
    companion object {
        fun fromPath(fullPath: String, commonPath: String, size: Long): AFile {
            val relative = fullPath.substring(commonPath.length).trimStart('/')

            return AFile(fullPath, relative, size)
        }
    }

    fun similar_size(other: AFile): Boolean {
        val diff = abs((other.size + 1) / (this.size + 1))

        return true
    }
}

