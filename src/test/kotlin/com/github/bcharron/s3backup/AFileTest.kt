package com.github.bcharron.s3backup

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals

class SyncEvaluatorTest {
    @Test
    @DisplayName("Size with less than X% difference are similar")
    fun isSimilarTest() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 100)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 71)

        assertTrue(evaluator.isSimilarSize(file1, file2))
        assertTrue(evaluator.isSimilarSize(file2, file1))
    }

    @Test
    @DisplayName("Size with more than x% difference are not similar")
    fun isNotSimilarTest() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 100)

        assertFalse(evaluator.isSimilarSize(file1, file2))
        assertFalse(evaluator.isSimilarSize(file2, file1))
    }

    @Test
    @DisplayName("Doesn't cause division by zero")
    fun divisionByZeroTest() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 10)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 0)

        assertDoesNotThrow {
            // 10 % 0 and 0 % 10
            assertFalse(evaluator.isSimilarSize(file1, file2))
            assertFalse(evaluator.isSimilarSize(file2, file1))

            // 0 % 0
            assertTrue(evaluator.isSimilarSize(file2, file2))
        }
    }

    @Test
    @DisplayName("Files not in bucket need to be uploaded")
    fun needsUploadNoRemote() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val entry = SyncEntry(file1, null, null)

        assertTrue(evaluator.needsUpload(entry))
    }

    @Test
    @DisplayName("Files with big size difference need to be uploaded")
    fun needsUploadFilesDifferent() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 100)
        val entry = SyncEntry(file1, file2, null)

        assertTrue(evaluator.needsUpload(entry))
    }

    @Test
    @DisplayName("Files with the same size don't need to be uploaded")
    fun noUploadNeededFilesSameSize() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val entry = SyncEntry(file1, file1, null)

        assertFalse(evaluator.needsUpload(entry))
    }

    @Test
    @DisplayName("Encrypted files with same size as remote don't need to be uploaded")
    fun encryptedSizeMatches() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 1000)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val file3 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val entry = SyncEntry(file1, file2, file3)

        assertFalse(evaluator.needsUpload(entry))
    }

    @Test
    @DisplayName("Encrypted files with different size need to be uploaded")
    fun encryptedSizeDoesntMatch() {
        val evaluator = SyncEvaluator(0.3)

        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 100)
        val file2 = AFile.fromPath("/a/b/c.png", "/a/b", 200)
        val file3 = AFile.fromPath("/a/b/c.png", "/a/b", 100)
        val entry = SyncEntry(file1, file2, file3)

        assertTrue(evaluator.needsUpload(entry))
    }

    @Test
    @DisplayName("200 should be converted to 0.2")
    fun sizeKBTest() {
        val file1 = AFile.fromPath("/a/b/c.png", "/a/b", 200)

        assertEquals(file1.sizeKB(), "0.20")
    }
}
