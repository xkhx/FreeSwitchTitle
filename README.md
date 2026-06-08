# FreeSwitchTitle

FreeSwitchTitle 是一个基于 TabooLib 的 Bukkit / Spigot 称号插件，面向 Minecraft 1.20.4 开发。插件支持称号佩戴、GUI、商城、限时称号、权限发放、粒子/药水/属性效果、动作系统、PlaceholderAPI、称号图鉴，以及基于资源包的头顶图片称号。

## 功能概览

| 模块 | 状态 | 说明 |
| --- | --- | --- |
| 基础称号 | 已完成 | 称号加载、拥有、佩戴、卸下、查看、管理 |
| GUI | 已完成 | 我的称号、商城、全部称号、查看他人、图鉴、分类筛选、购买确认 |
| 商城 | 已完成 | 支持 Vault、PlayerPoints、BOTH、FREE，多货币与购买日志 |
| 权限 | 已完成 | 支持 LuckPerms、GroupManager、NONE，安全记录本插件授予的权限 |
| 生命周期 | 已完成 | 永久/限时称号、续费、过期清理、上线清理、在线周期清理 |
| 动作系统 | 已完成 | 支持命令、消息、广播、动作栏、标题、音效、延迟动作 |
| 视觉效果 | 已完成 | 粒子预设、药水预设、属性预设、粒子预览 |
| 图片称号 | 已完成 | 基于 TextDisplay + 资源包字体 glyph 在玩家头顶显示图片 |
| 图鉴收藏 | 已完成 | 稀有度、隐藏称号、收藏进度、收藏奖励 |
| PAPI | 已完成 | 当前称号、UID、拥有数量、收藏进度等变量 |
| 配置迁移 | 已完成 | 自动补齐缺失配置节点并备份旧配置 |
| 配置检查 | 已完成 | `/fst validate` 检查常见配置错误 |

## 运行环境

- Minecraft / Spigot：当前按 `1.20.4` 编译测试。
- Java：遵循项目 Gradle 配置，目标 Java 8 字节码。
- 必需：TabooLib 运行环境由构建产物处理。
- 可选依赖：
  - PlaceholderAPI：显示 `%fst_*%` 变量。
  - Vault + 经济插件：Vault 货币购买。
  - PlayerPoints：点券购买。
  - LuckPerms：权限授予/回收。
  - GroupManager：命令式权限授予/回收。

## 快速开始

1. 构建插件：

   ```bash
   ./gradlew.bat build
   ```

2. 将构建产物放入服务器插件目录：

   ```text
   build/libs/FreeSwitchTitle-2.0.0-SNAPSHOT.jar
   ```

3. 启动服务器，生成默认配置。

4. 编辑：

   ```text
   plugins/FreeSwitchTitle/config.yml
   plugins/FreeSwitchTitle/titledata/title.yml
   plugins/FreeSwitchTitle/gui.yml
   plugins/FreeSwitchTitle/particles.yml
   plugins/FreeSwitchTitle/effects.yml
   ```

5. 重载：

   ```text
   /fst reload
   ```

6. 检查配置：

   ```text
   /fst validate
   ```

## 命令

主命令：`/freeswitchtitle`  
简写：`/fst`

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/fst open [category]` | `freeswitchtitle.command.open` | 打开自己的称号列表 |
| `/fst shop [category]` | `freeswitchtitle.command.shop` | 打开称号商城 |
| `/fst collection [category]` | `freeswitchtitle.command.collection` | 打开称号图鉴 |
| `/fst reset` | `freeswitchtitle.command.reset` | 卸下当前称号 |
| `/fst balance [player]` | `freeswitchtitle.command.balance` | 查看余额，查看他人需 `freeswitchtitle.command.balance.other` |
| `/fst renew <uid>` | `freeswitchtitle.command.renew` | 续费限时称号 |
| `/fst buy <uid>` | `freeswitchtitle.command.buy` | 直接购买或领取称号 |
| `/fst preview <uid>` | `freeswitchtitle.command.preview` | 预览称号粒子效果 |
| `/fst previewdisplay <uid>` | `freeswitchtitle.command.previewdisplay` | 预览称号头顶图片显示 |
| `/fst resourcepack` | `freeswitchtitle.command.resourcepack` | 查看图片称号资源包状态 |
| `/fst look <player> [category]` | `freeswitchtitle.command.look` | 查看其他玩家称号 |
| `/fst list [category]` | `freeswitchtitle.command.list` | 查看全部称号 |
| `/fst show <uid>` | `freeswitchtitle.command.show` | 查看指定称号详情 |
| `/fst validate` | `freeswitchtitle.command.validate` | 检查配置问题 |
| `/fst add <uid> [player]` | `freeswitchtitle.command.add` | 添加称号 |
| `/fst remove <uid> [player]` | `freeswitchtitle.command.remove` | 移除称号 |
| `/fst set <player> <uid>` | `freeswitchtitle.command.set` | 设置玩家当前称号 |
| `/fst clear <player>` | `freeswitchtitle.command.clear` | 清除玩家当前称号 |
| `/fst reload` | `freeswitchtitle.command.reload` | 重载配置和称号数据 |

## 称号配置示例

默认称号文件位于：

```text
plugins/FreeSwitchTitle/titledata/title.yml
```

示例：

```yaml
fly:
  title: '&r[&b飞行员&r]'
  material: FEATHER
  lore:
    - '&r恭喜你成为飞行员'
    - '&r点击佩戴这个称号'
    - '&r称号期限: {duration}'
  join-message: '&b飞行员 &f{0} &b加入了游戏'
  category: default
  rarity: common
  hidden: false
  duration: permanent
  shop:
    enable: true
    currency: VAULT
    available-from: ''
    available-until: ''
    vault-price: 1000.0
    points-price: 50
    permission: ''
  requirements:
    permissions: []
  permission:
    - essentials.fly
  display:
    enable: true
    image: vip.png
    text: '\uE001'
    y-offset: 2.55
    scale: 1.0
    shadow: false
    see-through: false
  effects:
    particle: angel-wings
    potion: speed
    attribute: swift
  actions:
    equip:
      - '[message] &a你佩戴了 {title}'
    unequip:
      - '[message] &c你卸下了 {title}'
    buy:
      - '[message] &a你购买了 {title}，价格: {price}'
    expire:
      - '[message] &c你的称号 {title} 已过期'
```

## 图片称号

图片称号通过 `TextDisplay` 显示在玩家头顶。图片本身通过自动生成资源包的字体 glyph 显示。

基本步骤：

1. 将 PNG 放入：

   ```text
   plugins/FreeSwitchTitle/images/vip.png
   ```

2. 在称号中配置：

   ```yaml
   display:
     enable: true
     image: vip.png
     y-offset: 2.55
     scale: 1.0
   ```

3. 执行：

   ```text
   /fst reload
   ```

4. 插件会生成：

   ```text
   plugins/FreeSwitchTitle/image-mapping.yml
   plugins/FreeSwitchTitle/resourcepack/
   plugins/FreeSwitchTitle/resourcepack.zip
   ```

   `image-mapping.yml` 会固定图片与 Unicode 字符映射，后续新增图片不会影响旧图片称号。

5. 可用 `/fst resourcepack` 查看资源包路径、SHA1、图片数量和映射文件路径。

6. 将 `resourcepack.zip` 配置为服务器资源包，或上传到资源包地址后配置 `server.properties`。

可以用 `/fst previewdisplay <uid>` 临时预览某个称号的头顶图片显示，便于调试高度和缩放。

注意：插件当前只生成资源包，不自动开 HTTP 服务下发资源包。玩家必须加载该资源包才能看到图片，否则会看到占位字符。

更多说明见：[`docs/RESOURCE_PACK.md`](docs/RESOURCE_PACK.md)。

## 效果配置

### 粒子

粒子预设位于：

```text
plugins/FreeSwitchTitle/particles.yml
```

称号中引用：

```yaml
effects:
  particle: angel-wings
```

支持形状：`HALO`、`RING`、`AURA`、`SPIRAL`、`TRAIL`、`WINGS`。

### 药水与属性

效果预设位于：

```text
plugins/FreeSwitchTitle/effects.yml
```

称号中引用：

```yaml
effects:
  potion: speed
  attribute: swift
```

药水和属性会在佩戴/上线时应用，并在切换、卸下、过期、退出、插件关闭时清理。

## PlaceholderAPI

| 变量 | 说明 |
| --- | --- |
| `%fst_title%` | 当前称号；没有称号时返回默认前缀 |
| `%fst_title_show%` | 当前称号；没有称号时返回 `无` |
| `%fst_title_uid%` | 当前称号 UID |
| `%fst_title_count%` | 玩家拥有称号数量 |
| `%fst_title_collected%` | 图鉴已收集数量 |
| `%fst_title_total%` | 图鉴总数量 |
| `%fst_title_progress%` | 图鉴收集进度百分比 |

## 动作系统

支持动作前缀：

| 前缀 | 说明 |
| --- | --- |
| `[console]` | 控制台执行命令 |
| `[player]` | 玩家执行命令 |
| `[op]` | 临时 OP 执行命令 |
| `[message]` | 给玩家发送消息 |
| `[broadcast]` | 全服广播 |
| `[actionbar]` | 发送动作栏 |
| `[title]` | 发送屏幕标题，格式 `主标题;副标题` |
| `[sound]` | 播放音效，格式 `声音;音量;音调` |
| `[delay]` | 延迟执行后续动作，支持 `20t`、`2s`、`500ms`、`1m` |

动作节点：

- `actions.obtain`
- `actions.equip`
- `actions.unequip`
- `actions.buy`
- `actions.expire`
- `actions.remove`
- `actions.reset`

变量：

- `{player}` / `%player%`
- `{uuid}` / `%uuid%`
- `{title}` / `%title%`
- `{uid}` / `{title_id}` / `%title_id%`
- `{duration}` / `%duration%`
- `{expire}` / `%expire%`
- `{price}` / `%price%`

## 文档

- [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md)：完整使用指南。
- [`docs/RESOURCE_PACK.md`](docs/RESOURCE_PACK.md)：图片称号与资源包说明。
- [`CURRENT_STATUS.md`](CURRENT_STATUS.md)：当前开发状态。
- [`MILESTONES.md`](MILESTONES.md)：开发路线规划。

## 构建

```bash
./gradlew.bat build
```

成功后产物位于：

```text
build/libs/FreeSwitchTitle-2.0.0-SNAPSHOT.jar
```

## 开发者说明

公开 API 入口：

```kotlin
top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
```

事件包括：

- `TitleGrantEvent`
- `TitleRemoveEvent`
- `TitleEquipEvent`
- `TitleUnequipEvent`
- `TitleResetEvent`
- `TitleExpireEvent`
- `TitleBuyEvent`

## License

This project is licensed under the Apache License 2.0. See [`LICENSE`](LICENSE) for details.
