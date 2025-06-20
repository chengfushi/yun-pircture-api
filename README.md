# 智能协同云云图库平台后端

> 作者:[Chengfu Shi](https://github.com/chengfushi)
> 
> 基于 Vue 3 + Spring Boot + COS + WebSocket 的企业级智能协同云图库平台。项目应用场景广泛，可作为表情包网站、设计素材网站、壁纸网站、个人云盘、企业活动相册等。用户可以在平台公开上传和检索图片素材；管理员可以上传、审核和管理分析图片；个人用户可将图片上传至私有空间进行批量管理、检索、编辑和分析；企业可开通团队空间并邀请成员，共享图片并实时协同编辑图片。技术栈包括 MySQL 分库分表、Redis + Caffeine 多级缓存、COS 对象存储、Sa-Token 权限控制、DDD 领域驱动设计、WebSocket 实时通讯、JUC、Disruptor、AI 绘图大模型、设计模式等

## 项目结构
```
yun-picture-api/
├── src/  # 核心源码与资源目录，承载项目业务逻辑、配置等
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── chengfu/
│   │   │           └── yunpictureapi/
│   │   │               ├── common/  # 通用基础模块：存放全局复用 DTO、工具类
│   │   │               │   ├── BaseResponse.java   # 统一接口返回格式（code、data、msg 封装）
│   │   │               │   ├── DeleteRequest.java  # 删除操作请求参数封装（如 ID 传递）
│   │   │               │   ├── PageRequest.java    # 分页查询参数封装（页码、条数等）
│   │   │               │   └── ResultUtils.java    # 快捷构建 BaseResponse 工具（简化响应返回）
│   │   │               ├── constant/  # 常量定义中心：枚举、静态常量集中管理
│   │   │               │   └── UserConstant.java    # 用户相关常量（权限标识、状态码等）
│   │   │               ├── controller/  # 控制层：暴露 HTTP 接口，对接前端请求
│   │   │               │   └── （待补充具体 Controller，如 PictureController）
│   │   │               ├── exception/  # 异常处理体系：自定义异常 + 全局拦截
│   │   │               │   ├── BusinessException.java   # 业务异常（参数校验失败等场景）
│   │   │               │   ├── ErrorCode.java           # 错误码枚举（规范错误编码）
│   │   │               │   ├── GlobalExceptionHandler.java # 全局异常拦截，统一响应格式
│   │   │               │   └── ThrowUtils.java          # 快捷抛异常工具（简化业务校验逻辑）
│   │   │               ├── mapper/  # 数据访问层（MyBatis 场景）：Mapper 接口 + XML 存放
│   │   │               │   └── （待补充具体 Mapper，如 PictureMapper）
│   │   │               ├── service/  # 业务逻辑层：封装核心业务流程
│   │   │               │   ├── （待补充具体 Service，如 PictureService）
│   │   │               │   └── impl/  # （可选）Service 实现类专属目录
│   │   │               ├── utils/  # 通用工具类：与业务解耦的工具方法集合
│   │   │               │   ├── ColorSimilarUtils.java   # 颜色相似度计算工具
│   │   │               │   └── ColorTransformUtils.java  # 颜色格式转换工具
│   │   │               └── YunPictureApiApplication.java # Spring Boot 启动类（项目入口）
│   │   └── resources/  # 资源配置目录
│   │       ├── static/  # 静态资源存放（前端页面/CSS/JS 等，前后端分离场景可空）
│   │       └── application.yml  # Spring Boot 配置文件（数据库、端口、日志等配置）
│   └── test/  # 测试代码目录：单元测试、集成测试用例（如 JUnit 用例）
│       └── （待补充具体测试类，如 PictureServiceTest）
├── .gitignore  # Git 忽略规则配置：定义无需版本控制的文件/目录
├── pom.xml  # Maven 项目描述：依赖、插件、打包等配置定义
└── README.md  # 项目说明文档：功能介绍、部署步骤、接口文档等指引 
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
