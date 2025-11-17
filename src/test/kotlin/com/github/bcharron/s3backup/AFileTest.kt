package com.github.bcharron.s3backup

import kotlin.io.path.createTempFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

class AFileTest {
    @Test
    @DisplayName("Size with less than 30% difference are similar")
    fun isSimilarTest() {
        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 100)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 70)

        assertTrue(file1.isSimilarSize(file2))
        assertTrue(file2.isSimilarSize(file1))
    }

    @Test
    @DisplayName("Size with more than 30% difference are not similar")
    fun isNotSimilarTest() {
        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 100)

        assertFalse(file1.isSimilarSize(file2))
        assertFalse(file2.isSimilarSize(file1))
    }

    @Test
    @DisplayName("Encryption should succeed")
    fun encryptFileTest() {
        val tempFile = createTempFile(prefix = "testPlain", suffix = ".tmp").toFile()

        tempFile.outputStream().use { out ->
            val data = "Hello!".toByteArray()
            out.write(data)
        }

        val cert = File("src/test/resources/cert.asc").readText()

        val inputFile = AFile(tempFile.path, tempFile.path, tempFile.length())
        val outputFile = inputFile.encrypt(cert)

        assertTrue(outputFile.size > 0)

        // tempFile.delete()
    }
}
