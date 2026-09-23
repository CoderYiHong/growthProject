# YiHong. — 个人成长作品集管理系统

## 项目结构

```
growth/
├── frontend/          # Vue 3 前端（用 WebStorm 打开这个文件夹）
│   ├── src/
│   │   ├── views/     # 页面（前台 + 后台）
│   │   ├── components/# 组件
│   │   ├── api/       # 后端接口调用
│   │   ├── stores/    # Pinia 状态管理
│   │   ├── router/    # 路由配置
│   │   └── data/      # 学习计划等本地模块数据（文章/项目不再使用）
│   ├── package.json
│   └── vite.config.js
│
├── backend/           # Spring Boot 后端（用 IDEA 打开这个文件夹）
│   ├── src/main/java/com/yihong/growth/
│   │   ├── controller/   # 接口层
│   │   ├── service/      # 业务层
│   │   ├── mapper/       # 数据库层
│   │   └── entity/       # 实体类
│   ├── src/main/resources/
│   │   ├── application.yml  # 配置
│   │   ├── schema.sql       # 建表
│   │   └── data.sql         # 初始数据
│   └── pom.xml
│
└── README.md
```

## 技术栈

| 前端 | 后端 |
|------|------|
| Vue 3 + Vite | Spring Boot 3.2 |
| Element Plus | MyBatis-Plus 3.5 |
| ECharts | MySQL / H2 |
| Pinia | Maven |
| Vue Router | Java 17 |

## 快速启动

### 1. 启动后端（终端 1）

用 IDEA 打开 `backend/` 文件夹，运行 `GrowthApplication.main()`。

或者命令行：
```bash
cd backend
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 2. 启动前端（终端 2）

用 WebStorm 打开 `frontend/` 文件夹，然后：
```bash
cd frontend
npm install    # 首次运行
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 3. 登录后台

```
地址：http://localhost:5173/admin/login
账号：admin
密码：admin123
```

### 4. 同步 CSDN 文章

进入 `后台管理 → 文章管理`，点击“同步 CSDN”：

1. 后端从 CSDN RSS 拉取最新文章；
2. 按 CSDN 原文链接去重，重复同步不会重复新增；
3. 新同步文章默认“前台隐藏”；
4. 在文章列表打开“前台展示”开关后，访客才能看到该文章；
5. 再次同步会保留手工上传的封面、推荐状态和展示选择。

封面优先级固定为：

```text
后台上传的自定义封面 > CSDN 原文首图 > 默认占位图
```

默认 RSS 地址可通过环境变量覆盖：

```bash
CSDN_RSS_URL=https://blog.csdn.net/Yihong1833100198/rss/list
CSDN_AUTHOR_NAME=YiHong
```

### 5. 同步 GitHub 项目

进入 `后台管理 → 项目管理`，点击“同步 GitHub”：

1. 后端读取 `CoderYiHong` 拥有的公开仓库，并默认跳过 Fork；
2. 新同步仓库默认“前台隐藏”，由后台逐个选择是否展示；
3. 项目默认使用 GitHub Open Graph 仓库图；
4. 可以在项目编辑抽屉上传自定义封面，移除后自动恢复 GitHub 默认图；
5. 再次同步会保留自定义封面、显隐、推荐和排序。

GitHub 未登录公共接口有较低的共享额度，建议配置个人访问令牌：

```bash
GITHUB_USERNAME=CoderYiHong
GITHUB_TOKEN=你的只读Token
GITHUB_INCLUDE_FORKS=false
```

令牌只需读取公开仓库元数据，不要提交到 Git。

### 6. 已有数据库升级

旧数据库先备份，再执行：

```bash
mysql -u root -p growth < backend/src/main/resources/sql/migrate_v4_source_sync.sql
```

新建数据库已由 `schema.sql` / `sql/init.sql` 包含所需字段。文章和项目的初始化演示数据已移除。

### 7. 联系表单接入 Gmail

联系表单会先把留言保存到数据库，再尝试通过 Gmail 通知站长。邮件的回复地址会自动设为访客填写的邮箱，因此可以直接在 Gmail 中点击“回复”。

1. 给 Google 账号开启两步验证；
2. 在 Google 账号中创建“应用专用密码”；
3. 启动后端前，在 PowerShell 中设置环境变量：

```powershell
$env:GMAIL_USERNAME="你的邮箱@gmail.com"
$env:GMAIL_APP_PASSWORD="Google 生成的 16 位应用专用密码"
# 可选：通知要发送到另一个邮箱时再设置
$env:CONTACT_RECEIVE_EMAIL="你的邮箱@gmail.com"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

不要使用 Gmail 登录密码，也不要把真实应用专用密码写入配置文件或提交到 Git。未配置 Gmail 时，留言仍会保存到后台，前台会提示“邮件通知暂未启用”。

## Git 上传说明

三个 `.gitignore` 已配置好：

| 文件 | 忽略内容 |
|------|----------|
| 根目录 `.gitignore` | IDE 文件、系统文件、UI.png |
| `frontend/.gitignore` | node_modules、dist、.env |
| `backend/.gitignore` | target、data/、.idea |

**上传时这些不会被提交：**
- `node_modules/`（前端依赖，npm install 重新装）
- `dist/`（前端构建产物，npm run build 重新生成）
- `target/`（后端编译产物，Maven 重新编译）
- `backend/data/`（数据库文件，本地开发用）
