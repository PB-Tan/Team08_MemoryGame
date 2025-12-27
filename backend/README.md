# Memory Game API - 后端使用说明

## 快速启动

### 1. 进入后端目录
```bash
cd backend
```

### 2. 运行项目
```bash
dotnet run
```

### 3. 访问API
- **本地访问**: http://localhost:5000
- **Swagger文档**: http://localhost:5000/swagger
- **局域网访问**: http://YOUR_IP:5000 (例如: http://192.168.1.100:5000)

## 获取你的电脑IP地址

### Windows:
```bash
ipconfig
```
查找 "IPv4 地址"

### 示例输出:
```
无线局域网适配器 WLAN:
   IPv4 地址 . . . . . . . . . . . . : 192.168.1.100
```

在Android中使用: `http://192.168.1.100:5000`

## API 端点

### 认证
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/users` - 获取所有用户（测试）

### 分数
- `POST /api/scores` - 提交分数
- `GET /api/scores/top5` - 获取排行榜
- `GET /api/scores` - 获取所有分数（测试）

### 广告
- `GET /api/images/ads` - 获取广告列表
- `GET /api/images/ads/random` - 获取随机广告

### 房间
- `POST /api/rooms` - 创建房间
- `POST /api/rooms/join` - 加入房间
- `GET /api/rooms/{roomCode}` - 获取房间信息
- `POST /api/rooms/{roomCode}/start` - 开始游戏
- `POST /api/rooms/{roomCode}/scores` - 提交房间分数
- `GET /api/rooms/{roomCode}/leaderboard` - 房间排行榜
- `POST /api/rooms/{roomCode}/leave` - 退出房间

## 测试用户

| 用户名 | 密码 | 类型 |
|--------|------|------|
| alice | pass123 | 免费用户 |
| bob | pass456 | 付费用户 |
| charlie | pass789 | 免费用户 |
| david | pass000 | 付费用户 |
| eve | pass111 | 免费用户 |

## 测试建议

1. 先在浏览器访问 Swagger 测试接口
2. 使用 Postman 测试完整流程
3. 配置 Android 使用你的电脑 IP
4. 确保电脑和手机在同一局域网

## 数据库

- 使用 SQLite
- 数据库文件: `memorygame.db`
- 自动创建并初始化测试数据

## 故障排除

### 端口被占用
如果5000端口被占用，修改 `Program.cs` 中的端口号

### Android 无法连接
1. 检查防火墙设置
2. 确保在同一局域网
3. 使用电脑IP而不是localhost
