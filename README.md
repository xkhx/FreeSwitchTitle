# FreeSwitchTitle

FreeSwitchTitle 是一个基于 TabooLib 的 Bukkit 称号插件, 支持称号佩戴、GUI 列表、称号商城、Vault/PlayerPoints 购买、PlaceholderAPI 显示、权限授予和称号动作。

## 功能状态

| 模块 | 状态 | 说明 |
| --- | --- | --- |
| 基础称号 | 已完成 | 称号加载、拥有、佩戴、取下、查看 |
| GUI | 已完成 | 我的称号、商城、全部称号、查看他人、配置化 lore |
| 商城 | 已完成 | 支持 Vault、PlayerPoints、混合价格 |
| 权限 | 已完成 | 支持 LuckPerms、GroupManager、NONE 模式 |
| 动作 | 已完成 | 支持佩戴、卸下、购买时执行动作 |
| PAPI | 已完成 | 支持 `%fst_title%`、`%fst_title_show%` |
| 粒子效果 | 未完成 | `particle` 字段暂未落地 |
| 属性加成 | 未完成 | `attribute` 字段暂未落地 |
| 称号期限 | 已完成 | 支持永久/限时称号、登录与在线周期过期清理 |

## 命令

主命令: `/freeswitchtitle`, 简写: `/fst`

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/fst open` | `freeswitchtitle.command.open` | 打开自己的称号列表 |
| `/fst shop` | `freeswitchtitle.command.shop` | 打开称号商城 |
| `/fst reset` | `freeswitchtitle.command.reset` | 取下当前称号 |
| `/fst buy <uid>` | `freeswitchtitle.command.buy` | 直接购买指定称号 |
| `/fst look <player>` | `freeswitchtitle.command.look` | 查看玩家拥有的称号 |
| `/fst list` | `freeswitchtitle.command.list` | 查看全部称号 |
| `/fst show <uid>` | `freeswitchtitle.command.show` | 查看指定称号信息 |
| `/fst add <uid> [player]` | `freeswitchtitle.command.add` | 给自己或指定玩家添加称号 |
| `/fst remove <uid> [player]` | `freeswitchtitle.command.remove` | 移除自己或指定玩家的称号 |
| `/fst set <player> <uid>` | `freeswitchtitle.command.set` | 给指定在线玩家佩戴称号 |
| `/fst clear <player>` | `freeswitchtitle.command.clear` | 清空指定在线玩家当前称号 |
| `/fst reload` | `freeswitchtitle.command.reload` | 重载配置与称号数据 |

## 称号配置

默认称号文件位于插件数据目录的 `titledata/title.yml`。

```yaml
fly:
  title: '&r[&b飞行员&r]'
  material: STONE
  lore:
    - '&r恭喜你成为飞行员'
    - '&r点击佩戴这个称号'
    - '&r称号期限: {duration}'
  join-message: "飞行员 {0} 加入了游戏"
  duration: permanent
  shop:
    enable: true
    vault-price: 1000.0
    points-price: 10
    permission: ''
  permission:
    - essentials.fly
    - essentials.tp
  actions:
    equip:
      - '[console] say {player} 佩戴了 {title}'
      - '[message] &a你佩戴了 {title}'
    unequip:
      - '[console] say {player} 卸下了 {title}'
    buy:
      - '[console] say {player} 购买了 {title}'
```

- `shop.enable`: 是否上架到商城。
- `shop.vault-price`: Vault 金币价格。
- `shop.points-price`: PlayerPoints 点券价格。
- `shop.permission`: 购买该称号需要的权限。
- `permission`: 玩家佩戴该称号时授予的权限。
- `duration`: 称号有效期，`permanent` 表示永久，也支持 `7d`、`12h`、`30m`、`10s`、`1000ms`。
- `actions`: 在佩戴、卸下、购买时执行的动作。

动作格式:

| 类型 | 说明 |
| --- | --- |
| `[console] command` | 以控制台执行命令 |
| `[player] command` | 以玩家身份执行命令 |
| `[op] command` | 临时 OP 执行命令 |
| `[message] text` | 给玩家发送消息 |

可用变量:

| 变量 | 说明 |
| --- | --- |
| `{player}` | 玩家名 |
| `{uuid}` | 玩家 UUID |
| `{title}` | 称号显示文本 |
| `{uid}` | 称号 UID |

## GUI lore 模板

`gui.yml` 的 `gui.lore` 可配置不同界面和状态下追加到称号物品上的 lore。旧版 `gui.status` 仍作为兼容回退。

可用变量:

| 变量 | 说明 |
| --- | --- |
| `{title}` | 称号显示文本 |
| `{uid}` | 称号 UID |
| `{duration}` | 称号配置有效期 |
| `{expire}` | 玩家拥有该称号时的剩余时间 |
| `{vault_price}` | Vault 价格 |
| `{points_price}` | PlayerPoints 价格 |

## PlaceholderAPI

| 变量 | 说明 |
| --- | --- |
| `%fst_title%` | 获取玩家当前称号, 没有称号时返回默认前缀 |
| `%fst_title_show%` | 获取玩家当前称号, 没有称号时返回 `无` |

## 构建发行版本

发行版本用于正常使用, 不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体, 用于开发者使用, 但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 -PDeleteCode 表示移除所有逻辑代码以减少体积。
