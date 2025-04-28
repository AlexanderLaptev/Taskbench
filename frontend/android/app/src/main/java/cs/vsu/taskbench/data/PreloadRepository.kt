package cs.vsu.taskbench.data

interface PreloadRepository {
    suspend fun preload()
}
