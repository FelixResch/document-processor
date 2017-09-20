package org.web25.felix.documents

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import org.web25.felix.jobs.ConfigNotPresentException
import org.web25.felix.jobs.ProcessableFile
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*

class FileStateTable(val parentDir: File) {

    private val fileInfo : MutableList<FileInformation>

    private val objectMapper by lazy {
        ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    }

    init {
        val file = File(parentDir, ".file-list.yml")
        if(file.exists()) {
            fileInfo = objectMapper.readValue(file.reader(), object: TypeReference<MutableList<FileInformation>>() {})
        } else {
            fileInfo = mutableListOf()
        }
    }

    fun unmodified(file: ProcessableFile): Boolean {
        val digest = md5(file.content)
        val name = createPropertyName(file)
        return fileInfo.any { it.path == name } && this[name].digest == digest
    }

    private fun createPropertyName(file: ProcessableFile): String {
        val relative = file.src.relativeTo(parentDir)
        return relative.path
    }

    fun update(file: ProcessableFile) {
        val digest = md5(file.content)
        val name = createPropertyName(file)
        val info : FileInformation
        if(!fileInfo.any { it.path == name }) {
            info = FileInformation(path = name, digest = digest)
            fileInfo.add(info)
        } else {
            info = this[name]
            info.digest = digest
        }
        try {
            val frontmatter = file.getConfig(FrontmatterData::class)
            val data = frontmatter.data
            if(data.containsKey("title")) {
                val title = data["title"]
                info.title = title?.first()
            }
            if(data.containsKey("version")) {
                val version = data["version"]
                info.version = version?.first()
            }
        } catch (e: ConfigNotPresentException) {}
        
        objectMapper.writeValue(File(parentDir, ".file-list.yml"), fileInfo)
    }

    val messageDigest : MessageDigest by singleton (threadLocal = true) {
        MessageDigest.getInstance("MD5")
    }

    fun md5(byteArray: ByteArray): String {
        messageDigest.reset()
        messageDigest.update(byteArray)
        return Base64.getEncoder().encodeToString(messageDigest.digest())
    }

    operator fun get(path: String): FileInformation {
        val value = fileInfo.find { it.path == path }
        if(value != null) {
            return value
        } else {
            throw RuntimeException("File not found in file information")
        }
    }

    operator fun get(file: ProcessableFile): FileInformation {
        return this[createPropertyName(file)]
    }

    data class FileInformation(val path: String, var digest: String, var title: String? = null, var version: String? = null)

}