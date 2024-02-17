# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists dwbi;

-- 切换库
use dwbi;

-- 图表信息表
create table if not exists chart
(
    id          bigint auto_increment comment 'id' primary key,
    goal        text                               null comment '分析目标',
    `name`      varchar(128)                       null comment '图表名称',
    chartData   text                               null comment '图表数据',
    chartType   varchar(128)                       null comment '图表类型',
    genChart    text                               null comment '生成的图表数据',
    genResult   text                               null comment '生成的分析结论',
    status      varchar(128)                       not null default 'await' comment 'await,running,succeed,failed',
    execMessage text                               null comment '执行信息',
    userId      bigint                             null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update current_timestamp comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色 user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 文本任务表
create table if not exists text_task
(
    id             bigint auto_increment comment '任务id' primary key,
    `name`         varchar(128)                       null comment '笔记名称',
    textType       varchar(128)                       null comment '文本类型',
    genTextContent text                               null comment '生成的文本内容',
    userId         bigint                             null comment '创建用户Id',
    `status`       varchar(128)                       not null default 'wait' comment 'wait,running,succeed,failed',
    execMessage    text                               null comment '执行信息',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除'
) comment '文本任务表' collate = utf8mb4_unicode_ci;

-- 文本记录表
create table if not exists text_record
(
    id             bigint auto_increment comment 'id' primary key,
    textTaskId     bigint comment '文本任务id',
    textContent    text                               null comment '文本内容',
    genTextContent text                               null comment '生成的文本内容',
    `status`       varchar(128)                       not null default 'wait' comment 'wait,running,succeed,failed',
    execMessage    text                               null comment '执行信息',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除'
) comment '文本记录表' collate = utf8mb4_unicode_ci;