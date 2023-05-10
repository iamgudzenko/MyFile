package com.example.myfile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
lateinit var getFilePresenter: IGetFilePresenter
var path = ""
var currentFile: File? = null
var fileList: MutableList<File> = mutableListOf()

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity(), IGetFileView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getFilePresenter = GetFilePresenter(this)
        checkermission()
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
                    getFilePresenter.getFileSLoading(file.path)
                } else {
                    val URI: Uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    Log.w("URI", URI.toString())
                    val intent =
                        Intent(Intent.ACTION_VIEW, URI)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
//                fileList.clear()
//                binding.recyclerViewFile.removeAllViews()
//                path = Environment.getExternalStorageDirectory().toString() + "/${file.name}"
//                getFilePresenter.getFileSLoading(file.path)
            }
        })
        getFilePresenter.getFileSLoading(Environment.getExternalStorageDirectory().toString())

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
    }
    fun shareFile(file: File){
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setType("*/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        Log.w("GETFILES", file.name)
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

}