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

fun main(args: Array<String>) {
    println("➡️ Initializing...")
    val userDir = File(System.getProperty("user.dir"))
    val gfxInfoList = mutableListOf<GfxInfo>()
    userDir.walk()
        .forEach { file ->
            if (file.isDirectory) return@forEach
            val fileContents = file.readText()
            val matchResult = gfxInfoRegEx.find(fileContents)
            if (matchResult != null) {
                println("--------------------")
                println("👓Parsing ${file.absolutePath}")
                println("--------------------")
                parseGfxInfo(matchResult).also {
                    gfxInfoList.add(it)
                    println(it.toReport())
                }
            } else {
                println("⚠️ Not gfxinfo file. Skipping ${file.absolutePath}")
            }
        }

    if (gfxInfoList.isEmpty()) {
        println("❌ Couldn't find any gfxinfo file in ${userDir.absolutePath}")
        return
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
        p50Cpu = groups["p50Cpu"]!!.value.toInt(),
        p90Cpu = groups["p90Cpu"]!!.value.toInt(),
        p95Cpu = groups["p95Cpu"]!!.value.toInt(),
        p99Cpu = groups["p99Cpu"]!!.value.toInt(),
    )
}
