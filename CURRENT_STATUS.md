# FreeSwitchTitle 当前开发状态

## 分支与提交

- 当前本地分支：`dev`
- 当前跟踪远程：`origin/dev`
- 远程 `dev` 最新提交：
  - `dabf27a Add title action executor`
- 当前本地在 `dabf27a` 之上继续开发，存在未提交改动。

## 最新构建状态

最新一次完整构建结果：

```text
./gradlew.bat build
BUILD SUCCESSFUL
```

本次构建已覆盖：

- TrMenu 风格称号动作系统
- GUI lore 模板化
- 称号有效期基础系统
- 登录与在线周期过期清理

## 已完成并推送的功能

### 1. TrMenu 风格称号动作系统

已实现并推送到 `origin/dev`：

- 支持 `[console]`
- 支持 `[player]`
- 支持 `[op]`
- 支持 `[message]`
- 无前缀动作默认按 `[console]` 执行
- `actions` 配置优先
- `commands` 配置作为兼容回退
- 命令动作兼容前导 `/`

### 2. 称号商城与权限系统

已实现：

- Vault 购买
- PlayerPoints 购买
- BOTH 双货币购买
- LuckPerms 权限发放/回收
- GroupManager 命令式权限发放/回收
- NONE 权限模式
- 安全记录本插件实际授予过的权限，避免误删玩家原有权限

### 3. 命令与 PlaceholderAPI

已实现：

- `/fst buy <uid>`
- `/fst look <player>`
- `/fst set <player> <uid>`
- `/fst clear <player>`
- `/fst reset` 返回值修复
- `%fst_title%`
- `%fst_title_show%`
- `%fst_title_uid%`
- `%fst_title_count%`

## 当前未提交开发内容

### 1. GUI lore 模板化

已实现本地改动：

- 新增 `gui.lore.*` 模板配置。
- GUI 称号物品追加 lore 改为读取模板。
- 旧 `gui.status.*` 仍作为兼容回退。
- 支持模板变量：
  - `{title}`
  - `{uid}`
  - `{duration}`
  - `{expire}`
  - `{vault_price}`
  - `{points_price}`

### 2. 称号有效期系统

已实现本地改动：

- `title.yml` 新增 `duration` 配置。
- 支持永久称号：`permanent`、`forever`、`永久`、`0`。
- 支持限时称号：`7d`、`12h`、`30m`、`10s`、`1000ms`。
- 无单位数字按秒解析。
- 玩家拥有称号时记录到期时间。
- 过期称号会从玩家拥有列表中移除。
- 如果当前佩戴称号过期，会自动卸下并回收权限。
- 玩家登录时清理过期称号。
- 在线玩家每 10 分钟周期清理一次。

## 当前工作区状态

当前存在未提交改动：

```text
M src/main/kotlin/top/zoyn/freeswitchtitle/FreeSwitchTitle.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/api/FreeSwitchTitleAPI.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/data/TitleData.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/gui/PlayerGui.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/listener/PlayerListener.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/ConfigUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/Utils.kt
M src/main/resources/gui.yml
M src/main/resources/lang/zh_CN.yml
M src/main/resources/titledata/title.yml
M README.md
?? src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleDurationUtils.kt
```

## 尚未完成

- 尚未进行真实服务器运行时验证。
- 尚未提交本次 GUI lore 模板化与称号有效期改动。
- 暂不处理 `/fst balance`。
- 暂不处理 PlayerPoints 强类型依赖。

## 建议下一步

1. 做真实服务器内验证：
   - GUI lore 模板是否按状态渲染。
   - 永久称号是否不受过期逻辑影响。
   - 限时称号是否能自动过期。
   - 当前佩戴限时称号过期后是否卸下并回收权限。
2. 验证通过后提交：

```text
Add title expiry and GUI lore templates
```
