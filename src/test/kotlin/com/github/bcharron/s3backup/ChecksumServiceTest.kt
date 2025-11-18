package com.github.bcharron.s3backup

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.test.assertEquals

class ChecksumServiceTest {
    @Test
    fun `validate md5`() {
        val tempFile = createTempFile(prefix = "testPlain", suffix = ".tmp")
        val checksumService = ChecksumService()

        try {
            val tmp = tempFile.toFile()
            tmp.writeText("Hello")

            val checksum = checksumService.md5(tmp.path)

            assertEquals(checksum, "8b1a9953c4611296a827abf8c47804d7")
        } finally {
            tempFile.deleteIfExists()
        }
    }
}
