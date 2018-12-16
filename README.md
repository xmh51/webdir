# WEB DIR

#### 项目介绍
多账户 文件管理系统 配合aria2 可以离线下载
新建文件夹 重命名文件 等文件管理的基本功能
aria2 无需前端配置，不需要使用方配置繁琐的aria2配置


#### 软件架构
软件架构说明 Java


#### 安装教程

1. 安装tomcat环境，把war包部署到ROOT目录
2. 自定义配置文件(WEB-INF/classes/config/config.yml) 配置下载目录和aria2配置
3. 启动tomcat
4. 查看tomcat日志是否正常

#### 使用说明

1. 离线下载需要 aria2 
2. 中文乱码解决。设置linux 系统字符集为utf-8 设置完成后需要重启
3. 运行 aria2 需要和tomcat 同系统账户运行，防止无法删除文件  
   指定 用户运行  
   debian  
   -u www  -g group "aria2c --conf-path=/data/aria2/conf/aria2.conf -D"  
   centos    
   su - www -c "aria2c --conf-path=/data/aria2/conf/aria2.conf -D"  
4. 支持多个用户,在配置文件添加用户，重启即可
#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

#### 感谢

特别感谢 PotatoCloudDrive webui-aria2  本项目中使用到了这些项目的源代码
