package com.github.petruki.playground;

import java.sql.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.framework.exceptions.DBConnectionException;
import com.github.petruki.playground.dao.Entity;
import com.github.petruki.playground.dao.EntityRepository;

public class AppSample {
	
	private static final Logger logger = LogManager.getLogger(AppSample.class);

	public static void main(String[] args) {
		try {
			new AppSample().init();
		} catch (DBConnectionException e) {
			logger.error(e);
		}
	}
	
	private void init() throws DBConnectionException {
		Entity ent1 = new Entity(1, "John", new Date(System.currentTimeMillis()));
		ent1.setSalary(1000f);
		EntityRepository dao = new EntityRepository();
		
		dao.insert(ent1);
		ent1 = dao.queryById(1);
		System.out.println(ent1.getName() + " - " + ent1.getSalary());
		
		List<Entity> list = dao.query("name", "J%");
		list.forEach(System.out::println);
		
		dao.deleteById(1);
	}

}
