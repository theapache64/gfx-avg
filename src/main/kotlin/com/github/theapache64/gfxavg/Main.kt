package com.github.theapache64.gfxavg

import java.io.File

private val gfxInfoRegEx = """
    Total frames rendered: (?<totalFrames>\d+)
    Janky frames: (?<jankyFrames>\d+) \((?<jankyFramesPerc>\d+.\d+)%\)
    ?.*
    50th percentile: (?<p50>\d+)ms
    90th percentile: (?<p90>\d+)ms
    95th percentile: (?<p95>\d+)ms
    99th percentile: (?<p99>\d+)ms
    Number Missed Vsync: (?<missedVsyncs>\d+)
    Number High input latency: (?<highInputLatency>\d+)
    Number Slow UI thread: (?<slowUiThreads>\d+)
    Number Slow bitmap uploads: (?<slowBitmapUploads>\d+)
    Number Slow issue draw commands: (?<slowDrawCommands>\d+)
    Number Frame deadline missed: (?<frameDeadlineMissed>\d+)
    HISTOGRAM: (?<histogram>.+)
    ?.*
    ?.*
    50th gpu percentile: (?<p50Cpu>\d+)ms
    90th gpu percentile: (?<p90Cpu>\d+)ms
    95th gpu percentile: (?<p95Cpu>\d+)ms
    99th gpu percentile: (?<p99Cpu>\d+)ms
""".trimIndent().toRegex(
    setOf(
        RegexOption.MULTILINE
    )
)

private val gfxInfoRegExSimple = """
    Total frames rendered: (?<totalFrames>\d+)
    Janky frames: (?<jankyFrames>\d+) \((?<jankyFramesPerc>\d+.\d+)%\)
    ?.*
    50th percentile: (?<p50>\d+)ms
    90th percentile: (?<p90>\d+)ms
    95th percentile: (?<p95>\d+)ms
    99th percentile: (?<p99>\d+)ms
    Number Missed Vsync: (?<missedVsyncs>\d+)
    Number High input latency: (?<highInputLatency>\d+)
    Number Slow UI thread: (?<slowUiThreads>\d+)
    Number Slow bitmap uploads: (?<slowBitmapUploads>\d+)
    Number Slow issue draw commands: (?<slowDrawCommands>\d+)
    Number Frame deadline missed: (?<frameDeadlineMissed>\d+)
    ?.*
    HISTOGRAM: (?<histogram>.+)
""".trimIndent().toRegex(
    setOf(
        RegexOption.MULTILINE
    )
)

private val regExes = listOf(
    gfxInfoRegEx,
    gfxInfoRegExSimple
)

private val isDebug = true
private val debugDir = File("/Users/theapache64/Downloads/gfx-info")
fun main(args: Array<String>) {
    println("➡️ Initializing gfx-avg...")
    val isVerbose = args.contains("-v")

    val userDir = if (isDebug) debugDir else File(System.getProperty("user.dir"))
    val regEx = getMatchingRegex(userDir)
    if (regEx == null) {
        println("❌ Couldn't find matching regEx")
        return
    }

    val gfxInfoList = mutableListOf<GfxInfo>()
    userDir.walk()
        .forEach { file ->
            if (file.isDirectory) return@forEach
            val fileContents = file.readText()
            val matchResult = regEx.find(fileContents)
            if (matchResult != null) {
                println("--------------------")
                println("👓Parsing ${file.absolutePath}")
                println("--------------------")
                parseGfxInfo(matchResult).also {
                    gfxInfoList.add(it)
                    if (isVerbose) {
                        println(it.toReport())
                    }
                }
            } else {
                println("⚠️ Not gfxinfo file. Skipping ${file.absolutePath}")
            }
        }

    println("-----------")
    println("➗ Average")
    println("-----------")
    // Averaging out
    val averageGfxInfo = GfxInfo(
        totalFrames = gfxInfoList.sumOf { it.totalFrames } / gfxInfoList.size,
        jankyFrames = gfxInfoList.sumOf { it.jankyFrames } / gfxInfoList.size,
        jankyFramesPerc = gfxInfoList.sumOf { it.jankyFramesPerc } / gfxInfoList.size,
        p50 = gfxInfoList.sumOf { it.p50 } / gfxInfoList.size,
        p90 = gfxInfoList.sumOf { it.p90 } / gfxInfoList.size,
        p95 = gfxInfoList.sumOf { it.p95 } / gfxInfoList.size,
        p99 = gfxInfoList.sumOf { it.p99 } / gfxInfoList.size,
        missedVsyncs = gfxInfoList.sumOf { it.missedVsyncs } / gfxInfoList.size,
        highInputLatency = gfxInfoList.sumOf { it.highInputLatency } / gfxInfoList.size,
        slowUiThreads = gfxInfoList.sumOf { it.slowUiThreads } / gfxInfoList.size,
        slowBitmapUploads = gfxInfoList.sumOf { it.slowBitmapUploads } / gfxInfoList.size,
        slowDrawCommands = gfxInfoList.sumOf { it.slowDrawCommands } / gfxInfoList.size,
        frameDeadlineMissed = gfxInfoList.sumOf { it.frameDeadlineMissed } / gfxInfoList.size,
        p50Cpu = gfxInfoList.sumOf { it.p50Cpu } / gfxInfoList.size,
        p90Cpu = gfxInfoList.sumOf { it.p90Cpu } / gfxInfoList.size,
        p95Cpu = gfxInfoList.sumOf { it.p95Cpu } / gfxInfoList.size,
        p99Cpu = gfxInfoList.sumOf { it.p99Cpu } / gfxInfoList.size
    )

    println(
        averageGfxInfo.toReport()
    )

    println("------------")
    println("📈 Histogram")
    println("------------")

    // frames more than given ms
    arrayOf(100, 500, 700, 1000).forEach { duration ->
        println("Frames more than $duration ms = ${getAverageFrameCount(duration, gfxInfoList)}")
    }
}

fun getMatchingRegex(userDir: File): Regex? {
    // get a gfx avg info file
    val gfxAvgFile = userDir.walk()
        .find { file ->
            file.isFile && file.readLines().firstOrNull()?.startsWith("Applications Graphics Acceleration Info") == true
        } ?: return null

    println("☑️ Found sample gfxAvg file -> '${gfxAvgFile.absolutePath}'")
    var matchedRegex: Regex? = null
    val fileContent = gfxAvgFile.readText()
    for (regEx in regExes) {
        val matchResult = regEx.find(fileContent)
        if (matchResult != null) {
            matchedRegex = regEx
            break
        }
    }
    return matchedRegex
}

fun getAverageFrameCount(duration: Int, gfxInfoList: MutableList<GfxInfo>): Float {
    return gfxInfoList.sumOf { it.histogram.filter { entry -> entry.key >= duration }.values.sum() } / gfxInfoList.size.toFloat()
}

fun getSuffix(key: String): String {
    return when {
        key.endsWith("Perc") -> "%"
        key.matches("p\\d+(Cpu)?".toRegex()) -> "ms"
        else -> ""
    }
}

fun parseGfxInfo(matchResult: MatchResult): GfxInfo {
    val groups = matchResult.groups
    return GfxInfo(
        totalFrames = groups["totalFrames"]!!.value.toInt(),
        jankyFrames = groups["jankyFrames"]!!.value.toInt(),
        jankyFramesPerc = groups["jankyFramesPerc"]!!.value.toDouble(),
        p50 = groups["p50"]!!.value.toInt(),
        p90 = groups["p90"]!!.value.toInt(),
        p95 = groups["p95"]!!.value.toInt(),
        p99 = groups["p99"]!!.value.toInt(),
        missedVsyncs = groups["missedVsyncs"]!!.value.toInt(),
        highInputLatency = groups["highInputLatency"]!!.value.toInt(),
        slowUiThreads = groups["slowUiThreads"]!!.value.toInt(),
        slowBitmapUploads = groups["slowBitmapUploads"]!!.value.toInt(),
        slowDrawCommands = groups["slowDrawCommands"]!!.value.toInt(),
        frameDeadlineMissed = groups["frameDeadlineMissed"]!!.value.toInt(),
        p50Cpu = groups.getOrNull("p50Cpu")?.value?.toInt() ?: 0,
        p90Cpu = groups.getOrNull("p90Cpu")?.value?.toInt() ?: 0,
        p95Cpu = groups.getOrNull("p95Cpu")?.value?.toInt() ?: 0,
        p99Cpu = groups.getOrNull("p99Cpu")?.value?.toInt() ?: 0
    ).apply {
        histogram = parseHistogram(groups["histogram"]!!.value)
    }
}

private fun MatchGroupCollection.getOrNull(key: String): MatchGroup? {
    return try {
        get(key)
    } catch (e: IllegalArgumentException) {
        null
    }
}

// value : 5ms=0 6ms=9 7ms=5 8ms=7 9ms=25 10ms=27 11ms=62 12ms=78 13ms=79 14ms=82 15ms=108 16ms=62 17ms=43
fun parseHistogram(value: String): Map<Int, Int> {
    return mutableMapOf<Int, Int>().apply {
        value.split(" ").forEach {
            val (time, count) = it.split("ms=")
            put(time.trim().toInt(), count.trim().toInt())
        }
    }
}

