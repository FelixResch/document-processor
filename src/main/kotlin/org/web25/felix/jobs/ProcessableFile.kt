package org.web25.felix.jobs

import java.io.File
import kotlin.reflect.KClass

class ProcessableFile(val src: File) {

    var dst: File? = null

    var content: ByteArray = if(src.exists() && src.isFile) src.readBytes() else byteArrayOf()

    var stringContent: String
    get() {
        return String(content)
    }
    set(value) {
        content = value.toByteArray()
    }

    var config: MutableMap<KClass<*>, Config> = mutableMapOf()

    fun <T: Config> getConfig(kClass: KClass<T>): T {
        val config = this.config[kClass]
        if(config != null && kClass.isInstance(config)) {
            return config as T
        } else {
            throw ConfigNotPresentException(kClass)
        }
    }

    fun addConfig(config: Config) {
        this.config[config::class] = config
    }

    fun write(it: File) {
        it.writeBytes(content)
    }

}

