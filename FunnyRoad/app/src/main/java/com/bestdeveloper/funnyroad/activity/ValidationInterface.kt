package com.bestdeveloper.funnyroad.activity

interface ValidationInterface{
    fun emailOnEmptyResult()
    fun passwordOnEmptyResult()
    fun emailOnInvalidResult()
    fun passwordOnInvalidResult()
    fun OnSuccessResult()
    fun onBothEmpty()
}