- # Java编程常见问题（中级篇）

  #### Q1: 线程池的核心参数有哪些？如何合理配置线程池大小？

  - 核心参数：`corePoolSize`（核心线程数）、`maximumPoolSize`（最大线程数）、`keepAliveTime`（空闲线程存活时间）、`workQueue`（任务队列）、`threadFactory`（线程工厂）、`handler`（拒绝策略）。
  - 配置建议：
    - **CPU密集型**：线程数 = CPU核心数 + 1。
    - **IO密集型**：线程数 = CPU核心数 * (1 + IO等待时间 / CPU计算时间)，通常可设置较大（如2倍核心数）。
    - 需结合任务类型、系统资源动态调整，避免过度创建线程导致上下文切换开销。

  #### Q2: volatile关键字的作用是什么？和synchronized有什么区别？

  - **volatile** 保证变量的**可见性**（修改立即刷新到主存）和**有序性**（禁止指令重排），但不保证原子性。
  - **synchronized** 保证原子性、可见性和有序性，通过加锁实现，但开销较大。
  - 区别：
    - `volatile` 只能修饰变量，`synchronized` 可修饰方法或代码块。
    - `volatile` 不会阻塞线程，`synchronized` 会。
    - 适用场景：`volatile` 用于状态标记、单例双重检查；`synchronized` 用于多线程竞争资源。

  #### Q3: ConcurrentHashMap如何实现线程安全？与Hashtable有何区别？

  - **ConcurrentHashMap** 采用分段锁（Java7）或CAS + synchronized（Java8及以后）实现高效并发。Java8中，数组节点用`synchronized`锁住链表/红黑树头节点，读操作无锁，写操作细粒度加锁。
  - **Hashtable** 使用全局锁（`synchronized`修饰方法），并发效率低。
  - 区别：
    - `ConcurrentHashMap` 支持更高的并发度，不允许`null`键值。
    - `Hashtable` 已过时，不建议使用。

  #### Q4: Java类加载机制是怎样的？什么是双亲委派模型？

  - 类加载分为**加载、连接（验证、准备、解析）、初始化**三个阶段。
  - **双亲委派模型**：当一个类加载器收到类加载请求时，会先委托给父类加载器去加载，只有当父类无法加载时才自己尝试。
  - 优点：避免类重复加载，保证核心类库的安全（如`java.lang.Object`始终由引导类加载器加载）。

  #### Q5: try-with-resources语句的原理是什么？如何自定义一个支持try-with-resources的资源类？

  - **try-with-resources** 是Java7引入的自动资源管理语法，用于自动关闭实现了`AutoCloseable`接口的资源（如流、连接）。
  - 原理：编译时会将资源声明转换为`try-catch-finally`，在`finally`中自动调用资源的`close()`方法（按声明逆序）。
  - 自定义资源类需实现`AutoCloseable`或`Closeable`接口，并重写`close()`方法。

