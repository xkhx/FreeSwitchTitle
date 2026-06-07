# FreeSwitchTitle 当前开发状态

更新时间：2026-06-07

## 当前分支与提交

- 当前本地分支：`dev`
- 最近关键提交：
  - `11ad202 Add image title display support`
  - `6b78092 Improve title effect lifecycle checks`
- 当前文档更新正在工作区中，尚未提交。

## 最新构建状态

最近一次完整构建：

```text
./gradlew.bat build
BUILD SUCCESSFUL
```

已部署到本地测试服：

```text
E:/Minecraft/Server/plugins/FreeSwitchTitle-2.0.0-SNAPSHOT.jar
```

资源已同步到：

```text
E:/Minecraft/Server/plugins/FreeSwitchTitle/
```

## 已完成核心功能

### 基础称号

- 多文件称号配置加载。
- 称号拥有、佩戴、卸下、移除、重置。
- 当前称号查询、拥有称号查询。
- 管理员添加、移除、设置、清除称号。

### GUI

- 我的称号列表。
- 称号商城。
- 全部称号列表。
- 查看其他玩家称号。
- 称号图鉴。
- 分类筛选菜单。
- 购买确认 GUI。
- GUI lore 模板化。

### 商城

- Vault 货币。
- PlayerPoints 点券。
- BOTH 双货币。
- FREE 免费领取。
- 每个称号可单独配置货币类型。
- 购买权限条件。
- `requirements.permissions` 多权限购买条件。
- 购买日志 `purchase-logs.yml`。
- 限时上架 `available-from` / `available-until`。
- `/fst renew <uid>` 限时称号续费。

### 权限

- LuckPerms 模式。
- GroupManager 命令模式。
- NONE 模式。
- 安全记录本插件实际授予过的权限，避免卸下称号时误删玩家原有权限。

### 生命周期

- 永久称号。
- 限时称号。
- 上线清理过期称号。
- 在线玩家周期清理过期称号。
- 过期后自动卸下当前称号并回收权限。
- reload 后刷新在线玩家当前称号效果。
- disable 时清理预览、粒子、药水/属性和图片显示。

### 动作系统

支持动作前缀：

- `[console]`
- `[player]`
- `[op]`
- `[message]`
- `[broadcast]`
- `[actionbar]`
- `[title]`
- `[sound]`
- `[delay]`

支持动作节点：

- `obtain`
- `equip`
- `unequip`
- `buy`
- `expire`
- `remove`
- `reset`

支持变量：

- `{player}` / `%player%`
- `{uuid}` / `%uuid%`
- `{title}` / `%title%`
- `{uid}` / `{title_id}` / `%title_id%`
- `{duration}` / `%duration%`
- `{expire}` / `%expire%`
- `{price}` / `%price%`

### 视觉效果

- 独立 `particles.yml` 粒子预设。
- 支持 HALO、RING、AURA、SPIRAL、TRAIL、WINGS。
- 独立 `effects.yml` 药水与属性预设。
- 佩戴、切换、卸下、过期、退出、插件关闭时自动管理效果生命周期。
- 药水清理时恢复玩家原本已有的同类药水效果。
- 每 5 分钟刷新在线玩家称号 Buff，避免药水自然到期。
- `/fst preview <uid>` 粒子预览。

### 图片称号

- 新增 `display` 称号节点。
- 使用 Bukkit `TextDisplay` 在玩家头顶显示图片称号。
- 支持 `display.text` 手动资源包字符。
- 支持 `display.image` 自动图片模式。
- 自动扫描 `plugins/FreeSwitchTitle/images/*.png`。
- 自动生成资源包目录与 `resourcepack.zip`。
- 自动生成字体 bitmap glyph 映射。
- 自动生成并维护 `image-mapping.yml` 固定图片字符映射。
- 支持每张图片独立配置 `height` 与 `ascent`。
- 自动计算并输出资源包 SHA1。
- 已接入上线、佩戴、切换、卸下、过期、退出、reload、disable 生命周期。

当前限制：

- 插件只生成资源包 zip，不内置 HTTP 托管。
- 玩家必须加载资源包才能看到图片称号。
- 如手动重命名图片，需要同步调整 `image-mapping.yml` 中的 `file`。

### 图鉴与收藏

- 稀有度：common、rare、epic、legendary、limited。
- 隐藏称号未拥有时不显示。
- 收藏进度统计。
- 收藏奖励 `collection.rewards`。
- PlaceholderAPI 收藏变量。

### PlaceholderAPI

已支持：

- `%fst_title%`
- `%fst_title_show%`
- `%fst_title_uid%`
- `%fst_title_count%`
- `%fst_title_collected%`
- `%fst_title_total%`
- `%fst_title_progress%`

### 配置迁移与检查

- `config-version: 2`。
- 启动和 `/fst reload` 自动补齐缺失配置节点。
- 迁移前备份旧配置。
- 自动生成缺失的 `particles.yml`、`effects.yml`。
- 自动补齐默认粒子/药水/属性预设。
- `/fst validate` 检查：
  - 称号基础字段。
  - 价格负数。
  - 权限空节点。
  - 粒子/药水/属性类型。
  - 预设引用。
  - 收藏奖励引用。
  - 商城上架时间格式。
  - 图片称号配置与图片文件存在性。

## 当前文档状态

已更新：

- `README.md`：项目首页、功能概览、快速开始、命令、图片称号说明。
- `docs/USER_GUIDE.md`：完整服主使用指南。
- `docs/RESOURCE_PACK.md`：图片称号和资源包专题说明。
- `CURRENT_STATUS.md`：当前状态。

待更新/可继续完善：

- `MILESTONES.md` 可继续补充图片称号后续规划，例如内置 HTTP 下发、固定 image mapping、单图 height/ascent。
- 可补充开发者 API 专题文档。
- 可补充 GUI 配置专题文档。

## 审查结论

### 已达到可测试状态

当前代码功能完整度较高，默认配置能启动，构建通过，并且已部署过本地测试服。称号基础、商城、生命周期、效果、图片称号、配置检查均已形成闭环。

### 主要风险点

1. 图片称号资源包下发
   - 当前只生成 zip，不负责托管和下发。
   - 服主需要自行配置 `server.properties` 或上传到资源包地址。

2. 图片 glyph 映射维护
   - 当前已通过 `image-mapping.yml` 固定映射。
   - 若服主手动改 `char`，需要重新上传/下发资源包。
   - 若服主重命名图片，需要同步调整映射中的 `file`。

3. 真实经济/权限插件联动
   - 代码路径已实现，但需要在安装 Vault、PlayerPoints、LuckPerms、GroupManager 的真实环境继续验证。

4. GUI 实操验证
   - 构建和部署已完成，但仍建议使用真实玩家完整点一遍 GUI 流程。

## 推荐下一步规划

### P0：发布前验证

- 使用真实玩家验证 GUI：
  - `/fst open`
  - `/fst shop`
  - `/fst collection`
  - `/fst preview <uid>`
  - 购买确认 GUI
- 验证限时称号过期。
- 验证图片称号资源包加载。
- 验证 reload 后在线玩家效果刷新。

### P1：图片称号体验增强

- 可选：内置 HTTP 资源包托管和玩家进服自动下发。
- 图片称号预览命令。
- 支持更多单图字体参数，例如左右间距。

### P2：文档继续细化

- 新增 `docs/API.md`。
- 新增 `docs/GUI.md`。
- 新增 `docs/CONFIGURATION.md`。

### P3：体验增强

- 商城不可购买状态更细分，例如余额不足、未到上架时间、已下架。
- GUI 分类图标独立配置。
- 图片称号预览命令。
