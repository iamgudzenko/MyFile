package com.example.myfile.Presenter

import android.util.Log
import com.example.myfile.View.IGetFileView
import java.io.File

class GetFilePresenter(val fileView: IGetFileView) : IGetFilePresenter {


    override fun getFileSLoading(path: String) {
        Log.w("Files", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        if(files != null) {
            for (i in files.indices) {
                fileView.getFileView(files[i])
            }
        }
        if(files.size == 0){
            fileView.getEmptyFile()
        }
    }

}