package me.corv.pillenalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import me.corv.pillenalarm.databinding.ActivityMainBinding
import me.corv.pillenalarm.model.PillenDocument
import me.corv.pillenalarm.viewmodel.PillenViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PillenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyFirebaseMessagingService.createNotificationChannel(this)

        viewModel.document.observe(this, documentObserver)
        binding.permissionButton.setOnClickListener { askNotificationPermission() }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Firebase.firestore.collection(ENVIRONMENT.collection)
                .document(ENVIRONMENT.document)
                .update("token", token)
            // Log and toast
            Log.d(TAG, token)
        })
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
    }

    private val documentObserver = Observer<PillenDocument> { document ->
        val nav = findNavController(R.id.nav_host_fragment)
        val currentFragment = nav.currentDestination?.id
        when {
            document.done && currentFragment == R.id.loadingFragment -> nav.navigate(R.id.action_loadingFragment_to_doneFragment)
            document.done && currentFragment == R.id.inputFragment -> nav.navigate(R.id.action_inputFragment_to_doneFragment)
            !document.done && currentFragment == R.id.loadingFragment -> nav.navigate(R.id.action_loadingFragment_to_inputFragment)
            !document.done && currentFragment == R.id.doneFragment -> nav.navigate(R.id.action_doneFragment_to_inputFragment)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Cannot display notifications.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                binding.notificationBadge.visibility = View.GONE
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                binding.notificationBadge.visibility = View.VISIBLE
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "PillenAlarm"
    }
}