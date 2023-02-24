package org.demospirng;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yimingyu
 */
public class DemoApplicationContext {

    private Class configClass;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private Map<String, Object> singleBeanObjMap = new ConcurrentHashMap<>();

    public DemoApplicationContext() {}

    public DemoApplicationContext(Class configClass) {
        this.configClass = configClass;
        // 获取扫描路径下所有的bean class对象
        List<Class> scan = scan(configClass);

//        List<Class> newScan = filterSingleton(scan);
        System.out.println(scan);
        // 解析创建beanDefinition
        buildBeanDefinitionMap(scan);
        // 创建非lazy的、单例的bean对象
        instanceSingletonBean();
    }

    public Set<String> getAllBeans() {
        return beanDefinitionMap.keySet();
    }

    public void buildBeanDefinitionMap(List<Class> scan) {
        for (Class cls : scan) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(cls);
            Annotation assignAnnotation = getAssignAnnotation(cls, Lazy.class);
            if (assignAnnotation == null ){
                beanDefinition.setLazy(false);
            } else {
                Lazy lazy = (Lazy) assignAnnotation;
                if (lazy.value()) {
                    beanDefinition.setLazy(true);
                } else {
                    beanDefinition.setLazy(false);
                }
            }
            Annotation assignAnnotation1 = getAssignAnnotation(cls, Scope.class);
            if (assignAnnotation1 == null ){
                beanDefinition.setScope(ScopeEnum.SINGLETON.getType());
            } else {
                Scope scope = (Scope) assignAnnotation1;
                if (scope.value().equals(ScopeEnum.SINGLETON.getType())) {
                    beanDefinition.setScope(ScopeEnum.SINGLETON.getType());
                } else {
                    beanDefinition.setScope(ScopeEnum.PROTOTYPE.getType());
                }
            }
            String beanName = StringUtils.uncapitalize(cls.getSimpleName());
            this.beanDefinitionMap.put(beanName, beanDefinition);
        }
    }

    public void instanceSingletonBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            if (!beanDefinition.isLazy() && beanDefinition.getScope().equals(ScopeEnum.SINGLETON.getType())
                    && singleBeanObjMap.get(entry.getKey()) == null) {
                singleBeanObjMap.put(entry.getKey(), createBean(beanDefinition));
            }
        }
    }

    public Object createBean(BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        Object obj = null;
        try {
            obj = beanClass.getDeclaredConstructor().newInstance();
            Field[] declaredFields = beanClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    String beanName = StringUtils.uncapitalize(field.getType().getSimpleName());
                    BeanDefinition beanDefinition1 = beanDefinitionMap.get(beanName);
                    if (beanDefinition1 == null) {
                        throw new RuntimeException("bean not exist");
                    }
                    field.setAccessible(true);
                    if (beanDefinition1.getScope().equals(ScopeEnum.SINGLETON.getType())) {
                        Object o = singleBeanObjMap.get(beanName);
                        if (o == null) {
                            o  = createBean(beanDefinition1);
                            singleBeanObjMap.put(beanName, o);
                        }
                        field.set(obj, o);
                    } else {
                        Object o1  = createBean(beanDefinition1);
                        field.set(obj, o1);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private void  getPathAllClass(File file, List<Class> classList, ClassLoader classLoader) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                getPathAllClass(file1, classList, classLoader);
            }
        } else {
            String absolutePath = file.getAbsolutePath();
            String classes = absolutePath.substring(absolutePath.indexOf("classes") + 8, absolutePath.indexOf(".class"));
            Class<?> aClass = null;
            try {
                aClass = classLoader.loadClass(classes.replace("/", "."));
            }catch (Exception e) {
                e.printStackTrace();
            }
            if (aClass != null && getAssignAnnotation(aClass, Component.class) != null) {
                classList.add(aClass);
            }
        }

    }

    private List<Class> scan(Class configClass) {
        // 判断是不是配置类
        Annotation assignAnnotation = getAssignAnnotation(configClass, Configuration.class);
        if (assignAnnotation == null) {
            throw new RuntimeException("配置类不存在");
        }
        // 获取Bean扫描路径
        Annotation annotation = getAssignAnnotation(configClass, ComponentScan.class);
        ComponentScan componentScan = annotation == null ? null : (ComponentScan) annotation;
        String scanPath = "";
        if (componentScan != null) {
            String value = componentScan.value();
            if (StringUtils.isNotEmpty(value)) {
                scanPath = value.replace(".", "/");
            } else {
                String path = configClass.getResource("").getPath().split("classes")[1];
                scanPath = path.substring(1, path.length() - 1);
            }
        } else {
            String path = configClass.getResource("").getPath().split("classes")[1];
            scanPath = path.substring(1, path.length() - 1);
        }
        System.out.println(scanPath);
        // 获取该路径下所有的Bean class对象
        ClassLoader classLoader = DemoApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(scanPath);
        File file = new File(resource.getFile());
        List<Class> classes = new ArrayList<>();
        getPathAllClass(file, classes, classLoader);
        return classes;
    }

    public List<Class> filterSingleton(List<Class> classes) {
        List<Class> singletonClasses = new ArrayList<>();
        for (Class cls1 : classes) {
            Annotation assignAnnotation = getAssignAnnotation(cls1, Scope.class);
            if (assignAnnotation != null) {
                String value = ((Scope) assignAnnotation).value();
                if (StringUtils.isNotEmpty(value) && value.equals("prototype")) {
                    continue;
                }
            }
            singletonClasses.add(cls1);
        }
        return singletonClasses;
    }

    public Annotation getAssignAnnotation(Class cls, Class annotationCls) {
        Annotation[] declaredAnnotations = cls.getDeclaredAnnotations();
        Annotation assignAnnotation = null;
        for (Annotation annotation : declaredAnnotations) {
            if (annotation.annotationType().isAssignableFrom(annotationCls)) {
                assignAnnotation = annotation;
            }
        }
        return assignAnnotation;
    }

    public Object getBean(String className) {
        String beanName = StringUtils.uncapitalize(className);
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition != null) {
            if (!beanDefinition.isLazy() && beanDefinition.getScope().equals(ScopeEnum.SINGLETON.getType())) {
                return singleBeanObjMap.get(beanName);
            } else {
                return createBean(beanDefinition);
            }
        }
        return null;
    }

    public <T> T getBean(String className, Class<T> beanClass) {
        return null;
    }
}
