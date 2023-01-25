package com.github.theapache64.gfxavg

data class GfxInfo(
    val totalFrames: Int,// Total frames rendered: 157
    val jankyFrames: Int,// Janky frames: 126 (80.25%)
    val jankyFramesPerc: Double,// Janky frames: 126 (80.25%)
    val p50: Int,// 50th percentile: 19ms
    val p90: Int,// 90th percentile: 30ms
    val p95: Int,// 95th percentile: 77ms
    val p99: Int,// 99th percentile: 450ms
    val missedVsyncs: Int,// Number Missed Vsync: 8
    val highInputLatency: Int,// Number High input latency: 130
    val slowUiThreads: Int, // Number Slow UI thread: 9
    val slowBitmapUploads: Int, // Number Slow bitmap uploads: 0
    val slowDrawCommands: Int, // Number Slow issue draw commands: 5
    val frameDeadlineMissed: Int, // Number Frame deadline missed: 12
    val p50Cpu: Int, // 50th gpu percentile: 11ms
    val p90Cpu: Int, // 90th gpu percentile: 14ms
    val p95Cpu: Int,// 95th gpu percentile: 14ms
    val p99Cpu: Int, // 99th gpu percentile: 19ms
) {
    fun toReport(): String {
        return this.toString()
            .replace("(GfxInfo|\\(|\\)|\\s)".toRegex(), "")
            .replace("=", " : ")
            .split(",").joinToString("\n") {
                val (key, value) = it.split(" : ")
                val suffix = getSuffix(key)
                "$key : $value$suffix"
            }
    }
}