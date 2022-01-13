# Android-Performance
### 实现原理
* 使用AGP Transform API拿到工程编译后的所有Class文件
* 使用ASM对Class字节码文件进行变换，插入trace点

### 使用方法
在根目录下的build.gradle中配置插件
implementation "com.supertramp.plugin:performance:1.0"

在app module下的build.gradle文件下：
apply plugin : 'performance'
systrace {
    systraceOn = true
    traceClass = "com/supertramp/performance/TraceKt"
    enterMethod = "traceBegin"
    enterMethodDes = "(Ljava/lang/String;)V"
    exitMethod = "traceEnd"
    exitMethodDes = "()V"
}
