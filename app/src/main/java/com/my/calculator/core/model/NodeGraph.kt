package com.my.calculator.core.model

fun isFamily(el1: MathNode?, el2: MathNode?): Boolean {
    if (el1 == null || el2 == null) return false

    val parentEl: MathNode
    val childEl: MathNode
    if (el1.kind == NodeKind.ContainerOp) {
        parentEl = el1
        childEl = el2
    } else if (el2.kind == NodeKind.ContainerOp) {
        parentEl = el2
        childEl = el1
    } else {
        if (el1.kind == NodeKind.Container && el2.kind == NodeKind.Container) {
            return el1.parent === el2.parent
        } else {
            return false
        }
    }

    if (childEl.kind == NodeKind.Container) {
        return parentEl === childEl.parent
    }
    return false
}

data class LeftBlockResult(
    val stopNode: MathNode,
    val lastElement: MathNode?,
    val firstElement: MathNode?
)

fun getLeftBlock(left: MathNode): LeftBlockResult {
    var nextLeftNeighbor: MathNode = left
    var lastElement: MathNode? = null
    var firstElement: MathNode? = null
    var bracketCounter = 0

    if (nextLeftNeighbor.kind == NodeKind.BracketsOp) {
        bracketCounter -= 1
    } else if (nextLeftNeighbor.kind == NodeKind.BracketsClose) {
        bracketCounter += 1
    }

    while (
        nextLeftNeighbor.kind != NodeKind.Start &&
        bracketCounter >= 0 &&
        (nextLeftNeighbor.kind !in listOf(NodeKind.AdditiveOp, NodeKind.MultiOp, NodeKind.Sto) || bracketCounter > 0)
    ) {
        if (firstElement == null) {
            firstElement = nextLeftNeighbor
        }
        if (nextLeftNeighbor.kind == NodeKind.Container) {
            if (nextLeftNeighbor.isLastContainer) {
                nextLeftNeighbor = nextLeftNeighbor.parent!!
            } else {
                break
            }
        } else if (nextLeftNeighbor.kind == NodeKind.ContainerOp) {
            break
        }
        lastElement = nextLeftNeighbor
        nextLeftNeighbor = nextLeftNeighbor.left ?: break

        if (nextLeftNeighbor.kind == NodeKind.BracketsOp) {
            bracketCounter -= 1
        } else if (nextLeftNeighbor.kind == NodeKind.BracketsClose) {
            bracketCounter += 1
        }
    }

    return LeftBlockResult(nextLeftNeighbor, lastElement, firstElement)
}
