package base.boudicca.publisher.event.html.util.SnapshotHandler

import java.io.File

class SnapshotHandler(private val filename: String, private val path: String) {
  var content: String? = null

  init {
    val directory = File(path)
    if (!directory.exists()) {
      directory.mkdirs()
    }
  }

  fun save(content: String) {
    val filePath = "$path/$filename"
    println("Saving snapshot to $filePath:")
    println(content)
    File(filePath).writeText(content)
    this.content = content
  }

  fun read(): String {
    val filePath = "$path/$filename"
    println("Reading snapshot from $filePath:")
    val fileContent = File(filePath).readText()
    this.content = fileContent
    return fileContent
  }

  fun exists(): Boolean {
    val filePath = "$path/$filename"
    return File(filePath).exists()
  }
}
