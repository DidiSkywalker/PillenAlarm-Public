package me.corv.pillenalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import me.corv.pillenalarm.R
import me.corv.pillenalarm.adapter.TagListAdapter
import me.corv.pillenalarm.databinding.FragmentSettingsBinding
import me.corv.pillenalarm.viewmodel.PillenViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PillenViewModel by activityViewModels()
    private val tagsList: ArrayList<String> = ArrayList()
    private lateinit var tagListAdapter: TagListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagListAdapter = TagListAdapter(requireContext(), tagsList)
        tagListAdapter.setOnRemoveConsumer { position ->
            val tag = tagsList[position]
            tagsList.removeAt(position)
            tagListAdapter.notifyDataSetChanged()
            Snackbar.make(binding.root, R.string.gif_tag_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo) {
                    tagsList.add(tag)
                    tagListAdapter.notifyDataSetChanged()
                }.show()
        }
        tagListAdapter.setOnChangeConsumer { position, tag ->
            tagsList[position] = tag
            tagListAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_graph))
        binding.toolbar.setupWithNavController(findNavController(), appBarConfiguration)
        binding.tagList.adapter = tagListAdapter

        viewModel.document.observe(viewLifecycleOwner) { document ->
            tagsList.clear()
            tagsList.addAll(document.gifTags)
            tagListAdapter.notifyDataSetChanged()

            binding.buttonSave.setOnClickListener {
                document.gifTags = tagsList
                document.gifProvider = binding.providerSpinner.selectedItem.toString()
                viewModel.updateDocument()
                Toast.makeText(context, R.string.settings_saved, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.buttonAdd.setOnClickListener {
            tagsList.add("")
            tagListAdapter.notifyDataSetChanged()
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gif_providers,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.providerSpinner.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}