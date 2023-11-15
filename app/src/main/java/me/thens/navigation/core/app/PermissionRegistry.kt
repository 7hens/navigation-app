package me.thens.navigation.core.app

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionRegistry(
    private val permission: Permission,
    private val context: ComponentActivity,
    private val onResult: (Boolean) -> Unit,
) {
    private val permissionLauncher = context
        .registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onResult(permission.isGranted(context))
        }

    fun check() {
        if (permission.isGranted(context)) {
            onResult(true)
            return
        }
        val deniedPermissions = permission.permissions
            .filter { !permission.isGranted(context) }
            .sortedBy { !shouldShowRationale(it) }
        val shouldShowRationale = deniedPermissions
            .any { shouldShowRationale(it) }
        if (shouldShowRationale) {
            showRationale(deniedPermissions)
            return
        }
        requestPermissions(deniedPermissions)
    }

    private fun requestPermissions(permissions: List<String>) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun showRationale(permissions: List<String>) {
        val name = permission.displayName
        MaterialAlertDialogBuilder(context)
            .setTitle("Permission Required")
            .setMessage("This app requires $name permission to work property, please grant it.")
            .setPositiveButton("OK") { _, _ -> requestPermissions(permissions) }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    fun isGranted(): Boolean = permission.isGranted(context)

    fun requireGranted() = permission.requireGranted(context)

    private fun shouldShowRationale(permission: String): Boolean {
        return context.shouldShowPermissionRationale(permission)
    }
}