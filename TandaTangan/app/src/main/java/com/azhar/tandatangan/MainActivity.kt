package com.azhar.tandatangan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set permission
        verifyStoragePermissions(this);

        //draw signature
        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                //Toast.makeText(this@MainActivity, "Mulai menulis...", Toast.LENGTH_SHORT).show()
            }

            override fun onSigned() {
                btnSave.setEnabled(true)
                btnClear.setEnabled(true)
            }

            override fun onClear() {
                btnSave.setEnabled(false)
                btnClear.setEnabled(false)
            }
        })

        //clear signature
        btnClear.setOnClickListener {
            signaturePad.clear()
        }

        //save signature
        btnSave.setOnClickListener {
            val signatureBitmap = signaturePad.getSignatureBitmap()
            if (addJpgSignatureToGallery(signatureBitmap)) {
                Toast.makeText(this@MainActivity, "Tanda tangan disimpan ke dalam Galeri",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Tidak dapat menyimpan Tanda Tangan",
                        Toast.LENGTH_SHORT).show()
            }
            if (addSvgSignatureToGallery(signaturePad.getSignatureSvg())) {
                Toast.makeText(this@MainActivity, "Tanda tangan SVG disimpan ke dalam Galeri",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Tidak dapat menyimpan Tanda Tangan SVG",
                        Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.size <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Tidak dapat menulis gambar ke Media Penyimpanan",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getAlbumStorageDir(albumName: String?): File {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created")
        }
        return file
    }

    //Image Signature
    @Throws(IOException::class)
    fun saveBitmapToJPG(bitmap: Bitmap, photo: File?) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.close()
    }

    fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg",
                    System.currentTimeMillis()))
            saveBitmapToJPG(signature, photo)
            scanMediaFile(photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        this@MainActivity.sendBroadcast(mediaScanIntent)
    }

    //SVG Signature
    fun addSvgSignatureToGallery(signatureSvg: String?): Boolean {
        var result = false
        try {
            val svgFile = File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg",
                    System.currentTimeMillis()))
            val stream: OutputStream = FileOutputStream(svgFile)
            val writer = OutputStreamWriter(stream)
            writer.write(signatureSvg)
            writer.close()
            stream.flush()
            stream.close()
            scanMediaFile(svgFile)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        fun verifyStoragePermissions(activity: MainActivity?) {
            val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }
    }

}
