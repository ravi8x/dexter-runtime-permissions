package info.androidhive.runtime_permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import info.androidhive.runtime_permissions.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var requestOpenCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private var requestOpenSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCameraPermission.setOnClickListener {
            requestCameraPermission()
        }

        binding.btnMultiplePermissions.setOnClickListener {
            requestMultiplePermissions()
        }
    }

    /**
     * Requesting multiple permissions (record audio and location) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private fun requestMultiplePermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(
                            applicationContext,
                            "All permissions are granted!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    /**
     * Requesting camera permission
     * This uses single permission model from dexter
     * Once the permission granted, opens the camera
     * On permanent denial opens settings dialog
     */
    private fun requestCameraPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    // Congrats! permission is granted. You can open the camera row
                    openCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    // check for permanent denial of permission
                    if (response.isPermanentlyDenied) {
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener {error ->
                Toast.makeText(applicationContext, "Error occurred! $error", Toast.LENGTH_SHORT).show();
            }
            .check()
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.permissions_title))
            .setMessage(resources.getString(R.string.permissions_message))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton(resources.getString(R.string.goto_settings)) { dialog, which ->
                // Respond to positive button press
                openSettings()
            }
            .show()
    }

    // navigating user to app settings
    private fun openSettings() {
        requestOpenSettings.launch(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
    }

    private fun openCamera() {
        requestOpenCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }
}