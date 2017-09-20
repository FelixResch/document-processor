package org.web25.felix.documents

object CommandLineProgressPrinter {

    private val configs = mutableListOf<FileWatcherConfig>()

    private var lastLines = 0

    private var spinnerIndex = 0
    private val spinnerChars = arrayOf('⣷', '⣯', '⣟', '⡿', '⢿', '⣻', '⣽', '⣾')

    private var longestName = 0

    fun print() {
        synchronized(configs) {
            val modifiedFiles = configs.filter { !it.unmodified }
            print("\u001b[${lastLines}A\r")
            longestName = modifiedFiles.map {
                val name = it.name
                if(name != null) {
                    name.length
                } else {
                    it.fileName.length
                }
            }.max() ?: 0
            modifiedFiles.forEach { config ->
                print(" ")
                if (!config.done) {
                    print(spinnerChars[spinnerIndex])
                } else {
                    if(config.unmodified) {
                        print("\u001b[33m\u2022\u001b[0m")
                    } else {
                        print("\u001b[32m\u2714\u001b[0m")  //2022 unchanged
                    }
                }
                print(" ")
                var name = config.name
                if (name == null) {
                    name = config.fileName
                }
                print(name)
                for(i in name.length..longestName) {
                    print(" ")
                }
                if(!config.done) {
                    System.out.printf("  %-10s", config.stage)
                } else {
                    for(i in 1..10) print(" ")
                }
                println()
            }
            spinnerIndex = (spinnerIndex + 1) % spinnerChars.size
            lastLines = modifiedFiles.size
        }
    }

    fun addFileWatcherConfig(config: FileWatcherConfig) {
        synchronized(configs) {
            configs.add(config)
        }
    }
}