package com.my.calculator.core.model

import com.my.calculator.core.math.MathValue
import com.my.calculator.core.math.SexagesimalRewriter

data class TreeBuildResult(
    val displayString: String,
    val exprString: String,
    val calcOutput: Boolean,
    val startNode: MathNode,
    val cursorNode: MathNode,
    val stoNode: MathNode?
)

object TreeBuilder {

    fun build(
        history: List<String>,
        angleMode: String = "Deg",
        userLang: String = "en-US",
        userVars: Map<String, MathValue> = emptyMap(),
        lastResult: MathValue? = null,
        showCursor: Boolean = true
    ): TreeBuildResult {
        val startNode = MathNode(NodeKind.Start, "", "")
        var cursor: MathNode = startNode
        var nextAlignId = 1
        var nextSubresId = 1
        var calcOutput = false

        for (inputCode in history) {
            val oldNeighbors = arrayOf(cursor.left, cursor.down, cursor.right, cursor.up)
            val newElements = mutableListOf<MathNode>()

            when {
                inputCode == "key_on" -> {
                    // Handled at handler level
                }
                inputCode.startsWith("key_") && inputCode.length == 5 && inputCode[4].isDigit() -> {
                    val digit = inputCode.substring(4)
                    val n = MathNode(NodeKind.Int, digit, digit).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_comma" -> {
                    val sep = if (userLang.startsWith("de")) "," else "."
                    val n = MathNode(NodeKind.PointOp, sep, ".").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_pow10" -> {
                    val n = MathNode(NodeKind.PointOp, "<span class='pow10'>×⒑</span>", "e").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_plus" -> {
                    val n = MathNode(NodeKind.AdditiveOp, "+", "+").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_minus" || inputCode == "key_neg" -> {
                    val n = MathNode(NodeKind.AdditiveOp, "-", "-").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_x" -> {
                    val n = MathNode(NodeKind.MultiOp, "×", "*").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_div" -> {
                    val n = MathNode(NodeKind.MultiOp, "÷", "/").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_lparen" -> {
                    val n = MathNode(NodeKind.BracketsOp, "(", "(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_rparen" -> {
                    val n = MathNode(NodeKind.BracketsClose, ")", ")").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_deg" -> {
                    val n = MathNode(NodeKind.PointOp, "°", "°").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_faculty" -> {
                    val n = MathNode(NodeKind.PointOp, "!", "!").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_sin" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "sind("
                        "Gra" -> "sing("
                        else -> "sin("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "sin(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_cos" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "cosd("
                        "Gra" -> "cosg("
                        else -> "cos("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "cos(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_tan" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "tand("
                        "Gra" -> "tang("
                        else -> "tan("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "tan(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_sin_minus1" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "180/PI*asin("
                        "Gra" -> "200/PI*asin("
                        else -> "asin("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "sin<span class='pow_top'>-1</span>(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_cos_minus1" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "180/PI*acos("
                        "Gra" -> "200/PI*acos("
                        else -> "acos("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "cos<span class='pow_top'>-1</span>(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_tan_minus1" -> {
                    val fn = when (angleMode) {
                        "Deg" -> "180/PI*atan("
                        "Gra" -> "200/PI*atan("
                        else -> "atan("
                    }
                    val n = MathNode(NodeKind.BracketsOp, "tan<span class='pow_top'>-1</span>(", fn).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_sinh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "sinh(", "sinh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_cosh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "cosh(", "cosh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_tanh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "tanh(", "tanh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_asinh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "sinh<span class='pow_top'>-1</span>(", "asinh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_acosh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "cosh<span class='pow_top'>-1</span>(", "acosh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_atanh" -> {
                    val n = MathNode(NodeKind.BracketsOp, "tanh<span class='pow_top'>-1</span>(", "atanh(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_log" -> {
                    val n = MathNode(NodeKind.BracketsOp, "log(", "log10(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_ln" -> {
                    val n = MathNode(NodeKind.BracketsOp, "ln(", "log(").apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_i" -> {
                    val n = MathNode(NodeKind.Var, "i", "").apply {
                        varName = "i"
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_pi" -> {
                    val n = MathNode(NodeKind.Var, "<i>π</i>", "").apply {
                        constIndex = 40
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_e" -> {
                    val n = MathNode(NodeKind.Var, "<i>e</i>", "").apply {
                        constIndex = 41
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_perc" -> {
                    val n = MathNode(NodeKind.Var, "%", "").apply {
                        constIndex = 42
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode.startsWith("key_CONST") -> {
                    val idx = inputCode.substring(10).toInt() - 1
                    val n = MathNode(NodeKind.Var, CONST_CHARS[idx], "").apply {
                        constIndex = idx
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_Ans" -> {
                    val n = MathNode(NodeKind.Var, "Ans", "").apply {
                        varName = "Ans"
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode.startsWith("key_uservar_") -> {
                    val name = inputCode.substring(12)
                    val n = MathNode(NodeKind.Var, name, "").apply {
                        varName = name
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    cursor.right = n
                    oldNeighbors[2]?.left = n
                    newElements.add(n)
                    cursor = n
                }
                inputCode == "key_frac" -> {
                    val (nextLeft, lastElement, firstElement) = getLeftBlock(cursor)
                    val alignId = nextAlignId++
                    val fracOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='alignLeft$alignId'></span><span class='frac_wrapper'><span class='frac_top'>",
                        "(["
                    ).apply {
                        left = nextLeft
                    }

                    val child0 = MathNode(
                        NodeKind.Container,
                        "</span><span class='frac_bottom alignRight$alignId'>",
                        "][1]/["
                    ).apply {
                        parent = fracOp
                        isLastContainer = false
                    }

                    val child1 = MathNode(
                        NodeKind.Container,
                        "</span></span>",
                        "][1])"
                    ).apply {
                        parent = fracOp
                        isLastContainer = true
                    }

                    fracOp.children = mutableListOf(child0, child1)
                    nextLeft.right = fracOp

                    if (lastElement != null) {
                        lastElement.left = fracOp
                        fracOp.right = lastElement

                        child0.left = firstElement
                        firstElement!!.right = child0

                        var element: MathNode? = firstElement
                        val blockElements = mutableSetOf<MathNode>()
                        while (element != null && element !== lastElement.left) {
                            blockElements.add(element)
                            element = element.left
                        }
                        element = firstElement
                        while (element != null && element !== lastElement.left) {
                            if (element.down == null || element.down !in blockElements) {
                                element.down = child0
                            }
                            element = element.left
                        }
                        fracOp.skipToAfterCreation = child0
                    } else {
                        fracOp.right = child0
                        child0.left = fracOp
                    }

                    child0.right = child1
                    child1.left = child0
                    child1.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child1

                    fracOp.down = child0
                    child0.up = child0

                    newElements.add(fracOp)
                    cursor = fracOp
                }
                inputCode == "key_sqrt" -> {
                    val sqrtOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='sqrt_wrapper'><span class='scale_height'>√</span><span class='sqrt'>",
                        "nthRootComplex(2,["
                    ).apply {
                        left = cursor
                        right = oldNeighbors[2]
                    }
                    val child0 = MathNode(
                        NodeKind.Container,
                        "</span></span>",
                        "][1])"
                    ).apply {
                        parent = sqrtOp
                        isLastContainer = true
                    }
                    sqrtOp.children = mutableListOf(child0)
                    cursor.right = sqrtOp
                    sqrtOp.right = child0
                    child0.left = sqrtOp
                    child0.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child0

                    newElements.add(sqrtOp)
                    cursor = sqrtOp
                }
                inputCode == "key_sqrtn" || inputCode == "key_sqrt3" -> {
                    val is3 = inputCode == "key_sqrt3"
                    val sqrtnOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='pow_top'>",
                        "nthRootComplex(["
                    ).apply {
                        left = cursor
                    }
                    val child0 = MathNode(
                        NodeKind.Container,
                        "</span><span class='sqrt_wrapper'><span class='scale_height'>√</span><span class='sqrt'>",
                        "][1],["
                    ).apply {
                        parent = sqrtnOp
                        isLastContainer = false
                    }
                    val child1 = MathNode(
                        NodeKind.Container,
                        "</span></span>",
                        "][1])"
                    ).apply {
                        parent = sqrtnOp
                        isLastContainer = true
                    }
                    sqrtnOp.children = mutableListOf(child0, child1)
                    cursor.right = sqrtnOp
                    sqrtnOp.right = child0
                    child0.left = sqrtnOp
                    child0.right = child1
                    child1.left = child0
                    child1.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child1

                    newElements.add(sqrtnOp)

                    if (is3) {
                        cursor = child0
                        val prefilled = MathNode(NodeKind.Int, "3", "3").apply {
                            left = sqrtnOp
                            right = child0
                        }
                        sqrtnOp.right = prefilled
                        child0.left = prefilled
                        newElements.add(prefilled)
                    } else {
                        cursor = sqrtnOp
                    }
                }
                inputCode == "key_pown" || inputCode == "key_pow2" || inputCode == "key_pow3" ||
                inputCode == "key_pow_minus1" || inputCode == "key_epow" -> {
                    val isPrefilled = inputCode != "key_pown"
                    val (nextLeft, lastElement, firstElement) = getLeftBlock(cursor)

                    val powOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='pow_bottom'>(",
                        "(["
                    ).apply {
                        left = nextLeft
                    }

                    val child0 = MathNode(
                        NodeKind.Container,
                        ")</span><span class='pow_top'>",
                        "][1]^["
                    ).apply {
                        parent = powOp
                        isLastContainer = false
                    }

                    val child1 = MathNode(
                        NodeKind.Container,
                        "</span>",
                        "][1])"
                    ).apply {
                        parent = powOp
                        isLastContainer = true
                    }

                    powOp.children = mutableListOf(child0, child1)
                    nextLeft.right = powOp

                    if (lastElement != null) {
                        lastElement.left = powOp
                        powOp.right = lastElement
                        child0.left = firstElement
                        firstElement!!.right = child0
                        powOp.skipToAfterCreation = if (isPrefilled) child1 else child0
                    } else {
                        powOp.right = child0
                        child0.left = powOp
                    }

                    child0.right = child1
                    child1.left = child0
                    child1.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child1

                    newElements.add(powOp)
                    cursor = powOp

                    when (inputCode) {
                        "key_pow2", "key_pow3" -> {
                            val exp = inputCode.substring(7)
                            val prefilled = MathNode(NodeKind.Int, exp, exp).apply {
                                left = child0
                                right = child1
                            }
                            child0.right = prefilled
                            child1.left = prefilled
                            newElements.add(prefilled)
                        }
                        "key_pow_minus1" -> {
                            val prefilled1 = MathNode(NodeKind.AdditiveOp, "-", "-").apply {
                                left = child0
                            }
                            val prefilled2 = MathNode(NodeKind.Int, "1", "1").apply {
                                left = prefilled1
                                right = child1
                            }
                            child0.right = prefilled1
                            prefilled1.right = prefilled2
                            child1.left = prefilled2
                            newElements.add(prefilled1)
                            newElements.add(prefilled2)
                        }
                        "key_epow" -> {
                            val prefilled = MathNode(NodeKind.Var, "<i>e</i>", "").apply {
                                constIndex = 41
                                left = powOp
                                right = child0
                            }
                            powOp.right = prefilled
                            child0.left = prefilled
                            newElements.add(prefilled)
                            cursor = child0
                        }
                    }
                }
                inputCode == "key_logn" -> {
                    val lognOp = MathNode(
                        NodeKind.ContainerOp,
                        "log<span class='logn_bottom'>",
                        "(1/log(["
                    ).apply {
                        left = cursor
                    }
                    val child0 = MathNode(
                        NodeKind.Container,
                        "</span>(",
                        "][1])*log(["
                    ).apply {
                        parent = lognOp
                        isLastContainer = false
                    }
                    val child1 = MathNode(
                        NodeKind.Container,
                        ")",
                        "][1]))"
                    ).apply {
                        parent = lognOp
                        isLastContainer = true
                    }
                    lognOp.children = mutableListOf(child0, child1)
                    cursor.right = lognOp
                    lognOp.right = child0
                    child0.left = lognOp
                    child0.right = child1
                    child1.left = child0
                    child1.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child1

                    newElements.add(lognOp)
                    cursor = lognOp
                }
                inputCode == "key_integ" -> {
                    val subresId = nextSubresId++
                    val integOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='integ_wrapper'><span class='scale_height'>∫</span><span class='integ_wrapper_2'><span class='integ_equation'>",
                        "(subres${subresId}idinsert)subres${subresId}idstart"
                    ).apply {
                        left = cursor
                    }
                    val child0 = MathNode(
                        NodeKind.Container,
                        "dX</span><span class='integ_wrapper_3'><span class='integ_top'>",
                        "subres${subresId}idparam"
                    ).apply {
                        parent = integOp
                        isLastContainer = false
                    }
                    val child1 = MathNode(
                        NodeKind.Container,
                        "</span><span class='integ_bottom'>",
                        "subres${subresId}idparam"
                    ).apply {
                        parent = integOp
                        isLastContainer = false
                    }
                    val child2 = MathNode(
                        NodeKind.Container,
                        "</span></span></span></span>",
                        "subres${subresId}idend"
                    ).apply {
                        parent = integOp
                        isLastContainer = true
                    }
                    integOp.children = mutableListOf(child0, child1, child2)
                    cursor.right = integOp
                    integOp.right = child0
                    child0.left = integOp
                    child0.right = child1
                    child1.left = child0
                    child1.right = child2
                    child2.left = child1
                    child2.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child2

                    newElements.add(integOp)
                    cursor = integOp
                }
                inputCode == "key_deriv" -> {
                    val subresId = nextSubresId++
                    val derivOp = MathNode(
                        NodeKind.ContainerOp,
                        "<span class='frac_wrapper'><span class='frac_top'>d</span><span class='frac_bottom'>dx</span></span>(",
                        "(subres${subresId}idinsert)subres${subresId}idstart"
                    ).apply {
                        left = cursor
                    }
                    val child0 = MathNode(
                        NodeKind.Container,
                        ")|<span class='logn_bottom'><i>x</i>=",
                        "subres${subresId}idparam"
                    ).apply {
                        parent = derivOp
                        isLastContainer = false
                    }
                    val child1 = MathNode(
                        NodeKind.Container,
                        "</span>",
                        "subres${subresId}idend"
                    ).apply {
                        parent = derivOp
                        isLastContainer = true
                    }
                    derivOp.children = mutableListOf(child0, child1)
                    cursor.right = derivOp
                    derivOp.right = child0
                    child0.left = derivOp
                    child0.right = child1
                    child1.left = child0
                    child1.right = oldNeighbors[2]
                    oldNeighbors[2]?.left = child1

                    newElements.add(derivOp)
                    cursor = derivOp
                }
                inputCode.startsWith("key_STO_") || inputCode == "key_M_plus" || inputCode == "key_M_minus" -> {
                    if (cursor.kind != NodeKind.Start || cursor.right != null) {
                        while (cursor.right != null) {
                            cursor = cursor.right!!
                        }
                        val stoNode = if (inputCode.startsWith("key_STO_")) {
                            val varName = inputCode.substring(8)
                            MathNode(NodeKind.Sto, "→$varName", "").apply {
                                this.varName = varName
                            }
                        } else {
                            val signStr = if (inputCode == "key_M_minus") "-" else "+"
                            MathNode(NodeKind.Sto, "M$signStr", "").apply {
                                this.varName = "M"
                                this.sign = if (signStr == "-") -1.0 else 1.0
                            }
                        }
                        stoNode.left = cursor
                        cursor.right = stoNode
                        newElements.add(stoNode)
                        cursor = stoNode
                    }
                }
                inputCode == "key_equals" -> {
                    if (cursor.kind != NodeKind.Start || cursor.right != null) {
                        calcOutput = true
                    }
                }
                inputCode == "key_del" -> {
                    when (cursor.kind) {
                        NodeKind.Start -> {}
                        NodeKind.Container -> {
                            cursor = cursor.left ?: cursor
                        }
                        NodeKind.ContainerOp -> {
                            val toDelete = listOf(cursor) + (cursor.children ?: emptyList())
                            val prevLeft = cursor.left
                            cursor = prevLeft ?: startNode
                            var element: MathNode? = toDelete.lastOrNull()
                            val firstLeft = toDelete.first().left
                            while (element != null && element !== firstLeft) {
                                if (element in toDelete) {
                                    element.left?.right = element.right
                                    element.right?.left = element.left
                                } else {
                                    if (element.down in toDelete) {
                                        element.down = toDelete.last().down
                                    }
                                    if (element.up in toDelete) {
                                        element.up = toDelete.last().up
                                    }
                                }
                                element = element.left
                            }
                        }
                        else -> {
                            val prevLeft = cursor.left
                            cursor = prevLeft ?: startNode
                            cursor.right = oldNeighbors[2]
                            oldNeighbors[2]?.left = cursor
                        }
                    }
                }
                inputCode == "key_dir1" || inputCode == "key_dir3" -> {
                    val dir = inputCode.substring(7).toInt()
                    val target = cursor.getNeighbor(dir)
                    if (target != null) {
                        cursor = target
                        if (dir == 3) {
                            cursor = cursor.left ?: cursor
                        }
                    }
                }
                inputCode == "key_dir0" || inputCode == "key_dir2" -> {
                    var dir = inputCode.substring(7).toInt()
                    val target = cursor.getNeighbor(dir)
                    if (target != null) {
                        cursor = target
                    } else {
                        dir = (dir + 2) % 4
                        while (cursor.getNeighbor(dir) != null) {
                            cursor = cursor.getNeighbor(dir)!!
                        }
                    }
                }
                inputCode == "pos1" -> {
                    while (cursor.left != null) {
                        cursor = cursor.left!!
                    }
                }
                inputCode == "end" -> {
                    while (cursor.right != null) {
                        cursor = cursor.right!!
                    }
                }
            }

            if (newElements.isNotEmpty()) {
                if (newElements[0].skipToAfterCreation != null) {
                    cursor = newElements[0].skipToAfterCreation!!
                }

                if (oldNeighbors[2] != null) {
                    if (newElements[0].children != null) {
                        val lastChild = newElements[0].children!!.last()
                        lastChild.right = oldNeighbors[2]
                        oldNeighbors[2]!!.left = lastChild
                    } else {
                        newElements[0].right = oldNeighbors[2]
                        oldNeighbors[2]!!.left = newElements[0]
                    }
                }

                for (n in newElements) {
                    if (n.kind == NodeKind.ContainerOp) {
                        n.setContainerNeighbors(oldNeighbors)
                    } else {
                        if (n.down == null) n.down = oldNeighbors[1]
                        if (n.up == null) n.up = oldNeighbors[3]
                    }
                }
            }
        }

        // Render pass
        var res = ""
        var mathjsRes = ""
        var curr: MathNode? = startNode
        var placeholderSelect = false
        var sto: MathNode? = null

        while (curr != null) {
            when (curr.kind) {
                NodeKind.Start -> {}
                NodeKind.ContainerOp, NodeKind.Container -> {
                    if (isFamily(curr, curr.right)) {
                        res += curr.display
                        if (showCursor && curr === cursor) {
                            res += "<span class='cursor'>\uE000</span>"
                            placeholderSelect = true
                        }
                        res += "▯"
                    } else {
                        res += curr.display
                    }
                }
                else -> {
                    res += curr.display
                }
            }

            if (showCursor && curr === cursor && !placeholderSelect) {
                res += "<span class='cursor'>\uE000</span>"
            }

            if (sto != null) {
                mathjsRes = "error"
                break
            } else {
                when (curr.kind) {
                    NodeKind.Var -> {
                        mathjsRes += "(" + curr.getVarValue(userVars, lastResult) + ")"
                    }
                    NodeKind.Sto -> {
                        sto = curr
                    }
                    else -> {
                        mathjsRes += curr.expr
                    }
                }
            }

            curr = curr.right
        }

        res += "\u00A0"
        mathjsRes = SexagesimalRewriter.rewrite(mathjsRes)

        return TreeBuildResult(
            displayString = res,
            exprString = mathjsRes,
            calcOutput = calcOutput,
            startNode = startNode,
            cursorNode = cursor,
            stoNode = sto
        )
    }
}
