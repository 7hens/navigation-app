package me.thens.navigation.core.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.getPermissionLabel(permission: String): String {
    return packageManager.getPermissionInfo(permission, 0).loadLabel(packageManager).toString()
}

@OptIn(ExperimentalPermissionsApi::class)
sealed class Permission(
    private val displayName: String,
    private val permissions: List<String>,
    private val allRequired: Boolean = false,
) {
    constructor(displayName: String, vararg permissions: String, allRequired: Boolean = false) :
            this(displayName, permissions.toList(), allRequired)

    @Composable
    fun check(content: @Composable () -> Unit = {}): MultiplePermissionsState {
        val permissionsState = rememberMultiplePermissionsState(permissions = permissions)
        if (isGranted(permissionsState)) {
            content()
        } else {
            PermissionAlert(name = displayName, permissionsState = permissionsState)
        }
        return permissionsState
    }

    @Composable
    fun rememberGranted(): State<Boolean> {
        val context = LocalContext.current
        return remember {
            derivedStateOf { isGranted(context) }
        }
    }

    private fun isGranted(state: MultiplePermissionsState): Boolean {
        if (allRequired) {
            return state.allPermissionsGranted
        }
        return state.permissions.any { it.status.isGranted }
    }

    fun isGranted(context: Context): Boolean {
        if (allRequired) {
            return permissions.all { context.isPermissionGranted(it) }
        }
        return permissions.any { context.isPermissionGranted(it) }
    }

    fun requireGranted(context: Context) {
        require(isGranted(context)) { "$displayName permission is not granted" }
    }

    object LOCATION : Permission(
        "LOCATION",
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionAlert(name: String, permissionsState: MultiplePermissionsState) {
    AlertDialog(
        title = { Text("Permission Required") },
        text = { Text("This app requires $name permission to work property, please grant it.") },
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                Text("OK")
            }
        },
    )
}