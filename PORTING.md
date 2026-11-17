# Minecraft 1.21.11 移植说明

本仓库是 [sinkillerj/ProjectE](https://github.com/sinkillerj/ProjectE) 在 **Thatgfsj-minecraft** 组织下的 fork，维护一条 **Minecraft 1.21.11 / NeoForge 21.11** 构建线。

## 目标环境

| 项 | 值 |
|---|---|
| Minecraft | 1.21.11 |
| Mod Loader | NeoForge 21.11.48（neo_version_range=[21.11.48,)） |
| 映射 | Mojang 官方映射（Mojmap） |
| Java | 21（toolchain） |
| Gradle | 9.5.1 |
| 构建插件 | ModDevGradle 2.0.148、Shadow 9.6.1 |
| 可选联动 | JEI 27.54.0.116、Curios 14.0.0+1.21.11、Jade 21.1.7+neoforge |

## 移植来源与致谢（MIT）

- 上游基线：sinkillerj/ProjectE 分支 mc1.21.1（提交 f432b0c6，NeoForge 21.1）。
- 主体代码迁移来自上游 PR [sinkillerj/ProjectE#2488](https://github.com/sinkillerj/ProjectE/pull/2488)（作者 JamesClarke7283，MIT）。该 PR 把 ProjectE 从 1.21.1 迁移到 MC 26.3 / NeoForge 26.3，涵盖 NeoForge 事务化 transfer API 库存、渲染 render state 化、护甲装备资产、物品模型数据驱动、配方 codec、datagen 重写等；截至 2026-11-18 该 PR 仍是 **open 状态的 draft**，作者自述尚未做游戏内手测。
- 该 PR 声明其 API 迁移参考了 MIT 许可的 [Yaskulsky/Equivox](https://github.com/Yaskulsky/Equivox)；详细归属见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
- 本分支在此基础上做 **26.3 → 1.21.11 的回退适配**：Java 25→21、ModDevGradle/NeoForge 版本，以及 26.3 独有 API（ItemInstance / ItemStackTemplate / ContainerInput / GuiGraphicsExtractor / renderer.state.level / VersionChecker / SignTextSlot / BonemealSource / world.clock / EntityTypes 等）回退为 1.21.11 等价物。

## 构建与运行

    ./gradlew build          # 编译 + 打包（产物在 build/libs；单元测试默认跳过，见下）
    ./gradlew generateData   # 重新生成客户端资源 + 服务端数据（合并输出到 src/datagen/generated）
    ./gradlew test -PrunProjecteTests   # 运行单元测试
    ./gradlew runServer      # 开发环境专用服务器
    ./gradlew runClient      # 开发环境客户端

需要 JDK 21（可用 JAVA_HOME 指向 21；构建脚本使用 toolchain 21）。首次构建需要能访问 maven.neoforged.net 等仓库。

## 与上游集成模块的差异

- 保留编译并适配：JEI、Curios、Jade。
- 源码保留但**排除编译**：CraftTweaker（上游最新仍为 1.21.1）、EMI（上游最新仍为 1.21.1）、WTHIT（未确认 1.21.11 构建）、The One Probe（已有 1.21.11_neo-13.0.0-1 构建，但集成代码尚未针对 1.21.11 适配）。集成代码在 `build.gradle` 的 sourceSets 中排除；恢复启用时需同时恢复对应 plugin 描述文件。
- 因 WTHIT 集成被排除，已删除 `src/main/resources/wthit_plugins.json`，避免安装 WTHIT 时加载到不存在的插件类。
- 可选依赖版本见 `gradle.properties`。

## 验证状态（2026-11-18）

- [x] `./gradlew build`：main/api/datagen/test 全部编译，shadow jar 产出
- [x] `./gradlew generateData`：客户端资源 + 服务端数据两个 run 均成功并合并（407 个资产文件、467 个数据文件、108 个物品模型定义）
- [x] 专用服务器冒烟：启动到 Done（0.84s），加载 1618 个配方与全部 ProjectE EMC mapper，无 ProjectE 异常（仅网络相关的 UUID/更新检查线程因本机网络失败）
- [x] 客户端开发环境启动：资源重载与全部图集创建完成，无 missing model / missing texture 警告；进程保持到超时终止
- [x] 单元测试：`-PrunProjecteTests` 下 9 个测试类、**92 个测试全部通过**（0 失败 0 错误）
- [ ] 游戏内人工验收：转化桌 / EMC HUD / 炼金袋 / 收集器 / 凝聚器 / 戒指 / 时间怀表 / 世界嬗变
- [ ] 旧存档迁移实测（1.21.1 → 1.21.11）

## 数据生成说明（1.21.11 拆分）

1.21.11 的 datagen 拆成客户端数据与服务端数据两个 run（MDG 2.x 中 `runData` 是 clientData，`runServerData` 是独立的 serverData run；`GatherDataEvent` 改为 Client/Server 事件）。原版 HashCache 会清掉「不属于当前 run」的产物，因此两个 run **不能共享同一个输出目录**：

- `runData`（clientData）→ `src/datagen/generated`
- `runServerData`（serverData）→ `build/generated-server-data`
- `generateData` = 跑完两个 run 后把服务端 `data/` 合并进 `src/datagen/generated/data`（`mergeServerData` 必须在两个 run 之后执行，两个 run 之间没有顺序要求）

提交生成产物时请用 `./gradlew generateData`，不要单独跑 `runData`（会清掉 data/）。注意 `mergeServerData` 只复制不删除：若生成器删除或改名文件，旧 JSON 会残留在 `src/datagen/generated/data`，需要手工清理或检查 git diff。

## 相对 26.3 基座做出的 1.21.11 适配修复

- 收集器压缩崩溃：26.3 用 `RangedResourceHandler` 组合升级槽，但它不实现 `IndexModifier`，压缩时会抛异常；本分支改用 `SlicedItemStacksHandler`（可写切片）实现。
- 液体容器注液（药锅/含水方块）恢复：1.21.11 仍有 `LiquidBlockContainer`（`canPlaceLiquid` 签名为 `LivingEntity`），已恢复校验与 `placeLiquid` 分支。
- 天气设置参数顺序：1.21.11 `setWeatherParameters(clearWeatherTime, weatherTime, raining, thundering)`，清空 clearWeatherTime，否则会变成强制晴天。
- 炼金袋旧存档兼容：NeoForge 21.11 只接受 `MapCodec` 序列化 attachment，1.21.1 的布局是裸的 dye→inventory map；改用自定义 `IAttachmentSerializer` 读取两种布局、按旧布局写回，避免升级后炼金袋内容丢失。
- 转化桌消耗槽递归崩溃：1.21.11 的 `StackCopySlot.setChanged()` 语义变为「把缓存栈写回」，在 `setStackCopy` 内再调 `setChanged()` 会造成无限递归并重复加 EMC；已移除该调用。
- 物品模型补全：基座/转化桌物品模型改为引用方块模型；熔炉点燃模型补 `_front_on` 贴图；箱子模型补 `particle` 槽；墙火把补掉落表（掉落站立火把）与语言键。
- JEI 配方界面判定类名修正为 `mezz.jei.gui.recipes.RecipesGui`（原先的 `mezz.jei.library.gui.recipes.RecipesGui` 在 JEI 27 中不存在）。

## 已知偏差与遗留问题

- 护甲通过 `Item.Properties#humanoidArmor` 定义，会附带 ENCHANTABLE 组件，因此 PE 护甲可在附魔台附魔（1.21.1 上游是禁附魔）。后续可改为手工设置 EQUIPPABLE + ATTRIBUTE_MODIFIERS 恢复严格行为。
- 客户端特效迁移：1.21.11 的 `Item#inventoryTick` 只在服务端调用，GemFeet 的客户端移动处理等迁移到 `PlayerTickEvent` 的客户端分支，触发时机可能与旧版略有差异。
- 哲人石高亮 / 世界嬗变轮廓改为线框渲染（跟随 26.3 基座实现）。
- NovaExplosion 的伤害源与方块掉落逻辑沿用 26.3 实现（`mobAttack`/`generic`、`DESTROY_WITH_DECAY`），与 1.21.1 的爆炸归属 / TNT 掉落规则不同，待后续对齐。
- `PEItemStacksHandler#getStackInSlot` 返回内部 live `ItemStack`，绕过 transfer 事务快照；当前调用点都在 tick/UI 线程、未接入事务，后续建议改为返回快照或强制通过 transfer API 访问。
- `mergeServerData` 只复制不删除，生成器删/改名可能残留幽灵 JSON（见 datagen 说明）。
- `src/datagen/java/.../PERecipeProvider.java` 目前 1121 行，超出仓库「单文件 ≤1000 行」约定；该文件在上游 1.21.1 基线即 1128 行，拆分方案待定（会先征求 owner 意见）。
- 不再生成 `pack.mcmeta`：1.21.11 对资源包（format 75）与数据包（format 94）分别校验，mod 资源包用单一 `pack.mcmeta` 无法同时满足两侧规则（资源侧 min_format>64 时不允许再带 supported_formats，数据侧 min<=81 时必须带），因此交给 NeoForge 的 mod 资源包元数据，不在 jar 内提供该文件。

## 目录说明

- src/api：ProjectE 公共 API（独立 source set）
- src/main：模组主体
- src/datagen：数据生成器与生成产物（src/datagen/generated 会打进 jar）
- src/test：单元测试（含 transfer API 事务回滚、库存过滤等）

## 许可

MIT，与上游和 PR #2488 一致。本仓库保留上游版权声明与许可文本；第三方归属见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
