package me.corv.pillenalarm.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import me.corv.pillenalarm.R
import org.checkerframework.checker.units.qual.s
import java.util.function.BiConsumer
import java.util.function.Consumer

class TagListAdapter(context: Context, tagsList: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, tagsList) {

    private lateinit var onRemoveConsumer: Consumer<Int>
    private lateinit var onChangeConsumer: BiConsumer<Int, String>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listItem =
                LayoutInflater.from(context).inflate(R.layout.taglist_item, parent, false)
        }
        val tag = getItem(position)
        listItem!!.findViewById<MaterialButton>(R.id.button_remove).setOnClickListener {
            onRemoveConsumer.accept(position)
        }
        val input = listItem.findViewById<EditText>(R.id.tag_input)
        input.setText(tag)
        input.setOnEditorActionListener { _, _, _ ->
            onChangeConsumer.accept(position, input.text.toString())
            return@setOnEditorActionListener false
        }
        return listItem
    }


    fun setOnRemoveConsumer(consumer: Consumer<Int>) {
        this.onRemoveConsumer = consumer
    }

    fun setOnChangeConsumer(consumer: BiConsumer<Int, String>) {
        this.onChangeConsumer = consumer
    }
}