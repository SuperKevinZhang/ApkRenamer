# ApkRenamer

Use the renamer program to change an app name, a package name and an icon in an Android app.

Requirement - [JRE 1.8 (64-bit)](https://www.java.com/en/download/manual.jsp)

## Download:

[ApkRenamer.zip](https://github.com/dvaoru/ApkRenamer/releases/latest/download/ApkRenamer.zip)


## Usage:
```
java -jar renamer.jar [-a] <path/to/app.apk> [-o] <path/to/renamed.apk> [-n] <new_name> [-p] <new.package.name> [-i] <new_icon.png>
```
You can place app.apk to \"in\" folder, new_icon.png to \"icon\" folder
and run `java -jar renamer.jar` without arguments. Your renamed_app.apk will be placed in \"out\" folder

Optionally you can add the `[-d]` flag to perform a "deep renaming".

&nbsp;&nbsp;&nbsp;&nbsp;This will search for instances of the old package name in all files and replace them with the new package name.

&nbsp;&nbsp;&nbsp;&nbsp;If you rename an app with the deep renaming you can install the renamed app along with the original app on your device.  

&nbsp;&nbsp;&nbsp;&nbsp;Note that the deep renaming may cause unintended side effects, such as breaking app functionality.

Optionally you can add the `[-t]` flag and the program extract all apk resources at "temp" folder where you can modify it as you want.

&nbsp;&nbsp;&nbsp;&nbsp;After you made the changes you can resume program flow, and it builds and signs the renamed apk

Optionally you can add the `[-m]` flag and the program will not modify the resources of the apk.

&nbsp;&nbsp;&nbsp;&nbsp;It extracts the apk resources to "temp" folder where you can modify what you want manually.

&nbsp;&nbsp;&nbsp;&nbsp;The program will not rename anything. After you made changes resume the program, and it builds and signs the package.

Optionally, you can pass arguments to Apktool.

&nbsp;&nbsp;&nbsp;&nbsp; To implement arguments when Apktool decodes the apk, add the following flag: `-da "-option1 -option2"`. For example, `-da "--keep-broken-res"`. The string with arguments for Apktool should be enclosed in quotation marks.

&nbsp;&nbsp;&nbsp;&nbsp; To implement arguments when Apktool builds the apk, add the following flag: `-ba "-option1 -option2"`. For example, `-ba "--use-aapt2"`. The string with arguments for Apktool should be enclosed in quotation marks.

&nbsp;&nbsp;&nbsp;&nbsp; You can find a list of Apktool's arguments on its [official site](https://ibotpeaches.github.io/Apktool/documentation/).

Optionally add the `[-r] <path/to/dictionary.txt>` flag and the program will replace text in APK files using a dictionary.

&nbsp;&nbsp;&nbsp;&nbsp; Dictionary file format:
```
original text:replacement text
another original text:another replacement text
```
&nbsp;&nbsp;&nbsp;&nbsp; The splitter is ":" symbol. . If you need to include this symbol in the replacement text, you can escape it using "/:".

## Notice:
- You may not use ApkRenamer for any illegal purposes;
- The repacked APKs should not violate the original licenses.

## Third-Party Components

- [Apktool](https://github.com/iBotPeaches/Apktool)
- [Apk Sign](https://github.com/appium/sign)
- [Simple Java Image Tool](https://sjit.sourceforge.io/)
- [Thumbnailator](https://github.com/coobird/thumbnailator)
## Renamer 类 run 方法详细执行计划

`Renamer` 类的 `run` 方法是修改 APK 文件的核心逻辑，其执行计划如下：

**1. 初始化:**

* `delTempDir()`: 删除临时目录，为解包 APK 做准备。
* `extractApk()`: 使用 Apktool 解包 APK 文件到临时目录。

**2. 修改 APK 文件 (可选):**

* `if (!isSkipModify) modifySources()`: 如果 `isSkipModify` 为 false，则调用 `modifySources()` 方法修改 APK 文件的源代码和资源文件。
    * `modifySources()` 方法会修改 `AndroidManifest.xml` 文件中的包名、应用名称和图标等属性，修改字符串资源文件，以及可选地进行深度重命名。
* `if (isPauseActive || isSkipModify) makePause()`: 如果 `isPauseActive` 或 `isSkipModify` 为 true，则暂停程序，等待用户手动修改资源文件。
* `if (!dictionaryPath.equals("")) replaceViaDictionary()`: 如果 `dictionaryPath` 不为空，则使用字典文件替换 APK 文件中的文本内容。

**3. 重新打包 APK:**

* `buildApk()`: 使用 Apktool 重新打包 APK 文件，生成未签名的 APK 文件。

**4. 优化 APK:**

* `zipalignApk()`: 使用 zipalign 工具对齐 APK 文件，优化文件结构。

**5. 签名 APK:**

* `signApk()`: 使用 apksigner 工具对 APK 文件进行签名，生成最终的 APK 文件。

**6. 清理:**

* 删除临时文件和目录。

**流程图:**

```
+-----------------+
|   开始           |
+-------+---------+
        |
        V
+-------+---------+
| 删除临时目录     |
+-------+---------+
        |
        V
+-------+---------+
| 解包 APK 文件   |
+-------+---------+
        |
        V
+-------+---------+
| 修改 APK 文件？  |----N---->+-------+---------+
| (isSkipModify)   |         | 重新打包 APK 文件 |
+-------+---------+         +-------+---------+
        |Y                   |
        V                   V
+-------+---------+    +-------+---------+
| 修改源代码和资源文件 |    | 对齐 APK 文件    |
+-------+---------+    +-------+---------+
        |                   |
        V                   V
+-------+---------+    +-------+---------+
| 暂停程序？       |----N---->+-------+---------+
| (isPauseActive)  |         | 签名 APK 文件    |
+-------+---------+         +-------+---------+
        |Y                   |
        V                   V
+-------+---------+    +-------+---------+
| 等待用户输入     |    | 清理临时文件和目录 |
+-------+---------+    +-------+---------+
        |                   |
        +-------------------+
        |
        V
+-------+---------+
|   结束           |
+-----------------+
```

**总结:**

`Renamer` 类的 `run` 方法实现了修改 APK 文件的完整流程，包括解包、修改、重新打包、优化和签名等步骤.
