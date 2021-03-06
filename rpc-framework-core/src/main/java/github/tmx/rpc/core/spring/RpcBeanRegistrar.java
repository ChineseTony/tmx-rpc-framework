package github.tmx.rpc.core.spring;

import github.tmx.rpc.core.spring.annotation.EnableRPC;
import github.tmx.rpc.core.spring.annotation.RpcService;
import github.tmx.rpc.core.spring.component.ServiceNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 15:12
 */
public class RpcBeanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(RpcBeanRegistrar.class);

    private static final String SPRING_PROCESSOR_BASE_PACKAGE = "github.tmx.rpc.core.spring.component";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 输出 banner
        bannerPrint();
        // 获得 @EnableRPC 注解里边所有的属性
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.
                fromMap(annotationMetadata.getAnnotationAttributes(EnableRPC.class.getName()));
        String[] basePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // 获得 @EnableRPC 注解中 basePackage 这个字符串数组
            basePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        // 如果 @EnableRPC 中, 没有填 basePackage, 默认去扫描使用 @EnableRPC 那个类所在的路径
        if (basePackages.length == 0) {
            basePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // 扫描 @RpcService
        RpcBeanScanner rpcServiceScanner = new RpcBeanScanner(beanDefinitionRegistry, RpcService.class);
        rpcServiceScanner.setBeanNameGenerator(new ServiceNameGenerator());
        // 扫描 core 模块中的 2 个 processor
        RpcBeanScanner springBeanScanner = new RpcBeanScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int processorAmount = springBeanScanner.scan(SPRING_PROCESSOR_BASE_PACKAGE);
        logger.debug("component 扫描的数量 [{}]", processorAmount);
        int rpcServiceAmount = rpcServiceScanner.scan(basePackages);
        logger.debug("rpcService 扫描的数量 [{}]", rpcServiceAmount);
    }

    private void bannerPrint() {
        String fileName = "META-INF/banner";
        ClassLoader classLoader = RpcBeanRegistrar.class.getClassLoader();
        Enumeration<URL> urls = null;
        try {
            urls = classLoader.getResources(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        URL resourceUrl = urls.nextElement();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
