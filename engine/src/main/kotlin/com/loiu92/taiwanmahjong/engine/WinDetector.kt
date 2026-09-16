package com.loiu92.taiwanmahjong.engine

/**
 * Detects whether a 16-tile Taiwanese hand (concealed tiles + melds) can form 5 sets + 1 pair
 * including [winningTile] if provided as part of the concealed tiles.
 */
object WinDetector {

    fun canWin(
        concealed: List<Tile>,
        melds: List<Meld>,
        flowers: List<Tile.Flower> = emptyList(),
    ): Boolean {
        if (flowers.size == 8) return true
        if (flowers.size == 7) return true // seven flowers can rob 8th; treat as callable win shape later
        val neededSets = 5 - melds.size
        if (neededSets < 0) return false
        val tiles = concealed.filter { !it.isFlower() }.sorted()
        // Winning hand has neededSets*3 + 2 tiles in concealed
        if (tiles.size != neededSets * 3 + 2) return false
        return canForm(tiles, neededSets)
    }

    fun isWinningHand(
        hand: List<Tile>,
        melds: List<Meld>,
        flowers: List<Tile.Flower>,
        winningTile: Tile,
        alreadyInHand: Boolean,
    ): Boolean {
        if (flowers.size == 8) return true
        val concealed = if (alreadyInHand) hand else hand + winningTile
        return canWin(concealed, melds, flowers)
    }

    /** Returns one decomposition into pair + sets from concealed tiles, or null. */
    fun decompose(concealed: List<Tile>, melds: List<Meld>): HandDecomposition? {
        val neededSets = 5 - melds.size
        val tiles = concealed.filter { !it.isFlower() }.sorted()
        if (tiles.size != neededSets * 3 + 2) return null
        val pairs = findPairStarts(tiles)
        for (pairTile in pairs) {
            val remaining = removeTwo(tiles, pairTile)
            val sets = findSets(remaining, neededSets)
            if (sets != null) {
                return HandDecomposition(pair = pairTile, concealedSets = sets, openMelds = melds)
            }
        }
        return null
    }

    private fun canForm(tiles: List<Tile>, setsNeeded: Int): Boolean {
        if (setsNeeded == 0) return tiles.size == 2 && tiles[0] == tiles[1]
        for (pairTile in findPairStarts(tiles)) {
            val remaining = removeTwo(tiles, pairTile)
            if (findSets(remaining, setsNeeded) != null) return true
        }
        return false
    }

    private fun findPairStarts(tiles: List<Tile>): List<Tile> {
        val result = mutableListOf<Tile>()
        var i = 0
        while (i < tiles.size - 1) {
            if (tiles[i] == tiles[i + 1]) {
                result += tiles[i]
                while (i < tiles.size - 1 && tiles[i] == tiles[i + 1]) i++
            }
            i++
        }
        return result.distinct()
    }

    private fun findSets(tiles: List<Tile>, needed: Int): List<List<Tile>>? {
        if (needed == 0) return if (tiles.isEmpty()) emptyList() else null
        if (tiles.isEmpty()) return null
        val first = tiles.first()
        // Try pung
        if (tiles.count { it == first } >= 3) {
            val after = removeN(tiles, first, 3)
            val rest = findSets(after, needed - 1)
            if (rest != null) return listOf(listOf(first, first, first)) + rest
        }
        // Try chow
        if (first is Tile.SuitTile && first.rank <= 7) {
            val t2 = first.copy(rank = first.rank + 1)
            val t3 = first.copy(rank = first.rank + 2)
            if (tiles.contains(t2) && tiles.contains(t3)) {
                var after = removeN(tiles, first, 1)
                after = removeN(after, t2, 1)
                after = removeN(after, t3, 1)
                val rest = findSets(after, needed - 1)
                if (rest != null) return listOf(listOf(first, t2, t3)) + rest
            }
        }
        return null
    }

    private fun removeTwo(tiles: List<Tile>, tile: Tile): List<Tile> = removeN(tiles, tile, 2)

    private fun removeN(tiles: List<Tile>, tile: Tile, n: Int): List<Tile> {
        val out = tiles.toMutableList()
        repeat(n) {
            val idx = out.indexOf(tile)
            require(idx >= 0)
            out.removeAt(idx)
        }
        return out
    }
}

data class HandDecomposition(
    val pair: Tile,
    val concealedSets: List<List<Tile>>,
    val openMelds: List<Meld>,
)
