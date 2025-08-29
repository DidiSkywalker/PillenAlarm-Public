package me.corv.pillenalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.functions.FirebaseFunctionsException
import me.corv.pillenalarm.R
import me.corv.pillenalarm.databinding.FragmentTimerBinding
import me.corv.pillenalarm.viewmodel.TimerViewModel

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TimerViewModel by viewModels()

    private var baseTextColor: Int = 0
    private var highlightTextColor: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        baseTextColor = resources.getColor(R.color.md_theme_onSurface, null)
        highlightTextColor = resources.getColor(R.color.colorDarkBrown, null)

        viewModel.hoursText.observe(viewLifecycleOwner) { hours ->
            if (hours == null) {
                binding.hours.setTextColor(baseTextColor)
                binding.hLabel.setTextColor(baseTextColor)
                binding.hours.setText(R.string.timer_00)
            } else {
                binding.hours.setTextColor(highlightTextColor)
                binding.hLabel.setTextColor(highlightTextColor)
                binding.hours.text = hours.padStart(2, '0')
            }
        }
        viewModel.minutesText.observe(viewLifecycleOwner) { minutes ->
            if (minutes == null) {
                binding.minutes.setTextColor(baseTextColor)
                binding.mLabel.setTextColor(baseTextColor)
                binding.minutes.setText(R.string.timer_00)
            } else {
                binding.minutes.setTextColor(highlightTextColor)
                binding.mLabel.setTextColor(highlightTextColor)
                binding.minutes.text = minutes.padStart(2, '0')
            }
        }

        viewModel.commitVisible.observe(viewLifecycleOwner) { visible ->
            binding.commitLayout.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }

        binding.buttonCancel.setOnClickListener { findNavController().navigate(R.id.action_timerFragment_to_inputFragment) }
        binding.buttonBackspace.setOnClickListener { viewModel.backspace() }
        binding.buttonCommit.setOnClickListener {
            startLoading()
            viewModel.commit { error ->
                stopLoading()
                when (error) {
                    null -> {
                        findNavController().navigate(R.id.action_timerFragment_to_inputFragment)
                    }

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
        }

        val buttonListener: (View) -> Unit = { viewModel.input((it as Button).text.toString()) }
        binding.button1.setOnClickListener(buttonListener)
        binding.button2.setOnClickListener(buttonListener)
        binding.button3.setOnClickListener(buttonListener)
        binding.button4.setOnClickListener(buttonListener)
        binding.button5.setOnClickListener(buttonListener)
        binding.button6.setOnClickListener(buttonListener)
        binding.button7.setOnClickListener(buttonListener)
        binding.button8.setOnClickListener(buttonListener)
        binding.button9.setOnClickListener(buttonListener)
        binding.button0.setOnClickListener(buttonListener)
        binding.button00.setOnClickListener(buttonListener)

        return binding.root
    }

    private fun startLoading() {
        binding.buttonCommit.isEnabled = false
        binding.buttonCommit.setImageResource(ResourcesCompat.ID_NULL)
        binding.fabProgress.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        binding.buttonCommit.isEnabled = true
        binding.buttonCommit.setImageResource(R.drawable.check)
        binding.fabProgress.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}