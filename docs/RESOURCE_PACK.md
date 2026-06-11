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
    pack-format: 22
    start-codepoint: '0xE001'
    default-height: 16
    default-ascent: 8
```

图片来源：

```text
plugins/FreeSwitchTitle/images/
```

映射与资源包输出：

```text
plugins/FreeSwitchTitle/image-mapping.yml
plugins/FreeSwitchTitle/resourcepack/
plugins/FreeSwitchTitle/resourcepack.zip
```

`image-mapping.yml` 用于固定每张图片对应的字符、字体高度和基线，避免新增图片后旧称号错图。

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

## 3. 字符映射规则

插件第一次扫描新图片时，会从 `start-codepoint` 开始自动分配字符，并写入：

```text
plugins/FreeSwitchTitle/image-mapping.yml
```

示例：

```yaml
images:
  vip:
    file: vip.png
    char: '\uE001'
    height: 16
    ascent: 8
  king:
    file: king.png
    char: '\uE002'
    height: 32
    ascent: 16
```

后续 reload 时会优先读取 `image-mapping.yml`，因此：

- 新增图片不会改变旧图片的字符。
- 删除图片不会导致旧图片字符被立刻复用。
- 可以手动调整某张图片的 `char`、`height`、`ascent`。
- 修改图片内容时保持文件名不变即可沿用原映射。

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

全局默认值在 `config.yml` 中：

```yaml
resource-pack:
  default-height: 16
  default-ascent: 8
```

每张图片生成到 `image-mapping.yml` 后，可以单独调整：

```yaml
images:
  vip:
    file: vip.png
    char: '\uE001'
    height: 16
    ascent: 8
```

| 参数 | 说明 |
| --- | --- |
| `height` | 图片在字体中的高度 |
| `ascent` | 图片相对基线的上升值 |

调试建议：

- 图片太小：增大该图片的 `height` 或称号 `display.scale`。
- 图片位置偏低：增大该图片的 `ascent` 或 `display.y-offset`。
- 图片位置偏高：减小该图片的 `ascent` 或 `display.y-offset`。
- 修改后执行 `/fst reload`，并重新上传/下发新的 `resourcepack.zip`。

## 7. 资源包 pack_format

默认生成的 `pack.mcmeta` 使用适配 1.20.4 的 `pack_format: 22`。

如果你要支持其他 Minecraft 版本，可以在 `config.yml` 中调整：

```yaml
display:
  resource-pack:
    pack-format: 22
```

修改后执行 `/fst reload` 重新生成资源包。

## 8. 测试用例

下面几个用例可以直接放到 `plugins/FreeSwitchTitle/titledata/title.yml` 中测试。

### 8.1 正常自动图片

准备文件：

```text
plugins/FreeSwitchTitle/images/test_auto.png
```

配置：

```yaml
test-auto-image:
  title: '&a[自动图片]'
  material: EMERALD
  category: test
  display:
    enable: true
    image: test_auto.png
    y-offset: 2.55
    scale: 1.0
```

预期：

- `/fst reload` 后自动生成映射。
- `/fst validate` 不报该称号错误。
- `/fst show test-auto-image` 显示头顶图片来源为 `test_auto.png`。
- `/fst previewdisplay test-auto-image` 能看到图片。

### 8.2 图片不存在但使用手动字符回退

配置：

```yaml
test-image-fallback:
  title: '&e[图片回退]'
  material: PAPER
  category: test
  display:
    enable: true
    image: missing.png
    text: '\uE001'
    y-offset: 2.55
    scale: 1.0
```

预期：

- `/fst validate` 给出 warning，不是 error。
- 实际显示使用 `text` 中的字符。
- 如果客户端资源包里 `\uE001` 有图片映射，则能显示对应图片。

### 8.3 纯手动字符模式

配置：

```yaml
test-manual-text:
  title: '&b[手动字符]'
  material: NAME_TAG
  category: test
  display:
    enable: true
    text: 'U+E001'
    y-offset: 2.55
    scale: 1.0
```

也可以写成：

```yaml
text: '0xE001'
```

或：

```yaml
text: '\uE001'
```

预期：

- 三种写法都能解析成同一个 glyph 字符。
- `/fst validate` 不要求图片文件存在。

### 8.4 自动资源包关闭时的手动回退

先临时关闭自动资源包：

```yaml
display:
  resource-pack:
    enable: false
```

称号配置：

```yaml
test-resourcepack-disabled-fallback:
  title: '&d[资源包关闭回退]'
  material: BOOK
  category: test
  display:
    enable: true
    image: test_auto.png
    text: '\uE001'
    y-offset: 2.55
    scale: 1.0
```

预期：

- `/fst validate` 给出 warning：`image` 会被忽略，使用 `text` 回退。
- `/fst resourcepack` 显示自动资源包关闭。

### 8.5 自动资源包关闭且没有手动字符

配置：

```yaml
test-resourcepack-disabled-error:
  title: '&c[资源包关闭错误]'
  material: BARRIER
  category: test
  display:
    enable: true
    image: test_auto.png
```

预期：

- `/fst validate` 给出 error。
- 该称号不会有可显示的头顶图片字符。

### 8.6 非法图片文件名

配置：

```yaml
test-invalid-image-name:
  title: '&c[非法文件名]'
  material: REDSTONE
  category: test
  display:
    enable: true
    image: '测试图片.png'
```

预期：

- `/fst validate` 给出 error。
- 控制台 reload 时也会跳过非法图片文件名。

### 8.7 输出目录保护

临时把资源包输出目录改成危险值：

```yaml
display:
  resource-pack:
    output-folder: '..'
```

预期：

- `/fst reload` 不会删除插件数据目录外的目录。
- 控制台输出 warning。
- `/fst resourcepack` 显示已回退到安全目录。

测试后请改回：

```yaml
display:
  resource-pack:
    output-folder: resourcepack
```

### 8.8 测试命令顺序

每次修改配置后建议按顺序执行：

```text
/fst reload
/fst resourcepack
/fst validate
/fst show <测试称号 UID>
/fst previewdisplay <测试称号 UID>
```

如果要测试实际佩戴显示，可以执行：

```text
/fst add <测试称号 UID>
/fst set <你的玩家名> <测试称号 UID>
```

## 9. 常见问题

### 9.1 玩家看不到图片

可以先执行：

```text
/fst resourcepack
/fst previewdisplay <uid>
```

检查：

1. 玩家是否接受了服务器资源包。
2. `server.properties` 中资源包 URL 是否可以被玩家访问。
3. SHA1 是否和当前 zip 匹配。
4. `/fst reload` 后是否重新上传了新的 zip。
5. 称号是否配置了 `display.enable: true`。
6. 玩家是否正在佩戴该称号。

### 9.2 图片显示成方块

通常是资源包没有加载成功，或资源包不是插件生成的最新版本。

### 9.3 `/fst validate` 报图片不存在

确认图片路径：

```text
plugins/FreeSwitchTitle/images/图片名.png
```

确认文件名合法，且后缀是 `.png`。

### 9.4 如何避免新增图片后旧称号错图

当前版本会把映射固定写入 `image-mapping.yml`，正常新增图片不会改变旧图片字符。

建议：

- 不要删除旧图片在 `image-mapping.yml` 中的映射，除非确认不再使用。
- 修改图片外观时直接覆盖原 PNG 文件。
- 如果重命名图片，也同步修改 `image-mapping.yml` 中对应条目的 `file`。

## 10. 后续可扩展方向

当前版本不内置 HTTP 服务，也不主动下发资源包。

后续可选扩展：

- 内置 HTTP 托管 `resourcepack.zip`。
- 玩家进服自动调用 Bukkit 资源包 API 下发。
- 为每张图片增加更多字体参数，例如左右间距。
- 支持多图组合称号。
