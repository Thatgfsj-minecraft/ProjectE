# 第三方许可与归属说明

本仓库是 [sinkillerj/ProjectE](https://github.com/sinkillerj/ProjectE) 的 fork，主体代码沿用上游 MIT 许可与版权声明（见 LICENSE）。

## Minecraft 1.21.11 移植相关归属

- **sinkillerj/ProjectE 贡献者**：上游 1.21.1 基线代码，MIT。
- **JamesClarke7283**：上游 PR [sinkillerj/ProjectE#2488](https://github.com/sinkillerj/ProjectE/pull/2488)，将 ProjectE 从 MC 1.21.1 迁移到 MC 26.3 / NeoForge 26.3（NeoForge transfer API 库存、渲染 render state、数据驱动物品模型、配方 codec、datagen 重写等）。本仓库的 1.21.11 移植以其为该 PR 的基座做回退适配，MIT。
- **Yaskulsky/Equivox**：PR #2488 声明其 API 迁移参考了该项目，MIT，原许可与版权声明随上游约定保留。
- **NeoForge / Minecraft**：构建与运行所依赖的平台，遵循其各自许可（NeoForge LGPL-2.1；Minecraft EULA）。

## 说明

- 本仓库仅保留移植所需的 MIT 兼容代码；不含任何未授权的资源文件。
- 若发现归属缺失或许可不兼容之处，请开 issue 说明，我们会尽快更正。
