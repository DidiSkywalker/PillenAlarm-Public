package me.corv.pillenalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.functions.FirebaseFunctionsException
import me.corv.pillenalarm.R
import me.corv.pillenalarm.actions.DelayAction
import me.corv.pillenalarm.actions.DoneAction
import me.corv.pillenalarm.actions.PeriodAction
import me.corv.pillenalarm.databinding.FragmentInputBinding
import me.corv.pillenalarm.formatDurationAsString
import me.corv.pillenalarm.in1h
import me.corv.pillenalarm.in30min
import me.corv.pillenalarm.viewmodel.PillenViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class InputFragment : Fragment() {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PillenViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.document.observe(viewLifecycleOwner) { document ->
            val reminderInPast = System.currentTimeMillis() > document.scheduledReminder
            val inputVisibility = if (reminderInPast) View.VISIBLE else View.GONE
            binding.reminderLayout.visibility = inputVisibility
            binding.periodLayout.visibility = inputVisibility
            binding.divider.visibility = inputVisibility
            binding.nextReminderText.visibility = if (reminderInPast) View.GONE else View.VISIBLE

            val scheduledReminder = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(document.scheduledReminder),
                ZoneId.systemDefault()
            )
            val nextReminderIn = formatDurationAsString(LocalDateTime.now(), scheduledReminder)
            binding.nextReminderText.text = getString(R.string.next_reminder_text, nextReminderIn)
        }

        binding.buttonIn30min.setOnClickListener {
            startLoading()
            DelayAction().execute(in30min()) { handleDelayCallback(it) }
        }
        binding.buttonIn1h.setOnClickListener {
            startLoading()
            DelayAction().execute(in1h()) { handleDelayCallback(it) }
        }
        binding.buttonCustom.setOnClickListener { findNavController().navigate(R.id.action_inputFragment_to_timerFragment) }

        binding.buttonOk.setOnClickListener {DoneAction(view.context).execute(viewModel.document.value!!) }

        binding.buttonPeriod.setOnClickListener { PeriodAction(view.context).execute(viewModel.document.value!!) }
    }

    private fun handleDelayCallback(error: Exception?) {
        stopLoading()
        when (error) {
            null -> {}

            is FirebaseFunctionsException -> {
                val code = error.code
                val details = error.details
                Toast.makeText(
                    context,
                    "Verzögerte Erinnerung konnte nicht erstellt werden [$code]: $details",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    context,
                    "Verzögerte Erinnerung konnte nicht erstellt werden. (${error.message})",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startLoading() {
        binding.delayLoading.visibility = View.VISIBLE
        binding.buttonRow.visibility = View.GONE
    }

    private fun stopLoading() {
        binding.delayLoading.visibility = View.GONE
        binding.buttonRow.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "InputFragment"
    }
}