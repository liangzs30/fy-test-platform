# fy-test-platform

#### 介绍
使用过大厂的自动化测试平台，也使用过开源的测试平台，大厂的不对外使用，开源的有优秀的，也有体验不好的。个人觉得robotframework其实很优秀，但是没有平台化，脚本维护也不方便，尤其是不懂python编程，使用起来也比较难上手。开发这个自动化平台，差不多是基于robotframework的思想。不过平台可以多人协作，也可以分布式执行，可以降低维护成本，提高执行效率。


#### 软件架构
平台后端使用springboot 2.6.4，前端使用vue2 + elementui。后端框架直接使用的el-admin，eladmin非常优秀，可以快速构建自己的应用，本身具备权限管理、自动运维功能。感谢作者提供这个优秀的平台，项目地址：https://gitee.com/elunez/eladmin

执行机agent使用python3开发，接口自动化使用的是requests库，web自动化使用selenium4.6.1。后续会开发支持app自动化的关键字，app自动化计划是使用uiautomator2和facebook-wda库。

#### 安装教程

https://docs.qq.com/doc/DU1pYUmh3R05KSXZB?no_promotion=1

#### 使用说明

https://docs.qq.com/doc/DU1pYUmh3R05KSXZB?no_promotion=1

#### 体验地址
http://121.40.251.93:16081
账号:tester
密码：Fy@2025

欢迎加群一起交流自动化测试
1018407214
![输入图片说明](image.png)


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
