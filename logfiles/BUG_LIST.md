**20/06/02**
* 搜索页按home键再返回，再按搜索框，底部panel会随键盘上升。(1)

**20/06/05**
* 横竖屏切换时，APP会crash;(2 fixed:fragment未加无参构造方法)

### Version 2.0.0
1. 分页加载，当还未加载下一页时，删除元素，然后加载下一页，这时出现异常，
部分元素未成功加载。