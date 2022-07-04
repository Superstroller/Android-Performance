package com.supertramp.performance.ext

open class Systrace {

    var systraceOn : Boolean = false // systrace是否打开
    var online : Boolean = false //是否线上环境
    var output : String = ""
    var traceClass : String = "com/supertramp/performance/TraceKt"
    var enterMethod : String = "traceBegin"
    var enterMethodDes : String = "(Ljava/lang/String;)V"
    var exitMethod : String = "traceEnd"
    var exitMethodDes : String = "()V"
    var catchMethod : String = "catchIn"
    var catchMethodDes : String = "(Ljava/lang/String;)V"

    fun isSystraceOn() : Boolean {
        return systraceOn && traceClass.isNotEmpty() &&
                enterMethod.isNotEmpty() && enterMethodDes.isNotEmpty() &&
                exitMethod.isNotEmpty() && exitMethodDes.isNotEmpty()
    }

}