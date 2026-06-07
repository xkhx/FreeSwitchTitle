package top.zoyn.freeswitchtitle.util

import taboolib.common.platform.function.getDataFolder
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object TitleImageFontManager {

    private val fileNameRegex = Regex("^[A-Za-z0-9_.-]+\\.png$", RegexOption.IGNORE_CASE)
    private val imageGlyphs = linkedMapOf<String, String>()
    private val imageFiles = linkedMapOf<String, File>()
    private var lastSha1 = ""

    fun reload() {
        imageGlyphs.clear()
        imageFiles.clear()
        lastSha1 = ""
        if (!ConfigUtils.displayResourcePackEnable) return
        val imageFolder = imageFolder()
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
            FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 已生成图片称号目录: ${imageFolder.absolutePath}")
        }
        val images = imageFolder.listFiles { file -> file.isFile && file.extension.equals("png", ignoreCase = true) }
            ?.sortedBy { it.name.lowercase(Locale.getDefault()) }
            .orEmpty()
        val validImages = images.filter { isValidImageName(it.name) }
        images.filterNot { isValidImageName(it.name) }.forEach {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 跳过非法图片文件名: ${it.name}，仅允许字母、数字、_、-、. 且必须为 png")
        }
        validImages.forEachIndexed { index, file ->
            val key = normalizeImageName(file.name)
            val codepoint = ConfigUtils.displayResourcePackStartCodepoint + index
            imageGlyphs[key] = String(Character.toChars(codepoint))
            imageFiles[key] = file
        }
        generateResourcePack(validImages)
    }

    fun getGlyph(image: String): String? {
        return imageGlyphs[normalizeImageName(image)]
    }

    fun hasImage(image: String): Boolean {
        return imageFiles.containsKey(normalizeImageName(image))
    }

    fun isValidImageName(image: String): Boolean {
        return fileNameRegex.matches(image.trim())
    }

    fun getImageFolder(): File = imageFolder()

    fun getLastSha1(): String = lastSha1

    private fun imageFolder(): File {
        return File(getDataFolder(), ConfigUtils.displayImageFolder)
    }

    private fun outputFolder(): File {
        return File(getDataFolder(), ConfigUtils.displayResourcePackOutputFolder)
    }

    private fun outputZip(): File {
        return File(getDataFolder(), "${ConfigUtils.displayResourcePackOutputFolder}.zip")
    }

    private fun normalizeImageName(image: String): String {
        return image.trim().replace('\\', '/').substringAfterLast('/').lowercase(Locale.getDefault())
    }

    private fun generateResourcePack(images: List<File>) {
        val folder = outputFolder()
        if (folder.exists()) {
            folder.deleteRecursively()
        }
        val namespace = sanitizeNamespace(ConfigUtils.displayResourcePackNamespace)
        val textureFolder = File(folder, "assets/$namespace/textures/title")
        textureFolder.mkdirs()
        images.forEach { source ->
            source.copyTo(File(textureFolder, source.name), overwrite = true)
        }
        writePackMeta(folder)
        writeFontJson(folder, namespace)
        zipFolder(folder, outputZip())
        lastSha1 = sha1(outputZip())
        FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 图片称号资源包已生成: ${outputZip().absolutePath}")
        FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 图片称号资源包 SHA1: $lastSha1")
    }

    private fun sanitizeNamespace(raw: String): String {
        val namespace = raw.trim().lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9_.-]"), "_")
        return namespace.ifBlank { "freeswitchtitle" }
    }

    private fun writePackMeta(folder: File) {
        val file = File(folder, "pack.mcmeta")
        file.parentFile?.mkdirs()
        file.writeText(
            """
            {
              "pack": {
                "pack_format": 22,
                "description": "FreeSwitchTitle image title resource pack"
              }
            }
            """.trimIndent(),
            Charsets.UTF_8
        )
    }

    private fun writeFontJson(folder: File, namespace: String) {
        val fontFile = File(folder, "assets/minecraft/font/default.json")
        fontFile.parentFile?.mkdirs()
        val providers = imageGlyphs.entries.joinToString(",\n") { (image, glyph) ->
            """
                {
                  "type": "bitmap",
                  "file": "$namespace:title/$image",
                  "ascent": ${ConfigUtils.displayResourcePackDefaultAscent},
                  "height": ${ConfigUtils.displayResourcePackDefaultHeight},
                  "chars": ["${escapeJson(glyph)}"]
                }
            """.trimIndent()
        }
        fontFile.writeText(
            """
            {
              "providers": [
            $providers
              ]
            }
            """.trimIndent(),
            Charsets.UTF_8
        )
    }

    private fun escapeJson(text: String): String {
        return text.flatMap { char ->
            when (char) {
                '\\' -> listOf('\\', '\\')
                '"' -> listOf('\\', '"')
                else -> listOf(char)
            }
        }.joinToString("")
    }

    private fun zipFolder(folder: File, zipFile: File) {
        if (zipFile.exists()) {
            zipFile.delete()
        }
        ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
            folder.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val entryName = folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                    zip.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
        }
    }

    private fun sha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
