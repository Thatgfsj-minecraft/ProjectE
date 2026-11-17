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

- 上游基线：sinkillerj/ProjectE 分支 mc1.21.1（NeoForge 21.1）。
- 本移植的**主体代码迁移**来自上游 PR [sinkillerj/ProjectE#2488](https://github.com/sinkillerj/ProjectE/pull/2488)（作者 JamesClarke7283，MIT）：该项目把 ProjectE 从 1.21.1 完整移植到 MC 26.3 / NeoForge 26.3，包含 NeoForge 事务化 transfer API 库存、渲染提交化（render state）、护甲装备资产、物品模型数据驱动、配方 codec、datagen 重写等全部语义迁移。
- 该 PR 声明其 API 迁移参考了 MIT 许可的 [Yaskulsky/Equivox](https://github.com/Yaskulsky/Equivox)。
- 本分支在此基础上做 **26.3 → 1.21.11 的回退适配**：Java 25→21、ModDevGradle/NeoForge 版本，以及 26.3 独有 API（ItemInstance / ItemStackTemplate / ContainerInput / GuiGraphicsExtractor / renderer.state.level / VersionChecker / SignTextSlot / BonemealSource / world.clock / EntityTypes 等）回退为 1.21.11 等价物。

## 构建与运行

    ./gradlew build          # 编译 + 打包（产物在 build/libs；单元测试默认跳过，见下）
    ./gradlew generateData   # 重新生成客户端资源 + 服务端数据（合并输出到 src/datagen/generated）
    ./gradlew test -PrunProjecteTests   # 运行单元测试（见「已知问题」）
    ./gradlew runServer      # 开发环境专用服务器
    ./gradlew runClient      # 开发环境客户端

需要 JDK 21（可用 JAVA_HOME 指向 21；构建脚本使用 toolchain 21）。首次构建需要能访问 maven.neoforged.net 等仓库。

## 与上游集成模块的差异

- 保留编译并适配：JEI、Curios、Jade。
- 源码保留但**排除编译**（上游 1.21.11 无对应构建）：CraftTweaker、EMI、WTHIT、The One Probe。待上游发布 1.21.11 构建后可重新启用（见 build.gradle 的 sourceSets 排除项）。
- 可选依赖版本见 gradle.properties。

## 验证状态

- [x] ./gradlew build（main/api/test/jar）
- [ ] ./gradlew runData 在 1.21.11 下全量重跑
- [ ] 专用服务器冒烟（ProjectE 初始化无报错）
- [ ] 客户端目检（转化桌 / EMC HUD / 炼金袋 / 收集器 / 凝聚器 / 界面）
- [ ] 核心功能手测（EMC 映射、转化桌、炼金袋、飞行戒指、时间怀表、世界嬗变）

## 数据生成说明（1.21.11 拆分）

1.21.11 的 datagen 拆成客户端与服务端两个 run（MDG 2.x 只提供 clientData / serverData 两种 run type），而原版 HashCache 会清掉「不属于当前 run」的产物，因此两个 run **不能共享同一个 output 目录**：
- `runData`（clientData）→ `src/datagen/generated`
- `runServerData`（serverData）→ `build/generated-server-data`
- `generateData` = 依次跑两者 + 把服务端 `data/` 合并进 `src/datagen/generated/data`

提交生成产物时请用 `./gradlew generateData`，不要单独跑 `runData`（会清掉 data/）。

## 已知问题

- **单元测试默认不跑**：NeoForge 21.11 的 test framework 在关闭 ephemeral server 时偶发 JVM 退出码 255（测试本身全部通过，Gradle 视为进程崩溃）。CI 中该步骤为 informational；本地可用 `-PrunProjecteTests` 手动跑。

## 已知偏差（相对 1.21.1 上游行为）

- 护甲通过 Item.Properties#humanoidArmor 定义，会附带 ENCHANTABLE 组件，因此 PE 护甲可在附魔台附魔（1.21.1 上游是禁附魔）。后续版本可改为手工设置 EQUIPPABLE + ATTRIBUTE_MODIFIERS 恢复严格行为。
- 部分客户端视觉效果（戒指粒子等）从 Item#inventoryTick（1.21.11 仅服务端）迁移到客户端 tick 事件，触发时机可能与旧版略有差异。
- 调试用 hitbox 线框、哲人石高亮由面填充改为线框渲染（跟随 26.3 基座实现）。
- 26.3 基座新增的隐藏行为修复（如 GemFeet 条件属性改用 IHasConditionalAttributes）予以保留。

## 目录说明

- src/api：ProjectE 公共 API（独立 source set）
- src/main：模组主体
- src/datagen：数据生成器与生成产物（src/datagen/generated 会打进 jar）
- src/test：单元测试（含 transfer API 事务回滚、库存过滤等）

## 许可

MIT，与上游和 PR #2488 一致。本仓库保留上游版权声明与许可文本。