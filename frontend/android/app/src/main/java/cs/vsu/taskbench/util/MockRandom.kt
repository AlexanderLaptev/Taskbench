package cs.vsu.taskbench.util

import kotlin.random.Random

object MockRandom : Random() {
    private const val SEED = 4242
    private var _random = Random(SEED)

    fun reset() {
        _random = Random(SEED)
    }

    override fun nextInt(): Int = _random.nextInt()
    override fun nextBits(bitCount: Int): Int = _random.nextBits(bitCount)
}
