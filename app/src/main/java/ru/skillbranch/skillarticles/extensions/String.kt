package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(query: String,  ignoreCase: Boolean = true): List<Int> {
    if (this.isNullOrEmpty() || query.isBlank()) return emptyList()
    val results = ArrayList<Int>()
    var curIndex = 0
    while (curIndex < this.length){
        val index = if(ignoreCase)
            this.toLowerCase().indexOf(query.toLowerCase(), curIndex)
        else this.indexOf(query, curIndex)
        if (index < 0 ) break
        results.add(index)
        curIndex = index + query.length
    }
    return results

}
