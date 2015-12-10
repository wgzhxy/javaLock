atomic 常用工具类:

AtomicIntegerFieldUpdater 对象属性并发安全 
AtomicMarkableReference<Object,Boolean>   对象标记, boolean
AtomicStampedReference<Object,int>        对象标记, 整数

ABA问题的解决: AtomicMarkableReference/AtomicStampedReference 在解决“ABA问题”