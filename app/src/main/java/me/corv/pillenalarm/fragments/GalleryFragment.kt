package me.corv.pillenalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import me.corv.pillenalarm.R
import me.corv.pillenalarm.adapter.GalleryGridAdapter
import me.corv.pillenalarm.databinding.FragmentGalleryBinding
import me.corv.pillenalarm.viewmodel.PillenViewModel

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PillenViewModel by activityViewModels()
    private val gifsList: ArrayList<String> = ArrayList()
    private lateinit var galleryGridAdapter: GalleryGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryGridAdapter = GalleryGridAdapter(requireContext(), gifsList)
        galleryGridAdapter.setOnUnlikeConsumer { gif ->
            viewModel.toggleLike(gif)
            Snackbar.make(binding.root, R.string.snackbar_unlike, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo) {
                    viewModel.toggleLike(gif)
                }.show()
        }
        galleryGridAdapter.setOnClickGifConsumer {

            findNavController().navigate(R.id.action_galleryFragment_to_gifFragment, GifFragment.passUrl(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_graph))
        binding.toolbar.setupWithNavController(findNavController(), appBarConfiguration)
        binding.galleryGrid.adapter = galleryGridAdapter
        viewModel.document.observe(viewLifecycleOwner) { document ->
            gifsList.clear()
            gifsList.addAll(document.likedGifs)
            galleryGridAdapter.notifyDataSetChanged()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}