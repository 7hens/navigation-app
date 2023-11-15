# Navigation App

一个导航 App Demo。

## 特点

1. 展示地图，标记当前位置
2. 选择导航终点，出现导航路径
3. 点击 Start Navigation 开始导航
4. 导航结束后，会显示导航路径（蓝色）和实际途经路径（绿色），以及导航过程所花费的时间和实际行驶的距离。

## 已实现功能

该项目尝试了多个方案：

| 方案 | 分支 | 完成情况 |
| --- | ---- | --- |
| GoogleMaps + Compose | google-maps | 1. |
| BaiduLBS + Compose | baidu-maps | 1.2. |
| BaiduLBS + XML | baidu-maps | 已完成 |

> 因为 GoogleMaps 提供了 Compose SDK，所以尝试了一下。
> 后来发现 GoogleMaps 导航 Api 需要付费，而且不能绑定境内的卡，只能放弃了。
> 然后尝试了一下 BaiduLBS + Compose 方案，因为 BaiduLBS 没有提供 Compose 的 SDK，GitHub 也没有发现可用的封装好的库，需要花时间自己封装，所以也放弃了。
> 最后决定不用 Compose，使用原来的开发方法来实现。

## 技术栈和依赖库

- 编程语言：Kotlin
- UI 框架：XML Layouts, Jetpack Compose
- 架构模式：MVVM + LiveData
- 依赖注入：Hilt
- 异步：Kotlin Coroutines
- 序列化：Kotlin Parcelize
- 地图：BaiduLBS, Google Maps

## 目录结构

```
navigation/
|-- app/                        |
|   |-- src/main/java/<pkg>/    |
|   |   |-- core.app/           | 通用代码
|   |   |-- map/                | 地图模块
|   |   |   |-- activity/       | - Activity
|   |   |   |-- compose/        | - Compose
|   |   |   |-- model/          | - Model
|   |   |   |-- service/        | - Service
|   |   |   |-- util/           | - 地图工具类
|   |   |   |-- vm/             | - ViewModel
|   |   |-- App.kt              |
|   |-- build.gradle.kts        |
|-- local.properties            | 配置私有变量
```

| 关键类 | 说明 |
| ---- | ---- |
| HomeActivity | 主页：地图展示 + 选取导航点 + 显示导航路径 |
| NaviGuidActivity | 导航向导页面 |
| TripSummaryActivity | 旅程概览页面 |
| LocationService | 持续定位服务 |
| RouteService | 计算导航路径服务 |

## 构建

该项目使用了 secrets-gradle-plugin，需要配置下本地的 local.properties 文件添加 Api Key 的配置才可运行。

针对 Google Maps 分支的 local.properties：

```properties
# local.properties
GOOGLE_MAPS_API_KEY=xxx
```

针对 BaiduLBS 分支的 local.properties：

```properties
# local.properties
BAIDU_MAPS_API_KEY=xxx
```