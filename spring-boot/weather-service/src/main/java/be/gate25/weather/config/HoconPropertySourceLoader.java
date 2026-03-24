package be.gate25.weather.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HoconPropertySourceLoader implements PropertySourceLoader {

    @Override
    public String[] getFileExtensions() {
        return new String[]{"conf"};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws java.io.IOException {
        if (!resource.exists()) return List.of();

        Config cfg = ConfigFactory.parseReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        ).resolve();

        Properties props = new Properties();
        for (Map.Entry<String, ConfigValue> e : cfg.entrySet()) {
            Object v = e.getValue().unwrapped();
            props.put(e.getKey(), stringify(v));
        }
        return List.of(new PropertiesPropertySource(name, props));
    }

    private static String stringify(Object v) {
        if (v == null) return "";
        if (v instanceof List<?> list) {
            // Spring binder accepte les listes en CSV
            return list.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        }
        return String.valueOf(v);
    }
}