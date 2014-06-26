AsmEventBus
===========

*AsmEventBus* is an implementation of **event system** with asm. It is faster and more powerful than the *EventBus* of Google *Guava*.

What is ASM
-----------
*ASM* is a java library, which provides real-time bytecode modifying and generating. Its name is referred to *inline assembler*, <del>although I consider it Aya Shameimaru's Miniskirt...</del>

*AsmEventBus* is powered by *ASM*.

Why ASM
-----------
Well... We know that *Guava EventBus* uses "reflect" to invoke event listeners, right? And we also know reflect is slow. *AsmEventBus* will dynamically generate invoker class, to invoke event listeners by method calling, which is faster than reflect.
