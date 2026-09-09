package com.dumbphone.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dumb Phone launcher.
 *
 * This Activity is declared as a HOME launcher in the manifest, so once the
 * user sets it as their default launcher this screen replaces their normal
 * home screen / app drawer entirely. Only the eight tools below are reachable
 * from here — there is no app drawer, no search, no way to browse or open
 * anything else (including social media apps) from this screen.
 */
class MainActivity : Activity() {

    private var torchOn = false
    private var torchCameraId: String? = null
    private lateinit var cameraManager: CameraManager
    private val clockHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        torchCameraId = findTorchCameraId()

        setupTile(R.id.tilePhone, R.drawable.ic_phone, R.string.tile_phone) { openPhone() }
        setupTile(R.id.tileMessages, R.drawable.ic_message, R.string.tile_messages) { openMessages() }
        setupTile(R.id.tileEmail, R.drawable.ic_email, R.string.tile_email) { openEmail() }
        setupTile(R.id.tileGallery, R.drawable.ic_gallery, R.string.tile_gallery) { openGallery() }
        setupTile(R.id.tileCamera, R.drawable.ic_camera, R.string.tile_camera) { openCamera() }
        setupTile(R.id.tileClock, R.drawable.ic_clock, R.string.tile_clock) { openClock() }
        setupTile(R.id.tileRecorder, R.drawable.ic_mic, R.string.tile_recorder) { openRecorder() }
        setupTile(R.id.tileTorch, R.drawable.ic_torch, R.string.tile_torch) { toggleTorch() }

        // Long-press anywhere blank opens system Settings, so the phone
        // remains fully configurable (wifi, sound, changing launcher back, etc.)
        // without cluttering the home screen with a settings icon.
        findViewById<android.widget.LinearLayout>(R.id.rootContainer).setOnLongClickListener {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                toast(getString(R.string.no_app_found))
            }
            true
        }

        updateClock()
    }

    override fun onResume() {
        super.onResume()
        updateClock()
    }

    // ---------- Tile wiring ----------

    private fun setupTile(containerId: Int, iconRes: Int, labelRes: Int, onClick: () -> Unit) {
        val container = findViewById<android.view.View>(containerId)
        container.findViewById<ImageView>(R.id.tileIcon).setImageResource(iconRes)
        container.findViewById<TextView>(R.id.tileLabel).setText(labelRes)
        container.setOnClickListener { onClick() }
    }

    // ---------- Clock ----------

    private fun updateClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val now = Date()
        findViewById<TextView>(R.id.clockText).text = timeFormat.format(now)
        findViewById<TextView>(R.id.dateText).text = dateFormat.format(now)
        clockHandler.postDelayed({ updateClock() }, 30_000)
    }

    // ---------- Launch helpers ----------

    private fun openPhone() {
        launchIntentOrToast(Intent(Intent.ACTION_DIAL))
    }

    private fun openMessages() {
        val smsPackage = Settings.Secure.getString(contentResolver, "sms_default_application")
        if (!smsPackage.isNullOrEmpty()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(smsPackage)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return
            }
        }
        launchByCategoryOrToast("android.intent.category.APP_MESSAGING")
    }

    private fun openEmail() {
        launchByCategoryOrToast("android.intent.category.APP_EMAIL")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
        }
        launchIntentOrToast(intent, fallbackCategory = "android.intent.category.APP_GALLERY")
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        launchIntentOrToast(intent, fallbackCategory = "android.intent.category.APP_CAMERA")
    }

    private fun openClock() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return
        }
        // fall back to well-known clock app package names
        val candidates = listOf(
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.oneplus.deskclock"
        )
        if (launchFirstAvailablePackage(candidates)) return
        toast(getString(R.string.no_app_found))
    }

    private fun openRecorder() {
        val candidates = listOf(
            "com.google.android.apps.recorder",
            "com.android.soundrecorder",
            "com.sec.android.app.voicenote",
            "com.samsung.android.app.voicenote",
            "com.oneplus.soundrecorder",
            "com.miui.recorder",
            "com.coloros.soundrecorder"
        )
        if (launchFirstAvailablePackage(candidates)) return
        toast(getString(R.string.no_app_found))
    }

    private fun toggleTorch() {
        val id = torchCameraId
        if (id == null) {
            toast("No flashlight found on this device")
            return
        }
        try {
            torchOn = !torchOn
            cameraManager.setTorchMode(id, torchOn)
            val icon = findViewById<android.widget.LinearLayout>(R.id.tileTorch)
                .findViewById<ImageView>(R.id.tileIcon)
            icon.setColorFilter(
                if (torchOn) ContextCompat.getColor(this, R.color.torch_on) else android.graphics.Color.WHITE
            )
        } catch (e: CameraAccessException) {
            torchOn = false
            toast("Couldn't access the flashlight (camera may be in use)")
        }
    }

    private fun findTorchCameraId(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: CameraAccessException) {
            null
        }
    }

    // ---------- Generic intent helpers ----------

    private fun launchIntentOrToast(intent: Intent, fallbackCategory: String? = null) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return
        }
        if (fallbackCategory != null && launchByCategory(fallbackCategory)) return
        toast(getString(R.string.no_app_found))
    }

    private fun launchByCategoryOrToast(category: String) {
        if (!launchByCategory(category)) {
            toast(getString(R.string.no_app_found))
        }
    }

    private fun launchByCategory(category: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        val matches = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (matches.isEmpty()) return false
        val target = matches[0].activityInfo
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(category)
            setClassName(target.packageName, target.name)
        }
        startActivity(launchIntent)
        return true
    }

    private fun launchFirstAvailablePackage(packages: List<String>): Boolean {
        for (pkg in packages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return true
            }
        }
        return false
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Block the back button from ever leaving this screen — as a home
    // launcher there is nowhere "back" to go, and this keeps the user from
    // accidentally exposing any behavior outside the curated tile set.
    override fun onBackPressed() {
        // intentionally does nothing
    }
}
