package top.zoyn.freeswitchtitle.util

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object TitleImageFontManager {

    private data class ImageFontOptions(
        val glyph: String,
        val height: Int,
        val ascent: Int,
    )

    private data class StoredMapping(
        val id: String,
        val fileName: String,
        val glyph: String?,
        val height: Int?,
        val ascent: Int?,
    )

    private val fileNameRegex = Regex("^[A-Za-z0-9_.-]+\\.png$", RegexOption.IGNORE_CASE)
    private val imageGlyphs = linkedMapOf<String, String>()
    private val imageFiles = linkedMapOf<String, File>()
    private val imageOptions = linkedMapOf<String, ImageFontOptions>()
    private var lastSha1 = ""

    fun reload() {
        imageGlyphs.clear()
        imageFiles.clear()
        imageOptions.clear()
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
        val mappingFile = mappingFile()
        val mappingConfig = loadMappingConfig(mappingFile)
        val storedMappings = loadStoredMappings(mappingConfig)
        val usedCodepoints = storedMappings.values
            .mapNotNull { it.glyph?.codePointAtOrNull() }
            .toMutableSet()
        var changed = false
        validImages.forEach { file ->
            val key = normalizeImageName(file.name)
            val stored = storedMappings[key]
            val glyph = stored?.glyph ?: nextGlyph(usedCodepoints).also { changed = true }
            val height = stored?.height ?: ConfigUtils.displayResourcePackDefaultHeight.also { changed = true }
            val ascent = stored?.ascent ?: ConfigUtils.displayResourcePackDefaultAscent.also { changed = true }
            val id = stored?.id ?: uniqueMappingId(key, mappingConfig).also { changed = true }
            imageGlyphs[key] = glyph
            imageFiles[key] = file
            imageOptions[key] = ImageFontOptions(glyph, height, ascent)
            changed = writeMappingIfNeeded(mappingConfig, id, key, glyph, height, ascent) || changed
        }
        if (changed) {
            mappingConfig.saveToFile(mappingFile)
            FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 已更新图片称号映射: ${mappingFile.absolutePath}")
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

    fun getMappingFile(): File = mappingFile()

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

    private fun mappingFile(): File {
        return File(getDataFolder(), "image-mapping.yml")
    }

    private fun loadMappingConfig(file: File): Configuration {
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.writeText("images: {}\n", Charsets.UTF_8)
        }
        return Configuration.loadFromFile(file, Type.YAML)
    }

    private fun loadStoredMappings(config: Configuration): Map<String, StoredMapping> {
        val section = config.getConfigurationSection("images") ?: return emptyMap()
        return section.getKeys(false).mapNotNull { id ->
            val path = "images.$id"
            val fileName = config.getString("$path.file")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val normalized = normalizeImageName(fileName)
            StoredMapping(
                id = id,
                fileName = normalized,
                glyph = parseGlyph(config.getString("$path.char")?.trim().orEmpty()),
                height = config.getInt("$path.height", -1).takeIf { it > 0 },
                ascent = config.getInt("$path.ascent", -1).takeIf { it >= 0 },
            )
        }.associateBy { it.fileName }
    }

    private fun writeMappingIfNeeded(config: Configuration, id: String, image: String, glyph: String, height: Int, ascent: Int): Boolean {
        val path = "images.$id"
        var changed = false
        changed = setIfDifferent(config, "$path.file", image) || changed
        changed = setIfDifferent(config, "$path.char", encodeGlyph(glyph)) || changed
        changed = setIfDifferent(config, "$path.height", height) || changed
        changed = setIfDifferent(config, "$path.ascent", ascent) || changed
        return changed
    }

    private fun setIfDifferent(config: Configuration, path: String, value: Any): Boolean {
        if (config[path] == value) return false
        config[path] = value
        return true
    }

    private fun uniqueMappingId(imageName: String, config: Configuration): String {
        val base = imageName.substringBeforeLast('.')
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9_-]"), "_")
            .ifBlank { "image" }
        var id = base
        var index = 2
        while (config.contains("images.$id")) {
            id = "${base}_$index"
            index++
        }
        return id
    }

    private fun nextGlyph(usedCodepoints: MutableSet<Int>): String {
        var codepoint = ConfigUtils.displayResourcePackStartCodepoint
        while (codepoint in usedCodepoints) {
            codepoint++
        }
        usedCodepoints += codepoint
        return String(Character.toChars(codepoint))
    }

    private fun parseGlyph(raw: String): String? {
        val value = raw.trim().removeSurrounding("'").removeSurrounding("\"")
        if (value.isBlank()) return null
        val codepoint = when {
            value.startsWith("\\u", ignoreCase = true) && value.length >= 6 -> value.substring(2, 6).toIntOrNull(16)
            value.startsWith("0x", ignoreCase = true) -> value.substring(2).toIntOrNull(16)
            value.startsWith("U+", ignoreCase = true) -> value.substring(2).toIntOrNull(16)
            value.codePointCount(0, value.length) == 1 -> value.codePointAt(0)
            else -> null
        } ?: return null
        return String(Character.toChars(codepoint))
    }

    private fun encodeGlyph(glyph: String): String {
        val codepoint = glyph.codePointAtOrNull() ?: ConfigUtils.displayResourcePackStartCodepoint
        return if (codepoint <= 0xFFFF) {
            "\\u%04X".format(codepoint)
        } else {
            "U+%X".format(codepoint)
        }
    }

    private fun String.codePointAtOrNull(): Int? {
        return if (isBlank()) null else codePointAt(0)
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
            source.copyTo(File(textureFolder, normalizeImageName(source.name)), overwrite = true)
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
            val options = imageOptions[image]
            """
                {
                  "type": "bitmap",
                  "file": "$namespace:title/$image",
                  "ascent": ${options?.ascent ?: ConfigUtils.displayResourcePackDefaultAscent},
                  "height": ${options?.height ?: ConfigUtils.displayResourcePackDefaultHeight},
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
