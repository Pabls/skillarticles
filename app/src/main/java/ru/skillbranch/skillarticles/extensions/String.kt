package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    val list = mutableListOf<Int>()
    if (substr.isNotEmpty() && this != null) {
        var index = indexOf(string = substr, startIndex = 0, ignoreCase = ignoreCase)
        while (index >= 0) {
            list.add(index)
            index = indexOf(string = substr, startIndex = index + 1, ignoreCase = ignoreCase)
        }
    }
    return list
}