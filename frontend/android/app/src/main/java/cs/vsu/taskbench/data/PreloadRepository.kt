package cs.vsu.taskbench.data

interface PreloadRepository {
    /**
     * Preloads the data used by this repository.
     * @return true if authorized, false otherwise.
     */
    suspend fun preload(): Boolean
}
