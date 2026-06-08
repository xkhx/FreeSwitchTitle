# FreeSwitchTitle 使用指南

本文档面向服主，说明 FreeSwitchTitle 的安装、配置、命令、称号效果、商城、图片称号和排错流程。

## 1. 安装

### 1.1 放置插件

将构建好的插件 jar 放入服务器：

```text
plugins/FreeSwitchTitle-2.0.0-SNAPSHOT.jar
```

启动服务器后，插件会生成配置目录：

```text
plugins/FreeSwitchTitle/
```

### 1.2 可选插件

按实际需要安装：

| 插件 | 用途 |
| --- | --- |
| PlaceholderAPI | 显示 `%fst_*%` 变量 |
| Vault + 经济插件 | 金币购买 |
| PlayerPoints | 点券购买 |
| LuckPerms | 权限发放/回收 |
| GroupManager | 命令式权限发放/回收 |

如果不需要经济或权限联动，可以关闭对应功能或使用 `FREE` / `NONE` 模式。

## 2. 配置文件说明

| 文件 | 说明 |
| --- | --- |
| `config.yml` | 数据库、商城、预览、图片称号、图鉴奖励、权限、聊天格式 |
| `gui.yml` | GUI 标题、布局、按钮、lore 模板 |
| `titledata/title.yml` | 称号定义 |
| `particles.yml` | 粒子预设 |
| `effects.yml` | 药水/属性预设 |
| `lang/zh_CN.yml` | 语言消息 |

插件启动和 `/fst reload` 时会自动补齐缺失配置节点，并在补齐前备份原文件。

## 3. 基础称号配置

```yaml
fly:
  title: '&r[&b飞行员&r]'
  material: FEATHER
  lore:
    - '&r恭喜你成为飞行员'
    - '&r点击佩戴这个称号'
    - '&r称号期限: {duration}'
  category: default
  rarity: common
  hidden: false
  duration: permanent
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `title` | 称号显示文本 |
| `material` | GUI 中显示的物品材质 |
| `lore` | GUI 中称号物品的基础 lore |
| `join-message` | 佩戴该称号进服时的进服消息，可选 |
| `category` | 分类名，用于 GUI 和命令过滤 |
| `rarity` | 稀有度：`common`、`rare`、`epic`、`legendary`、`limited` |
| `hidden` | 是否隐藏；隐藏称号未拥有时不在图鉴显示 |
| `duration` | 有效期 |

## 4. 有效期

永久称号：

```yaml
duration: permanent
```

也支持：

- `forever`
- `永久`
- `0`

限时称号：

```yaml
duration: 7d
```

支持单位：

| 单位 | 说明 |
| --- | --- |
| `ms` | 毫秒 |
| `s` | 秒 |
| `m` | 分钟 |
| `h` | 小时 |
| `d` | 天 |

过期行为：

- 从玩家拥有列表移除。
- 如果当前正在佩戴，会自动卸下。
- 回收由本插件发放的权限。
- 清理粒子、药水、属性、图片称号显示。
- 执行 `actions.expire`。

## 5. 商城配置

```yaml
shop:
  enable: true
  currency: VAULT
  available-from: ''
  available-until: ''
  vault-price: 1000.0
  points-price: 50
  permission: ''
```

货币类型：

| 类型 | 说明 |
| --- | --- |
| `VAULT` | 使用 Vault 金币 |
| `PLAYER_POINTS` | 使用 PlayerPoints 点券 |
| `BOTH` | 同时扣金币和点券 |
| `FREE` | 免费领取 |

限时上架：

```yaml
available-from: '2026-06-01 00:00:00'
available-until: '2026-06-30 23:59:59'
```

为空表示不限制。

购买条件：

```yaml
requirements:
  permissions:
    - group.vip
    - activity.summer
```

玩家必须拥有列表中的全部权限才能购买。

## 6. 权限发放

称号佩戴时可临时授予权限：

```yaml
permission:
  - essentials.fly
  - essentials.tp
```

`config.yml` 中配置权限模式：

```yaml
permission:
  mode: LUCKPERMS
  safe-check: true
  record-key: fst_granted_permissions
```

模式：

| 模式 | 说明 |
| --- | --- |
| `LUCKPERMS` | 使用 LuckPerms API |
| `GROUP_MANAGER` | 执行 GroupManager 命令 |
| `NONE` | 不处理权限 |

`safe-check: true` 会尽量避免卸下称号时误删玩家原本拥有的权限。

## 7. 粒子、药水、属性效果

### 7.1 粒子

称号中引用粒子预设：

```yaml
effects:
  particle: angel-wings
```

预设在 `particles.yml` 中配置。

支持形状：

- `HALO`
- `RING`
- `AURA`
- `SPIRAL`
- `TRAIL`
- `WINGS`

### 7.2 药水与属性

```yaml
effects:
  potion: speed
  attribute: swift
```

预设在 `effects.yml` 中配置。

生命周期：

- 佩戴 / 上线时应用。
- 切换 / 卸下 / 过期 / 退出 / 插件关闭时清理。
- 药水清理时会恢复玩家原本已有的同类药水效果。

## 8. 图片称号

图片称号会显示在玩家头顶。

### 8.1 自动图片模式

1. 放入图片：

   ```text
   plugins/FreeSwitchTitle/images/vip.png
   ```

2. 配置称号：

   ```yaml
   display:
     enable: true
     image: vip.png
     y-offset: 2.55
     scale: 1.0
   ```

3. 重载：

   ```text
   /fst reload
   ```

4. 插件生成：

   ```text
   plugins/FreeSwitchTitle/image-mapping.yml
   plugins/FreeSwitchTitle/resourcepack.zip
   ```

   `image-mapping.yml` 会固定图片、字符、height、ascent，方便后续稳定维护。

5. 将资源包配置给服务器或上传到资源包地址。

### 8.2 手动字符模式

如果你已经有自己的资源包，可以直接写：

```yaml
display:
  enable: true
  text: '\uE001'
```

插件会把 `\uE001` 解析为真实 Unicode 字符。

更多说明见 [`RESOURCE_PACK.md`](RESOURCE_PACK.md)。

## 9. 动作系统

示例：

```yaml
actions:
  equip:
    - '[message] &a你佩戴了 {title}'
    - '[sound] ENTITY_PLAYER_LEVELUP;1;1'
  buy:
    - '[broadcast] &f{player} &a购买了 {title}'
    - '[delay] 2s'
    - '[message] &a感谢购买！'
```

动作前缀：

| 前缀 | 说明 |
| --- | --- |
| `[console]` | 控制台执行命令 |
| `[player]` | 玩家执行命令 |
| `[op]` | 临时 OP 执行命令 |
| `[message]` | 私聊玩家 |
| `[broadcast]` | 全服广播 |
| `[actionbar]` | 动作栏 |
| `[title]` | 屏幕标题，格式 `主标题;副标题` |
| `[sound]` | 音效，格式 `声音;音量;音调` |
| `[delay]` | 延迟执行后续动作 |

动作节点：

- `obtain`：获得称号时。
- `equip`：佩戴称号时。
- `unequip`：卸下称号时。
- `buy`：购买成功时。
- `expire`：过期时。
- `remove`：移除拥有权时。
- `reset`：清空当前称号时。

变量：

| 变量 | 说明 |
| --- | --- |
| `{player}` / `%player%` | 玩家名 |
| `{uuid}` / `%uuid%` | UUID |
| `{title}` / `%title%` | 称号显示文本 |
| `{uid}` / `{title_id}` / `%title_id%` | 称号 UID |
| `{duration}` / `%duration%` | 配置有效期 |
| `{expire}` / `%expire%` | 剩余时间 |
| `{price}` / `%price%` | 价格文本 |

## 10. 图鉴与收藏奖励

打开图鉴：

```text
/fst collection
```

配置收藏奖励：

```yaml
collection:
  rewards:
    5:
      - collector
```

玩家拥有 5 个称号时自动发放 `collector` 称号。奖励只会领取一次。

## 11. PlaceholderAPI

| 变量 | 说明 |
| --- | --- |
| `%fst_title%` | 当前称号，没有则返回默认前缀 |
| `%fst_title_show%` | 当前称号，没有则返回 `无` |
| `%fst_title_uid%` | 当前称号 UID |
| `%fst_title_count%` | 拥有称号数量 |
| `%fst_title_collected%` | 已收集数量 |
| `%fst_title_total%` | 图鉴总数量 |
| `%fst_title_progress%` | 收集进度 |

## 12. 常用命令流程

### 给玩家称号

```text
/fst add fly Steve
```

### 设置玩家当前称号

```text
/fst set Steve fly
```

### 移除玩家称号

```text
/fst remove fly Steve
```

### 检查配置

```text
/fst validate
```

### 预览图片称号

```text
/fst previewdisplay fly
```

### 查看资源包状态

```text
/fst resourcepack
```

### 重载

```text
/fst reload
```

## 13. 排错

### 图片称号显示为方块或乱码

原因：玩家没有加载资源包，或资源包不是插件生成的最新版本。

处理：

1. 确认 `resourcepack.zip` 已生成。
2. 确认服务器下发/配置的是最新 zip。
3. 确认客户端允许服务器资源包。
4. 执行 `/fst reload` 后重新进服测试。

### `/fst validate` 报图片不存在

检查图片是否放在：

```text
plugins/FreeSwitchTitle/images/
```

文件名只允许：

```text
A-Z a-z 0-9 _ - .
```

并且必须是 `.png`。

### 购买失败

用 `/fst balance` 检查余额，并确认：

- Vault / PlayerPoints 是否安装。
- `shop.currency` 是否正确。
- 玩家是否满足 `shop.permission` 和 `requirements.permissions`。
- 称号是否在上架时间内。

### 权限没有发放

检查：

```yaml
permission:
  mode: LUCKPERMS
```

并确认对应权限插件已安装。

## 14. 推荐验证清单

上线前建议至少测试：

- `/fst open`
- `/fst shop`
- `/fst collection`
- `/fst buy <uid>`
- `/fst set <player> <uid>`
- `/fst reset`
- `/fst validate`
- `/fst previewdisplay <uid>`
- `/fst resourcepack`
- 限时称号过期
- 图片称号资源包加载
- 粒子/药水/属性清理
- 插件 `/fst reload` 后在线玩家效果刷新
