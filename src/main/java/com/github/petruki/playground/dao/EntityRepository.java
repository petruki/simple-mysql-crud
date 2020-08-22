package com.github.petruki.playground.dao;

import com.github.petruki.framework.AbstractCrudRepository;

public class EntityRepository extends AbstractCrudRepository<Entity> {
	
	public EntityRepository() {
		super(new Entity());
	}

}
