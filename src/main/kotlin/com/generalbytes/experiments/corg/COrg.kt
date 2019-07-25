package com.generalbytes.experiments.corg

import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.RoundingMode

val TWO = BigDecimal("2")

fun sqrt(value: BigDecimal) : BigDecimal {
    val x = BigDecimal(Math.sqrt(value.toDouble()))
    return x.add(BigDecimal(value.subtract(x.multiply(x)).toDouble() / (x.toDouble() * 2.0))).setScale(8, RoundingMode.DOWN)
}


class COrg(val name:String, val slope: BigDecimal, val investmentToReserveRatio:BigDecimal, val revenueToReserveRatio:BigDecimal, val numberOfInitialTokens:BigDecimal) {
    private val tokenOwnership = mutableMapOf<String, BigDecimal>()
    var totalNumberOfTokens = BigDecimal.ZERO
    var reserve = BigDecimal.ZERO
    var capital = BigDecimal.ZERO

    private fun mintNewTokens(owner:String, numberOfTokens:BigDecimal) {
        totalNumberOfTokens+=numberOfTokens
        tokenOwnership[owner] = (tokenOwnership[owner] ?: BigDecimal.ZERO) + numberOfTokens
    }

    fun selfInvest(amountOfCapital:BigDecimal) {
        if (capital < amountOfCapital) {
            throw IllegalArgumentException()
        }
        capital-=amountOfCapital
        buyTokens(name, amountOfCapital, BigDecimal.ONE)

    }

    fun buyTokens(investor:String, investment:BigDecimal, investmentToReserveRatio:BigDecimal = this.investmentToReserveRatio):BigDecimal {
        val toReserve = investment * investmentToReserveRatio
        val toCapital = investment - toReserve
        capital+= toCapital
        reserve+= toReserve

        val numberOfNewTokens = sqrt((TWO * (capital + reserve)).divide(slope,8, RoundingMode.DOWN)) - totalNumberOfTokens
        mintNewTokens(investor, numberOfNewTokens)
        return numberOfNewTokens
    }

    fun revenue(amount:BigDecimal) {
        val toReserve = amount * revenueToReserveRatio
        val toCapital = amount - toReserve

        capital+= toCapital
        reserve+= toReserve
    }

    fun transferTokens(sender: String, recipient: String, numberOfTokens:BigDecimal) {
        val senderBalance = tokenOwnership[sender] ?: BigDecimal.ZERO

        if (senderBalance < numberOfTokens) {
            //Not enough balance
            throw IllegalArgumentException()
        }

        tokenOwnership[sender] = senderBalance - numberOfTokens
        tokenOwnership[recipient] = (tokenOwnership[recipient] ?: BigDecimal.ZERO) + numberOfTokens
    }

    fun sellTokens(seller:String, numberOfTokensToBurn: BigDecimal):BigDecimal {
        val sellerBalance = tokenOwnership[seller] ?: BigDecimal.ZERO

        if (sellerBalance < numberOfTokensToBurn) {
            //Not enough balance
            throw IllegalArgumentException()
        }

        tokenOwnership[seller] = sellerBalance - numberOfTokensToBurn

        val currentArea = ((totalNumberOfTokens * totalNumberOfTokens) * slope).divide(TWO,8,RoundingMode.DOWN)
        val newArea = (((totalNumberOfTokens - numberOfTokensToBurn) * (totalNumberOfTokens - numberOfTokensToBurn)) * slope).divide(TWO,8,RoundingMode.DOWN)

        totalNumberOfTokens-=numberOfTokensToBurn
        val areaChangeRatio = BigDecimal.ONE - newArea.divide(currentArea, 8, RoundingMode.DOWN)
        val withdraw = reserve * areaChangeRatio
        reserve-=withdraw
        return withdraw
    }

    fun burnTokens(owner: String, numberOfTokensToBurn: BigDecimal) {
        val sellerBalance = tokenOwnership[owner] ?: BigDecimal.ZERO

        if (sellerBalance < numberOfTokensToBurn) {
            //Not enough balance
            throw IllegalArgumentException()
        }

        tokenOwnership[owner] = sellerBalance - numberOfTokensToBurn
        totalNumberOfTokens-=numberOfTokensToBurn
    }


    override fun toString(): String {
        return "COrg(name='$name', tokenOwnership=$tokenOwnership, totalNumberOfTokens=$totalNumberOfTokens, reserve=$reserve, capital=$capital)"
    }


}

fun main() {
    //In this experiment I have chosen the simplest bonding curve. Slope i 1.0 and function is linear
    val c = COrg("PG",
        BigDecimal("1"),
        BigDecimal("0.1"),
        BigDecimal("0.2"),
        BigDecimal("1000000")
        )

    c.buyTokens("karel", BigDecimal("100"))
    c.buyTokens("martin", BigDecimal("100"))
    c.transferTokens("karel","graphic", BigDecimal("4.14213562"))
    c.revenue(BigDecimal("100"))
    c.selfInvest(BigDecimal("100"))
    c.buyTokens("martin", BigDecimal("100"))
    var withdraw = c.sellTokens("graphic", BigDecimal("2.14213562"))
    println("withdraw graphics = ${withdraw}")
    c.burnTokens(c.name, BigDecimal("2"))
    withdraw = c.sellTokens("karel", BigDecimal("10"))
    println("withdraw karel = ${withdraw}")

    println(c)
}