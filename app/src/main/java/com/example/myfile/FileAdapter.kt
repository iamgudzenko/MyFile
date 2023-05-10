package com.example.myfile

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myfile.databinding.ActivityMainBinding
import com.example.myfile.databinding.ItemFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


interface FileActionListener {
    fun goToFile(file: File)
    fun clickShare(file: File)
}

class FileAdapter(private val actionListener: FileActionListener
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>(), View.OnClickListener{


    var files: List<File?> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFileBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        return FileViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        with(holder.binding) {
            holder.itemView.tag = file
            nameFile.text = file?.name
            val lastModified = Date(file!!.lastModified())
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
            val answer: String = formatter.format(lastModified)
            dateCreateFile.text = answer.toString()
            buttonShare.setImageResource(R.drawable.icon_share)
            sizeFile.text = "${ file.length() / (1024) }" + " kb"

            val name = file.name.toString()
            var ext = ""
            if(name.lastIndexOf('.') > 0) {
                ext = name.substring(name.lastIndexOf('.')+1)
            }

            when (ext) {
                "apk" ->
                    photoImageView.setImageResource(R.drawable.icon_apk)
                "png", "jpg", "jpeg", "JPG"
                ->
                    photoImageView.setImageResource(R.drawable.icon_image)
                "txt" ->
                    photoImageView.setImageResource(R.drawable.icon_txt)
                "fb2" ->
                    photoImageView.setImageResource(R.drawable.icon_fb)
                "mp3" ->
                    photoImageView.setImageResource(R.drawable.icon_mp)
                "" -> {
                    var count = file.listFiles().size ?: 0
                    photoImageView.setImageResource(R.drawable.icon_folder)
                    sizeFile.text = "эл-ов:" + count.toString()
                }
            }

            buttonShare.setOnClickListener {
                actionListener.clickShare(file)
            }
        }
    }

    override fun getItemCount(): Int = files.size

    override fun onClick(v: View) {
        val file = v.tag as File
        actionListener.goToFile(file)
    }
}