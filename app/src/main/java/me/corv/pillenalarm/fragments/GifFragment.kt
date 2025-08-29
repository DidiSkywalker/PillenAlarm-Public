package me.corv.pillenalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import me.corv.pillenalarm.R
import me.corv.pillenalarm.databinding.FragmentGifBinding

class GifFragment : Fragment() {

    companion object {
        private const val GIF_URL = "gif"
        fun passUrl(url: String): Bundle {
            val extras = Bundle()
            extras.putString(GIF_URL, url)
            return extras
        }
    }

    private var _binding: FragmentGifBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGifBinding.inflate(inflater, container, false)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_graph))
        binding.toolbar.setupWithNavController(findNavController(), appBarConfiguration)
        Glide.with(this).load(arguments?.getString(GIF_URL)).into(binding.imageView)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}