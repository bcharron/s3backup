package com.github.bcharron.s3backup

import aws.sdk.kotlin.services.s3.S3Client
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.bouncycastle.openpgp.api.OpenPGPCertificate
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BackupTest {
    // @Test
    // @DisplayName("Should find missing files")
    // fun findMissingFiles() {
    //     val list1 =
    //         listOf(
    //             AFile("/moo/file1.txt", "file1.txt", 100),
    //             AFile("/moo/file2.txt", "file2.txt", 100),
    //         )
    //
    //     val list2 = listOf<AFile>()
    //
    //     val differences = Backup.findDifferences(list1, list2)
    //
    //     assertEquals(differences.size, 2)
    //     val filenames = differences.map { it.relativePath }.toSet()
    //
    //     assertEquals(filenames, setOf("file1.txt", "file2.txt"))
    // }
    //
    // @Test
    // @DisplayName("Should find files with different sizes")
    // fun findDifferentSizes() {
    //     val list1 =
    //         listOf(
    //             AFile("/moo/file1.txt", "file1.txt", 100),
    //             AFile("/moo/file2.txt", "file2.txt", 100),
    //         )
    //
    //     val list2 =
    //         listOf(
    //             AFile("/moo/file1.txt", "file1.txt", 200),
    //             AFile("/moo/file2.txt", "file2.txt", 100),
    //         )
    //
    //     val differences = Backup.findDifferences(list1, list2)
    //
    //     assertEquals(differences.size, 1)
    //     val filenames = differences.map { it.relativePath }.toSet()
    //
    //     assertEquals(filenames, setOf("file1.txt"))
    // }

    @Test
    @DisplayName("Should find local files")
    fun findLocalFiles() =
        runTest {
            val fakeS3Client = mockk<S3Storage>()
            val fakeCrypto = mockk<Crypto>()

            val backup = Backup(fakeS3Client, "fakeBucket", fakeCrypto)

            val localFiles = backup.listLocalFiles("src/test")

            val certFile = localFiles.filter { it.relativePath == "resources/cert.asc" }.firstOrNull()

            assertNotNull(certFile)
            assertEquals(certFile.path, "src/test/resources/cert.asc")
            assertEquals(certFile.relativePath, "resources/cert.asc")
            assertTrue(certFile.size > 0)
        }

    @Test
    @DisplayName("Integration test for sync")
    fun basicSync() =
        runTest {
            val fakeS3Client = mockk<S3Storage>()
            val fakeCrypto = mockk<Crypto>()

            val mockBucketList = listOf(AFile("fake/path/resource/cert.asc", "resources/cert.asc", 500))

            coEvery { fakeS3Client.listBucketFiles(any(), any()) } returns mockBucketList
            coEvery { fakeS3Client.upload(any(), any(), any()) } returns Unit
            coEvery { fakeCrypto.encrypt(any()) } returns AFile("/tmp/tempfile", "resources/fakefile", 100)

            val backup = Backup(fakeS3Client, "fakeBucket", fakeCrypto)

            assertDoesNotThrow {
                backup.sync("src/test", "fake/path")
            }

            coVerify { fakeS3Client.listBucketFiles("fakeBucket", "fake/path") }
            coVerify { fakeS3Client.upload(any(), "fake/path/resources/cert.asc", "fakeBucket") }
        }
}
