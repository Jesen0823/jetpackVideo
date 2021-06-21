package com.jesen.cod.libnavcompiler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.jesen.cod.libnavannotation.ActivityDestination;
import com.jesen.cod.libnavannotation.FragmentDestination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.jesen.cod.libnavannotation.ActivityDestination"
        ,"com.jesen.cod.libnavannotation.FragmentDestination"})
public class NavProcessor extends AbstractProcessor{

    private Messager messager;
    private Filer filter;

    // 生成文件的名称
    private static final String OUTPUT_FILE_NAME = "destination.json";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filter = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> fragmentElement = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);
        Set<? extends Element> activityElement = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);

        if (!fragmentElement.isEmpty() || !activityElement.isEmpty()){
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(fragmentElement, FragmentDestination.class, destMap);
            handleDestination(activityElement, ActivityDestination.class, destMap);

            FileOutputStream fos = null;
            OutputStreamWriter writer = null;
            // 生成文件到：app/src/main/assets
            try {
                FileObject resource = filter.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);

                // app/build/intermediates/javac/debug/classes/
                String resourcePath = resource.toUri().getPath();
                messager.printMessage(Diagnostic.Kind.NOTE, "resourcePath:"+resourcePath);

                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
                String assetsPath = appPath + "src/main/assets/";
                messager.printMessage(Diagnostic.Kind.NOTE, "assetsPath:"+assetsPath);

                File file = new File(assetsPath);
                if (!file.exists()){
                    file.mkdirs();
                }
                File outputFile = new File(file, OUTPUT_FILE_NAME);
                if (outputFile.exists()){
                    outputFile.delete();
                }
                outputFile.createNewFile();
                String content = JSON.toJSONString(destMap);
                fos = new FileOutputStream(outputFile);
                 writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(content);
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (writer != null){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private void handleDestination(Set<? extends Element> elements,
                                   Class<? extends Annotation> annotationClass,
                                   HashMap<String, JSONObject> destMap) {
        for (Element element:elements){
            TypeElement typeElement = (TypeElement) element;
            String pageUrl = null;
            String className = typeElement.getQualifiedName().toString();
            int id = Math.abs(className.hashCode());
            boolean needLogin = false;
            boolean asStarter = false;
            boolean isFragment =false;
            Annotation annotation = typeElement.getAnnotation(annotationClass);
            if (annotation instanceof FragmentDestination){
                FragmentDestination fDest = (FragmentDestination) annotation;
                pageUrl = fDest.pageUrl();
                asStarter = fDest.asStarter();
                needLogin = fDest.needLogin();
                isFragment = true;
            }else if (annotation instanceof ActivityDestination){
                ActivityDestination aDest = (ActivityDestination) annotation;
                pageUrl = aDest.pageUrl();
                asStarter = aDest.asStarter();
                needLogin = aDest.needLogin();
                isFragment = false;
            }

            // 校验
            if (destMap.containsKey(pageUrl)){
                messager.printMessage(Diagnostic.Kind.ERROR, "不允许两个页面有相同的pageUrl:"+className);
            }else {
                JSONObject object = new JSONObject();
                object.put("id",id);
                object.put("needLogin",needLogin);
                object.put("asStarter",asStarter);
                object.put("pageUrl",pageUrl);
                object.put("className",className);
                object.put("isFragment",isFragment);
            }

        }

    }
}
