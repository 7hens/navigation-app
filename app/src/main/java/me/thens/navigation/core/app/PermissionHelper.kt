package me.thens.navigation.core.app

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionHelper(
    private val activity: ComponentActivity,
) {
    private val permissionMap = mutableMapOf<String, (Boolean) -> Unit>()

    fun permission(permission: String, onResult: (Boolean) -> Unit = {}): PermissionHelper {
        permissionMap[permission] = onResult
        return this
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.entries.forEach { (permission, isGranted) ->
            permissionMap[permission]?.invoke(isGranted)
        }
    }

    fun check() {
        val deniedPermissions = mutableListOf<String>()
        permissionMap.forEach { (permission, onResult) ->
            if (activity.isPermissionGranted(permission)) {
                onResult(true)
            } else {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.isEmpty()) {
            return
        }
        val shouldShowRationale = deniedPermissions.any { shouldShowRationale(it) }
        if (shouldShowRationale) {
            showRationale(deniedPermissions)
            return
        }
        requestPermissions(deniedPermissions)
    }

    private fun requestPermissions(permissions: List<String>) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun shouldShowRationale(it: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)


    private fun showRationale(permissions: List<String>) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Required Permission")
            .setMessage(permissions.joinToString("\n\n") { getPermissionDescription(it) })
            .setPositiveButton("OK") { _, _ -> requestPermissions(permissions) }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun getPermissionDescription(permission: String): String {
        return permission.substringAfterLast(".") + ": " + activity.getPermissionLabel(permission)
    }
}