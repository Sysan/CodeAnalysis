# CodeAnalysis
## 简介
该项目是TSS2.0项目的一部分，用于对学生提交的考试代码进行分析并生成报告。

## 导入到IDE
先将[Lombok](https://projectlombok.org/)安装到你的IDE，然后下载该项目并作为Maven项目导入即可。

## 使用的工具
* **Spring Boot**：整合其他框架/类库，如SpringMVC等
* **Spring Data JPA**：ORM框架，用于操作数据库
* **Thymeleaf**：Java模版引擎，用于将数据渲染到html页面
* **Lombok**：使用注解简化Java代码（如简化getter/setter）
* **JGit**：Eclipse插件之一，用于Git相关操作
* **JDT**：Eclipse插件之一，用于将代码转化为AST（抽象语法树）并进行分析
* **Maven Invoker**：在代码中执行Maven命令

## 处理流程
 该项目将考试项目下载到本地进行分析，并生成两种分析报告。
### Score Report([查看示例(界面不忍直视，请轻喷:sweat_smile:)](http://115.29.184.56:15000/scoreReport?gitUrl=https://github.com/SuZiquan/MatrixExam.git)):
1. 对最后一个版本的代码进行度量
2. 对最后一个版本执行测试，得到分数  

### Analysis Report([查看示例(界面不忍直视，请轻喷:sweat_smile:)](http://115.29.184.56:15000/analysisReport?gitUrl=https://github.com/SuZiquan/MatrixExam.git)):
1. 对每一个版本的代码进行度量，得到度量值的变化
2. 对每一个版本执行测试，得到测试的过程数据

## 通信接口：
### RabbitMQ版（[接口文档](http://115.29.184.56:10000/html/TssInterface/%E5%88%86%E6%9E%90%E7%B3%BB%E7%BB%9F%E6%8E%A5%E5%8F%A3.html)）：
使用RabbitMQ进行通信。  

**RabbitMQ控制台地址**：
http://115.29.184.56:15672/  

**RabbitMQ配置**：  
```java  
spring.rabbitmq.host = 115.29.184.56
spring.rabbitmq.port = 5672
spring.rabbitmq.username = tssteam
spring.rabbitmq.password = tssteam
``` 
### Http版（[接口文档](http://115.29.184.56:15000/swagger-ui.html)）：
通过Http请求进行通信，现在已不再使用。

