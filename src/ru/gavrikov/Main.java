package ru.gavrikov;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Main {

    /*
     * The main method of the program.
     */
    public static void main(String[] args) throws IOException {

        ArrayList<String> arguments = new ArrayList(Arrays.asList(args));
        if (arguments.indexOf("-h") != -1) {
            showHelp();
            return;
        }

        ArgumentsParser parser = new ArgumentsParser(arguments);
        String in = parser.extractArgument("-a");
        String out = parser.extractArgument("-o");
        String name = parser.extractArgument("-n");
        String pack = parser.extractArgument("-p");
        String icon = parser.extractArgument("-i");
        String dictionary = parser.extractArgument("-r");
        String decodeArguments = parser.extractArgument("-da");
        String buildArguments = parser.extractArgument("-ba");


        Boolean isDeepRename = parser.extractBooleanArgument("-d");
        Boolean isPauseActive = parser.extractBooleanArgument("-t");
        Boolean isSkipModify = parser.extractBooleanArgument("-m");

        System.out.println(in);
        System.out.println(out);
        System.out.println(icon);


        Renamer mRenamer = new Renamer(in, out, name, pack, icon, isDeepRename, isPauseActive, isSkipModify, decodeArguments, buildArguments, dictionary);

        System.out.println("Java version = " + System.getProperty("java.version"));

        mRenamer.run();

    }

    /*
     * Show the help message.
     */
    private static void showHelp() {
        // String helpText = "\n" + "Use the renamer program to change an app name, a package name and an icon in an Android app.\n"
        //         + "\n" + "Usage: java -jar renamer.jar [-a path/to/app.apk] [-o path/to/renamed_app.apk] [-n new_name] [-p new.package.name] [-i new_icon.png] \n"
        //         + "\n" + "You can place app.apk to \"in\" folder, new_icon.png to \"icon\" folder \n"
        //         + "and run java -jar renamer.jar without arguments. Your renamed_app.apk will be placed in \"out\" folder"
        //         + "\n\nAdd the [-d] flag to perform a \"deep renaming\"."
        //         + "\n This will search for instances of the old package name in all files and replace them with the new package name."
        //         + "\n Note that the deep renaming may cause unintended side effects, such as breaking the app functionality."
        //         + "\n\nAdd the [-t] flag and the program extract all apk resources at \"temp\" folder where you can modify it as you want."
        //         + "\n After you made the changes you can resume program flow and it builds and signs the renamed apk"
        //         + "\n\nAdd the [-m] flag and the program will not modify the resources of the apk."
        //         + "\n It extracts the apk resources to \"temp\" folder where you can modify what you want manually."
        //         + "\n The program will not rename anything. After you made changes resume the program and it builds and signs the package."
        //         + "\n\nAdd the [-da \"-option1 -option2\"] to pass arguments to Apktool when it decodes the apk."
        //         + "\nAdd the [-ba \"-option1 -option2\"] to pass arguments to Apktool when it builds the apk."
        //         + "\nThe string with arguments for Apktool should be enclosed in quotation marks."
        //         + "\n\nAdd the [-r] <path/to/dictionary.txt> flag and the program will replace text in APK files using a dictionary.";
        String helpText = "\n" + "使用重命名程序更改Android应用程序中的应用名称、包名称和图标。\n"
            + "\n" + "用法: java -jar renamer.jar [-a path/to/app.apk] [-o path/to/renamed_app.apk] [-n new_name] [-p new.package.name] [-i new_icon.png] \n"
            + "\n" + "您可以将app.apk放置在\"in\"文件夹中，将new_icon.png放置在\"icon\"文件夹中\n"
            + "然后运行java -jar renamer.jar而不带参数。重命名后的app.apk将放置在\"out\"文件夹中"
            + "\n\n添加[-d]标志以执行“深度重命名”。"
            + "\n 这将在所有文件中搜索旧包名称的实例，并将其替换为新包名称。"
            + "\n 请注意，深度重命名可能会导致意外的副作用，例如破坏应用程序功能。"
            + "\n\n添加[-t]标志，程序将在\"temp\"文件夹中提取所有apk资源，您可以根据需要进行修改。"
            + "\n 修改完成后，您可以恢复程序流程，它将构建并签名重命名的apk"
            + "\n\n添加[-m]标志，程序将不会修改apk的资源。"
            + "\n 它会将apk资源提取到\"temp\"文件夹中，您可以手动修改所需内容。"
            + "\n 程序不会重命名任何内容。修改完成后恢复程序，它将构建并签名包。"
            + "\n\n添加[-da \"-option1 -option2\"]以在解码apk时传递参数给Apktool。"
            + "\n添加[-ba \"-option1 -option2\"]以在构建apk时传递参数给Apktool。"
            + "\nApktool的参数字符串应包含在引号中。"
            + "\n\n添加[-r] <path/to/dictionary.txt>标志，程序将使用字典替换APK文件中的文本。";
        System.out.println(helpText);
    }

}

class ArgumentsParser {
    ArrayList<String> arguments;

    ArgumentsParser(ArrayList<String> args) {
        arguments = args;
    }

    String extractArgument(String argName) {
        String result = "";
        int index = arguments.indexOf(argName);
        if (index != -1) {
            result = arguments.get((index + 1));
            arguments.remove(index);
            arguments.remove(index);
        }
        return result;
    }

    Boolean extractBooleanArgument(String argName) {
        Boolean result = false;
        int index = arguments.indexOf(argName);
        if (index != -1) {
            result = true;
            arguments.remove(index);
        }
        return result;
    }
}

/**
 * Renamer类用于处理APK文件的重命名和修改。
 * 
 * 主要功能包括：
 * - 解压APK文件
 * - 修改APK文件的资源和包名
 * - 通过字典替换文件内容
 * - 构建、对齐和签名APK文件
 * - 支持深度重命名和暂停功能
 * 
 * 成员变量：
 * - inApk: 输入的APK文件
 * - outApk: 输出的APK文件
 * - iconFile: 图标文件
 * - appName: 应用名称
 * - pacName: 包名称
 * - iconName: 图标名称
 * - isDeepRenaming: 是否进行深度重命名
 * - isPauseActive: 是否激活暂停
 * - isSkipModify: 是否跳过修改
 * - decodeArguments: 解码参数
 * - buildArguments: 构建参数
 * - dictionaryPath: 字典路径
 * 
 * 构造方法：
 * - Renamer(String in, String out, String name, String pack, String icon, Boolean isDeepRenaming, Boolean isPauseActive, Boolean isSkipModify, String decodeArguments, String buildArguments, String dictionaryPath)
 * 
 * 主要方法：
 * - run(): 执行重命名和修改流程
 * - replaceViaDictionary(): 通过字典替换文件内容
 * - makePause(): 显示消息并等待用户输入以继续
 * - deleteFolder(File folder): 删除文件夹及其内容
 * - nodeToString(Node node): 将XML节点转换为字符串
 * - runExec(File file, String[] args): 运行指定的可执行文件，并传递参数
 * - runJar(File file, String[] args): 运行指定的JAR文件，并传递参数
 * - delTempDir(): 删除临时目录
 * - l(String l): 打印日志信息
 * - inputNewName(): 提示用户输入新的应用名称
 * - inputNewPackageName(): 输入新的包名称
 * - input(String description): 输入信息
 * - getCurrentDir(): 获取当前目录
 * - getBinDir(): 获取bin目录
 * - getInDir(): 获取输入目录
 * - getOutDir(): 获取输出目录
 * - getTempDir(): 获取临时目录
 * - getCacheDir(): 获取缓存目录
 * - getKeyDir(): 获取密钥目录
 * - getIconDir(): 获取图标目录
 * - getResDir(): 获取资源目录
 * - getUnsignedApk(): 获取未签名的APK文件
 * - getSignedApk(): 获取已签名的APK文件
 * - getTempIdsigFile(): 获取临时ID签名文件
 * - getSubjectApk(): 获取目标APK文件
 * - getPk8Key(): 获取PK8密钥文件
 * - getPemKey(): 获取PEM密钥文件
 * - getIconPng(): 获取图标PNG文件
 * - getMapsApiKey(): 获取地图API密钥
 * - getFile(File inDir, String namePart): 获取指定目录中的文件
 * - getApktoolJar(): 获取Apktool JAR文件
 * - getSjitJar(): 获取SJIT JAR文件
 * - getZipalignExe(): 获取Zipalign可执行文件
 * - getApksignerJar(): 获取Apksigner JAR文件
 * - getZipalignedApk(): 获取对齐后的APK文件
 * - getSignApkJar(): 获取SignApk JAR文件
 * - getManifestFile(): 获取Manifest文件
 * - getDictionary(String pathToDictionary): 获取字典内容
 * - lineToMap(String line): 将字典行转换为键值对
 * - extractApk(): 解压APK文件
 * - buildApk(): 构建APK文件
 * - zipalignApk(): 对APK文件进行对齐
 * - signApk(): 签名APK文件
 * - changePackageName(Node n, String packageName): 修改包名称
 * - getMainXmlNode(File f): 获取XML文件的根节点
 * - replaceAttribute(Node node, String[] node_path, String attribute, String newValue): 替换XML节点的属性值
 * - getAttribute(Node node, String[] node_path, String attribute): 获取XML节点的属性
 * - getChildNode(Node node, String child): 获取XML节点的子节点
 * - saveXmlFile(File file, Node node): 保存XML文件
 * - getNameLabel(Node manifest): 获取应用名称标签
 * - modifySources(): 修改源文件
 * - changeImages(Node manifest, File newIcon): 修改图标
 * - changeIconName(Node n): 修改图标名称
 * - getNewIconName(): 获取新的图标名称
 * - generateString(Random rng, String characters, int length): 生成随机字符串
 * - sendNewIcon(File newIcon, File minmapDir, String newIconName): 发送新的图标
 * - getDpi(File mipMapFolder): 获取DPI值
 * - getMipmapFolderName(Node manifest): 获取Mipmap文件夹名称
 * - getMipMapFolders(String mipMapFolderName): 获取Mipmap文件夹
 * - changeStrings(Node manifest): 修改字符串资源
 * - fixProviderNoName(Node manifest): 修复Provider节点缺少名称的问题
 * - fixNoResourceError(): 修复资源错误
 */
class Renamer {
    // 输入的APK文件
    private File inApk = null;
    // 输出的APK文件
    private File outApk = null;
    // 图标文件
    private File iconFile = null;

    // 应用名称
    private String appName = "";
    // 包名称
    private String pacName = "out";
    // 图标名称
    private String iconName = "";

    // 是否进行深度重命名
    private Boolean isDeepRenaming = false;
    // 是否激活暂停
    private Boolean isPauseActive = false;
    
    // 是否跳过修改
    private Boolean isSkipModify = false;

    // 解码参数
    private String decodeArguments = "";
    // 构建参数
    private String buildArguments = "";

    // 字典路径
    private String dictionaryPath = "";


    public Renamer(String in,
                   String out,
                   String name,
                   String pack,
                   String icon,
                   Boolean isDeepRenaming,
                   Boolean isPauseActive,
                   Boolean isSkipModify,
                   String decodeArguments,
                   String buildArguments,
                   String dictionaryPath) {
        if (!in.equals("")) {
            this.inApk = new File(in);
        }
        if (!out.equals("")) {
            this.outApk = new File(out);
        }
        if (!isSkipModify) {
            if (!name.equals("")) {
                this.appName = name;
            } else {
                this.appName = inputNewName();
            }
            if (!pack.equals("")) {
                this.pacName = pack;
            } else {
                this.pacName = inputNewPackageName();
            }
        }
        if (!icon.equals("")) {
            this.iconFile = new File(icon);
        }

        this.decodeArguments = decodeArguments;
        this.buildArguments = buildArguments;

        this.isDeepRenaming = isDeepRenaming;
        this.isPauseActive = isPauseActive;
        this.isSkipModify = isSkipModify;

        this.dictionaryPath = dictionaryPath;

    }

    void run() {
        // 检查APK文件是否存在
        delTempDir();
        // 解压APK文件
        extractApk();
        // 如果不跳过修改，则修改源文件
        if (!isSkipModify) {
            modifySources();
        }
        // 如果激活暂停或跳过修改，则暂停
        if (isPauseActive || isSkipModify) {
            makePause();
        }
        // 如果字典路径不为空，则通过字典替换
        if (!dictionaryPath.equals("")) {
            replaceViaDictionary();
        }
        // 构建APK文件
        buildApk();
        // 打印日志，表示构建完成
        l("***************APK 构建完成***************\n");
        // 对APK文件进行对齐
        zipalignApk();
        l("***************APK 文件进行对齐完成***************\n");
        // 签名APK文件
        signApk();
        l("***************APK 签名完成***************\n");
    }

    // 通过字典替换文件内容
    private void replaceViaDictionary() {
        // 获取字典内容
        Map<String, String> dictionary = getDictionary(dictionaryPath);
        // 获取临时目录下的所有文件列表
        ArrayList<File> filesList = getFilesList(getTempDir());

        // 遍历文件列表
        for (File f : filesList) {
            // 遍历字典中的键值对
            for (String key : dictionary.keySet()) {
                try {
                    // 替换文件中的字节内容
                    replaceBytesInFile(f, key.getBytes("UTF-8"), dictionary.get(key).getBytes("UTF-8"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * 显示消息并等待用户输入以继续
     */
    private void makePause() {
        String message = ("\nThe process of building the package on the pause." +
                "\n You can modify the app resources in \"temp\" folder." +
                "\n The \"temp\" folder: " + getTempDir() +
                "\n Press ENTER to proceed the building process.\n");
        System.out.println(message);
        Scanner in = new Scanner(System.in);
        in.nextLine();
    }

    /*
     * 中文注释：删除文件夹及其内容
     */
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    /*
     * 中文注释：将XML节点转换为字符串
     */
    private static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
    
    /**
     * 运行指定的可执行文件，并传递参数。
     *
     * @param file 要运行的可执行文件
     * @param args 传递给可执行文件的参数数组
     *
     * 该方法将构建一个包含可执行文件路径和参数的命令数组，并执行该命令。
     * 它会捕获并记录标准输出和标准错误流中的所有输出。
     * 如果在执行过程中发生异常，将记录错误信息并打印堆栈跟踪。
     */
    private void runExec(File file, String[] args) {
        String[] command = new String[args.length + 1];
        command[0] = file.toString();
        for (int i = 1; i < command.length; i++) {
            command[i] = args[i - 1];
        }
        String logMessage = "RunExe: ";
        for (String c : command) {
            logMessage += c + " ";
        }
        l(logMessage);

        try {
            Process process = Runtime.getRuntime().exec(command);
            //process.waitFor();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                l(s);
            }
            while ((s = stdError.readLine()) != null) {
                l(s);
            }
        } catch (Exception e) {
            l("Error due execution " + file);
            e.printStackTrace();
        }

    }

    /*
     * 中文注释：运行JAR文件
     * 
     */
    // 自定义异常类，用于处理JAR文件执行过程中的异常
    public class JarExecutionException extends RuntimeException {
        public JarExecutionException(String message) {
            super(message);
        }
    }

    // 运行指定的JAR文件，并传递参数
    private void runJar(File file, String[] args) throws JarExecutionException {
        try {
            String filePath = file.getAbsolutePath();
            List<String> command = new ArrayList<String>();
            command.add("java");
            command.add("-jar");
            command.add(filePath);
            for (String arg : args) {
                command.add(arg);
            }
            String logText = "Run: ";
            for (String s : command) {
                logText += " " + s;
            }
            l(logText);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new JarExecutionException("The " + file.getName() + " file exited with an error: " + exitCode);
            }
        } catch (IOException e) {
            throw new JarExecutionException("An I/O error occurred while running the " + file.getName() + " file" + e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JarExecutionException("The " + file.getName() + " file execution was interrupted" + e);
        }
    }

    
    /*
     * 中文注释：删除临时目录
     */
    private void delTempDir() {
        deleteFolder(getTempDir());
    }

    // 打印日志信息
    public void l(String l) {
        System.out.println(l);
    }

    /*
     * 中文注释：提示用户输入新的应用名称
     */
    private String inputNewName() {
        return input("Enter a new name for the app:");
    }

    // 输入新的包名称
    private String inputNewPackageName() {
        return input("Enter a new package name:");
    }

    // 输入信息
    private String input(String description) {
        String result;
        System.out.println(description);
        Scanner in = new Scanner(System.in);
        result = in.nextLine();
        if (result.equals("")) {
            input(description);
        }
        return result;
    }

    // 获取当前目录
    private String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    // 获取bin目录
    private String getBinDir() {
        return getCurrentDir() + File.separator + "bin";
    }

    // 获取输入目录
    private File getInDir() {
        return new File(getCurrentDir() + File.separator + "in");
    }

    // 获取输出目录
    private String getOutDir() {
        return getCurrentDir() + File.separator + "out";
    }

    // 获取临时目录
    private File getTempDir() {
        return new File(getCurrentDir() + File.separator + "temp");
    }

    // 获取缓存目录
    private File getCacheDir() {
        return new File(getCurrentDir() + File.separator + "cache");
    }

    // 获取密钥目录
    private File getKeyDir() {
        return new File(getCurrentDir() + File.separator + "keys");
    }

    // 获取图标目录
    private File getIconDir() {
        return new File(getCurrentDir() + File.separator + "icon");
    }

    // 获取资源目录
    protected File getResDir() {
        return new File(getTempDir() + File.separator + "res");
    }

    // 获取未签名的APK文件
    private File getUnsignedApk() {
        return new File(getOutDir() + File.separator + pacName + ".unsigned.apk");
    }

    // 获取已签名的APK文件
    private File getSignedApk() {
        if (this.outApk == null) {
            outApk = new File(getOutDir() + File.separator + pacName + ".apk");
        } else {
            if (this.outApk.isDirectory()) {
                this.outApk = new File(this.outApk + File.separator + pacName + ".apk");
            }
        }
        return this.outApk;
    }

    private File getTempIdsigFile() {
        return new File(getSignedApk() + ".idsig");
    }

    private File getSubjectApk() {
        if (inApk == null) {
            inApk = getFile(getInDir(), ".apk");
        }
        return inApk;
    }

    private File getPk8Key() {
        return getFile(getKeyDir(), ".pk8");
    }

    private File getPemKey() {
        return getFile(getKeyDir(), ".pem");
    }

    private File getIconPng() {
        if (iconFile == null) {
            iconFile = getFile(getIconDir(), ".png");
        }
        return this.iconFile;
    }

    private HashMap<String, String> getMapsApiKey() {
        HashMap<String, String> result = new HashMap();
        File mapKeyFile = getFile(getKeyDir(), ".txt");
        if (mapKeyFile != null) {
            try {
                String name = mapKeyFile.getName().replace(".txt", "");
                BufferedReader br = new BufferedReader(new FileReader(mapKeyFile));
                String value = br.readLine();
                result.put(name, value);
            } catch (IOException e) {
            }
        }
        return result;
    }

    private File getFile(File inDir, String namePart) {
        File result = null;
        File[] apkList;
        if (namePart != null) {
            apkList = inDir.listFiles(pathname -> {
                return pathname.getName().contains(namePart);
            });
        } else {
            apkList = inDir.listFiles();
        }
        if ((apkList != null) && (apkList.length != 0)) {
            result = apkList[0];
        }
        return result;
    }

    private File getApktoolJar() {
        return new File(getBinDir() + File.separator + "apktool.jar");
    }

    private File getSjitJar() {
        return new File(getBinDir() + File.separator + "SJIT.jar");
    }


    private File getZipalignExe() {
        String osName = System.getProperty("os.name");
        File zipalignFile;
        if (osName.startsWith("Windows")) {
            zipalignFile = new File(getBinDir() + File.separator + "zipalign.exe");
        } else {
            zipalignFile = new File(getBinDir() + File.separator + "zipalign");
        }
        return zipalignFile;
    }

    private File getApksignerJar() {
        return new File(getBinDir() + File.separator + "apksigner.jar");
    }

    private File getZipalignedApk() {
        //修复bug，原来的是zipalign
        return new File(getOutDir() + File.separator + pacName + ".unsigned.apk");
    }

    private File getSignApkJar() {
        return new File(getBinDir() + File.separator + "signapk.jar");
    }

    private File getManifestFile() {
        return new File(getTempDir() + File.separator + "AndroidManifest.xml");
    }

    private Map getDictionary(String pathToDictionary) {
        List<String> allLines = new ArrayList<String>();
        Map result = new HashMap<String, String>();
        try {
            allLines = Files.readAllLines(Paths.get(pathToDictionary));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String line : allLines) {
            if (line.equals("")) continue;
            String[] pair = lineToMap(line);
            result.put(pair[0], pair[1]);
        }
        l("Dictionary: " + result.toString());
        return result;
    }

    private String[] lineToMap(String line) {
        List<Integer> splitterPosition = new ArrayList<>();
        for (int index = line.indexOf(":");
             index >= 0;
             index = line.indexOf(":", index + 1)) {
            int shieldPosition = index - 1;
            if (shieldPosition >= 0) {
                String shield = line.substring(shieldPosition, index);
                if (!shield.equals("\\")) {
                    splitterPosition.add(index);
                }
            }
        }
        if (splitterPosition.size() > 1)
            throw new RuntimeException("Dictionary file has double splitter symbol \":\" in line \'" + line + "\'");
        if (splitterPosition.size() == 0)
            throw new RuntimeException("Dictionary file has no splitter symbol \":\" in line \'" + line + "\'");
        String name = line.substring(0, splitterPosition.get(0)).replace("\\:", ":");
        String value = line.substring(splitterPosition.get(0) + 1).replace("\\:", ":");
        return new String[]{name, value};
    }


    /*
     * 中文注释：解压APK文件
     */
    private void extractApk() {
        String[] firstArgs = new String[]{"d", getSubjectApk().toString(), "-f", "-o", getTempDir().toString()};
        String[] additionalArgs = stringToArguments(decodeArguments);
        String[] command = concat(firstArgs, additionalArgs);
        runJar(getApktoolJar(), command);
    }
    /*
     * 中文注释：构建APK文件
     */
    private void buildApk() {
        String[] firstArgs = new String[]{"b", getTempDir().toString(), "-o", getUnsignedApk().toString()};
        String[] additionalArgs = stringToArguments(buildArguments);
        String[] command = concat(firstArgs, additionalArgs);

        try {
            runJar(getApktoolJar(), command);
        } catch (Renamer.JarExecutionException e) {
            try {
                l("\n!!!\nTry to fix No resource error");
                fixNoResourceError(); //Fix apktool problem with manifest
                runJar(getApktoolJar(), command);
            } catch (Renamer.JarExecutionException e1) {
                l("\n!!!\nTry to fix Invalid resource directory name error by --use-aapt2");
                String[] fixInvalidResourceDirectoryNameCommand = concat(command, new String[]{"--use-aapt2"});
                runJar(getApktoolJar(), fixInvalidResourceDirectoryNameCommand);
            }
        }
    }

    /*
     * 中文注释：对APK文件进行对齐
     */
    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    /*
     * 中文注释：签名APK文件
     */
    private String[] stringToArguments(String s) {
        ArrayList<String> result = new ArrayList();
        String[] listArgs = s.split(" ");
        for (int i = 0; i < listArgs.length; i++) {
            String value = listArgs[i];
            if (!value.equals("")) {
                result.add(value);
            }
        }
        return result.toArray(new String[0]);
    }

    /*
     * 中文注释：修复资源错误
     */
    private void zipalignApk() {
        File zipalignFile = getZipalignExe();
        if (!zipalignFile.canExecute()) {
            l("\nError! Can't execute zipalign file!");
            l("Please make the file 'zipalign' executable using the following command:\n" +
                    "sudo chmod +x '" + zipalignFile +"'");
            System.exit(1);
        }
        runExec(zipalignFile, new String[]{"-p", "-f", "-v", "4", getUnsignedApk().toString(), getZipalignedApk().toString()});
        // Kevin Test 暂时注释
//        getUnsignedApk().delete();
    }

    /*
     * 中文注释：签名APK文件
     */
    private void signApk() {
        String[] command = {"sign", "--key", getPk8Key().toString(), "--cert", getPemKey().toString(), "--out", getSignedApk().toString(), getZipalignedApk().toString()};
        l("Signed apk " + getSignedApk().toString());
        runJar(getApksignerJar(), command);
        // Kevin Test 暂时注释
//        getZipalignedApk().delete();
        getTempIdsigFile().delete();
        l("");
        if (getSignedApk().exists()) {
            l("Success. Path to your renamed apk: " + getSignedApk());
        } else {
            l(":-(");
            l("Rename unsuccessful.");

        }
    }

    /*
     * 中文注释：修改包名称
     */
    private void changePackageName(Node n, String packageName) {
        replaceAttribute(n, new String[]{}, "package", packageName);
    }

    /*
     * 中文注释：获取XML文件的根节点
     */
    private Node getMainXmlNode(File f) {
        Node root = null;
        //File f = new File(getManifestFile());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            root = doc.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
        }
        return root;
    }

    /*
     * 中文注释：替换XML节点的属性值
     */
    private void replaceAttribute(Node node, String[] node_path, String attribute, String newValue) {
        Node att = getAttribute(node, node_path, attribute);
        if(att !=null){
            att.setNodeValue(newValue);
        }

    }

    /*
     * 中文注释：获取XML节点的属性
     */
    private Node getAttribute(Node node, String[] node_path, String attribute) {

        Node myNode = node;
        NodeList childNodes = myNode.getChildNodes();
        for (String s : node_path) {
            myNode = getChildNode(myNode, s);
        }
        NamedNodeMap attr = myNode.getAttributes();
        //如果attr为null
        if(attr ==null)
        {
            return null;
        }
        Node att = attr.getNamedItem(attribute);
        return att;
    }

    /*
     * 中文注释：获取XML节点的子节点
     */
    private Node getChildNode(Node node, String child) {
        Node result = null;
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            String name = childNodes.item(i).getNodeName();
            if (name.equals(child)) {
                result = childNodes.item(i);
                return result;
            }
        }
        return null;
    }


    /*
     * 中文注释：保存XML文件
     */
    private void saveXmlFile(File file, Node node) {
        // write the content into xml file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerException e) {

        }

    }

    /*
     * 中文注释：获取应用名称标签
     */
    private String getNameLabel(Node manifest) {
        Node att = getAttribute(manifest, new String[]{"application"}, "android:label");
        if (att ==null){
            return  "";
        }
        String result = att.getNodeValue();
        if (!result.contains("@string/")) return null;
        result = result.replace("@string/", "");
        return result;
    }

    // We do all changes in unzipped sources
    /*
     * 中文注释：修改源文件
     */
    private void modifySources() {
        Node manifest = getMainXmlNode(getManifestFile());

        String packageName = getPackageName(manifest);


        if (!this.pacName.equals("")) {
            changePackageName(manifest, this.pacName);
        }


        changeStrings(manifest);
        fixProviderNoName(manifest);

        File newIcon = getIconPng();
        if (newIcon != null) {
            changeImages(manifest, newIcon);
            changeIconName(manifest);
        }
        saveXmlFile(getManifestFile(), manifest);
        if (this.isDeepRenaming) {
            l("Perform deep renaming...");
            renamePackageFolders(packageName, this.pacName);
        }

    }

    /*
     * 中文注释：修改图标
     */
    private void changeImages(Node manifest, File newIcon) {
        File[] mipMapFolders = getMipMapFolders(getMipmapFolderName(manifest));
        for (File f : mipMapFolders) {
            l(f.toString() + " " + getDpi(f));
            if (!f.getName().contains("anydpi")) {
                sendNewIcon(newIcon, f, getNewIconName());
            }
        }
    }

    /*
     * 中文注释：修改图标名称
     */
    private void changeIconName(Node n) {
        String name = getMipmapFolderName(n);
        replaceAttribute(n, new String[]{"application"}, "android:icon", "@" + name + "/" + getNewIconName());
    }

    /*
     * 中文注释：获取新的图标名称
     */
    private String getNewIconName() {
        if (this.iconName.equals("")) {
            this.iconName = generateString(new Random(), "abcdefghijklmnopqrstuvwxyz", 10);
        }
        return this.iconName;
    }

    /*
     * 中文注释：生成随机字符串
     */
    private static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    /*
     * 中文注释：发送新的图标
     */
    private void sendNewIcon(File newIcon, File minmapDir, String newIconName) {
        int size = getDpi(minmapDir);
//        String command = "" + getSjitJar() + " -in " + newIcon + " -resize " + size + "px -out " + minmapDir + File.separator + newIconName + ".png";
//
//        runJar(getSjitJar(), new String[]{"-in", newIcon.toString(), "-resize", size + "px", "-out", minmapDir + File.separator + newIconName + ".png"});

        try {
            File resizedIcon = new File(minmapDir, newIconName + ".png");
            l("Resize icon from " + newIcon + " to " + resizedIcon);
            Thumbnails.of(newIcon)
                    .size(size, size)
                    .toFile(resizedIcon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 中文注释：获取DPI值
     */
    private int getDpi(File mipMapFolder) {
        String name = mipMapFolder.getName();
        int result = 48;
        if (name.contains("xxxhdpi")) {
            result = 192;
        } else if (name.contains("xxhdpi")) {
            result = 144;
        } else if (name.contains("xhdpi")) {
            result = 96;
        } else if (name.contains("hdpi")) {
            result = 72;
        }
        return result;
    }


    /*
     * 中文注释：获取Mipmap文件夹名称
     */
    private String getMipmapFolderName(Node manifest) {
        Node att = getAttribute(manifest, new String[]{"application"}, "android:icon");
        if (att == null) {
            return "";
        }
        String result = att.getNodeValue();
        int end = result.indexOf("/");
        result = result.substring(1, end);
        return result;
    }

    /*
     * 中文注释：获取Mipmap文件夹
     */
    private File[] getMipMapFolders(String mipMapFolderName) {
        File[] result = getResDir().listFiles(pathname -> {
            return pathname.getName().contains(mipMapFolderName);
        });
        return result;
    }

    /*
     * 中文注释：修改字符串资源
     */
    private void changeStrings(Node manifest) {
        String name_label = getNameLabel(manifest);

        HashMap<String, String> forReplace = new HashMap<>();
        forReplace.putAll(getMapsApiKey());
        if (!this.appName.equals("")) {
            if (name_label != null) {
                forReplace.put(name_label, this.appName);
            }else {
                replaceAttribute(manifest, new String[]{"application"}, "android:label" , this.appName);
            }
        }

        for (File f : getStringsFiles()) {
            Node node = getMainXmlNode(f);
            replaceStrings(node, forReplace);
            saveXmlFile(f, node);
        }
    }

    //Fix problem if in manifest provider has no name
    /*
     * 中文注释：修复Provider节点缺少名称的问题
     */
    private void fixProviderNoName(Node manifest) {

        NodeList nl = manifest.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeName().contains("queries")) {
                NodeList child = node.getChildNodes();
                for (int j = 0; j < child.getLength(); j++) {
                    Node provider = child.item(j);
                    if (provider.getNodeName() == "provider") {
                        Boolean isNameAbsent = true;
                        NamedNodeMap attr = provider.getAttributes();
                        for (int n = 0; n < attr.getLength(); n++) {
                            l(attr.item(n).getNodeName());
                            if (attr.item(n).getNodeName() == "android:name") isNameAbsent = false;
                        }
                        if (isNameAbsent) {
                            Attr nameAttribute = provider.getOwnerDocument().createAttribute("android:name");
                            nameAttribute.setValue("myname");
                            Element providerElement = (Element) provider;
                            providerElement.setAttributeNode(nameAttribute);
                            l("Repair absent name tag in provider node");
                        }
                    }
                }
            }
        }
    }


    //Fix problem with error: Error: No resource type specified (at 'value' with value '@1996685312')
    //Make updates in the manifest
    private void fixNoResourceError() {
        Node manifest = getMainXmlNode(getManifestFile());
        NodeList nl = manifest.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeName().equals("application")) {
                NodeList applicationsNodes = node.getChildNodes();
                for (int j = 0; j < applicationsNodes.getLength(); j++) {
                    Node item = applicationsNodes.item(j);
                    if (item.getNodeName().equals("meta-data")) {
                        NamedNodeMap attributes = item.getAttributes();
                        if (attributes.getLength() > 1) {
                            if (attributes.item(0).toString().contains("com.android.vending.splits")) {
                                attributes.item(1).setNodeValue("base");
                                l("value = " + attributes.item(1));
                                saveXmlFile(getManifestFile(), manifest);
                                l("Repair error: No resource type specified");
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * 
     */
    private void replaceStrings(Node node, HashMap<String, String> forReplace) {
        NodeList nl = node.getChildNodes();
        Set keysForReplace = forReplace.keySet();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if (item.hasAttributes()) {
                String stringName = item.getAttributes().item(0).getNodeValue();
                if (keysForReplace.contains(stringName)) {
                    item.setTextContent(forReplace.get(stringName));
                }
            }
        }
    }


    /*
     * 中文注释：获取资源文件夹
     */
    private File[] getValuesFolders() {
        File res = getResDir();
        File[] valuesFolders = res.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean result = false;
                if ((pathname.isDirectory()) && (pathname.getName().contains("values"))) {
                    result = true;
                }
                return result;
            }
        });
        return valuesFolders;
    }

    /*
     * 中文注释：获取字符串资源文件
     */
    private ArrayList<File> getStringsFiles() {
        ArrayList<File> result = new ArrayList<>();
        File[] valuesFolders = getValuesFolders();
        for (File vf : valuesFolders) {
            File[] stringsFile = vf.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean res = false;
                    if (pathname.getName().equals("strings.xml")) {
                        res = true;
                    }
                    return res;
                }
            });
            if (stringsFile != null) {
                result.addAll(Arrays.asList(stringsFile));
            }
        }
        return result;
    }

    //Replace a text in a file
    /*
     * 中文注释：替换文件中的文本
     */
    private void replaceText(File file, String toReplace, String replacement) {
        try {
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(file.toPath()), charset);
            content = content.replaceAll(toReplace, replacement);
            Files.write(file.toPath(), content.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Replace bytes in a file
    /*
     * 中文注释：替换文件中的字节
     */
    private void replaceBytesInFile(File file, byte[] searchBytes, byte[] replaceBytes) throws IOException {
        byte[] origin = FileUtils.readFileToByteArray(file);
        byte[] replaced = replaceBytesInArray(origin, searchBytes, replaceBytes);

        if (replaced != null) {
            l("File: " + file + " changed");
            FileUtils.delete(file);
            FileUtils.writeByteArrayToFile(file, replaced);
        }
    }

    /*
     * 中文注释：替换数组中的字节
     */
    private byte[] replaceBytesInArray(byte[] origin, byte[] searchBytes, byte[] replaceBytes) {
        List<Byte> originList = arrayToList(origin);
        List<Byte> searchList = arrayToList(searchBytes);
        List<Byte> replaceList = arrayToList(replaceBytes);
        if (searchList.equals(replaceList)) return null;
        Boolean isChanged = false;
        while (true) {
            int startPosition = Collections.indexOfSubList(originList, searchList);
            if (startPosition == -1) break;
            isChanged = true;
            for (int i = 0; i < searchBytes.length; i++) {
                originList.remove(startPosition);
            }
            originList.addAll(startPosition, replaceList);
        }
        byte[] result = new byte[originList.size()];
        for (int i = 0; i < originList.size(); i++) {
            result[i] = originList.get(i);
        }
        if (isChanged) {
            return result;
        } else {
            return null;
        }
    }

    private static List<Byte> arrayToList(byte[] arr) {
        List<Byte> result = new ArrayList<>();
        for (byte b : arr) {
            result.add(b);
        }
        return result;
    }


    //Recursive list of files in a folder
    private ArrayList<File> getFilesList(File folder) {
        ArrayList<File> result = (ArrayList<File>) FileUtils.listFiles(folder, null, true);
        return result;
    }

    //Get package name from Manifest
    /*
     * 中文注释：从Manifest文件中获取包名称
     */
    private String getPackageName(Node manifest) {
        return manifest.getAttributes().getNamedItem("package").getNodeValue();
    }


    //Rename folders with smali for new package
    /*
     * 中文注释：为新的包重命名包文件夹
     */
    private void renamePackageFolders(String oldPackageName, String newPackageName) {
        List<File> smaliFolders = Arrays.asList(Objects.requireNonNull(getTempDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory() && pathname.getName().contains("smali"));
            }
        })));

        for (File folder : smaliFolders) {
            File cache = movePackageToCache(folder, oldPackageName);
            deleteOldPackageFolders(folder, oldPackageName);
            if (cache != null) {
                File destination = moveFromCacheToPackage(folder, newPackageName);
            }
        }
        ArrayList<File> smaliFiles = getFilesList(getTempDir());
        for (File f : smaliFiles) {
            String oldPackageLabel = oldPackageName.replace(".", "/");//Lru/gavrikov/mocklocations
            String newPackageLabel = newPackageName.replace(".", "/");
            try {
                replaceBytesInFile(f, oldPackageLabel.getBytes(), newPackageLabel.getBytes());
                replaceBytesInFile(f, oldPackageName.getBytes(), newPackageName.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }

    /*
     * 
     */
    private File movePackageToCache(File smaliFolder, String packageName) {
        File sourceFolder = new File(smaliFolder.getAbsolutePath() + File.separator + packageName.replace(".", File.separator));
        if (sourceFolder.exists()) {
            try {
                File cache = getCacheDir();
                FileUtils.deleteDirectory(cache);
                cache.mkdir();
                FileUtils.copyDirectory(sourceFolder, cache);
                return cache;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /*
     * 中文注释：删除旧的包文件夹
     */
    private void deleteOldPackageFolders(File smaliFolder, String packageName) {
        try {
            ArrayList<File> foldersForDeleting = getFolderTreeByPackageName(smaliFolder, packageName);
            if (foldersForDeleting.isEmpty()) return;
            FileUtils.deleteDirectory(foldersForDeleting.get(0));
            foldersForDeleting.remove(0);
            for (File f : foldersForDeleting) {
                if (f.listFiles().length == 0) {
                    FileUtils.deleteDirectory(f);
                }
                ;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 中文注释：根据包名称获取文件夹树
     */
    private ArrayList<File> getFolderTreeByPackageName(File smaliFolder, String packageName) {
        ArrayList<String> foldersTree = new ArrayList<>(Arrays.asList(packageName.split(("\\."))));
        ArrayList<File> resultFolders = new ArrayList<>();
        while (!foldersTree.isEmpty()) {
            File deepFolder = new File(smaliFolder.getAbsolutePath());
            for (String n : foldersTree) {
                deepFolder = new File(deepFolder, n);
            }
            if (deepFolder.exists()) {
                resultFolders.add(deepFolder);
            }
            foldersTree.remove(foldersTree.size() - 1);
        }
        return resultFolders;
    }

    /*
     * 中文注释：将缓存中的文件移动到包中
     */
    private File moveFromCacheToPackage(File smaliFolder, String packageName) {
        File cache = getCacheDir();
        File targetFolder = smaliFolder;
        for (String name : packageName.split("\\.")) {
            targetFolder = new File(targetFolder, name);
            targetFolder.mkdir();
        }
        try {
            FileUtils.copyDirectory(cache, targetFolder);
            FileUtils.deleteDirectory(cache);
            return targetFolder;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}