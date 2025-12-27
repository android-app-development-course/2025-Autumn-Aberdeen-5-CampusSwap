# Campus-transaction (校园圈)

一个基于 Android Studio 开发的校园二手交易平台 APP，为校园用户提供便捷的闲置物品交易服务。

## 项目简介

每逢毕业季，许多毕业生都会选择将自己的闲置物品进行售卖。本项目旨在为校园用户提供一个集中的二手交易平台，让买卖双方能够更高效地完成交易，提高闲置物品的流转率。

## 功能特性

### 用户系统
- 用户注册与登录
- 个人信息管理（姓名、专业、电话、QQ、地址）
- 密码修改
- 退出登录

### 商品交易
- 商品发布（支持图片、标题、价格、描述、联系方式）
- 商品浏览（按时间排序展示）
- 商品分类筛选（体育用品、生活用品、电子产品、学习用品）
- 商品详情查看
- 商品状态管理（在售/已卖出）
- 我发布的商品管理
- 已卖出商品查看

### 社交互动
- 商品评论功能
- 实时聊天消息（买卖双方私信沟通）
- 聊天列表管理
- 未读消息提醒

### 其他功能
- 意见反馈（支持功能建议、BUG反馈等）
- 关于我们

## 技术栈

| 类别 | 技术 |
|------|------|
| 开发语言 | Java 17 |
| 开发工具 | Android Studio |
| 构建工具 | Gradle 8.7.3 |
| 目标 SDK | Android 14 (API 34) |
| 最低 SDK | Android 5.0 (API 21) |
| 数据库 | SQLite |
| UI 组件 | Material Design Components |

### 主要依赖

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.core:core:1.12.0'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.cardview:cardview:1.0.0'
}
```

## 项目结构

```
Campus-transaction/
├── app/
│   ├── src/main/
│   │   ├── java/page/page1/           # Java 源码
│   │   │   ├── LoginMainActivity.java      # 登录页面
│   │   │   ├── RegisterMainActivity.java   # 注册页面
│   │   │   ├── main_page.java              # 主页（商品列表）
│   │   │   ├── item_info.java              # 商品详情
│   │   │   ├── AddItem.java                # 发布商品
│   │   │   ├── MyItems.java                # 我的发布
│   │   │   ├── MySoldItems.java            # 已卖出商品
│   │   │   ├── kind_page1~4.java           # 分类页面
│   │   │   ├── ChatActivity.java           # 聊天页面
│   │   │   ├── ChatListActivity.java       # 聊天列表
│   │   │   ├── MyselfActivity.java         # 个人中心
│   │   │   ├── FeedbackActivity.java       # 意见反馈
│   │   │   ├── DatabaseHelper.java         # 数据库管理
│   │   │   └── ...
│   │   ├── res/
│   │   │   ├── layout/                # 布局文件
│   │   │   ├── drawable/              # 图标和背景资源
│   │   │   ├── values/                # 字符串、颜色、样式
│   │   │   └── mipmap-*/              # 应用图标
│   │   └── AndroidManifest.xml        # 应用清单
│   └── build.gradle                   # 模块级配置
├── gradle/                            # Gradle Wrapper
├── build.gradle                       # 项目级配置
└── settings.gradle                    # Gradle 设置
```

## 数据库设计

项目使用 SQLite 作为本地数据库，当前数据库版本为 **4**。

### 数据表结构

#### 1. 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | VARCHAR(20) | 用户账号（主键） |
| passWord | VARCHAR(20) | 密码 |
| name | VARCHAR(20) | 姓名 |
| subject | VARCHAR(20) | 专业 |
| phone | VARCHAR(15) | 电话 |
| qq | VARCHAR(15) | QQ号 |
| address | VARCHAR(50) | 地址 |

#### 2. 商品信息表 (iteminfo)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 商品编号（主键，自增） |
| userId | VARCHAR(100) | 发布者账号 |
| title | VARCHAR(200) | 商品标题 |
| kind | VARCHAR(100) | 商品类别 |
| info | VARCHAR(1000) | 商品描述 |
| price | VARCHAR(100) | 商品价格 |
| image | BLOB | 商品图片 |
| time | DATETIME | 发布时间 |
| contact | VARCHAR(50) | 联系方式 |
| status | INTEGER | 状态（0在售，1已卖出） |

#### 3. 评论表 (comments)

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | VARCHAR(100) | 评论者账号 |
| itemId | INTEGER | 商品编号 |
| comment | VARCHAR(1000) | 评论内容 |
| time | DATETIME | 评论时间 |

#### 4. 消息表 (messages)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 消息ID（主键，自增） |
| senderId | VARCHAR(100) | 发送者ID |
| receiverId | VARCHAR(100) | 接收者ID |
| content | VARCHAR(2000) | 消息内容 |
| time | DATETIME | 发送时间 |
| isRead | INTEGER | 是否已读（0未读，1已读） |

#### 5. 会话表 (conversations)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 会话ID（主键，自增） |
| userId1 | VARCHAR(100) | 用户1 |
| userId2 | VARCHAR(100) | 用户2 |
| lastMessage | VARCHAR(500) | 最后一条消息 |
| lastTime | DATETIME | 最后消息时间 |
| unreadCount | INTEGER | 未读消息数 |

#### 6. 意见反馈表 (feedback)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 反馈ID（主键，自增） |
| userId | VARCHAR(100) | 用户ID |
| type | VARCHAR(50) | 反馈类型 |
| content | VARCHAR(2000) | 反馈内容 |
| contact | VARCHAR(100) | 联系方式 |
| time | DATETIME | 提交时间 |
| status | INTEGER | 处理状态（0待处理，1已处理） |

## 安装运行

### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.7.3

### 运行步骤

1. 克隆项目到本地
```bash
git clone https://github.com/your-username/Campus-transaction.git
```

2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器

5. 点击 Run 运行项目

## 应用架构

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  (Activities + XML Layouts + Adapters)                  │
├─────────────────────────────────────────────────────────┤
│                    Business Logic                       │
│  (Activity 内部处理业务逻辑)                              │
├─────────────────────────────────────────────────────────┤
│                    Data Layer                           │
│  (DatabaseHelper + SQLite)                              │
└─────────────────────────────────────────────────────────┘
```

## 核心功能实现

### 用户状态管理
使用静态变量 `LoginMainActivity.post_userid` 存储当前登录用户，实现简单的会话管理。

### 图片存储
商品图片以 BLOB 格式直接存储在 SQLite 数据库中，简化了文件管理。

### 实时聊天
通过定时器（3秒间隔）轮询数据库实现消息的"实时"更新。

### 页面导航
使用 Intent 进行页面跳转，通过 `putExtra()` 传递必要的数据参数。

## 商品分类

| 分类 | 说明 |
|------|------|
| 体育用品 | 运动器材、健身设备等 |
| 生活用品 | 日常生活用品、家居物品等 |
| 电子产品 | 手机、电脑、数码设备等 |
| 学习用品 | 书籍、文具、学习资料等 |

## 未来计划

- [ ] 引入后端服务器，实现真正的实时通讯
- [ ] 添加商品搜索功能
- [ ] 支持多图片上传
- [ ] 添加收藏功能
- [ ] 引入用户评价系统
- [ ] 添加消息推送通知
- [ ] 支持微信/QQ 第三方登录

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证

本项目仅供学习交流使用。

---

**校园圈** - 让闲置流转，让交易更简单
