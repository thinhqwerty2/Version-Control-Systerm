package svcs.command

import svcs.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

abstract class CommandExecute(val args: Array<String>) {
    open fun run() {}
    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getHash(filePath: String, algorithm: String = "SHA-256"): ByteArray {
        val md: MessageDigest = MessageDigest.getInstance(algorithm)
        val fis = FileInputStream(filePath)
        val dataBytes = ByteArray(1024)
        var nread = 0
        while (fis.read(dataBytes).also { nread = it } != -1) {
            md.update(dataBytes, 0, nread)
        }
        val mdBytes: ByteArray = md.digest()
        fis.close()
        return mdBytes
    }

    fun isNotChanged(lastCommitId: String): Boolean {
        val folderLastCommit = File("$PATH_DIR_COMMITS\\${lastCommitId}")


        val filePaths = folderLastCommit.listFiles()

        val fileHashes = hashSetOf<String>()
        for (filePath in filePaths!!) {
            val fileHash: ByteArray = getHash(filePath.path)
            fileHashes.add(fileHash.contentToString())
        }

        return fileHashes == getFileHash()


    }

    private fun getFileHash(indexPath: String = PATH_FILE_INDEX): HashSet<String> {
        val fileHashes = hashSetOf<String>()
        val filePaths = File(indexPath).readLines()

        for (filePath in filePaths) {
            val fileHash: ByteArray = getHash(filePath)
            fileHashes.add(fileHash.contentToString())
        }
        return fileHashes
    }


}

class ConfigCommand(args: Array<String>) : CommandExecute(args) {
    private val fileConfig = File(PATH_FILE_CONFIG)
    override fun run() {
        if (args.size == 1)
            if (fileConfig.readText() == "")
                println("Please, tell me who you are.")
            else
                println(fileConfig.readText())
        else {
            println("The username is ${args[1]}.")
            fileConfig.writeText("The username is ${args[1]}.")
        }
    }

}

class AddCommand(args: Array<String>) : CommandExecute(args) {
    private val fileIndex = File(PATH_FILE_INDEX)
    override fun run() {
        if (args.size == 1)
            if (fileIndex.readText() == "")
                println("Add a file to the index.")
            else {
                println("Tracked files:")
                println(fileIndex.readText())
            }
        else {
            val newFile = File(args[1])
            if (newFile.exists()) {
                fileIndex.appendText("${args[1]}\n")
                println("The file '${args[1]}' is tracked.")
            } else {
                println("Can't find '${args[1]}'.")
            }
        }

    }

}

class HelpCommand(args: Array<String>) : CommandExecute(args) {
    override fun run() {
        println(
            "These are SVCS commands:\n"
                    + "config     Get and set a username.\n"
                    + "add        Add a file to the index.\n"
                    + "log        Show commit logs.\n"
                    + "commit     Save changes.\n"
                    + "checkout   Restore a file."
        )
    }
}

class LogCommand(args: Array<String>) : CommandExecute(args) {
    private val log = File(PATH_FILE_LOG)

    override fun run() {
        val logs = log.readLines()
        if (logs.isEmpty()) {
            println("No commits yet.")
            return
        }

        for (i in 0..(logs.size - 3) step 3) {
            println("commit ${logs[i]}")
            println("Author: ${logs[i + 1].substring(16, logs[i + 1].length - 1)}")
            println(logs[i + 2])
            println()
        }

    }
}

class CommitCommand(args: Array<String>) : CommandExecute(args) {
    private val index = File(PATH_FILE_INDEX)
    private val log = File(PATH_FILE_LOG)
    private val config = File(PATH_FILE_CONFIG)
    override fun run() {
        if (args.size == 1) {
            println("Message was not passed.")
            return
        }

        val lastCommit = if (log.readLines().isEmpty()) "" else log.readLines()[0]


        //Check not changed
        if (lastCommit != "") {
            if (
                isNotChanged(lastCommit)) {
                println("Nothing to commit.")
                return
            }
        }


        //Create folder commit
        val idCommit = getRandomString(30)
        val nowCommit = File("$PATH_DIR_COMMITS\\${idCommit}")
        nowCommit.mkdir()
        val filesChange = index.readLines()
        for (i in 0..filesChange.lastIndex) {
            File(filesChange[i]).copyTo(File("$nowCommit\\${filesChange[i]}"), true)
        }
        val newLog = "${idCommit}\n" +
                "${config.readText()}\n" +
                "${args[1]}\n" +
                log.readText()

        log.writeText(newLog)
        println("Changes are committed.")

    }

}

class CheckoutCommand(args: Array<String>) : CommandExecute(args) {
    private val index = File(PATH_FILE_INDEX)

    override fun run() {
        if (args.size == 1) {
            println("Commit id was not passed.")
        } else {
            val commitId = args[1]

            val commitDirPath = "$PATH_DIR_COMMITS\\${commitId}"
            if (File(commitDirPath).exists()) {

                val filesChange = index.readLines()
                for (i in 0..filesChange.lastIndex) {
                    val curFile=File("$commitDirPath\\${filesChange[i]}")
                    curFile.copyTo(File(CURRENT_DIR), true)
                }
                println("Switched to commit $commitId.")
            } else {
                println("Commit does not exist.")
            }
        }
    }
}