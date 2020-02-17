package ru.amalnev;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SpringDataRepositoryScanner {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    private Map<Class<?>, CrudRepository> repositoryMap = new HashMap<>();

    @EventListener
    public void processContextRefresh(ContextRefreshedEvent contextRefreshedEvent) {
        final ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        for (final String beanName : applicationContext.getBeanDefinitionNames()) {
            final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            final String beanScope = beanDefinition.getScope();

            if (beanScope == null ||
                beanScope.equals(ConfigurableBeanFactory.SCOPE_PROTOTYPE) ||
                beanScope.equals("session"))
                continue;

            final Object bean = applicationContext.getBean(beanName);
            if (bean instanceof CrudRepository) {
                for (final Class<?> interfaceClass : AopProxyUtils.proxiedUserInterfaces(bean)) {
                    if (CrudRepository.class.isAssignableFrom(interfaceClass) && interfaceClass.getGenericInterfaces().length > 0) {
                        Type[] interfaceTypeArguments = ((ParameterizedType) interfaceClass.getGenericInterfaces()[0]).getActualTypeArguments();
                        if (interfaceTypeArguments.length == 2) {
                            repositoryMap.put((Class<?>) interfaceTypeArguments[0], (CrudRepository) bean);
                        }
                    }
                }
            }
        }
    }

    public Collection<String> getEntityClassNames() {
        return repositoryMap
                .keySet()
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
    }

    public Class<?> getEntityClass(String entityClassName) {
        return repositoryMap
                .keySet()
                .stream()
                .filter(entityClass -> entityClass.getSimpleName().equals(entityClassName))
                .findFirst()
                .orElse(null);
    }

    CrudRepository getRepository(String entityClassName) {
        return repositoryMap
                .keySet()
                .stream()
                .filter(entityClass -> entityClass.getSimpleName().equals(entityClassName))
                .map(entityClass -> repositoryMap.get(entityClass))
                .findFirst()
                .orElse(null);
    }
}
