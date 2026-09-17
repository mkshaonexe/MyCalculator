package com.my.calculator.display

object HtmlToDisplayNode {

    private sealed interface Token {
        data class OpenTag(val name: String, val clazz: String) : Token
        data class CloseTag(val name: String) : Token
        data class TextToken(val text: String) : Token
    }

    private data class Element(
        val tag: String,
        val clazz: String,
        val children: MutableList<Any> = mutableListOf() // Element or String
    )

    fun parse(html: String): DisplayNode {
        if (html.isEmpty()) return DisplayNode.Row(emptyList())

        val cleanHtml = html.replace("&nbsp;", " ")
        val tokens = tokenize(cleanHtml)
        val root = Element("root", "")
        val stack = ArrayDeque<Element>()
        stack.addLast(root)

        for (token in tokens) {
            when (token) {
                is Token.OpenTag -> {
                    if (token.name == "br") {
                        stack.last().children.add(Element("br", ""))
                    } else {
                        val el = Element(token.name, token.clazz)
                        stack.last().children.add(el)
                        stack.addLast(el)
                    }
                }
                is Token.CloseTag -> {
                    if (stack.size > 1 && stack.last().tag == token.name) {
                        stack.removeLast()
                    }
                }
                is Token.TextToken -> {
                    stack.last().children.add(token.text)
                }
            }
        }

        return elementToDisplayNode(root)
    }

    private fun tokenize(html: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val n = html.length

        while (i < n) {
            if (html[i] == '<') {
                val closeIdx = html.indexOf('>', i)
                if (closeIdx != -1) {
                    val tagContent = html.substring(i + 1, closeIdx).trim()
                    if (tagContent.startsWith("/")) {
                        tokens.add(Token.CloseTag(tagContent.substring(1).trim().lowercase()))
                    } else if (tagContent.lowercase() == "br" || tagContent.lowercase() == "br/") {
                        tokens.add(Token.OpenTag("br", ""))
                    } else {
                        val spaceIdx = tagContent.indexOf(' ')
                        val tagName = if (spaceIdx != -1) tagContent.substring(0, spaceIdx).lowercase() else tagContent.lowercase()
                        var clazz = ""
                        val classIdx = tagContent.indexOf("class=")
                        if (classIdx != -1) {
                            val quote = tagContent[classIdx + 6]
                            val endQuote = tagContent.indexOf(quote, classIdx + 7)
                            if (endQuote != -1) {
                                clazz = tagContent.substring(classIdx + 7, endQuote)
                            }
                        }
                        tokens.add(Token.OpenTag(tagName, clazz))
                    }
                    i = closeIdx + 1
                    continue
                }
            }

            val nextTag = html.indexOf('<', i)
            val textEnd = if (nextTag != -1) nextTag else n
            val text = html.substring(i, textEnd)
            if (text.isNotEmpty()) {
                tokens.add(Token.TextToken(text))
            }
            i = textEnd
        }

        return tokens
    }

    private fun elementToDisplayNode(el: Element): DisplayNode {
        val childrenNodes = mutableListOf<DisplayNode>()

        var i = 0
        while (i < el.children.size) {
            val child = el.children[i]
            if (child is String) {
                var txt = child
                if (txt.contains("▯")) {
                    val parts = txt.split("▯")
                    for (p in parts.indices) {
                        if (parts[p].isNotEmpty()) {
                            childrenNodes.add(DisplayNode.Text(parts[p]))
                        }
                        if (p < parts.size - 1) {
                            childrenNodes.add(DisplayNode.Placeholder)
                        }
                    }
                } else {
                    childrenNodes.add(DisplayNode.Text(txt))
                }
                i++
                continue
            }

            val childEl = child as Element
            when {
                childEl.tag == "br" -> {
                    childrenNodes.add(DisplayNode.LineBreak)
                }
                childEl.clazz.contains("frac_wrapper") -> {
                    val topEl = childEl.children.filterIsInstance<Element>().firstOrNull { it.clazz.contains("frac_top") }
                    val bottomEl = childEl.children.filterIsInstance<Element>().firstOrNull { it.clazz.contains("frac_bottom") }
                    val topNode = if (topEl != null) elementToDisplayNode(topEl) else DisplayNode.Row(emptyList())
                    val bottomNode = if (bottomEl != null) elementToDisplayNode(bottomEl) else DisplayNode.Row(emptyList())
                    childrenNodes.add(DisplayNode.Frac(topNode, bottomNode))
                }
                childEl.clazz.contains("sqrt_wrapper") -> {
                    val sqrtEl = childEl.children.filterIsInstance<Element>().firstOrNull { it.clazz.contains("sqrt") }
                    val radNode = if (sqrtEl != null) elementToDisplayNode(sqrtEl) else DisplayNode.Row(emptyList())
                    childrenNodes.add(DisplayNode.Sqrt(radNode))
                }
                childEl.clazz.contains("pow_top") -> {
                    childrenNodes.add(DisplayNode.SupScript(elementToDisplayNode(childEl)))
                }
                childEl.clazz.contains("pow_bottom") -> {
                    childrenNodes.add(elementToDisplayNode(childEl))
                }
                childEl.clazz.contains("cursor") -> {
                    childrenNodes.add(DisplayNode.Cursor)
                }
                childEl.clazz.contains("logn_bottom") -> {
                    childrenNodes.add(DisplayNode.SubScript(elementToDisplayNode(childEl)))
                }
                childEl.tag == "i" -> {
                    val sub = elementToDisplayNode(childEl)
                    if (sub is DisplayNode.Text) {
                        childrenNodes.add(sub.copy(italic = true))
                    } else if (sub is DisplayNode.Row) {
                        childrenNodes.addAll(sub.children.map { if (it is DisplayNode.Text) it.copy(italic = true) else it })
                    } else {
                        childrenNodes.add(sub)
                    }
                }
                else -> {
                    childrenNodes.add(elementToDisplayNode(childEl))
                }
            }
            i++
        }

        return if (childrenNodes.size == 1) childrenNodes[0] else DisplayNode.Row(childrenNodes)
    }
}
