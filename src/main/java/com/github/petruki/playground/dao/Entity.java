package com.github.petruki.playground.dao;

import java.sql.Date;

import com.github.petruki.framework.AbstractEntity;
import com.github.petruki.framework.annotations.Column;
import com.github.petruki.framework.annotations.Table;

@Table(name = "entity")
public class Entity extends AbstractEntity<Entity>  {
	
	@Column(name = "id")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "date")
	private Date date;
	
	@Column(name = "salary")
	private Float salary;
	
	public Entity() {}

	public Entity(int id, String name, Date date) {
		this.id = id;
		this.name = name;
		this.date = date;
	}
	
	@Override
	public Entity newInstance() {
		return new Entity();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Float getSalary() {
		return salary;
	}

	public void setSalary(Float salary) {
		this.salary = salary;
	}
	
	@Override
	public String toString() {
		return "Entity [id=" + id + ", name=" + name + ", date=" + date + ", salary=" + salary + "]";
	}
	
}
