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
- M4 动作增强：`[broadcast]`、`[actionbar]`、`[title]`、`[sound]`、`[delay]`
- M4 动作变量统一
- M5 稀有度、称号图鉴、隐藏称号、收藏进度与收藏奖励
- M6 数据版本、迁移备份、旧数据自动升级与获得元数据
- M7 开发者 API 结果对象与 Bukkit 事件
- GUI 分类选择菜单
- 图鉴未解锁称号灰色占位显示
- 管理员 `/fst balance <player>` 查询在线玩家余额
- `/fst validate` 配置检查命令
- 称号内置粒子效果：HALO、RING、AURA、SPIRAL、TRAIL、WINGS
- 独立 `particles.yml` 粒子预设配置，称号可复用预设
- 独立 `effects.yml` 药水/属性预设配置，称号可复用增益效果
- `/fst preview <uid>` 称号粒子预览与商城购买确认 GUI
- 配置迁移与自动补齐，`config-version` 升级到 2

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

- `/fst balance`
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
  - `{category}`
  - `{duration}`
  - `{expire}`
  - `{vault_price}`
  - `{points_price}`

### 2. 称号分类过滤

已实现本地改动：

- `title.yml` 新增 `category` 配置，未配置时默认 `default`。
- `TitleData` 新增 `category` 字段。
- GUI 列表支持按分类过滤。
- `/fst open <category>` 支持按分类查看自己的称号。
- `/fst shop <category>` 支持按分类查看商城称号。
- `/fst list <category>` 支持按分类查看全部称号。
- `/fst look <player> <category>` 支持按分类查看目标玩家称号。
- 分类参数补全支持 `all` 和已加载分类。
- GUI lore 支持 `{category}` 变量。
- 暂未新增独立分类选择 GUI，后续可继续扩展。

### 3. 商城货币增强

已实现本地改动：

- `CurrencyType` 新增 `FREE`。
- `TitleData` 新增 `shopCurrency` 字段。
- `title.yml` 支持每个称号单独配置 `shop.currency`。
- 未配置称号级 `shop.currency` 时回退 `config.yml` 的全局 `shop.currency`。
- 购买逻辑改为按称号自身货币类型检查余额、扣款和回滚。
- `FREE` 称号不检查余额、不扣款，购买成功后正常发放称号并执行购买动作。
- GUI 商城价格显示改为按称号自身货币类型展示。
- GUI 新增 `shop.free-price` 和旧版 `gui.status.free-price` 兼容回退。
- `config.yml` 注释已更新，说明支持 `VAULT / PLAYER_POINTS / BOTH / FREE`。

### 4. 购买条件

已实现本地改动：

- `title.yml` 新增 `requirements.permissions` 购买条件配置。
- `TitleData` 新增 `requiredPermissions` 字段。
- 购买时会检查玩家是否满足 `requirements.permissions` 中的全部权限。
- 旧字段 `shop.permission` 继续保留并优先兼容。
- 新增购买失败结果 `REQUIREMENT_NOT_MET`。
- 新增语言项 `purchase-requirement-not-met`。
- `FREE` 免费称号同样会先检查购买条件，再发放称号。

### 5. 购买日志

已实现本地改动：

- 新增 `PurchaseSource`，区分 `COMMAND` 与 `GUI` 购买来源。
- 新增 `PurchaseLogger`，将购买成功/失败追加记录到 `purchase-logs.yml`。
- 新增配置 `shop.log-purchases`，默认开启。
- 命令购买记录来源为 `COMMAND`。
- GUI 购买记录来源为 `GUI`。
- 日志字段包含时间、玩家、UUID、称号 UID、称号名、结果、货币类型、价格和来源。
- 日志写入失败只输出控制台告警，不影响购买结果。

### 6. 称号续费

已实现本地改动：

- 新增 `/fst renew <uid>` 续费命令。
- 只能续费当前仍拥有的限时称号。
- 永久称号不可续费。
- 已过期并被清理的称号视为未拥有，不能续费。
- 续费时按称号自身 `duration` 延长一个周期。
- 如果当前到期时间仍在未来，则从当前到期时间继续延长。
- 如果没有到期时间但仍拥有该限时称号，则从当前时间开始延长。
- 续费费用复用称号当前 `shop.currency`、`vault-price`、`points-price`。
- `FREE` 称号续费不扣费。
- 续费不执行 `buyActions`，也不写入 `purchase-logs.yml`。

### 7. 商城限时上架

已实现本地改动：

- `title.yml` 的 `shop` 节点新增 `available-from` 和 `available-until`。
- 时间格式固定为 `yyyy-MM-dd HH:mm:ss`。
- 两个字段为空时不限制上架时间。
- GUI 商城只显示当前处于上架时间窗口内的称号。
- `/fst buy <uid>` 会检查上架时间窗口。
- 不在可购买时间内会返回 `purchase-not-available-time`。
- 续费 `/fst renew <uid>` 不受上架时间影响。
- 时间解析失败时视为不限制该字段，并输出控制台警告。

### 8. 动作类型增强

已实现本地改动：

- 新增 `[broadcast]` 全服广播动作。
- 新增 `[actionbar]` 动作栏动作；运行时若服务端不支持 Spigot ActionBar API，会退回普通消息。
- 新增 `[title]` 屏幕标题动作，格式为 `主标题;副标题`。
- 新增 `[sound]` 播放声音动作，格式为 `声音名;音量;音调`。
- 新增 `[delay]` 延迟动作，用于延迟执行后续动作。
- `[delay]` 支持 `20t`、`2s`、`500ms`、`1m`，纯数字按 tick 处理。
- `[delay]` 后续动作执行前会检查玩家是否在线，玩家离线时安全停止动作链。
- 无效声音名会被忽略，不中断后续动作。
- `title.yml` 已补充支持动作前缀注释。

### 9. 动作变量统一

已实现本地改动：

- 动作文本新增支持 `%player%`、`%uuid%`、`%title%`、`%title_id%`、`%duration%`、`%expire%`、`%price%`。
- 继续兼容 `{player}`、`{uuid}`、`{title}`、`{uid}`。
- 同步补充 `{title_id}`、`{duration}`、`{expire}`、`{price}`。
- `{expire}` / `%expire%` 会显示玩家该称号剩余时间，永久称号显示永久。
- `{price}` / `%price%` 会按称号自身货币类型显示金币、点券、双货币或免费。

### 10. 动作节点增强

已实现本地改动：

- 新增 `actions.obtain`，玩家获得称号时触发。
- 新增 `actions.remove`，玩家失去称号拥有权时触发。
- 新增 `actions.reset`，玩家清除当前佩戴称号时触发。
- 购买成功会触发 `obtainActions + buyActions`。
- 移除当前佩戴称号时动作顺序为 `unequipActions -> resetActions -> removeActions`。
- 过期清理仍只触发 `expireActions`，不会误触发 `removeActions`。
- 新节点同样兼容旧格式 `commands.obtain/remove/reset`。

### 11. 称号有效期系统

已实现本地改动：

- `title.yml` 新增 `duration` 配置。
- 支持永久称号：`permanent`、`forever`、`永久`、`0`。
- 支持限时称号：`7d`、`12h`、`30m`、`10s`、`1000ms`。
- 无单位数字按秒解析。
- 玩家拥有称号时记录到期时间。
- 过期称号会从玩家拥有列表中移除。
- 如果当前佩戴称号过期，会自动卸下并回收权限。
- 支持 `actions.expire` 过期动作。
- `actions.expire` 会在在线清理或玩家上线清理过期称号时执行。
- 旧格式 `commands.expire` 可作为兼容回退。
- 玩家登录时清理过期称号。
- 在线玩家每 10 分钟周期清理一次。

### 12. 稀有度、图鉴与收藏玩法

已实现本地改动：

- 新增 `TitleRarity`，支持 `common`、`rare`、`epic`、`legendary`、`limited`。
- `title.yml` 新增 `rarity` 和 `hidden` 配置。
- `TitleData` 新增 `rarity` 和 `hidden` 字段。
- 新增 `/fst collection <category>` 图鉴入口，支持分类过滤。
- 图鉴中隐藏称号未拥有时不可见，已拥有后显示。
- GUI lore 新增图鉴模板 `gui.lore.collection.owned/not-owned`。
- GUI lore 变量新增 `{rarity}`、`{rarity_name}`、`{rarity_color}`、`{collected}`、`{total}`、`{progress}`。
- PlaceholderAPI 新增 `%fst_title_collected%`、`%fst_title_total%`、`%fst_title_progress%`。
- 新增 `collection.rewards` 配置，达到收藏数量阈值后自动发放奖励称号。
- 收藏奖励阈值会记录到玩家数据，避免重复发放。
- 玩家上线和新增称号时会检查收藏奖励。

### 13. 数据版本与迁移

已实现本地改动：

- `config.yml` 新增 `config-version: 1`。
- 玩家数据新增 `data-version`，当前版本为 `1`。
- 玩家旧数据首次访问或上线时会自动迁移。
- 迁移前会把旧字段快照保存到 `migration_backup_v1`。
- 迁移保留旧字段 `title_list`、`using`、`title_expire_map` 作为兼容主路径。
- 新增 `title_obtain_time_map`，记录称号获得时间。
- 新增 `title_obtain_source_map`，记录称号获得来源。
- 迁移已有称号时，来源标记为 `MIGRATION`。
- 命令/API 添加称号默认来源为 `COMMAND`。
- GUI/命令购买称号会按 `PurchaseSource` 记录来源。
- 收藏奖励称号来源标记为 `COLLECTION_REWARD`。
- 移除或过期称号时会同步清理获得元数据。
- 迁移失败只输出控制台警告，不删除旧数据。
- API 新增玩家数据版本、获得时间和获得来源查询方法。

### 14. 开发者 API 与 Bukkit 事件

已实现本地改动：

- 新增 `TitleOperationResult`，用于返回明确 API 操作结果。
- 新增 result API：发放、移除、设置当前称号、重置当前称号。
- 旧 Boolean API 继续保留并兼容。
- 新增 `TitleGrantEvent`，玩家获得称号前触发，可取消。
- 新增 `TitleRemoveEvent`，玩家失去称号前触发，可取消。
- 新增 `TitleEquipEvent`，玩家佩戴称号前触发，可取消。
- 新增 `TitleResetEvent`，玩家重置当前称号前触发，可取消。
- 新增 `TitleUnequipEvent`，玩家卸下称号后触发，不可取消。
- 新增 `TitleExpireEvent`，称号过期清理后触发，不可取消。
- 新增 `TitleBuyEvent`，购买成功并发放称号后触发，不可取消。
- API 失败原因包含称号不存在、已拥有、未拥有、无当前称号、权限失败、事件取消、玩家离线等。

## 当前工作区状态

当前存在未提交改动：

```text
M CURRENT_STATUS.md
M src/main/kotlin/top/zoyn/freeswitchtitle/api/FreeSwitchTitleAPI.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/command/FreeSwitchTitleCommand.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/data/TitleData.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/gui/PlayerGui.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/gui/type/GuiType.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/hook/PapiHook.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/CurrencyType.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/EconomyManager.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/PurchaseResult.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/listener/PlayerListener.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/ConfigUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleEffectUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/Utils.kt
M src/main/resources/config.yml
M src/main/resources/gui.yml
M src/main/resources/lang/zh_CN.yml
M src/main/resources/titledata/title.yml
?? MILESTONES.md
?? src/main/kotlin/top/zoyn/freeswitchtitle/api/TitleOperationResult.kt
?? src/main/kotlin/top/zoyn/freeswitchtitle/data/TitleRarity.kt
?? src/main/kotlin/top/zoyn/freeswitchtitle/event/
?? src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/PurchaseLogger.kt
?? src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/PurchaseSource.kt
```

## 里程碑规划

已新增正式规划文档：

```text
MILESTONES.md
```

规划阶段：

1. M0 发布前收尾与稳定化
2. M1 GUI 与配置体验完善
3. M2 商城系统增强
4. M3 称号生命周期系统
5. M4 动作系统增强
6. M5 稀有度、图鉴与收藏玩法
7. M6 数据结构与迁移
8. M7 开发者 API 与 Bukkit 事件

## 真实服务端验证状态

已使用以下测试环境完成基础运行时验证：

```text
E:\Minecraft\Server
spigot-1.20.4.jar
plugins/FreeSwitchTitle-2.0.0-SNAPSHOT.jar
plugins/PlaceholderAPI-2.11.6.jar
```

本次已完成：

- 已备份旧插件：`plugins/_backup_fst/FreeSwitchTitle-2.0-SNAPSHOT.jar.bak_20260607`。
- 已部署新构建：`plugins/FreeSwitchTitle-2.0.0-SNAPSHOT.jar`。
- 服务端可正常启动到 `Done`。
- FreeSwitchTitle 可正常加载并启用。
- SQLite 数据加载完成。
- 称号配置可正常读取，当前加载 1 个称号。
- PlaceholderAPI 内部扩展 `fst` 注册成功。
- 控制台命令验证通过：
  - `fst list`
  - `fst show fly`
  - `fst reload`
  - reload 后再次 `fst list`
- 服务端可正常停止，FreeSwitchTitle 可正常 disable。

已观察到但不属于本插件阻塞的问题：

- PlaceholderAPI 在快速停止服务端时出现异步更新检查异常。
- FreeSwitchTitle 出现 PlaceholderAPI softdepend 警告；插件仍正常加载和注册占位符。

## 最新增强功能

本轮已按推荐方向完成以下增强，并通过 `./gradlew.bat build`：

- GUI 新增分类筛选按钮与分类选择菜单，玩家可在 `/fst open`、`/fst shop`、`/fst collection`、`/fst list` 打开的 GUI 中切换分类。
- 图鉴未解锁称号改为灰色占位物品显示，避免提前暴露称号名和完整 lore，同时保留稀有度与收藏进度提示。
- `/fst balance <player>` 支持管理员查询在线玩家 Vault / PlayerPoints 余额，需要 `freeswitchtitle.command.balance.other` 权限。
- 新增 `/fst validate`，可检查称号数量、分类数量、收藏奖励引用、价格负数、空权限节点等常见配置问题。
- `gui.yml` 与 `lang/zh_CN.yml` 已同步新增相关配置项和语言项。
- 已新增独立 `particles.yml` 粒子预设配置，称号可通过 `effects.particle: <预设名>` 或 `effects.particle.preset: <预设名>` 复用预设。
- 仍兼容称号内联 `effects.particle` 完整配置，适合少量特殊称号覆盖。
- 粒子生命周期支持佩戴启动、切换/卸下/过期/退出/插件关闭自动停止。
- 粒子形状支持 `HALO`、`RING`、`AURA`、`SPIRAL`、`TRAIL`、`WINGS`，可配置粒子类型、刷新间隔、数量、半径、高度、点数、速度和偏移。
- `particles.yml` 已内置 `angel-wings` 翅膀预设，可直接在称号中使用 `effects.particle: angel-wings`。
- `/fst show <uid>` 会显示称号粒子预设/类型/形状，`/fst validate` 会检查 `particles.yml` 与称号粒子配置中的粒子类型是否合法。
- 该方案不依赖 PlayerParticles；项目已安装 TabooLib `MinecraftEffect` 环境模块，当前实现使用 Bukkit 原生粒子生成，后续可基于 `minecraft-effect` 扩展更复杂形状。
- 已新增独立 `effects.yml`，支持 `potions` 药水效果预设和 `attributes` 属性效果预设。
- 称号可通过 `effects.potion: <预设名>`、`effects.attribute: <预设名>` 引用预设，也可通过 `effects.potions`、`effects.attributes` 内联覆盖。
- 药水/属性效果会在佩戴和上线时应用，在切换、卸下、过期、退出和插件关闭时清理。
- `/fst show <uid>` 会显示增益预设，`/fst validate` 会检查药水类型和属性类型是否合法。
- 已新增 `/fst preview <uid>`，可临时预览称号粒子效果，不改变当前称号、不发放权限、不应用药水/属性。
- 已新增 `preview.enable`、`preview.duration`、`preview.cooldown` 配置，并在退出时自动清理预览。
- 商城点击称号时可通过 `shop.confirm-purchase` 打开购买确认 GUI，点击确认后才扣费购买，点击取消不会扣费。
- 已新增配置迁移器，启动和 `/fst reload` 时会自动备份并补齐缺失节点。
- 自动补齐范围包括 `config.yml` 的预览/确认购买节点、`gui.yml` 的分类/图鉴/确认购买节点、`lang/zh_CN.yml` 的新增语言项，以及缺失的 `particles.yml`、`effects.yml`。
- 自动补齐只写缺失节点，不覆盖已有配置值。

## 尚未完成

- 尚未进行在线玩家 GUI 实操验证。
- 尚未进行在线玩家购买、佩戴、卸下、重置完整流程验证。
- 测试服当前未安装 Vault / PlayerPoints / LuckPerms / GroupManager，暂未验证经济与权限插件联动。
- `/fst balance` 已实现玩家自查与管理员查看在线玩家余额。
- 暂不处理 PlayerPoints 强类型依赖。

## 建议下一步

1. 安装或接入 Vault / PlayerPoints / LuckPerms / GroupManager 后验证经济与权限联动。
2. 使用在线玩家验证：
   - GUI lore 模板是否按状态渲染。
   - 永久称号是否不受过期逻辑影响。
   - 限时称号是否能自动过期。
   - 当前佩戴限时称号过期后是否卸下并回收权限。
   - `/fst open`、`/fst shop`、`/fst collection` GUI 是否正常。
3. 若接受当前基础运行时验证结果，可整理并提交当前里程碑改动。
