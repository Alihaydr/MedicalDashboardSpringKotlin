package com.libanux.medicaldashboard_backend.firebase

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

@Service
class FirebaseService {

    private val logger: Logger = LoggerFactory.getLogger(FirebaseService::class.java)

    @Throws(IOException::class)
    private fun uploadFile(file: File, folderName: String, fileName: String): String {
        val blobId = BlobId.of("medical-dashaboard.appspot.com", fileName)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("application/pdf")
            .setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .build()
        val inputStream: InputStream = FirebaseService::class.java.getClassLoader()
            .getResourceAsStream("medical-dashaboard-firebase-adminsdk-m4bep-d5f3e30b5f.json")!!
        val credentials: Credentials = GoogleCredentials.fromStream(inputStream)
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
        storage.create(blobInfo, Files.readAllBytes(file.toPath()))
        val DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/medical-dashaboard.appspot.com/o/%s?alt=media"
        return String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8))
    }

    @Throws(IOException::class)
    private fun convertToFile(multipartFile: MultipartFile, fileName: String): File {
        val tempFile = File(fileName)
        FileOutputStream(tempFile).use { fos ->
            fos.write(multipartFile.bytes)
            fos.close()
        }
        return tempFile
    }

    private fun getExtension(fileName: String?): String {
        return fileName!!.substring(fileName!!.lastIndexOf("."))
    }

    fun upload(multipartFile: MultipartFile, folder: String): String {
        return try {
            var fileName = multipartFile.originalFilename
            fileName = UUID.randomUUID().toString() + getExtension(fileName)
            val file = convertToFile(multipartFile, fileName)
            val URL = uploadFile(file, folder, fileName)
            file.delete()
            logger.info("Pdf Files Uploaded Successfully")
            URL
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Cv couldn't upload, Something went wrong")
        }
    }
}