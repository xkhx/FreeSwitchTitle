# 图片称号与资源包说明

FreeSwitchTitle 的图片称号基于 Minecraft 资源包字体机制实现。

插件负责：

1. 扫描图片文件。
2. 给图片分配私有 Unicode 字符。
3. 生成资源包目录和 `resourcepack.zip`。
4. 在玩家头顶创建 `TextDisplay` 显示对应字符。

客户端负责：

1. 加载资源包。
2. 将该字符渲染成图片。

如果客户端没有加载资源包，就无法看到图片，只会看到占位字符或方块。

## 1. 目录结构

默认配置：

```yaml
display:
  resource-pack:
    enable: true
    image-folder: images
    output-folder: resourcepack
    namespace: freeswitchtitle
    start-codepoint: '0xE001'
    default-height: 16
    default-ascent: 8
```

图片来源：

```text
plugins/FreeSwitchTitle/images/
```

资源包输出：

```text
plugins/FreeSwitchTitle/resourcepack/
plugins/FreeSwitchTitle/resourcepack.zip
```

生成后的资源包结构大致为：

```text
resourcepack/
  pack.mcmeta
  assets/
    minecraft/
      font/
        default.json
    freeswitchtitle/
      textures/
        title/
          vip.png
          king.png
```

## 2. 使用步骤

### 2.1 放入图片

```text
plugins/FreeSwitchTitle/images/vip.png
```

建议图片：

- 格式：PNG
- 背景：透明
- 推荐尺寸：`16x16`、`32x16`、`64x16`、`64x32`
- 文件名：只使用字母、数字、下划线、短横线、点号

合法示例：

```text
vip.png
king-title.png
season_01.png
```

非法示例：

```text
贵族.png
vip title.png
vip.jpg
```

### 2.2 配置称号

```yaml
vip:
  title: '&6[VIP]'
  material: GOLD_INGOT
  display:
    enable: true
    image: vip.png
    y-offset: 2.55
    scale: 1.0
```

### 2.3 重新加载

```text
/fst reload
```

插件会输出类似：

```text
图片称号资源包已生成: plugins/FreeSwitchTitle/resourcepack.zip
图片称号资源包 SHA1: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 2.4 配置服务器资源包

将 `resourcepack.zip` 上传到公网可访问地址，然后配置 `server.properties`：

```properties
resource-pack=https://example.com/resourcepack.zip
resource-pack-sha1=这里填写插件输出的SHA1
```

玩家进服后，Minecraft 会提示加载服务器资源包。

> 不要把地址写成 `127.0.0.1`，除非玩家客户端和服务器在同一台机器。对普通玩家来说，`127.0.0.1` 指的是玩家自己的电脑。

## 3. 字符分配规则

插件从 `start-codepoint` 开始，按图片文件名排序分配字符：

```text
king.png -> \uE001
vip.png  -> \uE002
```

实际顺序取决于图片文件名排序。

如果你新增、删除或重命名图片，字符映射可能改变。因此建议：

- 上线后尽量不要频繁重命名图片。
- 如果需要稳定映射，可以保留旧图片名。
- 修改图片内容时保持文件名不变。

## 4. 自动图片模式与手动字符模式

### 自动图片模式

```yaml
display:
  enable: true
  image: vip.png
```

插件自动查找图片并解析为 glyph 字符。

### 手动字符模式

```yaml
display:
  enable: true
  text: '\uE001'
```

适用于你已经有自己的资源包，或想手动控制字体字符。

### 回退规则

如果同时配置：

```yaml
display:
  image: vip.png
  text: '\uE001'
```

优先级：

1. `image` 存在且已成功映射：使用图片 glyph。
2. `image` 不存在或未映射：回退到 `text`。
3. 两者都不可用：不显示头顶图片称号。

## 5. 显示参数

```yaml
display:
  enable: true
  image: vip.png
  y-offset: 2.55
  scale: 1.0
  shadow: false
  see-through: false
```

| 参数 | 说明 |
| --- | --- |
| `enable` | 是否启用头顶显示 |
| `image` | 自动资源包图片文件名 |
| `text` | 手动资源包字符 |
| `y-offset` | 显示高度，基于玩家脚下位置向上偏移 |
| `scale` | TextDisplay 缩放 |
| `shadow` | 是否显示文字阴影 |
| `see-through` | 是否隔墙可见 |

全局默认值在 `config.yml` 的 `display` 节点中配置。

## 6. 资源包字体参数

```yaml
resource-pack:
  default-height: 16
  default-ascent: 8
```

| 参数 | 说明 |
| --- | --- |
| `default-height` | 图片在字体中的高度 |
| `default-ascent` | 图片相对基线的上升值 |

调试建议：

- 图片太小：增大 `default-height` 或称号 `display.scale`。
- 图片位置偏低：增大 `default-ascent` 或 `display.y-offset`。
- 图片位置偏高：减小 `default-ascent` 或 `display.y-offset`。

## 7. 资源包 pack_format

当前生成的 `pack.mcmeta` 使用适配 1.20.4 的 `pack_format`。

如果你要支持其他 Minecraft 版本，可能需要手动调整资源包 `pack.mcmeta` 中的 `pack_format`。

## 8. 常见问题

### 8.1 玩家看不到图片

检查：

1. 玩家是否接受了服务器资源包。
2. `server.properties` 中资源包 URL 是否可以被玩家访问。
3. SHA1 是否和当前 zip 匹配。
4. `/fst reload` 后是否重新上传了新的 zip。
5. 称号是否配置了 `display.enable: true`。
6. 玩家是否正在佩戴该称号。

### 8.2 图片显示成方块

通常是资源包没有加载成功，或资源包不是插件生成的最新版本。

### 8.3 `/fst validate` 报图片不存在

确认图片路径：

```text
plugins/FreeSwitchTitle/images/图片名.png
```

确认文件名合法，且后缀是 `.png`。

### 8.4 新增图片后旧称号显示错图

因为自动分配字符按文件名排序，新增/删除图片可能改变字符顺序。建议上线后：

- 不删除旧图片。
- 不重命名旧图片。
- 修改图片时直接覆盖原文件。

如果需要完全稳定映射，后续可以扩展独立 `image-mapping.yml` 固定图片与字符关系。

## 9. 后续可扩展方向

当前版本不内置 HTTP 服务，也不主动下发资源包。

后续可选扩展：

- 内置 HTTP 托管 `resourcepack.zip`。
- 玩家进服自动调用 Bukkit 资源包 API 下发。
- 固定图片到 glyph 的映射文件，避免排序变化。
- 为每张图片单独配置 `height` / `ascent`。
- 支持多图组合称号。
