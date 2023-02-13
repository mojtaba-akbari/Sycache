package org.SyCache;

import org.SyCache.Services.MiddleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.InvocationTargetException;

@SpringBootApplication
public class SyCacheApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SyCacheApplication.class, args);
	}

	@Override
	public void run(String... args) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

	}
}
