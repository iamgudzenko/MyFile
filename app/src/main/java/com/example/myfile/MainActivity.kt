package com.example.myfile

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myfile.Presenter.GetFilePresenter
import com.example.myfile.Presenter.IGetFilePresenter
import com.example.myfile.View.IGetFileView
import com.example.myfile.databinding.ActivityMainBinding
import java.io.File

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityMainBinding
private lateinit var adapter: FileAdapter
private var sortingBy = true
lateinit var getFilePresenter: IGetFilePresenter
var path = ""
var currentFile: File? = null
var fileList: MutableList<File> = mutableListOf()
private var spinerrText = arrayOf("Дата", "Имя", "Тип", "Размер")

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity(), IGetFileView {
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getFilePresenter = GetFilePresenter(this)
        checkermission()

        val adapterSpinner: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_spinner_item, spinerrText)
        adapterSpinner.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapterSpinner


        val itemSelectedListener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val item = parent.getItemAtPosition(position) as String
                    if (item == spinerrText[0]) {
                        sortingFile(0)
                    } else if (item == spinerrText[1]) {
                        sortingFile(1)
                    } else if( item == spinerrText[2]) {
                        sortingFile(2)
                    } else if(item == spinerrText[3]) {
                        sortingFile(3)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        binding.spinner.onItemSelectedListener = itemSelectedListener

        adapter = FileAdapter(object : FileActionListener {
            override fun goToFile(file: File) {

                binding.textInfo.visibility = View.GONE
                currentFile = file
                val name = file.name.toString()
                var ext = ""
                if(name.lastIndexOf('.') > 0) {
                    ext = name.substring(name.lastIndexOf('.')+1)
                }
                if(ext == "") {
                    fileList.clear()
                    binding.recyclerViewFile.removeAllViews()
                    path = Environment.getExternalStorageDirectory().toString() + "/${file.name}"
                    getFilePresenter.getFileSLoading(file.absolutePath)
                } else {

                    openFile(file)
                }
            }

            override fun clickShare(file: File) {
                shareFile(file)
            }

        })
        getFilePresenter.getFileSLoading(Environment.getExternalStorageDirectory().path.toString())

        binding.buttonBack.setOnClickListener {
            fileList.clear()
            binding.recyclerViewFile.removeAllViews()
            binding.textInfo.visibility = View.GONE
            binding.recyclerViewFile.visibility = View.VISIBLE
            if(currentFile != null) {
                getFilePresenter.getFileSLoading(currentFile!!.parent.toString())
                currentFile = currentFile?.parentFile
            } else {
                getFilePresenter.getFileSLoading(Environment.getExternalStorageDirectory().toString())
            }

        }

        binding.buttonSorting.setOnClickListener {
            sortingBy = !sortingBy
            if(sortingBy) {
                binding.buttonSorting.text = "↓"
            } else {
                binding.buttonSorting.text = "↑"
            }

            binding.recyclerViewFile.removeAllViews()
            fileList.reverse()
            adapter.files = fileList.toList()
            val layoutManager = LinearLayoutManager(this)
            binding.recyclerViewFile.layoutManager = layoutManager
            binding.recyclerViewFile.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    fun fileType(file: File): String {
        val name = file.name.toString()
        var ext = ""
        if(name.lastIndexOf('.') > 0) {
            if(file.isFile) {
                ext = name.substring(name.lastIndexOf('.')+1)
            }
        }
        return ext
    }

    fun openFile(file:File) {
        val contentUri = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName + ".provider",
            file
        )
        val openFileIntent = Intent(Intent.ACTION_VIEW)

        val name = file.name.toString()
        var ext = ""
        if(name.lastIndexOf('.') > 0) {
            ext = name.substring(name.lastIndexOf('.')+1)
        }
        when(ext) {
            "png", "jpg", "jpeg"
            -> openFileIntent.setDataAndTypeAndNormalize(contentUri, "image/*")
            "pdf"
            -> openFileIntent.setDataAndTypeAndNormalize(contentUri, "application/pdf")
            "txt"
            -> openFileIntent.setDataAndTypeAndNormalize(contentUri, "text/plain")
            else
            -> openFileIntent.setDataAndTypeAndNormalize(contentUri, "*/*")

        }

//        openFileIntent.setDataAndTypeAndNormalize(contentUri, "image/*")
        openFileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(openFileIntent)
    }

    fun shareFile(file: File){
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        Log.w("URI", uri.toString())
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun checkermission() {

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }

            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                7)
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            7 -> {

                if (grantResults.size > 0
                    && grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {



                } else {

                }
                return
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun getFileView(file: File) {
        fileList.add(file)
        adapter.files = fileList.toList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFile.layoutManager = layoutManager
        binding.recyclerViewFile.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun getEmptyFile() {
        Log.w("empty", "sobaka")
        fileList.clear()
        binding.recyclerViewFile.removeAllViews()
        binding.recyclerViewFile.visibility = View.GONE
        binding.textInfo.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortingFile(param: Int) {
        binding.recyclerViewFile.removeAllViews()
        if(param == 0) {
            fileList.sortBy { it.lastModified() }
        } else if(param == 1) {
            fileList.sortBy { it.name }
        } else if(param == 2) {
            fileList.sortBy { fileType(it) }
        } else if(param == 3) {
            fileList.sortBy { it.length() }
        }

        adapter.files = fileList.toList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFile.layoutManager = layoutManager
        binding.recyclerViewFile.adapter = adapter
        adapter.notifyDataSetChanged()
    }

}