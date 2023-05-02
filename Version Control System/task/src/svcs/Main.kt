package svcs

import svcs.command.*
import java.io.File
import java.nio.file.Paths


const val PATH_DIR = "vcs"
const val PATH_FILE_CONFIG = "vcs\\config.txt"
const val PATH_FILE_INDEX = "vcs\\index.txt"
const val PATH_FILE_LOG = "vcs\\log.txt"
const val PATH_DIR_COMMITS = "vcs\\commits"
val CURRENT_DIR = Paths.get("").toAbsolutePath().toString()
fun main(args: Array<String>) {

    init()

    do {

        when (if (args.isNotEmpty()) args.first() else "") {
            "--help", "" -> HelpCommand(args).run()
            "config" -> ConfigCommand(args).run()
            "add" -> AddCommand(args).run()
            "log" -> LogCommand(args).run()
            "commit" -> CommitCommand(args).run()
            "checkout" -> CheckoutCommand(args).run()

            else -> print("'${args.first()}' is not a SVCS command.")
        }
    } while (false)
}

fun init() {

    File(PATH_DIR).mkdir()
    File(PATH_FILE_CONFIG).createNewFile()
    File(PATH_FILE_INDEX).createNewFile()
    File(PATH_FILE_LOG).createNewFile()
    File(PATH_DIR_COMMITS).mkdir()

}
