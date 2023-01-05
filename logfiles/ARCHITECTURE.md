### 项目架构 🐅 
- 播放器 `AudioPlayer` 运行于独立进程，`AudioPlayerManager` 通过 `AIDL` 进行调用，然后对外暴露播放器方法给业务层调用；
- `AudioPlayerManager` 借助观察者模式将播放器的状态告知业务层，业务层根据播放器状态执行相应逻辑；
- 业务层使用 `Jetpack Components` 实现，即 `MVVM` 架构；
