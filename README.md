QRCodeTool
==========

二维码解码和编码的GUI工具。(A QR Code decoding and encoding tool with a simple and easy-to-use GUI.)

工具截图 (Screenshots)
---------------------
![QRCodeTool Screenshot 1](https://raw.githubusercontent.com/pollyman/QRCodeTool/master/screenshots/QRCodeTool_1.png)

关于二维码中添加Logo图原理
----------------------
    二维码本身具有纠错能力，因此遮挡其中的一部分并不影响数据的正常读取。
    遮挡部分的比例取决于当前二维码本身预设的纠错等级，用本工具生成的二维码可以遮挡部分的比例大约为15%。
    注意：本工具本身暂不支持添加Logo图，有需要者请自行PS之。

项目中用到的第三方类库
------------------
    [zxing](https://github.com/zxing/zxing)
    lib/zxing-core.jar
    lib/zxing-javase.jar

    [commons-codec](http://commons.apache.org/proper/commons-codec/)
    lib/commons-codec-1.9.jar

其它
---
    项目使用 Oracle Java SE 7 环境编译发布。
    源文件编码：UTF-8
    目录说明：
      src         - 源文件目录
      lib         - 第三方类库目录
      dist        - 工具可执行jar包目录
      screenshots - 工具截图目录
