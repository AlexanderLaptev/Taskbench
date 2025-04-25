package cs.vsu.taskbench.util

object Lipsum {
    @Suppress("SpellCheckingInspection")
    private val LIPSUM = listOf(
        "a", "ac", "accumsan", "adipiscing", "aenean", "aliquam", "aliquet", "amet", "ante",
        "arcu", "at", "auctor", "augue", "bibendum", "blandit", "commodo", "condimentum",
        "congue", "consectetur", "consequat", "convallis", "cras", "curabitur", "cursus",
        "dapibus", "diam", "dictum", "dignissim", "dolor", "donec", "dui", "duis", "efficitur",
        "egestas", "eget", "eleifend", "elementum", "elit", "enim", "erat", "eros", "est",
        "et", "etiam", "eu", "euismod", "ex", "facilisis", "faucibus", "felis", "fermentum",
        "feugiat", "finibus", "fringilla", "fusce", "gravida", "hendrerit", "iaculis", "id",
        "imperdiet", "in", "integer", "ipsum", "justo", "lacinia", "lacus", "laoreet", "lectus",
        "leo", "libero", "ligula", "lobortis", "lorem", "luctus", "maecenas", "magna", "malesuada",
        "massa", "mattis", "mauris", "metus", "mi", "molestie", "mollis", "morbi", "nec", "neque",
        "nibh", "nisi", "nisl", "non", "nulla", "nullam", "nunc", "odio", "orci", "ornare",
        "pellentesque", "pharetra", "phasellus", "placerat", "porta", "porttitor", "posuere",
        "praesent", "pretium", "proin", "pulvinar", "purus", "quam", "quis", "quisque", "rhoncus",
        "risus", "rutrum", "sagittis", "sapien", "scelerisque", "sed", "sem", "semper", "sit",
        "sodales", "sollicitudin", "suscipit", "tellus", "tempor", "tempus", "tincidunt", "tortor",
        "tristique", "turpis", "ullamcorper", "ultrices", "ultricies", "urna", "ut", "varius",
        "vehicula", "vel", "velit", "venenatis", "vestibulum", "vitae", "vivamus", "volutpat",
        "vulputate",
    )

    fun get(capitalize: Boolean = true): String =
        get(4, 11, capitalize)

    fun get(min: Int, max: Int, capitalize: Boolean = true): String =
        get(MockRandom.nextInt(min, max + 1), capitalize)

    fun get(wordCount: Int, capitalize: Boolean = true): String {
        val words = mutableListOf<String>()
        repeat(wordCount) {
            words += LIPSUM.random(MockRandom)
        }
        if (capitalize) words[0] = words[0].replaceFirstChar { it.uppercaseChar() }
        return words.joinToString(" ")
    }
}
