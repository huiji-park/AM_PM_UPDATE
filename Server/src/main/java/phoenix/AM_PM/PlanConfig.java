
package phoenix.AM_PM;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PlanConfig implements WebMvcConfigurer {
    //@Value("${app.file.storage.mapping}")
    String currentDirectory = System.getProperty("user.dir");
    // String location="file:///c:/kosastudy/springedu/src/main/resources/static/images/";
    String correctedPath = currentDirectory.replace("\\", "/");
    String location = "file:///" + correctedPath + "/server/src/main/resources/static/img/plan/";

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        System.out.println("=================== : " +location);
        registry.addResourceHandler("/img/plan/**").addResourceLocations(location);
    }
}


//Hospital information