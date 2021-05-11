package pl.edu.agh.im.voicehealthanalyzer

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private var audioFilePath: String? = null
    private lateinit var stopButton: Button
    private lateinit var playButton: Button
    private lateinit var recordButton: Button

    private var isRecording = false

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)

        initMediaRecorder()
        initMediaPlayer()
        initButtons()

        if (!hasNecessaryPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    private fun initMediaRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
    }

    private fun initButtons() {
        if (!hasMicrophone()) {
            configureButtons(recordEnabled = false, stopEnabled = false, playEnabled = false)
        } else {
            configureButtons(recordEnabled = true, stopEnabled = false, playEnabled = false)
        }
    }

    private fun hasNecessaryPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE
        )
    }

    private fun generateRandomString(length: Long): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun configureButtons(
        recordEnabled: Boolean,
        stopEnabled: Boolean,
        playEnabled: Boolean
    ) {
        recordButton.isEnabled = recordEnabled
        stopButton.isEnabled = stopEnabled
        playButton.isEnabled = playEnabled
    }

    private fun generateRandomFileName(): String {
        return Environment.getExternalStorageDirectory().toString() +
                File.separator.toString() + generateRandomString(5) + "AudioRecording.3gp"
    }

    @Throws(IOException::class)
    fun recordAudio(view: View?) {
        audioFilePath = generateRandomFileName()
        isRecording = true
        configureButtons(recordEnabled = false, stopEnabled = true, playEnabled = false)
        try {
            mediaRecorder?.setOutputFile(audioFilePath)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
        Log.i("MainActivity", "Started recording, audio file path: $audioFilePath")
        Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
    }

    fun stopAudio(view: View?) {
        if (isRecording) {
            configureButtons(recordEnabled = false, stopEnabled = false, playEnabled = true)
            mediaRecorder?.stop()
            mediaRecorder?.release()
            isRecording = false
            Log.i("MainActivity", "Stopped recording, audio file path: $audioFilePath")
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
        } else {
            mediaPlayer?.release()
            configureButtons(recordEnabled = true, stopEnabled = false, playEnabled = true)
            Log.i("MainActivity", "Stopped playing audio, audio file path: $audioFilePath")
            Toast.makeText(this, "Stopped playing your audio!", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    fun playAudio(view: View?) {
        configureButtons(recordEnabled = false, stopEnabled = true, playEnabled = false)
        mediaPlayer?.setDataSource(audioFilePath)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        Log.i("MainActivity", "Started playing audio, audio file path: $audioFilePath")
        Toast.makeText(this, "Started playing your audio!", Toast.LENGTH_SHORT).show()
    }

}