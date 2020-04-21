package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
    val result = mutableListOf<List<Pair<Int, Int>>>()

    bounds.forEach { (startBound, endBound) ->
        val boundRange = startBound..endBound
        val listOfResultInCurrentBound = mutableListOf<Pair<Int, Int>>()
        this.forEach { (startInterval, endInterval) ->
            if (startInterval in boundRange && endInterval in boundRange) {
                listOfResultInCurrentBound.add(startInterval to endInterval)
            } else if (startInterval in boundRange) {
                listOfResultInCurrentBound.add(startInterval to endBound)
            } else if (endInterval in boundRange && endInterval != startBound) {
                listOfResultInCurrentBound.add(startBound to endInterval)
            }
        }
        result.add(listOfResultInCurrentBound)
    }
    return result
}