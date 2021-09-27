package com.supertramp.performance.ext

open class Systrace {

    var systraceOn : Boolean = false // systrace是否打开
    var traceClass : String = ""
    var enterMethod : String = ""
    var enterMethodDes : String = ""
    var exitMethod : String = ""
    var exitMethodDes : String = ""

    fun isSystraceOn() : Boolean {
        return systraceOn && traceClass.isNotEmpty() &&
                enterMethod.isNotEmpty() && enterMethodDes.isNotEmpty() &&
                exitMethod.isNotEmpty() && exitMethodDes.isNotEmpty()
    }

}