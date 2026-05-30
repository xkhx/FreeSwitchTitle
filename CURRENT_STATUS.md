# FreeSwitchTitle 当前开发状态

## 当前分支与提交

- 当前本地分支：`master`
- 最近本地提交：
  - `90af23e Improve title commands and switch handling`
  - `cc378d0 Add title shop economy and permission hooks`
- 已覆盖推送到远程 `dev` 的提交：`cc378d0`
- 注意：`90af23e` 目前是本地提交，尚未推送到远程。

## 构建状态

最新一次完整构建结果：

```text
./gradlew.bat build
BUILD SUCCESSFUL
```

该构建已在动作系统改动完成后执行，验证了：

- 称号商城经济逻辑
- 权限发放/回收逻辑
- `/fst buy`
- `/fst look`
- `/fst set`
- `/fst clear`
- `/fst reset` 返回值修复
- 称号切换权限回滚修复
- TrMenu 风格称号动作系统编译通过

## 已完成并提交的功能

### 1. 称号商城经济系统

已实现：

- Vault 经济购买
- PlayerPoints 购买
- BOTH 双货币购买
- 余额检查
- 扣款失败处理
- 添加称号失败后的退款回滚
- 购买结果提示

相关文件：

```text
src/main/kotlin/top/zoyn/freeswitchtitle/hook/economy/
```

### 2. Vault 接入

当前使用 TabooLib 提供的：

```kotlin
VaultService.economy
```

并通过 Vault API 执行：

- `getBalance`
- `has`
- `withdrawPlayer`
- `depositPlayer`

`build.gradle.kts` 中 VaultAPI 使用非传递依赖，避免拉取旧 Bukkit：

```kotlin
compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
    isTransitive = false
}
```

### 3. PlayerPoints 接入

当前 PlayerPoints 使用反射调用，原因是 Maven 坐标解析失败。

现状：

- 暂时不处理该问题。
- 后续如果需要，可以改成把 PlayerPoints jar 放入 `libs` 后强类型调用。

### 4. 权限系统

已实现：

- LuckPerms 权限发放/回收
- GroupManager 命令式发放/回收
- NONE 模式
- 安全记录本插件实际授予过的权限，避免误删玩家原有权限

相关文件：

```text
src/main/kotlin/top/zoyn/freeswitchtitle/hook/permission/
```

### 5. 称号切换逻辑修复

已修复旧问题：

- 原本切换称号时会先卸下旧称号，再尝试发放新称号权限。
- 如果新称号权限发放失败，会导致玩家数据仍显示旧称号，但旧称号权限已经被移除。

现在逻辑：

1. 先尝试发放新称号权限。
2. 成功后再卸下旧称号。
3. 写入当前称号。
4. 执行新称号佩戴动作。

另外已处理重叠权限问题：

- 如果旧称号和新称号拥有相同权限，不会误删这些重叠权限。
- 重叠权限的授权记录会从旧称号转移到新称号。

### 6. 命令补齐

已新增：

```text
/fst buy <uid>
/fst look <player>
/fst set <player> <uid>
/fst clear <player>
```

已修复：

```text
/fst reset
```

现在没有佩戴称号时会发送失败提示，而不是永远提示成功。

### 7. PlaceholderAPI 扩展

当前支持：

```text
%fst_title%
%fst_title_show%
%fst_title_uid%
%fst_title_count%
```

## 当前正在开发的功能

### TrMenu 风格动作系统

当前处于未提交完成状态，已构建验证。

已修改文件：

```text
src/main/kotlin/top/zoyn/freeswitchtitle/data/TitleData.kt
src/main/kotlin/top/zoyn/freeswitchtitle/util/ConfigUtils.kt
src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleEffectUtils.kt
src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleUtils.kt
```

当前已完成的动作系统改动：

1. `TitleData` 字段从命令语义改为动作语义：

```kotlin
val equipActions: List<String>
val unequipActions: List<String>
val buyActions: List<String>
```

2. `ConfigUtils` 新增动作读取逻辑：

- 优先读取：

```yaml
actions:
  equip:
  unequip:
  buy:
```

- 如果新配置为空，则回退读取旧配置：

```yaml
commands:
  equip:
  unequip:
  buy:
```

3. `TitleEffectUtils` 已改为动作执行器，当前支持：

```text
[console]
[player]
[op]
[message]
```

4. 无前缀动作默认按 `[console]` 执行，以兼容旧配置。

命令类动作会兼容开头 `/`，执行前会自动去掉命令前导 `/`。

5. 默认示例配置已从 `commands:` 更新为 `actions:`：

```text
src/main/resources/titledata/title.yml
```

6. 已搜索旧命名引用，`equipCommands`、`unequipCommands`、`buyCommands`、`runCommands` 等旧命名无残留。

7. 旧 `commands:` 配置路径仅在 `ConfigUtils` 中作为兼容回退保留：

```kotlin
titleConfig.getStringList("$uid.commands.$type")
```

当前动作示例：

```yaml
actions:
  equip:
    - '[console] say {player} 佩戴了 {title}'
    - '[message] &a你佩戴了 {title}'
  unequip:
    - '[message] &c你卸下了 {title}'
  buy:
    - '[message] &a你购买了 {title}'
```

支持变量：

```text
{player}
{uuid}
{title}
{uid}
```

## 动作系统当前状态

第一版动作系统已完成，包含：

- 支持 `[console]`
- 支持 `[player]`
- 支持 `[op]`
- 支持 `[message]`
- 无前缀动作默认按 `[console]` 执行
- 优先读取 `actions` 配置
- `actions` 为空时回退读取旧 `commands` 配置

已完成构建验证：

```bash
./gradlew.bat build
```

```text
BUILD SUCCESSFUL
```

当前未提交，未 push。

## 暂时不做的功能

### `/fst balance`

用户明确说明暂时不需要，因此不实现。

### PlayerPoints 强类型依赖

暂时不处理，继续保留反射调用。

### GUI lore 模板化

已列为后续任务，但当前未开始。

### 称号有效期系统

已列为后续任务，但当前未开始。

## 后续任务列表

### 1. 提交动作系统

优先级：中

剩余事项：

- 检查 diff
- 提交动作系统改动
- 如需要，再推送远程

### 2. GUI lore 模板化

优先级：中

目标：

- 将当前写死在代码里的 GUI 状态 lore 改成配置模板。
- 支持不同状态使用不同 lore。

### 3. 称号有效期系统

优先级：低

目标：

- 支持限时称号。
- 支持永久称号。
- 支持登录或定时检查过期称号。

## 当前工作区状态

当前存在未提交改动，均属于动作系统开发：

```text
M src/main/kotlin/top/zoyn/freeswitchtitle/data/TitleData.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/ConfigUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleEffectUtils.kt
M src/main/kotlin/top/zoyn/freeswitchtitle/util/TitleUtils.kt
M README.md
M src/main/resources/titledata/title.yml
?? CURRENT_STATUS.md
```

## 建议下一步

建议下一步处理提交：

1. 确认是否要把 `CURRENT_STATUS.md` 纳入版本管理。
2. 检查动作系统 diff。
3. 提交：

```text
Add title action executor
```
