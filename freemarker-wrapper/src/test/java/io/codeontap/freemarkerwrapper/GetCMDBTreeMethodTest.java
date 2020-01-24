package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.adapters.JsonValueWrapper;
import io.codeontap.freemarkerwrapper.files.methods.tree.cmdb.GetCMDBTreeMethod;
import io.codeontap.freemarkerwrapper.files.methods.list.cmdb.GetCMDBsMethod;
import io.codeontap.freemarkerwrapper.files.methods.tree.plugin.GetPluginTreeMethod;
import io.codeontap.freemarkerwrapper.files.methods.list.plugin.GetPluginsMethod;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetCMDBTreeMethodTest {

    private static Version freemarkerVersion = Configuration.VERSION_2_3_28;
    private static Configuration cfg;
    private static Map<String, Object> input = null;

    private final String templatesPath = "/tmp/gen3/templates";
    private final String cmdbsPath = "/tmp/gen3/cmdbs";
    private final String pluginsPath = "/tmp/gen3/plugins";


    {
        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setObjectWrapper(new JsonValueWrapper(cfg.getIncompatibleImprovements()));
    }


    @Before
    public void before(){
        File templateDir = new File(templatesPath);
        if (!templateDir.exists()) templateDir.mkdirs();
        File cmdbsDir = new File(cmdbsPath);
        if (!cmdbsDir.exists()) cmdbsDir.mkdirs();

    }

    @After
    public void after(){
        File templateDir = new File(templatesPath);
        if (templateDir.exists()) {
            try {
                FileUtils.deleteDirectory(templateDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File cmdbsDir = new File(cmdbsPath);
        if (cmdbsDir.exists()) {
            try {
                FileUtils.deleteDirectory(cmdbsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File pluginsDir = new File(pluginsPath);
        if (pluginsDir.exists()) {
            try {
                FileUtils.deleteDirectory(pluginsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getPlugins() throws IOException, TemplateException{
        input = new HashMap<String, Object>();
        input.put("getPlugins", new GetPluginsMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getPluginsTemplate).getBytes());
        createPlugin("aws");
        createPlugin("azure");

        input.put("pluginLayers", Arrays.asList(new String[]{pluginsPath.concat("/azure"), pluginsPath.concat("/aws")}));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = new String(byteArrayOutputStream.toByteArray());
        Pattern p = Pattern.compile("Name : ");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTree() throws IOException, TemplateException{
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate).getBytes());

        createFile(pluginsPath,"test/aws", "test.json", "{}");
        createFile(pluginsPath,"test/azure", "test.json", "{}");
        createFile(pluginsPath,"test/test", "test-1.json", "{}");

        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")
        }));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = new String(byteArrayOutputStream.toByteArray());
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTree2() throws IOException, TemplateException{
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate2).getBytes());

        createFile(pluginsPath,"test/aws", "test-aws.json", "{}");
        createFile(pluginsPath,"test/azure", "test-azure.json", "{}");
        createFile(pluginsPath,"test/test", "test.json", "{}");

        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")
        }));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = new String(byteArrayOutputStream.toByteArray());
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

    }

    @Test
    public void getCMDBs() throws IOException, TemplateException{
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("getCMDBs", new GetCMDBsMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getCMDBsTemplateFileActiveOnly).getBytes());
        String content = getCMDBsAccountsTemplate;

        createFile(cmdbsPath,"accounts", ".cmdb", content);
        createFile(cmdbsPath,"api", ".cmdb", "{}");
        createFile(cmdbsPath,"almv2", ".cmdb", "{}");

        Map<String,String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);

        input.put("lookupDirs", Arrays.asList(new String[]{}));
        input.put("CMDBNames", Arrays.asList(new String[]{  "accounts","api", "almv2",}));
        input.put("baseCMDB","accounts");

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = new String(byteArrayOutputStream.toByteArray());
        Pattern p = Pattern.compile("Name : ");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("lookupDirs", Arrays.asList(new String[]{cmdbsPath}));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(3, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("lookupDirs", Arrays.asList(new String[]{}));
        cmdbPathMapping.put("accounts", cmdbsPath.concat("/accounts"));
        cmdbPathMapping.put("almv2", cmdbsPath.concat("/almv2"));
        cmdbPathMapping.put("api", cmdbsPath.concat("/api"));
        input.put("cmdbPathMappings", cmdbPathMapping);
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(3, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList(new String[]{"accounts", "api"}));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

    }

    @Test
    public void testLookupDir() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        String fileName = templatesPath.concat("/file.ftl");
            Files.write(Paths.get(fileName), (getFileTreeAccountsTemplate).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath,"accounts", ".cmdb", content);
        createFile(cmdbsPath,"api", ".cmdb", "{}");
        createFile(cmdbsPath,"almv2", ".cmdb", "{}");
        Map<String,String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(new String[]{cmdbsPath}));
        input.put("CMDBNames", Arrays.asList(new String[]{ "accounts", "api", "almv2",  }));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = new String(byteArrayOutputStream.toByteArray());
        Pattern p = Pattern.compile("File : \\/.cmdb");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList(new String[]{ "api", "almv2", "accounts", }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("File : \\/products\\/api\\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList(new String[]{ "almv2", "accounts", "api", }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("File : \\/products\\/almv2\\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList(new String[]{ "almv2", "api", }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("File : \\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    private void createPlugin(String cmdbName){
        File accountsDir = new File(pluginsPath.concat("/").concat(cmdbName));
        if (!accountsDir.exists()) accountsDir.mkdirs();
    }

    private void createFile(String path, String dirName, String fileName, String content){
        File accountsDir = new File(path.concat("/").concat(dirName));
        if (!accountsDir.exists()) accountsDir.mkdirs();
        if(content!=null) {
            String cmdbFile = path.concat("/").concat(dirName).concat("/".concat(fileName));
            try {
                Files.write(Paths.get(cmdbFile), content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final String getPluginsTemplate = "[#ftl]\n" +
            "\n" +
            "\n" +
            "[#assign plugins = getPlugins(\n" +
            "        {}\n" +
            "    ) ]\n" +
            "\n" +
            "[#list plugins as plugin ]\n" +
            "[#list plugin as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]";

    private final String getEngineTemplate = "[#ftl]\n" +
            "[#assign regex=\"test.json\"]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";

    private final String getEngineTemplate2 = "[#ftl]\n" +
            "[#assign regex=[\"test-aws.json\", \"test-azure.json\", \"test.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";

    private final String getCMDBsTemplateFileActiveOnly = "[#ftl]\n" +
            "\n" +
            "\n" +
            "[#assign cmdbs = getCMDBs(\n" +
            "        {\"ActiveOnly\":true}\n" +
            "    ) ]\n" +
            "\n" +
            "[#list cmdbs as cmdb ]\n" +
            "[#list cmdb as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]";

    private final String getCMDBsAccountsTemplate = "{\n" +
            "  \"Version\": {\n" +
            "    \"Upgrade\": \"v1.3.2\",\n" +
            "    \"Cleanup\": \"v1.1.0\"\n" +
            "  },\n" +
            "  \"Layers\" : [\n" +
            "    {\n" +
            "      \"Name\" : \"api\",\n" +
            "      \"BasePath\" : \"products/api\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"Name\" : \"almv2\",\n" +
            "      \"BasePath\" : \"/products/almv2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final String getFileTreeAccountsTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\".cmdb\"]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "\t\"IncludeCMDBInformation\" : true\t,\n" +
            "\t\"UseCMDBPrefix\" : false\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n" +
            "\n";
}