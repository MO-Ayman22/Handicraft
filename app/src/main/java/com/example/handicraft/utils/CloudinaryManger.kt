package com.example.handicraft_graduation_project_2025.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Properties

object CloudinaryManager {
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L
    private lateinit var cloudName: String
    private lateinit var uploadPreset: String
    private val client = OkHttpClient()
    private val firestore = FirebaseFirestore.getInstance()

    fun initialize(context: Context) {
        val properties = Properties()
        context.assets.open("cloudinary.properties").use { properties.load(it) }
        cloudName = properties.getProperty("cloudinary.cloud_name") ?: throw IllegalStateException("Cloudinary cloud_name not found")
        uploadPreset = properties.getProperty("cloudinary.upload_preset") ?: throw IllegalStateException("Cloudinary upload_preset not found")
    }

    suspend fun uploadImage(
        context: Context,
        imageFile: File,
        collectionPath: String,
        documentId: String,
        imageField: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var attempt = 0
        while (attempt < MAX_RETRIES) {
            try {
                val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", imageFile.name, imageFile.asRequestBody("image/*".toMediaType()))
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()

                response.use {
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IOException("Upload failed: ${response.code}"))
                    }
                    val json = JSONObject(response.body!!.string())
                    val imageUrl = json.getString("secure_url")

                    // Save to Firestore
                    val data = hashMapOf(
                        imageField to imageUrl,
                        "uploadedAt" to FieldValue.serverTimestamp()
                    )
                    firestore.collection(collectionPath).document(documentId)
                        .set(data, SetOptions.merge()).await()

                    return@withContext Result.success(imageUrl)
                }
            } catch (e: Exception) {
                attempt++
                if (attempt == MAX_RETRIES) {
                    return@withContext Result.failure(e)
                }
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
            }
        }
        Result.failure(IllegalStateException("Upload failed after $MAX_RETRIES attempts"))
    }

    suspend fun getImageUrl(collectionPath: String, documentId: String, imageField: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(collectionPath).document(documentId).get().await()
            val imageUrl = document.getString(imageField)
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun uriToFile(uri: Uri,context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }
}