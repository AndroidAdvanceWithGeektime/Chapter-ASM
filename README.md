# Chapter-ASM
先在编译 asm-gradle-plugin 模块中的 buildAndPublishToLocalMaven

然后可以在ASMSample尝试效果，编译修改后的class文件在ASMSample/build/ASMTraceTransform/classes中

ASM的核心代码在ASMCode中，我们也可以尝试在里面增加一些其他的功能

例如
1. 给某个方法增加 try catch
2. 查看代码中谁获取了IMEI权限

对于ASM，大家在工具的帮助之余，需要多实践，需要看得懂"ASM Bytecode Outline"生成的代码，也知道ASM源码中有哪些类可以帮助我们。

