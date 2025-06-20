# 智能协同云云图库平台后端

> 作者:[Chengfu Shi](https://github.com/chengfushi)
> 
> 基于 Vue 3 + Spring Boot + COS + WebSocket 的企业级智能协同云图库平台。项目应用场景广泛，可作为表情包网站、设计素材网站、壁纸网站、个人云盘、企业活动相册等。用户可以在平台公开上传和检索图片素材；管理员可以上传、审核和管理分析图片；个人用户可将图片上传至私有空间进行批量管理、检索、编辑和分析；企业可开通团队空间并邀请成员，共享图片并实时协同编辑图片。技术栈包括 MySQL 分库分表、Redis + Caffeine 多级缓存、COS 对象存储、Sa-Token 权限控制、DDD 领域驱动设计、WebSocket 实时通讯、JUC、Disruptor、AI 绘图大模型、设计模式等

## 项目结构
```
yun-picture-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── chengfu/
│   │   │           └── yunpictureapi/
│   │   │               ├── common/  # 通用基础模块，存放全局复用的 DTO、工具类
│   │   │               │   ├── BaseResponse.java   # 统一接口返回格式（如 code、data、msg）
│   │   │               │   ├── DeleteRequest.java  # 封装删除操作的请求参数（如 ID）
│   │   │               │   ├── PageRequest.java    # 封装分页查询的请求参数（页码、条数等）
│   │   │               │   └── ResultUtils.java    # 快速构建 BaseResponse 的工具类（成功/失败响应）
│   │   │               ├── constant/  # 常量定义，集中管理枚举、静态常量
│   │   │               │   └── UserConstant.java    # 用户相关常量（如权限标识、状态码）
│   │   │               ├── controller/  # 控制层，暴露 HTTP 接口，处理前端请求
│   │   │               │   └── （待补充具体 Controller 类，如 PictureController）
│   │   │               ├── exception/  # 异常处理模块，含自定义异常、全局处理器
│   │   │               │   ├── BusinessException.java   # 业务异常（如参数校验失败）
│   │   │               │   ├── ErrorCode.java           # 错误码枚举（规范错误编码）
│   │   │               │   ├── GlobalExceptionHandler.java # 全局异常拦截，统一返回格式
│   │   │               │   └── ThrowUtils.java          # 快捷抛异常工具（简化业务校验）
│   │   │               ├── mapper/  # 数据访问层（MyBatis 场景），定义 Mapper 接口 + XML
│   │   │               │   └── （待补充具体 Mapper 类，如 PictureMapper）
│   │   │               ├── service/  # 业务逻辑层，封装核心业务流程
│   │   │               │   ├── （待补充具体 Service 类，如 PictureService）
│   │   │               │   └── impl/  # （可选）Service 实现类目录
│   │   │               ├── utils/  # 通用工具类，与业务无关的工具方法
│   │   │               │   ├── ColorSimilarUtils.java   # 颜色相似度计算工具
│   │   │               │   └── ColorTransformUtils.java  # 颜色格式转换工具
│   │   │               └── YunPictureApiApplication.java # Spring Boot 启动类（项目入口）
│   │   └── resources/  # 资源文件目录
│   │       ├── static/  # 静态资源（前端页面、CSS、JS 等，若前后端分离可空）
│   │       └── application.yml  # Spring Boot 配置文件（数据库、端口、日志等配置）
│   └── test/  # 测试目录，存放单元测试、集成测试代码（如 JUnit 用例）
│       └── （待补充具体测试类，如 PictureServiceTest）
├── target/  # Maven 编译输出目录（class 文件、打包产物等，一般不提交 Git）
├── .gitignore  # Git 忽略规则（指定不上传的文件，如 target、IDE 缓存）
├── pom.xml  # Maven 项目描述文件（定义依赖、插件、打包配置）
└── README.md  # 项目说明文档（功能介绍、部署步骤、接口文档等）
```

## 技术选型
- Java Spring Boot 框架
- MySQL 数据库 + MyBatis - Plus 框架 + MyBatis X
- Redis 分布式缓存 + Caffeine 本地缓存
- Jsoup 数据抓取
- ⭐️ COS 对象存储
- ⭐️ ShardingSphere 分库分表
- ⭐️ Sa - Token 权限控制
- ⭐️ DDD 领域驱动设计
- ⭐️ WebSocket 双向通信
- ⭐️ Disruptor 高性能无锁队列
- ⭐️ JUC 并发和异步编程
- ⭐️ AI 绘图大模型接入
- ⭐️ 多种设计模式的运用
- ⭐️ 多角度项目优化：性能、成本、安全性等  
