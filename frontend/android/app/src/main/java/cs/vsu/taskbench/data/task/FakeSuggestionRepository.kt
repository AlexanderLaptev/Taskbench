package cs.vsu.taskbench.data.task

import android.util.Log
import kotlin.random.Random

object FakeSuggestionRepository : SuggestionRepository {
    private val TAG = FakeSuggestionRepository::class.simpleName

    private val LIPSUM =
        ("lorem ipsum dolor sit amet consectetur adipiscing elit phasellus dictum sem nec " +
                "convallis egestas quisque felis ligula imperdiet non porta eget euismod vitae " +
                "massa nunc feugiat tortor sit amet tempus ultrices elit massa auctor neque a " +
                "pulvinar elit lacus at orci etiam dictum lacus quis consequat dictum cras " +
                "vulputate pulvinar enim eget elementum").splitToSequence(" ").distinct().toList()

    override suspend fun getSuggestions(prompt: String): List<String> {
        Log.d(TAG, "requested suggestions")

        if (prompt.isBlank()) {
            Log.d(TAG, "empty prompt, returning nothing")
            return listOf()
        }

        val random = Random(prompt.hashCode())
        val result = mutableListOf<String>()
        repeat(random.nextInt(0, 7)) {
            val words = mutableListOf<String>()
            repeat(random.nextInt(4, 9)) {
                words += LIPSUM.random(random)
            }
            result += "${it + 1}. ${words.joinToString(" ")}"
        }

        Log.d(TAG, "returned ${result.size} suggestions")
        return result
    }
}
