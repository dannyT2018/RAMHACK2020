package com.example.ramhack2020


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val FILE_NAME = "photo.jpg"
private const val CAMERA_REQUEST_CODE = 42
private lateinit var photoFile: File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(this,"com.example.ramhack2020.fileprovider", photoFile)
            callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (callCameraIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(callCameraIntent,CAMERA_REQUEST_CODE)
            }else{
                Toast.makeText(this,"Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPhotoFile(fileName: String): File{
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            /*val takenImage = data?.extras?.get("data") as Bitmap
            cameraImageView.setImageBitmap(takenImage)*/
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            cameraImageView.setImageBitmap(takenImage)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}
