package cs.vsu.taskbench.util

fun String.toAuthHeader(): String = "Bearer $this"
