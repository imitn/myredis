# Hello Redis，myredis是一个spring集成redis的例子，让初学者迅速上手在java项目中使用redis

## 什么是Redis？
Redis是一个开源，高级的键值存储和一个适用的解决方案，用于构建高性能，可扩展的Web应用程序。

## Redis的特点
 Redis有三个主要特点，使它优越于其它键值数据存储系统。
* Redis将其数据库完全保存在内存中，仅使用磁盘进行持久化。
* 与其它键值数据存储相比，Redis有一组相对丰富的数据类型。
* Redis可以将数据复制到任意数量的从机中。
## Redis的优点
以下是Redis的一些优点。
* 异常快 - Redis非常快，每秒可执行大约110000次的设置(SET)操作，每秒大约可执行81000次的读取/获取(GET)操作。
* 支持丰富的数据类型 。Redis支持开发人员常用的大多数数据类型，例如列表，集合，排序集和散列等等。这使得Redis很容易被用来解决各种问题，因为我们知道哪些问题可以更好使用地哪些数据类型来处理解决。
* 操作具有原子性 - 所有Redis操作都是原子操作，这确保如果两个客户端并发访问，Redis服务器能接收更新的值。
* 多实用工具。Redis是一个多实用工具，可用于多种用例，如：缓存，消息队列(Redis本地支持发布/订阅)，应用程序中的任何短期数据，例如，web应用程序中的会话，网页命中计数等。
## 安装Redis
由于后面我们要使用redis的sentinel模块，而且该模块从2.8发布版本后才开始稳定，所以我们使用最新的版本。  

1. 新建目录存放redis安装文件。从[Redis官方网站](http://www.redis.io/)下载redis-3.2.8.tar.gz，并将其拷贝到/tmp/redis-test/目录下。
```
> mkdir /tmp/redis-test/
> cd /tmp/redis-test/
```
2. 安装redis到/usr/local/redis/。
```
> tar xzvf redis-3.2.8.tar.gz
> cd redis-3.2.8
> make PREFIX=/usr/local/redis/ install
```
3. 在安装目录下新建conf目录，并从redis的源代码目录下拷贝配置文件到conf下。
```
> cd /usr/local/redis/
> mkdir conf
> cp /tmp/redis-test/redis-3.2.8/redis.conf ./conf/
```
4. 启动redis
```
> ./bin/redis-server conf/redis.conf
```
## Redis高可用
Redis-Sentinel是Redis官方推荐的高可用性(HA)解决方案。Redis 的 Sentinel 系统用于管理多个 Redis 服务器（instance），该系统执行以下三个任务：
* 监控（Monitoring）： Sentinel 会不断地检查你的主服务器和从服务器是否运作正常。
* 提醒（Notification）： 当被监控的某个 Redis 服务器出现问题时， Sentinel 可以通过 API 向管理员或者其他应用程序发送通知。
* 自动故障迁移（Automatic failover）： 当一个主服务器不能正常工作时， Sentinel 会开始一次自动故障迁移操作， 它会将失效主服务器的其中一个从服务器升级为新的主服务器， 并让失效主服务器的其他从服务器改为复制新的主服务器； 当客户端试图连接失效的主服务器时， 集群也会向客户端返回新主服务器的地址， 使得集群可以使用新主服务器代替失效服务器。

当用Redis做master-slave的高可用方案时，假如master宕机了，Redis本身并没有实现自动进行主备切换的功能。而sentinel是一个独立运行的进程，它能监控多个master-slave集群，发现master宕机后能进行自动切换。很显然，只使用单个sentinel进程来监控redis集群是不可靠的，当sentinel进程宕掉后（sentinel本身也有单点问题，single-point-of-failure）整个集群系统将无法按照预期的方式运行。如果只有一个sentinel进程，如果这个进程运行出错，或者是网络堵塞，那么将无法实现redis集群的主备切换（单点问题）。所以有必要将sentinel集群化，这样有几个好处。
* 只有在多个sentinel同意master失效才进行失效备援，降低假阳性的概率。
* 即使有一些sentinel进程宕掉了，依然可以进行redis集群的主备切换。
* redis的客户端可以随意地连接任意一个sentinel来获得redis集群信息。
### 主从配置
我们使用3台主机构建redis集群（虽然这算不上真正的集群，我们姑且这么叫吧）。
```
master：192.168.1.1
slave1：192.168.1.2
slave2：192.168.1.3
```
1. 在3台主机上分别安装1个Reids实例。
2. 修改master的redis.conf需要修改的配置
```
> cd /usr/local/redis/
> vi conf/redis.conf
```
redis.conf
```
# bind 127.0.0.1
protected-mode no
requirepass sfdp
masterauth sfdp
min-slaves-to-write 1
min-slaves-max-lag 10
```
3. slave1和slave2的redis.conf需要修改的配置
```
# bind 127.0.0.1
protected-mode no
requirepass sfdp
slaveof 192.168.1.1 6379
masterauth sfdp
min-slaves-to-write 1
min-slaves-max-lag 10
```
### 主从切换
4. 在192.168.1.1上，将redis源码路径下的sentinel.conf文件拷贝到mster的conf目录下。
```
> cp /tmp/redis-test/redis-3.2.8/sentinel.conf conf/
```
5. 修改sentinel.conf。
```
> vi conf/sentinel.conf
```
sentinel.conf
```
sentinel monitor sfdpmaster 192.168.1.1 6379 2
sentinel auth-pass sfdpmaster sfdp
sentinel down-after-milliseconds sfdpmaster 60000
sentinel failover-timeout sfdpmaster 180000
sentinel parallel-syncs sfdpmaster 1
protected-mode no
```
6. 将sentinel.conf拷贝到slave的conf目录下。
```
> scp conf/sentinel.conf 192.168.1.2:/usr/local/redis/conf/sentinel.conf
> scp conf/sentinel.conf 192.168.1.3:/usr/local/redis/conf/sentinel.conf
```
7. 启动master。
```
> ./bin/redis-server conf/redis.conf
```
8. 启动slave1和slave2。
```
> ./bin/redis-server conf/redis.conf
```
9. 启动master和slave的sentinel
```
> ./bin/redis-sentinel conf/sentinel.conf
```
## 启动myredis
通过以上步骤，我们的redis高可用集群已经搭建并启动完毕。接着就可以在eclipse导入myredis项目（maven项目）了。运行RedisServiceTest测试用例，就可以看到Java项目中对redis进行操作的结果了。
